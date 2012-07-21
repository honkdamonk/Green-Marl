package backend_cpp;

import ast.ast_procdef;
import inc.gm_compile_step;

import common.GlobalMembersGm_main;
import common.GlobalMembersGm_traverse;

public class gm_cpp_opt_select_par extends gm_compile_step
{
	private gm_cpp_opt_select_par()
	{
		set_description("Select parallel regions");
	}
	public void process(ast_procdef p)
	{
		if (!GlobalMembersGm_main.CPP_BE.is_target_omp())
		{
			make_all_seq_t A = new make_all_seq_t();
			GlobalMembersGm_traverse.gm_traverse_sents(p, A);
		}
		else
		{
			choose_parallel_t A = new choose_parallel_t();
			GlobalMembersGm_traverse.gm_traverse_sents(p, A);
		}
    
		return;
	}
	@Override
	public gm_compile_step get_instance()
	{
		return new gm_cpp_opt_select_par();
	}
	public static gm_compile_step get_factory()
	{
		return new gm_cpp_opt_select_par();
	}
}