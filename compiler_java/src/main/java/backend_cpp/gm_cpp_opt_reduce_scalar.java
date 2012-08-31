package backend_cpp;

import ast.ast_procdef;
import inc.gm_compile_step;

import common.gm_traverse;

import frontend.gm_rw_analysis;

public class gm_cpp_opt_reduce_scalar extends gm_compile_step
{
	private gm_cpp_opt_reduce_scalar()
	{
		set_description("Privitize reduction to scalar");
	}
	public void process(ast_procdef p)
	{
		opt_scalar_reduction_t T = new opt_scalar_reduction_t();
		gm_rw_analysis.gm_redo_rw_analysis(p.get_body());
		gm_traverse.gm_traverse_sents(p.get_body(), T);
		if (T.has_targets())
		{
			T.transform_targets();
    
			// need redo rw analysis
			gm_rw_analysis.gm_redo_rw_analysis(p.get_body());
    
			set_affected(true);
		}
	}
	@Override
	public gm_compile_step get_instance()
	{
		return new gm_cpp_opt_reduce_scalar();
	}
	public static gm_compile_step get_factory()
	{
		return new gm_cpp_opt_reduce_scalar();
	}
}