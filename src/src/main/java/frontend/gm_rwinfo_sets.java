package frontend;

import ast.ast_extra_info;
import ast.gm_rwinfo_map;

public class gm_rwinfo_sets extends ast_extra_info
{
	public gm_rwinfo_map read_set = new gm_rwinfo_map();
	public gm_rwinfo_map write_set = new gm_rwinfo_map();
	public gm_rwinfo_map reduce_set = new gm_rwinfo_map();
	public gm_rwinfo_map mutate_set = new gm_rwinfo_map();

	public void dispose()
	{
		GlobalMembersGm_rw_analysis.gm_delete_rwinfo_map(read_set);
		GlobalMembersGm_rw_analysis.gm_delete_rwinfo_map(write_set);
		GlobalMembersGm_rw_analysis.gm_delete_rwinfo_map(reduce_set);
		GlobalMembersGm_rw_analysis.gm_delete_rwinfo_map(mutate_set);
	}
}