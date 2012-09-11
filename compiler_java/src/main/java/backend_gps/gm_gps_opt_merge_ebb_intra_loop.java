package backend_gps;

import static backend_gps.GPSConstants.GPS_FLAG_IS_INTRA_MERGED_CONDITIONAL;
import static backend_gps.GPSConstants.GPS_INT_INTRA_MERGED_CONDITIONAL_NO;
import static backend_gps.GPSConstants.GPS_LIST_INTRA_MERGED_CONDITIONAL;
import inc.gm_compile_step;

import java.util.LinkedList;

import ast.ast_procdef;
import ast.ast_sent;

import common.gm_argopts;
import common.gm_main;

public class gm_gps_opt_merge_ebb_intra_loop extends gm_compile_step {
	
	private gm_gps_opt_merge_ebb_intra_loop() {
		set_description("Merging Intra-Loop EBBs");
	}

	public void process(ast_procdef p) {
		if (!gm_main.OPTIONS.get_arg_bool(gm_argopts.GMARGFLAG_MERGE_BB_INTRA))
			return;

		gm_gps_beinfo info = (gm_gps_beinfo) gm_main.FE.get_backend_info(p);
		gm_gps_basic_block entry = info.get_entry_basic_block();

		// gps_bb_print_all(entry);

		// -------------------------------------------
		// find linear (do-)whlie segments
		// -------------------------------------------
		LinkedList<gps_intra_merge_candidate_t> L = new LinkedList<gps_intra_merge_candidate_t>();
		find_linear_while_segments(entry, L);

		/*
		 * for(I=L.begin(); I!=L.end(); I++) { gps_intra_merge_candidate_t* C =
		 * *I; printf("Found cands: %d %d %d %d", C->par1->get_id(),
		 * C->seq1->get_id(), C->parn->get_id(), C->seqn->get_id()); if (C->seq0
		 * == NULL) printf("\n"); else printf(" (+%d) \n", C->seq0->get_id()); }
		 */

		// -------------------------------------------
		// find merge them
		// -------------------------------------------
		for (gps_intra_merge_candidate_t candidate : L) {
			apply_intra_merge(candidate);
		}
	}

	@Override
	public gm_compile_step get_instance() {
		return new gm_gps_opt_merge_ebb_intra_loop();
	}

	public static gm_compile_step get_factory() {
		return new gm_gps_opt_merge_ebb_intra_loop();
	}

