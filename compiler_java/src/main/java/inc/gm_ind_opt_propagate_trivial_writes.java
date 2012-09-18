package inc;

import opt.gm_opt_propagate_trivial_write_t;
import ast.ast_procdef;
import frontend.gm_rw_analysis;

public class gm_ind_opt_propagate_trivial_writes extends gm_compile_step
{
	private gm_ind_opt_propagate_trivial_writes()
	{
		set_description("Propagate Trivial Writes");
	}

	public void process(ast_procdef proc)
	{
		boolean changed;
		do
		{
			gm_opt_propagate_trivial_write_t T = new gm_opt_propagate_trivial_write_t();
			proc.traverse_pre(T);
			T.post_process();
    
			changed = T.has_effect();
    
		} while (changed);
    
		// re-do rw_analysis
		gm_rw_analysis.gm_redo_rw_analysis(proc.get_body());
	}

	@Override
	public gm_compile_step get_instance()
	{
		return new gm_ind_opt_propagate_trivial_writes();
	}

	public static gm_compile_step get_factory()
	{
		return new gm_ind_opt_propagate_trivial_writes();
	}
}