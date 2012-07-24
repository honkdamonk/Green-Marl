package ast;

import inc.GMTYPE_T;

import common.GlobalMembersGm_dumptree;
import common.GlobalMembersGm_misc;
import common.gm_apply;

public class ast_foreach extends ast_sent {
	public void dispose() {
		if (body != null)
			body.dispose();
		if (iterator != null)
			iterator.dispose();
		if (source != null)
			source.dispose();
		if (source2 != null)
			source2.dispose();
		if (cond != null)
			cond.dispose();
		// delete symbol info
		delete_symtabs();
	}

	private ast_foreach() {
		super(AST_NODE_TYPE.AST_FOREACH);
		this.body = null;
		this.iterator = null;
		this.source = null;
		this.source2 = null;
		this.cond = null;
		this.seq_exe = false;
		this.use_reverse = false;
		this.iter_type = GMTYPE_T.GMTYPE_GRAPH;
		create_symtabs();
	}

	// iterate on a graph
	public static ast_foreach new_foreach(ast_id it, ast_id src, ast_sent b, GMTYPE_T iter_type) {
		return new_foreach(it, src, b, iter_type, null);
	}

	// C++ TO JAVA CONVERTER NOTE: Java does not allow default values for
	// parameters. Overloaded methods are inserted above.
	// ORIGINAL LINE: static ast_foreach* new_foreach(ast_id* it, ast_id* src,
	// ast_sent* b, int iter_type, ast_expr* cond = null)
	public static ast_foreach new_foreach(ast_id it, ast_id src, ast_sent b, GMTYPE_T iter_type, ast_expr cond) {
		ast_foreach d = new ast_foreach();
		d.iterator = it;
		d.source = src;
		d.body = b;
		d.iter_type = iter_type;
		d.cond = cond;
		src.set_parent(d);
		it.set_parent(d);
		b.set_parent(d);
		if (cond != null)
			cond.set_parent(d);
		return d;
	}

	public void reproduce(int ind_level) {

		if (!is_sequential()) {
			Out.push("Foreach (");
		} else {
			Out.push("For (");
		}
		iterator.reproduce(0);
		Out.push(" : ");
		source.reproduce(0);
		Out.push(".");
		Out.push(GlobalMembersGm_misc.gm_get_iter_type_string(iter_type));
		if (iter_type.is_common_nbr_iter_type()) {
			Out.push('(');
			source2.reproduce(0);
			Out.push(')');
		}
		Out.pushln(")");
		if (cond != null) {
			Out.push("( ");
			cond.reproduce(0);
			Out.pushln(")");
		}
		if (body.get_nodetype() == AST_NODE_TYPE.AST_SENTBLOCK)
			body.reproduce(0);
		else {
			Out.push_indent();
			body.reproduce(0);
			Out.pop_indent();
		}
	}

	public void dump_tree(int ind_level) {
		GlobalMembersGm_dumptree.IND(ind_level);
		assert parent != null;
		if (!is_sequential())
			System.out.print("[FOREACH ");
		else
			System.out.print("[FOR ");
		iterator.dump_tree(0);
		System.out.print(" : ");
		source.dump_tree(ind_level + 1);
		System.out.print("  ");
		System.out.printf("%s ", GlobalMembersGm_misc.gm_get_iter_type_string(iter_type));
		if (cond != null)
			System.out.print(" FILTER: ");
		System.out.print("\n");
		if (cond != null) {
			cond.dump_tree(ind_level + 1);
			System.out.print("\n");
		}
		body.dump_tree(ind_level + 1);
		System.out.print("\n");
		GlobalMembersGm_dumptree.IND(ind_level);
		System.out.print("]");
	}

	public void traverse_sent(gm_apply a, boolean is_post, boolean is_pre) {
		// a->begin_context(this);

		boolean for_id = a.is_for_id();
		boolean for_rhs = a.is_for_rhs();

		if (is_pre) {
			ast_id src = get_source();
			ast_id src2 = get_source2();
			ast_id it = get_iterator();
			if (for_id) {
				a.apply(src);
				a.apply(it);
				if (src2 != null)
					a.apply(src2);
			}
			if (for_rhs) {
				a.apply_rhs(src);
				a.apply_rhs(it);
				if (src2 != null)
					a.apply_rhs(src2);

			}
		}

		// traverse
		ast_sent ss = get_body();
		if (!a.is_traverse_local_expr_only())
			ss.traverse(a, is_post, is_pre);

		ast_expr f = get_filter();
		if (f != null)
			f.traverse(a, is_post, is_pre);

		if (is_post) {
			ast_id src = get_source();
			ast_id src2 = get_source2();
			ast_id id = get_iterator();
			if (for_id) {
				if (a.has_separate_post_apply()) {
					a.apply2(src);
					a.apply2(id);
					if (src2 != null)
						a.apply2(src2);
				} else {
					a.apply(src);
					a.apply(id);
					if (src2 != null)
						a.apply(src2);
				}
			}
			if (for_rhs) {
				if (a.has_separate_post_apply()) {
					a.apply_rhs(src);
					a.apply_rhs(id);
					if (src2 != null)
						a.apply_rhs(src2);
				} else {
					a.apply_rhs2(src);
					a.apply_rhs2(id);
					if (src2 != null)
						a.apply_rhs2(src2);
				}
			}
		}
	}

	public final ast_id get_source() {
		return source;
	}

	public final ast_id get_iterator() {
		return iterator;
	}

	public final void set_iterator(ast_id newIterator) {
		iterator = newIterator;
	}

	public final ast_sent get_body() {
		return body;
	}

	public final ast_expr get_filter() {
		return cond;
	}

	public final GMTYPE_T get_iter_type() {
		return iter_type;
	} // GM_ITERATORS

	public final void set_iter_type(GMTYPE_T i) {
		iter_type = i;
	} // GM_ITERATORS

	// should be same to get_iterator()->get_type_summary()
	public final ast_id get_source2() {
		return source2;
	}

	public final void set_source2(ast_id i) {
		source2 = i;
		if (i != null)
			i.set_parent(this);
	}

	public final void set_filter() {
		set_filter(null);
	}

	// C++ TO JAVA CONVERTER NOTE: Java does not allow default values for
	// parameters. Overloaded methods are inserted above.
	// ORIGINAL LINE: void set_filter(ast_expr* expr = null)
	public final void set_filter(ast_expr expr) {
		cond = expr;
		if (cond != null)
			cond.set_parent(this);
	}

	public final void set_body(ast_sent s) {
		body = s;
		assert body != null;
		body.set_parent(this);
	}

	@Override
	public boolean has_scope() {
		return true;
	}

	@Override
	public boolean is_under_parallel_execution() {
		return is_parallel();
	}

	// For is sequential while FOREACH is parallel.
	// Optimization may override parallel execution with sequential.

	// sequential execution
	public final boolean is_sequential() {
		return seq_exe;
	}

	public final void set_sequential(boolean b) {
		seq_exe = b;
	}

	// parallel execution
	public final boolean is_parallel() {
		return !is_sequential();
	}

	// for set iterator
	public final boolean is_reverse_iteration() {
		return use_reverse;
	}

	public final void set_reverse_iteration(boolean b) {
		use_reverse = b;
	}

	private ast_sent body;
	private ast_id iterator;
	private ast_id source; // graph
	private ast_id source2; // common nbr
	private GMTYPE_T iter_type; // GM_ITERATORS
	private ast_expr cond;
	private boolean seq_exe;
	private boolean use_reverse;
}