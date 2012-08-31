package frontend;

import ast.ast_extra_info;
import ast.gm_rwinfo_map;

public class gm_bound_set_info extends ast_extra_info {
	
	public void dispose() {
		gm_rw_analysis.gm_delete_rwinfo_map(bound_set);
	}

	// all the reduce/defer ops that are bound to this foreach/bfs
	public gm_rwinfo_map bound_set = new gm_rwinfo_map();

	// (for-bfs) all the reduce/defer ops that are bound to bfs-backward
	public gm_rwinfo_map bound_set_back = new gm_rwinfo_map(); 
}