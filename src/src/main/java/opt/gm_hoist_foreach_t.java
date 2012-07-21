package opt;

import ast.AST_NODE_TYPE;
import ast.ast_assign;
import ast.ast_sent;

//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define TO_STR(X) #X
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define DEF_STRING(X) static const char *X = "X"
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP(CLASS, DESC) class CLASS : public gm_compile_step { private: CLASS() {set_description(DESC);}public: virtual void process(ast_procdef*p); virtual gm_compile_step* get_instance(){return new CLASS();} static gm_compile_step* get_factory(){return new CLASS();} };
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP_FACTORY(CLASS) CLASS::get_factory()

//--------------------------------------------------------------------
// hoist up initialization assignment as far as possible
//--------------------------------------------------------------------

// code almost similar to hoist_assign. <i.e. need to restructure rather than copy-paste>
public class gm_hoist_foreach_t extends gm_hoist_normal_sent_t
{
	// need post apply.
	@Override
	protected boolean check_target(ast_sent target)
	{
		if (target.get_nodetype() != AST_NODE_TYPE.AST_FOREACH)
			return false;
		else
			return true;
	}

	@Override
	protected boolean check_trivial_pred(ast_sent S)
	{
		if (S.get_nodetype() == AST_NODE_TYPE.AST_VARDECL)
			return true;
		else if (S.get_nodetype() == AST_NODE_TYPE.AST_FOREACH)
			return true;
		else if (S.get_nodetype() == AST_NODE_TYPE.AST_ASSIGN)
		{
			ast_assign a = (ast_assign) S;
			is_const_check.prepare();
			a.get_rhs().traverse_pre(is_const_check);

			if (is_const_check.is_const())
				return true; // do not pass over const assignment
			else
				return false;
		}
		else
			return false;
	}
	protected gm_check_if_constant_t is_const_check = new gm_check_if_constant_t();
}
//bool gm_independent_optimize::do_hoist_foreach(ast_procdef* p)

