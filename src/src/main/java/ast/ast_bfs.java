package ast;

import inc.GMTYPE_T;
import common.gm_apply;

// BFS or DFS
public class ast_bfs extends ast_sent
{
	public void dispose()
	{
		if (f_body != null)
			f_body.dispose();
		if (b_body != null)
			b_body.dispose();
		if (f_filter != null)
			f_filter.dispose();
		if (b_filter != null)
			b_filter.dispose();
		if (navigator != null)
			navigator.dispose();
		if (iter != null)
			iter.dispose();
		if (src != null)
			src.dispose();
		if (root != null)
			root.dispose();
		if (iter2 != null)
			iter2.dispose();
		delete_symtabs();
	}

	public static ast_bfs new_bfs(ast_id it, ast_id src, ast_id root, ast_expr navigator, ast_expr f_filter, ast_expr b_filter, ast_sentblock fb, ast_sentblock bb, boolean use_tp)
	{
		return new_bfs(it, src, root, navigator, f_filter, b_filter, fb, bb, use_tp, true);
	}
//C++ TO JAVA CONVERTER NOTE: Java does not allow default values for parameters. Overloaded methods are inserted above.
//ORIGINAL LINE: static ast_bfs* new_bfs(ast_id* it, ast_id* src, ast_id* root, ast_expr* navigator, ast_expr* f_filter, ast_expr* b_filter, ast_sentblock* fb, ast_sentblock* bb, boolean use_tp, boolean is_bfs = true)
	public static ast_bfs new_bfs(ast_id it, ast_id src, ast_id root, ast_expr navigator, ast_expr f_filter, ast_expr b_filter, ast_sentblock fb, ast_sentblock bb, boolean use_tp, boolean is_bfs)
	{
		ast_bfs d = new ast_bfs();
		d.iter = it;
		d.src = src;
		d.root = root;
		d.f_body = fb;
		d.b_body = bb;
		d.use_transpose = use_tp;
		d.b_filter = b_filter;
		d.f_filter = f_filter;
		d.navigator = navigator;
		d._bfs = is_bfs;

		src.set_parent(d);
		it.set_parent(d);
		root.set_parent(d);
		if (fb != null)
			fb.set_parent(d);
		if (bb != null)
			bb.set_parent(d);
		if (navigator != null)
			navigator.set_parent(d);
		if (f_filter != null)
			f_filter.set_parent(d);
		if (b_filter != null)
			b_filter.set_parent(d);
		return d;
	}

	public final ast_sentblock get_fbody()
	{
		return f_body;
	}
	public final ast_sentblock get_bbody()
	{
		return b_body;
	}
	public final ast_expr get_navigator()
	{
		return navigator;
	}
	public final ast_expr get_f_filter()
	{
		return f_filter;
	}
	public final ast_expr get_b_filter()
	{
		return b_filter;
	}
	public final ast_id get_iterator()
	{
		return iter;
	}
	public final ast_id get_iterator2()
	{
		return iter2;
	}
	public final ast_id get_source()
	{
		return src;
	}
	public final ast_id get_root()
	{
		return root;
	}
	public final boolean is_transpose()
	{
		return use_transpose;
	}
	public final boolean is_bfs()
	{
		return _bfs;
	}

