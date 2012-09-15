package frontend;

import static inc.GM_PROP_USAGE_T.GMUSAGE_INOUT;
import static inc.GM_PROP_USAGE_T.GMUSAGE_INVALID;
import static inc.GM_PROP_USAGE_T.GMUSAGE_OUT;
import inc.GMTYPE_T;

import java.util.HashSet;
import java.util.LinkedList;

import ast.ast_node_type;
import ast.ast_assign;
import ast.ast_expr;
import ast.ast_field;
import ast.ast_foreach;
import ast.ast_node;
import ast.ast_sent;

import common.gm_apply;

/**
 * Examination about how each property is used inside a procedure : inout vs
 * output If every element is initialized first, and then read it is being used
 * as output Otherwise, it is inout. (Do we need this at all?)
 */
public class gm_check_property_usage_t extends gm_apply {

	private final HashSet<gm_symtab_entry> under_current_linear_update = new HashSet<gm_symtab_entry>();
	private gm_symtab_entry topmost_iterator = null;
	private final LinkedList<ast_sent> condition_stack = new LinkedList<ast_sent>();
	private final LinkedList<ast_sent> random_iter_stack = new LinkedList<ast_sent>();

	public gm_check_property_usage_t() {
		set_separate_post_apply(true);
		set_for_sent(true);
		set_for_expr(true);
	}

	@Override
	public boolean apply(ast_sent s) {
		if (s.get_nodetype() == ast_node_type.AST_FOREACH) {
			ast_foreach fe = (ast_foreach) s;
			if (is_under_condition() || is_under_random_iterator() || (topmost_iterator != null)) {
				random_iter_stack.addLast(s);
			} // TODO remove else if?
			else if (fe.get_iter_type() == GMTYPE_T.GMTYPE_NODEITER_ALL) {
				topmost_iterator = fe.get_iterator().getSymInfo();
			} else {
				topmost_iterator = fe.get_iterator().getSymInfo();
			}
		} else if (s.get_nodetype() == ast_node_type.AST_BFS) {
			random_iter_stack.addLast(s);
		} else if ((s.get_nodetype() == ast_node_type.AST_WHILE) || (s.get_nodetype() == ast_node_type.AST_IF)) {
			condition_stack.addLast(s);
		} else if (s.get_nodetype() == ast_node_type.AST_ASSIGN) {
			ast_assign a = (ast_assign) s;
			if (!a.is_target_scalar() && !a.is_target_map_entry()) {
				ast_field f = a.get_lhs_field();
				if (a.is_reduce_assign()) // this is read & write
				{
					property_is_read(f.get_second().getSymInfo(), f.get_first().getSymInfo(), true);
					java.util.LinkedList<ast_node> L = a.get_lhs_list();
					for (ast_node n : L) {
						assert n.get_nodetype() == ast_node_type.AST_FIELD;
						ast_field f2 = (ast_field) n;
						property_is_read(f2.get_second().getSymInfo(), f2.get_first().getSymInfo(), true);
					}
				} // normal assignment
				else {
					property_is_written(f.get_second().getSymInfo(), f.get_first().getSymInfo());
				}
			}
		} else if (s.get_nodetype() == ast_node_type.AST_CALL) {
			// [todo]
		}

		return true;
	}

	@Override
	public boolean apply2(ast_sent s) {
		if (!condition_stack.isEmpty() && s == condition_stack.getLast()) {
			condition_stack.removeLast();
		} else if (!random_iter_stack.isEmpty() && s == random_iter_stack.getLast()) {
			random_iter_stack.removeLast();
		} else {
			if (s.get_nodetype() == ast_node_type.AST_FOREACH) {
				ast_foreach fe = (ast_foreach) s;
				if (fe.get_iterator().getSymInfo() == topmost_iterator) {
					topmost_iterator = null;
					// finish written
					for (gm_symtab_entry prop : under_current_linear_update) {
						if (prop.find_info_int(gm_frontend.GMUSAGE_PROPERTY) != GMUSAGE_INVALID.getValue())
							continue;

						prop.add_info_int(gm_frontend.GMUSAGE_PROPERTY, GMUSAGE_OUT.getValue());
					}

					under_current_linear_update.clear();
				}
			}
		}

		return true;
	}

	@Override
	public boolean apply(ast_expr e) {
		if (e.is_field()) {
			gm_symtab_entry prop = e.get_field().get_first().getSymInfo();
			gm_symtab_entry driver = e.get_field().get_first().getSymInfo();
			property_is_read(prop, driver, false);
		}

		return true;
	}

	public final void property_is_written(gm_symtab_entry prop, gm_symtab_entry driver) {
		if (prop.find_info_int(gm_frontend.GMUSAGE_PROPERTY) != GMUSAGE_INVALID.getValue())
			return;

		if (!is_under_condition() && !is_under_random_iterator() && (driver == topmost_iterator)) {
			under_current_linear_update.add(prop);
		}
	}

	public final void property_is_read(gm_symtab_entry prop, gm_symtab_entry driver, boolean is_reduce) {
		if (prop.find_info_int(gm_frontend.GMUSAGE_PROPERTY) != GMUSAGE_INVALID.getValue())
			return;

		if (is_reduce) {
			prop.add_info_int(gm_frontend.GMUSAGE_PROPERTY, GMUSAGE_INOUT.getValue());
		}

		if (driver == topmost_iterator) {
			if (is_under_random_iterator()) {
				prop.add_info_int(gm_frontend.GMUSAGE_PROPERTY, GMUSAGE_INOUT.getValue());
			}
		}
	}

	public final boolean is_under_condition() {
		return condition_stack.size() > 0;
	}

	public final boolean is_under_random_iterator() {
		return random_iter_stack.size() > 0;
	}

}
// ----------------------------------------------------------
// Check how property argument is used only
// - input ; read-only
// - output ; write only or (write whole -> read)
// - inout ; read and write
// ----------------------------------------------------------
