package backend_gps;

import inc.gm_compile_step;
import ast.ast_procdef;

public class gm_gps_new_analyze_scope_rhs_lhs extends gm_compile_step {
	
	private gm_gps_new_analyze_scope_rhs_lhs() {
		set_description("Analyzing scope of rhs and lhs");
	}

	public void process(ast_procdef proc) {
		GlobalMembersGm_gps_new_analysis_scope_rhs_lhs.gm_gps_do_new_analysis_rhs_lhs(proc);
		return;
	}

	@Override
	public gm_compile_step get_instance() {
		return new gm_gps_new_analyze_scope_rhs_lhs();
	}

	public static gm_compile_step get_factory() {
		return new gm_gps_new_analyze_scope_rhs_lhs();
	}
}