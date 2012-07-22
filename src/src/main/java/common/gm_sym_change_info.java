package common;

import frontend.gm_symtab_entry;

//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define TO_STR(X) #X
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define DEF_STRING(X) static const char *X = "X"

public class gm_sym_change_info
{
	public boolean change_lhs;
	public boolean change_rhs;
	public boolean src_scalar;
	public boolean tgt_scalar;
	public gm_symtab_entry src;
	public gm_symtab_entry s_drv;
	public gm_symtab_entry tgt;
	public gm_symtab_entry t_drv;
}