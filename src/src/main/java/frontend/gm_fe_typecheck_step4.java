package frontend;

import ast.ast_procdef;
import inc.gm_compile_step;

public class gm_fe_typecheck_step4 extends gm_compile_step
{
	private gm_fe_typecheck_step4()
	{
		set_description("Typecheck: determine size of INF");
	}
	public void process(ast_procdef p)
	{
		gm_typechecker_stage_4 T = new gm_typechecker_stage_4(p.get_return_type());
		p.traverse_pre(T);
		//return T.is_okay();
		//return true;
	}
	@Override
	public gm_compile_step get_instance()
	{
		return new gm_fe_typecheck_step4();
	}
	public static gm_compile_step get_factory()
	{
		return new gm_fe_typecheck_step4();
	}
}