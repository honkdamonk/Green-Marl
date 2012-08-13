package backend_gps;

import inc.gm_compile_step;
import ast.ast_procdef;

public class gm_gps_new_analyze_scope_sent_var extends gm_compile_step {
	
	private gm_gps_new_analyze_scope_sent_var() {
		set_description("Analyzing scope of symbols and sentences");
	}

	public void process(ast_procdef proc) {
		gm_gps_new_analysis_scope_sent_var_t.gm_gps_do_new_analysis_scope_sent_var(proc);
		return;
	}

	@Override
	public gm_compile_step get_instance() {
		return new gm_gps_new_analyze_scope_sent_var();
	}

	public static gm_compile_step get_factory() {
		return new gm_gps_new_analyze_scope_sent_var();
	}
}