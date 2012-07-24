package frontend;

import inc.GMTYPE_T;
import inc.GM_REDUCE_T;

import java.util.Iterator;
import java.util.LinkedList;

import ast.AST_NODE_TYPE;
import ast.ast_assign;
import ast.ast_bfs;
import ast.ast_expr;
import ast.ast_field;
import ast.ast_foreach;
import ast.ast_id;
import ast.ast_if;
import ast.ast_node;
import ast.ast_return;
import ast.ast_sent;
import ast.ast_typedecl;
import ast.ast_while;

import common.GM_ERRORS_AND_WARNINGS;
import common.GlobalMembersGm_error;
import common.GlobalMembersGm_misc;
import common.gm_apply;

//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define TO_STR(X) #X
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define DEF_STRING(X) static const char *X = "X"
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP(CLASS, DESC) class CLASS : public gm_compile_step { private: CLASS() {set_description(DESC);}public: virtual void process(ast_procdef*p); virtual gm_compile_step* get_instance(){return new CLASS();} static gm_compile_step* get_factory(){return new CLASS();} };
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP_FACTORY(CLASS) CLASS::get_factory()
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define AUX_INFO(X,Y) "X"":""Y"
///#define GM_BLTIN_MUTATE_GROW 1
///#define GM_BLTIN_MUTATE_SHRINK 2
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_BLTIN_FLAG_TRUE true

//----------------------------------------------------------------
// Type-check Step 5: 
//   Check type between LHS and RHS
//   Check filter/cond types
//   Check argmin/argmax assignment
//       - LHS should either have same driver (e.g <n.A; n.B> min= <... ; ... > )
//       - or should be all scalar            (e.g <x; y, z>  max= <... ; ... > )
//----------------------------------------------------------------

// resolve type of every sub-expression
public class gm_typechecker_stage_5 extends gm_apply {
	public gm_typechecker_stage_5() {
		_is_okay = true;
		set_for_sent(true);
		ret = null;
	}

	public final void set_return_type(ast_typedecl t) {
		ret = t;
	}

	// post apply
	@Override
	public boolean apply(ast_sent s) {
		boolean okay = true;
		switch (s.get_nodetype()) {
		case AST_IF: {
			ast_if i = (ast_if) s;
			okay = should_be_boolean(i.get_cond());
			break;
		}
		case AST_WHILE: {
			ast_while w = (ast_while) s;
			okay = should_be_boolean(w.get_cond());
			break;
		}
		case AST_FOREACH: {
			ast_foreach fe = (ast_foreach) s;
			if (fe.get_filter() != null) {
				okay = should_be_boolean(fe.get_filter());
			}
			break;
		}
		case AST_BFS: {
			ast_bfs bfs = (ast_bfs) s;
			if (bfs.get_navigator() != null) {
				okay = should_be_boolean(bfs.get_navigator()) && okay;
			}
			if (bfs.get_f_filter() != null) {
				okay = should_be_boolean(bfs.get_f_filter()) && okay;
			}
			if (bfs.get_b_filter() != null) {
				okay = should_be_boolean(bfs.get_b_filter()) && okay;
			}
			break;
		}

		case AST_RETURN: {
			ast_return r = (ast_return) s;
			GMTYPE_T summary_lhs = ret.getTypeSummary();
			if (summary_lhs.is_void_type())
				break;

			if (r.get_expr() == null) {
				GlobalMembersGm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_RETURN_MISMATCH, r.get_line(), r.get_col(),
						GlobalMembersGm_misc.gm_get_type_string(summary_lhs), GlobalMembersGm_misc.gm_get_type_string(GMTYPE_T.GMTYPE_VOID));
				break;
			}

			GMTYPE_T summary_rhs = r.get_expr().get_type_summary();

			boolean warn;
			int coed;
			if (!GlobalMembersGm_typecheck.gm_is_compatible_type_for_assign(summary_lhs, summary_rhs, coed, warn)) {
				GlobalMembersGm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_RETURN_MISMATCH, r.get_line(), r.get_col(),
						GlobalMembersGm_misc.gm_get_type_string(summary_lhs), GlobalMembersGm_misc.gm_get_type_string(summary_rhs));

				okay = false;
			}

			if (warn && summary_lhs.is_prim_type()) {
				System.out.printf("warning: adding type conversion %s->%s\n", GlobalMembersGm_misc.gm_get_type_string(summary_rhs),
						GlobalMembersGm_misc.gm_get_type_string(summary_lhs));
				coercion_targets.put(r.get_expr(), summary_lhs);
			}
			break;
		}

		case AST_ASSIGN: {
			okay = check_assign((ast_assign) s);
			break;
		}

