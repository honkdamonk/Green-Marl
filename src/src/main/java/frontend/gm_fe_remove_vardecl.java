package frontend;

import inc.gm_compile_step;
import ast.ast_procdef;

import common.GlobalMembersGm_main;


public class gm_fe_remove_vardecl extends gm_compile_step
{
	private gm_fe_remove_vardecl()
	{
		set_description("Remove variable declarations (Use Symtab)");
	}
	public void process(ast_procdef p)
	{
		remove_vardecl_t T = new remove_vardecl_t();
		T.do_removal(p);
    
		rename_all_t T2 = new rename_all_t();
		T2.do_rename_all_potential(p);
    
		GlobalMembersGm_main.FE.set_vardecl_removed(true);
	}
	@Override
	public gm_compile_step get_instance()
	{
		return new gm_fe_remove_vardecl();
	}
	public static gm_compile_step get_factory()
	{
		return new gm_fe_remove_vardecl();
	}
}