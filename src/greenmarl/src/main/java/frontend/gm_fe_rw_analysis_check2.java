package frontend;

import ast.ast_procdef;
import inc.gm_compile_step;

public class gm_fe_rw_analysis_check2 extends gm_compile_step
{
	private gm_fe_rw_analysis_check2()
	{
		set_description("Check RW conflict errors");
	}
	public void process(ast_procdef p)
	{
		set_okay(GlobalMembersGm_rw_analysis_check2.gm_check_parall_conflict_error(p.get_body()));
	}
	@Override
	public gm_compile_step get_instance()
	{
		return new gm_fe_rw_analysis_check2();
	}
	public static gm_compile_step get_factory()
	{
		return new gm_fe_rw_analysis_check2();
	}
}