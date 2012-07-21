import ast.ast_id;
import ast.ast_node;
import frontend.gm_symtab_entry;

public class gm_replace_symbol_entry_t extends gm_apply
{
	@Override
	public boolean apply(ast_id i)
	{
		assert _src != null;
		assert _target != null;
		assert i.getSymInfo() != null;
		if (i.getSymInfo() == _src)
		{
			i.setSymInfo(_target);
			_changed = true;
		}
		return true;
	}
	public final boolean is_changed()
	{
		return _changed;
	}
	public final void do_replace(gm_symtab_entry e_old, gm_symtab_entry e_new, ast_node top)
	{
		set_all(false);
		set_for_id(true);
		_src = e_old;
		_target = e_new;
		_changed = false;
		//_need_change_name = ! gm_is_same_string(e_old->getId()->get_orgname(), e_new->getId()->get_orgname());
		top.traverse_pre(this);
	}
	protected boolean _changed;
	//bool _need_change_name;
	protected gm_symtab_entry _src;
	protected gm_symtab_entry _target;
}