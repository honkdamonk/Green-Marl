package frontend;

import ast.ast_id;
import ast.ast_procdef;

import common.GlobalMembersGm_main;
import common.gm_apply;

//---------------------------------------------------
// rename all potential name conflicts
//---------------------------------------------------
public class rename_all_t extends gm_apply {
	public rename_all_t() {
		set_for_symtab(true);
	}

	@Override
	public boolean apply(gm_symtab_entry e, SYMTAB_TYPES symtab_type) {
		ast_id id = e.getId();

		// if name is already in -> generate-new-name
		String name = id.get_orgname();

		// printf("checking :%s\n",name);
		if (GlobalMembersGm_main.FE.voca_isin((String) name)) {
			// should use a new name
			String new_name = GlobalMembersGm_main.FE.voca_temp_name(name);
			id.set_orgname(new_name); // new name is copied & old name is
										// deleted inside
			new_name = null;
		}

		// printf("adding :%s\n",id->get_orgname());
		// add to vocabulary
		GlobalMembersGm_main.FE.voca_add(id.get_orgname());
		// assert(FE.voca_isin(id->get_orgname()));
		return true;
	}

	// -------------------------------------------------
	// rename all potential name conflicts
	// -------------------------------------------------
	public final void do_rename_all_potential(ast_procdef p) {
		assert p == GlobalMembersGm_main.FE.get_current_proc();

		GlobalMembersGm_main.FE.voca_clear(); // rebuild vocaburary
		p.traverse_pre(this);
	}
}