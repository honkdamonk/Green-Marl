package common;

import frontend.gm_scope;

// (NULL -> scope)
public class putnew_upper_scope_on_null extends replace_upper_scope {
	
	public final gm_scope O = new gm_scope();

	public putnew_upper_scope_on_null() {
		set_for_symtab(true);
		O.push_symtabs(null, null, null);
		set_old_scope(O);
	}

	public final void set_scope_to_put(gm_scope s) {
		set_new_scope(s);
	}
}