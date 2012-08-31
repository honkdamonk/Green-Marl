package frontend;

import java.util.LinkedList;

import ast.AST_NODE_TYPE;
import ast.ast_assign;
import ast.ast_bfs;
import ast.ast_foreach;
import ast.ast_id;
import ast.ast_node;
import ast.ast_sent;

import common.GM_ERRORS_AND_WARNINGS;
import common.gm_error;
import common.gm_transform_helper;
import common.gm_apply;

public class find_hpb_t extends gm_apply {
	
	private LinkedList<ast_assign> targets = new LinkedList<ast_assign>();
	private LinkedList<find_bound_t> scope = new LinkedList<find_bound_t>();
	private boolean opt_seq_bound = false;

	// ------------------------
	// make a big table
	// each symbol -> depth
	// ------------------------
	public find_hpb_t() {
		find_bound_t curr_T = new find_bound_t();
		curr_T.v_scope = null;
		curr_T.f_scope = null;
		curr_T.is_par = false;
		curr_T.is_boundary = false;
		curr_T.iter = null;

		scope.addLast(curr_T);
	}

	public final void set_opt_seq_bound(boolean b) {
		opt_seq_bound = b;
	}

	public final void begin_context(ast_node t) {

		find_bound_t curr_T = new find_bound_t();

		curr_T.v_scope = t.get_symtab_var();
		curr_T.f_scope = t.get_symtab_field();
		curr_T.is_par = false;
		curr_T.is_boundary = false;
		curr_T.iter = null;

		if (t.get_nodetype() == AST_NODE_TYPE.AST_FOREACH) {
			ast_foreach fe = (ast_foreach) t;
			curr_T.is_boundary = true;
			curr_T.iter = fe.get_iterator().getSymInfo();
			if (fe.is_parallel()) {
				curr_T.is_par = true;
			}
		} else if (t.get_nodetype() == AST_NODE_TYPE.AST_BFS) {
			ast_bfs fe = (ast_bfs) t;
			curr_T.is_boundary = true;
			curr_T.iter = fe.get_iterator().getSymInfo();
			if (fe.is_parallel()) {
				curr_T.is_par = true;
			}
		} else {
			// do nithing
		}
		scope.addLast(curr_T);
	}

	public final void end_context(ast_node t) {
		// curr_T = scope.back();
		scope.removeLast();
	}

	public final gm_symtab_entry find_closest_any_boundary_iterator() // can be
																		// a
																		// sequence
	{
		for (int i = scope.size() - 1; i >= 0; i--) {
			find_bound_t T = scope.get(i);
			if (T.is_boundary)
				return T.iter;
		}
		return null;
	}

	// -----------------------------------------------------------
	// find highest parallel boundary after sym is defined
	// -----------------------------------------------------------
	public final gm_symtab_entry find_highest_parallel_boundary_iterator(gm_symtab_entry entry, boolean is_prop) {
		boolean def_found = false;
		// first find symbol def
		for (find_bound_t I : scope) {
			if (!def_found) {
				gm_symtab scope_to_look = (is_prop) ? I.f_scope : I.v_scope;
				if (scope_to_look == null)
					continue;
				if (scope_to_look.is_entry_in_the_tab(entry)) {
					def_found = true;
				} else {
					continue;
				}
			}

			assert def_found;
			// find parallel boundary
			if (I.is_par && I.is_boundary)
				return I.iter;
		}

		// no parallel boundary!
		if (!def_found) // argument or current boundary)
		{
			// okay
		}
		return null;
	}

	// -----------------------------------------------------------
	// find first parallel boundary after current bound
	// -----------------------------------------------------------
	public final gm_symtab_entry find_tighter_bound(gm_symtab_entry curr_bound) {
		boolean def_found = false;

		// first find current bound
		for (find_bound_t I : scope) {
			if (!def_found) {
				if (I.iter == curr_bound) {
					def_found = true;
					if (I.is_par && I.is_boundary)
						return I.iter;
					else
						continue;
				} else {
					continue;
				}
			} else {
				// find parallel boundary
				if (I.is_par && I.is_boundary)
					return I.iter;
			}
		}

		assert def_found == true;
		return null;
	}

	@Override
	public boolean apply(ast_sent s) {
		if (s.get_nodetype() != AST_NODE_TYPE.AST_ASSIGN)
			return true;
		ast_assign a = (ast_assign) s;

		if (a.is_defer_assign()) {
			if (a.get_bound() == null) {
				gm_symtab_entry bound = find_closest_any_boundary_iterator();
				if (bound == null) {
					gm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_UNBOUND_REDUCE, a.get_line(), a.get_col());
					return false;
				}

				a.set_bound(bound.getId().copy(true));
			}
		} else if (a.is_reduce_assign()) {
			// null bound
			// find higest parallel region
			if (a.get_bound() == null) {
				gm_symtab_entry target; // target or driver symbol
				gm_symtab_entry new_iter;
				if (a.is_target_scalar()) {
					target = a.get_lhs_scala().getSymInfo();
					new_iter = find_highest_parallel_boundary_iterator(target, false);
				} else {
					target = a.get_lhs_field().get_second().getSymInfo();
					new_iter = find_highest_parallel_boundary_iterator(target, true);
				}

				// no such iterator
				if (new_iter == null) {
					// gm_make_normal_assign(a);
					targets.addLast(a);
				} else {
					a.set_bound(new_iter.getId().copy(true));
				}
			} else if (opt_seq_bound) {
				// check if bound is sequential
				ast_id old_bound = a.get_bound();
				gm_symtab_entry new_bound = find_tighter_bound(old_bound.getSymInfo());
				if (new_bound == null) {
					// gm_make_normal_assign(a);
					targets.addLast(a);
				} else if (new_bound != old_bound.getSymInfo()) {
					a.set_bound(new_bound.getId().copy(true));
					if (old_bound != null)
						old_bound.dispose();
				}
			}
		}

		return true;
	}

	public final void post_process() {
		for (ast_assign a : targets) {
			gm_transform_helper.gm_make_it_belong_to_sentblock(a);
			FrontendGlobal.gm_make_normal_assign(a);
		}
	}

}