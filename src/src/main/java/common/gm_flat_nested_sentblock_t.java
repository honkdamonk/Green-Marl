package common;

import ast.AST_NODE_TYPE;
import ast.ast_sent;
import ast.ast_sentblock;

public class gm_flat_nested_sentblock_t extends gm_apply
{
	// requires 'post' apply
	public gm_flat_nested_sentblock_t()
	{
		set_for_sent(true);
	}

	public final boolean apply(ast_sent s)
	{
		// parent should be another sentblock
		if ((s.get_parent() == null) || (s.get_parent().get_nodetype() != AST_NODE_TYPE.AST_SENTBLOCK))
			return true;

		if (s.get_nodetype() == AST_NODE_TYPE.AST_SENTBLOCK)
		{
			ast_sentblock sb = (ast_sentblock) s;

			// flat only if no new symbol is defined
			if ((sb.get_symtab_field().get_num_symbols() == 0) && (sb.get_symtab_var().get_num_symbols() == 0) && (sb.get_symtab_proc().get_num_symbols() == 0))
			{
				targets.addLast(sb);
			}
		}

		return true;
	}

	public final void post_process()
	{
		java.util.Iterator<ast_sentblock> I;
		for (I = targets.iterator(); I.hasNext();)
		{
			ast_sentblock sb = I.next();
			ast_sentblock parent = (ast_sentblock) sb.get_parent();

			java.util.LinkedList<ast_sent> parent_sents = parent.get_sents();
			java.util.LinkedList<ast_sent> my_sents = sb.get_sents();

			java.util.Iterator<ast_sent> I;

			// prepare moving mine to parent's
			for (I = my_sents.iterator(); I.hasNext();)
			{
				ast_sent s = I.next();
				s.set_parent(parent);
			}

			// find location
			for (I = parent_sents.iterator(); I.hasNext();)
			{
				if (I.next() == sb)
					break;
			}
			assert I.hasNext();

			// move my_sents after I
			parent_sents.splice(I, my_sents);
//C++ TO JAVA CONVERTER TODO TASK: There is no direct equivalent to the STL list 'erase' method in Java:
			parent_sents.erase(I);

			// delete SB
			if (sb != null)
				sb.dispose();
		}
	}

	private java.util.LinkedList<ast_sentblock> targets = new java.util.LinkedList<ast_sentblock>();

}