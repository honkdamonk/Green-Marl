package backend_gps;

import static backend_gps.GPSConstants.GPS_FLAG_HAS_COMMUNICATION;
import static backend_gps.GPSConstants.GPS_FLAG_HAS_COMMUNICATION_RANDOM;
import static backend_gps.GPSConstants.GPS_FLAG_WHILE_HEAD;
import static backend_gps.GPSConstants.GPS_FLAG_WHILE_TAIL;
import frontend.GlobalMembersGm_rw_analysis_check2;
import frontend.gm_rwinfo_sets;
import frontend.gm_symtab_entry;
import inc.gps_apply_bb;
import inc.gps_apply_bb_ast;

import java.util.LinkedList;

import ast.gm_rwinfo_map;

public class gps_find_intra_merge_candidate_t extends gps_apply_bb {
	
	private LinkedList<gps_intra_merge_candidate_t> cands;
	private LinkedList<gm_gps_basic_block> stack = new LinkedList<gm_gps_basic_block>();
	private int current_trace_head = -1;
	private gm_gps_basic_block curr_head = null;
	private gm_gps_basic_block curr_tail = null;
	
	public gps_find_intra_merge_candidate_t(LinkedList<gps_intra_merge_candidate_t> L) {
		cands = new LinkedList<gps_intra_merge_candidate_t>(L);
	}

	@Override
	public void apply(gm_gps_basic_block b) {
		// printf("visiting :%d\n", b->get_id());
		if (b.has_info(GPS_FLAG_WHILE_HEAD)) {
			start_new_trace(b.get_id(), b);
		} else if (b.has_info(GPS_FLAG_WHILE_TAIL)) {
			int head_id = b.find_info_int(GPS_FLAG_WHILE_TAIL);
			check_and_finish_trace(head_id, b);
		} else if (current_trace_head != -1) {
			if ((b.get_num_entries() == 1) && (b.get_num_exits() == 1)) // simple
																		// path
			{
				stack.addLast(b);
			}
		}
	}

	private void start_new_trace(int id, gm_gps_basic_block b) {
		stack.clear();
		current_trace_head = id;
		curr_head = b;
		// printf("start trace : %d\n", id);
	}

	private void check_and_finish_trace(int id, gm_gps_basic_block b) {
		curr_tail = b;
		// printf("end trace : %d\n", id);
		if ((b.get_num_entries() == 1) && (b.get_num_exits() == 1))
			stack.addLast(b);

		if (id == current_trace_head) {
			// found simple lines in the task
			// length should be larger than 4
			gm_gps_basic_block p1;
			gm_gps_basic_block p2;
			gm_gps_basic_block s1;
			gm_gps_basic_block s2;
			gm_gps_basic_block s0 = null;
			if (stack.size() >= 4) {

				int i = 0;
				p1 = stack.get(i);
				if(!p1.is_vertex()) {
					s0 = p1;
					i++;
					p1 = stack.get(i);
				}
				
				i++;
				s1 = stack.get(i);
				i = stack.size() - 1;
				s2 = stack.get(i);
				i--;
				p2 = stack.get(i);
				
				boolean is_okay = false;
				if (p1.is_vertex() && p2.is_vertex() && !s1.is_vertex() && !s2.is_vertex() && ((s0 == null) || (!s0.is_vertex()))) {
					is_okay = true;
				}

				// check PAR1 contains no receive
				if (p1.has_receiver())
					is_okay = false;
				else if (p2.find_info_bool(GPS_FLAG_HAS_COMMUNICATION)
						|| p2.find_info_bool(GPS_FLAG_HAS_COMMUNICATION_RANDOM))
					is_okay = false;

				gm_rwinfo_sets rwi = null;
				if (is_okay) {

					// check dependency between p1 and s_n
					rwi = new gm_rwinfo_sets();
					gm_gps_get_rwinfo_from_bb(p1, rwi);

					gm_rwinfo_sets rwi_n = new gm_rwinfo_sets();
					gm_gps_get_rwinfo_from_bb(s2, rwi_n);

					if (GlobalMembersGm_rw_analysis_check2.gm_has_dependency(rwi, rwi_n))
						is_okay = false;

					if (is_okay && (s0 != null)) {
						/*
						 * printf("hello2\n"); s0->print(); p1->print();
						 * s1->print(); p2->print(); s2->print();
						 */

						gm_rwinfo_sets rwi_0 = new gm_rwinfo_sets(); // s_0
						gm_gps_get_rwinfo_from_bb(s0, rwi_0);

						// check dependency between s1 and s0
						gm_rwinfo_sets rwi_s1 = new gm_rwinfo_sets();
						gm_gps_get_rwinfo_from_bb(s1, rwi_s1);
						if (GlobalMembersGm_rw_analysis_check2.gm_has_dependency(rwi_0, rwi_s1))
							is_okay = false;

						// check dependency between p1 and s0
						if (GlobalMembersGm_rw_analysis_check2.gm_has_dependency(rwi_0, rwi))
							is_okay = false;

						if (rwi_0 != null)
							rwi_0.dispose();
						if (rwi_s1 != null)
							rwi_s1.dispose();
					}

					if (rwi_n != null)
						rwi_n.dispose();
				}

				if (is_okay) {
					// accumulated rwi
					gm_gps_get_rwinfo_from_bb(s1, rwi);

					if (s0 != null)
						gm_gps_get_rwinfo_from_bb(s0, rwi);

					/*
					 * printf("read set for BB:%d,%d\n", p1->get_id(),
					 * s1->get_id()); gm_print_rwinfo_set(rwi->read_set);
					 * printf("write set for BB:%d,%d\n", p1->get_id(),
					 * s1->get_id()); gm_print_rwinfo_set(rwi->write_set);
					 */

					// check if argument is modified inside p1 or s1
					boolean b1 = check_if_argument_is_modified(rwi.write_set);
					if (b1)
						is_okay = false;
				}

				if (is_okay) {
					// bb after while-exit loop
					gm_gps_basic_block next;
					if (curr_head.get_num_exits() > 1) // WHILE
						next = curr_head.get_nth_exit(1);
					else
						next = curr_tail.get_nth_exit(1);

					gm_rwinfo_sets rwi2 = new gm_rwinfo_sets();
					gm_gps_get_rwinfo_from_all_reachable_bb(next, rwi2);

					/*
					 * printf("read set for reachable:\n");
					 * gm_print_rwinfo_set(rwi2->read_set);
					 * printf("write set for reeachable\n");
					 * gm_print_rwinfo_set(rwi2->write_set);
					 */

					// check if future is modified
					is_okay = !GlobalMembersGm_rw_analysis_check2.gm_does_intersect(rwi.write_set, rwi2.read_set);

					if (rwi2 != null)
						rwi2.dispose();
				}

				if (rwi != null)
					rwi.dispose();

				if (is_okay) {
					gps_intra_merge_candidate_t C = new gps_intra_merge_candidate_t();
					if (curr_head.get_num_exits() > 1) // WHILE
						C.while_cond = curr_head;
					else {
						C.while_cond = curr_tail;
						assert curr_tail.get_num_exits() > 1;
						// DO-WHILE
					}
					C.par1 = p1;
					C.seq1 = s1;
					C.parn = p2;
					C.seqn = s2;
					C.seq0 = s0;
					cands.addLast(C);
				}
			}
		}
		current_trace_head = -1;
		curr_head = null;
	}
	
