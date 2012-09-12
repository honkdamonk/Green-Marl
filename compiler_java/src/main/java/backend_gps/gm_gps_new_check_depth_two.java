package backend_gps;

import inc.gm_compile_step;
import ast.ast_procdef;

public class gm_gps_new_check_depth_two extends gm_compile_step
{
	private gm_gps_new_check_depth_two()
	{
		set_description("Checking if has max two foreach depths");
	}
	public void process(ast_procdef proc)
	{
		// analyze_symbol_scope should be done before.
		gps_new_check_depth_two_t T = new gps_new_check_depth_two_t();
		proc.traverse_both(T);
		set_okay(!T.is_error());
    
		return;
	}
	@Override
	public gm_compile_step get_instance()
	{
		return new gm_gps_new_check_depth_two();
	}
	public static gm_compile_step get_factory()
	{
		return new gm_gps_new_check_depth_two();
	}
}