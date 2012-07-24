package opt;

import ast.AST_NODE_TYPE;

import ast.ast_assign;
import ast.ast_bfs;
import ast.ast_foreach;
import ast.ast_node;
import ast.ast_sent;
import frontend.GlobalMembersGm_fixup_bound_symbol;
import frontend.gm_symtab_entry;
import inc.GMTYPE_T;

import common.GlobalMembersGm_transform_helper;
import common.gm_apply;

import static inc.GMTYPE_T.GMTYPE_NODEITER_ALL;
import static inc.GMTYPE_T.GMTYPE_EDGEITER_ALL;
import static inc.GMTYPE_T.GMTYPE_NODEITER_SET;
import static inc.GMTYPE_T.GMTYPE_EDGEITER_SET;
import static inc.GMTYPE_T.GMTYPE_NODEITER_ORDER;
import static inc.GMTYPE_T.GMTYPE_EDGEITER_ORDER;

// optimize reductions if every assignment is reached linearly
//
// condition
//    no other parallel loops in between
public class gm_reduce_opt_linear_t extends gm_apply {
	public gm_reduce_opt_linear_t() {
		set_for_sent(true);
		under_rev_bfs = false;
	}

	public final boolean apply(ast_sent s) {
		if (s.get_nodetype() != AST_NODE_TYPE.AST_ASSIGN)
			return true;
		ast_assign a = (ast_assign) s;
		if (!a.is_reduce_assign())
			return true;
		if (a.is_target_scalar())
			return true;

		assert a.get_bound() != null;
		gm_symtab_entry bound = a.get_bound().getSymInfo();
		gm_symtab_entry target = a.get_lhs_field().get_second().getSymInfo();
		int is_rev_bfs = under_rev_bfs ? 1 : 0;

		triple_t key = new triple_t();
		key.bound = bound;
		key.target = target;
		key.is_rev_bfs = is_rev_bfs;

		if (!candidates.containsKey(key)) {
			java.util.LinkedList<ast_assign> L = new java.util.LinkedList<ast_assign>();
			candidates.put(key, L); // copy
		}
		candidates.get(key).push_back(a);

		// todo distinguish fw and bw
		return true;
	}

	public final void post_process() {

		java.util.Iterator<triple_t, java.util.LinkedList<ast_assign>, triple_comp_t> I;
		for (I = candidates.iterator(); I.hasNext();) {
			gm_symtab_entry target = I.next().getKey().target;
			gm_symtab_entry bound = I.next().getKey().bound;
			java.util.LinkedList<ast_assign> L = I.next().getValue();

			// check if every write is linear
			if (check_all_okay(L, bound)) {
				// add list to targets
				targets.splice(targets.end(), L);
			}
		}

		post_process2();
	}

	public final void post_process2() {
		java.util.Iterator<ast_assign> I;
		for (I = targets.iterator(); I.hasNext();) {
			ast_assign a = I.next();
			assert a.is_reduce_assign();
			GlobalMembersGm_transform_helper.gm_make_it_belong_to_sentblock(a);
			GlobalMembersGm_fixup_bound_symbol.gm_make_normal_assign(a);
		}
	}

	@Override
	public boolean begin_traverse_reverse(ast_bfs bfs) {
		under_rev_bfs = true;
		return true;
	}

	@Override
	public boolean end_traverse_reverse(ast_bfs bfs) {
		under_rev_bfs = false;
		return true;
	}

	private boolean check_all_okay(java.util.LinkedList<ast_assign> L, gm_symtab_entry bound) {
		java.util.Iterator<ast_assign> I;
		for (I = L.iterator(); I.hasNext();) {
			ast_assign a = I.next();

			if (a.get_lhs_field().get_first().getSymInfo() != bound)
				return false;

			// go up to bound
			ast_node n = a.get_parent();
			while (true) {
				assert n != null;
				if (n.get_nodetype() == AST_NODE_TYPE.AST_FOREACH) {
					ast_foreach fe = (ast_foreach) n;
					if (fe.get_iterator().getSymInfo() != bound) {
						if (!fe.is_sequential())
							return false;
					} else {
						GMTYPE_T iter_type = fe.get_iter_type();
						if ((iter_type == GMTYPE_NODEITER_ALL) || (iter_type == GMTYPE_EDGEITER_ALL) || (iter_type == GMTYPE_NODEITER_SET)
								|| (iter_type == GMTYPE_EDGEITER_SET) || (iter_type == GMTYPE_NODEITER_ORDER) || (iter_type == GMTYPE_EDGEITER_ORDER)) {
							break;
						} else {
							return false;
						}
					}
				}
				if (n.get_nodetype() == AST_NODE_TYPE.AST_BFS) {
					ast_bfs bfs = (ast_bfs) n;
					if ((bfs.get_iterator().getSymInfo() == bound) || (bfs.get_iterator2().getSymInfo() == bound)) {
						break;
					} else if (bfs.is_bfs())
						return false;
				}

				n = n.get_parent();
			}
		}

		return true;
	}

	// map [(target, bound, is_bfs) ==> list of assign]
	// std::map< std::pair<gm_symtab_entry*, gm_symtab_entry*>,
	// std::list<ast_assign*> > candidates;
	private java.util.HashMap<triple_t, java.util.LinkedList<ast_assign>, triple_comp_t> candidates = new java.util.HashMap<triple_t, java.util.LinkedList<ast_assign>, triple_comp_t>();
	private java.util.LinkedList<ast_assign> targets = new java.util.LinkedList<ast_assign>();
	private boolean under_rev_bfs;
}