package common;

import ast.ast_id;
import inc.gm_procinfo;
import frontend.symtab_types;
import frontend.gm_symtab_entry;

public class gm_prepare_genname_T extends gm_apply {

	private gm_vocabulary lang_voca;
	private gm_procinfo proc_info;

	public gm_prepare_genname_T(gm_procinfo pi, gm_vocabulary v) {
		lang_voca = v;
		proc_info = pi;
		set_for_symtab(true);
	}

	@Override
	public boolean apply(gm_symtab_entry e, symtab_types symtab_type) {
		ast_id ID = e.getId();
		String org_name = ID.get_orgname();

		final boolean TRY_ORG_NAME_FIRST = true;

		String gen_name = proc_info.generate_temp_name(org_name, lang_voca, TRY_ORG_NAME_FIRST);

		// add gen_name into proc_voca
		proc_info.add_voca(gen_name);
		ID.set_genname(gen_name);
		return true;
	}

}