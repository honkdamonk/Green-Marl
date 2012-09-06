package backend_cpp;

import ast.AST_NODE_TYPE;
import ast.ast_bfs;
import ast.ast_foreach;
import ast.ast_sent;

import common.gm_apply;

public class cpp_check_save_bfs_t extends gm_apply {
	public cpp_check_save_bfs_t() {
		set_for_sent(true);
		set_separate_post_apply(true);
	}

	// pre
	public final boolean apply(ast_sent s) {
		if (s.get_nodetype() == AST_NODE_TYPE.AST_BFS) {
			ast_bfs bfs = (ast_bfs) s;
			if (bfs.is_bfs())
				L.addFirst(bfs);
		} else if (s.get_nodetype() == AST_NODE_TYPE.AST_FOREACH) {
			ast_foreach fe = (ast_foreach) s;
			if (fe.get_iter_type().is_iteration_on_down_neighbors()) {
				ast_bfs bfs = L.getFirst();
				bfs.add_info_bool(gm_cpp_gen.CPPBE_INFO_USE_DOWN_NBR, true);
			}
		}
		return true;
	}

	public final boolean apply2(ast_sent s) {
		if (s.get_nodetype() == AST_NODE_TYPE.AST_BFS) {
			ast_bfs bfs = (ast_bfs) s;
			if (bfs.is_bfs())
				L.removeFirst();
		}
		return true;
	}

	private java.util.LinkedList<ast_bfs> L = new java.util.LinkedList<ast_bfs>();
}