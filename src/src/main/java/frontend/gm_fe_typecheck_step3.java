package frontend;

import inc.gm_compile_step;
import ast.ast_procdef;

public class gm_fe_typecheck_step3 extends gm_compile_step
{
	private gm_fe_typecheck_step3()
	{
		set_description("Typecheck: resolve expression types");
	}
	public void process(ast_procdef p)
	{
		gm_typechecker_stage_3 T = new gm_typechecker_stage_3();
		p.traverse_post(T); // post-apply
    
		if (T.is_okay())
		{
			GlobalMembersGm_coercion.gm_insert_explicit_type_conversion_for_op(T.coercion_targets);
		}
    
		check_argmax_num_args_t T2 = new check_argmax_num_args_t();
		p.traverse_pre(T2);
    
		set_okay(T.is_okay() && T2.is_okay());
    
	}
	@Override
	public gm_compile_step get_instance()
	{
		return new gm_fe_typecheck_step3();
	}
	public static gm_compile_step get_factory()
	{
		return new gm_fe_typecheck_step3();
	}
}