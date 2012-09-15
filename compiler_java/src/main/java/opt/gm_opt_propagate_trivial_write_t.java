package opt;

import java.util.HashMap;

import ast.ast_node_type;
import ast.ast_assign;
import ast.ast_id;
import ast.ast_sent;
import ast.ast_sentblock;
import ast.ast_typedecl;

import common.gm_add_symbol;
import common.gm_apply;
import common.gm_resolve_nc;
import common.gm_transform_helper;

import frontend.gm_symtab_entry;

//-------------------------------------------------------------------
//e.g.
//   { Int x = y;
//     z = x + 3; }
// ==>
//   { z = y + 3; } // x optimized out
//
//  Rule
//     (0) LHS is sclar, primitive or node/edge type
//     (1) the variable is assigned only once
//     (2) the rhs of assignment is trivial 
//     (3) [temporary] if the type of rhs is node/edge compatiable type, they should be iterators
//         (this is because for translation of random-write in GPS backend, you need explicit trivial writes)
//-------------------------------------------------------------------
//

public class gm_opt_propagate_trivial_write_t extends gm_apply
{
	public gm_opt_propagate_trivial_write_t()
	{
		set_for_sent(true);
		set_for_lhs(true);
		_changed = false;
	}
	@Override
	public boolean apply_lhs(ast_id i)
	{
		gm_symtab_entry z = i.getSymInfo();
		assert z != null;
		Integer old = assign_cnt.containsKey(z) ? assign_cnt.get(z) : 0;
		assign_cnt.put(z, old + 1);
		return true;
	}

	@Override
	public boolean apply(ast_sent s)
	{

		if (s.get_nodetype() == ast_node_type.AST_ASSIGN)
		{
			ast_assign a = (ast_assign) s;
			if (a.is_defer_assign() || a.is_reduce_assign())
				return true;
			if (!a.is_target_scalar())
				return true;
			ast_id lhs = a.get_lhs_scala();
			gm_symtab_entry z = lhs.getSymInfo();
			assert z != null;
			if (!lhs.getTypeInfo().is_primitive() && (!lhs.getTypeInfo().is_node_edge_compatible()))
			   return true;

			if (lhs.getSymInfo().isArgument())
				return true;

			if (a.get_rhs().is_id())
			{
				ast_id id = a.get_rhs().get_id();
				assert id.getSymInfo() != null;
				ast_typedecl t = id.getTypeInfo();
				// temporary
				if (t.is_primitive() || (t.is_node_edge_iterator()))
				{
					potential_assign.put(z, a);
				}
			}
		}
		return true;
	}

	public final boolean has_effect()
	{
		return _changed;
	}

	public final void post_process()
	{
		for (gm_symtab_entry lhs_sym : potential_assign.keySet())
		{
			ast_assign a = potential_assign.get(lhs_sym);
			if (assign_cnt.get(lhs_sym) > 1)
				continue;

			// now replace old symbol to rhs symbol
			gm_symtab_entry rhs_sym = a.get_rhs().get_id().getSymInfo();
			ast_sentblock sb = gm_add_symbol.gm_find_defining_sentblock_up(a, lhs_sym);
			assert sb != null;
			gm_resolve_nc.gm_replace_symbol_entry(lhs_sym, rhs_sym, sb);

			_changed = true;

			// remove assignment
			gm_transform_helper.gm_ripoff_sent(a);
			if (a != null)
				a.dispose();
		}
	}

	private HashMap<gm_symtab_entry, Integer> assign_cnt = new HashMap<gm_symtab_entry, Integer>();
	private HashMap<gm_symtab_entry, ast_assign> potential_assign = new HashMap<gm_symtab_entry, ast_assign>();
	private boolean _changed;

}