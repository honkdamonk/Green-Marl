package frontend;

import inc.gm_compile_step;
import ast.ast_procdef;

//-------------------------------------------
// [Step 1]
// Add delaration here
//-------------------------------------------
public class gm_fe_check_syntax_rules extends gm_compile_step {
	// the following step is only applied during debug

	private gm_fe_check_syntax_rules() {
		set_description("Check syntax rules");
	}

	@Override
	public void process(ast_procdef p) {
		gm_check_par_return_t T = new gm_check_par_return_t();
		p.traverse_both(T);
		set_okay(T.is_okay());
	}

	@Override
	public gm_compile_step get_instance() {
		return new gm_fe_check_syntax_rules();
	}

	public static gm_compile_step get_factory() {
		return new gm_fe_check_syntax_rules();
	}
}