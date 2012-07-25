package backend_gps;

import ast.ast_expr;
import ast.ast_expr_builtin;
import frontend.gm_symtab_entry;
import inc.GMEXPR_CLASS;
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
//-----------------------------------------------------------------
// Check random access
//-----------------------------------------------------------------

public class gps_check_random_read_t extends gm_apply
{
	public gps_check_random_read_t()
	{
		set_for_expr(true);
		_error = false;
	}

	public final boolean is_error()
	{
		return _error;
	}

	public final boolean apply(ast_expr f)
	{
		// random read always happens by field or builtin
		if ((f.get_opclass() == GMEXPR_CLASS.GMEXPR_FIELD) || (f.get_opclass() == GMEXPR_CLASS.GMEXPR_BUILTIN))
		{
			if (f.find_info_int(GlobalMembersGm_backend_gps.GPS_INT_EXPR_SCOPE) == gm_gps_new_scope_analysis_t.GPS_NEW_SCOPE_RANDOM.getValue())
			{
				gm_symtab_entry driver = (f.get_opclass() == GMEXPR_CLASS.GMEXPR_FIELD) ? f.get_field().get_first().getSymInfo() : ((ast_expr_builtin) f).get_driver().getSymInfo();

				if (driver.getType().is_graph())
					return true;

				// Random Read
				if ((f.get_opclass() == GMEXPR_CLASS.GMEXPR_FIELD))
					System.out.printf("%s.%s\n", f.get_field().get_first().get_genname(), f.get_field().get_second().get_genname());
				else
					System.out.printf("%s->..()\n", ((ast_expr_builtin) f).get_driver().get_genname());
				GlobalMembersGm_error.gm_backend_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_GPS_RANDOM_NODE_READ, f.get_line(), f.get_col(), "");
				_error = true;
			}
		}
		return true;
	}
	private boolean _error;

}