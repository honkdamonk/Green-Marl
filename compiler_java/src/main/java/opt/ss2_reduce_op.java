package opt;

import static inc.GMTYPE_T.GMTYPE_DOUBLE;
import static inc.GMTYPE_T.GMTYPE_FLOAT;
import static inc.GMTYPE_T.GMTYPE_LONG;
import static opt.GlobalMembersGm_syntax_sugar2.OPT_FLAG_NESTED_REDUCTION;
import static opt.GlobalMembersGm_syntax_sugar2.OPT_SB_NESTED_REDUCTION_SCOPE;
import static opt.GlobalMembersGm_syntax_sugar2.OPT_SYM_NESTED_REDUCTION_BOUND;
import static opt.GlobalMembersGm_syntax_sugar2.OPT_SYM_NESTED_REDUCTION_TARGET;
import static opt.GlobalMembersGm_syntax_sugar2.check_has_nested;
import static opt.GlobalMembersGm_syntax_sugar2.find_count_function;
import static opt.GlobalMembersGm_syntax_sugar2.insert_def_and_init_before;
import static opt.GlobalMembersGm_syntax_sugar2.replace_avg_to_varaible;
import frontend.gm_symtab_entry;
import inc.GMTYPE_T;
import inc.GM_OPS_T;
import inc.GM_REDUCE_T;
import inc.gm_assignment_t;

import java.util.LinkedList;

import tangible.RefObject;
import ast.ast_assign;
import ast.ast_expr;
import ast.ast_expr_builtin;
import ast.ast_expr_reduce;
import ast.ast_foreach;
import ast.ast_id;
import ast.ast_if;
import ast.ast_node;
import ast.ast_sent;
import ast.ast_sentblock;

import common.GlobalMembersGm_add_symbol;
import common.GlobalMembersGm_main;
import common.GlobalMembersGm_new_sents_after_tc;
import common.GlobalMembersGm_resolve_nc;
import common.GlobalMembersGm_transform_helper;
import common.gm_apply;
import common.gm_builtin_def;
import common.gm_method_id_t;

/**
 * --------------------------------------------------- reduction_op =>
 * initialization + foreach + reduction_assign <e.g> X = Y + Sum(t: G.Nbrs) {t.A
 * + t.B} => Int _t =0; Foreach(t: G.Nbrs) _t += t.A + t.B @ t; X = Y + _t;
 * ---------------------------------------------------
 */
public class ss2_reduce_op extends gm_apply {

	/** ReduceOps that should be replaced */
	protected LinkedList<ast_expr_reduce> targets = new LinkedList<ast_expr_reduce>();

	public ss2_reduce_op() {
		set_for_expr(true);
	}

	// Pre visit.
	// Resolve nested 'reductions' from outside
	@Override
	public boolean apply(ast_expr s) {
		if (!s.is_reduction())
			return true;

		ast_expr_reduce r = (ast_expr_reduce) s;
		targets.addLast(r);
		return true;
	}

	public final void post_process() {
		for (ast_expr_reduce reduce : targets) {
			post_process_body(reduce);
		}
	}

