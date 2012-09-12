package backend_gps;

import inc.gm_compile_step;
import ast.ast_procdef;

public class gm_gps_opt_edge_iteration extends gm_compile_step {
	private gm_gps_opt_edge_iteration() {
		set_description("Transform Edge Iteration");
	}

	public void process(ast_procdef p) {
		assert false; /* to be implemented */
	}

	@Override
	public gm_compile_step get_instance() {
		return new gm_gps_opt_edge_iteration();
	}

	public static gm_compile_step get_factory() {
		return new gm_gps_opt_edge_iteration();
	}

}