package backend_cpp;

import ast.AST_NODE_TYPE;
import ast.ast_sent;
import frontend.gm_symtab_entry;

import common.GM_ERRORS_AND_WARNINGS;
import common.gm_error;
import common.gm_apply;

//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define TO_STR(X) #X
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define DEF_STRING(X) static const char *X = "X"
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP(CLASS, DESC) class CLASS : public gm_compile_step { private: CLASS() {set_description(DESC);}public: virtual void process(ast_procdef*p); virtual gm_compile_step* get_instance(){return new CLASS();} static gm_compile_step* get_factory(){return new CLASS();} };
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP_FACTORY(CLASS) CLASS::get_factory()

public class check_cpp_feasible_t extends gm_apply
{
	public check_cpp_feasible_t()
	{
		set_for_symtab(true);
		set_for_sent(true);
		set_separate_post_apply(true);
		bfs_depth = 0;
		_okay = true;
	}

	public final boolean apply(ast_sent s)
	{
		if (s.get_nodetype() == AST_NODE_TYPE.AST_BFS)
		{
			if (bfs_depth > 0)
			{
				gm_error.gm_backend_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_CPP_UNSUPPORTED_SYNTAX, s.get_line(), s.get_col(), "nested DFS/BFS.");
				set_okay(false);
			}
			bfs_depth++;
		}
		return true;
	}
	public final boolean apply2(ast_sent s)
	{
		if (s.get_nodetype() == AST_NODE_TYPE.AST_BFS)
		{
			bfs_depth--;
		}
		return true;
	}
	public final boolean apply(gm_symtab_entry e, int gm_symtab_type)
	{
		/*
		 if (e->getType()->is_edge_iterator())
		 {
		 gm_backend_error(GM_ERROR_CPP_UNSUPPORTED_SYNTAX,
		 e->getId()->get_line(),
		 e->getId()->get_col(),
		 "Edge iteration.");
		 set_okay(false);
		 }
		 */
		return true;
	}

	public final boolean is_okay()
	{
		return _okay;
	}
	public final void set_okay(boolean b)
	{
		_okay = b;
	}
	private boolean _okay;
	private int bfs_depth;
}