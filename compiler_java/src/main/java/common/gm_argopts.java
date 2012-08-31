package common;

import tangible.RefObject;
import tangible.StringFunctions;

public class gm_argopts {

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

	// public static String GMARGFLAG_STOP_MAJOR = "DS";
	// public static String GMARGFLAG_STOP_MINOR = "Ds";
	public static String GMARGFLAG_STOP_STRING = "DS";
	public static String GMARGFLAG_REPRODUCE = "Dr";
	public static String GMARGFLAG_PRINTRW = "Dw";
	public static String GMARGFLAG_PRINTBB = "Db";
	public static String GMARGFLAG_FLIP_REVERSE = "FlipRev";
	public static String GMARGFLAG_FLIP_PULL = "FlipPull";
	public static String GMARGFLAG_FLIP_BFSUP = "FlipUp";

	public static String GMARGFLAG_MERGE_BB = "GPSMerge";
	public static String GMARGFLAG_MERGE_BB_INTRA = "GPSMergeIntra";
	// public static String GMARGFLAG_DUMPIR = "Dd";
	// public static String GMARGFLAG_NOMERGE = "NoMerge";
	// public static String GMARGFLAG_NOSCREDUCE = "NoScalarReduce";

	// Add compiler options here
	public static gm_comp_args[] GM_compiler_options = { //
	new gm_comp_args(GMARGFLAG_HELP, GMARG_NULL, "Print help messages", null), new gm_comp_args(GMARGFLAG_VERSION, GMARG_NULL, "Print version", null),
			new gm_comp_args(GMARGFLAG_TARGET, GMARG_STRING, "Target backend. Current valid targets are 'cpp_omp', 'cpp_seq', 'gps' and 'giraph'.", "cpp_omp"),
			new gm_comp_args(GMARGFLAG_OUTDIR, GMARG_STRING, "Output directory.", "."),
			new gm_comp_args(GMARGFLAG_OPT_LEV, GMARG_INT, "Optimization Level (tobe implemented)", "1"),
			new gm_comp_args(GMARGFLAG_VERB_LEV, GMARG_INT, "Verbosity Level 0:silent, 1:show compile stage", "0"),
			new gm_comp_args(GMARGFLAG_FLIP_BFSUP, GMARG_BOOL, "Enable 'flipping edges for BFS Up-nbrs'", "1"),
			new gm_comp_args(GMARGFLAG_FLIP_REVERSE, GMARG_BOOL, "Enable 'flipping edges' to avoid the use of reverse edges", "0"),
			new gm_comp_args(GMARGFLAG_FLIP_PULL, GMARG_BOOL, "Enable 'flipping edges' to avoid the use of pull-based computation", "0"),
			new gm_comp_args(GMARGFLAG_MERGE_BB, GMARG_BOOL, "(For gps) Enable bb merge optimization", "1"),
			new gm_comp_args(GMARGFLAG_MERGE_BB_INTRA, GMARG_BOOL, "(For gps) Enable intra-loop bb merge optimization", "1"),
			new gm_comp_args(GMARGFLAG_STOP_STRING, GMARG_STRING, "(For debug) Stop the compiler after certain stage. <string>=(step)[.(step)]", "0"),
			new gm_comp_args(GMARGFLAG_REPRODUCE, GMARG_BOOL, "(For debug) When stopped, reproduce green-marl program", "1"),
			new gm_comp_args(GMARGFLAG_PRINTRW, GMARG_BOOL, "(For debug) When stopped, print rw analysis information", "0"),
			new gm_comp_args(GMARGFLAG_PRINTBB, GMARG_BOOL, "(For gps-debug) When stopped, print basicblock information", "0") //
	};