	private static boolean check_if_argument_is_modified(gm_rwinfo_map M) {
		for (gm_symtab_entry e : M.keySet()) {
			if (e.isArgument())
				return true;
		}
		return false;
	}
	
	private static gm_rwinfo_sets gm_gps_get_rwinfo_from_all_reachable_bb(gm_gps_basic_block BB, gm_rwinfo_sets S) {
		return gm_gps_get_rwinfo_from_all_reachable_bb(BB, S, false);
	}

	private static gm_rwinfo_sets gm_gps_get_rwinfo_from_all_reachable_bb(gm_gps_basic_block BB, gm_rwinfo_sets S, boolean check_receivers) {
		if (S == null)
			S = new gm_rwinfo_sets();
		assert check_receivers == false;

		// -------------------------------------------
		// traverse AST inside BB
		// merge read/write sets
		// caution for communicating symbols
		// -------------------------------------------
		gm_gps_find_rwinfo_simple T = new gm_gps_find_rwinfo_simple(S);
		T.set_check_receiver(check_receivers);

		// post && pre
		GlobalMembersGm_gps_misc.gps_bb_traverse_ast(BB, T, true, true); 

		return S;
	}
	
	private static gm_rwinfo_sets gm_gps_get_rwinfo_from_bb(gm_gps_basic_block BB, gm_rwinfo_sets S) {
		return gm_gps_get_rwinfo_from_bb(BB, S, false);
	}

	private static gm_rwinfo_sets gm_gps_get_rwinfo_from_bb(gm_gps_basic_block BB, gm_rwinfo_sets S, boolean check_receivers) {
		if (S == null)
			S = new gm_rwinfo_sets();
		assert check_receivers == false;

		// -------------------------------------------
		// traverse AST inside BB
		// merge read/write sets
		// caution for communicating symbols
		// -------------------------------------------
		gm_gps_find_rwinfo_simple T = new gm_gps_find_rwinfo_simple(S);
		T.set_check_receiver(check_receivers);
		// post && pre
		gps_bb_traverse_ast_single(BB, T, true, true);

		return S;
	}
	
	private static void gps_bb_traverse_ast_single(gm_gps_basic_block entry, gps_apply_bb_ast apply, boolean is_post, boolean is_pre) {
		apply.set_is_post(is_post);
		apply.set_is_pre(is_pre);
		apply.apply(entry);
	}

}