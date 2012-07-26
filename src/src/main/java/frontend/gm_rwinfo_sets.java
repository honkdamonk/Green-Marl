package frontend;

import java.util.HashMap;

import ast.ast_extra_info;
import ast.gm_rwinfo_list;

public class gm_rwinfo_sets extends ast_extra_info
{
	public HashMap<gm_symtab_entry, gm_rwinfo_list> read_set = new HashMap<gm_symtab_entry, gm_rwinfo_list>();
	public HashMap<gm_symtab_entry, gm_rwinfo_list> write_set = new HashMap<gm_symtab_entry, gm_rwinfo_list>();
	public HashMap<gm_symtab_entry, gm_rwinfo_list> reduce_set = new HashMap<gm_symtab_entry, gm_rwinfo_list>();
	public HashMap<gm_symtab_entry, gm_rwinfo_list> mutate_set = new HashMap<gm_symtab_entry, gm_rwinfo_list>();

	public void dispose()
	{
		GlobalMembersGm_rw_analysis.gm_delete_rwinfo_map(read_set);
		GlobalMembersGm_rw_analysis.gm_delete_rwinfo_map(write_set);
		GlobalMembersGm_rw_analysis.gm_delete_rwinfo_map(reduce_set);
		GlobalMembersGm_rw_analysis.gm_delete_rwinfo_map(mutate_set);
	}
}