package backend_gps;

import static backend_gps.GPSConstants.GPS_FLAG_IS_INNER_LOOP;
import static backend_gps.GPSConstants.GPS_INT_SYNTAX_CONTEXT;

import java.util.LinkedList;

import ast.ast_node_type;
import ast.ast_foreach;
import ast.ast_sent;

import common.gm_transform_helper;
import common.gm_apply;

//-----------------------------------------------------------------
// Rewrite expressions to make the messages compact
//-----------------------------------------------------------------
//
// Foreach (n: G.Nodes) {
//     Foreach(t: n.Nbrs) {
//         if (t.A + n.A > 10) {
//            t.B += n.D * n.E + t.C;
//         }
//     }
// }
// ==>
// Foreach (n: G.Nodes) {
//     Foreach(t: n.Nbrs) {
//         <type> _t1 = n.A;
//         <type> _t2 = n.D * n.E;
//         if (t.A + _t1 > 10) {
//            t.B += _t2 + t.C;
//         }
//     }
// }
//-----------------------------------------------------------------
public class gps_rewrite_rhs_preprocessing_t extends gm_apply {
	
	private LinkedList<ast_foreach> inner_loops = new LinkedList<ast_foreach>();
	
	public gps_rewrite_rhs_preprocessing_t() {
		set_for_sent(true);
	}

	public final boolean apply(ast_sent s) {
		if (s.get_nodetype() == ast_node_type.AST_FOREACH) {
			if (s.find_info_bool(GPS_FLAG_IS_INNER_LOOP)) {
				ast_foreach fe = (ast_foreach) s;
				if (fe.get_body().get_nodetype() != ast_node_type.AST_SENTBLOCK) {
					inner_loops.addLast(fe);
				}
			}
		}
		return true;
	}

	public final void process() {
		for (ast_foreach fe : inner_loops) {
			ast_sent s = fe.get_body();
			gm_transform_helper.gm_make_it_belong_to_sentblock(s);

			assert s.get_parent().get_nodetype() == ast_node_type.AST_SENTBLOCK;
			s.get_parent().add_info_int(GPS_INT_SYNTAX_CONTEXT, gm_gps_new_scope_analysis_t.GPS_NEW_SCOPE_IN.getValue());

			assert fe.get_body().get_nodetype() == ast_node_type.AST_SENTBLOCK;
			// printf("(1)fe = %p, sb = %p\n", fe, fe->get_body());
		}
	}

}