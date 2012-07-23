package frontend;

import ast.AST_NODE_TYPE;
import ast.ast_assign;
import ast.ast_bfs;
import ast.ast_foreach;
import ast.ast_id;
import ast.ast_node;
import ast.ast_sent;

import common.gm_apply;

//-----------------------------------------------------------------
// Find highest parallel bound
//  - The highest parallel scope below where the symbol is defined.
//    (from the current subtree where the reduce operation is defined)
//
// [Example]
// { Int a;
//   For(x: ...) {
//     Foreach(y: x.Nbrs) {
//       Foreach(z: y.Nbrs) {
//          a++ [@ y];    // <- Most of the time, one should be bound to HPB
// } } } }
//
//
// // This is the only exception
//
// { N_P<Int>(G) A,B,C;
//   Foreach(x: G.Nodes) {  // <- HPB
//      Foreach(y: x.Nbrs)  
//          x.A += y.C @ y; // <- maynot bound to HPB. (but writing to driver HPB)
//      x.B = x.A + 1;
// } }
//-----------------------------------------------------------------

public class find_hpb_t1 extends gm_apply {
	// ------------------------
	// make a big table
	// each symbol -> depth
	// ------------------------
	public find_hpb_t1() {
		current_depth = 0;
	}

	public final void begin_context(ast_node t) {
		if (t.get_nodetype() == AST_NODE_TYPE.AST_FOREACH) {
			ast_foreach fe = (ast_foreach) t;
			if (fe.is_parallel()) {
				current_depth++;
			}
			para_iter_map.put(fe.get_iterator().getSymInfo(), fe.is_parallel());
		} else if (t.get_nodetype() == AST_NODE_TYPE.AST_BFS) {
			ast_bfs fe = (ast_bfs) t;
			if (fe.is_parallel())
				current_depth++;
			para_iter_map.put(fe.get_iterator().getSymInfo(), fe.is_parallel());
		}
	}

	public final void end_context(ast_node t) {
		if (t.get_nodetype() == AST_NODE_TYPE.AST_FOREACH) {
			ast_foreach fe = (ast_foreach) t;
			if (fe.is_parallel())
				current_depth--;
		} else if (t.get_nodetype() == AST_NODE_TYPE.AST_BFS) {
			ast_bfs fe = (ast_bfs) t;
			if (fe.is_parallel())
				current_depth--;
		}
	}

	// phase 1: create depth_map
	@Override
	public boolean apply(gm_symtab_entry e, int symtab_type) {
		depth_map.put(e, current_depth);
		return true;
	}

	// ----------------------------------------------------------------
	// phase 2: for every reduction assignment, find HPB.
	// If HPB is NULL -> change it to normal assign
	// If current bound is NULL or higher than HPB -> lower it to HPB.
	// If current bound is lower than HPB,
	// If parallel -> leave it (can be an error or not)
	// If sequential-> fix it silently (or leave it to be error).
	// ----------------------------------------------------------------
	@Override
	public boolean apply(ast_sent s) {
		if (s.get_nodetype() != AST_NODE_TYPE.AST_ASSIGN)
			return true;
		ast_assign a = (ast_assign) s;

		if (!a.is_reduce_assign())
			return true;

		gm_symtab_entry HPB = find_highest_parallel_bound_from(a);
		System.out.printf("finding_bound_for:%p\n", a);

		if (HPB == null) {
			GlobalMembersGm_fixup_bound_symbol.gm_make_normal_assign(a);
		} else if (a.get_bound() == null) {
			assert HPB.getId() != null;
			ast_id new_bound = HPB.getId().copy(true);
			a.set_bound(new_bound);
		} else if (a.get_bound().getSymInfo() == HPB) {
			return true;
		} else {
			assert a.get_bound() != null;
			assert a.get_bound().getSymInfo() != null;
			assert HPB.getId() != null;

			int HPB_depth = depth_map.get(HPB);
			int curr_bound_depth = depth_map.get(a.get_bound().getSymInfo());
			// smaller number means higher scope
			// curr bound level is lower
			if (HPB_depth < curr_bound_depth) {
				if (para_iter_map.get(a.get_bound().getSymInfo())) // is
																	// parallel
				{
					// do nothing: there is a special case.
				} else {
					// fixing error
					ast_id old_bound = a.get_bound();
					if (old_bound != null)
						old_bound.dispose();
					ast_id new_bound = HPB.getId().copy(true);
					a.set_bound(new_bound);
				}
			} else {
				// fixing error
				ast_id old_bound = a.get_bound();
				if (old_bound != null)
					old_bound.dispose();
				ast_id new_bound = HPB.getId().copy(true);
				a.set_bound(new_bound);
			}
		}
		return true;
	}

	public final gm_symtab_entry find_highest_parallel_bound_from(ast_assign a) {
		gm_symtab_entry dest;
		if (a.is_target_scalar()) {
			ast_id i = a.get_lhs_scala();
			dest = i.getSymInfo();
		} else {
			ast_id i = a.get_lhs_field().get_second();
			dest = i.getSymInfo();
		}
		assert dest != null;

		// smaller number means higher scope
		int dest_depth = depth_map.get(dest);
		ast_node n = a.get_parent();

		gm_symtab_entry HPB = null;

		while (n != null) {
			// printf("B3 %s\n",
			// gm_get_nodetype_string(n->get_nodetype()));fflush(stdout);
			if (n.get_nodetype() == AST_NODE_TYPE.AST_FOREACH) {
				ast_foreach fe = (ast_foreach) n;
				assert fe.get_iterator().getSymInfo() != null;
				int iter_depth = depth_map.get(fe.get_iterator().getSymInfo());

				if (iter_depth <= dest_depth)
					break;
				if (fe.is_parallel())
					HPB = fe.get_iterator().getSymInfo();
			} else if (n.get_nodetype() == AST_NODE_TYPE.AST_BFS) {
				ast_bfs fe = (ast_bfs) n;
				assert fe.get_iterator().getSymInfo() != null;
				int iter_depth = depth_map.get(fe.get_iterator().getSymInfo());

				if (iter_depth <= dest_depth)
					break;
				if (fe.is_parallel())
					HPB = fe.get_iterator().getSymInfo();
			}

			n = n.get_parent();
		}

		return HPB;
	}

	private int current_depth;
	private java.util.HashMap<gm_symtab_entry, Integer> depth_map = new java.util.HashMap<gm_symtab_entry, Integer>(); // map
																														// of
																														// symbol
																														// &
																														// depth
	private java.util.HashMap<gm_symtab_entry, Boolean> para_iter_map = new java.util.HashMap<gm_symtab_entry, Boolean>(); // map
																															// of
																															// iterator
																															// symbol
																															// &
																															// is
																															// parallel
}