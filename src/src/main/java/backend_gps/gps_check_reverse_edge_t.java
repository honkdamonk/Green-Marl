package backend_gps;

import ast.AST_NODE_TYPE;
import ast.ast_expr;
import ast.ast_expr_builtin;
import ast.ast_foreach;
import ast.ast_sent;

import common.gm_apply;
import common.gm_method_id_t;

import frontend.gm_symtab_entry;

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

public class gps_check_reverse_edge_t extends gm_apply
{
	public gps_check_reverse_edge_t()
	{
		set_for_sent(true);
		set_for_expr(true);
		r_edge = false;
		r_degree = false;
		target_graph = null;
	}
	public final boolean apply(ast_sent s)
	{
		if (s.get_nodetype() == AST_NODE_TYPE.AST_FOREACH)
		{
			ast_foreach fe = (ast_foreach) s;
			if (fe.get_iter_type().is_iteration_use_reverse())
			{
				target_graph = fe.get_iterator().getTypeInfo().get_target_graph_sym();
				r_edge = true;
			}
		}
		return true;
	}
	public final boolean apply(ast_expr e)
	{
		if (e.is_builtin())
		{
			ast_expr_builtin b = (ast_expr_builtin) e;
			if (b.get_builtin_def().get_method_id() == gm_method_id_t.GM_BLTIN_NODE_IN_DEGREE)
			{
				target_graph = b.get_driver().getTypeInfo().get_target_graph_sym();
				r_degree = true;
			}
		}
		return true;
	}

	public final boolean use_in_degree()
	{
		return r_degree;
	}
	public final boolean use_rev_edge()
	{
		return r_edge;
	}
	public final gm_symtab_entry get_target_graph()
	{
		return target_graph;
	}

	private boolean r_edge;
	private boolean r_degree;
	private gm_symtab_entry target_graph;
}