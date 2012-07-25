package common;

public class GlobalMembersGm_argopts {
	
	public static final int GMARG_NULL = 0;
	public static final int GMARG_STRING = 1;
	public static final int GMARG_INT = 2;
	public static final int GMARG_BOOL = 3;

	public static String GMARGFLAG_HELP = "h";
	public static String GMARGFLAG_VERSION = "v";
	public static String GMARGFLAG_TARGET = "t";
	public static String GMARGFLAG_OUTDIR = "o";
	public static String GMARGFLAG_OPT_LEV = "O";
	public static String GMARGFLAG_VERB_LEV = "V";

	// static const char* GMARGFLAG_STOP_MAJOR = "DS";
	// static const char* GMARGFLAG_STOP_MINOR = "Ds";
	public static String GMARGFLAG_STOP_STRING = "DS";
	public static String GMARGFLAG_REPRODUCE = "Dr";
	public static String GMARGFLAG_PRINTRW = "Dw";
	public static String GMARGFLAG_PRINTBB = "Db";
	public static String GMARGFLAG_FLIP_REVERSE = "FlipRev";
	public static String GMARGFLAG_FLIP_PULL = "FlipPull";
	public static String GMARGFLAG_FLIP_BFSUP = "FlipUp";

	public static String GMARGFLAG_MERGE_BB = "GPSMerge";
	public static String GMARGFLAG_MERGE_BB_INTRA = "GPSMergeIntra";
	// static const char* GMARGFLAG_DUMPIR = "Dd";
	// static const char* GMARGFLAG_NOMERGE = "NoMerge";
	// static const char* GMARGFLAG_NOSCREDUCE = "NoScalarReduce";

	// Add compiler options here
	public static GM_comp_args[] GM_compiler_options = { //
			new GM_comp_args(GMARGFLAG_HELP, GMARG_NULL, "Print help messages", null), 
			new GM_comp_args(GMARGFLAG_VERSION, GMARG_NULL, "Print version", null),
			new GM_comp_args(GMARGFLAG_TARGET, GMARG_STRING, "Target backend. Current valid targets are 'cpp_omp', 'cpp_seq', 'gps' and 'giraph'.", "cpp_omp"),
			new GM_comp_args(GMARGFLAG_OUTDIR, GMARG_STRING, "Output directory.", "."),
			new GM_comp_args(GMARGFLAG_OPT_LEV, GMARG_INT, "Optimization Level (tobe implemented)", "1"),
			new GM_comp_args(GMARGFLAG_VERB_LEV, GMARG_INT, "Verbosity Level 0:silent, 1:show compile stage", "0"),
			new GM_comp_args(GMARGFLAG_FLIP_BFSUP, GMARG_BOOL, "Enable 'flipping edges for BFS Up-nbrs'", "1"),
			new GM_comp_args(GMARGFLAG_FLIP_REVERSE, GMARG_BOOL, "Enable 'flipping edges' to avoid the use of reverse edges", "0"),
			new GM_comp_args(GMARGFLAG_FLIP_PULL, GMARG_BOOL, "Enable 'flipping edges' to avoid the use of pull-based computation", "0"),
			new GM_comp_args(GMARGFLAG_MERGE_BB, GMARG_BOOL, "(For gps) Enable bb merge optimization", "1"),
			new GM_comp_args(GMARGFLAG_MERGE_BB_INTRA, GMARG_BOOL, "(For gps) Enable intra-loop bb merge optimization", "1"),
			new GM_comp_args(GMARGFLAG_STOP_STRING, GMARG_STRING, "(For debug) Stop the compiler after certain stage. <string>=(step)[.(step)]", "0"),
			new GM_comp_args(GMARGFLAG_REPRODUCE, GMARG_BOOL, "(For debug) When stopped, reproduce green-marl program", "1"),
			new GM_comp_args(GMARGFLAG_PRINTRW, GMARG_BOOL, "(For debug) When stopped, print rw analysis information", "0"),
			new GM_comp_args(GMARGFLAG_PRINTBB, GMARG_BOOL, "(For gps-debug) When stopped, print basicblock information", "0") //
	};

	// C++ TO JAVA CONVERTER NOTE: 'extern' variable declarations are not
	// required in Java:
	// extern gm_userargs OPTIONS;

	// C++ TO JAVA CONVERTER NOTE: 'extern' variable declarations are not
	// required in Java:
	// extern gm_userargs OPTIONS;
	// C++ TO JAVA CONVERTER NOTE: 'extern' variable declarations are not
	// required in Java:
	// extern java.util.LinkedList<sbyte*> GM_input_lists;

	public static void print_help(tangible.RefObject<String> bin_name) {
		System.out.printf("Usage: %s [options] input_file\n", bin_name.argvalue);
		System.out.print("Options:\n");
		// C++ TO JAVA CONVERTER WARNING: This 'sizeof' ratio was replaced with
		// a direct reference to the array length:
		// ORIGINAL LINE: int s = sizeof(GM_compiler_options) /
		// sizeof(GM_comp_args);
		int s = GM_compiler_options.length;
		for (int i = 0; i < s; i++) {
			int t = 0;
			t += System.out.printf("  -%s", GM_compiler_options[i].name);
			if (GM_compiler_options[i].arg_type == GMARG_NULL) {
				// do nothing
			} else if (GM_compiler_options[i].arg_type == GMARG_BOOL) {
				t += System.out.print("[=0/1]");
			} else if (GM_compiler_options[i].arg_type == GMARG_STRING) {
				t += System.out.print("=<string>");
			} else if (GM_compiler_options[i].arg_type == GMARG_INT) {
				t += System.out.print("=<int>");
			}
			for (; t < 24; t++)
				System.out.print(" ");
			System.out.printf("%s", GM_compiler_options[i].help_string);
			if (GM_compiler_options[i].arg_type != GMARG_NULL) {
				System.out.printf(" (Default is %s)", GM_compiler_options[i].def_value);
			}
			System.out.print("\n");
		}
	}

