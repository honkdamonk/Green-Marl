package backend_gps;

import inc.gm_compile_step;
import ast.ast_procdef;

import common.GlobalMembersGm_argopts;
import common.GlobalMembersGm_main;

public class gm_gps_opt_merge_ebb_intra_loop extends gm_compile_step
{
	private gm_gps_opt_merge_ebb_intra_loop()
	{
		set_description("Merging Intra-Loop EBBs");
	}
	public void process(ast_procdef p)
	{
		if (!GlobalMembersGm_main.OPTIONS.get_arg_bool(GlobalMembersGm_argopts.GMARGFLAG_MERGE_BB_INTRA))
			return;
    
		gm_gps_beinfo info = (gm_gps_beinfo) GlobalMembersGm_main.FE.get_backend_info(p);
		gm_gps_basic_block entry = info.get_entry_basic_block();
    
		//gps_bb_print_all(entry);
    
		//-------------------------------------------
		// find linear (do-)whlie segments
		//-------------------------------------------
		java.util.LinkedList<gps_intra_merge_candidate_t> L = new java.util.LinkedList<gps_intra_merge_candidate_t>();
		GlobalMembersGm_gps_bb_merge_intra_loop.find_linear_while_segments(entry, L);
    
		/*
		 for(I=L.begin(); I!=L.end(); I++) {
		 gps_intra_merge_candidate_t* C = *I;
		 printf("Found cands: %d %d %d %d",
		 C->par1->get_id(), C->seq1->get_id(),
		 C->parn->get_id(), C->seqn->get_id());
		 if (C->seq0 == NULL) printf("\n");
		 else printf(" (+%d) \n", C->seq0->get_id());
		 }
		 */
    
		//-------------------------------------------
		// find merge them
		//-------------------------------------------
		for (gps_intra_merge_candidate_t candidate: L)
		{
			GlobalMembersGm_gps_bb_merge_intra_loop.apply_intra_merge(candidate);
		}
	}
	@Override
	public gm_compile_step get_instance()
	{
		return new gm_gps_opt_merge_ebb_intra_loop();
	}
	public static gm_compile_step get_factory()
	{
		return new gm_gps_opt_merge_ebb_intra_loop();
	}
}