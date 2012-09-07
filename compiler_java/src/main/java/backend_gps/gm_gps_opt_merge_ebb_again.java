package backend_gps;

import inc.gm_compile_step;

import java.util.HashSet;
import java.util.LinkedList;

import ast.ast_procdef;
import ast.ast_sent;

import common.gm_argopts;
import common.gm_main;

public class gm_gps_opt_merge_ebb_again extends gm_compile_step {

	public static final int FOR_PAR1 = 0;
	public static final int FOR_SEQ1 = 1;
	public static final int FOR_PAR2 = 2;

	private gm_gps_opt_merge_ebb_again() {
		set_description("Merging EBBs");
	}

	public void process(ast_procdef p) {
		if (!gm_main.OPTIONS.get_arg_bool(gm_argopts.GMARGFLAG_MERGE_BB))
			return;

		gm_gps_beinfo info = (gm_gps_beinfo) gm_main.FE.get_backend_info(p);
		gm_gps_basic_block entry = info.get_entry_basic_block();

		// gps_bb_print_all(entry);

		// -------------------------------------------
		// find linear segments
		// -------------------------------------------
		LinkedList<gm_gps_basic_block> current_list = new LinkedList<gm_gps_basic_block>();
		LinkedList<LinkedList<gm_gps_basic_block>> all_lists = new LinkedList<LinkedList<gm_gps_basic_block>>();
		HashSet<gm_gps_basic_block> visited = new HashSet<gm_gps_basic_block>();
		find_linear_segments(entry, current_list, all_lists, visited);

		// -------------------------------------------
		// Apply State Merge
		// -------------------------------------------
		for (LinkedList<gm_gps_basic_block> CL : all_lists) {
			if (CL.size() == 0)
				continue;
			/*
			 * //printf("//==== SEGMENT BEGIN\n"); for(I=CL.begin();
			 * I!=CL.end(); I++) { gps_bb* b = *I; assert(b->get_num_entries()
			 * <= 1); // Test Print //b->print();
			 * 
			 * }
			 */

			find_pattern_and_merge_bb(CL);
			// printf("\n");
		}

	}

	@Override
	public gm_compile_step get_instance() {
		return new gm_gps_opt_merge_ebb_again();
	}

	public static gm_compile_step get_factory() {
		return new gm_gps_opt_merge_ebb_again();
	}

	/**
	 * ------------------------------------------------------------------ Merge
	 * BB again (1) Find Linear Segment
	 * 
	 * (2) Find BB Pattern ... [PAR1] -> [SEQ1] -> [PAR2] -> [SEQ2] ... and
	 * merges into ... [PAR1/2] -> [SEQ1/2] ...
	 * 
	 * <conditions> PAR2 does not contain receive. PAR2 does not use any global
	 * (scala) symbol SEQ1/PAR1 is modifying PAR2 does not modify any global
	 * (scala) symbol SEQ1 is modifying or using PAR2 does not modify any global
	 * (scala) symbol PAR1 is modifying unless with same reduction
	 * 
	 * <after merge> [(PAR1 Receive) -> (PAR1 SentBlock :: PAR2 SentBlock)] ==>
	 * [ (SEQ1) -> (SEQ2) ]
	 * ------------------------------------------------------------------
	 */
	private static void check_and_merge_bb_main(gm_gps_basic_block par1, gm_gps_basic_block seq1, gm_gps_basic_block par2, gm_gps_basic_block seq2) {
		if (par2.has_receiver())
			return;

		check_usage_t T = new check_usage_t();
		T.set_state(FOR_SEQ1);
		T.apply(seq1);

		T.set_state(FOR_PAR1);
		T.apply(par1);

		T.set_state(FOR_PAR2);
		T.apply(par2);

		if (T.is_okay_to_merge()) {
			/*
			 * printf("Merging %d %d %d %d\n", par1->get_id(), seq1->get_id(),
			 * par2->get_id(), seq2->get_id());
			 */

			// add all the sentences
			LinkedList<ast_sent> P1 = par1.get_sents();
			LinkedList<ast_sent> S1 = seq1.get_sents();
			LinkedList<ast_sent> P2 = par2.get_sents();
			LinkedList<ast_sent> S2 = seq2.get_sents();

			S1.addAll(S2);
			P1.addAll(P2);

			// now re-arrange basic blocks
			if (seq2.get_num_exits() == 1) {
				gm_gps_basic_block next = seq2.get_nth_exit(0);
				assert next != null;
				/*
				 * printf("seq2 =%d, next =%d\n", seq2->get_id(),
				 * next->get_id()); for(int j=0;j<next->get_num_entries();j++) {
				 * printf( "%d ", next->get_nth_entry(j)->get_id()); }
				 * printf("\n");
				 */

				next.update_entry_from(seq2, seq1);
				assert seq1.get_num_exits() == 1;
				seq1.remove_all_exits();
				seq1.add_exit(next, false);
			} else {
				assert seq2.get_num_exits() == 0;
				seq1.remove_all_exits();
			}

			// printf("deleting %d\n", seq2->get_id());
			// printf("deleting %d\n", par2->get_id());
			seq1.copy_info_from(seq2);
			par2.copy_info_from(par1);
			if (seq2 != null)
				seq2.dispose();
			if (par2 != null)
				par2.dispose();
		}
	}

