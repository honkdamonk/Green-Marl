package backend_gps;

import ast.AST_NODE_TYPE;
import ast.ast_assign;
import ast.ast_foreach;
import ast.ast_id;
import ast.ast_node;
import ast.ast_sent;
import frontend.SYMTAB_TYPES;
import frontend.gm_symtab_entry;

import common.gm_apply;

//----------------------------------------------------
// Foreach (n: G.Nodes) {
//   Int S;
//   S = 0;
//   Foreach( t: n.InNbrs) {
//       S += t.A;
//   }
//   n.B = S * alpha + beta;
// }
// ===>  
// N_P<Int>(G) _tmp_S;
// Foreach (n: G.Nodes) {
//   n._tmp_S = 0;
//   Foreach( t: n.InNbrs) {
//       n._tmp_S += t.A;
//   }
//   n.B = n._tmp_S * alpha + beta;
// }
//----------------------------------------------------

public class gps_opt_find_scalar_replace_target_t extends gm_apply
{

	public gps_opt_find_scalar_replace_target_t(java.util.HashMap<ast_foreach, ast_foreach> M)
	{
		this.MAP = new java.util.HashMap<ast_foreach, ast_foreach>(M);
		set_for_sent(true);
		set_for_symtab(true);
		set_separate_post_apply(true);
		level = 0;
		outloop = null;
		inloop = null;
	}
	@Override
	public boolean apply(gm_symtab_entry e, int symtab_type)
	{
		// find scalar variables defined in the first level
		if ((level == 1) && (symtab_type == SYMTAB_TYPES.GM_SYMTAB_VAR.getValue()))
		{
			if (e.getType().is_primitive())
				potential_target_syms.add(e);
		}
		return true;
	}

	@Override
	public boolean apply(ast_sent s)
	{
		// level management
		if (s.get_nodetype() == AST_NODE_TYPE.AST_FOREACH)
		{
			ast_foreach fe = (ast_foreach) s;
			if (level == 0)
			{
				if (MAP.containsKey(fe))
				{
					level = 1;
					outloop = fe;
				}
			}
			else if (level == 1)
			{
				if (MAP.containsKey(fe))
				{
					level = 2;
					inloop = fe;
				}
			}
		}

		else if ((level == 2) && (s.get_nodetype() == AST_NODE_TYPE.AST_ASSIGN))
		{
			ast_assign a = (ast_assign) s;
			if (a.is_target_scalar())
			{
				// check if LHS is potential target
				gm_symtab_entry target = a.get_lhs_scala().getSymInfo();
				if (potential_target_syms.find(target).hasNext())
				{
					target_syms.put(target, outloop); // found target
				}

				if (a.has_lhs_list())
				{
					java.util.LinkedList<ast_node> lhs_list = a.get_lhs_list();
					java.util.Iterator<ast_node> I;
					for (I = lhs_list.iterator(); I.hasNext();)
					{
						ast_node n = I.next();
						if (n.get_nodetype() != AST_NODE_TYPE.AST_ID)
							continue;
						ast_id id = (ast_id) n;
						target = id.getSymInfo();
						if (potential_target_syms.find(target).hasNext()) // found target
							target_syms.put(target, outloop);
					}
				}
			}
		}
		return true;
	}

	@Override
	public boolean apply2(ast_sent s)
	{
		if (s.get_nodetype() == AST_NODE_TYPE.AST_FOREACH)
		{
			ast_foreach fe = (ast_foreach) s;
			if ((level == 2) && (inloop == fe))
			{
				level = 1;
			}
			else if ((level == 1) && (outloop = fe) != null)
			{
				level = 0;
			}
		}
		return true;
	}

	public final java.util.HashMap<gm_symtab_entry, ast_foreach> get_target_syms_and_outer_loop()
	{
		return target_syms;
	}

	private java.util.HashMap<ast_foreach, ast_foreach> MAP;
	private java.util.HashSet<gm_symtab_entry> potential_target_syms = new java.util.HashSet<gm_symtab_entry>();
	private java.util.HashMap<gm_symtab_entry, ast_foreach> target_syms = new java.util.HashMap<gm_symtab_entry, ast_foreach>();
	private ast_foreach outloop;
	private ast_foreach inloop;
	private int level;
}