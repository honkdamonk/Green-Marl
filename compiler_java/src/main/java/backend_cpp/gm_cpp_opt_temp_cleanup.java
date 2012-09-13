package backend_cpp;

import ast.ast_procdef;
import inc.gm_compile_step;

class gm_cpp_opt_temp_cleanup extends gm_compile_step {
	
	private gm_cpp_opt_temp_cleanup() {
		set_description("Clean-up routines for temporary properties");
	}

	@Override
	public gm_compile_step get_instance() {
		return new gm_cpp_opt_temp_cleanup();
	}

	public static gm_compile_step get_factory() {
		return new gm_cpp_opt_temp_cleanup();
	}

	@Override
	public void process(ast_procdef p) {
		// TODO Auto-generated method stub
	}
}