	public static void process_nullargs(tangible.RefObject<String> c, tangible.RefObject<String> bin_name) {
		if (GlobalMembersGm_misc.gm_is_same_string(c.argvalue, "h")) {
			tangible.RefObject<String> tempRef_bin_name = bin_name;
			GlobalMembersGm_argopts.print_help(tempRef_bin_name);
			bin_name.argvalue = tempRef_bin_name.argvalue;
			System.exit(0);
		} else if (GlobalMembersGm_misc.gm_is_same_string(c.argvalue, "v")) {
			System.out.printf("version %s\n", GlobalMembersGm_version_string.gm_version_string);
			System.exit(0);
		}
		// add here
	}

	public static void parse_arg(tangible.RefObject<String> argv, tangible.RefObject<String> bin_name) {
		// C++ TO JAVA CONVERTER WARNING: This 'sizeof' ratio was replaced with
		// a direct reference to the array length:
		// ORIGINAL LINE: int s = sizeof(GM_compiler_options) /
		// sizeof(GM_comp_args);
		int s = GM_compiler_options.length;
		if (argv.argvalue.charAt(0) == '-') {
			// search '=' in the argument
			int z = argv.argvalue.length();
			String key_begin = argv.argvalue.charAt(1);
			String val_begin = null;
			for (int i = 1; i <= z; i++) {
				if (argv.argvalue.charAt(i) == '=') {
					argv.argvalue = tangible.StringFunctions.changeCharacter(argv.argvalue, i, '\0');
					val_begin = (argv.argvalue.charAt(i + 1));
					break;
				}
			}
			// ------------------------------
			// find matching key
			// ------------------------------
			int i;
			for (i = 0; i < s; i++) {
				GM_comp_args t = GM_compiler_options[i];
				if (!GlobalMembersGm_misc.gm_is_same_string(t.name, key_begin))
					continue;

				if (t.arg_type == GMARG_NULL) {
					tangible.RefObject<String> tempRef_key_begin = new tangible.RefObject<String>(key_begin);
					tangible.RefObject<String> tempRef_bin_name = bin_name;
					GlobalMembersGm_argopts.process_nullargs(tempRef_key_begin, tempRef_bin_name);
					key_begin = tempRef_key_begin.argvalue;
					bin_name.argvalue = tempRef_bin_name.argvalue;
				} else if (t.arg_type == GMARG_STRING) {
					if (val_begin == null)
						val_begin = (String) "";
					GlobalMembersGm_main.OPTIONS.set_arg_string(key_begin, val_begin);
				} else if (t.arg_type == GMARG_INT) {
					if (val_begin == null)
						GlobalMembersGm_main.OPTIONS.set_arg_int(key_begin, 0);
					else
						GlobalMembersGm_main.OPTIONS.set_arg_int(key_begin, Integer.parseInt(val_begin));
				} else if (t.arg_type == GMARG_BOOL) {
					if (val_begin == null)
						GlobalMembersGm_main.OPTIONS.set_arg_bool(key_begin, true);
					else {
						GlobalMembersGm_main.OPTIONS.set_arg_bool(key_begin, Integer.parseInt(val_begin) == 0 ? false : true);
					}
				} else {
					assert false;
				}
				break;
			}
			if (i == s) {
				System.out.printf("ignoring unknown option: %s\n", key_begin);
			}
		} else {
			GlobalMembersGm_main.GM_input_lists.addLast(argv.argvalue);
		}
	}

	public static void process_args(int argc, tangible.RefObject<String[]> argv) {
		// set default values
		// C++ TO JAVA CONVERTER WARNING: This 'sizeof' ratio was replaced with
		// a direct reference to the array length:
		// ORIGINAL LINE: int s = sizeof(GM_compiler_options) /
		// sizeof(GM_comp_args);
		int s = GM_compiler_options.length;
		for (int i = 0; i < s; i++) {
			GM_comp_args t = GM_compiler_options[i];
			if (t.def_value == null)
				continue;
			else if (t.arg_type == GMARG_NULL) {
				continue;
			} else if (t.arg_type == GMARG_STRING) {
				GlobalMembersGm_main.OPTIONS.set_arg_string(t.name, t.def_value);
			} else if (t.arg_type == GMARG_INT) {
				GlobalMembersGm_main.OPTIONS.set_arg_int(t.name, Integer.parseInt(t.def_value));
			} else if (t.arg_type == GMARG_BOOL) {
				GlobalMembersGm_main.OPTIONS.set_arg_bool(t.name, (Integer.parseInt(t.def_value) == 0) ? false : true);
			} else {
				assert false;
			}
		}

		// process arguments
		String bin_name = argv.argvalue[0];
		for (int i = 1; i < argc; i++) {
			tangible.RefObject<String> tempRef_bin_name = new tangible.RefObject<String>(bin_name);
			GlobalMembersGm_argopts.parse_arg(argv.argvalue[i], tempRef_bin_name);
			bin_name = tempRef_bin_name.argvalue;
		}

		// check num files
		if (GlobalMembersGm_main.GM_input_lists.size() == 0) {
			tangible.RefObject<String> tempRef_bin_name2 = new tangible.RefObject<String>(bin_name);
			GlobalMembersGm_argopts.print_help(tempRef_bin_name2);
			bin_name = tempRef_bin_name2.argvalue;
			System.exit(0);
		} else if (GlobalMembersGm_main.GM_input_lists.size() > 1) {
			System.out.print("[Warning] Current version only can hanle only one input file; only the first input will be processed.\n");
		}
	}
}