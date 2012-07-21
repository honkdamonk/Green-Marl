package backend_gps;

import inc.gm_compile_step;
import ast.ast_procdef;

public class gm_gps_opt_simplify_expr1 extends gm_compile_step
{
	private gm_gps_opt_simplify_expr1()
	{
		set_description("Seperating builtin-calls from outer-loop driver");
	}
	public void process(ast_procdef p)
	{
		gps_opt_simplify_outer_builtin_t T = new gps_opt_simplify_outer_builtin_t();
		p.traverse_both(T);
		T.post_process();
	}
	@Override
	public gm_compile_step get_instance()
	{
		return new gm_gps_opt_simplify_expr1();
	}
	public static gm_compile_step get_factory()
	{
		return new gm_gps_opt_simplify_expr1();
	}
}