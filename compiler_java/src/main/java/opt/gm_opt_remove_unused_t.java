package opt;

import java.util.HashSet;
import java.util.Iterator;

import common.gm_apply;

import frontend.symtab_types;
import frontend.gm_symtab;
import frontend.gm_symtab_entry;

public class gm_opt_remove_unused_t extends gm_apply
{
	public gm_opt_remove_unused_t(HashSet<gm_symtab_entry> U)
	{
		this.used = new HashSet<gm_symtab_entry>(U);
		set_for_symtab(true);
	}
	
	@Override
	public boolean apply(gm_symtab tab, symtab_types symtab_type)
	{
		if (symtab_type != symtab_types.GM_SYMTAB_VAR)
			return true;

		Iterator<gm_symtab_entry> I;
		HashSet<gm_symtab_entry> to_remove = new HashSet<gm_symtab_entry>();
		HashSet<gm_symtab_entry> v = tab.get_entries();
		for (I = v.iterator(); I.hasNext();)
		{
			gm_symtab_entry z = I.next();
			if (!z.getType().is_primitive() && !z.getType().is_nodeedge())
				continue;
			if (!used.contains(z))
			{
				to_remove.add(z);
			}
		}
		for (I = to_remove.iterator(); I.hasNext();)
		{
			tab.remove_entry_in_the_tab(I.next());
		}

		return true;
	}

	private HashSet<gm_symtab_entry> used;

}