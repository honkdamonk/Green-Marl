package inc;

import ast.ast_procdef;
import opt.gm_flip_backedge_t;

import common.GlobalMembersGm_argopts;
import common.GlobalMembersGm_main;

import frontend.GlobalMembersGm_rw_analysis;

//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP(CLASS, DESC) class CLASS : public gm_compile_step { private: CLASS() {set_description(DESC);}public: virtual void process(ast_procdef*p); virtual gm_compile_step* get_instance(){return new CLASS();} static gm_compile_step* get_factory(){return new CLASS();} };
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP_FACTORY(CLASS) CLASS::get_factory()


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
		if (GlobalMembersGm_main.OPTIONS.get_arg_bool(GlobalMembersGm_argopts.GMARGFLAG_FLIP_BFSUP) == false)
			return;
    
		gm_flip_backedge_t T = new gm_flip_backedge_t();
		p.traverse_pre(T);
		boolean changed = T.post_process();
    
		// re-do rw_analysis
		if (changed)
			GlobalMembersGm_rw_analysis.gm_redo_rw_analysis(p.get_body());
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