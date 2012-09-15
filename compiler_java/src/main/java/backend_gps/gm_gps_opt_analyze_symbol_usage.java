package backend_gps;

import inc.gm_compile_step;
import ast.ast_procdef;

import common.gm_main;

public class gm_gps_opt_analyze_symbol_usage extends gm_compile_step {
	
	private gm_gps_opt_analyze_symbol_usage() {
		set_description("Analyze Symbol Usage Information");
	}

	@Override
	public void process(ast_procdef p) {
		gm_gps_beinfo beinfo = (gm_gps_beinfo) gm_main.FE.get_backend_info(p);
		gm_gps_basic_block entry_BB = beinfo.get_entry_basic_block();
		assert p != null;
		assert entry_BB != null;

		// traverse BB
		gps_merge_symbol_usage_t T = new gps_merge_symbol_usage_t(beinfo);
		gm_gps_misc.gps_bb_traverse_ast(entry_BB, T, true, true);

		set_okay(true);
	}

	@Override
	public gm_compile_step get_instance() {
		return new gm_gps_opt_analyze_symbol_usage();
	}

	public static gm_compile_step get_factory() {
		return new gm_gps_opt_analyze_symbol_usage();
	}
}