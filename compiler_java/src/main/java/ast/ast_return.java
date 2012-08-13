package ast;

import common.GlobalMembersGm_dumptree;
import common.gm_apply;

public class ast_return extends ast_sent
{
	protected ast_return()
	{
		super(AST_NODE_TYPE.AST_RETURN);
		this.expr = null;
	}
	protected ast_expr expr;

	public void dispose()
	{
		if (expr != null)
			expr.dispose();
	}
	public static ast_return new_return(ast_expr e)
	{
		ast_return R = new ast_return();
		R.expr = e;
		if (e != null)
			e.set_parent(R);
		return R;
	}
	public final ast_expr get_expr()
	{
		return expr;
	}
	public final void set_expr(ast_expr e)
	{
		expr = e;
		if (e != null)
			e.set_parent(this);
	}

	public void reproduce(int ind_level)
	{
		Out.push("Return ");
		if (expr != null)
			expr.reproduce(0);
		Out.pushln(";");
	}
	public void dump_tree(int ind_level)
	{
		GlobalMembersGm_dumptree.IND(ind_level);
		System.out.print("[Return ");
		if (expr != null)
		{
			System.out.print("\n");
			expr.dump_tree(ind_level + 1);
			System.out.print("\n");
			GlobalMembersGm_dumptree.IND(ind_level);
		}
		System.out.print("]");
	}
	public void traverse_sent(gm_apply a, boolean is_post, boolean is_pre)
	{
		if (get_expr() != null)
			get_expr().traverse(a, is_post, is_pre);
	}
}