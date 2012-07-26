package frontend;

import java.util.HashMap;

import ast.ast_extra_info;
import ast.gm_rwinfo_list;

public class gm_bound_set_info extends ast_extra_info {
	
	public void dispose() {
		GlobalMembersGm_rw_analysis.gm_delete_rwinfo_map(bound_set);
	}

	// all the reduce/defer ops that are bound to this foreach/bfs
	public HashMap<gm_symtab_entry, gm_rwinfo_list> bound_set = new HashMap<gm_symtab_entry, gm_rwinfo_list>();

	// (for-bfs) all the reduce/defer ops that are bound to bfs-backward
	public HashMap<gm_symtab_entry, gm_rwinfo_list> bound_set_back = new HashMap<gm_symtab_entry, gm_rwinfo_list>(); 
}