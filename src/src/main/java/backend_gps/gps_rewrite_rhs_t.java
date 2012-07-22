package backend_gps;

import ast.AST_NODE_TYPE;
import ast.ast_assign;
import ast.ast_expr;
import ast.ast_field;
import ast.ast_foreach;
import ast.ast_id;
import ast.ast_sent;
import ast.ast_sentblock;
import frontend.gm_symtab_entry;
import inc.GMTYPE_T;
import inc.GlobalMembersGm_backend_gps;
import inc.GlobalMembersGm_defs;

import common.GlobalMembersGm_add_symbol;
import common.GlobalMembersGm_main;
import common.GlobalMembersGm_transform_helper;
import common.gm_apply;

public class gps_rewrite_rhs_t extends gm_apply
{
	public gps_rewrite_rhs_t()
	{
		set_for_expr(true);
		set_for_sent(true);
		current_fe = null;
	}

	public final boolean apply(ast_sent s)
	{
		if (s.get_nodetype() == AST_NODE_TYPE.AST_FOREACH)
		{
			if (s.find_info_bool(GlobalMembersGm_backend_gps.GPS_FLAG_IS_INNER_LOOP))
			{
				current_fe = (ast_foreach) s;
				java.util.HashSet<ast_expr> empty = new java.util.HashSet<ast_expr>();
				sub_exprs.put(current_fe, empty); // initialization by copying
			}
		}

		return true;
	}

	public final boolean apply(ast_expr e)
	{
		ast_sent s = get_current_sent();
		if (s.find_info_int(GlobalMembersGm_backend_gps.GPS_INT_SYNTAX_CONTEXT) != gm_gps_new_scope_analysis_t.GPS_NEW_SCOPE_IN)
			return true;

		if ((e.find_info_int(GlobalMembersGm_backend_gps.GPS_INT_EXPR_SCOPE) == gm_gps_new_scope_analysis_t.GPS_NEW_SCOPE_OUT) || (e.find_info_int(GlobalMembersGm_backend_gps.GPS_INT_EXPR_SCOPE) == gm_gps_new_scope_analysis_t.GPS_NEW_SCOPE_EDGE))
		{
			// (current traversal engine does not support pruning, so should look at parents
			//
			if (!parent_already_added(e))
			{
				sub_exprs.get(current_fe).add(e);
			}
		}

		return true;
	}

	public final void process()
	{
		java.util.Iterator<ast_foreach, java.util.HashSet<ast_expr>> I;

		for (I = sub_exprs.iterator(); I.hasNext();)
		{
			if (I.next().getValue().size() > 0)
				process_foreach(I.next().getKey(), I.next().getValue());
		}
	}

