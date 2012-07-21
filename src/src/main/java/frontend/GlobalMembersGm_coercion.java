package frontend;

import ast.AST_NODE_TYPE;
import ast.ast_assign;
import ast.ast_expr;
import ast.ast_node;
import ast.ast_return;

public class GlobalMembersGm_coercion
{
	public static void gm_insert_explicit_type_conversion_for_op(java.util.HashMap<ast_expr, Integer> targets)
	{

		java.util.Iterator<ast_expr, Integer> I;

		for (I = targets.iterator(); I.hasNext();)
		{

			ast_expr t = I.next().getKey();
			int dest_type = I.next().getValue();

			ast_expr up = t.get_up_op();
			assert up != null;
			boolean is_left;
			if (up.get_left_op() == t)
				is_left = true;
			else
			{
				assert up.get_right_op() == t;
				is_left = false;
			}

			ast_expr tc = ast_expr.new_typeconv_expr(dest_type, t);

			if (is_left)
			{
				up.set_left_op(tc);
			}
			else
			{
				up.set_right_op(tc);
			}
			tc.set_parent(up);
			tc.set_up_op(up);

		}

	}

	public static void gm_insert_explicit_type_conversion_for_assign_or_return(java.util.HashMap<ast_expr, Integer> targets)
	{

		java.util.Iterator<ast_expr, Integer> I;

		for (I = targets.iterator(); I.hasNext();)
		{
			ast_expr t = I.next().getKey();
			int dest_type = I.next().getValue();

			assert t.get_up_op() == null;
			ast_node n = t.get_parent();

			ast_expr tc = ast_expr.new_typeconv_expr(dest_type, t);

			if (n.get_nodetype() == AST_NODE_TYPE.AST_ASSIGN)
			{
				ast_assign a = (ast_assign) n;
				a.set_rhs(tc);
			}
			else if (n.get_nodetype() == AST_NODE_TYPE.AST_RETURN)
			{
				ast_return r = (ast_return) n;
				r.set_expr(tc);
			}
			else
			{
				assert false;
			}
		}

	}
}