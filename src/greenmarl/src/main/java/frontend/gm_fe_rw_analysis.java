package frontend;

import ast.ast_procdef;
import inc.gm_compile_step;
import common.GlobalMembersGm_traverse;


public class gm_fe_rw_analysis extends gm_compile_step
{
	private gm_fe_rw_analysis()
	{
		set_description("Do RW analysis");
	}
	public void process(ast_procdef p)
	{
		gm_rw_analysis RWA = new gm_rw_analysis();
		GlobalMembersGm_traverse.gm_traverse_sents(p, RWA, GlobalMembersGm_traverse.GM_POST_APPLY); // post apply
		set_okay(RWA.is_success());
	}
	@Override
	public gm_compile_step get_instance()
	{
		return new gm_fe_rw_analysis();
	}
	public static gm_compile_step get_factory()
	{
		return new gm_fe_rw_analysis();
	}
}