	private static void find_pattern_and_merge_bb(LinkedList<gm_gps_basic_block> current_list) {
		if (current_list.size() < 4)
			return;

		gm_gps_basic_block par1 = null;
		gm_gps_basic_block seq1 = null;
		gm_gps_basic_block par2 = null;
		gm_gps_basic_block seq2 = null;

		// reverse iteration
		for (int i = current_list.size() - 1; i >= 0; i--) {
			gm_gps_basic_block curr = current_list.get(i);
			// printf("curr = %d\n", curr->get_id());
			assert curr.get_num_entries() <= 1;
			assert curr.get_num_exits() <= 1;
			if (seq2 == null) {
				if (curr.get_type() != gm_gps_bbtype_t.GM_GPS_BBTYPE_SEQ) {
					continue;
				} else {
					seq2 = curr;
					continue;
				}
			}

			else if (par2 == null) {
				if (curr.get_type() == gm_gps_bbtype_t.GM_GPS_BBTYPE_BEGIN_VERTEX) {
					par2 = curr;
					continue;
				} else if (curr.get_type() == gm_gps_bbtype_t.GM_GPS_BBTYPE_SEQ) {
					seq2 = curr;
					par2 = null;
					continue;
				} else {
					seq2 = par2 = null;
					continue;
				}
			} else if (seq1 == null) {
				if (curr.get_type() == gm_gps_bbtype_t.GM_GPS_BBTYPE_SEQ) {
					seq1 = curr;
					continue;
				} else {
					seq1 = seq2 = par2 = null;
					continue;
				}
			} else if (par1 == null) {
				if (curr.get_type() == gm_gps_bbtype_t.GM_GPS_BBTYPE_BEGIN_VERTEX) {
					par1 = curr; // go through
				} else if (curr.get_type() == gm_gps_bbtype_t.GM_GPS_BBTYPE_SEQ) {
					seq2 = curr;
					par1 = par2 = seq1 = null;
					continue;
				} else {
					par1 = seq1 = seq2 = par2 = null;
					continue;
				}
			}

			/*
			 * printf("checking %d %d %d %d\n", par1->get_id(), seq1->get_id(),
			 * par2->get_id(), seq2->get_id());
			 */
			assert par1 != null;
			check_and_merge_bb_main(par1, seq1, par2, seq2);

			par2 = par1;
			seq2 = seq1;
			par1 = null;
			seq1 = null;
		}
	}

	private static void find_linear_segments(gm_gps_basic_block current, LinkedList<gm_gps_basic_block> current_list,
			LinkedList<LinkedList<gm_gps_basic_block>> all_lists, HashSet<gm_gps_basic_block> visited) {

		if (visited.contains(current)) {
			if (current_list.size() > 0) {
				all_lists.addLast(current_list); // end of segment
			}
			return; // already visited
		}

		visited.add(current);

		boolean finish_current_list_exclusive = (current.get_num_entries() > 1) || (current.get_num_exits() > 1);
		boolean finish_current_list_inclusive = (current.get_num_entries() == 1) && (current.get_num_exits() == 0);

		boolean finish_current_list = finish_current_list_inclusive || finish_current_list_exclusive;

		boolean continue_current_list = !finish_current_list;

		if (continue_current_list || finish_current_list_inclusive) {
			current_list.addLast(current);
		}

		if (continue_current_list) {
			//TODO bug! exit 0 does not always exist
			if (current.get_num_exits() > 0) {
				find_linear_segments(current.get_nth_exit(0), current_list, all_lists, visited);
			}
		} // finish_current_list
		else {
			if (current_list.size() > 0) // end of segment
				all_lists.addLast(current_list);

			for (int i = 0; i < current.get_num_exits(); i++) {
				gm_gps_basic_block next = current.get_nth_exit(i);
				LinkedList<gm_gps_basic_block> new_list = new LinkedList<gm_gps_basic_block>();
				find_linear_segments(next, new_list, all_lists, visited);
			}
		}
	}
}