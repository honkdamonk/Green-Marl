package opt;

import ast.ast_procdef;

public class gm_ind_opt_remove_unused_scalar
{
	public void process(ast_procdef proc)
	{
    
		gm_opt_check_used_t T = new gm_opt_check_used_t();
		proc.traverse_pre(T);
    
		gm_opt_remove_unused_t T2 = new gm_opt_remove_unused_t(T.get_used_set());
		proc.traverse_pre(T2);
	}
}