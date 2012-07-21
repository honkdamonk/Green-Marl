package opt;

import ast.AST_NODE_TYPE;
import ast.ast_foreach;
import ast.ast_sent;
import ast.ast_sentblock;
import backend_cpp.*;
import backend_giraph.*;
import common.*;
import frontend.*;
import inc.*;
import tangible.*;

public class gm_merge_loop_t extends gm_apply
{
	@Override
	public boolean apply(ast_sent s)
	{
		if (s.get_nodetype() != AST_NODE_TYPE.AST_SENTBLOCK)
			return true;

		ast_sentblock sb = (ast_sentblock) s;
		java.util.LinkedList<ast_sent> sents = sb.get_sents(); // work with a copyed list
		java.util.Iterator<ast_sent> i;
		ast_foreach prev = null;
		for (i = sents.iterator(); i.hasNext();)
		{
			if (prev == null)
			{
				if ((i.next()).get_nodetype() == AST_NODE_TYPE.AST_FOREACH)
					prev = (ast_foreach)(i.next());
				continue;
			}
			else
			{
				// pick two consecutive foreach blocks.
				// check they are mergeable.
				// If so, merge. delete the second one.
				if ((i.next()).get_nodetype() == AST_NODE_TYPE.AST_FOREACH)
				{
					ast_foreach curr = (ast_foreach)(i.next());
					if (GlobalMembersGm_merge_loops.gm_is_mergeable_loops(prev, curr))
					{

						// replace curr's iterator with prev's
						GlobalMembersGm_merge_loops.replace_iterator_sym(prev, curr);

						// merge body and delete curr.
						if (prev.get_body().get_nodetype() != AST_NODE_TYPE.AST_SENTBLOCK)
							GlobalMembersGm_transform_helper.gm_make_it_belong_to_sentblock(prev.get_body());
						if (curr.get_body().get_nodetype() != AST_NODE_TYPE.AST_SENTBLOCK)
							GlobalMembersGm_transform_helper.gm_make_it_belong_to_sentblock(curr.get_body());

						GlobalMembersGm_merge_sentblock.gm_merge_sentblock((ast_sentblock) prev.get_body(), (ast_sentblock) curr.get_body());

						// redo-rw-analysis
						GlobalMembersGm_rw_analysis.gm_redo_rw_analysis(prev);

						GlobalMembersGm_transform_helper.gm_ripoff_sent(curr, GlobalMembersGm_transform_helper.GM_NOFIX_SYMTAB); // it will be deleted
						if (curr != null)
							curr.dispose();

						_changed = true;
					}
					else
					{
						prev = curr;
					}
				}
				else
				{
					prev = null;
				}
			}
		}
		return true;
	}
	public final boolean is_changed()
	{
		return _changed;
	}
	public final void do_loop_merge(ast_sentblock top)
	{
		set_all(false);
		set_for_sent(true);
		_changed = false;
		top.traverse_post(this);
		java.util.Iterator<ast_sent> i;
		for (i = to_be_deleted.iterator(); i.hasNext();)
			i.next() = null;

		to_be_deleted.clear();
	}
	protected boolean _changed;
	protected java.util.LinkedList<ast_sent> to_be_deleted = new java.util.LinkedList<ast_sent>();
}
//bool gm_independent_optimize::do_merge_foreach(ast_procdef* proc) 
