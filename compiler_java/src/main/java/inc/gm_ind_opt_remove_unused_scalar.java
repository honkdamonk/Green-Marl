package inc;

import ast.ast_procdef;

public class gm_ind_opt_remove_unused_scalar extends gm_compile_step
{
	private gm_ind_opt_remove_unused_scalar()
	{
		set_description("");
	}

	public void process(ast_procdef proc)
	{
		assert false; // to be implemented
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