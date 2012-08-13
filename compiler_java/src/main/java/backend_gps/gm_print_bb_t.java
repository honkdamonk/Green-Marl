package backend_gps;

import ast.ast_procdef;
import inc.gm_compile_step;

import common.GlobalMembersGm_main;

public class gm_print_bb_t extends gm_compile_step {
	
	@Override
	public gm_compile_step get_instance() {
		return new gm_print_bb_t();
	}

	@Override
	public void process(ast_procdef p) {
		gm_gps_beinfo info = (gm_gps_beinfo) GlobalMembersGm_main.FE.get_backend_info(p);
		if (info == null)
			return;
		if (info.get_entry_basic_block() == null)
			return;
		gps_bb_print_all(info.get_entry_basic_block());
	}
	
	// return or of has_changed
	private static void gps_bb_print_all(gm_gps_basic_block entry) {
		gps_print_apply G = new gps_print_apply();
		GlobalMembersGm_gps_misc.gps_bb_apply_only_once(entry, G);
	}
}