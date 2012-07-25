package backend_gps;

import java.util.LinkedList;

import inc.gm_compile_step;
import ast.ast_procdef;

import common.GlobalMembersGm_argopts;
import common.GlobalMembersGm_main;

public class gm_gps_opt_merge_ebb_again extends gm_compile_step
{
	private gm_gps_opt_merge_ebb_again()
	{
		set_description("Merging EBBs");
	}
	public void process(ast_procdef p)
	{
		if (!GlobalMembersGm_main.OPTIONS.get_arg_bool(GlobalMembersGm_argopts.GMARGFLAG_MERGE_BB))
			return;
    
		gm_gps_beinfo info = (gm_gps_beinfo) GlobalMembersGm_main.FE.get_backend_info(p);
		gm_gps_basic_block entry = info.get_entry_basic_block();
    
		//gps_bb_print_all(entry);
    
		//-------------------------------------------
		// find linear segments
		//-------------------------------------------
		java.util.LinkedList<gm_gps_basic_block> current_list = new java.util.LinkedList<gm_gps_basic_block>();
		java.util.LinkedList<java.util.LinkedList<gm_gps_basic_block>> all_lists = new java.util.LinkedList<java.util.LinkedList<gm_gps_basic_block>>();
		java.util.HashSet<gm_gps_basic_block> visited = new java.util.HashSet<gm_gps_basic_block>();
		GlobalMembersGm_gps_bb_merge_again.find_linear_segments(entry, current_list, all_lists, visited);
    
		//-------------------------------------------
		// Apply State Merge
		//-------------------------------------------
		for (LinkedList<gm_gps_basic_block> CL : all_lists)
		{
			if (CL.size() == 0)
				continue;
			/*
			 //printf("//==== SEGMENT BEGIN\n");
			 for(I=CL.begin(); I!=CL.end(); I++)
			 {
			 gps_bb* b = *I;
			 assert(b->get_num_entries() <= 1);
			 // Test Print
			 //b->print();
	
			 }
			 */
    
			GlobalMembersGm_gps_bb_merge_again.find_pattern_and_merge_bb(CL);
			//printf("\n");
		}
    
	}
	@Override
	public gm_compile_step get_instance()
	{
		return new gm_gps_opt_merge_ebb_again();
	}
	public static gm_compile_step get_factory()
	{
		return new gm_gps_opt_merge_ebb_again();
	}
}