	private static void apply_intra_merge(gps_intra_merge_candidate_t C) {
		// appl
		gm_gps_basic_block p_1 = C.par1;
		gm_gps_basic_block s_1 = C.seq1;
		gm_gps_basic_block p_n = C.parn;
		gm_gps_basic_block s_n = C.seqn;
		gm_gps_basic_block s_0 = C.seq0;
		gm_gps_basic_block while_cond = C.while_cond;

		// ---------------------------------------------------
		// merge PN/P1, SN/S1
		// ---------------------------------------------------
		// mark p_n's sents/receivers as 'conditional'
		for (ast_sent sent : p_n.get_sents())
			sent.add_info_bool(GPS_FLAG_IS_INTRA_MERGED_CONDITIONAL, true);
		for (ast_sent sent : s_n.get_sents())
			sent.add_info_bool(GPS_FLAG_IS_INTRA_MERGED_CONDITIONAL, true);

		/*
		 * // don't need this std::list<gm_gps_comm_unit>::iterator J; for(J =
		 * p_n->get_receivers().begin(); J!= p_n->get_receivers().end(); J++) {
		 * gm_gps_comm_unit& U = *J; if (U.fe != NULL) {
		 * U.fe->add_info_bool(GPS_FLAG_IS_INTRA_MERGED_CONDITIONAL, true); }
		 * else if (U.sb != NULL) {
		 * U.sb->add_info_bool(GPS_FLAG_IS_INTRA_MERGED_CONDITIONAL, true); } }
		 */

		// move p_1's sents and information to p_n (p_1 has no receivers)
		for (ast_sent sent : p_1.get_sents())
			p_n.add_sent(sent);
		for (ast_sent sent : s_1.get_sents())
			s_n.add_sent(sent);
		p_n.copy_info_from(p_1);
		s_n.copy_info_from(s_1);

		// add tags
		p_n.add_info_bool(GPS_FLAG_IS_INTRA_MERGED_CONDITIONAL, true);
		p_n.add_info_int(GPS_INT_INTRA_MERGED_CONDITIONAL_NO, while_cond.get_id());
		s_n.add_info_bool(GPS_FLAG_IS_INTRA_MERGED_CONDITIONAL, true);
		s_n.add_info_int(GPS_INT_INTRA_MERGED_CONDITIONAL_NO, while_cond.get_id());

		// create new state
		gm_gps_beinfo BEINFO = (gm_gps_beinfo) gm_main.FE.get_current_backend_info();
		gm_gps_basic_block TAIL2 = new gm_gps_basic_block(BEINFO.issue_basicblock_id(), gm_gps_bbtype_t.GM_GPS_BBTYPE_MERGED_TAIL);

		// add tag that this is merged conditional
		TAIL2.add_info_int(GPS_INT_INTRA_MERGED_CONDITIONAL_NO, while_cond.get_id());
		while_cond.add_info_bool(GPS_FLAG_IS_INTRA_MERGED_CONDITIONAL, true);
		gm_main.FE.get_current_proc().add_info_list_element(GPS_LIST_INTRA_MERGED_CONDITIONAL, while_cond);

		assert s_n.get_num_exits() == 1;
		assert s_n.get_nth_exit(0) == while_cond;
		assert s_1.get_num_entries() == 1;

		// -------------------------------------------------------------
		// Now re-arrange connections
		// -------------------------------------------------------------
		gm_gps_basic_block org_exit = s_n.get_nth_exit(0);
		gm_gps_basic_block p_2 = s_1.get_nth_exit(0); // p_2 may be p_n

		boolean only_two_states = (p_2 == p_n);
		boolean has_s_0 = (s_0 != null);

		if (has_s_0)
			assert s_0.get_nth_exit(0) == p_1;
		else
			assert p_1.get_num_entries() == 1;

		assert p_n.get_num_entries() == 1;
		gm_gps_basic_block while_entry = p_1.get_nth_entry(0);
		gm_gps_basic_block s_n_1 = p_n.get_nth_entry(0);

		// -------------------------------------
		// case 1: S_0, TWO_STATES
		// -------------------------------------
		if (has_s_0) {
			// (only two states)
			// HEAD -> SEQ0 -> P1 -> S1 -> PN -> SN -> ORG_EXIT
			// ==>
			// HEAD -> SEQ0 => PN/P1 -> SN/S1 => TAIL2 => SEQ0
			// => ORG_EXIT
			// (more than 3 states)
			// HEAD -> SEQ0 -> P1 -> S1 -> P2-> ... -> SN-1 ->PN -> SN ->
			// ORG_EXIT
			// ==>
			// HEAD -> SEQ0 => PN/P1 -> SN/S1 => TAIL2 => P2 -> .. -> SN-1 =>
			// SEQ0
			// => ORG_EXIT

			// SEQ0 => PN
			s_0.update_exit_to(p_1, p_n);
			p_n.update_entry_from(s_n_1, s_0);

			// SN => TAIL2
			s_n.update_exit_to(org_exit, TAIL2);
			TAIL2.add_entry(s_n);

			// is_first case
			if (only_two_states) {
				// TAIL2=>S_0
				TAIL2.add_exit(s_0, false);
				s_0.add_entry(TAIL2);
			} else {
				// TAiL2 => P2
				TAIL2.add_exit(p_2, false);
				p_2.update_entry_from(s_1, TAIL2);

				// S_N-1 => SEQ0
				s_n_1.update_exit_to(p_n, s_0);
				s_0.add_entry(s_n_1);
			}

			// TAIL2=>ORG_EXIT (!is_first)
			TAIL2.add_exit(org_exit);
			org_exit.update_entry_from(s_n, TAIL2);

		} else {
			// (only two states)
			// HEAD -> P1 -> S1 -> PN -> SN -> ORG_EXIT
			// ==>
			// HEAD => PN/P1 -> SN/S1 => TAIL2 => PN
			// => ORG_EXIT
			// (more than 3 states)
			// HEAD -> P1 -> S1 -> P2-> ... -> SN-1 ->PN -> SN -> ORG_EXIT
			// ==>
			// HEAD => PN/P1 -> SN/S1 => TAIL2 => P2 -> .. -> SN-1 -> PN
			// => ORG_EXIT

			// Head => PN
			while_entry.update_exit_to(p_1, p_n);
			p_n.update_entry_from(s_n_1, while_entry);

			// SN => TAIL2
			s_n.update_exit_to(org_exit, TAIL2);
			TAIL2.add_entry(s_n);

			// TAiL2 => P2 (P2 == PN if only two states)
			TAIL2.add_exit(p_2, false);
			if (only_two_states) {
				p_2.add_entry(TAIL2);
			} else {
				p_2.update_entry_from(s_1, TAIL2);
			}

			// TAIL2=>ORG_EXIT (!is_first)
			TAIL2.add_exit(org_exit);
			org_exit.update_entry_from(s_n, TAIL2);

		}

		// delete states
		if (p_1 != null)
			p_1.dispose();
		if (s_1 != null)
			s_1.dispose();
	}
	
	private static void find_linear_while_segments(gm_gps_basic_block entry, LinkedList<gps_intra_merge_candidate_t> L) {
		// apply this in DFS traversal of basic blocks
		gps_find_intra_merge_candidate_t T = new gps_find_intra_merge_candidate_t(L);
		// in DFS order
		gm_gps_misc.gps_bb_apply_only_once(entry, T);
	}

}