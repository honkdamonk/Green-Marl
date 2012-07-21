package backend_gps;

import ast.AST_NODE_TYPE;
import ast.ast_bfs;
import ast.ast_foreach;
import ast.ast_node;
import ast.ast_sent;
import inc.GlobalMembersGm_backend_gps;
import inc.GlobalMembersGm_defs;

import common.gm_apply;

//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define TO_STR(X) #X
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define DEF_STRING(X) static const char *X = "X"
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP(CLASS, DESC) class CLASS : public gm_compile_step { private: CLASS() {set_description(DESC);}public: virtual void process(ast_procdef*p); virtual gm_compile_step* get_instance(){return new CLASS();} static gm_compile_step* get_factory(){return new CLASS();} };
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP_FACTORY(CLASS) CLASS::get_factory()
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define AUX_INFO(X,Y) "X"":""Y"
///#define GM_BLTIN_MUTATE_GROW 1
///#define GM_BLTIN_MUTATE_SHRINK 2
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_BLTIN_FLAG_TRUE true

//--------------------------------------------------------
//
//
//
//
//----------------------------------------------------

public class gps_opt_find_bfs_t extends gm_apply
{

	public gps_opt_find_bfs_t()
	{
	   set_for_sent(true);
	   set_separate_post_apply(true);
	   in_bfs = false;
	   current_bfs = null;
	}

	// pre
	@Override
	public boolean apply(ast_sent s)
	{
		if (s.get_nodetype() == AST_NODE_TYPE.AST_BFS)
		{
			assert!in_bfs; // no nested BFS for now
			in_bfs = true;
			current_bfs = (ast_bfs) s;
			BFS.addLast(current_bfs);
		}

		else if (s.get_nodetype() == AST_NODE_TYPE.AST_FOREACH)
		{
			ast_foreach fe = (ast_foreach) s;
			if (in_bfs)
			{
				int itt = fe.get_iter_type();
				if (GlobalMembersGm_defs.gm_is_iteration_on_down_neighbors(itt))
				{
				// check if this is forward bfs
					ast_node current = fe;
					ast_node parent = fe.get_parent();
					while (parent != current_bfs)
					{
						assert parent != null;
						current = parent;
						parent = parent.get_parent();
					}
					if (current == current_bfs.get_fbody())
					{
						current_bfs.add_info_bool(GlobalMembersGm_backend_gps.GPS_FLAG_HAS_DOWN_NBRS, true);
					}
				}
			}
		}
		return true;
	}

	// post
	@Override
	public boolean apply2(ast_sent s)
	{
		if (s.get_nodetype() == AST_NODE_TYPE.AST_BFS)
		{
			in_bfs = false;
			current_bfs = null;
		}
		return true;
	}

	public final java.util.LinkedList<ast_bfs> get_targets()
	{
		return BFS;
	}

	private boolean in_bfs;
	private ast_bfs current_bfs;
	private java.util.LinkedList<ast_bfs> BFS = new java.util.LinkedList<ast_bfs>();
}