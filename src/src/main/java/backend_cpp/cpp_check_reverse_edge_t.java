package backend_cpp;

import ast.AST_NODE_TYPE;
import ast.ast_bfs;
import ast.ast_expr;
import ast.ast_expr_builtin;
import ast.ast_foreach;
import ast.ast_id;
import ast.ast_sent;
import frontend.gm_symtab_entry;
import inc.GMTYPE_T;
import inc.GlobalMembersGm_backend_cpp;
import inc.GlobalMembersGm_defs;

import common.GlobalMembersGm_builtin;
import common.gm_apply;
import common.gm_builtin_def;

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

//-------------------------------------------------------------
// Misc checks before code generation
//   (1) Mark graph symbols if it uses reverse edges
//   (2) Mark graph symbols if it uses is_neighbor
//   (3) Mark graph symbols if it uses from/to
//-------------------------------------------------------------
public class cpp_check_reverse_edge_t extends gm_apply
{
	public cpp_check_reverse_edge_t()
	{
		set_for_sent(true);
		set_for_expr(true);
	}
	public final boolean apply(ast_expr s)
	{
		if (s.is_builtin())
		{
			ast_expr_builtin b = (ast_expr_builtin) s;
			gm_builtin_def def = b.get_builtin_def();

			if (def.find_info_bool(GlobalMembersGm_builtin.GM_BLTIN_INFO_USE_REVERSE))
			{
				ast_id driver = b.get_driver();
				assert driver != null;
				ast_id G = driver.getTypeInfo().get_target_graph_id();
				assert G != null;
				gm_symtab_entry e = G.getSymInfo();
				e.add_info_bool(GlobalMembersGm_backend_cpp.CPPBE_INFO_USE_REVERSE_EDGE, true);
			}

			if (def.find_info_bool(GlobalMembersGm_builtin.GM_BLTIN_INFO_CHECK_NBR))
			{
				ast_id driver = b.get_driver();
				assert driver != null;
				ast_id G = driver.getTypeInfo().get_target_graph_id();
				assert G != null;
				gm_symtab_entry e = G.getSymInfo();
				e.add_info_bool(GlobalMembersGm_backend_cpp.CPPBE_INFO_NEED_SEMI_SORT, true);
			}

			if (def.find_info_bool(GlobalMembersGm_builtin.GM_BLTIN_INFO_NEED_FROM))
			{
				ast_id driver = b.get_driver();
				assert driver != null;
				ast_id G = driver.getTypeInfo().get_target_graph_id();
				assert G != null;
				gm_symtab_entry e = G.getSymInfo();
				e.add_info_bool(GlobalMembersGm_backend_cpp.CPPBE_INFO_NEED_FROM_INFO, true);
			}
		}
		return true;
	}

	public final boolean apply(ast_sent s)
	{
		if (s.get_nodetype() == AST_NODE_TYPE.AST_BFS)
		{
			ast_bfs bfs = (ast_bfs) s;
			if (bfs.is_transpose())
			{
				ast_id G = bfs.get_source();
				gm_symtab_entry e = G.getSymInfo();
				if (e.find_info_bool(GlobalMembersGm_backend_cpp.CPPBE_INFO_USE_REVERSE_EDGE) == false)
				{
					e.add_info_bool(GlobalMembersGm_backend_cpp.CPPBE_INFO_USE_REVERSE_EDGE, true);
				}
			}
		}

		if (s.get_nodetype() == AST_NODE_TYPE.AST_FOREACH)
		{
			ast_foreach fe = (ast_foreach) s;
			GMTYPE_T iter_type = fe.get_iter_type();
			if (GlobalMembersGm_defs.gm_is_iteration_use_reverse(iter_type))
			{
				ast_id G = fe.get_source().getTypeInfo().get_target_graph_id();
				if (G != null)
				{
					gm_symtab_entry e = G.getSymInfo();
					if (e.find_info_bool(GlobalMembersGm_backend_cpp.CPPBE_INFO_USE_REVERSE_EDGE) == false)
					{
						e.add_info_bool(GlobalMembersGm_backend_cpp.CPPBE_INFO_USE_REVERSE_EDGE, true);
					}
				}
			}

			if (GlobalMembersGm_defs.gm_is_common_nbr_iter_type(iter_type))
			{
				ast_id G = fe.get_source().getTypeInfo().get_target_graph_id();
				if (G != null)
				{
					gm_symtab_entry e = G.getSymInfo();
					if (e.find_info_bool(GlobalMembersGm_backend_cpp.CPPBE_INFO_NEED_SEMI_SORT) == false)
					{
						e.add_info_bool(GlobalMembersGm_backend_cpp.CPPBE_INFO_NEED_SEMI_SORT, true);
					}
				}
			}
		}
		return true;
	}
}