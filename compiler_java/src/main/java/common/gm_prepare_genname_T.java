package common;

import ast.ast_id;
import inc.gm_procinfo;
import frontend.SYMTAB_TYPES;
import frontend.gm_symtab_entry;

public class gm_prepare_genname_T extends gm_apply {
	public gm_prepare_genname_T(gm_procinfo pi, gm_vocabulary v) {
		lang_voca = v;
		proc_info = pi;
		set_for_symtab(true);
	}

	@Override
	public boolean apply(gm_symtab_entry e, SYMTAB_TYPES symtab_type) {
		ast_id ID = e.getId();
		// C++ TO JAVA CONVERTER TODO TASK: Java does not have an equivalent for
		// pointers to value types:
		// ORIGINAL LINE: sbyte* org_name = ID->get_orgname();
		String org_name = ID.get_orgname();

		final boolean TRY_ORG_NAME_FIRST = true;

		// C++ TO JAVA CONVERTER TODO TASK: Java does not have an equivalent for
		// pointers to value types:
		// ORIGINAL LINE: sbyte* gen_name =
		// proc_info->generate_temp_name(org_name, lang_voca,
		// TRY_ORG_NAME_FIRST);
		String gen_name = proc_info.generate_temp_name(org_name, lang_voca, TRY_ORG_NAME_FIRST);

		// add gen_name into proc_voca
		proc_info.add_voca(gen_name);
		ID.set_genname(gen_name);
		return true;
	}

	private gm_vocabulary lang_voca;
	private gm_procinfo proc_info;
}