package backend_cpp;

import ast.ast_procdef;
import inc.gm_compile_step;
import common.gm_main;

public class gm_cpp_gen_proc extends gm_compile_step
{
	private gm_cpp_gen_proc()
	{
		set_description("Creating source for each procedure");
	}
	public void process(ast_procdef proc)
	{
		gm_main.CPP_BE.generate_proc(proc);
	}
	@Override
	public gm_compile_step get_instance()
	{
		return new gm_cpp_gen_proc();
	}
	public static gm_compile_step get_factory()
	{
		return new gm_cpp_gen_proc();
	}
}
//-------------------------------------------
// [Step 2]
// Implement step::process in a seperate file
//-------------------------------------------

//------------------------------------------------------
// [Step 3]
// Add step in module initialization (gm_cpp_gen_main.cc)
//------------------------------------------------------


