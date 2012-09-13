package frontend;

import static inc.GMTYPE_T.GMTYPE_BOOL;
import static inc.GMTYPE_T.GMTYPE_INT;
import static inc.GMTYPE_T.GMTYPE_NIL_EDGE;
import static inc.GMTYPE_T.GMTYPE_NIL_NODE;
import inc.GMTYPE_T;

import java.util.LinkedList;

import ast.AST_NODE_TYPE;
import ast.ast_assign;
import ast.ast_bfs;
import ast.ast_call;
import ast.ast_expr;
import ast.ast_expr_builtin;
import ast.ast_expr_reduce;
import ast.ast_foreach;
import ast.ast_if;
import ast.ast_mapaccess;
import ast.ast_maptypedecl;
import ast.ast_return;
import ast.ast_sent;
import ast.ast_typedecl;
import ast.ast_while;

import common.gm_apply;
import common.gm_builtin_def;

public class gm_typechecker_stage_4 extends gm_apply {

	private ast_typedecl ret_type;

	public gm_typechecker_stage_4(ast_typedecl r_type) {
		set_for_sent(true);
		ret_type = r_type;
	}

	public final boolean apply(ast_sent s) {
		if (s.get_nodetype() == AST_NODE_TYPE.AST_ASSIGN) {
			ast_assign a = (ast_assign) s;
			GMTYPE_T lhs_type;
			if (a.is_target_scalar()) {
				lhs_type = a.get_lhs_scala().getTypeSummary();
			} else if (a.is_target_map_entry()) {
				ast_mapaccess mapAccess = a.to_assign_mapentry().get_lhs_mapaccess();
				ast_maptypedecl mapDecl = (ast_maptypedecl) mapAccess.get_map_id().getTypeInfo();
				lhs_type = mapDecl.getValueTypeSummary();
			} else {
				lhs_type = a.get_lhs_field().getTargetTypeSummary();
			}
			ast_expr e = a.get_rhs();

			gm_resolve_size_of_inf_expr(e, lhs_type);
		} else if (s.get_nodetype() == AST_NODE_TYPE.AST_RETURN) {
			ast_expr e = ((ast_return) s).get_expr();
			if (e != null) {
				gm_resolve_size_of_inf_expr(e, ret_type.getTypeSummary());
			}
		} else if (s.get_nodetype() == AST_NODE_TYPE.AST_CALL) {
			ast_expr e = ((ast_call) s).get_builtin();
			if (e != null) {
				gm_resolve_size_of_inf_expr(e, GMTYPE_INT);
			}
		} else if (s.get_nodetype() == AST_NODE_TYPE.AST_IF) {
			ast_expr e = ((ast_if) s).get_cond();
			gm_resolve_size_of_inf_expr(e, GMTYPE_BOOL);
		} else if (s.get_nodetype() == AST_NODE_TYPE.AST_FOREACH) {
			ast_expr e = ((ast_foreach) s).get_filter();
			if (e != null)
				gm_resolve_size_of_inf_expr(e, GMTYPE_BOOL);
		} else if (s.get_nodetype() == AST_NODE_TYPE.AST_WHILE) {
			ast_expr e = ((ast_while) s).get_cond();
			gm_resolve_size_of_inf_expr(e, GMTYPE_BOOL);
		} else if (s.get_nodetype() == AST_NODE_TYPE.AST_BFS) {
			ast_expr e;
			e = ((ast_bfs) s).get_f_filter();
			if (e != null)
				gm_resolve_size_of_inf_expr(e, GMTYPE_BOOL);
			e = ((ast_bfs) s).get_b_filter();
			if (e != null)
				gm_resolve_size_of_inf_expr(e, GMTYPE_BOOL);
			e = ((ast_bfs) s).get_navigator();
			if (e != null)
				gm_resolve_size_of_inf_expr(e, GMTYPE_BOOL);
		}

		return true;
	}

	private static boolean gm_resolve_size_of_inf_expr(ast_expr e, GMTYPE_T dest_type) {
		if (e.get_type_summary().is_inf_type()) {
			e.set_type_summary(dest_type.get_sized_inf_type());
		} else if (e.get_type_summary().is_nil_type()) {
			if (dest_type.is_node_type()) {
				e.set_type_summary(GMTYPE_NIL_NODE);
			} else if (dest_type.is_edge_type()) {
				e.set_type_summary(GMTYPE_NIL_EDGE);
			} else {
				e.set_type_summary(GMTYPE_NIL_NODE);
			}
		}

		switch (e.get_opclass()) {
		case GMEXPR_ID:
		case GMEXPR_FIELD:
		case GMEXPR_IVAL:
		case GMEXPR_FVAL:
		case GMEXPR_BVAL:
		case GMEXPR_INF:
		case GMEXPR_NIL:
		case GMEXPR_MAPACCESS:
			break;
		case GMEXPR_UOP:
		case GMEXPR_LUOP:
			if (e.get_type_summary().is_inf_type()) {
				gm_resolve_size_of_inf_expr(e.get_left_op(), dest_type);
			} // type conversion
			else {
				gm_resolve_size_of_inf_expr(e.get_left_op(), e.get_type_summary());
			}
			break;
		case GMEXPR_BIOP:
		case GMEXPR_LBIOP:
			gm_resolve_size_of_inf_expr(e.get_left_op(), dest_type);
			gm_resolve_size_of_inf_expr(e.get_right_op(), dest_type);
			break;
		case GMEXPR_TER:
			gm_resolve_size_of_inf_expr(e.get_cond_op(), GMTYPE_BOOL);
			gm_resolve_size_of_inf_expr(e.get_left_op(), dest_type);
			gm_resolve_size_of_inf_expr(e.get_right_op(), dest_type);
			break;
		case GMEXPR_REDUCE: {
			ast_expr_reduce r = (ast_expr_reduce) e;
			ast_expr f = r.get_filter();
			ast_expr b = r.get_body();
			if (f != null) {
				gm_resolve_size_of_inf_expr(f, GMTYPE_BOOL);
			}
			gm_resolve_size_of_inf_expr(b, dest_type);
			break;
		}
		case GMEXPR_BUILTIN_FIELD:
		case GMEXPR_BUILTIN: {
			// for each argument check type check
			ast_expr_builtin r = (ast_expr_builtin) e;
			gm_builtin_def def = r.get_builtin_def();

			LinkedList<ast_expr> ARGS = r.get_args();
			int i = 0;
			for (ast_expr e_arg : ARGS) {
				GMTYPE_T arg_type = def.get_arg_type(i);
				gm_resolve_size_of_inf_expr(e_arg, arg_type);
				i++;
			}
			break;
		}
		case GMEXPR_COMP: {
			// check left and right
			GMTYPE_T l_type = e.get_left_op().get_type_summary();
			GMTYPE_T r_type = e.get_right_op().get_type_summary();

			if (l_type.is_inf_type() && r_type.is_inf_type()) {
				l_type = GMTYPE_INT;
				r_type = GMTYPE_INT;
			} else if (l_type.is_inf_type()) {
				l_type = r_type;
			} else if (r_type.is_inf_type()) {
				r_type = l_type;
			}

			gm_resolve_size_of_inf_expr(e.get_left_op(), l_type);
			gm_resolve_size_of_inf_expr(e.get_right_op(), r_type);

			break;
		}
		case GMEXPR_FOREIGN: {
			break;
		}
		default:
			assert false;
			break;
		}
		return true;
	}

}
