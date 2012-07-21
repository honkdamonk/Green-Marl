package backend_cpp;

import ast.ast_procdef;
import inc.gm_compile_step;

//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP(CLASS, DESC) class CLASS : public gm_compile_step { private: CLASS() {set_description(DESC);}public: virtual void process(ast_procdef*p); virtual gm_compile_step* get_instance(){return new CLASS();} static gm_compile_step* get_factory(){return new CLASS();} };
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP_FACTORY(CLASS) CLASS::get_factory()


//-------------------------------------------
// [Step 1]
// Add delaration here
// declaration of optimization steps
//-------------------------------------------
public class gm_cpp_opt_check_feasible extends gm_compile_step
//GM_COMPILE_STEP(gm_cpp_opt_reduce_bound, "Optimize reductions with sequential bound ")
{
	private gm_cpp_opt_check_feasible()
	{
		set_description("Check compiler feasiblity");
	}
	public void process(ast_procdef p)
	{
		check_cpp_feasible_t T = new check_cpp_feasible_t();
		p.traverse_both(T);
    
		set_okay(T.is_okay());
	}
	@Override
	public gm_compile_step get_instance()
	{
		return new gm_cpp_opt_check_feasible();
	}
	public static gm_compile_step get_factory()
	{
		return new gm_cpp_opt_check_feasible();
	}
}