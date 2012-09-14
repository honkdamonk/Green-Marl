package frontend;

import inc.gm_compile_step;
import ast.ast_procdef;

public class gm_fe_typecheck_step2 extends gm_compile_step {

	private gm_fe_typecheck_step2() {
		set_description("Typecheck: find function calls");
	}

	@Override
	public void process(ast_procdef p) {
		gm_typechecker_stage_2 T = new gm_typechecker_stage_2();
		p.traverse_both(T); // pre and post apply
		set_okay(T.is_okay());
	}

	@Override
	public gm_compile_step get_instance() {
		return new gm_fe_typecheck_step2();
	}

	public static gm_compile_step get_factory() {
		return new gm_fe_typecheck_step2();
	}

}