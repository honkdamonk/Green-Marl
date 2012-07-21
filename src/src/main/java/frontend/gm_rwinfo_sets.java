import ast.ast_extra_info;

public class gm_rwinfo_sets extends ast_extra_info
{
	public java.util.HashMap<gm_symtab_entry, java.util.LinkedList<gm_rwinfo>> read_set = new java.util.HashMap<gm_symtab_entry, java.util.LinkedList<gm_rwinfo>>();
	public java.util.HashMap<gm_symtab_entry, java.util.LinkedList<gm_rwinfo>> write_set = new java.util.HashMap<gm_symtab_entry, java.util.LinkedList<gm_rwinfo>>();
	public java.util.HashMap<gm_symtab_entry, java.util.LinkedList<gm_rwinfo>> reduce_set = new java.util.HashMap<gm_symtab_entry, java.util.LinkedList<gm_rwinfo>>();
	public java.util.HashMap<gm_symtab_entry, java.util.LinkedList<gm_rwinfo>> mutate_set = new java.util.HashMap<gm_symtab_entry, java.util.LinkedList<gm_rwinfo>>();

	public void dispose()
	{
		GlobalMembersGm_rw_analysis.gm_delete_rwinfo_map(read_set);
		GlobalMembersGm_rw_analysis.gm_delete_rwinfo_map(write_set);
		GlobalMembersGm_rw_analysis.gm_delete_rwinfo_map(reduce_set);
		GlobalMembersGm_rw_analysis.gm_delete_rwinfo_map(mutate_set);
	}
}