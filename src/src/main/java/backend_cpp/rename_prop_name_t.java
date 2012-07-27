package backend_cpp;

import ast.ast_id;
import ast.ast_typedecl;

import common.GlobalMembersGm_main;
import common.gm_apply;

import frontend.SYMTAB_TYPES;
import frontend.gm_symtab_entry;

public class rename_prop_name_t extends gm_apply {

	public rename_prop_name_t() {
		set_for_symtab(true);
	}

	public final boolean apply(gm_symtab_entry e, int symtab_type) {

		if (symtab_type != SYMTAB_TYPES.GM_SYMTAB_FIELD.getValue())
			return true;

		ast_id id = e.getId();
		ast_typedecl T = e.getType();
		ast_id graph = T.get_target_graph_id();
		assert graph != null;

		// rename A(G) => G_A
		String buf = new String(new char[1024]);
		buf = String.format("%s_%s", graph.get_orgname(), id.get_genname());
		String new_name = GlobalMembersGm_main.FE.voca_temp_name(buf, null, true);
		id.set_genname(new_name);
		GlobalMembersGm_main.FE.voca_add(new_name);
		return true;
	}
}