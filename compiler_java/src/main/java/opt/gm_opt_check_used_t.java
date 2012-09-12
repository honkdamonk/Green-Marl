package opt;

import java.util.HashSet;

import ast.ast_id;

import common.gm_apply;

import frontend.gm_symtab_entry;

//-------------------------------------------------------------------
//  Remove unused scalar variables
//  targets:
//    type - primitive or node or edge
//    is not iterator
//    is not used at all
//-------------------------------------------------------------------

public class gm_opt_check_used_t extends gm_apply
{
	public gm_opt_check_used_t()
	{
		set_for_id(true);
	}
	
	@Override
	public boolean apply(ast_id i)
	{
		gm_symtab_entry z = i.getSymInfo();
		assert z != null;
		used.add(z);
		return true;
	}

	public final HashSet<gm_symtab_entry> get_used_set()
	{
		return used;
	}

	private HashSet<gm_symtab_entry> used = new HashSet<gm_symtab_entry>();

}