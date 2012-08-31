package frontend;

import ast.AST_NODE_TYPE;
import ast.ast_bfs;
import ast.ast_expr;
import ast.ast_foreach;
import ast.ast_if;
import ast.ast_sent;
import ast.ast_sentblock;

import common.gm_apply;

//---------------------------------------------------
// syntax sugar elimination, prior to type resolution
//  0. (argument) x,y:Z ==> x:TYPE, y:TYPE
//  1. filter of foreach --> if inside foreach
//  2. define and initialization --> define; initialization
//---------------------------------------------------
public class gm_ss1_filter extends gm_apply
{
	private ast_sent make_if_then(ast_sent old_body, ast_expr filter)
	{
		ast_if new_body = ast_if.new_if(filter, old_body, null);
		return new_body;
	}

	@Override
	public boolean apply(ast_sent s)
	{
		if (s.get_nodetype() == AST_NODE_TYPE.AST_FOREACH)
		{

			// if it has a filter, change it into if-then
			ast_foreach fe = (ast_foreach) s;
			ast_expr e = fe.get_filter();
			if (e == null)
				return true;

			ast_sent old_body = fe.get_body();
			ast_sent new_body = make_if_then(old_body, e);
			ast_sentblock sb = ast_sentblock.new_sentblock();
			sb.add_sent(new_body);

			fe.set_filter(null);
			fe.set_body(sb);
		} // or DFS
		else if (s.get_nodetype() == AST_NODE_TYPE.AST_BFS)
		{
			ast_bfs bfs = (ast_bfs) s;
			ast_expr e = bfs.get_f_filter();
			if (e != null)
			{
				ast_sent old_body = bfs.get_fbody();
				ast_sent new_body = make_if_then(old_body, e);
				ast_sentblock sb = ast_sentblock.new_sentblock();
				sb.add_sent(new_body);

				bfs.set_f_filter(null);
				bfs.set_fbody(sb);
			}
			e = bfs.get_b_filter();
			if (e != null)
			{
				ast_sent old_body = bfs.get_bbody();
				ast_sent new_body = make_if_then(old_body, e);
				ast_sentblock sb = ast_sentblock.new_sentblock();
				sb.add_sent(new_body);

				bfs.set_b_filter(null);
				bfs.set_bbody(sb);
			}

			// Note that no symtab is available yet
		}
		return true;
	}

}