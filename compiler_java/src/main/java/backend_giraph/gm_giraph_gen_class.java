package backend_giraph;

import inc.gm_compile_step;
import ast.ast_procdef;

import common.gm_main;

//-------------------------------------------
// [Step 1]
// Add delaration here
// declaration of optimization steps
//-------------------------------------------
public class gm_giraph_gen_class extends gm_compile_step {

	private gm_giraph_gen_class() {
		set_description("Generate Code");
	}

	@Override
	public void process(ast_procdef proc) {
		gm_main.PREGEL_BE.generate_proc(proc);
	}

	@Override
	public gm_compile_step get_instance() {
		return new gm_giraph_gen_class();
	}

	public static gm_compile_step get_factory() {
		return new gm_giraph_gen_class();
	}
}
// -------------------------------------------
// [Step 2]
// Implement the definition in seperate files
// -------------------------------------------

// ------------------------------------------------------
// [Step 3]
// Include initialization in gm_giraph_gen.cc
// ------------------------------------------------------

