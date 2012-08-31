package backend_giraph;

import inc.gm_compile_step;
import ast.ast_procdef;
import common.gm_main;

//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP(CLASS, DESC) class CLASS : public gm_compile_step { private: CLASS() {set_description(DESC);}public: virtual void process(ast_procdef*p); virtual gm_compile_step* get_instance(){return new CLASS();} static gm_compile_step* get_factory(){return new CLASS();} };
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP_FACTORY(CLASS) CLASS::get_factory()


//-------------------------------------------
// [Step 1]
// Add delaration here
// declaration of optimization steps
//-------------------------------------------
public class gm_giraph_gen_class extends gm_compile_step
{
	private gm_giraph_gen_class()
	{
		set_description("Generate Code");
	}
	public void process(ast_procdef proc)
	{
		gm_main.PREGEL_BE.generate_proc(proc);
	}
	@Override
	public gm_compile_step get_instance()
	{
		return new gm_giraph_gen_class();
	}
	public static gm_compile_step get_factory()
	{
		return new gm_giraph_gen_class();
	}
}
//-------------------------------------------
// [Step 2]
//   Implement the definition in seperate files
//-------------------------------------------

//------------------------------------------------------
// [Step 3]
//   Include initialization in gm_giraph_gen.cc
//------------------------------------------------------


