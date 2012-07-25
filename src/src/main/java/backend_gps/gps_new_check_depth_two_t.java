package backend_gps;

import ast.AST_NODE_TYPE;
import ast.ast_foreach;
import ast.ast_sent;
import inc.GMTYPE_T;
import inc.GlobalMembersGm_backend_gps;

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

//------------------------------------------------------------------------
//  Check if maximum two depth of Foreach
//     - Each foreach is parallel
//     - First foreach: graph-wide. Second foreach: in/out neighbor
//
//   scope analysis is invoked at the end of this analysis
//------------------------------------------------------------------------
// Information Created
//    GPS_FLAG_IS_INNER_LOOP: <to:>foreach or symbol of iterator <what:>if inner loop
//    GPS_FLAG_IS_OUTER_LOOP: <to:>foreach or symbol of iterator <what:>if inner loop
//------------------------------------------------------------------------

public class gps_new_check_depth_two_t extends gm_apply
{
	public gps_new_check_depth_two_t()
	{
		_error = false;
		foreach_depth = 0;

		set_separate_post_apply(true);
		set_for_sent(true);
	}
	public final boolean is_error()
	{
		return _error;
	}

	@Override
	public boolean apply(ast_sent s)
	{
		if (s.get_nodetype() != AST_NODE_TYPE.AST_FOREACH)
			return true;

		foreach_depth++;
		ast_foreach fe = (ast_foreach) s;

		if (foreach_depth == 1)
		{
			// check if node-wide foreach
			if (fe.get_iter_type() != GMTYPE_T.GMTYPE_NODEITER_ALL)
			{
				GlobalMembersGm_error.gm_backend_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_GPS_UNSUPPORTED_RANGE_MASTER, s.get_line(), s.get_col(), "");
				_error = true;
			}

			if (fe.is_sequential())
			{
				GlobalMembersGm_error.gm_backend_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_GPS_NEED_PARALLEL, s.get_line(), s.get_col(), "");
				_error = true;
			}

			fe.add_info_bool(GlobalMembersGm_backend_gps.GPS_FLAG_IS_OUTER_LOOP, true);
			fe.get_iterator().getSymInfo().add_info_bool(GlobalMembersGm_backend_gps.GPS_FLAG_IS_OUTER_LOOP, true);
		}

		else if (foreach_depth == 2)
		{
			// check if out-nbr iteration
			if ((fe.get_iter_type() != GMTYPE_T.GMTYPE_NODEITER_NBRS) && (fe.get_iter_type() != GMTYPE_T.GMTYPE_NODEITER_IN_NBRS))
			{
				GlobalMembersGm_error.gm_backend_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_GPS_UNSUPPORTED_RANGE_VERTEX, s.get_line(), s.get_col(), "");
				_error = true;
			}
			if (fe.is_sequential())
			{
				GlobalMembersGm_error.gm_backend_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_GPS_NEED_PARALLEL, s.get_line(), s.get_col(), "");
				_error = true;
			}

			fe.add_info_bool(GlobalMembersGm_backend_gps.GPS_FLAG_IS_INNER_LOOP, true);
			fe.get_iterator().getSymInfo().add_info_bool(GlobalMembersGm_backend_gps.GPS_FLAG_IS_INNER_LOOP, true);
		} // (depth > 3)
		else
		{
			_error = true;
			GlobalMembersGm_error.gm_backend_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_GPS_NBR_LOOP_TOO_DEEP, s.get_line(), s.get_col(), "");
		}

		return true;

	}

	@Override
	public boolean apply2(ast_sent s)
	{
		if (s.get_nodetype() == AST_NODE_TYPE.AST_FOREACH)
			foreach_depth--;

		return true;
	}


	private boolean _error;
	private int foreach_depth;

}