package backend_gps;

import inc.gm_compile_step;
import ast.ast_procdef;

import common.gm_main;

public class gm_gps_gen_class extends gm_compile_step
{
	private gm_gps_gen_class()
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
		return new gm_gps_gen_class();
	}
	public static gm_compile_step get_factory()
	{
		return new gm_gps_gen_class();
	}
}
//-------------------------------------------
// [Step 2]
//   Implement the definition in seperate files
//-------------------------------------------

//------------------------------------------------------
// [Step 3]
//   Include initialization in gm_gps_gen.cc
//------------------------------------------------------


