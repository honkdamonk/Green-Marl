package backend_gps;

import inc.gm_compile_step;
import ast.ast_procdef;

import common.GlobalMembersGm_main;

public class gm_gps_opt_split_comm_ebb extends gm_compile_step
{
	private gm_gps_opt_split_comm_ebb()
	{
		set_description("Split communicatining EBBs");
	}
	public void process(ast_procdef p)
	{
		gm_gps_beinfo info = (gm_gps_beinfo) GlobalMembersGm_main.FE.get_backend_info(p);
		gm_gps_basic_block entry = info.get_entry_basic_block();
    
		//-------------------------------------------
		// find Basic Blocks that contain nested communication
		//-------------------------------------------
		gps_find_comm_vertex_bb T = new gps_find_comm_vertex_bb(info);
		GlobalMembersGm_gps_misc.gps_bb_apply_only_once(entry, T);
    
		java.util.HashSet<gm_gps_basic_block> BB_list = T.get_target_basic_blocks();
    
		//-------------------------------------------
		// split BB into two
		///  BB => 
		//   BB1 (send) -> seq -> BB2 (receive) 
		//-------------------------------------------
		for (gm_gps_basic_block BB : BB_list)
		{
			gm_gps_basic_block BB2 = GlobalMembersGm_gps_bb_split.split_vertex_BB(BB, info);
		}
	}
	@Override
	public gm_compile_step get_instance()
	{
		return new gm_gps_opt_split_comm_ebb();
	}
	public static gm_compile_step get_factory()
	{
		return new gm_gps_opt_split_comm_ebb();
	}
}