package backend_gps;

import ast.ast_expr;
import ast.ast_expr_builtin;

import common.gm_apply;

import frontend.gm_symtab_entry;

public class gps_opt_check_contain_builtin_through_t extends gm_apply
{
	public gps_opt_check_contain_builtin_through_t(gm_symtab_entry drv)
	{
		set_for_expr(true);
		set_traverse_local_expr_only(true);
		contain = false;
		sym = drv;
	}
	public final boolean has_it()
	{
		return contain;
	}
	@Override
	public boolean apply(ast_expr e)
	{
		if (e.is_builtin())
		{
			ast_expr_builtin b = (ast_expr_builtin) e;
			if ((b.get_driver() != null) && (b.get_driver().getSymInfo() == sym))
				contain = true;
		}
		return true;
	}

	private gm_symtab_entry sym;
	private boolean contain;
}