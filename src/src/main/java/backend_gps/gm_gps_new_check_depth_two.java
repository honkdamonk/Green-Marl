package backend_gps;

import inc.gm_compile_step;
import ast.ast_procdef;

import common.GM_ERRORS_AND_WARNINGS;
import common.GlobalMembersGm_error;
import common.GlobalMembersGm_main;

public class gm_gps_new_check_depth_two extends gm_compile_step
{
	private gm_gps_new_check_depth_two()
	{
		set_description("Checking if has max two foreach depths");
	}
	public void process(ast_procdef proc)
	{
		// Check number of procedure name is same to the filename
		String fname = GlobalMembersGm_main.PREGEL_BE.getFileName();
		assert fname != null;
		if (strcmp(proc.get_procname().get_genname(), fname) != 0)
		{
			GlobalMembersGm_error.gm_backend_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_GPS_PROC_NAME, proc.get_procname().get_genname(), fname);
			set_okay(false);
			return;
		}
    
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