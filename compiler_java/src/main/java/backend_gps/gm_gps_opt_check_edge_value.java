package backend_gps;

import inc.gm_compile_step;
import ast.ast_procdef;

public class gm_gps_opt_check_edge_value extends gm_compile_step {
	
	private gm_gps_opt_check_edge_value() {
		set_description("Check use of edge values");
	}

	@Override
	public void process(ast_procdef proc) {
		gps_check_edge_value_t T2 = new gps_check_edge_value_t();
		proc.traverse_both(T2);
		set_okay(!T2.is_error());
		return;
	}

	@Override
	public gm_compile_step get_instance() {
		return new gm_gps_opt_check_edge_value();
	}

	public static gm_compile_step get_factory() {
		return new gm_gps_opt_check_edge_value();
	}
}