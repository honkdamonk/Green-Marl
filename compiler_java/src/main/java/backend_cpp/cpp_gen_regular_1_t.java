package backend_cpp;

import ast.AST_NODE_TYPE;
import ast.ast_foreach;
import ast.ast_sent;

import common.gm_main;
import common.gm_transform_helper;
import common.gm_apply;

//------------------------------------------------------------------
// Code Regularization
//   (1) Make sure Return is located inside a sentence block
//   (2) Make sure Foreach is located inside a sentence block
//------------------------------------------------------------------
public class cpp_gen_regular_1_t extends gm_apply
{
	public cpp_gen_regular_1_t()
	{
		set_for_sent(true);
	}

	@Override
	public boolean apply(ast_sent s)
	{
		if (s.get_nodetype() == AST_NODE_TYPE.AST_RETURN)
		{
			targets.addLast(s);
		}
		else if (s.get_nodetype() == AST_NODE_TYPE.AST_FOREACH)
		{
			ast_foreach fe = (ast_foreach) s;
			if (gm_main.CPP_BE.get_lib().need_up_initializer(fe))
			{
				targets.addLast(fe);
			}

			if ((fe.get_body().get_nodetype() != AST_NODE_TYPE.AST_SENTBLOCK) && gm_main.CPP_BE.get_lib().need_down_initializer(fe))
			{
				targets.addLast(fe.get_body());
			}
		}
		return true;
	}

	public final void post_process()
	{
		for (ast_sent sent : targets)
			gm_transform_helper.gm_make_it_belong_to_sentblock(sent);
	}

	private java.util.LinkedList<ast_sent> targets = new java.util.LinkedList<ast_sent>();

}