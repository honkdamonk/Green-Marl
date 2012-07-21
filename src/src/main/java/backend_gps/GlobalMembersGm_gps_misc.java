package backend_gps;

import inc.gps_apply_bb;
import inc.gps_apply_bb_ast;

public class GlobalMembersGm_gps_misc
{
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define TO_STR(X) #X
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define DEF_STRING(X) static const char *X = "X"
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define GM_COMPILE_STEP(CLASS, DESC) class CLASS : public gm_compile_step { private: CLASS() {set_description(DESC);}public: virtual void process(ast_procdef*p); virtual gm_compile_step* get_instance(){return new CLASS();} static gm_compile_step* get_factory(){return new CLASS();} };
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define GM_COMPILE_STEP_FACTORY(CLASS) CLASS::get_factory()

	//extern void gm_flush_reproduce();
	//extern void gm_newline_reproduce();
	//extern void gm_push_reproduce(tangible.RefObject<String> s);

	// depth-first recurse
	public static void bb_apply_recurse(java.util.HashSet<gm_gps_basic_block> set, gm_gps_basic_block B, gps_apply_bb apply)
	{
		apply.apply(B);
		set.add(B);
		for (int i = 0; i < B.get_num_exits(); i++)
		{
			gm_gps_basic_block b = B.get_nth_exit(i);

			if (set.find(b) == set.end())
			{
				GlobalMembersGm_gps_misc.bb_apply_recurse(set, b, apply);
			}
		}
	}

	// return or of has_changed
	public static void gps_bb_apply_only_once(gm_gps_basic_block entry, gps_apply_bb apply)
	{
		java.util.HashSet<gm_gps_basic_block> set = new java.util.HashSet<gm_gps_basic_block>();
		set.clear();
		GlobalMembersGm_gps_misc.bb_apply_recurse(set, entry, apply);
	}

	public static boolean gps_bb_apply_until_no_change(gm_gps_basic_block entry, gps_apply_bb apply)
	{
		java.util.HashSet<gm_gps_basic_block> set = new java.util.HashSet<gm_gps_basic_block>();
		boolean b = false;
		do
		{
			apply.set_changed(false);
			set.clear();
			GlobalMembersGm_gps_misc.bb_apply_recurse(set, entry, apply);
			if (apply.has_changed())
				b = true;
		} while (apply.has_changed());

		return b; // return true if changed at least once
	}

	public static void gps_bb_print_all(gm_gps_basic_block entry) // return or of has_changed
	{
		gps_print_apply G = new gps_print_apply();
		GlobalMembersGm_gps_misc.gps_bb_apply_only_once(entry, G);
	}

	public static void gps_bb_traverse_ast(gm_gps_basic_block entry, gps_apply_bb_ast apply, boolean is_post, boolean is_pre)
	{
		apply.set_is_post(is_post);
		apply.set_is_pre(is_pre);

		// apply it once
		GlobalMembersGm_gps_misc.gps_bb_apply_only_once(entry, apply);

	}

	public static void gps_bb_traverse_ast_single(gm_gps_basic_block entry, gps_apply_bb_ast apply, boolean is_post, boolean is_pre)
	{
		apply.set_is_post(is_post);
		apply.set_is_pre(is_pre);
		apply.apply(entry);
	}
}