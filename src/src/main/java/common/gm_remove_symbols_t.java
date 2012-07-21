import frontend.gm_symtab;
import frontend.gm_symtab_entry;

//---------------------------------------------------------------------------
// remove set of symbols
//---------------------------------------------------------------------------
public class gm_remove_symbols_t extends gm_apply
{
	public gm_remove_symbols_t(java.util.HashSet<gm_symtab_entry> S)
	{
		this.TARGETS = new java.util.HashSet<gm_symtab_entry>(S);
		set_for_symtab(true);
	}

	@Override
	public boolean apply(gm_symtab e, int symtab_sype)
	{
		java.util.Iterator<gm_symtab_entry> T;
		for (T = TARGETS.iterator(); T.hasNext();)
		{
			gm_symtab_entry t = T.next();
			if (e.is_entry_in_the_tab(t))
			{
				e.remove_entry_in_the_tab(t);
			}
		}

		return true;
	}

	private java.util.HashSet<gm_symtab_entry> TARGETS = new java.util.HashSet<gm_symtab_entry>();
}