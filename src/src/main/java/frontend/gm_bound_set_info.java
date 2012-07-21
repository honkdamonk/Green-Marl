import ast.ast_extra_info;

public class gm_bound_set_info extends ast_extra_info
{
	public void dispose()
	{
		GlobalMembersGm_rw_analysis.gm_delete_rwinfo_map(bound_set);
	}
	public java.util.HashMap<gm_symtab_entry, java.util.LinkedList<gm_rwinfo>> bound_set = new java.util.HashMap<gm_symtab_entry, java.util.LinkedList<gm_rwinfo>>(); // all the reduce/defer ops that are bound to this foreach/bfs
	public java.util.HashMap<gm_symtab_entry, java.util.LinkedList<gm_rwinfo>> bound_set_back = new java.util.HashMap<gm_symtab_entry, java.util.LinkedList<gm_rwinfo>>(); // (for-bfs) all the reduce/defer ops that are bound to bfs-backward
}