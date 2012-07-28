package frontend;

import inc.gm_compile_step;
import ast.ast_procdef;

public class gm_fe_typecheck_step5 extends gm_compile_step {
	
	private gm_fe_typecheck_step5() {
		set_description("Typecheck: check assignments and call-sites");
	}

	public void process(ast_procdef p) {
		gm_typechecker_stage_5 T = new gm_typechecker_stage_5();
		T.set_return_type(p.get_return_type());
		p.traverse_post(T);

		gm_fe_check_argmin_lhs_consistency T2 = new gm_fe_check_argmin_lhs_consistency();
		p.traverse_pre(T2);

		set_okay(T.is_okay() && T2.is_okay());
		if (T.is_okay() && T2.is_okay()) {
			GlobalMembersGm_coercion.gm_insert_explicit_type_conversion_for_assign_or_return(T.coercion_targets);
		}
	}

	@Override
	public gm_compile_step get_instance() {
		return new gm_fe_typecheck_step5();
	}

	public static gm_compile_step get_factory() {
		return new gm_fe_typecheck_step5();
	}
}