	// replace selected expressions.
	protected final void post_process_body(ast_expr_reduce target) {

		GMTYPE_T expr_type = target.get_body().get_type_summary();
		// true if nested
		boolean is_nested = target.find_info_bool(OPT_FLAG_NESTED_REDUCTION);
		GM_REDUCE_T rtype = target.get_reduce_type();
		boolean is_avg = (rtype == GM_REDUCE_T.GMREDUCE_AVG);

		RefObject<ast_expr_reduce> left_nested_ref = new RefObject<ast_expr_reduce>(null);
		RefObject<ast_expr_reduce> right_nested_ref = new RefObject<ast_expr_reduce>(null);
		RefObject<Boolean> tempRef_has_other_rhs = new RefObject<Boolean>(false);
		boolean has_nested = check_has_nested(target.get_body(), rtype, tempRef_has_other_rhs, left_nested_ref, right_nested_ref);
		boolean has_other_rhs = tempRef_has_other_rhs.argvalue;
		ast_expr_reduce left_nested = left_nested_ref.argvalue;
		ast_expr_reduce right_nested = right_nested_ref.argvalue;

		ast_sent holder = null;
		ast_sentblock nested_up_sentblock = null;

		if (is_nested) {
			nested_up_sentblock = (ast_sentblock) target.find_info_ptr(OPT_SB_NESTED_REDUCTION_SCOPE);
			assert nested_up_sentblock != null;
		} else {
			ast_node up = target.get_parent();
			while (true) {
				assert up != null;
				if (up.is_sentence())
					break;
				up = up.get_parent();
			}
			holder = (ast_sent) up; // sentence that holds this rhs expression
		}

		// -------------------------------------------------
		// 1. add lhs_var decleation
		// 2. create reduce_assign
		// 3. create foreach
		// 4. fixup symtab entries
		// 5. replace target expr in the holder
		// 6. (for average) insert final computation
		// -------------------------------------------------

		gm_symtab_entry lhs_symbol = null;
		; // target temp variable;
		gm_symtab_entry bound_sym = null;
		gm_symtab_entry cnt_symbol = null;
		gm_symtab_entry avg_val_symbol = null;

		boolean need_count_for_avg = false;
		if (is_nested) {
			// ------------------------------------------------
			// If nested, no need to create initializer or lhs symbol
			// ------------------------------------------------
			lhs_symbol = (gm_symtab_entry) target.find_info_ptr(OPT_SYM_NESTED_REDUCTION_TARGET);
			assert lhs_symbol != null;
		} else {
			// -------------------------------------------------
			// Need to create initialization
			// -------------------------------------------------

			// 1.1 variable name
			String t_name_base = "";
			switch (rtype) {
			case GMREDUCE_AVG: // go through
			case GMREDUCE_PLUS:
				t_name_base = "_S";
				break; // Sum
			case GMREDUCE_MULT:
				t_name_base = "_P";
				break; // Product
			case GMREDUCE_MIN:
				t_name_base = "_Mn";
				break; // Min
			case GMREDUCE_MAX:
				t_name_base = "_Mx";
				break; // Max
			case GMREDUCE_AND:
				t_name_base = "_A";
				break;
			case GMREDUCE_OR:
				t_name_base = "_E";
				break;
			default:
				assert false;
				break;
			}

			// FIXME: was need_count_for_avg - seems to be a bug in the cpp
			// compiler
			boolean need_count_for_avg1 = false;
			if (is_avg) {
				rtype = GM_REDUCE_T.GMREDUCE_PLUS; // Need sum

				need_count_for_avg1 = true;
				if (target.get_filter() == null) {
					GMTYPE_T iter_type = target.get_iter_type();
					GMTYPE_T src_type = target.get_source().getTypeInfo().getTypeSummary();
					if (find_count_function(src_type, iter_type) == gm_method_id_t.GM_BLTIN_END)
						need_count_for_avg1 = true;
					else
						need_count_for_avg1 = false;
				}
			}

			// 1.2 initial value
			assert expr_type.is_prim_type();
			ast_expr init_val = GlobalMembersGm_new_sents_after_tc.gm_new_bottom_symbol(rtype, expr_type);

			// 1.3 add init
			String temp_name = GlobalMembersGm_main.FE.voca_temp_name(t_name_base);
			assert holder != null;
			lhs_symbol = insert_def_and_init_before(temp_name, expr_type, holder, init_val);

			if (is_avg) {
				String temp_cnt = GlobalMembersGm_main.FE.voca_temp_name("_cnt");
				String temp_avg = GlobalMembersGm_main.FE.voca_temp_name("_avg");
				ast_sentblock sb = (ast_sentblock) holder.get_parent();

				cnt_symbol = insert_def_and_init_before(temp_cnt, GMTYPE_LONG, holder, ast_expr.new_ival_expr(0));

				avg_val_symbol = GlobalMembersGm_add_symbol.gm_add_new_symbol_primtype(sb, (expr_type == GMTYPE_FLOAT) ? GMTYPE_FLOAT : GMTYPE_DOUBLE,
						new RefObject<String>(temp_avg));
			}
		}

		// ----------------------------------------------------
		// 2. Filter & Body of Reduciton ==> reduce assign + if
		// ----------------------------------------------------
		// 2.1. rip-off body
		ast_expr body = target.get_body();
		target.set_body(null);
		GlobalMembersGm_transform_helper.gm_ripoff_upper_scope(body);
		body.set_up_op(null);

		// 2.2. new assignment state (as for the body of for-each)
		ast_sent foreach_body = null;
		ast_assign r_assign = null;
		ast_sentblock nested_sentblock = null;

		ast_id old_iter = target.get_iterator();
		ast_id lhs_id = null;
		ast_id bound_id = null;
		ast_id bound_id2 = null;

		lhs_id = lhs_symbol.getId().copy(true);
		if (is_nested) {
			bound_sym = (gm_symtab_entry) target.find_info_ptr(OPT_SYM_NESTED_REDUCTION_BOUND);
			assert bound_sym != null;
			bound_id = bound_sym.getId().copy(true);
		} else {
			bound_sym = null; // will set later
			bound_id = old_iter.copy(false); // dummy value;
		}

		if (!has_nested) {
			r_assign = ast_assign.new_assign_scala(lhs_id, body, gm_assignment_t.GMASSIGN_REDUCE, bound_id, rtype);
			foreach_body = r_assign;

			if (need_count_for_avg) {
				ast_sentblock sb = ast_sentblock.new_sentblock();
				// symInfo is correct for LHS
				// FIXME: was lhs_id - seems to be a bug in the cpp gm-compiler
				ast_id lhs_id1 = cnt_symbol.getId().copy(true);
				bound_id2 = old_iter.copy(false); // symInfo not available yet
				ast_assign r_assign2 = ast_assign.new_assign_scala(lhs_id1, ast_expr.new_ival_expr(1), gm_assignment_t.GMASSIGN_REDUCE, bound_id2,
						GM_REDUCE_T.GMREDUCE_PLUS);

				GlobalMembersGm_transform_helper.gm_insert_sent_end_of_sb(sb, r_assign);
				GlobalMembersGm_transform_helper.gm_insert_sent_end_of_sb(sb, r_assign2);

				foreach_body = sb;
			}
		} // has_nested
		else {
			nested_sentblock = ast_sentblock.new_sentblock();
			foreach_body = nested_sentblock;
			if (has_other_rhs) {
				ast_expr left = body.get_left_op();
				ast_expr right = body.get_right_op();
				ast_expr r_assign_body = (left_nested == left) ? right : left;
				r_assign_body.set_up_op(null);
				r_assign = ast_assign.new_assign_scala(lhs_id, r_assign_body, gm_assignment_t.GMASSIGN_REDUCE, bound_id, rtype);
				GlobalMembersGm_transform_helper.gm_insert_sent_end_of_sb(nested_sentblock, r_assign);
			}
		}

		ast_expr filter = target.get_filter();
		if (filter != null) {
			target.set_filter(null);
			GlobalMembersGm_transform_helper.gm_ripoff_upper_scope(filter);

			ast_if iff = ast_if.new_if(filter, foreach_body, null);
			foreach_body = iff;
			assert filter.get_parent() != null;
		}

		// -------------------------------------------------
		// 3. Create foreach
		// -------------------------------------------------
		// 3.1 create foreach
		ast_id foreach_it = old_iter.copy();
		ast_id foreach_src = target.get_source().copy(true); // copy SymInfo as
																// well
		ast_id foreach_src2 = target.get_source2();
		if (foreach_src2 != null)
			foreach_src2 = foreach_src2.copy(true);

		GMTYPE_T iter_type = target.get_iter_type();

		// see common/new_sent_after_tc.cc
		ast_foreach fe_new = GlobalMembersGm_new_sents_after_tc.gm_new_foreach_after_tc(foreach_it, foreach_src, foreach_body, iter_type);
		fe_new.set_source2(foreach_src2); // xxx: what was this again?

		// 3.2 add foreach
		if (!is_nested) {
			GlobalMembersGm_transform_helper.gm_add_sent_before(holder, fe_new);
		} else {
			assert nested_up_sentblock != null;
			GlobalMembersGm_transform_helper.gm_insert_sent_end_of_sb(nested_up_sentblock, fe_new);
		}

		// -------------------------------------------------
		// 4. Fix-up symtabs entries in the assign
		// -------------------------------------------------
		// 4.1 bound symbols
		assert foreach_it.getSymInfo() != null;
		if (!is_nested) {
			bound_id.setSymInfo(foreach_it.getSymInfo());
			if (bound_id2 != null) // for average
				bound_id2.setSymInfo(foreach_it.getSymInfo());
			bound_sym = foreach_it.getSymInfo();
		}

		// 4.2 replace every iterator (symbol) in the body_expression with the
		// new foreach iterator
		GlobalMembersGm_resolve_nc.gm_replace_symbol_entry(old_iter.getSymInfo(), foreach_it.getSymInfo(), foreach_body);
		if (has_nested)
			GlobalMembersGm_resolve_nc.gm_replace_symbol_entry(old_iter.getSymInfo(), foreach_it.getSymInfo(), body);

		// ----------------------------------------------
		// 5. replace <Sum(..){}> with <lhs_var>
		// ----------------------------------------------
		if (!is_nested) {
			replace_avg_to_varaible(holder, target, (is_avg) ? avg_val_symbol : lhs_symbol);
		}

		// ----------------------------------------------
		// 6. For average
		// ----------------------------------------------
		if (is_avg) {

			GMTYPE_T result_type = (expr_type == GMTYPE_FLOAT) ? GMTYPE_FLOAT : GMTYPE_DOUBLE;
			// (cnt_symbol == 0)? 0 : sum_val / (float) cnt_symbol
			ast_expr zero1 = ast_expr.new_ival_expr(0);
			ast_expr zero2 = ast_expr.new_fval_expr(0);
			ast_expr cnt1 = ast_expr.new_id_expr(cnt_symbol.getId().copy(true));
			ast_expr cnt2 = ast_expr.new_id_expr(cnt_symbol.getId().copy(true));
			ast_expr sum = ast_expr.new_id_expr(lhs_symbol.getId().copy(true));
			ast_expr comp = ast_expr.new_comp_expr(GM_OPS_T.GMOP_EQ, zero1, cnt1);
			ast_expr t_conv = ast_expr.new_typeconv_expr(result_type, cnt2);
			ast_expr div = ast_expr.new_biop_expr(GM_OPS_T.GMOP_DIV, sum, t_conv);
			div.set_type_summary(result_type);
			ast_expr ter = ast_expr.new_ternary_expr(comp, zero2, div);

			ast_assign a = ast_assign.new_assign_scala(avg_val_symbol.getId().copy(true), ter);

			GlobalMembersGm_transform_helper.gm_add_sent_after(fe_new, a);

			if (!need_count_for_avg) {
				GMTYPE_T iter_type1 = target.get_iter_type();
				GMTYPE_T src_type = target.get_source().getTypeSummary();
				gm_method_id_t method_id = find_count_function(src_type, iter_type1);
				assert method_id != gm_method_id_t.GM_BLTIN_END;

				// make a call to built-in funciton
				gm_builtin_def def = GlobalMembersGm_main.BUILT_IN.find_builtin_def(src_type, method_id);
				assert def != null;

				ast_expr_builtin rhs = ast_expr_builtin.new_builtin_expr(target.get_source().copy(true), def, null);
				ast_assign a1 = ast_assign.new_assign_scala(cnt_symbol.getId().copy(true), rhs);

				GlobalMembersGm_transform_helper.gm_add_sent_after(fe_new, a1);
			}
		}

		// -----------------------------------
		// propagate information for nested
		// -----------------------------------
		if (has_nested) {
			assert nested_sentblock != null;
			assert lhs_symbol != null;
			assert bound_sym != null;
			if (left_nested != null) {
				left_nested.add_info_bool(OPT_FLAG_NESTED_REDUCTION, true);
				left_nested.add_info_ptr(OPT_SB_NESTED_REDUCTION_SCOPE, nested_sentblock);
				left_nested.add_info_ptr(OPT_SYM_NESTED_REDUCTION_TARGET, lhs_symbol);
				left_nested.add_info_ptr(OPT_SYM_NESTED_REDUCTION_BOUND, bound_sym);
			}
			if (right_nested != null) {
				(right_nested).add_info_bool(OPT_FLAG_NESTED_REDUCTION, true);
				(right_nested).add_info_ptr(OPT_SB_NESTED_REDUCTION_SCOPE, nested_sentblock);
				(right_nested).add_info_ptr(OPT_SYM_NESTED_REDUCTION_TARGET, lhs_symbol);
				(right_nested).add_info_ptr(OPT_SYM_NESTED_REDUCTION_BOUND, bound_sym);
			}
		}
	}
}