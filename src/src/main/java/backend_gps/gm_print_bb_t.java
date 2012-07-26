package backend_gps;

import ast.ast_procdef;
import inc.gm_compile_step;

import common.GlobalMembersGm_main;

//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define TO_STR(X) #X
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define DEF_STRING(X) static const char *X = "X"
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP(CLASS, DESC) class CLASS : public gm_compile_step { private: CLASS() {set_description(DESC);}public: virtual void process(ast_procdef*p); virtual gm_compile_step* get_instance(){return new CLASS();} static gm_compile_step* get_factory(){return new CLASS();} };
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP_FACTORY(CLASS) CLASS::get_factory()

public class gm_print_bb_t extends gm_compile_step {
	
	@Override
	public gm_compile_step get_instance() {
		return new gm_print_bb_t();
	}

	@Override
	public void process(ast_procdef p) {
		gm_gps_beinfo info = (gm_gps_beinfo) GlobalMembersGm_main.FE.get_backend_info(p);
		if (info == null)
			return;
		if (info.get_entry_basic_block() == null)
			return;
		GlobalMembersGm_gps_misc.gps_bb_print_all(info.get_entry_basic_block());
	}
}