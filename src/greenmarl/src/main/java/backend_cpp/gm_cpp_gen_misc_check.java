package backend_cpp;

import ast.ast_procdef;
import inc.gm_compile_step;

public class gm_cpp_gen_misc_check extends gm_compile_step
{
	private gm_cpp_gen_misc_check()
	{
		set_description("Creating source for each procedure");
	}
	public void process(ast_procdef d)
	{
		// re-do rw analysis
		cpp_check_reverse_edge_t T = new cpp_check_reverse_edge_t();
		d.traverse_pre(T);
	}
	@Override
	public gm_compile_step get_instance()
	{
		return new gm_cpp_gen_misc_check();
	}
	public static gm_compile_step get_factory()
	{
		return new gm_cpp_gen_misc_check();
	}
}