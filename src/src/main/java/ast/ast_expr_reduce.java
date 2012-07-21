package ast;

import inc.GMEXPR_CLASS;
import inc.GM_REDUCE_T;
import inc.GlobalMembersGm_defs;
import common.GlobalMembersGm_dumptree;
import common.GlobalMembersGm_misc;
import common.gm_apply;

// Reduction expression
public class ast_expr_reduce extends ast_expr
{
	public void dispose()
	{
		if (iter != null)
			iter.dispose();
		if (body != null)
			body.dispose();
		if (filter != null)
			filter.dispose();
		if (src != null)
			src.dispose();
		if (src2 != null)
			src2.dispose();
		delete_symtabs();
	}
	public static ast_expr_reduce new_reduce_expr(int optype, ast_id iter, ast_id src, int iter_op, ast_expr body)
	{
		return new_reduce_expr(optype, iter, src, iter_op, body, null);
	}
//C++ TO JAVA CONVERTER NOTE: Java does not allow default values for parameters. Overloaded methods are inserted above.
//ORIGINAL LINE: static ast_expr_reduce* new_reduce_expr(int optype, ast_id* iter, ast_id* src, int iter_op, ast_expr* body, ast_expr* filter = null)
	public static ast_expr_reduce new_reduce_expr(int optype, ast_id iter, ast_id src, int iter_op, ast_expr body, ast_expr filter)
	{
		ast_expr_reduce e = new ast_expr_reduce();
		e.iter = iter;
		e.body = body;
		e.filter = filter;
		e.src = src;
		e.expr_class = GMEXPR_CLASS.GMEXPR_REDUCE;
		e.reduce_type = optype;
		e.iter_type = iter_op;

		iter.set_parent(e);
		src.set_parent(e);
		body.set_parent(e);
		body.set_up_op(e);
		if (filter != null)
		{
			filter.set_parent(e);
			filter.set_up_op(e);
		}

		return e;
	}

