package backend_gps;

import inc.gps_apply_bb;

//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define TO_STR(X) #X
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define DEF_STRING(X) static const char *X = "X"
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP(CLASS, DESC) class CLASS : public gm_compile_step { private: CLASS() {set_description(DESC);}public: virtual void process(ast_procdef*p); virtual gm_compile_step* get_instance(){return new CLASS();} static gm_compile_step* get_factory(){return new CLASS();} };
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP_FACTORY(CLASS) CLASS::get_factory()

//------------------------------------------------
// Traverse BB DAG
// Push visited BB nodes into the list
//------------------------------------------------
public class gps_get_reachable_bb_list_t extends gps_apply_bb
{
	public gps_get_reachable_bb_list_t(java.util.LinkedList<gm_gps_basic_block> bb_blocks)
	{
		this.blist = new java.util.LinkedList<gm_gps_basic_block>(bb_blocks);
		blist.clear();
	}

	@Override
	public void apply(gm_gps_basic_block b)
	{
		blist.addLast(b);
	}
	public java.util.LinkedList<gm_gps_basic_block> blist;
}