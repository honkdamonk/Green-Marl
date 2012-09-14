package common;

import java.util.HashSet;

import frontend.SYMTAB_TYPES;
import frontend.gm_symtab;
import frontend.gm_symtab_entry;

//---------------------------------------------------------------------------
// remove set of symbols
//---------------------------------------------------------------------------
public class gm_remove_symbols_t extends gm_apply {
	
	private HashSet<gm_symtab_entry> TARGETS = new HashSet<gm_symtab_entry>();
	
	public gm_remove_symbols_t(HashSet<gm_symtab_entry> S) {
		TARGETS = new HashSet<gm_symtab_entry>(S);
		set_for_symtab(true);
	}

	@Override
	public boolean apply(gm_symtab e, SYMTAB_TYPES symtab_sype) {
		for (gm_symtab_entry t : TARGETS) {
			if (e.is_entry_in_the_tab(t)) {
				e.remove_entry_in_the_tab(t);
			}
		}
		return true;
	}

}