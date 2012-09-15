package frontend;

import inc.gm_reduce;

//--------------------------------------------
// Avoid same report
//--------------------------------------------
public class bound_info_t {
	public gm_symtab_entry target; // target
	public gm_symtab_entry bound;
	public gm_reduce reduce_type;
}