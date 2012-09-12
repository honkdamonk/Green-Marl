package opt;

import frontend.gm_rw_analysis;
import ast.ast_procdef;

public class gm_ind_opt_propagate_trivial_writes
{
//C++ TO JAVA CONVERTER WARNING: The original C++ declaration of the following method implementation was not found:
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
}