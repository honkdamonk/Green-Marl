import ast.AST_NODE_TYPE;
import ast.ast_assign;
import ast.ast_sent;

import common.GM_ERRORS_AND_WARNINGS;
import common.GlobalMembersGm_error;
import common.gm_apply;

public class check_argmax_num_args_t extends gm_apply
{
	public check_argmax_num_args_t()
	{
		_is_okay = true;
		set_for_sent(true);
	}
	// post apply
	public final boolean apply(ast_sent e)
	{
		if (e.get_nodetype() == AST_NODE_TYPE.AST_ASSIGN)
		{
			ast_assign a = (ast_assign) e;
			if (a.is_argminmax_assign())
			{
				int l_count = a.get_lhs_list().size();
				int r_count = a.get_rhs_list().size();
				if (l_count != r_count)
				{
					String temp = new String(new char[128]);
					String.format(temp, "lhs_count:%d, rhs_count:%d", l_count, r_count);
					GlobalMembersGm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_INVALID_ARGMAX_COUNT, a.get_line(), a.get_col(), temp);
					_is_okay = false;
				}
			}
		}

		return true;
	}
	public final boolean is_okay()
	{
		return _is_okay;
	}
	private boolean _is_okay;
}