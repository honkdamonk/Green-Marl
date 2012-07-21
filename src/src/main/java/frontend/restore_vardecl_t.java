import ast.AST_NODE_TYPE;
import ast.ast_id;
import ast.ast_procdef;
import ast.ast_sent;
import ast.ast_sentblock;
import ast.ast_typedecl;
import ast.ast_vardecl;

import common.GlobalMembersGm_transform_helper;
import common.GlobalMembersGm_traverse;
import common.gm_apply;

public class restore_vardecl_t extends gm_apply
{
	@Override
	public boolean apply(ast_sent b)
	{
		if (b.get_nodetype() != AST_NODE_TYPE.AST_SENTBLOCK)
			return true;

		ast_sentblock sb = (ast_sentblock) b;
		gm_symtab V = sb.get_symtab_var();
		gm_symtab F = sb.get_symtab_field();
		java.util.HashSet<gm_symtab_entry> Vs = V.get_entries();
		java.util.HashSet<gm_symtab_entry> Fs = F.get_entries();

		ast_sent top = null;
		//-------------------------------------
		// Add vardecls after all 'NOP's
		//-------------------------------------
		java.util.LinkedList<ast_sent> sents = sb.get_sents();
		java.util.Iterator<ast_sent> ii;
		for (ii = sents.iterator(); ii.hasNext();)
		{
			if ((ii.next()).get_nodetype() != AST_NODE_TYPE.AST_NOP)
				break;
			top = ii.next();
		}

		//----------------------------------------
		// Iterate over symtab. 
		// Add vardecl for each symbol
		//----------------------------------------
		java.util.Iterator<gm_symtab_entry> i;
		for (i = Vs.iterator(); i.hasNext();) // scalar
		{
			gm_symtab_entry e = i.next();
			ast_typedecl type = e.getType().copy();
			ast_id id = e.getId().copy(true);

			ast_vardecl v = ast_vardecl.new_vardecl(type, id);

			if (id.is_instantly_assigned())
				continue; //we throw away the vardecl here, because we will declare it later at the assignment

			if (top == null)
				GlobalMembersGm_transform_helper.gm_insert_sent_begin_of_sb(sb, v, GlobalMembersGm_transform_helper.GM_NOFIX_SYMTAB);
			else
			{
				GlobalMembersGm_transform_helper.gm_add_sent_after(top, v, GlobalMembersGm_transform_helper.GM_NOFIX_SYMTAB);
			}
			top = v;
		}

		for (i = Fs.iterator(); i.hasNext();) // field
		{
			gm_symtab_entry e = i.next();
			ast_typedecl type = e.getType().copy();
			ast_id id = e.getId().copy(true);

			ast_vardecl v = ast_vardecl.new_vardecl(type, id);
			assert v.get_idlist().get_item(0).getSymInfo() != null;
			if (top == null)
				GlobalMembersGm_transform_helper.gm_insert_sent_begin_of_sb(sb, v, GlobalMembersGm_transform_helper.GM_NOFIX_SYMTAB);
			else
			{
				GlobalMembersGm_transform_helper.gm_add_sent_after(top, v, GlobalMembersGm_transform_helper.GM_NOFIX_SYMTAB);
			}
			top = v;
		}
		return true;
	}

	public final void do_restore(ast_procdef p)
	{
		set_all(false);
		set_for_sent(true);
		GlobalMembersGm_traverse.gm_traverse_sents(p, this, GlobalMembersGm_traverse.GM_POST_APPLY);
	}
}