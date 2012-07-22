package common;

import ast.ast_id;
import inc.gm_procinfo;
import frontend.gm_symtab_entry;

//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define TO_STR(X) #X
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define DEF_STRING(X) static const char *X = "X"
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP(CLASS, DESC) class CLASS : public gm_compile_step { private: CLASS() {set_description(DESC);}public: virtual void process(ast_procdef*p); virtual gm_compile_step* get_instance(){return new CLASS();} static gm_compile_step* get_factory(){return new CLASS();} };
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP_FACTORY(CLASS) CLASS::get_factory()

public class gm_prepare_genname_T extends gm_apply
{
	public gm_prepare_genname_T(gm_procinfo pi, gm_vocabulary v)
	{
		lang_voca = v;
		proc_info = pi;
		set_for_symtab(true);
	}

	@Override
	public boolean apply(gm_symtab_entry e, int symtab_type)
	{
		ast_id ID = e.getId();
//C++ TO JAVA CONVERTER TODO TASK: Java does not have an equivalent for pointers to value types:
//ORIGINAL LINE: sbyte* org_name = ID->get_orgname();
		byte org_name = ID.get_orgname();

		final boolean TRY_ORG_NAME_FIRST = true;

//C++ TO JAVA CONVERTER TODO TASK: Java does not have an equivalent for pointers to value types:
//ORIGINAL LINE: sbyte* gen_name = proc_info->generate_temp_name(org_name, lang_voca, TRY_ORG_NAME_FIRST);
		byte gen_name = proc_info.generate_temp_name(org_name, lang_voca, TRY_ORG_NAME_FIRST);

		// add gen_name into proc_voca
		tangible.RefObject<String> tempRef_gen_name = new tangible.RefObject<String>(gen_name);
		proc_info.add_voca(tempRef_gen_name);
		gen_name = tempRef_gen_name.argvalue;
		ID.set_genname(gen_name);
		return true;
	}

	private gm_vocabulary lang_voca;
	private gm_procinfo proc_info;
}