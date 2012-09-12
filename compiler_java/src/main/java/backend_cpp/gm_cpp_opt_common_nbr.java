package backend_cpp;

import ast.ast_procdef;
import inc.gm_compile_step;

public class gm_cpp_opt_common_nbr extends gm_compile_step
{
	private gm_cpp_opt_common_nbr()
	{
		set_description("Common Neigbhor Iteration");
	}
	
	public void process(ast_procdef p)
	{
		assert false; // to be implemented
	}
	
	@Override
	public gm_compile_step get_instance()
	{
		return new gm_cpp_opt_common_nbr();
	}
	
	public static gm_compile_step get_factory()
	{
		return new gm_cpp_opt_common_nbr();
	}
}