package common;

import ast.AST_NODE_TYPE;
import ast.ast_expr;
import ast.ast_expr_reduce;

public class replace_subexpr_A extends gm_apply
{
	public replace_subexpr_A(ast_expr e_old, ast_expr e_new)
	{
		super();
		found = false;
		o = e_old;
		n = e_new;
		set_for_expr(true);
	}
	public final boolean has_found()
	{
		return found;
	}

	@Override
	public boolean apply(ast_expr e)
	{
		if (e == o)
		{
			assert found == false;
			found = true;

			ast_expr E = e.get_up_op(); // should be subexpression
			assert E != null;

			// replace e(==0) with n
			if (E.get_nodetype() == AST_NODE_TYPE.AST_EXPR_RDC)
			{
				ast_expr_reduce R = (ast_expr_reduce) E;
				if (e == R.get_filter())
				{
					R.set_filter(n);
					n.set_parent(R);
					n.set_up_op(R);
				}
				else if (e == R.get_body())
				{
					R.set_body(n);
					n.set_parent(R);
					n.set_up_op(R);
				}
			}
			else
			{
				if (e == E.get_left_op())
				{
					E.set_left_op(n);
					n.set_parent(E);
					n.set_up_op(E);
				}
				else if (e == E.get_right_op())
				{
					E.set_right_op(n);
					n.set_parent(E);
					n.set_up_op(E);
				}
				else
				{
					assert false;
				}
			}
		}
		return true;
	}

	private ast_expr o;
	private ast_expr n;
	private boolean found;
}