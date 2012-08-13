package backend_gps;

import ast.ast_expr;
import frontend.gm_symtab_entry;

import common.gm_expr_replacement_t;

public class gm_replace_simple_props_t extends gm_expr_replacement_t
{
	public gm_replace_simple_props_t(ast_expr s, gm_symtab_entry t, boolean d)
	{
		this.source = s;
		this.target = t;
		this.dest = d;
	}
	@Override
	public boolean is_target(ast_expr e)
	{
		return e == source;
	}
	@Override
	public ast_expr create_new_expr(ast_expr org, tangible.RefObject<Boolean> destroy_target_after)
	{
		ast_expr new_expr = ast_expr.new_id_expr(target.getId().copy(true));
		new_expr.set_type_summary(target.getType().get_typeid());
		destroy_target_after.argvalue = dest;
		return new_expr;
	}
	private ast_expr source;
	private gm_symtab_entry target;
	private boolean dest;
}