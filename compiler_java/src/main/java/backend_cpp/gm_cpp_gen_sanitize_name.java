package backend_cpp;

import ast.ast_procdef;
import inc.gm_compile_step;

import common.gm_main;
import common.GlobalMembersGm_prepare_genname;
import common.gm_vocabulary;

//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP(CLASS, DESC) class CLASS : public gm_compile_step { private: CLASS() {set_description(DESC);}public: virtual void process(ast_procdef*p); virtual gm_compile_step* get_instance(){return new CLASS();} static gm_compile_step* get_factory(){return new CLASS();} };
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP_FACTORY(CLASS) CLASS::get_factory()


//-------------------------------------------
// [Step 1]
// Add declaration of step here
//-------------------------------------------
public class gm_cpp_gen_sanitize_name extends gm_compile_step
{
	private gm_cpp_gen_sanitize_name()
	{
		set_description("Sanitize identifier");
	}
	public void process(ast_procdef proc)
	{
		// (1) create gen-name
		gm_vocabulary V = gm_main.CPP_BE.get_language_voca();
		GlobalMembersGm_prepare_genname.gm_prepare_genname(proc, V);
    
		// (2) rename property names  A --> G_A
		rename_prop_name_t T = new rename_prop_name_t();
		proc.traverse_pre(T);
	}
	@Override
	public gm_compile_step get_instance()
	{
		return new gm_cpp_gen_sanitize_name();
	}
	public static gm_compile_step get_factory()
	{
		return new gm_cpp_gen_sanitize_name();
	}
}