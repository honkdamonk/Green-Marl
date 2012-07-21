package backend_cpp;

import ast.AST_NODE_TYPE;
import ast.ast_sent;
import ast.ast_sentblock;
import frontend.gm_symtab;
import inc.GlobalMembersGm_backend_cpp;

import common.gm_apply;

//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define TO_STR(X) #X
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define DEF_STRING(X) static const char *X = "X"
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP(CLASS, DESC) class CLASS : public gm_compile_step { private: CLASS() {set_description(DESC);}public: virtual void process(ast_procdef*p); virtual gm_compile_step* get_instance(){return new CLASS();} static gm_compile_step* get_factory(){return new CLASS();} };
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP_FACTORY(CLASS) CLASS::get_factory()

//--------------------------------------------------------------------
// Checking routines for temporary procedure declaration and removal
//   (1) Check if there are any temoprary properties in ths procedure
//   (2) Mark each sentence-block if it has property declaration
//   (3) Mark entry sentence block
//--------------------------------------------------------------------
public class property_decl_check_t extends gm_apply
{
	public property_decl_check_t()
	{
		this.has_prop_decl = false;
	}

	@Override
	public boolean apply(ast_sent s)
	{
		if (s.get_nodetype() == AST_NODE_TYPE.AST_SENTBLOCK)
		{
			ast_sentblock sb = (ast_sentblock) s;
			gm_symtab e = sb.get_symtab_field();

			if (e.get_entries().size() != 0)
			{
				has_prop_decl = true;
				s.add_info_bool(GlobalMembersGm_backend_cpp.CPPBE_INFO_HAS_PROPDECL, true);
			}
		}
		return true;
	}
	public boolean has_prop_decl;
}