	public void reproduce(int ind_level)
	{
		Out.SPC();
		Out.push(GlobalMembersGm_misc.gm_get_reduce_expr_string(reduce_type));
    
		Out.push('(');
		iter.reproduce(0);
		Out.push(": ");
		src.reproduce(0);
		Out.push(".");
		Out.push(GlobalMembersGm_misc.gm_get_iter_type_string(iter_type));
		if (GlobalMembersGm_defs.gm_is_common_nbr_iter_type(iter_type))
		{
			Out.push('(');
			src2.reproduce(0);
			Out.push(')');
		}
		Out.push(")");
		if (filter != null)
		{
			Out.push('(');
			filter.reproduce(0);
			Out.push(')');
		}
		Out.push("{");
		body.reproduce(0);
		Out.push("} ");
	}
	public void dump_tree(int ind_level)
	{
		GlobalMembersGm_dumptree.IND(ind_level);
		System.out.printf("[%s ", GlobalMembersGm_misc.gm_get_reduce_expr_string(reduce_type));
		System.out.print(" (");
		iter.dump_tree(0);
		System.out.print(":");
		src.dump_tree(0);
		System.out.print(".");
		System.out.printf("%s)\n", GlobalMembersGm_misc.gm_get_iter_type_string(iter_type));
		if (filter != null)
		{
			GlobalMembersGm_dumptree.IND(ind_level + 1);
			System.out.print("<Filter> \n");
			filter.dump_tree(ind_level + 1);
			System.out.print("\n");
		}
		GlobalMembersGm_dumptree.IND(ind_level + 1);
		System.out.print("<Body> \n");
		body.dump_tree(ind_level + 1);
		System.out.print("\n");
    
		GlobalMembersGm_dumptree.IND(ind_level);
		System.out.print("]");
	}
	public void traverse(gm_apply a, boolean is_post, boolean is_pre)
	{
		a.begin_context(this);
    
		boolean for_id = a.is_for_id();
		boolean for_expr = a.is_for_expr();
		boolean for_symtab = a.is_for_symtab();
		boolean for_rhs = a.is_for_rhs();
    
		if (is_pre)
		{
			if (for_symtab)
			{
				apply_symtabs(a, PRE_APPLY);
			}
			ast_id src = get_source();
			ast_id it = get_iterator();
			ast_id src2 = get_source2();
			if (for_id)
			{
				a.apply(src);
				a.apply(it);
				if (src2 != null)
					a.apply(src2);
			}
			if (for_rhs)
			{
				a.apply_rhs(src);
				a.apply_rhs(it);
				if (src2 != null)
					a.apply_rhs(src2);
			}
			if (for_expr)
				a.apply(this);
		}
    
		if (get_filter() != null)
			get_filter().traverse(a, is_post, is_pre);
    
		// fixme: get_body might be null; in the middle for syntax-sugar2 transform (Sum=>Foreach)
		if (get_body() != null)
			get_body().traverse(a, is_post, is_pre);
    
		if (is_post)
		{
			boolean b = a.has_separate_post_apply();
			if (for_symtab)
			{
				apply_symtabs(a, POST_APPLY);
			}
			ast_id src = get_source();
			ast_id it = get_iterator();
			ast_id src2 = get_source2();
			if (for_id)
			{
				if (b)
				{
					a.apply2(src);
					a.apply2(it);
					if (src2 != null)
						a.apply2(src2);
				}
				else
				{
					a.apply(src);
					a.apply(it);
					if (src2 != null)
						a.apply(src2);
				}
			}
			if (for_rhs)
			{
				if (b)
				{
					a.apply_rhs2(src);
					a.apply_rhs2(it);
					if (src2 != null)
						a.apply_rhs2(src2);
				}
				else
				{
					a.apply_rhs(src);
					a.apply_rhs(it);
					if (src2 != null)
						a.apply_rhs(src2);
				}
			}
			if (for_expr)
			{
				if (b)
					a.apply2(this);
				else
					a.apply(this);
			}
		}
    
		a.end_context(this);
	}
	@Override
	public boolean has_scope()
	{
		return true;
	}

	public final int get_iter_type()
	{
		return iter_type;
	}
	public final void set_iter_type(int i)
	{
		iter_type = i;
	}
	public final int get_reduce_type()
	{
		return reduce_type;
	}
	public final ast_id get_source()
	{
		return src;
	}
	public final ast_id get_iterator()
	{
		return iter;
	}
	public final ast_expr get_filter()
	{
		return filter;
	}
	public final ast_expr get_body()
	{
		return body;
	}
	public final ast_id get_source2()
	{
		return src2;
	}

	public final void set_source2(ast_id i)
	{
		src2 = i;
		if (i != null)
			i.set_parent(this);
	}
	public final void set_filter(ast_expr e)
	{
		filter = e;
		if (e != null)
			e.set_parent(this);
	}
	public final void set_body(ast_expr e)
	{
		body = e;
		if (e != null)
			e.set_parent(this);
	}
	public ast_expr copy(boolean b)
	{
		//--------------------------------------------------------
		// this is wrong. a new local symbol table should be built.
		//--------------------------------------------------------
		assert false;
		ast_expr_reduce e = ast_expr_reduce.new_reduce_expr(reduce_type, iter.copy(b), src.copy(b), iter_type, body.copy(b), (filter != null) ? filter.copy(b) : null);
    
		e.set_type_summary(this.get_type_summary());
		return e;
	}

	private ast_expr_reduce()
	{
		super();
		this.iter = null;
		this.src = null;
		this.src2 = null;
		this.body = null;
		this.filter = null;
		this.reduce_type = GM_REDUCE_T.GMREDUCE_NULL.getValue();
		this.iter_type = 0;
		set_nodetype(AST_NODE_TYPE.AST_EXPR_RDC);
		create_symtabs();
	}
	private ast_id iter;
	private ast_id src;
	private ast_id src2;
	private ast_expr body;
	private ast_expr filter;
	private int reduce_type;
	private int iter_type;
}