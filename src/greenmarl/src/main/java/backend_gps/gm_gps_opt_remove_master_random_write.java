package backend_gps;

import inc.gm_compile_step;
import ast.ast_procdef;
import frontend.GlobalMembersGm_rw_analysis;

public class gm_gps_opt_remove_master_random_write extends gm_compile_step
{
	private gm_gps_opt_remove_master_random_write()
	{
		set_description("Remove master random writes");
	}
	public void process(ast_procdef p)
	{
    
		gm_gps_opt_remove_master_random_write_t T = new gm_gps_opt_remove_master_random_write_t();
		p.traverse_both(T);
		T.post_process();
    
		GlobalMembersGm_rw_analysis.gm_redo_rw_analysis(p.get_body());
	}
	@Override
	public gm_compile_step get_instance()
	{
		return new gm_gps_opt_remove_master_random_write();
	}
	public static gm_compile_step get_factory()
	{
		return new gm_gps_opt_remove_master_random_write();
	}
}