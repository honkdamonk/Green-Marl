package inc;

import ast.ast_procdef;
import opt.gm_opt_optimize_single_reduction_t;
import opt.gm_reduce_opt_linear_t;
import frontend.gm_rw_analysis;

public class gm_ind_opt_nonconf_reduce extends gm_compile_step
{
	private gm_ind_opt_nonconf_reduce()
	{
		set_description("Optimizing non-conflicting reductions");
	}
	public void process(ast_procdef proc)
	{
    
		gm_opt_optimize_single_reduction_t T1 = new gm_opt_optimize_single_reduction_t();
		proc.traverse_pre(T1);
		T1.post_process();
    
		gm_reduce_opt_linear_t T2 = new gm_reduce_opt_linear_t();
		proc.traverse_pre(T2);
		T2.post_process();
    
		// re-do rw_analysis
		gm_rw_analysis.gm_redo_rw_analysis(proc.get_body());
    
	}
	@Override
	public gm_compile_step get_instance()
	{
		return new gm_ind_opt_nonconf_reduce();
	}
	public static gm_compile_step get_factory()
	{
		return new gm_ind_opt_nonconf_reduce();
	}
}