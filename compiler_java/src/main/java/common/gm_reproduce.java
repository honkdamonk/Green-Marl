package common;

import inc.gm_code_writer;
import backend_cpp.FILE;

public class gm_reproduce {
	// C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced
	// in-line:
	// /#define TO_STR(X) #X
	// C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced
	// in-line:
	// /#define DEF_STRING(X) static const char *X = "X"
	// C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced
	// in-line:
	// /#define GM_COMPILE_STEP(CLASS, DESC) class CLASS : public
	// gm_compile_step { private: CLASS() {set_description(DESC);}public:
	// virtual void process(ast_procdef*p); virtual gm_compile_step*
	// get_instance(){return new CLASS();} static gm_compile_step*
	// get_factory(){return new CLASS();} };
	// C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced
	// in-line:
	// /#define GM_COMPILE_STEP_FACTORY(CLASS) CLASS::get_factory()

	public static gm_code_writer Out = new gm_code_writer();

	public static void gm_flush_reproduce() {
		Out.flush();
	}

	public static void gm_newline_reproduce() {
		Out.NL();
	}

	public static void gm_redirect_reproduce(FILE f) {
		Out.setOutputFile(f);
	}

	public static void gm_baseindent_reproduce(int i) {
		Out.setBaseIndent(i);
	}

	public static void gm_push_reproduce(String s) {
		Out.push(s);
	}
}
// ----------------------------------------------------------------------------------------
// For debugging.
// : Reproduce parsed input into GM text
// ----------------------------------------------------------------------------------------

