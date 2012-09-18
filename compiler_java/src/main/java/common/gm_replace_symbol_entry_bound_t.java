package common;

import ast.ast_assign;
import ast.ast_id;
import ast.ast_node;
import ast.ast_node_type;
import ast.ast_sent;
import frontend.gm_symtab_entry;

public class gm_replace_symbol_entry_bound_t extends gm_apply {
	
	protected boolean _changed;
	protected gm_symtab_entry _src;
	protected gm_symtab_entry _target;
	
	@Override
	public boolean apply(ast_sent s) {
		assert _src != null;
		assert _target != null;

		if (s.get_nodetype() == ast_node_type.AST_ASSIGN) {
			ast_assign a = (ast_assign) s;
			if (a.get_bound() != null) {
				ast_id i = a.get_bound();
				assert i.getSymInfo() != null;
				if (i.getSymInfo() == _src) {
					i.setSymInfo(_target);
					_changed = true;
				}
				return true;
			}
		}
		return true;
	}

	public final boolean is_changed() {
		return _changed;
	}

	public final void do_replace(gm_symtab_entry e_old, gm_symtab_entry e_new,
			ast_node top) {
		set_for_sent(true);
		_src = e_old;
		_target = e_new;
		_changed = false;
		top.traverse_pre(this);
	}
	
}