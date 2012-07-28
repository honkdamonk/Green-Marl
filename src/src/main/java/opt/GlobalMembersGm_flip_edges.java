package opt;

import tangible.RefObject;
import inc.GMTYPE_T;
import ast.AST_NODE_TYPE;
import ast.ast_assign;
import ast.ast_expr;
import ast.ast_foreach;
import ast.ast_if;
import ast.ast_sent;
import ast.ast_sentblock;
import frontend.gm_symtab;
import frontend.gm_symtab_entry;

public class GlobalMembersGm_flip_edges {

	// --------------------------------------------------------------------
	// flip edges
	// --------------------------------------------------------------------
	// Foreach (n: G.Nodes)
	// if (f(n)) <- optional
	// Foreach(t: n. Nbrs)
	// if (g(t)) <- optional
	// sentence (reduce @ n)
	// --------------------------------------------------------------------
	// Foreach (t: G.Nodes)
	// if (g(t))
	// Foreach(n: t. InNbrs)
	// if (f(n))
	// sentence (reduce @ t)
	// --------------------------------------------------------------------
	// Steps
	// 1. Find candiates
	// - avoid reverse edges (for cpp) // Inner loop use InNbr
	// - avoid pull syntax (for gps) // sentence is an assign and dest is
	// outer-loop iter
	// 2. Flip each nested foreach
	// --------------------------------------------------------------------

	// { { foo; } } ==> foo
	// { foo; bar; } ==> NULL

	public static ast_sent get_single_destination_sentence(ast_sent s) {
		while (true) {
			if (s.get_nodetype() != AST_NODE_TYPE.AST_SENTBLOCK)
				return s;

			ast_sentblock sb = (ast_sentblock) s;
			if (sb.get_sents().size() != 1) // no single sentence
				return null;
			s = sb.get_sents().getFirst();
		}
	}

	// return true if sym is used inside e
	public static boolean check_if_outer_loop_iterator_used(ast_expr e, gm_symtab_entry sym) {
		check_sym_used T = new check_sym_used(sym);
		e.traverse_pre(T);
		return T.is_used();
	}

	public static boolean capture_pattern(ast_foreach out, RefObject<ast_if> if1_ref, RefObject<ast_foreach> in_ref, RefObject<ast_if> if2_ref,
			RefObject<ast_sent> dest_ref) {
		ast_if if1 = null;
		ast_if if2 = null;
		ast_foreach in = null;

		if (!out.get_iter_type().is_all_graph_node_iter_type())
			return false;

		ast_sent body1;
		body1 = GlobalMembersGm_flip_edges.get_single_destination_sentence(out.get_body());
		if (body1 == null)
			return false;

		if (body1.get_nodetype() == AST_NODE_TYPE.AST_IF) {
			if1 = (ast_if) body1;
			if (if1.get_else() != null)
				return false;
			body1 = GlobalMembersGm_flip_edges.get_single_destination_sentence(if1.get_then());
			if (body1 == null)
				return false;
		}

		if (body1.get_nodetype() != AST_NODE_TYPE.AST_FOREACH)
			return false;
		in = (ast_foreach) body1;

		GMTYPE_T iter2 = in.get_iter_type();
		if ((iter2 != GMTYPE_T.GMTYPE_NODEITER_NBRS) && (iter2 != GMTYPE_T.GMTYPE_NODEITER_IN_NBRS))
			return false;

		if (in.get_source().getSymInfo() != out.get_iterator().getSymInfo())
			return false;

		body1 = GlobalMembersGm_flip_edges.get_single_destination_sentence(in.get_body());
		if (body1 == null)
			return false;
		if (body1.get_nodetype() == AST_NODE_TYPE.AST_IF) {
			if2 = (ast_if) body1;
			if (if2.get_else() != null)
				return false;

			// outer loop iterator must not be used
			if (GlobalMembersGm_flip_edges.check_if_outer_loop_iterator_used(if2.get_cond(), out.get_iterator().getSymInfo()))
				return false;

			body1 = GlobalMembersGm_flip_edges.get_single_destination_sentence(if2.get_then());
			if (body1 == null)
				return false;
		}

		dest_ref.argvalue = body1;
		in_ref.argvalue = in;
		if1_ref.argvalue = if1;
		if2_ref.argvalue = if2;
		return true;
	}

