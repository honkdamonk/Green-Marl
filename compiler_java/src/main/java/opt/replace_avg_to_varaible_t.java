package opt;

import ast.ast_expr;
import ast.ast_id;

import common.gm_expr_replacement_t;

import frontend.gm_symtab_entry;

public class replace_avg_to_varaible_t extends gm_expr_replacement_t
{
	public replace_avg_to_varaible_t(ast_expr target, gm_symtab_entry entry)
	{
		this.T = target;
		this.E = entry;
	}

	@Override
	public boolean is_target(ast_expr e)
	{
		return e == T;
	}
	@Override
	public ast_expr create_new_expr(ast_expr t, tangible.RefObject<Boolean> destroy)
	{
		destroy.argvalue = false;
		ast_id i = E.getId().copy(true);
		ast_expr new_expr = ast_expr.new_id_expr(i);
		return new_expr;
	}
	private ast_expr T;
	private gm_symtab_entry E;
}