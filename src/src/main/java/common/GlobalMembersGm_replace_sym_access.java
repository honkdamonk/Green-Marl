package common;

import ast.ast_node;
import frontend.gm_symtab_entry;

public class GlobalMembersGm_replace_sym_access {

	public static boolean replace_symbol_access(ast_node top, gm_sym_change_info I) {
		// traverse the nodes
		// replace every instance of symbol access
		gm_replace_symbol_access_t T = new gm_replace_symbol_access_t(I);
		top.traverse_pre(T);

		return T.is_changed();
	}

	public static boolean gm_replace_symbol_access_scalar_scalar(ast_node top, gm_symtab_entry src, gm_symtab_entry target, boolean change_rhs,
			boolean change_lhs) {
		gm_sym_change_info S = new gm_sym_change_info();
		S.change_lhs = change_lhs;
		S.change_rhs = change_rhs;
		S.src_scalar = true;
		S.tgt_scalar = true;
		S.src = src;
		S.tgt = target;
		return GlobalMembersGm_replace_sym_access.replace_symbol_access(top, S);
	}

	public static boolean gm_replace_symbol_access_scalar_field(ast_node top, gm_symtab_entry src, gm_symtab_entry t_drv, gm_symtab_entry target,
			boolean change_rhs, boolean change_lhs) {
		gm_sym_change_info S = new gm_sym_change_info();
		S.change_lhs = change_lhs;
		S.change_rhs = change_rhs;
		S.src_scalar = true;
		S.tgt_scalar = false;
		S.src = src;
		S.tgt = target;
		S.t_drv = t_drv;
		return GlobalMembersGm_replace_sym_access.replace_symbol_access(top, S);
	}

	public static boolean gm_replace_symbol_access_field_scalar(ast_node top, gm_symtab_entry src_drv, gm_symtab_entry src, gm_symtab_entry target,
			boolean change_rhs, boolean change_lhs) {
		gm_sym_change_info S = new gm_sym_change_info();
		S.change_lhs = change_lhs;
		S.change_rhs = change_rhs;
		S.src_scalar = false;
		S.tgt_scalar = true;
		S.src = src;
		S.s_drv = src_drv;
		S.tgt = target;
		return GlobalMembersGm_replace_sym_access.replace_symbol_access(top, S);
	}

	public static boolean gm_replace_symbol_access_field_field(ast_node top, gm_symtab_entry src_drv, gm_symtab_entry src, gm_symtab_entry t_drv,
			gm_symtab_entry target, boolean change_rhs, boolean change_lhs) {
		gm_sym_change_info S = new gm_sym_change_info();
		S.change_lhs = change_lhs;
		S.change_rhs = change_rhs;
		S.src_scalar = true;
		S.tgt_scalar = true;
		S.src = src;
		S.s_drv = src_drv;
		S.tgt = target;
		S.t_drv = t_drv;

		return GlobalMembersGm_replace_sym_access.replace_symbol_access(top, S);
	}
}