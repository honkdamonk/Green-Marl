package backend_gps;

import inc.gm_compile_step;
import ast.ast_procdef;

import common.gm_errors_and_warnings;
import common.gm_error;
import common.gm_main;

public class gm_gps_opt_check_synthesizable extends gm_compile_step
{
	private gm_gps_opt_check_synthesizable()
	{
		set_description("Check GPS synthesizable");
	}
	public void process(ast_procdef proc)
	{
		//----------------------------------
		// check condition (1) to (4)
		//----------------------------------
		gps_check_synth_t T = new gps_check_synth_t(proc);
		proc.traverse(T, true, true); // pre & post visit
		if (T.is_error())
		{
			set_okay(false);
			return; // return is_okay
		}
    
		if (!T.is_graph_defined())
		{
			gm_error.gm_backend_error(gm_errors_and_warnings.GM_ERROR_GPS_NO_GRAPH, proc.get_procname().get_line(), proc.get_procname().get_col());
    
			set_okay(false);
			return; // return is_okay
		}
    
		gps_check_synth2_t T2 = new gps_check_synth2_t();
		proc.traverse_pre(T2); // pre & post visit
		if (T2.is_error())
		{
			set_okay(false);
			return;
		}
    
		gm_gps_beinfo beinfo = (gm_gps_beinfo) gm_main.FE.get_current_backend_info();
    
		if (T2.is_rand_used())
			beinfo.set_rand_used(true);
	}
	@Override
	public gm_compile_step get_instance()
	{
		return new gm_gps_opt_check_synthesizable();
	}
	public static gm_compile_step get_factory()
	{
		return new gm_gps_opt_check_synthesizable();
	}
}
//-------------------------------------------
// [Step 2]
//   Implement the definition in seperate files
//-------------------------------------------

//------------------------------------------------------
// [Step 3]
//   Include initialization in gm_gps_opt.cc
//------------------------------------------------------


