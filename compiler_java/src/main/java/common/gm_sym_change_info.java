package common;

import frontend.gm_symtab_entry;

public class gm_sym_change_info {
	
	public boolean change_lhs;
	public boolean change_rhs;
	public boolean src_scalar;
	public boolean tgt_scalar;
	public gm_symtab_entry src;
	public gm_symtab_entry s_drv;
	public gm_symtab_entry tgt;
	public gm_symtab_entry t_drv;
	
	public gm_sym_change_info() {
	}
	
	public gm_sym_change_info(gm_sym_change_info info) {
		change_lhs = info.change_lhs;
		change_rhs = info.change_rhs;
		src_scalar = info.src_scalar;
		tgt_scalar = info.tgt_scalar;
		src = info.src;
		s_drv = info.s_drv;
		tgt = info.tgt;
		t_drv = info.t_drv;
	}

}