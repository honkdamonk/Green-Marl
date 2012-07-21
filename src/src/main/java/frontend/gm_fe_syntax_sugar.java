package frontend;

import ast.ast_procdef;
import inc.gm_compile_step;
import common.GlobalMembersGm_traverse;


public class gm_fe_syntax_sugar extends gm_compile_step
{
	private gm_fe_syntax_sugar()
	{
		set_description("Regularize syntax (without typeinfo)");
	}
	public void process(ast_procdef p)
	{
		gm_ss1_filter s1 = new gm_ss1_filter();
		GlobalMembersGm_traverse.gm_traverse_sents(p, s1);
    
		gm_ss1_initial_expr s2 = new gm_ss1_initial_expr();
		GlobalMembersGm_traverse.gm_traverse_sents(p, s2);
    
		GlobalMembersGm_syntax_sugar.gm_expand_argument_list(p.get_in_args());
		GlobalMembersGm_syntax_sugar.gm_expand_argument_list(p.get_out_args());
    
		//return true;
	}
	@Override
	public gm_compile_step get_instance()
	{
		return new gm_fe_syntax_sugar();
	}
	public static gm_compile_step get_factory()
	{
		return new gm_fe_syntax_sugar();
	}
}