package backend_gps;

import inc.gm_compile_step;


public class gm_gps_opt_find_nested_loops_test extends gm_compile_step
{
	private gm_gps_opt_find_nested_loops_test()
	{
		set_description("test find nested loops");
	}
//	virtual void process(ast_procdef p);
	@Override
	public gm_compile_step get_instance()
	{
		return new gm_gps_opt_find_nested_loops_test();
	}
	public static gm_compile_step get_factory()
	{
		return new gm_gps_opt_find_nested_loops_test();
	}
}