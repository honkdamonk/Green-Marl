package backend_gps;

import frontend.gm_symtab_entry;
import inc.GMTYPE_T;
import ast.ast_node_type;
import ast.ast_assign;
import ast.ast_expr;
import ast.ast_expr_builtin;
import ast.ast_foreach;
import ast.ast_id;
import ast.ast_node;
import ast.ast_sent;
import ast.ast_sentblock;

import common.gm_add_symbol;
import common.gm_apply;
import common.gm_main;
import common.gm_method_id_t;
import common.gm_new_sents_after_tc;
import common.gm_resolve_nc;
import common.gm_transform_helper;

//----------------------------------------------------
// Foreach(e:G.Edges) {
//
//   Node a = e.From()
//   Node b = e.To()
//
// }
// ==>
// Foreach(n:G.Nodes)
//    Foreach(t:n.Nbrs) {
//       Edge e = t.ToEdge();
//       // Node a = e.From()
//       // Node b = e.To()
//       // a --> n
//       // b --> t
//    }
// }
//----------------------------------------------------

public class gps_opt_edge_iteration_t extends gm_apply
{
	public gps_opt_edge_iteration_t()
	{
		set_for_sent(true);
	}

	@Override
	public boolean apply(ast_sent s)
	{
		if (s.get_nodetype() == ast_node_type.AST_FOREACH)
		{
			// check iteratation type
			ast_foreach fe = (ast_foreach) s;
			if (fe.get_iter_type() == GMTYPE_T.GMTYPE_EDGEITER_ALL)
			{
				_targets.addLast(fe);
			}
		}
		return true;
	}

	public final boolean has_targets()
	{
		return _targets.size() > 0;
	}

	public final void post_process()
	{
		java.util.Iterator<ast_foreach> I;
		for (I = _targets.iterator(); I.hasNext();)
		{
			ast_foreach fe = I.next();

			String old_edge_iter_name = fe.get_iterator().get_genname();
			ast_sent body = fe.get_body();
			gm_transform_helper.gm_ripoff_sent(body);

			// (1) create outer foreach loop
			ast_sentblock sb = ast_sentblock.new_sentblock();
			String outer_name = gm_main.FE.voca_temp_name_and_add("_n");
			ast_id outer_id = ast_id.new_id(outer_name, fe.get_iterator().get_line(), fe.get_iterator().get_col());
			ast_foreach outer_fe = gm_new_sents_after_tc.gm_new_foreach_after_tc(outer_id, fe.get_iterator().getTypeInfo().get_target_graph_id().copy(true), sb, GMTYPE_T.GMTYPE_NODEITER_ALL);

			// (2) create inner foreach loop 
			String inner_name = gm_main.FE.voca_temp_name_and_add("_t");
			ast_id inner_id = ast_id.new_id(inner_name, fe.get_iterator().get_line(), fe.get_iterator().get_col());
			ast_foreach inner_fe = gm_new_sents_after_tc.gm_new_foreach_after_tc(inner_id, outer_id.copy(true), body, GMTYPE_T.GMTYPE_NODEITER_NBRS);
			sb.add_sent(inner_fe);

			// (3) replace fe -> outer_fe
			gm_transform_helper.gm_replace_sent(fe, outer_fe);
			gm_transform_helper.gm_reconstruct_scope(outer_fe);

			// (4) create new symbol entry in the body
			if (body.get_nodetype() != ast_node_type.AST_SENTBLOCK)
			{
				gm_transform_helper.gm_make_it_belong_to_sentblock(body);
				body = (ast_sentblock) body.get_parent();
				assert body.get_nodetype() == ast_node_type.AST_SENTBLOCK;
			}
			ast_sentblock sb2 = (ast_sentblock) body;
			gm_symtab_entry old_edge_symbol = fe.get_iterator().getSymInfo();
			gm_symtab_entry new_edge_symbol = gm_add_symbol.gm_add_new_symbol_nodeedge_type(sb2, GMTYPE_T.GMTYPE_EDGE, fe.get_iterator().getTypeInfo().get_target_graph_sym(), old_edge_iter_name);

			// (5) replace expressions (from/to)
			replace_from_to_builtin(body, old_edge_symbol, outer_fe.get_iterator().getSymInfo(), inner_fe.get_iterator().getSymInfo());

			// (5) replace other symbol accesses
			gm_resolve_nc.gm_replace_symbol_entry_bound(old_edge_symbol, outer_fe.get_iterator().getSymInfo(), body);
			gm_resolve_nc.gm_replace_symbol_entry(old_edge_symbol, new_edge_symbol, body);

			// (6) add definition of the new edge symbol at the top
			ast_expr rhs = ast_expr_builtin.new_builtin_expr(inner_id.copy(true), gm_main.BUILT_IN.find_builtin_def(GMTYPE_T.GMTYPE_NODEITER_NBRS, gm_method_id_t.GM_BLTIN_NODE_TO_EDGE), null);
			ast_assign new_assign = ast_assign.new_assign_scala(new_edge_symbol.getId().copy(true), rhs);
			gm_transform_helper.gm_insert_sent_begin_of_sb(sb2, new_assign);
			gm_transform_helper.gm_reconstruct_scope(outer_fe);

			old_edge_iter_name = null;
			outer_name = null;
			inner_name = null;

			if (fe != null)
				fe.dispose();
			//printf("fe = %p\n", fe);
			//printf("outer_fe = %p\n", outer_fe);
			//printf("inner_fe = %p\n", inner_fe);
		}
	}

	private java.util.LinkedList<ast_foreach> _targets = new java.util.LinkedList<ast_foreach>();
	private void replace_from_to_builtin(ast_node body, gm_symtab_entry old_edge, gm_symtab_entry outer, gm_symtab_entry inner)
	{

		gm_gps_opt_replace_from_to_builtin_t Q = new gm_gps_opt_replace_from_to_builtin_t(old_edge, outer, inner);
		gm_transform_helper.gm_replace_expr_general(body, Q);
	}

}