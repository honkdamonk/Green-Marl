package backend_gps;

import inc.gm_compile_step;
import ast.ast_bfs;
import ast.ast_procdef;

import common.GlobalMembersGm_flat_nested_sentblock;

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
public class gm_gps_opt_transform_bfs extends gm_compile_step
//GM_COMPILE_STEP(gm_gps_opt_find_nested_loops_test, "test find nested loops")
{
	private gm_gps_opt_transform_bfs()
	{
		set_description("Transform BFS into while and foreach");
	}
	public void process(ast_procdef p)
	{
    
		gps_opt_find_bfs_t T = new gps_opt_find_bfs_t();
		p.traverse_both(T);
		for (ast_bfs b : T.get_targets())
		{
			GlobalMembersGm_gps_opt_transform_bfs.gm_gps_rewrite_bfs(b);
		}
    
		GlobalMembersGm_flat_nested_sentblock.gm_flat_nested_sentblock(p);
    
		GlobalMembersGm_rw_analysis.gm_redo_rw_analysis(p.get_body());
	}
	@Override
	public gm_compile_step get_instance()
	{
		return new gm_gps_opt_transform_bfs();
	}
	public static gm_compile_step get_factory()
	{
		return new gm_gps_opt_transform_bfs();
	}
}