package frontend;

import ast.ast_procdef;
import inc.gm_compile_step;

public class gm_fe_typecheck_step1 extends gm_compile_step
{
	private gm_fe_typecheck_step1()
	{
		set_description("Typecheck: check symbols");
	}
	public void process(ast_procdef p)
	{
		gm_typechecker_stage_1 T = new gm_typechecker_stage_1();
		p.traverse_pre(T); // pre-apply: for SENT and for EXPR
		set_okay(T.is_okay());
	}
	@Override
	public gm_compile_step get_instance()
	{
		return new gm_fe_typecheck_step1();
	}
	public static gm_compile_step get_factory()
	{
		return new gm_fe_typecheck_step1();
	}
}