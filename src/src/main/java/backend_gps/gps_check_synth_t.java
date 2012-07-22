package backend_gps;

import ast.AST_NODE_TYPE;
import ast.ast_procdef;
import ast.ast_sent;
import frontend.gm_symtab_entry;
import inc.GMTYPE_T;
import inc.GlobalMembersGm_backend_gps;
import inc.GlobalMembersGm_defs;

import common.GM_ERRORS_AND_WARNINGS;
import common.GlobalMembersGm_error;
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

//------------------------------------------------
// Check basic things about if the program is synthesizable
//
//  1. BFS (yet) /DFS is not supported.
//      
//  2. Collections are not avaialble (yet). e.g. NodeSet(G) 
//
//  3. There should be one and only one Graph (as in argument)
//
//  4. There must be not 'Return' in non-master context
//
//  5. Some built-in functions are not supported
//------------------------------------------------

// check condition 1-4
public class gps_check_synth_t extends gm_apply
{
	public gps_check_synth_t(ast_procdef p)
	{
		_error = false;
		set_for_symtab(true);
		set_for_sent(true);
		set_separate_post_apply(true);

		foreach_depth = 0;
		_graph_defined = false;
		proc = p;
	}
	public final boolean is_error()
	{
		return _error;
	}

	// pre apply
	@Override
	public boolean apply(ast_sent s)
	{
		if (s.get_nodetype() == AST_NODE_TYPE.AST_BFS)
		{
			GlobalMembersGm_error.gm_backend_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_GPS_UNSUPPORTED_OP, s.get_line(), s.get_col(), "BFS or DFS");
			_error = true;
		}

		if (s.get_nodetype() == AST_NODE_TYPE.AST_RETURN)
		{
			if (foreach_depth > 0)
			{
				GlobalMembersGm_error.gm_backend_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_GPS_UNSUPPORTED_OP, s.get_line(), s.get_col(), "Return inside foreach");
				_error = true;
			}
		}

		if (s.get_nodetype() == AST_NODE_TYPE.AST_FOREACH)
		{
			foreach_depth++;
		}

		return true;
	}

	@Override
	public boolean apply2(ast_sent s)
	{
		if (s.get_nodetype() == AST_NODE_TYPE.AST_FOREACH)
		{
			foreach_depth--;
		}
		return true;
	}

	// visit entry
	@Override
	public boolean apply(gm_symtab_entry e, int symtab_type)
	{
		GMTYPE_T type_id = e.getType().get_typeid();
		if (GlobalMembersGm_defs.gm_is_collection_type(type_id))
		{
			GlobalMembersGm_error.gm_backend_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_GPS_UNSUPPORTED_COLLECTION, e.getId().get_line(), e.getId().get_col(), e.getId().get_orgname());
			_error = true;
		}

		else if (GlobalMembersGm_defs.gm_is_edge_property_type(type_id))
		{
			/*
			 gm_backend_error(
			 GM_ERROR_GPS_UNSUPPORTED_COLLECTION,
			 e->getId()->get_line(),
			 e->getId()->get_col(),
			 e->getId()->get_orgname());
			 _error = true;
			 */
			proc.add_info_bool(GlobalMembersGm_backend_gps.GPS_FLAG_USE_EDGE_PROP, true);
		}

		else if (GlobalMembersGm_defs.gm_is_graph_type(type_id))
		{
			if (_graph_defined)
			{
				GlobalMembersGm_error.gm_backend_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_GPS_MULTIPLE_GRAPH, e.getId().get_line(), e.getId().get_col(), e.getId().get_orgname());
				_error = true;
			}
			_graph_defined = true;
		}

		return true;
	}

	public final boolean is_graph_defined()
	{
		return _graph_defined;
	}

	private boolean _error;
	private boolean _graph_defined;
	private int foreach_depth;
	private ast_procdef proc;
}