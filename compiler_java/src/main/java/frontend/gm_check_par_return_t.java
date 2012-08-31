package frontend;

import ast.AST_NODE_TYPE;
import ast.ast_bfs;
import ast.ast_foreach;
import ast.ast_sent;

import common.GM_ERRORS_AND_WARNINGS;
import common.gm_error;
import common.gm_apply;

//--------------------------------------------
// Additional rules
// (1) No return in the middle of par-context 
//--------------------------------------------

public class gm_check_par_return_t extends gm_apply
{
	public gm_check_par_return_t()
	{
		set_for_sent(true);
		set_separate_post_apply(true);
		par_depth = 0;
		_is_okay = true;
	}

	public final boolean is_okay()
	{
		return _is_okay;
	}

	@Override
	public boolean apply(ast_sent s)
	{
		if (s.get_nodetype() == AST_NODE_TYPE.AST_RETURN)
		{
			if (par_depth > 0)
			{
				gm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_PAR_RETURN, s.get_line(), s.get_col());
				_is_okay = false;
			}
		}
		else if (s.get_nodetype() == AST_NODE_TYPE.AST_FOREACH)
		{
			ast_foreach fe = (ast_foreach) s;
			if (fe.is_parallel())
			{
				par_depth++;
			}
		}
		else if (s.get_nodetype() == AST_NODE_TYPE.AST_BFS)
		{
			ast_bfs bfs = (ast_bfs) s;
			if (bfs.is_parallel())
			{
				par_depth++;
			}
		}
		return true;
	}
	@Override
	public boolean apply2(ast_sent s)
	{
		if (s.get_nodetype() == AST_NODE_TYPE.AST_FOREACH)
		{
			ast_foreach fe = (ast_foreach) s;
			if (fe.is_parallel())
			{
				par_depth--;
			}
		}
		else if (s.get_nodetype() == AST_NODE_TYPE.AST_BFS)
		{
			ast_bfs bfs = (ast_bfs) s;
			if (bfs.is_parallel())
			{
				par_depth--;
			}
		}
		return true;
	}
	private int par_depth;
	private boolean _is_okay;
}