	public final void process_foreach(ast_foreach fe, java.util.HashSet<ast_expr> exprs)
	{
		java.util.HashMap<gm_symtab_entry, gm_symtab_entry > props_vars = new java.util.HashMap<gm_symtab_entry, gm_symtab_entry >();
		java.util.HashMap<ast_expr, gm_symtab_entry > expr_vars = new java.util.HashMap<ast_expr, gm_symtab_entry >();

		assert fe.find_info_bool(GlobalMembersGm_backend_gps.GPS_FLAG_IS_INNER_LOOP);

		ast_sentblock sb = (ast_sentblock)(fe.get_body());
		//printf("(2)fe = %p, sb = %p\n", fe, sb);
		assert sb.get_nodetype() == AST_NODE_TYPE.AST_SENTBLOCK;


		gm_symtab_entry out_iter = null;

		// process raw property access first
		java.util.Iterator<ast_expr> I;
		for (I = exprs.iterator(); I.hasNext();)
		{
			ast_expr e = I.next();
			if (e.is_field())
			{
				gm_symtab_entry p = e.get_field().get_second().getSymInfo();
				if (!props_vars.containsKey(p))
				{
					gm_symtab_entry target = define_temp(p.getType().getTargetTypeSummary(), sb, p.getType().get_target_graph_sym());

					props_vars.put(p, target);
					if (out_iter == null)
						out_iter = e.get_field().get_first().getSymInfo();
					else
					{
						assert out_iter == e.get_field().get_first().getSymInfo();
					}
				}
			}
		}

		// process complicated sub expressions
		for (I = exprs.iterator(); I.hasNext();)
		{
			ast_expr e = I.next();
			if (e.is_field())
				continue;

			// for future optimization
			if (GlobalMembersGm_gps_new_rewrite_rhs.is_composed_of(e, props_vars))
			{
				assert false;
			}
			else
			{
				gm_symtab_entry target;
				if (GlobalMembersGm_defs.gm_is_node_edge_compatible_type(e.get_type_summary()))
				{
					assert e.is_id();
					target = define_temp(e.get_type_summary(), sb, e.get_id().getTypeInfo().get_target_graph_sym());
				}
				else
				{
					target = define_temp(e.get_type_summary(), sb, null);
				}
				expr_vars.put(e, target);
			}
		}

		// replace accesses
		for (I = exprs.iterator(); I.hasNext();)
		{
			ast_expr e = I.next();
			// if not is_composed_of ...
			if (e.is_field())
				GlobalMembersGm_gps_new_rewrite_rhs.replace_access_expr(e, props_vars.get(e.get_field().get_second().getSymInfo()), true);
			else
				GlobalMembersGm_gps_new_rewrite_rhs.replace_access_expr(e, expr_vars.get(e), false);
		}

		// create definitions
		java.util.Iterator<ast_expr, gm_symtab_entry > K;
		for (K = expr_vars.iterator(); K.hasNext();)
		{
			ast_expr rhs = K.next().getKey();
			gm_symtab_entry target = K.next().getValue();
			ast_id lhs_id = target.getId().copy(true);
			ast_assign r_assign = ast_assign.new_assign_scala(lhs_id, rhs);

			r_assign.add_info_bool(GlobalMembersGm_backend_gps.GPS_FLAG_COMM_DEF_ASSIGN, true);

			GlobalMembersGm_transform_helper.gm_insert_sent_begin_of_sb(sb, r_assign);
		}

		java.util.Iterator<gm_symtab_entry, gm_symtab_entry > J;
		for (J = props_vars.iterator(); J.hasNext();)
		{
			gm_symtab_entry prop = J.next().getKey();
			gm_symtab_entry target = J.next().getValue();

			ast_id lhs_id = target.getId().copy(true);
			ast_id driver = out_iter.getId().copy(true);
			ast_id prop_id = prop.getId().copy(true);
			ast_field f = ast_field.new_field(driver, prop_id);
			ast_expr rhs = ast_expr.new_field_expr(f);
			ast_assign r_assign = ast_assign.new_assign_scala(lhs_id, rhs);

			r_assign.add_info_bool(GlobalMembersGm_backend_gps.GPS_FLAG_COMM_DEF_ASSIGN, true);

			GlobalMembersGm_transform_helper.gm_insert_sent_begin_of_sb(sb, r_assign);
		}

		// move edge-defining writes at the top
		java.util.LinkedList<ast_sent> sents_to_move = new java.util.LinkedList<ast_sent>();
		java.util.Iterator<ast_sent> Y;
		for (Y = sb.get_sents().iterator(); Y.hasNext();)
		{
			ast_sent s = Y.next();
			if (s.find_info_bool(GlobalMembersGm_backend_gps.GPS_FLAG_EDGE_DEFINING_WRITE))
			{
				sents_to_move.addLast(s);
			}
		}
		for (Y = sents_to_move.iterator(); Y.hasNext();)
		{
			GlobalMembersGm_transform_helper.gm_ripoff_sent(Y.next());
			GlobalMembersGm_transform_helper.gm_insert_sent_begin_of_sb(sb, Y.next());
		}

	}

	private java.util.HashMap<ast_foreach, java.util.HashSet<ast_expr>> sub_exprs = new java.util.HashMap<ast_foreach, java.util.HashSet<ast_expr>>();
	private ast_foreach current_fe;

	private gm_symtab_entry define_temp(GMTYPE_T type, ast_sentblock sb, gm_symtab_entry graph)
	{
		String temp_name = GlobalMembersGm_main.FE.voca_temp_name_and_add("_m");
		gm_symtab_entry target;
		if (GlobalMembersGm_defs.gm_is_prim_type(type))
		{
			target = GlobalMembersGm_add_symbol.gm_add_new_symbol_primtype(sb, type, (String) temp_name);
		}
		else if (GlobalMembersGm_defs.gm_is_node_edge_compatible_type(type))
		{
			if (GlobalMembersGm_defs.gm_is_node_compatible_type(type))
			{
				type = GMTYPE_T.GMTYPE_NODE;
			}
			else if (GlobalMembersGm_defs.gm_is_edge_compatible_type(type))
			{
				type = GMTYPE_T.GMTYPE_EDGE;
			}
			target = GlobalMembersGm_add_symbol.gm_add_new_symbol_nodeedge_type(sb, type, graph, (String) temp_name);
		}
		else
		{
			assert false;
		}
		temp_name = null;

		return target;
	}

	private boolean parent_already_added(ast_expr e)
	{
		e = e.get_up_op();
		while (e != null)
		{
			if (sub_exprs.get(current_fe).find(e).hasNext())
			{
				return true;
			}
			e = e.get_up_op();
		}
		return false;
	}
}