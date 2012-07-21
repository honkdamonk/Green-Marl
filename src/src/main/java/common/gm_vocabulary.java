public class gm_vocabulary
{

	public void dispose()
	{
		clear();
	}

	public final void clear()
	{
		java.util.Iterator<String, gm_comp_string> I;
		for (I = words.iterator(); I.hasNext();)
			I.next() = null;
		words.clear();
	}
	public final void add_word(String w)
	{
		add_word((String) w);
	}
	public final void add_word(tangible.RefObject<String> w)
	{
//C++ TO JAVA CONVERTER TODO TASK: Java does not have an equivalent for pointers to value types:
//ORIGINAL LINE: sbyte* dup = gm_strdup(w);
		byte dup = GlobalMembersGm_misc.gm_strdup(w.argvalue);
		words.add((String) dup);
	}
	public final boolean has_word(tangible.RefObject<String> w)
	{
		return (words.find((String) w.argvalue).hasNext());
	}

	private java.util.HashSet<String, gm_comp_string> words = new java.util.HashSet<String, gm_comp_string>();

}
/*
 */



//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP(CLASS, DESC) class CLASS : public gm_compile_step { private: CLASS() {set_description(DESC);}public: virtual void process(ast_procdef*p); virtual gm_compile_step* get_instance(){return new CLASS();} static gm_compile_step* get_factory(){return new CLASS();} };
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP_FACTORY(CLASS) CLASS::get_factory()

