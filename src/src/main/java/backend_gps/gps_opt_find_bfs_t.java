package backend_gps;

import static backend_gps.GPSConstants.GPS_FLAG_HAS_DOWN_NBRS;
import inc.GMTYPE_T;

import java.util.LinkedList;

import ast.AST_NODE_TYPE;
import ast.ast_bfs;
import ast.ast_foreach;
import ast.ast_node;
import ast.ast_sent;

import common.gm_apply;

public class gps_opt_find_bfs_t extends gm_apply {

	private boolean in_bfs = false;
	private ast_bfs current_bfs = null;
	private LinkedList<ast_bfs> BFS = new LinkedList<ast_bfs>();

	public gps_opt_find_bfs_t() {
		set_for_sent(true);
		set_separate_post_apply(true);
	}

	// pre
	@Override
	public boolean apply(ast_sent s) {
		if (s.get_nodetype() == AST_NODE_TYPE.AST_BFS) {
			assert !in_bfs; // no nested BFS for now
			in_bfs = true;
			current_bfs = (ast_bfs) s;
			BFS.addLast(current_bfs);
		}

		else if (s.get_nodetype() == AST_NODE_TYPE.AST_FOREACH) {
			ast_foreach fe = (ast_foreach) s;
			if (in_bfs) {
				GMTYPE_T itt = fe.get_iter_type();
				if (itt.is_iteration_on_down_neighbors()) {
					// check if this is forward bfs
					ast_node current = fe;
					ast_node parent = fe.get_parent();
					while (parent != current_bfs) {
						assert parent != null;
						current = parent;
						parent = parent.get_parent();
					}
					if (current == current_bfs.get_fbody()) {
						current_bfs.add_info_bool(GPS_FLAG_HAS_DOWN_NBRS, true);
					}
				}
			}
		}
		return true;
	}

	// post
	@Override
	public boolean apply2(ast_sent s) {
		if (s.get_nodetype() == AST_NODE_TYPE.AST_BFS) {
			in_bfs = false;
			current_bfs = null;
		}
		return true;
	}

	public final LinkedList<ast_bfs> get_targets() {
		return BFS;
	}

}