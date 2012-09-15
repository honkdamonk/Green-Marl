package common;

import ast.ast_node;
import frontend.gm_symtab_entry;

public class gm_resolve_nc {

	// ---------------------------------------------------------------------------------------
	// For the subtree(top),
	// replace any id's symbol entry refrence e_old into e_new.
	// If the new symbol has different orgname() from the old one, modify the
	// name in the id node as well.
	// (Assumption. e_new is a valid symbol entry that does not break scoping
	// rule)
	// ---------------------------------------------------------------------------------------
	public static boolean gm_replace_symbol_entry(gm_symtab_entry e_old, gm_symtab_entry e_new, ast_node top) {
		gm_replace_symbol_entry_t T = new gm_replace_symbol_entry_t();
		T.do_replace(e_old, e_new, top);
		return T.is_changed();
	}

	// replace symbol entry only for bounds
	public static boolean gm_replace_symbol_entry_bound(gm_symtab_entry e_old, gm_symtab_entry e_new, ast_node top) {
		gm_replace_symbol_entry_bound_t T = new gm_replace_symbol_entry_bound_t();
		T.do_replace(e_old, e_new, top);
		return T.is_changed();
	}
}