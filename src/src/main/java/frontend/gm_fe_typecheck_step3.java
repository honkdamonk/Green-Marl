package frontend;

import inc.GMTYPE_T;
import inc.gm_compile_step;

import java.util.HashMap;

import ast.ast_expr;
import ast.ast_procdef;

public class gm_fe_typecheck_step3 extends gm_compile_step {
	private gm_fe_typecheck_step3() {
		set_description("Typecheck: resolve expression types");
	}

	public void process(ast_procdef p) {
		gm_typechecker_stage_3 T = new gm_typechecker_stage_3();
		p.traverse_post(T); // post-apply

		if (T.is_okay()) {
			insert_explicit_type_conversion_for_op(T.coercion_targets);
		}

		check_argmax_num_args_t T2 = new check_argmax_num_args_t();
		p.traverse_pre(T2);

		set_okay(T.is_okay() && T2.is_okay());

	}

	@Override
	public gm_compile_step get_instance() {
		return new gm_fe_typecheck_step3();
	}

	public static gm_compile_step get_factory() {
		return new gm_fe_typecheck_step3();
	}
	
	private static void insert_explicit_type_conversion_for_op(HashMap<ast_expr, GMTYPE_T> coercion_targets) {
		for (ast_expr t : coercion_targets.keySet()) {
			GMTYPE_T dest_type = coercion_targets.get(t);

			ast_expr up = t.get_up_op();
			assert up != null;
			boolean is_left;
			if (up.get_left_op() == t)
				is_left = true;
			else {
				assert up.get_right_op() == t;
				is_left = false;
			}

			ast_expr tc = ast_expr.new_typeconv_expr(dest_type, t);

			if (is_left) {
				up.set_left_op(tc);
			} else {
				up.set_right_op(tc);
			}
			tc.set_parent(up);
			tc.set_up_op(up);
		}
	}

}