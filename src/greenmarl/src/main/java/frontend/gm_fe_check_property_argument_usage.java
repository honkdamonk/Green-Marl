package frontend;

import ast.ast_procdef;
import inc.GM_PROP_USAGE_T;
import inc.gm_compile_step;

public class gm_fe_check_property_argument_usage extends gm_compile_step
{
	private gm_fe_check_property_argument_usage()
	{
		set_description("Checking property usages");
	}
	public void process(ast_procdef proc)
	{
		gm_symtab props = proc.get_symtab_field();
		java.util.HashSet<gm_symtab_entry> SET = props.get_entries();
		java.util.Iterator<gm_symtab_entry> I;
		java.util.HashSet<gm_symtab_entry> write_or_read_write = new java.util.HashSet<gm_symtab_entry>();
		java.util.HashMap<gm_symtab_entry, java.util.LinkedList<gm_rwinfo>> R = GlobalMembersGm_rw_analysis.get_rwinfo_sets(proc.get_body()).read_set;
		java.util.HashMap<gm_symtab_entry, java.util.LinkedList<gm_rwinfo>> W = GlobalMembersGm_rw_analysis.get_rwinfo_sets(proc.get_body()).write_set;
		for (I = SET.iterator(); I.hasNext();)
		{
			gm_symtab_entry e = I.next();
			if ((!R.containsKey(e)) && (!W.containsKey(e)))
				e.add_info_int(GlobalMembersGm_frontend.GMUSAGE_PROPERTY, GM_PROP_USAGE_T.GMUSAGE_UNUSED);
			else if ((!R.containsKey(e)) && (W.containsKey(e)))
				e.add_info_int(GlobalMembersGm_frontend.GMUSAGE_PROPERTY, GM_PROP_USAGE_T.GMUSAGE_OUT);
			else if ((!W.containsKey(e)) && (R.containsKey(e)))
				e.add_info_int(GlobalMembersGm_frontend.GMUSAGE_PROPERTY, GM_PROP_USAGE_T.GMUSAGE_IN);
			else
			{
				e.add_info_int(GlobalMembersGm_frontend.GMUSAGE_PROPERTY, GM_PROP_USAGE_T.GMUSAGE_INVALID); // temporary marking
			}
		}
    
		// now traverse the source and see if write after read
		gm_check_property_usage_t T = new gm_check_property_usage_t();
		proc.get_body().traverse_both(T);
		/*
		 for(I=SET.begin(); I!=SET.end(); I++)
		 {
		 gm_symtab_entry* e = *I;
		 assert( e->find_info_int(GMUSAGE_PROPERTY) != GMUSAGE_INVALID); // temporary marking
		 printf("%s used as : %s\n", e->getId()->get_orgname(),
		 (e->find_info_int(GMUSAGE_PROPERTY) == GMUSAGE_UNUSED) ? "Unused" :
		 (e->find_info_int(GMUSAGE_PROPERTY) == GMUSAGE_OUT)    ? "Output" :
		 (e->find_info_int(GMUSAGE_PROPERTY) == GMUSAGE_IN)     ? "Input" :
		 (e->find_info_int(GMUSAGE_PROPERTY) == GMUSAGE_INOUT) ? "Inout" : "Invalid");
		 }
		 */
	}
	@Override
	public gm_compile_step get_instance()
	{
		return new gm_fe_check_property_argument_usage();
	}
	public static gm_compile_step get_factory()
	{
		return new gm_fe_check_property_argument_usage();
	}
}