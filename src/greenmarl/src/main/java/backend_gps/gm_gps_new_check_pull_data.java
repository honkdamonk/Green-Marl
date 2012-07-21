package backend_gps;

import inc.gm_compile_step;
import ast.ast_procdef;

public class gm_gps_new_check_pull_data extends gm_compile_step
{
	private gm_gps_new_check_pull_data()
	{
		set_description("Checking if there exist data pulling");
	}
	public void process(ast_procdef proc)
	{
		// check pull-syntax 
		gm_gps_new_check_pull_syntax_t T = new gm_gps_new_check_pull_syntax_t();
		proc.traverse_pre(T);
		set_okay(!T.is_error());
    
		return;
	}
	@Override
	public gm_compile_step get_instance()
	{
		return new gm_gps_new_check_pull_data();
	}
	public static gm_compile_step get_factory()
	{
		return new gm_gps_new_check_pull_data();
	}
}