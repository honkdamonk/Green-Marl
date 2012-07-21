package opt;

import ast.ast_expr;

import common.gm_apply;

// todo: find a better palce to put this code

public class gm_check_if_constant_t extends gm_apply
{
	public gm_check_if_constant_t()
	{
		this._is_const = true;
		set_all(false);
		set_for_expr(true);
	}
	@Override
	public boolean apply(ast_expr e)
	{
		if (e.is_id() || e.is_field()) // is builtin const? --> yes
			_is_const = false;
		return true;
	}
	public final boolean is_const()
	{
		return _is_const;
	}
	public final void prepare()
	{
		_is_const = true;
	}
	protected boolean _is_const;
}