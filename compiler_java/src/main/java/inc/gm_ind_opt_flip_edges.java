package inc;

import ast.ast_procdef;
import opt.gm_flip_edges;
import opt.gm_flip_find_candidate;

import common.gm_argopts;
import common.gm_main;
import common.gm_traverse;

public class gm_ind_opt_flip_edges extends gm_compile_step
{
	private gm_ind_opt_flip_edges()
	{
		set_description("Flipping Edges in Nested Foeach");
	}
	public void process(ast_procdef p)
	{
		// find candidates
		gm_flip_find_candidate T = new gm_flip_find_candidate();
    
		// cannot set both options
		if (gm_main.OPTIONS.is_arg_bool(gm_argopts.GMARGFLAG_FLIP_PULL))
			T.set_to_avoid_pull_computation(true);
		else if (gm_main.OPTIONS.is_arg_bool(gm_argopts.GMARGFLAG_FLIP_REVERSE))
			T.set_to_avoid_reverse_edges(true);
		else
			return; // no need to do
    
		gm_traverse.gm_traverse_sents(p, T);
    
		// apply flip
		gm_flip_edges.do_flip_edges(T.get_target());
    
		return;
    
	}
	@Override
	public gm_compile_step get_instance()
	{
		return new gm_ind_opt_flip_edges();
	}
	public static gm_compile_step get_factory()
	{
		return new gm_ind_opt_flip_edges();
	}
}