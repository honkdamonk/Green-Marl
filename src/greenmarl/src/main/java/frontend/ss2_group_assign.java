import ast.AST_NODE_TYPE;
import ast.ast_assign;
import ast.ast_expr;
import ast.ast_expr_builtin;
import ast.ast_field;
import ast.ast_foreach;
import ast.ast_id;
import ast.ast_nop;
import ast.ast_sent;
import inc.GMTYPE_T;
import inc.GlobalMembersGm_defs;

import common.GlobalMembersGm_transform_helper;
import common.gm_apply;

public class ss2_group_assign extends gm_apply
{
	// traverse sentence
	@Override
	public boolean apply(ast_sent s)
	{
		if (s.get_nodetype() != AST_NODE_TYPE.AST_ASSIGN)
			return true;
		ast_assign a = (ast_assign) s;
		if (a.is_target_scalar())
			return true;

		ast_field lhs = a.get_lhs_field();
		assert lhs != null;
		if (!GlobalMembersGm_defs.gm_is_graph_type(lhs.getSourceTypeSummary()))
			return true;

		// append to a seperate list and process them later
		target_list.addLast(a);
		return true;
	}

	public final void post_process()
	{
		java.util.Iterator<ast_assign> i;
		for (i = target_list.iterator(); i.hasNext();)
		{
			ast_assign next = i.next();
			post_process_item(next);
		}
		target_list.clear();
	}

	protected java.util.LinkedList<ast_assign> target_list = new java.util.LinkedList<ast_assign>();

	protected final boolean post_process_item(ast_assign a)
	{
		// temporary node
		ast_nop NOP = new ast_temp_marker();

		//----------------------------------------------------
		// (Replace s with s', but with correct IR management)
		// 1. add nop after s.
		// 2. rip-off s
		// 3. create s' out of s
		// 4. add s' after nop
		// 5. rip off nop.
		//----------------------------------------------------
		GlobalMembersGm_transform_helper.gm_add_sent_after(a, NOP);
		//ast_sentblock *SB = (ast_sentblock*) a->get_parent();
		GlobalMembersGm_transform_helper.gm_ripoff_sent(a);
		ast_foreach fe = GlobalMembersGm_expand_group_assignment.create_surrounding_fe(a);
		GlobalMembersGm_transform_helper.gm_add_sent_after(NOP, fe);
		GlobalMembersGm_transform_helper.gm_ripoff_sent(NOP);

		if (NOP != null)
			NOP.dispose(); // no need after this

		//--------------------------------------------------------------------
		// 1. replace lhs driver with iterator 
		// 2. traverse rhs, replace graph reference to new iterator reference
		//--------------------------------------------------------------------
		ast_field lhs = a.get_lhs_field();
		ast_id old = lhs.get_first();
		ast_id iter = fe.get_iterator().copy(true);
		iter.set_line(old.get_line());
		iter.set_col(old.get_col());
		lhs.set_first(iter);

		// 2.
		this.old_driver_sym = old.getSymInfo();
		this.new_driver = iter;
		this.set_for_expr(true);
		ast_expr rhs = a.get_rhs();
		rhs.traverse_pre(this);
		this.set_for_expr(false);

		if (old != null)
			old.dispose();

		return true;
	}

	// traverse expr
	@Override
	public boolean apply(ast_expr e)
	{
		if (e.is_id())
		{
			ast_id old = e.get_id();
			// replace G.A -> iter.A
			if ((old.getSymInfo() == this.old_driver_sym) && ((e.get_type_summary() == GMTYPE_T.GMTYPE_NODE) || (e.get_type_summary() == GMTYPE_T.GMTYPE_EDGE)))
			{
				old.setSymInfo(this.new_driver.getSymInfo());
				e.set_type_summary(new_driver.getTypeSummary());
			}
		}
		if (e.is_field())
		{
			ast_field f = e.get_field();
			ast_id old = f.get_first();
			// replace G.A -> iter.A
			if (old.getSymInfo() == this.old_driver_sym)
			{
				ast_id iter = new_driver.copy(true);
				iter.set_line(old.get_line());
				iter.set_col(old.get_col());
				f.set_first(iter);
				if (old != null)
					old.dispose();
			}
		}
		else if (e.is_builtin())
		{
			ast_expr_builtin e2 = (ast_expr_builtin) e;
			ast_id old = e2.get_driver();
			if ((old != null) && (old.getSymInfo() == this.old_driver_sym))
			{

				// If the builtin-op is for graph do not replace!
				if (old.getTypeSummary() != e2.get_builtin_def().get_source_type_summary())
				{
					ast_id iter = new_driver.copy(true);
					iter.set_line(old.get_line());
					iter.set_col(old.get_col());
					if (old != null)
						old.dispose();
					e2.set_driver(iter);
				}
			}
		}

		return true;
	}

	protected gm_symtab_entry old_driver_sym;
	protected ast_id new_driver;

}