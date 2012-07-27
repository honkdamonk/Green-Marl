package backend_cpp;

import ast.ast_id;
import ast.ast_typedecl;

import common.GlobalMembersGm_main;
import common.gm_apply;

import frontend.SYMTAB_TYPES;
import frontend.gm_symtab_entry;

//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define TO_STR(X) #X
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define DEF_STRING(X) static const char *X = "X"
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP(CLASS, DESC) class CLASS : public gm_compile_step { private: CLASS() {set_description(DESC);}public: virtual void process(ast_procdef*p); virtual gm_compile_step* get_instance(){return new CLASS();} static gm_compile_step* get_factory(){return new CLASS();} };
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP_FACTORY(CLASS) CLASS::get_factory()
///#include "gm_argopts.h"

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
		// C++ TO JAVA CONVERTER TODO TASK: Java does not have an equivalent for
		// pointers to value types:
		// ORIGINAL LINE: sbyte* new_name = FE.voca_temp_name(buf, null, true);
		String new_name = GlobalMembersGm_main.FE.voca_temp_name(buf, null, true);
		id.set_genname(new_name);
		tangible.RefObject<String> tempRef_new_name = new tangible.RefObject<String>(new_name);
		GlobalMembersGm_main.FE.voca_add(tempRef_new_name);
		new_name = tempRef_new_name.argvalue;
		return true;
	}
}