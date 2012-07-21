package ast;

import common.GlobalMembersGm_dumptree;
import common.gm_apply;

public class ast_call extends ast_sent
{

	public void dispose()
	{
		if (b_in != null)
			b_in.dispose();
	}
	public void reproduce(int ind_level)
	{
		assert is_builtin_call();
		b_in.reproduce(ind_level);
	}
	public void dump_tree(int ind_level)
	{
		GlobalMembersGm_dumptree.IND(ind_level);
		System.out.print("[CALL\n");
		assert is_builtin_call();
		b_in.dump_tree(ind_level + 1);
		System.out.print("\n");
		GlobalMembersGm_dumptree.IND(ind_level);
		System.out.print("]");
    
	}
	public void traverse_sent(gm_apply a, boolean is_post, boolean is_pre)
	{
		assert is_builtin_call();
		b_in.traverse(a, is_post, is_pre);
	}

	public static ast_call new_builtin_call(ast_expr_builtin b)
	{
		ast_call C = new ast_call();
		b.set_parent(C);
		C.b_in = b;
		C.is_blt_in = true;
		return C;
	}

	private ast_call()
	{
		super(AST_NODE_TYPE.AST_CALL);
		this.b_in = null;
		this.is_blt_in = false;
	}

	public final ast_expr_builtin get_builtin()
	{
		return b_in;
	}
	public final void set_builtin(ast_expr_builtin b)
	{
		b_in = b;
		assert b_in != null;
		b_in.set_parent(this);
	}
	public final boolean is_builtin_call()
	{
		return is_blt_in;
	}

	private ast_expr_builtin b_in;
	private boolean is_blt_in;
}