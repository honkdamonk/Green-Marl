package ast;

import common.gm_dumptree;
import common.gm_apply;

public class ast_while extends ast_sent
{
	public void dispose()
	{
		if (body != null)
			body.dispose();
		if (cond != null)
			cond.dispose();
	}
	private ast_while()
	{
		super(AST_NODE_TYPE.AST_WHILE);
		this.body = null;
		this.cond = null;
		this.do_while = false;
	}

	public static ast_while new_while(ast_expr c, ast_sentblock s)
	{
		ast_while w = new ast_while();
		w.cond = c;
		w.body = s;
		w.do_while = false;
		c.set_parent(w);
		s.set_parent(w);
		return w;
	}
	public static ast_while new_do_while(ast_expr c, ast_sentblock s)
	{
		ast_while w = new ast_while();
		w.cond = c;
		w.body = s;
		w.do_while = true;
		c.set_parent(w);
		s.set_parent(w);
		return w;
	}

	// rotuines that should be implemented
	public void reproduce(int ind_level)
	{
		if (is_do_while())
		{
			Out.push("Do ");
			body.reproduce(ind_level); // body is always sentence block
			Out.push("While (");
			cond.reproduce(0);
			Out.pushln(" );");
		}
		else
		{
			Out.push("While (");
			cond.reproduce(0);
			Out.pushln(" )");
			body.reproduce(ind_level); // body is always sentence blco
		}
	}
	public void dump_tree(int ind_level)
	{
		gm_dumptree.IND(ind_level);
		assert parent != null;
		if (is_do_while())
		{
			System.out.print("[DO BODY: \n");
			body.dump_tree(ind_level + 1);
			System.out.print(" WHILE ");
			cond.dump_tree(0);
			System.out.print("\n");
			gm_dumptree.IND(ind_level);
			System.out.print("]");
		}
		else
		{
			System.out.print("[WHILE ");
			cond.dump_tree(0);
			System.out.print("  BODY: \n");
			body.dump_tree(ind_level + 1);
			System.out.print("\n");
			gm_dumptree.IND(ind_level);
			System.out.print("]");
		}
	}
	public void traverse_sent(gm_apply a, boolean is_post, boolean is_pre)
	{
		// traverse only
		get_cond().traverse(a, is_post, is_pre);
		if (!a.is_traverse_local_expr_only())
		{
			get_body().traverse(a, is_post, is_pre);
		}
	}

	public final ast_sent get_body()
	{
		return body;
	}
	public final ast_expr get_cond()
	{
		return cond;
	}
	public final boolean is_do_while()
	{
		return do_while;
	}
	public final void set_body(ast_sentblock s)
	{
		body = s;
	}
	public final void set_cond(ast_expr e)
	{
		cond = e;
		if (e != null)
		{
			e.set_parent(this);
		}
	}

	private ast_sentblock body;
	private ast_expr cond;
	private boolean do_while; // if true do_while, else while
}