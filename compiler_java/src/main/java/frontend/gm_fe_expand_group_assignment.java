package frontend;

import inc.gm_compile_step;
import ast.ast_procdef;

import common.GlobalMembersGm_traverse;


public class gm_fe_expand_group_assignment extends gm_compile_step
{
	private gm_fe_expand_group_assignment()
	{
		set_description("Expand Group Assignment");
	}
	public void process(ast_procdef p)
	{
		//1. Group Assign -->  Foreach
		ss2_group_assign ga = new ss2_group_assign();
		GlobalMembersGm_traverse.gm_traverse_sents(p, ga); // mark
		ga.post_process(); // process
	}
	@Override
	public gm_compile_step get_instance()
	{
		return new gm_fe_expand_group_assignment();
	}
	public static gm_compile_step get_factory()
	{
		return new gm_fe_expand_group_assignment();
	}
}