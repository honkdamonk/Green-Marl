package frontend;

import inc.GMTYPE_T;
import ast.AST_NODE_TYPE;
import ast.ast_assign;
import ast.ast_bfs;
import ast.ast_call;
import ast.ast_expr;
import ast.ast_foreach;
import ast.ast_if;
import ast.ast_return;
import ast.ast_sent;
import ast.ast_typedecl;
import ast.ast_while;

import common.GlobalMembersGm_resolve_inf_size;
import common.gm_apply;

public class gm_typechecker_stage_4 extends gm_apply {
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
			} else {
				lhs_type = a.get_lhs_field().getTargetTypeSummary();
			}
			ast_expr e = a.get_rhs();

			GlobalMembersGm_resolve_inf_size.gm_resolve_size_of_inf_expr(e, lhs_type);
		} else if (s.get_nodetype() == AST_NODE_TYPE.AST_RETURN) {
			ast_expr e = ((ast_return) s).get_expr();
			if (e != null) {
				GlobalMembersGm_resolve_inf_size.gm_resolve_size_of_inf_expr(e, ret_type.getTypeSummary());
			}
		} else if (s.get_nodetype() == AST_NODE_TYPE.AST_CALL) {
			ast_expr e = ((ast_call) s).get_builtin();
			if (e != null) {
				GlobalMembersGm_resolve_inf_size.gm_resolve_size_of_inf_expr(e, GMTYPE_T.GMTYPE_INT);
			}
		} else if (s.get_nodetype() == AST_NODE_TYPE.AST_IF) {
			ast_expr e = ((ast_if) s).get_cond();
			GlobalMembersGm_resolve_inf_size.gm_resolve_size_of_inf_expr(e, GMTYPE_T.GMTYPE_BOOL);
		} else if (s.get_nodetype() == AST_NODE_TYPE.AST_FOREACH) {
			ast_expr e = ((ast_foreach) s).get_filter();
			if (e != null)
				GlobalMembersGm_resolve_inf_size.gm_resolve_size_of_inf_expr(e, GMTYPE_T.GMTYPE_BOOL);
		} else if (s.get_nodetype() == AST_NODE_TYPE.AST_WHILE) {
			ast_expr e = ((ast_while) s).get_cond();
			GlobalMembersGm_resolve_inf_size.gm_resolve_size_of_inf_expr(e, GMTYPE_T.GMTYPE_BOOL);
		} else if (s.get_nodetype() == AST_NODE_TYPE.AST_BFS) {
			ast_expr e;
			e = ((ast_bfs) s).get_f_filter();
			if (e != null)
				GlobalMembersGm_resolve_inf_size.gm_resolve_size_of_inf_expr(e, GMTYPE_T.GMTYPE_BOOL);
			e = ((ast_bfs) s).get_b_filter();
			if (e != null)
				GlobalMembersGm_resolve_inf_size.gm_resolve_size_of_inf_expr(e, GMTYPE_T.GMTYPE_BOOL);
			e = ((ast_bfs) s).get_navigator();
			if (e != null)
				GlobalMembersGm_resolve_inf_size.gm_resolve_size_of_inf_expr(e, GMTYPE_T.GMTYPE_BOOL);
		}

		return true;
	}

	private ast_typedecl ret_type;

}
// bool gm_frontend::do_typecheck_step4_resolve_inf(ast_procdef* p)
