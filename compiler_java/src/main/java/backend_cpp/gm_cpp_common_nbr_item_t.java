package backend_cpp;

import ast.ast_foreach;
import ast.ast_if;
import frontend.gm_symtab_entry;

public class gm_cpp_common_nbr_item_t {
	public ast_foreach fe;
	public ast_if iff;
	public ast_if out_iff;
	public boolean nested_iff;
	public gm_symtab_entry common_sym;
}