package frontend;

import static ast.ast_node_type.AST_MAPACCESS;
import static common.gm_errors_and_warnings.GM_ERROR_KEY_MISSMATCH;
import static common.gm_errors_and_warnings.GM_ERROR_TARGET_MISMATCH;
import static inc.gm_type.GMTYPE_NORDER;
import static inc.gm_type.GMTYPE_NSEQ;
import static inc.gm_type.GMTYPE_NSET;
import inc.gm_reduce;
import inc.gm_type;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import tangible.RefObject;
import ast.ast_assign;
import ast.ast_bfs;
import ast.ast_expr;
import ast.ast_expr_mapaccess;
import ast.ast_field;
import ast.ast_foreach;
import ast.ast_id;
import ast.ast_if;
import ast.ast_mapaccess;
import ast.ast_maptypedecl;
import ast.ast_node;
import ast.ast_node_type;
import ast.ast_return;
import ast.ast_sent;
import ast.ast_typedecl;
import ast.ast_while;

import common.gm_apply;
import common.gm_error;
import common.gm_errors_and_warnings;

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

	public final Map<ast_expr, gm_type> coercion_targets = new TreeMap<ast_expr, gm_type>();

	private boolean _is_okay = true;
	private ast_typedecl ret = null;

	public gm_typechecker_stage_5() {
		set_for_sent(true);
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
			gm_type summary_lhs = ret.getTypeSummary();
			if (summary_lhs.is_void_type())
				break;

			if (r.get_expr() == null) {
				gm_error.gm_type_error(gm_errors_and_warnings.GM_ERROR_RETURN_MISMATCH, r.get_line(), r.get_col(), summary_lhs.get_type_string(),
						gm_type.GMTYPE_VOID.get_type_string());
				break;
			}

			gm_type summary_rhs = r.get_expr().get_type_summary();

			RefObject<gm_type> coed_ref = new RefObject<gm_type>(null);
			RefObject<Boolean> warn_ref = new RefObject<Boolean>(null);
			boolean test = gm_typecheck.gm_is_compatible_type_for_assign(summary_lhs, summary_rhs, coed_ref, warn_ref);
			boolean warn = warn_ref.argvalue;
			if (!test) {
				gm_error.gm_type_error(gm_errors_and_warnings.GM_ERROR_RETURN_MISMATCH, r.get_line(), r.get_col(), summary_lhs.get_type_string(),
						summary_rhs.get_type_string());

				okay = false;
			}

			if (warn && summary_lhs.is_prim_type()) {
				System.out.printf("warning: adding type conversion %s->%s\n", summary_rhs.get_type_string(), summary_lhs.get_type_string());
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

	private gm_type tryResolveIfUnknown(gm_type type) {
		switch (type) {
		case GMTYPE_PROPERTYITER_SET:
			return GMTYPE_NSET;
		case GMTYPE_PROPERTYITER_SEQ:
			return GMTYPE_NSEQ;
		case GMTYPE_PROPERTYITER_ORDER:
			return GMTYPE_NORDER;
		default:
			break;
		}
		return type;
	}

	public final boolean check_assign_lhs_rhs(ast_node lhs, ast_expr rhs, int l, int c) {
		gm_type summary_lhs;
		gm_type summary_rhs;
		gm_symtab_entry l_sym = null;

		if (lhs.get_nodetype() == ast_node_type.AST_ID) {
			ast_id l2 = (ast_id) lhs;
			summary_lhs = l2.getTypeSummary();

			if (l2.getTypeInfo().has_target_graph()) {
				l_sym = l2.getTypeInfo().get_target_graph_sym();
			}

			if (!l2.getSymInfo().isWriteable()) {
				gm_error.gm_type_error(gm_errors_and_warnings.GM_ERROR_READONLY, l2);
				return false;
			}
		} else if (lhs.get_nodetype() == AST_MAPACCESS) {
			ast_mapaccess mapAccess = (ast_mapaccess) lhs;
			ast_maptypedecl mapDecl = (ast_maptypedecl) mapAccess.get_map_id().getTypeInfo();
			l_sym = mapAccess.get_bound_graph_for_value();
			summary_lhs = mapDecl.getValueTypeSummary();

			gm_type keyType = mapDecl.getKeyTypeSummary();
			gm_type keyExprType = mapAccess.get_key_expr().get_type_summary();
			RefObject<Boolean> warningRef = new RefObject<Boolean>(false);
			boolean isOkay = gm_type.gm_is_compatible_type_for_assign(keyType, keyExprType, new RefObject<gm_type>(null), warningRef);
			if (!isOkay) {
				gm_error.gm_type_error(GM_ERROR_KEY_MISSMATCH, l, c, keyType.get_type_string(), keyExprType.get_type_string());
				return false;
			} else if (warningRef.argvalue) {
				System.out.printf("warning: implicit type conversion %s->%s\n", keyType.get_type_string(), keyExprType.get_type_string());
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
		summary_rhs = tryResolveIfUnknown(summary_rhs);

		RefObject<gm_type> coed_ref = new RefObject<gm_type>(null);
		RefObject<Boolean> warn_ref = new RefObject<Boolean>(null);
		boolean test = gm_typecheck.gm_is_compatible_type_for_assign(summary_lhs, summary_rhs, coed_ref, warn_ref);
		boolean warn = warn_ref.argvalue;
		if (!test) {
			gm_error.gm_type_error(gm_errors_and_warnings.GM_ERROR_ASSIGN_TYPE_MISMATCH, l, c, summary_lhs.get_type_string(), summary_rhs.get_type_string());
			return false;
		}
		if (warn && summary_lhs.is_prim_type()) {
			System.out.printf("warning: adding type conversion %s->%s\n", summary_rhs.get_type_string(), summary_lhs.get_type_string());
			coercion_targets.put(rhs, summary_lhs);
		}

		if (summary_lhs.has_target_graph_type()) {
			gm_symtab_entry r_sym;
			if (rhs.is_mapaccess()) {
				ast_mapaccess mapAccess = ((ast_expr_mapaccess) rhs).get_mapaccess();
				r_sym = mapAccess.get_bound_graph_for_value();
			} else {
				r_sym = rhs.get_bound_graph();
			}
			return checkGraphs(l_sym, r_sym, summary_rhs, l, c);
		}

		return true;
	}

	private boolean checkGraphs(gm_symtab_entry l_sym, gm_symtab_entry r_sym, gm_type summary_rhs, int line, int column) {
		assert (l_sym != null);
		if (r_sym == null) {
			assert (summary_rhs.is_nil_type() || summary_rhs.is_foreign_expr_type());
		} else {
			if (l_sym != r_sym) {
				gm_error.gm_type_error(GM_ERROR_TARGET_MISMATCH, line, column);
				return false;
			}
		}
		return true;
	}

	public final boolean check_assign(ast_assign a) {
		boolean okay;
		int l = a.get_line();
		int c = a.get_col();
		gm_type summary_lhs;
		if (a.is_target_scalar()) {
			okay = check_assign_lhs_rhs(a.get_lhs_scala(), a.get_rhs(), l, c);
			summary_lhs = a.get_lhs_scala().getTypeSummary();
		} else if (a.is_target_map_entry()) {
			ast_mapaccess mapAccess = a.to_assign_mapentry().get_lhs_mapaccess();
			okay = check_assign_lhs_rhs(mapAccess, a.get_rhs(), l, c);
			ast_maptypedecl mapDecl = (ast_maptypedecl) mapAccess.get_map_id().getTypeInfo();
			summary_lhs = mapDecl.getValueTypeSummary();
		} else {
			okay = check_assign_lhs_rhs(a.get_lhs_field(), a.get_rhs(), l, c);
			summary_lhs = a.get_lhs_field().get_second().getTargetTypeSummary();
		}

		// check body of reduce
		if (a.is_reduce_assign()) {

			a.get_rhs().get_type_summary();
			// SUM/MULT/MAX/MIN ==> numeirc
			// AND/OR ==> boolean
			gm_reduce reduce_op = a.get_reduce_type();
			if (reduce_op.is_numeric_reduce_op()) {
				if (!summary_lhs.is_numeric_type()) {
					gm_error.gm_type_error(gm_errors_and_warnings.GM_ERROR_REQUIRE_NUMERIC_REDUCE, l, c);
					return false;
				}
			} else if (reduce_op.is_boolean_reduce_op()) {
				if (!summary_lhs.is_boolean_type()) {
					gm_error.gm_type_error(gm_errors_and_warnings.GM_ERROR_REQUIRE_BOOLEAN_REDUCE, l, c);
					return false;
				}
			}
		}

		if (a.is_argminmax_assign()) {
			boolean okay1 = true;
			LinkedList<ast_node> L = a.get_lhs_list();
			LinkedList<ast_expr> R = a.get_rhs_list();

			Iterator<ast_node> I;
			Iterator<ast_expr> J;
			for (I = L.iterator(), J = R.iterator(); I.hasNext();) {
				ast_node n = I.next();
				ast_expr e = J.next();
				boolean b = check_assign_lhs_rhs(n, e, n.get_line(), n.get_col());
				okay1 = b && okay1;
			}

			if (!okay1)
				return false;
		}

		return okay;
	}

	public final boolean should_be_boolean(ast_expr e) {
		if (!e.get_type_summary().is_boolean_type()) {
			gm_error.gm_type_error(gm_errors_and_warnings.GM_ERROR_NEED_BOOLEAN, e.get_line(), e.get_col());
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

}