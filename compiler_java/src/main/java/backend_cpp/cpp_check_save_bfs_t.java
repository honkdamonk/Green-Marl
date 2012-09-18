package backend_cpp;

import java.util.LinkedList;

import ast.ast_bfs;
import ast.ast_foreach;
import ast.ast_node_type;
import ast.ast_sent;

import common.gm_apply;

class cpp_check_save_bfs_t extends gm_apply {
	
	private final LinkedList<ast_bfs> L = new LinkedList<ast_bfs>();
	
	cpp_check_save_bfs_t() {
		set_for_sent(true);
		set_separate_post_apply(true);
	}

	// pre
	@Override
	public final boolean apply(ast_sent s) {
		if (s.get_nodetype() == ast_node_type.AST_BFS) {
			ast_bfs bfs = (ast_bfs) s;
			if (bfs.is_bfs())
				L.addFirst(bfs);
		} else if (s.get_nodetype() == ast_node_type.AST_FOREACH) {
			ast_foreach fe = (ast_foreach) s;
			if (fe.get_iter_type().is_iteration_on_down_neighbors()) {
				ast_bfs bfs = L.getFirst();
				bfs.add_info_bool(gm_cpp_gen.CPPBE_INFO_USE_DOWN_NBR, true);
			}
		}
		return true;
	}

	@Override
	public final boolean apply2(ast_sent s) {
		if (s.get_nodetype() == ast_node_type.AST_BFS) {
			ast_bfs bfs = (ast_bfs) s;
			if (bfs.is_bfs())
				L.removeFirst();
		}
		return true;
	}

}