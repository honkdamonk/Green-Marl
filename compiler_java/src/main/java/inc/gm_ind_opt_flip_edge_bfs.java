package inc;

import ast.ast_procdef;
import opt.gm_flip_backedge_t;

import common.gm_argopts;
import common.gm_main;

import frontend.gm_rw_analysis;

//-------------------------------------------
// [Step 1]
// Add delaration here
// declaration of optimization steps
//-------------------------------------------
public class gm_ind_opt_flip_edge_bfs extends gm_compile_step
{
	private gm_ind_opt_flip_edge_bfs()
	{
		set_description("Flipping Edges in BFS");
	}
	public void process(ast_procdef p)
	{
		if (gm_main.OPTIONS.get_arg_bool(gm_argopts.GMARGFLAG_FLIP_BFSUP) == false)
			return;
    
		gm_flip_backedge_t T = new gm_flip_backedge_t();
		p.traverse_pre(T);
		boolean changed = T.post_process();
    
		// re-do rw_analysis
		if (changed)
			gm_rw_analysis.gm_redo_rw_analysis(p.get_body());
	}
	@Override
	public gm_compile_step get_instance()
	{
		return new gm_ind_opt_flip_edge_bfs();
	}
	public static gm_compile_step get_factory()
	{
		return new gm_ind_opt_flip_edge_bfs();
	}
}