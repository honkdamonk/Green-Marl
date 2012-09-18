package backend_gps;

import static backend_gps.GPSConstants.GPS_FLAG_HAS_COMMUNICATION;
import static backend_gps.GPSConstants.GPS_FLAG_HAS_COMMUNICATION_RANDOM;
import static backend_gps.GPSConstants.GPS_FLAG_IS_INNER_LOOP;
import static backend_gps.GPSConstants.GPS_FLAG_IS_OUTER_LOOP;
import static backend_gps.GPSConstants.GPS_FLAG_SENT_BLOCK_FOR_RANDOM_WRITE_ASSIGN;
import frontend.gm_symtab_entry;
import inc.gps_apply_bb_ast;

import java.util.HashSet;

import ast.ast_assign;
import ast.ast_field;
import ast.ast_foreach;
import ast.ast_node_type;
import ast.ast_sent;
import ast.ast_sentblock;

//------------------------------------------------------------------
// Split Basic Blocks
//     - Find BB that contains communication, and add into a list
//     - Mark communication foreach statement (assign ID)
//------------------------------------------------------------------
public class gps_find_comm_vertex_bb extends gps_apply_bb_ast {

	private gm_gps_beinfo gen;
	private final HashSet<gm_gps_basic_block> target_bb = new HashSet<gm_gps_basic_block>();
	private ast_foreach current_outer_loop = null;

	public gps_find_comm_vertex_bb(gm_gps_beinfo g) {
		gen = g;
		set_for_sent(true);
	}

	@Override
	public boolean apply(ast_sent s) {
		// receiver should be empty.
		assert is_under_receiver_traverse() == false;

		gm_gps_basic_block curr = get_curr_BB();

		// only look at vertex BB
		if (!curr.is_vertex())
			return true;

		// neighborhood looking foreach statement is a communicating bb
		if (s.get_nodetype() == ast_node_type.AST_FOREACH) {
			ast_foreach fe = (ast_foreach) s;
			if (fe.find_info_bool(GPS_FLAG_IS_OUTER_LOOP)) {
				current_outer_loop = fe;

			} else if (fe.find_info_bool(GPS_FLAG_IS_INNER_LOOP)) {

				gen.add_communication_unit_nested(fe); // adding inner loop

				// add the foreach loop as 'receiver' of this state,
				// temporariliy.
				// (Receiver loop will be moved to the 'next' state, after
				// split)
				curr.add_nested_receiver(fe);

				// list of bbs that should be splited
				target_bb.add(curr);

				// mark current outer loop to have communication
				assert current_outer_loop != null;
				current_outer_loop.add_info_bool(GPS_FLAG_HAS_COMMUNICATION, true);
			} else {
				assert false;
			}

			// curr->set_has_sender(true);

		} else if (s.get_nodetype() == ast_node_type.AST_ASSIGN) {
			if (s.find_info_ptr(GPS_FLAG_SENT_BLOCK_FOR_RANDOM_WRITE_ASSIGN) != null) {
				ast_assign a = (ast_assign) s;
				ast_sentblock sb = (ast_sentblock) (s.find_info_ptr(GPS_FLAG_SENT_BLOCK_FOR_RANDOM_WRITE_ASSIGN));
				ast_field f = a.get_lhs_field();
				gm_symtab_entry sym = f.get_first().getSymInfo();

				gen.add_communication_unit_random_write(sb, sym);
				gen.add_random_write_sent(sb, sym, s);
				curr.add_random_write_receiver(sb, sym);

				target_bb.add(curr);

				assert current_outer_loop != null;
				current_outer_loop.add_info_bool(GPS_FLAG_HAS_COMMUNICATION_RANDOM, true);
			}
		}

		return true;
	}

	public final HashSet<gm_gps_basic_block> get_target_basic_blocks() {
		return target_bb;
	}

}