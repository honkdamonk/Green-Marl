package opt;

import inc.gm_assignment;
import inc.gm_type;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import ast.ast_assign;
import ast.ast_bfs;
import ast.ast_expr;
import ast.ast_expr_reduce;
import ast.ast_field;
import ast.ast_foreach;
import ast.ast_id;
import ast.ast_node;
import ast.ast_node_type;
import ast.ast_sent;
import ast.ast_sentblock;
import ast.gm_rwinfo_list;
import ast.gm_rwinfo_map;

import common.gm_apply;
import common.gm_new_sents_after_tc;
import common.gm_resolve_nc;
import common.gm_transform_helper;

import frontend.gm_range_type;
import frontend.gm_rw_analysis;
import frontend.gm_rwinfo;
import frontend.gm_rwinfo_sets;
import frontend.gm_symtab_entry;

//-------------------------------------------------------------------
// Currently, this optimization is too specialized.
// More generalized solution will be available soon with GPS project
//--------------------------------------------------------------------
// flip edges 
//   G.X = 0;
//   s.X = ...
//   InBFS(t: G.Nodes; s)
//     t.X = Sum (u: t.UpNbrs) {...}
// -->
//   G.X = 0;
//   s.X = ...
//   InBFS(t: G.Nodes)
//     Foreach(u: t.DownNbrs) 
//        u.X += ... @ t
//--------------------------------------------------------------------
public class gm_flip_backedge_t extends gm_apply {

	private final LinkedList<ast_sentblock> _tops = new LinkedList<ast_sentblock>();
	private final LinkedList<ast_assign> _cands = new LinkedList<ast_assign>();

	public gm_flip_backedge_t() {
		set_for_sent(true);
	}

	@Override
	public boolean apply(ast_sent sent) {
		if (sent.get_nodetype() != ast_node_type.AST_BFS)
			return true;
		ast_bfs bfs = (ast_bfs) sent;

		// should be no filter or navigator
		if (bfs.get_f_filter() != null)
			return true;
		if (bfs.get_b_filter() != null)
			return true;
		if (bfs.get_navigator() != null)
			return true;

		gm_symtab_entry root = bfs.get_root().getSymInfo();
		gm_symtab_entry current_bfs_iter = bfs.get_iterator().getSymInfo();
		ast_sentblock body = bfs.get_fbody();
		LinkedList<ast_sent> S = body.get_sents();

		LinkedList<gm_symtab_entry> targets = new LinkedList<gm_symtab_entry>();
		Map<gm_symtab_entry, Boolean> check_init = new TreeMap<gm_symtab_entry, Boolean>();

		// --------------------------------------
		// check if bodies are all assignments
		// --------------------------------------
		for (ast_sent s : S) {
			if (s.get_nodetype() != ast_node_type.AST_ASSIGN)
				return true;

			ast_assign a = (ast_assign) s;

			if (a.is_defer_assign())
				return true;
			if (a.is_reduce_assign())
				return true;
			if (a.is_target_scalar())
				return true;

			ast_field f = a.get_lhs_field();
			if (f.get_first().getSymInfo() != current_bfs_iter)
				return true;

			ast_expr r = a.get_rhs();
			if (r.get_nodetype() != ast_node_type.AST_EXPR_RDC)
				return true;
			ast_expr_reduce D = (ast_expr_reduce) r;
			gm_type iter_type = D.get_iter_type();
			if (iter_type != gm_type.GMTYPE_NODEITER_UP_NBRS)
				return true;
			if (D.get_filter() != null) // todo considering filters
				return true;

			targets.addLast(f.get_second().getSymInfo());
			// todo check if D contains any other neted sum.
		}

		// --------------------------------------
		// check initializations are preceeding BFS
		// --------------------------------------
		ast_node up = bfs.get_parent();
		assert up != null;
		if (up.get_nodetype() != ast_node_type.AST_SENTBLOCK)
			return true;
		ast_sentblock sb = (ast_sentblock) up;

		for (ast_sent s : sb.get_sents()) {
			gm_rwinfo_sets RW = gm_rw_analysis.gm_get_rwinfo_sets(s);
			gm_rwinfo_map W = RW.write_set;

			// check if this sentence initializes any target
			for (gm_symtab_entry t : targets) {
				if (!W.containsKey(t))
					continue;
				gm_rwinfo_list lst = W.get(t);
				for (gm_rwinfo info : lst) {
					if (info.driver != null) {
						if (info.driver != root) // other than thru root, init
													// is being broken.
						{
							check_init.put(t, false);
						}
					} else {
						if (info.access_range != gm_range_type.GM_RANGE_LINEAR) {
							check_init.put(t, false);
						} else {
							check_init.put(t, true);
						}
					}
				}
			}
		}

		// check if every symbol has been initialized
		for (gm_symtab_entry t : targets) {
			if (!check_init.containsKey(t))
				return true;
			if (check_init.get(t) == false)
				return true;
		}

		// now put every assignment in the candiate statement
		for (ast_sent sent1 : S) {
			ast_assign a = (ast_assign) sent1;
			// add to target
			_cands.addLast(a);
			_tops.addLast(body);
		}

		return true;
	}

