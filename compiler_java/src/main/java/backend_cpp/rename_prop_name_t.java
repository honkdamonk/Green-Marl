package backend_cpp;

import ast.ast_id;
import ast.ast_typedecl;

import common.gm_main;
import common.gm_apply;

import frontend.symtab_types;
import frontend.gm_symtab_entry;

class rename_prop_name_t extends gm_apply {

	rename_prop_name_t() {
		set_for_symtab(true);
	}

	public final boolean apply(gm_symtab_entry e, int symtab_type) {

		if (symtab_type != symtab_types.GM_SYMTAB_FIELD.getValue())
			return true;

		ast_id id = e.getId();
		ast_typedecl T = e.getType();
		ast_id graph = T.get_target_graph_id();
		assert graph != null;

		// rename A(G) => G_A
		String buf = String.format("%s_%s", graph.get_orgname(), id.get_genname());
		String new_name = gm_main.FE.voca_temp_name(buf, null, true);
		id.set_genname(new_name);
		gm_main.FE.voca_add(new_name);
		return true;
	}
}