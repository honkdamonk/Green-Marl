package frontend;

import java.util.HashMap;

import inc.GMTYPE_T;
import inc.gm_compile_step;
import ast.ast_node_type;
import ast.ast_assign;
import ast.ast_expr;
import ast.ast_node;
import ast.ast_procdef;
import ast.ast_return;

public class gm_fe_typecheck_step5 extends gm_compile_step {

	private gm_fe_typecheck_step5() {
		set_description("Typecheck: check assignments and call-sites");
	}

	@Override
	public void process(ast_procdef p) {
		gm_typechecker_stage_5 T = new gm_typechecker_stage_5();
		T.set_return_type(p.get_return_type());
		p.traverse_post(T);

		gm_fe_check_argmin_lhs_consistency T2 = new gm_fe_check_argmin_lhs_consistency();
		p.traverse_pre(T2);

		set_okay(T.is_okay() && T2.is_okay());
		if (T.is_okay() && T2.is_okay()) {
			insert_explicit_type_conversion_for_assign_or_return(T.coercion_targets);
		}
	}

	@Override
	public gm_compile_step get_instance() {
		return new gm_fe_typecheck_step5();
	}

	public static gm_compile_step get_factory() {
		return new gm_fe_typecheck_step5();
	}

	private static void insert_explicit_type_conversion_for_assign_or_return(HashMap<ast_expr, GMTYPE_T> coercion_targets) {
		for (ast_expr t : coercion_targets.keySet()) {
			GMTYPE_T dest_type = coercion_targets.get(t);

			assert t.get_up_op() == null;
			ast_node n = t.get_parent();

			ast_expr tc = ast_expr.new_typeconv_expr(dest_type, t);

			if (n.get_nodetype() == ast_node_type.AST_ASSIGN) {
				ast_assign a = (ast_assign) n;
				a.set_rhs(tc);
			} else if (n.get_nodetype() == ast_node_type.AST_RETURN) {
				ast_return r = (ast_return) n;
				r.set_expr(tc);
			} else {
				assert false;
			}
		}

	}
}