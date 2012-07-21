package backend_gps;

import ast.ast_expr;
import ast.ast_expr_builtin;
import ast.ast_id;
import frontend.gm_symtab_entry;
import inc.GlobalMembersGm_backend_gps;

import common.gm_apply;

//---------------------------------------------------------------------
// Find scope of each expression
//---------------------------------------------------------------------
public class gm_gps_new_analysis_rhs_lhs_t extends gm_apply
{
	public gm_gps_new_analysis_rhs_lhs_t()
	{
		set_for_expr(true);
	}

	public final boolean apply(ast_expr e)
	{
		int t;
		int l;
		int r;
		int scope;

		switch (e.get_opclass())
		{

			case GMEXPR_IVAL:
			case GMEXPR_FVAL:
			case GMEXPR_BVAL:
			case GMEXPR_INF:
			case GMEXPR_NIL:
				e.add_info_int(GlobalMembersGm_backend_gps.GPS_INT_EXPR_SCOPE, gm_gps_new_scope_analysis_t.GPS_NEW_SCOPE_GLOBAL);
				break;
			case GMEXPR_ID:
				scope = get_scope_from_id(e.get_id().getSymInfo());
				e.add_info_int(GlobalMembersGm_backend_gps.GPS_INT_EXPR_SCOPE, scope);
				break;

			case GMEXPR_FIELD:
				scope = get_scope_from_driver(e.get_field().get_first().getSymInfo());
				e.add_info_int(GlobalMembersGm_backend_gps.GPS_INT_EXPR_SCOPE, scope);
				break;

			case GMEXPR_UOP:
			case GMEXPR_LUOP:
				e.add_info_int(GlobalMembersGm_backend_gps.GPS_INT_EXPR_SCOPE, e.get_left_op().find_info_int(GlobalMembersGm_backend_gps.GPS_INT_EXPR_SCOPE));
				break;

			case GMEXPR_BIOP:
			case GMEXPR_LBIOP:
			case GMEXPR_COMP:
				l = e.get_left_op().find_info_int(GlobalMembersGm_backend_gps.GPS_INT_EXPR_SCOPE);
				r = e.get_right_op().find_info_int(GlobalMembersGm_backend_gps.GPS_INT_EXPR_SCOPE);
				e.add_info_int(GlobalMembersGm_backend_gps.GPS_INT_EXPR_SCOPE, GlobalMembersGm_gps_new_analysis_scope_rhs_lhs.get_more_restricted_scope(l, r));
				break;

			case GMEXPR_TER:
				l = e.get_left_op().find_info_int(GlobalMembersGm_backend_gps.GPS_INT_EXPR_SCOPE);
				r = e.get_right_op().find_info_int(GlobalMembersGm_backend_gps.GPS_INT_EXPR_SCOPE);
				t = e.get_cond_op().find_info_int(GlobalMembersGm_backend_gps.GPS_INT_EXPR_SCOPE);
				e.add_info_int(GlobalMembersGm_backend_gps.GPS_INT_EXPR_SCOPE, GlobalMembersGm_gps_new_analysis_scope_rhs_lhs.get_more_restricted_scope(t, GlobalMembersGm_gps_new_analysis_scope_rhs_lhs.get_more_restricted_scope(l, r)));
				break;

			case GMEXPR_BUILTIN:
			{
				ast_expr_builtin b = (ast_expr_builtin) e;
				ast_id i = b.get_driver();
				if (i == null)
				{
					e.add_info_int(GlobalMembersGm_backend_gps.GPS_INT_EXPR_SCOPE, gm_gps_new_scope_analysis_t.GPS_NEW_SCOPE_GLOBAL);
					break;
				}

				// scope from driver
				int t = get_scope_from_driver(i.getSymInfo());

				// scope of arguments
				java.util.Iterator<ast_expr> I;
				java.util.LinkedList<ast_expr> L = b.get_args();
				for (I = L.iterator(); I.hasNext();)
				{
					ast_expr ee = I.next();
					t = GlobalMembersGm_gps_new_analysis_scope_rhs_lhs.get_more_restricted_scope(t, ee.find_info_int(GlobalMembersGm_backend_gps.GPS_INT_EXPR_SCOPE));
				}

				e.add_info_int(GlobalMembersGm_backend_gps.GPS_INT_EXPR_SCOPE, t);
			}
				break;

			case GMEXPR_REDUCE:
			case GMEXPR_FOREIGN:
			default:
				assert false;
				break;
		}
		return true;
	}

	public final int get_scope_from_id(gm_symtab_entry e)
	{
		return e.find_info_int(GlobalMembersGm_backend_gps.GPS_INT_SYMBOL_SCOPE);
	}

	public final int get_scope_from_driver(gm_symtab_entry e)
	{
		return get_scope_from_driver(e, false);
	}
//C++ TO JAVA CONVERTER NOTE: Java does not allow default values for parameters. Overloaded methods are inserted above.
//ORIGINAL LINE: int get_scope_from_driver(gm_symtab_entry* e, boolean is_rarrow = false)
	public final int get_scope_from_driver(gm_symtab_entry e, boolean is_rarrow)
	{
		if (e.find_info_bool(GlobalMembersGm_backend_gps.GPS_FLAG_IS_INNER_LOOP))
			return gm_gps_new_scope_analysis_t.GPS_NEW_SCOPE_IN;
		else if (e.find_info_bool(GlobalMembersGm_backend_gps.GPS_FLAG_IS_OUTER_LOOP))
			return gm_gps_new_scope_analysis_t.GPS_NEW_SCOPE_OUT;
		else if (e.find_info_bool(GlobalMembersGm_backend_gps.GPS_FLAG_IS_EDGE_ITERATOR))
			return gm_gps_new_scope_analysis_t.GPS_NEW_SCOPE_EDGE;
		else
			return gm_gps_new_scope_analysis_t.GPS_NEW_SCOPE_RANDOM;
	}


}