package ast;

import java.util.HashMap;

import frontend.gm_symtab_entry;

public class gm_rwinfo_map extends HashMap<gm_symtab_entry, gm_rwinfo_list> {
	
	private static final long serialVersionUID = -3847370352583870871L;

//	public gm_rwinfo_map(gm_rwinfo_map other) {
//		super(other);
//	}

	public gm_rwinfo_map() {
		super();
	}

}