	public static void print_help(String bin_name) {
		System.out.printf("Usage: %s [options] input_file\n", bin_name);
		System.out.print("Options:\n");

		int s = GM_compiler_options.length;
		for (int i = 0; i < s; i++) {
			int t = 0;
			String message = String.format("  -%s", GM_compiler_options[i].name);
			t += message.length();
			System.out.print(message);
			if (GM_compiler_options[i].arg_type == GMARG_NULL) {
				// do nothing
			} else if (GM_compiler_options[i].arg_type == GMARG_BOOL) {
				message = "[=0/1]";
				t += message.length();
				System.out.print(message);
			} else if (GM_compiler_options[i].arg_type == GMARG_STRING) {
				message = "=<string>";
				t += message.length();
				System.out.print(message);
			} else if (GM_compiler_options[i].arg_type == GMARG_INT) {
				message = "=<int>";
				t += message.length();
				System.out.print(message);
			}
			for (; t < 24; t++) {
				System.out.print(" ");
			}
			System.out.printf("%s", GM_compiler_options[i].help_string);
			if (GM_compiler_options[i].arg_type != GMARG_NULL) {
				System.out.printf(" (Default is %s)", GM_compiler_options[i].def_value);
			}
			System.out.print("\n");
		}
	}

	public static void process_nullargs(RefObject<String> c, String bin_name) {
		if (c.argvalue.equals("h")) {
			gm_argopts.print_help(bin_name);
			System.exit(0);
		} else if (c.argvalue.equals("v")) {
			System.out.printf("version %s\n", gm_main.gm_version_string);
			System.exit(0);
		}
		// add here
	}

	public static void parse_arg(String argv, String bin_name) {
		int s = GM_compiler_options.length;
		if (argv.charAt(0) == '-') {
			// search '=' in the argument
			int z = argv.length();
			String key_begin = argv.charAt(1) + "";
			String val_begin = null;
			for (int i = 1; i < z; i++) {
				if (argv.charAt(i) == '=') {
					argv = StringFunctions.changeCharacter(argv, i, '\0');
					val_begin = argv.charAt(i + 1) + "";
					break;
				}
			}
			// ------------------------------
			// find matching key
			// ------------------------------
			int i;
			for (i = 0; i < s; i++) {
				gm_comp_args t = GM_compiler_options[i];
				if (!t.name.equals(key_begin))
					continue;

				if (t.arg_type == GMARG_NULL) {
					RefObject<String> tempRef_key_begin = new RefObject<String>(key_begin);
					gm_argopts.process_nullargs(tempRef_key_begin, bin_name);
					key_begin = tempRef_key_begin.argvalue;
				} else if (t.arg_type == GMARG_STRING) {
					if (val_begin == null)
						val_begin = (String) "";
					gm_main.OPTIONS.set_arg_string(key_begin, val_begin);
				} else if (t.arg_type == GMARG_INT) {
					if (val_begin == null)
						gm_main.OPTIONS.set_arg_int(key_begin, 0);
					else
						gm_main.OPTIONS.set_arg_int(key_begin, Integer.parseInt(val_begin));
				} else if (t.arg_type == GMARG_BOOL) {
					if (val_begin == null)
						gm_main.OPTIONS.set_arg_bool(key_begin, true);
					else {
						gm_main.OPTIONS.set_arg_bool(key_begin, Integer.parseInt(val_begin) == 0 ? false : true);
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
			gm_main.GM_input_lists.addLast(argv);
		}
	}

	public static void process_args(String[] args) {

		int s = GM_compiler_options.length;
		for (int i = 0; i < s; i++) {
			gm_comp_args t = GM_compiler_options[i];
			if (t.def_value == null)
				continue;
			else if (t.arg_type == GMARG_NULL) {
				continue;
			} else if (t.arg_type == GMARG_STRING) {
				gm_main.OPTIONS.set_arg_string(t.name, t.def_value);
			} else if (t.arg_type == GMARG_INT) {
				gm_main.OPTIONS.set_arg_int(t.name, Integer.parseInt(t.def_value));
			} else if (t.arg_type == GMARG_BOOL) {
				gm_main.OPTIONS.set_arg_bool(t.name, (Integer.parseInt(t.def_value) == 0) ? false : true);
			} else {
				assert false;
			}
		}

		// process arguments
		String bin_name = gm_main.gm_bin_name;
		for (int i = 0; i < args.length; i++) {
			gm_argopts.parse_arg(args[i], bin_name);
		}

		// check num files
		if (gm_main.GM_input_lists.size() == 0) {
			gm_argopts.print_help(bin_name);
			System.exit(0);
		} else if (gm_main.GM_input_lists.size() > 1) {
			System.out.print("[Warning] Current version only can hanle only one input file; only the first input will be processed.\n");
		}
	}
}