package opt;

import ast.ast_id;

import common.gm_apply;

import frontend.gm_symtab_entry;

public class check_sym_used extends gm_apply
{
	public check_sym_used(gm_symtab_entry t)
	{
		set_for_id(true);
		_target = t;
		_used = false;
	}
	@Override
	public boolean apply(ast_id i)
	{
		if (i.getSymInfo() == _target)
		{
			_used = true;
		}
		return true;
	}
	public final boolean is_used()
	{
		return _used;
	}

	private gm_symtab_entry _target;
	private boolean _used;
}