	// Now actually flip the edges
	public static void do_flip_edges(java.util.LinkedList<ast_foreach> target) {
		ast_foreach in;
		ast_if if1;
		ast_if if2;
		ast_sent dest;

		for (ast_foreach out : target) {
			RefObject<ast_foreach> in_ref = new RefObject<ast_foreach>(null);
			RefObject<ast_if> if1_ref = new RefObject<ast_if>(null);
			RefObject<ast_if> if2_ref = new RefObject<ast_if>(null);
			RefObject<ast_sent> dest_ref = new RefObject<ast_sent>(null);
			boolean b = GlobalMembersGm_flip_edges.capture_pattern(out, if1_ref, in_ref, if2_ref, dest_ref);
			in = in_ref.argvalue;
			if1 = if1_ref.argvalue;
			if2 = if2_ref.argvalue;
			dest = dest_ref.argvalue;
			assert b;

			// --------------------------------------------------------
			// now do flipping edges
			// 1. exchange iterators between for loops
			// 2. flip inner loop edge direction
			// 3. exchange if conditions
			// - if2 does not exist
			// - if1 does not exist
			// 4. set new binding symbol
			// --------------------------------------------------------

			// 1) iterator exchange
			gm_symtab_entry iter_out = out.get_iterator().getSymInfo();
			gm_symtab_entry iter_in = in.get_iterator().getSymInfo();
			gm_symtab scope_out = out.get_symtab_var();
			assert scope_out.is_entry_in_the_tab(iter_out);
			gm_symtab scope_in = in.get_symtab_var();
			assert scope_in.is_entry_in_the_tab(iter_in);

			scope_out.remove_entry_in_the_tab(iter_out);
			scope_in.remove_entry_in_the_tab(iter_in);
			scope_in.add_symbol(iter_out);
			scope_out.add_symbol(iter_in);

			out.get_iterator().setSymInfo(iter_in);
			in.get_iterator().setSymInfo(iter_out);

			assert in.get_source().getSymInfo() == iter_out;
			in.get_source().setSymInfo(iter_in);

			// 2) flip inner edge direction
			if (in.get_iter_type() == GMTYPE_T.GMTYPE_NODEITER_NBRS)
				in.set_iter_type(GMTYPE_T.GMTYPE_NODEITER_IN_NBRS);
			else if (in.get_iter_type() == GMTYPE_T.GMTYPE_NODEITER_IN_NBRS)
				in.set_iter_type(GMTYPE_T.GMTYPE_NODEITER_NBRS);
			else {
				assert false;
			}

			iter_in.getType().set_typeid(out.get_iter_type());
			iter_out.getType().set_typeid(in.get_iter_type());

			// 3) exchange if conditions
			if ((if1 == null) && (if2 == null)) {
				// done
			} else if (if1 == null) {
				ast_sent body1 = out.get_body();
				ast_sent body2 = if2.get_then();

				in.set_body(body2);
				body2.set_parent(in);

				if2.set_then(body1);
				body1.set_parent(if2);

				out.set_body(if2);
				if2.set_parent(out);
			}

			else if (if2 == null) {
				ast_sent body1 = if1.get_then();
				ast_sent body2 = in.get_body();

				in.set_body(if1);
				if1.set_parent(in);

				if1.set_then(body2);
				body2.set_parent(if1);

				out.set_body(in);
				in.set_parent(out);
			} else {
				// exchange conditions
				ast_expr e1 = if1.get_cond();
				ast_expr e2 = if2.get_cond();
				if1.set_cond(e2);
				e2.set_parent(if1);
				if2.set_cond(e1);
				e1.set_parent(if2);
			}

			if (dest.get_nodetype() == AST_NODE_TYPE.AST_ASSIGN) {
				ast_assign a = (ast_assign) dest;
				if (a.get_bound() != null) {
					gm_symtab_entry bound_sym = a.get_bound().getSymInfo();
					// [xx] correct?
					// due to flip, the bound should be always be new outer-loop
					if ((bound_sym == iter_out) || (bound_sym == iter_in)) {
						a.get_bound().setSymInfo(iter_in); // new outer-loop
					}
				}
			}
		}
	}
}