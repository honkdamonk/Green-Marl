package frontend;

import inc.gm_compile_step;
import ast.ast_procdef;

import common.GlobalMembersGm_traverse;


public class gm_fe_reduce_error_check extends gm_compile_step
{
	private gm_fe_reduce_error_check()
	{
		set_description("Check reduction boundary errors");
	}
	public void process(ast_procdef p)
	{
		gm_check_reduce_error_t CHECK_1 = new gm_check_reduce_error_t();
		GlobalMembersGm_traverse.gm_traverse_sents(p, CHECK_1);
		set_okay(CHECK_1.is_okay);
	}
	@Override
	public gm_compile_step get_instance()
	{
		return new gm_fe_reduce_error_check();
	}
	public static gm_compile_step get_factory()
	{
		return new gm_fe_reduce_error_check();
	}
}