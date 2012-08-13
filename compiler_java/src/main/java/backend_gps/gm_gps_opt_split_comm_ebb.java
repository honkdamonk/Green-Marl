package backend_gps;

import inc.gm_compile_step;

import java.util.HashSet;
import java.util.LinkedList;

import ast.ast_procdef;

import common.GlobalMembersGm_main;

public class gm_gps_opt_split_comm_ebb extends gm_compile_step {
	private gm_gps_opt_split_comm_ebb() {
		set_description("Split communicatining EBBs");
	}

	public void process(ast_procdef p) {
		gm_gps_beinfo info = (gm_gps_beinfo) GlobalMembersGm_main.FE.get_backend_info(p);
		gm_gps_basic_block entry = info.get_entry_basic_block();

		// -------------------------------------------
		// find Basic Blocks that contain nested communication
		// -------------------------------------------
		gps_find_comm_vertex_bb T = new gps_find_comm_vertex_bb(info);
		GlobalMembersGm_gps_misc.gps_bb_apply_only_once(entry, T);

		HashSet<gm_gps_basic_block> BB_list = T.get_target_basic_blocks();

		// -------------------------------------------
		// split BB into two
		// / BB =>
		// BB1 (send) -> seq -> BB2 (receive)
		// -------------------------------------------
		for (gm_gps_basic_block BB : BB_list) {
			gm_gps_basic_block BB2 = split_vertex_BB(BB, info);
		}
	}

	@Override
	public gm_compile_step get_instance() {
		return new gm_gps_opt_split_comm_ebb();
	}

	public static gm_compile_step get_factory() {
		return new gm_gps_opt_split_comm_ebb();
	}

	// [prev -> BB -> next] ==>
	// [prev -> BB(S) -> new_seq -> BB(R) -> next]
	private static gm_gps_basic_block split_vertex_BB(gm_gps_basic_block BB, gm_gps_beinfo gen) {
		// printf("splitting BB id = %d\n", BB->get_id());

		assert BB.is_vertex();
		// assert(BB->has_sender());
		assert BB.has_receiver();
		assert BB.get_num_entries() == 1;
		assert BB.get_num_exits() == 1;

		gm_gps_basic_block prev = BB.get_nth_entry(0);
		gm_gps_basic_block next = BB.get_nth_exit(0);

		assert !prev.is_vertex();
		assert !next.is_vertex();
		assert next.get_num_entries() == 1;

		gm_gps_basic_block new_seq = new gm_gps_basic_block(gen.issue_basicblock_id());
		new_seq.set_after_vertex(true);

		gm_gps_basic_block new_BB = new gm_gps_basic_block(gen.issue_basicblock_id(), gm_gps_bbtype_t.GM_GPS_BBTYPE_BEGIN_VERTEX);

		// --------------------------------------
		// migrate receiver list to new_BB
		// --------------------------------------
		LinkedList<gm_gps_comm_unit> L = BB.get_receivers();

		for (gm_gps_comm_unit unit : L) {
			new_BB.add_receiver(unit);
		}
		BB.clear_receivers();

		// insert basic blocks
		BB.remove_all_exits();
		next.remove_all_entries();

		BB.add_exit(new_seq);
		new_seq.add_exit(new_BB);
		new_BB.add_exit(next);

		LinkedList<gm_gps_basic_block> BBLIST = gen.get_basic_blocks();
		BBLIST.addLast(new_seq);
		BBLIST.addLast(new_BB);

		return new_BB;

	}
}