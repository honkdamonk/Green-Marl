package frontend;

import inc.gm_prop_usage;
import inc.gm_compile_step;

import java.util.HashSet;

import ast.ast_procdef;
import ast.gm_rwinfo_map;

public class gm_fe_check_property_argument_usage extends gm_compile_step {

	private gm_fe_check_property_argument_usage() {
		set_description("Checking property usages");
	}

	@Override
	public void process(ast_procdef proc) {
		gm_symtab props = proc.get_symtab_field();
		HashSet<gm_symtab_entry> SET = props.get_entries();
		gm_rwinfo_map R = gm_rw_analysis.get_rwinfo_sets(proc.get_body()).read_set;
		gm_rwinfo_map W = gm_rw_analysis.get_rwinfo_sets(proc.get_body()).write_set;
		for (gm_symtab_entry e : SET) {
			if ((!R.containsKey(e)) && (!W.containsKey(e)))
				e.add_info_obj(gm_frontend.GMUSAGE_PROPERTY, gm_prop_usage.GMUSAGE_UNUSED);
			else if ((!R.containsKey(e)) && (W.containsKey(e)))
				e.add_info_obj(gm_frontend.GMUSAGE_PROPERTY, gm_prop_usage.GMUSAGE_OUT);
			else if ((!W.containsKey(e)) && (R.containsKey(e)))
				e.add_info_obj(gm_frontend.GMUSAGE_PROPERTY, gm_prop_usage.GMUSAGE_IN);
			else {
				e.add_info_obj(gm_frontend.GMUSAGE_PROPERTY, gm_prop_usage.GMUSAGE_INVALID); // temporary marking
			}
		}

		// now traverse the source and see if write after read
		gm_check_property_usage_t T = new gm_check_property_usage_t();
		proc.get_body().traverse_both(T);
		/*
		 * for(I=SET.begin(); I!=SET.end(); I++) { gm_symtab_entry* e = *I;
		 * assert( e->find_info_int(GMUSAGE_PROPERTY) != GMUSAGE_INVALID); //
		 * temporary marking printf("%s used as : %s\n",
		 * e->getId()->get_orgname(), (e->find_info_int(GMUSAGE_PROPERTY) ==
		 * GMUSAGE_UNUSED) ? "Unused" : (e->find_info_int(GMUSAGE_PROPERTY) ==
		 * GMUSAGE_OUT) ? "Output" : (e->find_info_int(GMUSAGE_PROPERTY) ==
		 * GMUSAGE_IN) ? "Input" : (e->find_info_int(GMUSAGE_PROPERTY) ==
		 * GMUSAGE_INOUT) ? "Inout" : "Invalid"); }
		 */
	}

	@Override
	public gm_compile_step get_instance() {
		return new gm_fe_check_property_argument_usage();
	}

	public static gm_compile_step get_factory() {
		return new gm_fe_check_property_argument_usage();
	}
	
}