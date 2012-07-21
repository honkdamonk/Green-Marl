package backend_cpp;

import inc.gm_compile_step;


public class gm_cpp_opt_sanitize_name extends gm_compile_step
{
	private gm_cpp_opt_sanitize_name()
	{
		set_description("Sanitize identifier");
	}
//	virtual void process(ast_procdef p);
	@Override
	public gm_compile_step get_instance()
	{
		return new gm_cpp_opt_sanitize_name();
	}
	public static gm_compile_step get_factory()
	{
		return new gm_cpp_opt_sanitize_name();
	}
}