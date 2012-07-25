package backend_gps;

import inc.GM_PROP_USAGE_T;
import inc.GlobalMembersGm_backend_gps;
import inc.gm_compile_step;
import ast.ast_procdef;

import common.GlobalMembersGm_main;

import frontend.GlobalMembersGm_frontend;
import frontend.gm_symtab_entry;

public class gm_gps_opt_analyze_symbol_summary extends gm_compile_step {
	private gm_gps_opt_analyze_symbol_summary() {
		set_description("Create Symbol Summary");
	}

	public void process(ast_procdef p) {

		// -----------------------------------------------
		// mark special markers to the property arguments
		// -----------------------------------------------
		java.util.HashSet<gm_symtab_entry> args = p.get_symtab_field().get_entries();
		for (gm_symtab_entry sym : args) {
			gps_syminfo syminfo = (gps_syminfo) sym.find_info(GlobalMembersGps_syminfo.GPS_TAG_BB_USAGE);
			assert syminfo != null;
			syminfo.set_is_argument(true);
		}

		// -------------------------------------
		// make a flat symbol table
		// -------------------------------------
		gm_gps_beinfo beinfo = (gm_gps_beinfo) GlobalMembersGm_main.FE.get_backend_info(p);

		java.util.HashSet<gm_symtab_entry> prop = beinfo.get_node_prop_symbols();
		java.util.HashSet<gm_symtab_entry> e_prop = beinfo.get_edge_prop_symbols();
		gps_flat_symbol_t T = new gps_flat_symbol_t(beinfo.get_scalar_symbols(), prop, e_prop);
		p.traverse(T, false, true);

		// -----------------------------------------------------------
		// Enlist property symbols (todo: opt ordering for cacheline ?)
		// -----------------------------------------------------------
		beinfo.set_total_node_property_size(GlobalMembersGm_gps_sym_analyze_create_summary.comp_start_byte(prop));
		beinfo.set_total_edge_property_size(GlobalMembersGm_gps_sym_analyze_create_summary.comp_start_byte(e_prop));

		beinfo.compute_max_communication_size();

		// --------------------------------------------------------
		// check if input node parsing parsing is required
		// --------------------------------------------------------
		gm_gps_beinfo info = (gm_gps_beinfo) GlobalMembersGm_main.FE.get_current_backend_info();
		boolean need_node_prop_init = false;
		for (gm_symtab_entry e : prop) {
			if ((e.find_info_int(GlobalMembersGm_frontend.GMUSAGE_PROPERTY) == GM_PROP_USAGE_T.GMUSAGE_IN.getValue())
					|| (e.find_info_int(GlobalMembersGm_frontend.GMUSAGE_PROPERTY) == GM_PROP_USAGE_T.GMUSAGE_INOUT.getValue())) {
				/*
				 * printf("in/inout -> %s :%d\n", e->getId()->get_genname(),
				 * e->find_info_int(GMUSAGE_PROPERTY) );
				 */
				need_node_prop_init = true;
			}
		}

		ast_procdef proc = GlobalMembersGm_main.FE.get_current_proc();
		proc.add_info_bool(GlobalMembersGm_backend_gps.GPS_FLAG_NODE_VALUE_INIT, need_node_prop_init);

		set_okay(true);
	}

	@Override
	public gm_compile_step get_instance() {
		return new gm_gps_opt_analyze_symbol_summary();
	}

	public static gm_compile_step get_factory() {
		return new gm_gps_opt_analyze_symbol_summary();
	}
}