		default:
			break;
		}

		set_okay(okay);
		return okay;
	}

	public final boolean check_assign_lhs_rhs(ast_node lhs, ast_expr rhs, int l, int c) {
		GMTYPE_T summary_lhs;
		GMTYPE_T summary_rhs;
		ast_typedecl lhs_typedecl = null;
		gm_symtab_entry l_sym = null;

		if (lhs.get_nodetype() == AST_NODE_TYPE.AST_ID) {
			ast_id l2 = (ast_id) lhs;
			summary_lhs = l2.getTypeSummary();

			if (l2.getTypeInfo().has_target_graph()) {
				l_sym = l2.getTypeInfo().get_target_graph_sym();
			}

			if (!l2.getSymInfo().isWriteable()) {
				GlobalMembersGm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_READONLY, l2);
				return false;
			}
		} else {
			// target type (e.g. N_P<Int> -> Int)
			ast_field f = (ast_field) lhs;
			summary_lhs = f.get_second().getTargetTypeSummary();

			if (f.getTargetTypeInfo().has_target_graph()) {
				l_sym = f.getTargetTypeInfo().get_target_graph_sym();
			}
		}

		// check assignable
		summary_rhs = rhs.get_type_summary();

		boolean warn;
		int coed;
		if (!GlobalMembersGm_typecheck.gm_is_compatible_type_for_assign(summary_lhs, summary_rhs, coed, warn)) {
			GlobalMembersGm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_ASSIGN_TYPE_MISMATCH, l, c,
					GlobalMembersGm_misc.gm_get_type_string(summary_lhs), GlobalMembersGm_misc.gm_get_type_string(summary_rhs));
			return false;
		}
		if (warn && summary_lhs.is_prim_type()) {
			System.out.printf("warning: adding type conversion %s->%s\n", GlobalMembersGm_misc.gm_get_type_string(summary_rhs),
					GlobalMembersGm_misc.gm_get_type_string(summary_lhs));
			coercion_targets.put(rhs, summary_lhs);
		}

		if (summary_lhs.has_target_graph_type()) {
			gm_symtab_entry r_sym = rhs.get_bound_graph();
			assert l_sym != null;
			if (r_sym == null) {
				assert summary_rhs.is_nil_type() || summary_rhs.is_foreign_expr_type();
			} else {
				if (l_sym != r_sym) {
					GlobalMembersGm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_TARGET_MISMATCH, l, c);
					return false;
				}
			}
		}

		return true;
	}

	public final boolean check_assign(ast_assign a) {
		boolean okay;
		int l = a.get_line();
		int c = a.get_col();
		GMTYPE_T summary_lhs;
		if (a.is_target_scalar()) {
			okay = check_assign_lhs_rhs(a.get_lhs_scala(), a.get_rhs(), l, c);
			summary_lhs = a.get_lhs_scala().getTypeSummary();
		} else {
			okay = check_assign_lhs_rhs(a.get_lhs_field(), a.get_rhs(), l, c);
			summary_lhs = a.get_lhs_field().get_second().getTargetTypeSummary();
		}

		// check body of reduce
		if (a.is_reduce_assign()) {

			GMTYPE_T summary_rhs = a.get_rhs().get_type_summary();
			// SUM/MULT/MAX/MIN ==> numeirc
			// AND/OR ==> boolean
			GM_REDUCE_T reduce_op = a.get_reduce_type();
			if (reduce_op.is_numeric_reduce_op()) {
				if (!summary_lhs.is_numeric_type()) {
					GlobalMembersGm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_REQUIRE_NUMERIC_REDUCE, l, c);
					return false;
				}
			} else if (reduce_op.is_boolean_reduce_op()) {
				if (!summary_lhs.is_boolean_type()) {
					GlobalMembersGm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_REQUIRE_BOOLEAN_REDUCE, l, c);
					return false;
				}
			}
		}

		if (a.is_argminmax_assign()) {
			boolean okay = true;
			LinkedList<ast_node> L = a.get_lhs_list();
			LinkedList<ast_expr> R = a.get_rhs_list();

			Iterator<ast_node> I;
			Iterator<ast_expr> J;
			for (I = L.iterator(), J = R.iterator(); I.hasNext();) {
				ast_node n = I.next();
				ast_expr e = J.next();
				boolean b = check_assign_lhs_rhs(n, e, n.get_line(), n.get_col());
				okay = b && okay;
			}

			if (!okay)
				return false;
		}

		return okay;
	}

	public final boolean should_be_boolean(ast_expr e) {
		if (!e.get_type_summary().is_boolean_type()) {
			GlobalMembersGm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_NEED_BOOLEAN, e.get_line(), e.get_col());
			return false;
		}
		return true;
	}

	public final void set_okay(boolean b) {
		_is_okay = b && _is_okay;
	}

	public final boolean is_okay() {
		return _is_okay;
	}

	private boolean _is_okay;
	private ast_typedecl ret;

	public java.util.HashMap<ast_expr, GMTYPE_T> coercion_targets = new java.util.HashMap<ast_expr, GMTYPE_T>();
}