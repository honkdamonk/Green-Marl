package frontend;

import inc.GM_REDUCE_T;

//--------------------------------------------
// Avoid same report
//--------------------------------------------
public class bound_info_t
{
	public gm_symtab_entry target; // target
	public gm_symtab_entry bound;
	public GM_REDUCE_T reduce_type;
	
	@Deprecated
	public void dispose() {}
}