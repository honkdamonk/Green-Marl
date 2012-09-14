package inc;

import opt.gm_moveup_propdecl_t;
import ast.ast_procdef;
import frontend.gm_rw_analysis;

public class gm_ind_opt_move_propdecl extends gm_compile_step
{
	private gm_ind_opt_move_propdecl()
	{
		set_description("Move property declarations out of sequential loops");
	}
	public void process(ast_procdef p)
	{
		gm_moveup_propdecl_t T = new gm_moveup_propdecl_t();
		p.get_body().traverse_both(T);
		T.post_process();
    
		gm_rw_analysis.gm_redo_rw_analysis(p.get_body());
	}
	@Override
	public gm_compile_step get_instance()
	{
		return new gm_ind_opt_move_propdecl();
	}
	public static gm_compile_step get_factory()
	{
		return new gm_ind_opt_move_propdecl();
	}
}