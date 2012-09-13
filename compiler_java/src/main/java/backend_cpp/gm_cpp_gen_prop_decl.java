package backend_cpp;

import inc.gm_compile_step;
import ast.ast_procdef;

import common.gm_traverse;

class gm_cpp_gen_prop_decl extends gm_compile_step {
	
	private gm_cpp_gen_prop_decl() {
		set_description("Check property declaration");
	}

	public void process(ast_procdef proc) {
		property_decl_check_t T1 = new property_decl_check_t();
		gm_traverse.gm_traverse_sents(proc.get_body(), T1);

		proc.add_info_bool(gm_cpp_gen.CPPBE_INFO_HAS_PROPDECL, T1.has_prop_decl);

		proc.get_body().add_info_bool(gm_cpp_gen.CPPBE_INFO_IS_PROC_ENTRY, true);

	}

	@Override
	public gm_compile_step get_instance() {
		return new gm_cpp_gen_prop_decl();
	}

	public static gm_compile_step get_factory() {
		return new gm_cpp_gen_prop_decl();
	}
}