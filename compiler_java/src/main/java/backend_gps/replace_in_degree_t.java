package backend_gps;

import ast.ast_expr;
import ast.ast_expr_builtin;
import ast.ast_field;
import ast.ast_id;

import common.gm_expr_replacement_t;
import common.gm_method_id;

import frontend.gm_symtab_entry;

public class replace_in_degree_t extends gm_expr_replacement_t
{
	@Override
	public boolean is_target(ast_expr e)
	{
		if (e.is_builtin())
		{
			ast_expr_builtin b = (ast_expr_builtin) e;
			if (b.get_builtin_def().get_method_id() == gm_method_id.GM_BLTIN_NODE_IN_DEGREE)
			{
				return true;
			}
		}
		return false;
	}
	@Override
	public ast_expr create_new_expr(ast_expr target, tangible.RefObject<Boolean> destory_target_after)
	{
		destory_target_after.argvalue = true;

		ast_expr_builtin b = (ast_expr_builtin) target;
		ast_id i = b.get_driver().copy(true);
		i.copy_line_info(b.get_driver());
		ast_id f = new_prop.getId().copy(true);
		f.copy_line_info(b.get_driver());
		ast_field field = ast_field.new_field(i, f);
		field.copy_line_info(b.get_driver());

		ast_expr rhs = ast_expr.new_field_expr(field);
		return rhs;
	}
	public final void set_new_prop(gm_symtab_entry e)
	{
		new_prop = e;
	}
	private gm_symtab_entry new_prop;

}
// check if reverse edge or num reverse edges are used
