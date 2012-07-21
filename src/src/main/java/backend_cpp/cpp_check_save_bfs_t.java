package backend_cpp;

import ast.AST_NODE_TYPE;
import ast.ast_bfs;
import ast.ast_foreach;
import ast.ast_sent;
import inc.GlobalMembersGm_backend_cpp;
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


public class cpp_check_save_bfs_t extends gm_apply
{
	public cpp_check_save_bfs_t()
	{
		set_for_sent(true);
		set_separate_post_apply(true);
	}

	// pre
	public final boolean apply(ast_sent s)
	{
		if (s.get_nodetype() == AST_NODE_TYPE.AST_BFS)
		{
			ast_bfs bfs = (ast_bfs) s;
			if (bfs.is_bfs())
				L.addFirst(bfs);
		}
		else if (s.get_nodetype() == AST_NODE_TYPE.AST_FOREACH)
		{
			ast_foreach fe = (ast_foreach) s;
			if (GlobalMembersGm_defs.gm_is_iteration_on_down_neighbors(fe.get_iter_type()))
			{
				ast_bfs bfs = L.getFirst();
				bfs.add_info_bool(GlobalMembersGm_backend_cpp.CPPBE_INFO_USE_DOWN_NBR, true);
			}
		}
		return true;
	}

	public final boolean apply2(ast_sent s)
	{
		if (s.get_nodetype() == AST_NODE_TYPE.AST_BFS)
		{
			ast_bfs bfs = (ast_bfs) s;
			if (bfs.is_bfs())
				L.removeFirst();
		}
		return true;
	}
	private java.util.LinkedList<ast_bfs> L = new java.util.LinkedList<ast_bfs>();
}