package backend_gps;

import frontend.GlobalMembersGm_rw_analysis;
import frontend.gm_symtab_entry;
import inc.gm_compile_step;

import java.util.HashMap;
import java.util.HashSet;

import ast.ast_foreach;
import ast.ast_procdef;
import ast.ast_sentblock;

import common.gm_add_symbol;
import common.gm_main;
import common.gm_replace_sym_access;

public class gm_gps_opt_insert_temp_property extends gm_compile_step {
	private gm_gps_opt_insert_temp_property() {
		set_description("(Pre-Flip) Replacing scalars with temp-properties");
	}

	public void process(ast_procdef p) {
		// -------------------------------------
		// Find nested loops
		// -------------------------------------
		HashMap<ast_foreach, ast_foreach> MAP = new HashMap<ast_foreach, ast_foreach>();
		BackendGpsGlobal.gm_gps_find_double_nested_loops(p, MAP);

		// -------------------------------------
		// Find scalar targets
		// -------------------------------------
		gps_opt_find_scalar_replace_target_t T = new gps_opt_find_scalar_replace_target_t(MAP);
		p.traverse_both(T);

		// -------------------------------------
		// - Define temporary symbol
		// - Replace accesses
		// - Remove symbol
		// -------------------------------------
		HashMap<gm_symtab_entry, ast_foreach> MAP2 = T.get_target_syms_and_outer_loop();
		for (gm_symtab_entry sym : MAP2.keySet()) {
			ast_foreach out_loop = MAP2.get(sym);

			// scope where the temp property will be defined
			ast_sentblock sb = gm_add_symbol.gm_find_upscope(out_loop);
			assert sb != null;

			String temp_name = gm_main.FE.voca_temp_name_and_add(sym.getId().get_orgname(), "prop", null, true);
			tangible.RefObject<String> tempRef_temp_name = new tangible.RefObject<String>(temp_name);
			gm_symtab_entry temp_prop = gm_add_symbol.gm_add_new_symbol_property(sb, sym.getType().getTypeSummary(), true, out_loop.get_iterator()
					.getTypeInfo().get_target_graph_sym(), tempRef_temp_name);
			temp_name = tempRef_temp_name.argvalue;

			// replace accesses:
			// sym ==> out_iter.temp_prop
			gm_replace_sym_access.gm_replace_symbol_access_scalar_field(out_loop, sym, out_loop.get_iterator().getSymInfo(), temp_prop);

			/*
			 * printf("target %s inside loop %s ==> %s (temp_name)\n",
			 * I->first->getId()->get_genname(),
			 * I->second->get_iterator()->get_genname(), temp_name);
			 */

			temp_name = null;
		}

		// remove old symbols
		HashSet<gm_symtab_entry> Set = new HashSet<gm_symtab_entry>();
		for (gm_symtab_entry sym : MAP2.keySet()) {
			Set.add(sym);
		}

		gm_add_symbol.gm_remove_symbols(p.get_body(), Set);

		// -------------------------------------
		// Re-do RW analysis
		// -------------------------------------
		GlobalMembersGm_rw_analysis.gm_redo_rw_analysis(p.get_body());
	}

	@Override
	public gm_compile_step get_instance() {
		return new gm_gps_opt_insert_temp_property();
	}

	public static gm_compile_step get_factory() {
		return new gm_gps_opt_insert_temp_property();
	}
}