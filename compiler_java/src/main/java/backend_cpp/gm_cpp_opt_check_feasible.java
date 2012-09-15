package backend_cpp;

import inc.gm_compile_step;
import ast.ast_procdef;

//-------------------------------------------
// [Step 1]
// Add delaration here
// declaration of optimization steps
//-------------------------------------------
class gm_cpp_opt_check_feasible extends gm_compile_step {
// GM_COMPILE_STEP(gm_cpp_opt_reduce_bound,
// "Optimize reductions with sequential bound ")

	private gm_cpp_opt_check_feasible() {
		set_description("Check compiler feasiblity");
	}

	public void process(ast_procdef p) {
		check_cpp_feasible_t T = new check_cpp_feasible_t();
		p.traverse_both(T);

		set_okay(T.is_okay());
	}

	@Override
	public gm_compile_step get_instance() {
		return new gm_cpp_opt_check_feasible();
	}

	public static gm_compile_step get_factory() {
		return new gm_cpp_opt_check_feasible();
	}
}