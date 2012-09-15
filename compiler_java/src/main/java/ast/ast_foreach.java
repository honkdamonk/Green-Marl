package ast;

import inc.gm_type;

import common.gm_apply;
import common.gm_dumptree;

public class ast_foreach extends ast_sent {
	
	private ast_sent body = null;
	private ast_id iterator = null;
	private ast_id source = null; // graph
	private ast_id source2 = null; // common nbr
	private gm_type iter_type; // GM_ITERATORS
	private ast_expr cond = null;
	private boolean seq_exe = false;
	private boolean use_reverse = false;

	private ast_foreach() {
		super(ast_node_type.AST_FOREACH);
		iter_type = gm_type.GMTYPE_GRAPH;
		create_symtabs();
	}

	// iterate on a graph
	public static ast_foreach new_foreach(ast_id it, ast_id src, ast_sent b, gm_type iter_type) {
		return new_foreach(it, src, b, iter_type, null);
	}

	public static ast_foreach new_foreach(ast_id it, ast_id src, ast_sent b, gm_type iter_type, ast_expr cond) {
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
		Out.push(iter_type.get_iter_type_string());
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
		if (body.get_nodetype() == ast_node_type.AST_SENTBLOCK)
			body.reproduce(0);
		else {
			Out.pushIndent();
			body.reproduce(0);
			Out.popIndent();
		}
	}

	public void dump_tree(int ind_level) {
		gm_dumptree.IND(ind_level);
		assert parent != null;
		if (!is_sequential())
			System.out.print("[FOREACH ");
		else
			System.out.print("[FOR ");
		iterator.dump_tree(0);
		System.out.print(" : ");
		source.dump_tree(ind_level + 1);
		System.out.print("  ");
		System.out.printf("%s ", iter_type.get_iter_type_string());
		if (cond != null)
			System.out.print(" FILTER: ");
		System.out.print("\n");
		if (cond != null) {
			cond.dump_tree(ind_level + 1);
			System.out.print("\n");
		}
		body.dump_tree(ind_level + 1);
		System.out.print("\n");
		gm_dumptree.IND(ind_level);
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

	public final gm_type get_iter_type() {
		return iter_type;
	} // GM_ITERATORS

	public final void set_iter_type(gm_type i) {
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
	
}