package inc;

import ast.ast_procdef;
import opt.gm_merge_loop_t;
import frontend.gm_rw_analysis;

public class gm_ind_opt_loop_merge extends gm_compile_step
{
	private gm_ind_opt_loop_merge()
	{
		set_description("Merge loops");
	}
	public void process(ast_procdef proc)
	{
		gm_merge_loop_t T = new gm_merge_loop_t();
		T.do_loop_merge(proc.get_body());
    
		// re-do rw-analysis (should be done already inside loop_merge. but to be sure...)
		gm_rw_analysis.gm_redo_rw_analysis(proc.get_body());
		//return true;
	}
	@Override
	public gm_compile_step get_instance()
	{
		return new gm_ind_opt_loop_merge();
	}
	public static gm_compile_step get_factory()
	{
		return new gm_ind_opt_loop_merge();
	}
}