	public final void set_iterator2(ast_id id)
	{
		assert iter2 == null;
		iter2 = id;
	}
	public final void set_navigator(ast_expr e)
	{
		if (e != null)
			e.set_parent(this);
		navigator = e;
	}
	public final void set_f_filter(ast_expr e)
	{
		if (e != null)
			e.set_parent(this);
		f_filter = e;
	}
	public final void set_b_filter(ast_expr e)
	{
		if (e != null)
			e.set_parent(this);
		b_filter = e;
	}
	public final void set_fbody(ast_sentblock b)
	{
		f_body = b;
	}
	public final void set_bbody(ast_sentblock b)
	{
		b_body = b;
	}
	public void reproduce(int ind_level)
	{
		Out.push("InBFS (");
		iter.reproduce(0);
		Out.push(" : ");
		src.reproduce(0);
		if (use_transpose)
			Out.push("^");
		Out.push(".Nodes ");
		Out.push(" ; ");
		root.reproduce(0);
		Out.push(")");
		/*
		 if ((edge_cond != NULL) || (node_cond != NULL)) {
		 Out.push ('[');
		 if (node_cond != NULL) node_cond->reproduce(0);
		 Out.push (';');
		 if (edge_cond != NULL) edge_cond->reproduce(0);
		 Out.push (']');
		 }
	
		 if (filter != NULL) {
		 Out.push('(');
		 filter->reproduce(0);
		 Out.push(')');
		 }
		 */
		if (navigator != null)
		{
			Out.push('[');
			navigator.reproduce(0);
			Out.push(']');
		}
		if (f_filter != null)
		{
			Out.push('[');
			f_filter.reproduce(0);
			Out.push(']');
		}
    
		if (f_body != null)
		{
			f_body.reproduce(0);
		}
		else
		{
			Out.pushln("{}");
		}
    
		if (b_filter != null)
		{
			Out.push('[');
			b_filter.reproduce(0);
			Out.push(']');
		}
		if (b_body != null)
		{
			Out.pushln("InReverse ");
			b_body.reproduce(0);
		}
	}
	public void dump_tree(int ind_level)
	{
		// later
		assert false;
	}
	public void traverse_sent(gm_apply a, boolean is_post, boolean is_pre)
	{
		boolean for_id = a.is_for_id();
		boolean for_rhs = a.is_for_rhs();
    
		if (is_pre)
		{
			ast_id src = get_source();
			ast_id it = get_iterator();
			ast_id root = get_root();
			if (for_id)
			{
				a.apply(src);
				a.apply(it);
				a.apply(root);
			}
			if (for_rhs)
			{
				a.apply_rhs(src);
				a.apply_rhs(it);
				a.apply_rhs(root);
    
			}
		}
    
		// traverse
		ast_expr n = get_navigator();
		ast_expr fc = get_f_filter();
		ast_expr bc = get_b_filter();
		if (n != null)
			n.traverse(a, is_post, is_pre);
		if (fc != null)
			fc.traverse(a, is_post, is_pre);
		if (bc != null)
		{
			bc.traverse(a, is_post, is_pre);
		}
    
		if (!a.is_traverse_local_expr_only())
		{
			ast_sentblock fb = get_fbody();
			ast_sentblock bb = get_bbody();
			if (fb != null)
				fb.traverse(a, is_post, is_pre);
			if (bb != null)
			{
				a.begin_traverse_reverse(this);
				bb.traverse(a, is_post, is_pre);
				a.end_traverse_reverse(this);
			}
		}
    
		if (is_post)
		{
			ast_id src = get_source();
			ast_id id = get_iterator();
			ast_id root = get_root();
			if (for_id)
			{
				if (a.has_separate_post_apply())
				{
					a.apply2(src);
					a.apply2(id);
					a.apply2(root);
				}
				else
				{
					a.apply(src);
					a.apply(id);
					a.apply(root);
				}
			}
			if (for_rhs)
			{
				if (a.has_separate_post_apply())
				{
					a.apply_rhs2(src);
					a.apply_rhs2(id);
					a.apply_rhs2(root);
				}
				else
				{
					a.apply_rhs(src);
					a.apply_rhs(id);
					a.apply_rhs(root);
				}
			}
		}
	}
	public final int get_iter_type()
	{
		return GMTYPE_T.GMTYPE_NODEITER_BFS;
	}
	public final int get_iter_type2()
	{
		return is_transpose() ? GMTYPE_T.GMTYPE_NODEITER_IN_NBRS : GMTYPE_T.GMTYPE_NODEITER_NBRS;
	}

	@Override
	public boolean has_scope()
	{
		return true;
	}

	// currently BFS is always parallel. (codegen assumes there is only one BFS. also flip-edge opt does)
	public final boolean is_sequential()
	{
		return !is_bfs();
	} // sequential execution
	public final boolean is_parallel()
	{
		return is_bfs();
	} // sequential execution

	protected ast_bfs()
	{
		super(AST_NODE_TYPE.AST_BFS);
		this.f_body = null;
		this.b_body = null;
		this.f_filter = null;
		this.b_filter = null;
		this.navigator = null;
		this.iter = null;
		this.src = null;
		this.root = null;
		this.iter2 = null;
		this.use_transpose = false;
		this._bfs = true;
		create_symtabs();
	}

	private ast_sentblock f_body;
	private ast_sentblock b_body;

	private ast_expr f_filter;
	private ast_expr b_filter;
	private ast_expr navigator;

	private ast_id iter;
	private ast_id src;
	private ast_id root;
	private ast_id iter2; // iterator used for frontier expansion [xxx] what?
	private boolean use_transpose;
	private boolean _bfs;
}