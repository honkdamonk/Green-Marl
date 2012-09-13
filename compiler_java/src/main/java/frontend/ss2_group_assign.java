package frontend;

import static inc.GMTYPE_T.GMTYPE_EDGE;
import static inc.GMTYPE_T.GMTYPE_EDGEITER_ALL;
import static inc.GMTYPE_T.GMTYPE_NODE;
import static inc.GMTYPE_T.GMTYPE_NODEITER_ALL;
import inc.GMTYPE_T;

import java.util.LinkedList;

import ast.AST_NODE_TYPE;
import ast.ast_assign;
import ast.ast_expr;
import ast.ast_expr_builtin;
import ast.ast_field;
import ast.ast_foreach;
import ast.ast_id;
import ast.ast_nop;
import ast.ast_sent;

import common.gm_main;
import common.gm_new_sents_after_tc;
import common.gm_transform_helper;
import common.gm_apply;

public class ss2_group_assign extends gm_apply {

	protected gm_symtab_entry old_driver_sym;
	protected ast_id new_driver;

	/** traverse sentence */
	@Override
	public boolean apply(ast_sent s) {
		if (s.get_nodetype() != AST_NODE_TYPE.AST_ASSIGN)
			return true;
		ast_assign a = (ast_assign) s;
		if (a.is_target_scalar() || a.is_target_map_entry())
			return true;

		ast_field lhs = a.get_lhs_field();
		assert lhs != null;
		if (!lhs.getSourceTypeSummary().is_graph_type())
			return true;

		// append to a seperate list and process them later
		target_list.addLast(a);
		return true;
	}

	public final void post_process() {
		for (ast_assign next : target_list) {
			post_process_item(next);
		}
		target_list.clear();
	}

	protected LinkedList<ast_assign> target_list = new LinkedList<ast_assign>();

	protected final boolean post_process_item(ast_assign a) {
		// temporary node
		ast_nop NOP = new ast_temp_marker();

		// ----------------------------------------------------
		// (Replace s with s', but with correct IR management)
		// 1. add nop after s.
		// 2. rip-off s
		// 3. create s' out of s
		// 4. add s' after nop
		// 5. rip off nop.
		// ----------------------------------------------------
		gm_transform_helper.gm_add_sent_after(a, NOP);
		// ast_sentblock *SB = (ast_sentblock*) a->get_parent();
		gm_transform_helper.gm_ripoff_sent(a);
		ast_foreach fe = create_surrounding_fe(a);
		gm_transform_helper.gm_add_sent_after(NOP, fe);
		gm_transform_helper.gm_ripoff_sent(NOP);

		if (NOP != null)
			NOP.dispose(); // no need after this

		// --------------------------------------------------------------------
		// 1. replace lhs driver with iterator
		// 2. traverse rhs, replace graph reference to new iterator reference
		// --------------------------------------------------------------------
		ast_field lhs = a.get_lhs_field();
		ast_id old = lhs.get_first();
		ast_id iter = fe.get_iterator().copy(true);
		iter.set_line(old.get_line());
		iter.set_col(old.get_col());
		lhs.set_first(iter);

		// 2.
		old_driver_sym = old.getSymInfo();
		new_driver = iter;
		set_for_expr(true);
		ast_expr rhs = a.get_rhs();
		rhs.traverse_pre(this);
		set_for_expr(false);

		if (old != null)
			old.dispose();

		return true;
	}

	/** traverse expr */
	@Override
	public boolean apply(ast_expr e) {
		if (e.is_id()) {
			ast_id old = e.get_id();
			// replace G.A -> iter.A
			if ((old.getSymInfo() == old_driver_sym) && ((e.get_type_summary() == GMTYPE_NODE) || (e.get_type_summary() == GMTYPE_EDGE))) {
				old.setSymInfo(new_driver.getSymInfo());
				e.set_type_summary(new_driver.getTypeSummary());
			}
		}
		if (e.is_field()) {
			ast_field f = e.get_field();
			ast_id old = f.get_first();
			// replace G.A -> iter.A
			if (old.getSymInfo() == old_driver_sym) {
				ast_id iter = new_driver.copy(true);
				iter.set_line(old.get_line());
				iter.set_col(old.get_col());
				f.set_first(iter);
				if (old != null)
					old.dispose();
			}
		} else if (e.is_builtin()) {
			ast_expr_builtin e2 = (ast_expr_builtin) e;
			ast_id old = e2.get_driver();
			if ((old != null) && (old.getSymInfo() == old_driver_sym)) {

				// If the builtin-op is for graph do not replace!
				if (old.getTypeSummary() != e2.get_builtin_def().get_source_type_summary()) {
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

	/**
	 * syntax sugar elimination (after type resolution)
	 * --------------------------------------------------- Group assignment ->
	 * foreach e.g.> G.A = G.B + 1; => Foreach(_t:G.Nodes) _t.A = _t.B + 1;
	 */
	private static ast_foreach create_surrounding_fe(ast_assign a) {
		ast_field lhs = a.get_lhs_field(); // G.A
		ast_id first = lhs.get_first();
		ast_id second = lhs.get_second();

		// iterator : temp
		// source : graph
		// iter-type all nodes or all edges
		// body : assignment statement
		// const char* temp_name =
		// TEMP_GEN.getTempName("t"); // should I use first->get_orgname())?
		String temp_name = gm_main.FE.voca_temp_name("t");
		ast_id it = ast_id.new_id(temp_name, first.get_line(), first.get_col());
		ast_id src = first.copy(true);
		src.set_line(first.get_line());
		src.set_col(first.get_col());
		GMTYPE_T iter;
		if (second.getTypeSummary().is_node_property_type())
			iter = GMTYPE_NODEITER_ALL;
		else if (second.getTypeSummary().is_edge_property_type())
			iter = GMTYPE_EDGEITER_ALL;
		else {
			assert false;
			throw new AssertionError();
		}

		ast_foreach fe_new = gm_new_sents_after_tc.gm_new_foreach_after_tc(it, src, a, iter);

		return fe_new;
	}

}