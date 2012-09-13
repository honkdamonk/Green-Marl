package backend_cpp;

import ast.ast_procdef;
import inc.gm_compile_step;

class gm_cpp_gen_regular extends gm_compile_step {
	
	private gm_cpp_gen_regular() {
		set_description("Regularize code structure for code generation");
	}

	public void process(ast_procdef proc) {
		cpp_gen_regular_1_t T = new cpp_gen_regular_1_t();
		proc.traverse_pre(T);
	}

	@Override
	public gm_compile_step get_instance() {
		return new gm_cpp_gen_regular();
	}

	public static gm_compile_step get_factory() {
		return new gm_cpp_gen_regular();
	}
}