	/** return true if something changed */
	public final boolean post_process() { 
		if (_cands.size() > 0) {
			Iterator<ast_sentblock> P;
			Iterator<ast_assign> A;
			A = _cands.iterator();
			P = _tops.iterator();
			while (A.hasNext()) {
				ast_sentblock P_element = P.next();
				ast_assign A_element = A.next();
				flip_edges(A_element, P_element);
			}
			_cands.clear();
			_tops.clear();

			return true;
		}
		return false;
	}

	public final void flip_edges(ast_assign a, ast_sentblock p) {
		assert p.get_parent().get_nodetype() == ast_node_type.AST_BFS;

		ast_bfs bfs = (ast_bfs) p.get_parent();

		assert !a.is_target_scalar();
		assert a.get_rhs().get_nodetype() == ast_node_type.AST_EXPR_RDC;
		ast_field old_lhs = a.get_lhs_field();
		ast_expr_reduce old_rhs = (ast_expr_reduce) a.get_rhs();

		ast_id old_iter = old_rhs.get_iterator();
		assert old_iter.getTypeSummary() == gm_type.GMTYPE_NODEITER_UP_NBRS;

		// [TODO] considering filters in original RHS.
		assert old_rhs.get_filter() == null;

		// ------------------------------------
		// creating foreach statement
		// ------------------------------------
		ast_sentblock foreach_body = ast_sentblock.new_sentblock(); // body of
																	// foreach
		ast_id new_iter = old_iter.copy(); // same name, nullify symtab entry
		ast_id new_source = old_rhs.get_source().copy(true); // same symtab
		gm_type new_iter_type = gm_type.GMTYPE_NODEITER_DOWN_NBRS;

		// new_iter has a valid symtab entry, after foreach creating.
		// foreach_body has correct symtab hierachy
		ast_foreach fe_new = gm_new_sents_after_tc.gm_new_foreach_after_tc(new_iter, new_source, foreach_body, new_iter_type);

		// ------------------------------------
		// new assignment and put inside foreach
		// InBFS(t)
		// t.X = Sum (u. t.UpNbrs) {u.Y}
		// ==>
		// InBfs(t)
		// Foreach(u: t. DownNbrs) {
		// u.X += t.Y @ t
		// }
		// ------------------------------------
		// LHS: replace t -> u.
		ast_field new_lhs = ast_field.new_field(new_iter.copy(true), old_lhs.get_second().copy(true));

		// RHS: repalce u -> t.
		ast_expr new_rhs = old_rhs.get_body(); // reuse old expr structure.
		gm_resolve_nc.gm_replace_symbol_entry(old_iter.getSymInfo(), bfs.get_iterator().getSymInfo(), new_rhs);
		// prevent new_rhs being deleted with old assignment.
		old_rhs.set_body(null); 
		new_rhs.set_up_op(null);

		ast_assign new_assign = ast_assign.new_assign_field(new_lhs, new_rhs, gm_assignment.GMASSIGN_REDUCE, bfs.get_iterator().copy(true),
				old_rhs.get_reduce_type());

		gm_transform_helper.gm_insert_sent_begin_of_sb(foreach_body, new_assign);

		// now put new foreach in place of old assignment.
		// rip-off and delete old assignment
		gm_transform_helper.gm_add_sent_before(a, fe_new);
		// no need to fix symtab for a -- it will be deleted.
		gm_transform_helper.gm_ripoff_sent(a, gm_transform_helper.GM_NOFIX_SYMTAB);
	}

}

