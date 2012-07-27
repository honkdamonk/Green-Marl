package common;

import java.util.HashSet;

public class gm_vocabulary {

	public void dispose() {
		clear();
	}

	public final void clear() {
		words.clear();
	}

	public final void add_word(String word) {
		assert word != null;
		words.add(word);
	}

	public final boolean has_word(String word) {
		return words.contains(word);
	}

	private HashSet<String> words = new HashSet<String>();
	// private HashSet<String, gm_comp_string> words = new HashSet<String, gm_comp_string>();

}
/*
 */

// C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
// /#define GM_COMPILE_STEP(CLASS, DESC) class CLASS : public gm_compile_step {
// private: CLASS() {set_description(DESC);}public: virtual void
// process(ast_procdef*p); virtual gm_compile_step* get_instance(){return new
// CLASS();} static gm_compile_step* get_factory(){return new CLASS();} };
// C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
// /#define GM_COMPILE_STEP_FACTORY(CLASS) CLASS::get_factory()

