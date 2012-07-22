package common;

import ast.ast_node;
import frontend.gm_scope;

public class gm_reconstruct_scope_t extends gm_apply
{
	public gm_reconstruct_scope_t(ast_node n)
	{
		set_for_sent(true);
		S.push_symtabs(n.get_symtab_var(), n.get_symtab_field(), n.get_symtab_proc());
		top = n;
	}

	@Override
	public void begin_context(ast_node n)
	{
		if (n != top)
		{
			n.get_symtab_var().set_parent(S.get_varsyms());
			n.get_symtab_field().set_parent(S.get_fieldsyms());
			n.get_symtab_proc().set_parent(S.get_procsyms());
		}
		S.push_symtabs(n.get_symtab_var(), n.get_symtab_field(), n.get_symtab_proc());

	}
	@Override
	public void end_context(ast_node n)
	{
		S.pop_symtabs();
	}
	private gm_scope S = new gm_scope();
	private ast_node top;

}