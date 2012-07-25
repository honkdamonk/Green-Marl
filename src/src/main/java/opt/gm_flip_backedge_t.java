package opt;

import inc.GMTYPE_T;
import inc.gm_assignment_t;

import java.util.HashMap;
import java.util.LinkedList;

import ast.AST_NODE_TYPE;
import ast.ast_assign;
import ast.ast_bfs;
import ast.ast_expr;
import ast.ast_expr_reduce;
import ast.ast_field;
import ast.ast_foreach;
import ast.ast_id;
import ast.ast_node;
import ast.ast_sent;
import ast.ast_sentblock;

import common.GlobalMembersGm_new_sents_after_tc;
import common.GlobalMembersGm_resolve_nc;
import common.GlobalMembersGm_transform_helper;
import common.gm_apply;

import frontend.GlobalMembersGm_rw_analysis;
import frontend.gm_range_type_t;
import frontend.gm_rwinfo;
import frontend.gm_rwinfo_sets;
import frontend.gm_symtab_entry;

//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define TO_STR(X) #X
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define DEF_STRING(X) static const char *X = "X"
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP(CLASS, DESC) class CLASS : public gm_compile_step { private: CLASS() {set_description(DESC);}public: virtual void process(ast_procdef*p); virtual gm_compile_step* get_instance(){return new CLASS();} static gm_compile_step* get_factory(){return new CLASS();} };
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP_FACTORY(CLASS) CLASS::get_factory()

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
	public gm_flip_backedge_t() {
		this.set_for_sent(true);
	}

	// C++ TO JAVA CONVERTER NOTE: Access declarations are not available in
	// Java:
	// ;

	@Override
	public boolean apply(ast_sent sent) {
		if (sent.get_nodetype() != AST_NODE_TYPE.AST_BFS)
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
		HashMap<gm_symtab_entry, Boolean> check_init = new HashMap<gm_symtab_entry, Boolean>();

		// --------------------------------------
		// check if bodies are all assignments
		// --------------------------------------
		java.util.Iterator<ast_sent> I;
		for (I = S.iterator(); I.hasNext();) {
			ast_sent s = I.next();
			if (s.get_nodetype() != AST_NODE_TYPE.AST_ASSIGN)
				return true;

			ast_assign a = (ast_assign) (I.next());

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
			if (r.get_nodetype() != AST_NODE_TYPE.AST_EXPR_RDC)
				return true;
			ast_expr_reduce D = (ast_expr_reduce) r;
			GMTYPE_T iter_type = D.get_iter_type();
			if (iter_type != GMTYPE_T.GMTYPE_NODEITER_UP_NBRS)
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
		if (up.get_nodetype() != AST_NODE_TYPE.AST_SENTBLOCK)
			return true;
		ast_sentblock sb = (ast_sentblock) up;

		for (ast_sent s : sb.get_sents()) {
			gm_rwinfo_sets RW = GlobalMembersGm_rw_analysis.gm_get_rwinfo_sets(s);
			// C++ TO JAVA CONVERTER WARNING: The following line was determined
			// to be a copy constructor call - this should be verified and a
			// copy constructor should be created if it does not yet exist:
			// ORIGINAL LINE: HashMap<gm_symtab_entry*,
			// LinkedList<gm_rwinfo*>*>& W = RW->write_set;
			HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> W = new HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>>(RW.write_set);

			// check if this sentence initializes any target
			java.util.Iterator<gm_symtab_entry> T;
			for (T = targets.iterator(); T.hasNext();) {
				gm_symtab_entry t = T.next();
				if (!W.containsKey(t))
					continue;
				LinkedList<gm_rwinfo> lst = W.get(t);
				java.util.Iterator<gm_rwinfo> J;
				for (J = lst.iterator(); J.hasNext();) {
					gm_rwinfo info = J.next();
					if (info.driver != null) {
						if (info.driver != root) // other than thru root, init
													// is being broken.
						{
							check_init.put(t, false);
						}
					} else {
						if (info.access_range != gm_range_type_t.GM_RANGE_LINEAR) {
							check_init.put(t, false);
						} else {
							check_init.put(t, true);
						}
					}
				}
			}
		}

		// check if every symbol has been initialized
		java.util.Iterator<gm_symtab_entry> T;
		for (T = targets.iterator(); T.hasNext();) {
			gm_symtab_entry t = T.next();
			if (!check_init.containsKey(t))
				return true;
			if (check_init.get(t) == false)
				return true;
		}

		// now put every assignment in the candiate statement
		for (I = S.iterator(); I.hasNext();) {
			ast_assign a = (ast_assign) (I.next());
			// add to target
			_cands.addLast(a);
			_tops.addLast(body);
		}

		return true;
	}

	public final boolean post_process() // return true if something changed
	{
		if (_cands.size() > 0) {
			boolean b = false;
			java.util.Iterator<ast_sentblock> P;
			java.util.Iterator<ast_assign> A;
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
		assert p.get_parent().get_nodetype() == AST_NODE_TYPE.AST_BFS;

		ast_bfs bfs = (ast_bfs) p.get_parent();

		assert !a.is_target_scalar();
		assert a.get_rhs().get_nodetype() == AST_NODE_TYPE.AST_EXPR_RDC;
		ast_field old_lhs = a.get_lhs_field();
		ast_expr_reduce old_rhs = (ast_expr_reduce) a.get_rhs();

		ast_id old_iter = old_rhs.get_iterator();
		assert old_iter.getTypeSummary() == GMTYPE_T.GMTYPE_NODEITER_UP_NBRS;

		// [TODO] considering filters in original RHS.
		assert old_rhs.get_filter() == null;

		// ------------------------------------
		// creating foreach statement
		// ------------------------------------
		ast_sentblock foreach_body = ast_sentblock.new_sentblock(); // body of
																	// foreach
		ast_id new_iter = old_iter.copy(); // same name, nullify symtab entry
		ast_id new_source = old_rhs.get_source().copy(true); // same symtab
		GMTYPE_T new_iter_type = GMTYPE_T.GMTYPE_NODEITER_DOWN_NBRS;

		// new_iter has a valid symtab entry, after foreach creating.
		// foreach_body has correct symtab hierachy
		ast_foreach fe_new = GlobalMembersGm_new_sents_after_tc.gm_new_foreach_after_tc(new_iter, new_source, foreach_body, new_iter_type);

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
		GlobalMembersGm_resolve_nc.gm_replace_symbol_entry(old_iter.getSymInfo(), bfs.get_iterator().getSymInfo(), new_rhs);
		old_rhs.set_body(null); // prevent new_rhs being deleted with old
								// assignment.
		new_rhs.set_up_op(null);

		ast_assign new_assign = ast_assign.new_assign_field(new_lhs, new_rhs, gm_assignment_t.GMASSIGN_REDUCE, bfs.get_iterator().copy(true),
				old_rhs.get_reduce_type());

		GlobalMembersGm_transform_helper.gm_insert_sent_begin_of_sb(foreach_body, new_assign);

		// now put new foreach in place of old assignment.
		// rip-off and delete old assignment
		GlobalMembersGm_transform_helper.gm_add_sent_before(a, fe_new);
		GlobalMembersGm_transform_helper.gm_ripoff_sent(a, GlobalMembersGm_transform_helper.GM_NOFIX_SYMTAB); // no
																												// need
																												// to
																												// fix
																												// symtab
																												// for
																												// a
																												// --
																												// it
																												// will
																												// be
																												// deleted.
		if (a != null)
			a.dispose();
	}

	private LinkedList<ast_sentblock> _tops = new LinkedList<ast_sentblock>();
	private LinkedList<ast_assign> _cands = new LinkedList<ast_assign>();
}
// bool gm_independent_optimize::do_flip_edges(ast_procdef* p)

