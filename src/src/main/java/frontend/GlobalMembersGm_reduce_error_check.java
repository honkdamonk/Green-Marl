package frontend;

import inc.GM_REDUCE_T;
import ast.ast_id;

import common.GM_ERRORS_AND_WARNINGS;
import common.GlobalMembersGm_error;
import common.GlobalMembersGm_misc;

public class GlobalMembersGm_reduce_error_check
{
	public static boolean is_conflict(java.util.LinkedList<bound_info_t> L, gm_symtab_entry t, gm_symtab_entry b, int r_type, tangible.RefObject<Boolean> is_bound_error, tangible.RefObject<Boolean> is_type_error)
	{
		java.util.Iterator<bound_info_t> i;
		is_type_error.argvalue = false;
		is_bound_error.argvalue = false;
		for (i = L.iterator(); i.hasNext();)
		{
			bound_info_t db = i.next();
			if (db.target == t)
			{
				if (db.bound != b)
				{
					is_bound_error.argvalue = true;
					return true;
				}
				else if (db.reduce_type != r_type)
				{
					is_type_error.argvalue = true;
					return true;
				}
			}
		}
		return false;
	}
	public static void add_bound(java.util.LinkedList<bound_info_t> L, gm_symtab_entry t, gm_symtab_entry b, int r_type)
	{
		bound_info_t T = new bound_info_t();
		T.target = t;
		T.bound = b;
		T.reduce_type = r_type;
		L.addLast(T);
	}
	public static void remove_bound(java.util.LinkedList<bound_info_t> L, gm_symtab_entry t, gm_symtab_entry b, int r_type)
	{
		java.util.Iterator<bound_info_t> i;
		for (i = L.iterator(); i.hasNext();)
		{
			bound_info_t db = i.next();
			if ((db.target == t) && (db.reduce_type == r_type) && (db.bound == b))
			{
//C++ TO JAVA CONVERTER TODO TASK: There is no direct equivalent to the STL list 'erase' method in Java:
				L.erase(i);
				if (db != null)
				db.dispose();
				return;
			}
		}
		return;
	}

	// returns is_okay
	public static boolean check_add_and_report_conflicts(java.util.LinkedList<bound_info_t> L, java.util.HashMap<gm_symtab_entry, java.util.LinkedList<gm_rwinfo>> B)
	{
		java.util.Iterator<gm_symtab_entry, java.util.LinkedList<gm_rwinfo>> i;
		for (i = B.iterator(); i.hasNext();)
		{
			gm_symtab_entry e = i.next().getKey();
			java.util.LinkedList<gm_rwinfo> l = i.next().getValue();
			java.util.Iterator<gm_rwinfo> j;
			for (j = l.iterator(); j.hasNext();)
			{
				gm_rwinfo jj = j.next();
				boolean is_bound_error = false;
				boolean is_type_error = false;
				assert jj.bound_symbol != null;
				assert jj.reduce_op != GM_REDUCE_T.GMREDUCE_NULL.getValue();
			tangible.RefObject<Boolean> tempRef_is_bound_error = new tangible.RefObject<Boolean>(is_bound_error);
			tangible.RefObject<Boolean> tempRef_is_type_error = new tangible.RefObject<Boolean>(is_type_error);
				boolean tempVar = GlobalMembersGm_reduce_error_check.is_conflict(L, e, jj.bound_symbol, jj.reduce_op, tempRef_is_bound_error, tempRef_is_type_error);
			is_bound_error = tempRef_is_bound_error.argvalue;
			is_type_error = tempRef_is_type_error.argvalue;
			if (tempVar)
			{
					ast_id loc = jj.location;
					if (is_bound_error)
					{
						GlobalMembersGm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_DOUBLE_BOUND_ITOR, loc.get_line(), loc.get_col(), jj.bound_symbol.getId().get_orgname());
						return false;
					}
					if (is_type_error)
					{
						GlobalMembersGm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_DOUBLE_BOUND_OP, loc.get_line(), loc.get_col(), GlobalMembersGm_misc.gm_get_reduce_string(jj.reduce_op));
						return false;
					}
			}
				else
				{
					GlobalMembersGm_reduce_error_check.add_bound(L, e, jj.bound_symbol, jj.reduce_op);
				}
			}
		}
		return true;
	}
	public static void remove_all(java.util.LinkedList<bound_info_t> L, java.util.HashMap<gm_symtab_entry, java.util.LinkedList<gm_rwinfo>> B)
	{
		java.util.Iterator<gm_symtab_entry, java.util.LinkedList<gm_rwinfo>> i;
		for (i = B.iterator(); i.hasNext();)
		{
			gm_symtab_entry e = i.next().getKey();
			java.util.LinkedList<gm_rwinfo> l = i.next().getValue();
			java.util.Iterator<gm_rwinfo> j;
			for (j = l.iterator(); j.hasNext();)
			{
				gm_rwinfo jj = j.next();
				GlobalMembersGm_reduce_error_check.remove_bound(L, e, jj.bound_symbol, jj.reduce_op);
			}
		}
	}
}