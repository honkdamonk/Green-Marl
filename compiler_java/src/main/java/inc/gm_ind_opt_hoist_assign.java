package inc;

import opt.gm_hoist_assign_up_t;
import ast.ast_procdef;

import common.gm_traverse;

public class gm_ind_opt_hoist_assign extends gm_compile_step
{
	private gm_ind_opt_hoist_assign()
	{
		set_description("Move assign statments");
	}
	public void process(ast_procdef p)
	{
		gm_hoist_assign_up_t T1 = new gm_hoist_assign_up_t();
		gm_traverse.gm_traverse_sents(p, T1, gm_traverse.GM_POST_APPLY); // hoist const defs up
    
		// host final defs down
	}
	@Override
	public gm_compile_step get_instance()
	{
		return new gm_ind_opt_hoist_assign();
	}
	public static gm_compile_step get_factory()
	{
		return new gm_ind_opt_hoist_assign();
	}
}