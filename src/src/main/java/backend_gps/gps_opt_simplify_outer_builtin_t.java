package backend_gps;

import ast.AST_NODE_TYPE;
import ast.ast_assign;
import ast.ast_expr_builtin;
import ast.ast_foreach;
import ast.ast_id;
import ast.ast_sent;
import ast.ast_sentblock;
import frontend.gm_symtab_entry;

import common.GlobalMembersGm_main;
import common.GlobalMembersGm_transform_helper;
import common.gm_apply;
import common.gm_builtin_def;

public class gps_opt_simplify_outer_builtin_t extends gm_apply
{
	public gps_opt_simplify_outer_builtin_t()
	{
		set_for_sent(true);
		set_separate_post_apply(true);
		depth = 0;
		outer_iter = null;
	}

	@Override
	public boolean apply(ast_sent s)
	{
		if (s.get_nodetype() == AST_NODE_TYPE.AST_FOREACH)
		{
			depth++;
			if (depth == 1)
			{
				outer_iter = ((ast_foreach) s).get_iterator().getSymInfo();
			}
		}
		else if (depth == 2)
		{
			if (GlobalMembersGm_gps_opt_simplify_expr1.contains_built_in_through_driver(s, outer_iter))
			{
				L1.addLast(s);
				L2.addLast(outer_iter);
			}
		}
		return true;
	}
	@Override
	public boolean apply2(ast_sent s)
	{
		if (s.get_nodetype() == AST_NODE_TYPE.AST_FOREACH)
		{
			depth--;
		}
		return true;
	}

	public final void post_process()
	{
		java.util.Iterator<ast_sent> I = L1.iterator();
		java.util.Iterator<gm_symtab_entry> J = L2.iterator();
		java.util.HashMap<std.pair<ast_sentblock, Integer>, gm_symtab_entry> already_defined_map = new java.util.HashMap<std.pair<ast_sentblock, Integer>, gm_symtab_entry>();
		java.util.HashMap<ast_sentblock, gm_symtab_entry> sent_block_driver_map = new java.util.HashMap<ast_sentblock, gm_symtab_entry>(); // sentblock <-> driver
		for (; I.hasNext(); I++, J++)
		{
			ast_sent s = I.next();
			gm_symtab_entry drv = J.next();

			// make the sentence belong to a sent-block
			GlobalMembersGm_transform_helper.gm_make_it_belong_to_sentblock(s);
			assert s.get_parent().get_nodetype() == AST_NODE_TYPE.AST_SENTBLOCK;

			ast_sentblock sb = (ast_sentblock) s.get_parent();
			if (sent_block_driver_map.containsKey(sb))
			{
				assert sent_block_driver_map.get(sb) == drv;
			}
			sent_block_driver_map.put(sb, drv);

			GlobalMembersGm_gps_opt_simplify_expr1.replace_built_in(s, drv, sb, already_defined_map);
		}

		java.util.Iterator<std.pair<ast_sentblock, Integer>, gm_symtab_entry> K;
		for (K = already_defined_map.iterator(); K.hasNext();)
		{
			ast_sentblock sb = K.next().getKey().first;
			int method_id = K.next().getKey().second;
			gm_symtab_entry target = K.next().getValue();

			gm_symtab_entry drv = sent_block_driver_map.get(sb);
			assert sb != null;
			assert target != null;
			assert drv != null;

			// add initializer
			ast_id lhs_id = target.getId().copy(true);
			gm_builtin_def bin = GlobalMembersGm_main.BUILT_IN.find_builtin_def(drv.getType().getTypeSummary(), method_id);
			assert bin != null;
			assert bin.get_num_args() == 0;
			ast_id driver = drv.getId().copy(true);
			ast_expr_builtin rhs = ast_expr_builtin.new_builtin_expr(driver, bin.get_orgname(), null);
			rhs.set_type_summary(bin.get_result_type_summary());
			rhs.set_builtin_def(bin);

			ast_assign r_assign = ast_assign.new_assign_scala(lhs_id, rhs);
			GlobalMembersGm_transform_helper.gm_insert_sent_begin_of_sb(sb, r_assign);

		}
	}

	private java.util.LinkedList<ast_sent> L1 = new java.util.LinkedList<ast_sent>();
	private java.util.LinkedList<gm_symtab_entry> L2 = new java.util.LinkedList<gm_symtab_entry>();
	private int depth;
	private gm_symtab_entry outer_iter;

}