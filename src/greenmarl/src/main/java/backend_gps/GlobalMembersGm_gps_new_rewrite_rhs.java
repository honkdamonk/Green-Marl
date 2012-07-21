package backend_gps;

import ast.ast_expr;
import frontend.gm_symtab_entry;

import common.GlobalMembersGm_transform_helper;

public class GlobalMembersGm_gps_new_rewrite_rhs
{

	public static boolean is_composed_of(ast_expr e, java.util.HashMap<gm_symtab_entry, gm_symtab_entry> SYMS)
	{
		return false;
	}
	public static void replace_access_expr(ast_expr org, gm_symtab_entry target, boolean destroy)
	{
		gm_replace_simple_props_t T = new gm_replace_simple_props_t(org, target, destroy);
		assert org.get_parent() != null;
		GlobalMembersGm_transform_helper.gm_replace_expr_general(org.get_parent(), T);
	}

	//extern void gm_gps_do_new_analysis_scope_sent_var(ast_procdef proc);
	//extern void gm_gps_do_new_analysis_rhs_lhs(ast_procdef proc);
}