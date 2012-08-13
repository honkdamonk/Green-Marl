package backend_cpp;

import ast.AST_NODE_TYPE;
import ast.ast_bfs;
import ast.ast_foreach;
import ast.ast_node;
import ast.ast_sent;

import common.GlobalMembersGm_traverse;
import common.gm_apply;

public class choose_parallel_t extends gm_apply {
	
	public choose_parallel_t() {
		this._in_bfs = false;
	}

	public final void begin_context(ast_node n) {
		if ((n.get_nodetype() == AST_NODE_TYPE.AST_BFS) && (((ast_bfs) n).is_bfs())) {
			assert _in_bfs == false;
			_in_bfs = true;
		}
	}

	public final void end_context(ast_node n) {
		if ((n.get_nodetype() == AST_NODE_TYPE.AST_BFS) && (((ast_bfs) n).is_bfs())) {
			_in_bfs = false;
		}
	}

	public final boolean apply(ast_sent s) {
		if (s.get_nodetype() == AST_NODE_TYPE.AST_FOREACH) {
			ast_foreach fe = (ast_foreach) s;
			if (_in_bfs) {
				fe.set_sequential(true);
			} else if (fe.is_sequential()) {
				// user wants it sequential
				fe.set_sequential(true);
			} else if (fe.get_iter_type().is_iteration_on_all_graph()) {
				set_to_seq_t T = new set_to_seq_t();

				// set parallel
				fe.set_sequential(false);

				// negate all the up-way
				GlobalMembersGm_traverse.gm_traverse_up_sent(s.get_parent(), T);

				// [XXX] need to think about this
			} else if (fe.get_iter_type().is_iteration_on_collection()) {

				// make it sequential always
				set_to_seq_t T = new set_to_seq_t();

				fe.set_sequential(true);

			} else {
				// sequential
				fe.set_sequential(true);
			}
		}
		return true;
	}

	private boolean _in_bfs;
}