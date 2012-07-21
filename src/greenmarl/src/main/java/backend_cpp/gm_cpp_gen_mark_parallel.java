package backend_cpp;

import ast.ast_procdef;
import inc.gm_compile_step;
import common.GlobalMembersGm_parallel_helper;

public class gm_cpp_gen_mark_parallel extends gm_compile_step
{
	private gm_cpp_gen_mark_parallel()
	{
		set_description("Mark every parallel sentence");
	}
	public void process(ast_procdef p)
	{
		final boolean entry_is_seq = true;
		GlobalMembersGm_parallel_helper.gm_mark_sents_under_parallel_execution(p.get_body(), entry_is_seq);
	}
	@Override
	public gm_compile_step get_instance()
	{
		return new gm_cpp_gen_mark_parallel();
	}
	public static gm_compile_step get_factory()
	{
		return new gm_cpp_gen_mark_parallel();
	}
}