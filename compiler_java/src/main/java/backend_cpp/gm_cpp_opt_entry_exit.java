package backend_cpp;

import inc.gm_compile_step;
import ast.ast_procdef;

class gm_cpp_opt_entry_exit extends gm_compile_step {
	
	private gm_cpp_opt_entry_exit() {
		set_description("Add procedure enter and exit");
	}

	@Override
	public gm_compile_step get_instance() {
		return new gm_cpp_opt_entry_exit();
	}

	public static gm_compile_step get_factory() {
		return new gm_cpp_opt_entry_exit();
	}

	// virtual void process(ast_procdef p);
	@Override
	public void process(ast_procdef p) {
		// TODO Auto-generated method stub
	}
}
// -------------------------------------------
// [Step 2]
// Implement the definition in seperate files
// -------------------------------------------

// ------------------------------------------------------
// [Step 3]
// Include initialization in following steps
// ------------------------------------------------------

