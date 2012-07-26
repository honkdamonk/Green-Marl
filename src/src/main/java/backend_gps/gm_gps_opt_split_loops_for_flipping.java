package backend_gps;

import inc.gm_compile_step;
import ast.ast_foreach;
import ast.ast_procdef;

import common.GlobalMembersGm_flat_nested_sentblock;

import frontend.GlobalMembersGm_rw_analysis;

public class gm_gps_opt_split_loops_for_flipping extends gm_compile_step
{
	private gm_gps_opt_split_loops_for_flipping()
	{
		set_description("(Pre-Flip) Splitting loops");
	}
	public void process(ast_procdef p)
	{
		//-------------------------------------
		// Find nested loops
		//-------------------------------------
		java.util.HashMap<ast_foreach, ast_foreach> MAP = new java.util.HashMap<ast_foreach, ast_foreach>();
		java.util.HashSet<ast_foreach> SET = new java.util.HashSet<ast_foreach>();
		GlobalMembersGm_gps_opt_find_nested_foreach_loops.gm_gps_find_double_nested_loops(p, MAP);
    
		//-------------------------------------
		// Find target inner loops
		//-------------------------------------
		GlobalMembersGm_gps_opt_split_loops_for_flipping.filter_target_loops(MAP, SET);
    
		//-------------------------------------
		//  - Now split the loops
		//-------------------------------------
		for (ast_foreach fe : SET)
		{
			GlobalMembersGm_gps_opt_split_loops_for_flipping.split_the_loop(fe);
		}
    
		GlobalMembersGm_flat_nested_sentblock.gm_flat_nested_sentblock(p);
    
		// reconstruct_scope implied in flattening
		//gm_reconstruct_scope(p);  
    
		//-------------------------------------
		// Re-do RW analysis
		//-------------------------------------
		GlobalMembersGm_rw_analysis.gm_redo_rw_analysis(p.get_body());
    
	}
	@Override
	public gm_compile_step get_instance()
	{
		return new gm_gps_opt_split_loops_for_flipping();
	}
	public static gm_compile_step get_factory()
	{
		return new gm_gps_opt_split_loops_for_flipping();
	}
}