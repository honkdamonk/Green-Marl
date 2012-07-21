package inc;

import ast.ast_procdef;
import opt.gm_hoist_foreach_t;

import common.GlobalMembersGm_traverse;

public class gm_ind_opt_hoist_foreach extends gm_compile_step
{
	private gm_ind_opt_hoist_foreach()
	{
		set_description("Move foreach statements");
	}
	public void process(ast_procdef p)
	{
		gm_hoist_foreach_t T1 = new gm_hoist_foreach_t();
		GlobalMembersGm_traverse.gm_traverse_sents(p, T1, GlobalMembersGm_traverse.GM_POST_APPLY); // hoist const defs up
    
		// host final defs down
		//return true;
	}
	@Override
	public gm_compile_step get_instance()
	{
		return new gm_ind_opt_hoist_foreach();
	}
	public static gm_compile_step get_factory()
	{
		return new gm_ind_opt_hoist_foreach();
	}
}