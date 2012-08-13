package common;

import frontend.gm_scope;

// (scope -> NULL)
public class ripoff_upper_scope extends replace_upper_scope
{
	public gm_scope N = new gm_scope();
	public ripoff_upper_scope()
	{
		set_for_symtab(true);
		N.push_symtabs(null, null, null);
		set_new_scope(N);
	}
	public final void set_scope_to_remove(gm_scope s)
	{
		set_old_scope(s);
	}
}