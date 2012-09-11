package backend_gps;

import inc.gm_compile_step;
import ast.ast_procdef;

import common.gm_main;

public class gm_gps_opt_find_congruent_message extends gm_compile_step
{
	private gm_gps_opt_find_congruent_message()
	{
		set_description("Merge congruent message classes");
	}
	public void process(ast_procdef p)
	{
		// get global information
		gm_gps_beinfo beinfo = (gm_gps_beinfo) gm_main.FE.get_backend_info(p);
    
		gm_find_congruent_t T = new gm_find_congruent_t(beinfo);
		gm_gps_basic_block entry_BB = beinfo.get_entry_basic_block();
    
		// iterate over basic blocks and find congurent message classes
		gm_gps_misc.gps_bb_apply_only_once(entry_BB, T);
    
	}
	@Override
	public gm_compile_step get_instance()
	{
		return new gm_gps_opt_find_congruent_message();
	}
	public static gm_compile_step get_factory()
	{
		return new gm_gps_opt_find_congruent_message();
	}
}