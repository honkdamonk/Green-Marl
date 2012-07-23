package frontend;

import java.util.HashMap;
import java.util.LinkedList;

import ast.ast_extra_info;

public class gm_bound_set_info extends ast_extra_info {
	
	public void dispose() {
		GlobalMembersGm_rw_analysis.gm_delete_rwinfo_map(bound_set);
	}

	// all the reduce/defer ops that are bound to this foreach/bfs
	public HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> bound_set = new HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>>();

	// (for-bfs) all the reduce/defer ops that are bound to bfs-backward
	public HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> bound_set_back = new HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>>(); 
}