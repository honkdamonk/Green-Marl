package frontend;

import inc.gm_compile_step;
import ast.ast_procdef;

import common.gm_main;


public class gm_fe_restore_vardecl extends gm_compile_step
{
	private gm_fe_restore_vardecl()
	{
		set_description("Restore variable declarations (For Code Generation)");
	}
	public void process(ast_procdef p)
	{
		gm_main.FE.set_vardecl_removed(false);
		restore_vardecl_t T = new restore_vardecl_t();
		T.do_restore(p);
	}
	@Override
	public gm_compile_step get_instance()
	{
		return new gm_fe_restore_vardecl();
	}
	public static gm_compile_step get_factory()
	{
		return new gm_fe_restore_vardecl();
	}
}
//-------------------------------------------
// [Step 2]
//   Implement the definition in seperate files
//-------------------------------------------

//------------------------------------------------------
// [Step 3]
//   Include initialization 
//------------------------------------------------------


