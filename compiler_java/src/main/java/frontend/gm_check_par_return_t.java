package frontend;

import ast.ast_node_type;
import ast.ast_bfs;
import ast.ast_foreach;
import ast.ast_sent;

import common.gm_errors_and_warnings;
import common.gm_apply;
import common.gm_error;

//--------------------------------------------
// Additional rules
// (1) No return in the middle of par-context 
//--------------------------------------------
public class gm_check_par_return_t extends gm_apply {

	private int par_depth = 0;
	private boolean _is_okay = true;

	public gm_check_par_return_t() {
		set_for_sent(true);
		set_separate_post_apply(true);
	}

	public final boolean is_okay() {
		return _is_okay;
	}

	@Override
	public boolean apply(ast_sent s) {
		if (s.get_nodetype() == ast_node_type.AST_RETURN) {
			if (par_depth > 0) {
				gm_error.gm_type_error(gm_errors_and_warnings.GM_ERROR_PAR_RETURN, s.get_line(), s.get_col());
				_is_okay = false;
			}
		} else if (s.get_nodetype() == ast_node_type.AST_FOREACH) {
			ast_foreach fe = (ast_foreach) s;
			if (fe.is_parallel()) {
				par_depth++;
			}
		} else if (s.get_nodetype() == ast_node_type.AST_BFS) {
			ast_bfs bfs = (ast_bfs) s;
			if (bfs.is_parallel()) {
				par_depth++;
			}
		}
		return true;
	}

	@Override
	public boolean apply2(ast_sent s) {
		if (s.get_nodetype() == ast_node_type.AST_FOREACH) {
			ast_foreach fe = (ast_foreach) s;
			if (fe.is_parallel()) {
				par_depth--;
			}
		} else if (s.get_nodetype() == ast_node_type.AST_BFS) {
			ast_bfs bfs = (ast_bfs) s;
			if (bfs.is_parallel()) {
				par_depth--;
			}
		}
		return true;
	}

}