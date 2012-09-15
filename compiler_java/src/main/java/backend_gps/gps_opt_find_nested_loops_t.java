package backend_gps;

import java.util.HashMap;

import ast.ast_foreach;
import ast.ast_node_type;
import ast.ast_sent;

import common.gm_apply;

//--------------------------------------------------------
// Find all instances of foreach loops in the procedue 
// and make a map of following
//     - (outer loop -> NULL) or
//     - (inner loop -> outer loop)
//
//     outer-loop: node-wise iteration
//     inner-loop: neighbor hood iteration
//--------------------------------------------------------
//  Example>
//     foreach(n1: G.Nodes) {
//     }
//     foreach (n2: G.Nodes) {
//        foreach(t: n2.Nbrs) {..}
//        foreach(r: n2.Nbrs) {...}
//     }
// ==>
//     (n1, NULL)
//     (n2, NULL)
//     (n2, t)
//     (n2, r)
//
//-------------------------------------------------
// [todo] what to do with loops nested more than 3 depths
//----------------------------------------------------
public class gps_opt_find_nested_loops_t extends gm_apply {

	private int depth = 0;
	private ast_foreach outer_loop = null;
	private HashMap<ast_foreach, ast_foreach> MAP;

	public gps_opt_find_nested_loops_t(HashMap<ast_foreach, ast_foreach> M) {
		MAP = new HashMap<ast_foreach, ast_foreach>(M);
		set_for_sent(true);
		set_separate_post_apply(true);
	}

	@Override
	public boolean apply(ast_sent s) {
		if (s.get_nodetype() == ast_node_type.AST_FOREACH) {
			depth++;
			ast_foreach fe = (ast_foreach) s;
			if (depth == 1) {
				if (fe.get_iter_type().is_all_graph_node_iter_type()) {
					outer_loop = fe;
					MAP.put(fe, null);
				}
			} else if ((depth == 2) && (outer_loop != null)) {
				if (fe.get_iter_type().is_inout_nbr_node_iter_type()) {
					MAP.put(fe, outer_loop);
				}
			}
		}
		return true;
	}

	@Override
	public boolean apply2(ast_sent s) {
		if (s.get_nodetype() == ast_node_type.AST_FOREACH) {
			depth--;
			if (depth == 0)
				outer_loop = null;
		}
		return true;
	}

}
