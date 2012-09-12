package inc;

import opt.gm_opt_check_used_t;
import opt.gm_opt_remove_unused_t;
import ast.ast_procdef;

public class gm_ind_opt_remove_unused_scalar extends gm_compile_step
{
	private gm_ind_opt_remove_unused_scalar()
	{
		set_description("");
	}

	public void process(ast_procdef proc)
	{
		gm_opt_check_used_t T = new gm_opt_check_used_t();
		proc.traverse_pre(T);
    
		gm_opt_remove_unused_t T2 = new gm_opt_remove_unused_t(T.get_used_set());
		proc.traverse_pre(T2);
	}

	@Override
	public gm_compile_step get_instance()
	{
		return new gm_ind_opt_remove_unused_scalar();
	}

	public static gm_compile_step get_factory()
	{
		return new gm_ind_opt_remove_unused_scalar();
	}
}