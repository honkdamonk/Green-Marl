package opt;

import ast.AST_NODE_TYPE;
import ast.ast_assign;
import ast.ast_field;
import ast.ast_foreach;
import ast.ast_if;
import ast.ast_sent;
import inc.GMTYPE_T;

import common.gm_apply;

// find candiates
public class gm_flip_find_candidate extends gm_apply
{
	public gm_flip_find_candidate()
	{
		this.set_for_sent(true);

		avoid_reverse = false;
		avoid_pull = false;
	}

	@Override
	public boolean apply(ast_sent s)
	{
		if (s.get_nodetype() == AST_NODE_TYPE.AST_FOREACH)
		{
			ast_foreach fe = (ast_foreach) s;
			ast_foreach in;
			ast_if if1;
			ast_if if2;
			ast_sent dest;
			if (!GlobalMembersGm_flip_edges.capture_pattern(fe, if1, in, if2, dest))
				return true;


			if (avoid_reverse)
			{
				if (in.get_iter_type() == GMTYPE_T.GMTYPE_NODEITER_IN_NBRS)
				{
					target.addLast(fe);
					return true; // do ont push it twice
				}
			}

			if (avoid_pull)
			{
				if (dest.get_nodetype() == AST_NODE_TYPE.AST_ASSIGN)
				{
					ast_assign d = (ast_assign) dest;
					if (!d.is_target_scalar())
					{
						ast_field f = d.get_lhs_field();

						// driver is inner loop
						if (f.get_first().getSymInfo() == fe.get_iterator().getSymInfo())
						{
							target.addLast(fe);
							return true;
						}
					}
				}
			}
		}

		return true;
	}

	public final void set_to_avoid_reverse_edges(boolean b)
	{
		avoid_reverse = b;
	}

	public final void set_to_avoid_pull_computation(boolean b)
	{
		avoid_pull = b;
	}

	public final java.util.LinkedList<ast_foreach> get_target()
	{
		return target;
	}

	private boolean avoid_reverse;
	private boolean avoid_pull;

	private java.util.LinkedList<ast_foreach> target = new java.util.LinkedList<ast_foreach>();
}