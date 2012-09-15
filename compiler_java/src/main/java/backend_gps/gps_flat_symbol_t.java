package backend_gps;

import static inc.gps_apply_bb.GPS_TAG_BB_USAGE;
import ast.ast_extra_info;

import common.gm_apply;

import frontend.symtab_types;
import frontend.gm_symtab_entry;

//-----------------------------------------------------------------------------------
// Create a flat table, for the generation of fields in master/vertex class
//-----------------------------------------------------------------------------------

public class gps_flat_symbol_t extends gm_apply {
	public gps_flat_symbol_t(java.util.HashSet<gm_symtab_entry> s, java.util.HashSet<gm_symtab_entry> p, java.util.HashSet<gm_symtab_entry> e) {
		this.scalar = new java.util.HashSet<gm_symtab_entry>(s);
		this.prop = new java.util.HashSet<gm_symtab_entry>(p);
		this.edge_prop = new java.util.HashSet<gm_symtab_entry>(e);
		set_for_symtab(true);
	}

	@Override
	public boolean apply(gm_symtab_entry sym, symtab_types symtab_type) {
		ast_extra_info info = sym.find_info(GPS_TAG_BB_USAGE);
		if (info == null) // no information
			return true;

		gps_syminfo syminfo = (gps_syminfo) info;
		if (syminfo.is_scalar()) {
			// ignore iterator and graph
			if (sym.getType().is_graph() || sym.getType().is_node_edge_iterator()) {
				return true;
			}

			if (symtab_type == symtab_types.GM_SYMTAB_ARG) {
				syminfo.set_is_argument(true);
				scalar.add(sym);
				// } else if (syminfo->is_used_in_multiple_BB() &&
				// syminfo->is_scoped_global()) {
			} else if (syminfo.is_scoped_global()) {
				scalar.add(sym);
			} else {
				// temporary variables. can be ignored
			}
		} else {
			if (sym.getType().is_node_property()) {
				prop.add(sym);
			} else if (sym.getType().is_edge_property()) {
				edge_prop.add(sym);
			} else {
				System.out.printf("sym = %s\n", sym.getId().get_genname());
				assert false;
			}

			/*
			 * if (syminfo->is_argument()) { prop.insert(sym); } else if
			 * (syminfo->is_used_in_multiple_BB()){ prop.insert(sym); } else {
			 * //assert(false); prop.insert(sym); }
			 */
		}

		return true;
	}

	private java.util.HashSet<gm_symtab_entry> scalar;
	private java.util.HashSet<gm_symtab_entry> prop;
	private java.util.HashSet<gm_symtab_entry> edge_prop;
}