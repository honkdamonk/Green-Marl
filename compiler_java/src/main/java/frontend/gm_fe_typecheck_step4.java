package frontend;

import inc.gm_compile_step;
import ast.ast_procdef;

/**
 * <b>Type-check Step 4:</b><br>
 * Resolve the size of INF types from LHS Example)<br>
 * <dd>Int A = +INF; // +INF must be Integer infinity.<br>
 * 
 * Also NIL type is resolved as well.<br> 
 * <dd>NIL UNKNOWN => NIL NODE or NIL EDGE
 */
public class gm_fe_typecheck_step4 extends gm_compile_step {

	private gm_fe_typecheck_step4() {
		set_description("Typecheck: determine size of INF");
	}

	public void process(ast_procdef p) {
		gm_typechecker_stage_4 T = new gm_typechecker_stage_4(p.get_return_type());
		p.traverse_pre(T);
	}

	@Override
	public gm_compile_step get_instance() {
		return new gm_fe_typecheck_step4();
	}

	public static gm_compile_step get_factory() {
		return new gm_fe_typecheck_step4();
	}

}