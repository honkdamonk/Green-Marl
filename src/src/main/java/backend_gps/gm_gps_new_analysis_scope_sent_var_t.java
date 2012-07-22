package backend_gps;

import ast.AST_NODE_TYPE;
import ast.ast_assign;
import ast.ast_expr;
import ast.ast_expr_builtin;
import ast.ast_foreach;
import ast.ast_sent;
import frontend.SYMTAB_TYPES;
import frontend.gm_symtab_entry;
import inc.GMTYPE_T;
import inc.GlobalMembersGm_backend_gps;

import common.gm_apply;
import common.gm_method_id_t;

public class gm_gps_new_analysis_scope_sent_var_t extends gm_apply
{
	public gm_gps_new_analysis_scope_sent_var_t()
	{
		outer_loop = null;
		inner_loop = null;
		set_separate_post_apply(true);
		set_for_sent(true);
		set_for_symtab(true);
		current_scope = gm_gps_new_scope_analysis_t.GPS_NEW_SCOPE_GLOBAL;
	}

	public final boolean apply(gm_symtab_entry e, int symtab_type)
	{
		e.add_info_int(GlobalMembersGm_backend_gps.GPS_INT_SYMBOL_SCOPE, current_scope);

		//---------------------------------------------------------------------------
		// This information is redundant at this moment. Need to be clear up 
		//---------------------------------------------------------------------------
		GlobalMembersGm_gps_new_analysis_scope_sent_var.add_syminfo_struct(e, (symtab_type != SYMTAB_TYPES.GM_SYMTAB_FIELD), (current_scope == gm_gps_new_scope_analysis_t.GPS_NEW_SCOPE_GLOBAL) ? gm_gps_scope_t.GPS_SCOPE_GLOBAL : (current_scope == gm_gps_new_scope_analysis_t.GPS_NEW_SCOPE_IN) ? gm_gps_scope_t.GPS_SCOPE_INNER : gm_gps_scope_t.GPS_SCOPE_OUTER);

		return true;
	}

	public final boolean apply(ast_sent s)
	{
		s.add_info_int(GlobalMembersGm_backend_gps.GPS_INT_SYNTAX_CONTEXT, current_scope);

		if (s.get_nodetype() == AST_NODE_TYPE.AST_FOREACH)
		{
			ast_foreach fe = (ast_foreach) s;
			if (outer_loop == null)
			{
				assert fe.get_iterator().getTypeSummary() == GMTYPE_T.GMTYPE_NODEITER_ALL;
				outer_loop = fe;
				outer_loop.add_info_bool(GlobalMembersGm_backend_gps.GPS_FLAG_IS_OUTER_LOOP, true);
				outer_loop.get_iterator().getSymInfo().add_info_int(GlobalMembersGm_backend_gps.GPS_INT_SYMBOL_SCOPE, gm_gps_new_scope_analysis_t.GPS_NEW_SCOPE_OUT);
				outer_loop.get_iterator().getSymInfo().add_info_bool(GlobalMembersGm_backend_gps.GPS_FLAG_IS_OUTER_LOOP, true);
				current_scope = gm_gps_new_scope_analysis_t.GPS_NEW_SCOPE_OUT;
			}
			else if (inner_loop == null)
			{
				assert (fe.get_iterator().getTypeSummary() == GMTYPE_T.GMTYPE_NODEITER_NBRS) || (fe.get_iterator().getTypeSummary() == GMTYPE_T.GMTYPE_NODEITER_IN_NBRS);
				inner_loop = fe;
				inner_loop.add_info_bool(GlobalMembersGm_backend_gps.GPS_FLAG_IS_INNER_LOOP, true);
				inner_loop.get_iterator().getSymInfo().add_info_int(GlobalMembersGm_backend_gps.GPS_INT_SYMBOL_SCOPE, gm_gps_new_scope_analysis_t.GPS_NEW_SCOPE_IN);
				inner_loop.get_iterator().getSymInfo().add_info_bool(GlobalMembersGm_backend_gps.GPS_FLAG_IS_INNER_LOOP, true);
				current_scope = gm_gps_new_scope_analysis_t.GPS_NEW_SCOPE_IN;
			}
			else
			{
				assert false;
			}
		}
		else if (s.get_nodetype() == AST_NODE_TYPE.AST_ASSIGN)
		{
			ast_assign a = (ast_assign) s;
			if (a.is_target_scalar() && a.get_lhs_scala().getTypeInfo().is_edge_compatible())
			{
				if (current_scope == gm_gps_new_scope_analysis_t.GPS_NEW_SCOPE_IN)
				{
					// check rhs
					ast_expr rhs = a.get_rhs();
					assert rhs.is_builtin();
					ast_expr_builtin b = (ast_expr_builtin) rhs;

					if ((b.get_driver().getSymInfo().find_info_bool(GlobalMembersGm_backend_gps.GPS_FLAG_IS_INNER_LOOP)) && (b.get_builtin_def().get_method_id() == gm_method_id_t.GM_BLTIN_NODE_TO_EDGE))
					{
						a.get_lhs_scala().getSymInfo().add_info_bool(GlobalMembersGm_backend_gps.GPS_FLAG_IS_EDGE_ITERATOR, true);
					}
				}
			}
		}

		return true;
	}

	public final boolean apply2(ast_sent s)
	{
		if (s == outer_loop)
		{
			current_scope = gm_gps_new_scope_analysis_t.GPS_NEW_SCOPE_GLOBAL;
			outer_loop = null;
		}
		else if (s == inner_loop)
		{
			current_scope = gm_gps_new_scope_analysis_t.GPS_NEW_SCOPE_OUT;
			inner_loop = null;
		}
		return true;
	}

	private ast_foreach outer_loop;
	private ast_foreach inner_loop;
	private int current_scope;
}