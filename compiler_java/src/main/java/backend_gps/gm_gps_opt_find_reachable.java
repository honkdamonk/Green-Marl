package backend_gps;

import inc.gm_compile_step;
import ast.ast_procdef;

import common.gm_main;

public class gm_gps_opt_find_reachable extends gm_compile_step {
	
	private gm_gps_opt_find_reachable() {
		set_description("Pack reachable BBs into a list");
	}

	@Override
	public void process(ast_procdef p) {
		gm_gps_beinfo info = (gm_gps_beinfo) gm_main.FE.get_backend_info(p);
		// set list of reachable BBs
		gps_get_reachable_bb_list_t T = new gps_get_reachable_bb_list_t(info.get_basic_blocks());
		gm_gps_misc.gps_bb_apply_only_once(info.get_entry_basic_block(), T);
	}

	@Override
	public gm_compile_step get_instance() {
		return new gm_gps_opt_find_reachable();
	}

	public static gm_compile_step get_factory() {
		return new gm_gps_opt_find_reachable();
	}
}