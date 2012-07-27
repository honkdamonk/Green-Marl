package frontend;

import java.util.LinkedList;

import ast.AST_NODE_TYPE;
import ast.ast_bfs;
import ast.ast_foreach;
import ast.ast_node;
import ast.gm_rwinfo_map;

import common.gm_apply;

public class gm_check_reduce_error_t extends gm_apply {

	public boolean is_okay = true;
	// all the bounded syms in the current scope
	public LinkedList<bound_info_t> B_scope = new LinkedList<bound_info_t>();

	@Override
	public void begin_context(ast_node n) {
		// [hack] bfs body is always sent-block.
		// check If I am bfs body (forward/reverse)
		// add bound-set to the context
		if ((n.get_nodetype() == AST_NODE_TYPE.AST_SENTBLOCK) && (n.get_parent() != null)) {
			ast_node t = n.get_parent();
			if (t.get_nodetype() != AST_NODE_TYPE.AST_BFS)
				return;
			ast_bfs bfs = (ast_bfs) t;
			if (n == bfs.get_fbody()) {
				gm_rwinfo_map B_fw = GlobalMembersGm_rw_analysis.gm_get_bound_set_info(bfs).bound_set;

				// check bound error
				is_okay = GlobalMembersGm_reduce_error_check.check_add_and_report_conflicts(B_scope, B_fw) && is_okay;

			} else if (n == bfs.get_bbody()) {
				gm_rwinfo_map B_bw = GlobalMembersGm_rw_analysis.gm_get_bound_set_info(bfs).bound_set;
				is_okay = GlobalMembersGm_reduce_error_check.check_add_and_report_conflicts(B_scope, B_bw) && is_okay;
			} else {
				assert false;
			}
		} else if (n.get_nodetype() == AST_NODE_TYPE.AST_FOREACH) {
			ast_foreach fe = (ast_foreach) n;
			gm_rwinfo_map B = GlobalMembersGm_rw_analysis.gm_get_bound_set_info(fe).bound_set;
			is_okay = GlobalMembersGm_reduce_error_check.check_add_and_report_conflicts(B_scope, B) && is_okay;
		}
	}

	@Override
	public void end_context(ast_node n) {
		if ((n.get_nodetype() == AST_NODE_TYPE.AST_SENTBLOCK) && (n.get_parent() != null)) {
			ast_node t = n.get_parent();
			if (t.get_nodetype() != AST_NODE_TYPE.AST_BFS)
				return;
			ast_bfs bfs = (ast_bfs) t;
			if (n == bfs.get_fbody()) {
				gm_rwinfo_map B_fw = GlobalMembersGm_rw_analysis.gm_get_bound_set_info(bfs).bound_set;
				GlobalMembersGm_reduce_error_check.remove_all(B_scope, B_fw);
			} else if (n == bfs.get_bbody()) {
				gm_rwinfo_map B_bw = GlobalMembersGm_rw_analysis.gm_get_bound_set_info(bfs).bound_set;
				GlobalMembersGm_reduce_error_check.remove_all(B_scope, B_bw);
			} else {
				assert false;
			}
		} else if (n.get_nodetype() == AST_NODE_TYPE.AST_FOREACH) {
			ast_foreach fe = (ast_foreach) n;
			gm_rwinfo_map B = GlobalMembersGm_rw_analysis.gm_get_bound_set_info(fe).bound_set;
			GlobalMembersGm_reduce_error_check.remove_all(B_scope, B);
		}
	}
}
// =========================================================
// called from gm_typecheck.cc
// =========================================================
// bool gm_frontend::do_reduce_error_check(ast_procdef* p)

