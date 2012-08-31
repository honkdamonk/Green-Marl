package common;

import inc.gm_backend;

import java.util.LinkedList;

import opt.gm_independent_optimize;
import backend_cpp.gm_cpp_gen;
import backend_giraph.gm_giraph_gen;
import backend_gps.gm_gps_gen;
import frontend.gm_frontend;

public class gm_main {
	/***************************************
	 * Main - Process command line arguments - Call functions in following order
	 * (1) (Frontend) Parser (2) (Frontend) Frontend Transform (3) (Frontend)
	 * Independent Optimizer (4) (Backend) Target Optimizer (5) (Backend)
	 * Library Transform (6) (Backend) Code Generation
	 ***************************************/

	// -----------------------------------------------------
	// For compiler debug,
	// mark begining/end of compiler stage (major or minor).
	// All numbering should start from 1. (not from 0)
	private static final int GMSTAGE_PARSE = 1;
	private static final int GMSTAGE_FRONTEND = 2;
	private static final int GMSTAGE_INDEPENDENT_OPT = 3;
	private static final int GMSTAGE_BACKEND_OPT = 4;
	private static final int GMSTAGE_LIBRARY_OPT = 5;
	private static final int GMSTAGE_CODEGEN = 6;

	public static String gm_bin_name = "gm_jcomp";
	public static String gm_version_string = "0.4.0";

	public static gm_frontend FE = new gm_frontend();
	public static gm_cpp_gen CPP_BE = new gm_cpp_gen(); // CPP Backend
	public static gm_gps_gen GPS_BE = new gm_gps_gen(); // GPS Backend
	public static gm_giraph_gen GIRAPH_BE = new gm_giraph_gen(); // Giraph Backend
	public static gm_gps_gen PREGEL_BE; // TODO
	public static gm_backend BACK_END;
	public static gm_userargs OPTIONS = new gm_userargs();
	public static gm_independent_optimize IND_OPT = new gm_independent_optimize();
	public static gm_builtin_manager BUILT_IN = new gm_builtin_manager();

	static LinkedList<String> GM_input_lists = new LinkedList<String>();

	// -------------------------------------------------------------
	// For debug
	// Stop at various points during compilation
	// -------------------------------------------------------------
	public static int gm_stop_major = 0;
	public static int gm_stop_minor = 0;
	public static int gm_stage_major = 0;
	public static int gm_stage_minor = 0;
	public static String gm_major_desc;
	public static String gm_minor_desc;

	public static void do_compiler_action_at_stop() {
		// okay, this is a hack for debug
		// reconstruct here?
		if (FE.is_vardecl_removed())
			FE.restore_vardecl_all();

		/*
		 * if (OPTIONS.get_arg_bool(GMARGFLAG_DUMPIR)) {
		 * printf("======================================================\n");
		 * FE.dump_tree();
		 * printf("======================================================\n");
		 * printf("\n"); }
		 */

		if (OPTIONS.get_arg_bool(gm_argopts.GMARGFLAG_REPRODUCE)) {
			System.out.print("======================================================\n");
			FE.reproduce();
			System.out.print("======================================================\n");
			System.out.print("\n");

		}

		if (OPTIONS.get_arg_bool(gm_argopts.GMARGFLAG_PRINTRW)) {
			System.out.print("======================================================\n");
			FE.print_rwinfo();
			System.out.print("======================================================\n");
			System.out.print("\n");
		}

		if (OPTIONS.get_arg_bool(gm_argopts.GMARGFLAG_PRINTBB)) {
			System.out.print("======================================================\n");
			PREGEL_BE.print_basicblock();
			System.out.print("======================================================\n");
			System.out.print("\n");
		}
	}

	public static void parse_stop_string() {
		String c = OPTIONS.get_arg_string(gm_argopts.GMARGFLAG_STOP_STRING);
		if (c == null)
			return;

		String d = new String(c);
		String p = tangible.StringFunctions.strTok(d, ".");
		if (p == null) {
			return;
		}
		gm_stop_major = Integer.parseInt(p);
		p = tangible.StringFunctions.strTok(null, ".");
		if (p != null)
			gm_stop_minor = Integer.parseInt(p);

		if (gm_stop_major == 0) {
			return;
		}
		if (gm_stop_minor == 0) {
			System.out.printf("stopping after stage %d\n", gm_stop_major);
		} else {
			System.out.printf("stopping at stage %d.%d\n", gm_stop_major, gm_stop_minor);
		}
	}

	public static void gm_begin_major_compiler_stage(int major, String desc) {
		assert major > 0;
		gm_stage_major = major;
		gm_major_desc = desc;
	}

	public static void gm_end_major_compiler_stage() {
		if (gm_stop_major == gm_stage_major) {
			System.out.printf("...Stopping compiler after Stage %d:%s\n", gm_stop_major, gm_major_desc);
			gm_main.do_compiler_action_at_stop();
			System.exit(0);
		}
	}

	public static void gm_begin_minor_compiler_stage(int m, String desc) {
		assert m > 0;
		gm_stage_minor = m;
		gm_minor_desc = desc;
		if (OPTIONS.get_arg_int(gm_argopts.GMARGFLAG_VERB_LEV) > 0) {
			System.out.printf("...Stage %d.%d: %s.[%s]\n", gm_stage_major, gm_stage_minor, gm_major_desc, gm_minor_desc);
		}

	}

	public static void gm_end_minor_compiler_stage() {
		if (gm_stop_minor == 0)
			return;

		if ((gm_stop_major == gm_stage_major) && (gm_stop_minor == gm_stage_minor)) {
			System.out.printf("...Stopping compiler after Stage %d.%d:%s.[%s]\n", gm_stage_major, gm_stage_minor, gm_major_desc, gm_minor_desc);
			gm_main.do_compiler_action_at_stop();
			System.exit(0);
		}
	}

	public static void main(String[] args) {
		boolean ok = true;

		// -------------------------------------
		// parse arguments
		// -------------------------------------
		gm_argopts.process_args(args);

		gm_path_parser Path = new gm_path_parser();
		String fname = GM_input_lists.getFirst();
		Path.parsePath(fname);

		String name = OPTIONS.get_arg_string(gm_argopts.GMARGFLAG_TARGET);
		if (name.equals("cpp_seq")) {
			CPP_BE.set_target_omp(false);
			BACK_END = CPP_BE;
		} else if (name.equals("cpp_omp")) {
			CPP_BE.set_target_omp(true);
			BACK_END = CPP_BE;
		} else if (name.equals("gps")) {
			BACK_END = GPS_BE;
			PREGEL_BE = GPS_BE;
			OPTIONS.set_arg_bool(gm_argopts.GMARGFLAG_FLIP_PULL, true);
		} else if (name.equals("giraph")) {
			BACK_END = GIRAPH_BE;
			PREGEL_BE = GIRAPH_BE;
			OPTIONS.set_arg_bool(gm_argopts.GMARGFLAG_FLIP_PULL, true);
		} else {
			System.out.printf("Unsupported target = %s\n", name);
		}

		// ---------------------------------------
		// parse compiler stop string
		// ---------------------------------------
		gm_main.parse_stop_string();

		// -------------------------------------
		// Parse phase
		// -------------------------------------
		gm_main.gm_begin_major_compiler_stage(GMSTAGE_PARSE, "Parse");
		{
			// currently there should be only one file
			assert GM_input_lists.size() == 1;
			String fname1 = GM_input_lists.getFirst();
			gm_error.gm_set_current_filename(fname1);
			if (gm_error.GM_is_parse_error()) {
				System.exit(1);
			}
		}
		gm_main.gm_end_major_compiler_stage();

		// ---------------------------------------------------------------
		// Front-End Phase
		// [Local (intra-procedure)]
		// - syntax sugar resolve (phase 1)
		// - basic type check (phase 1)
		// - syntax sugar resolve (phase 2)
		// - rw analysis (phase 1)
		// - rw analysis (phase 2)
		// --------------------------------------------------------------
		gm_main.gm_begin_major_compiler_stage(GMSTAGE_FRONTEND, "Frontend");
		{
			ok = FE.do_local_frontend_process();
			if (!ok)
				System.exit(1);
		}
		gm_main.gm_end_major_compiler_stage();

		// ----------------------------------------------------------------
		// Backend-Independnet Optimization
		// - Hoist up variable definitions
		// - Hoist up assignments
		// - Loop Merge
		// - (Push down assignments)
		// - (Push down var-defs)
		// ----------------------------------------------------------------
		gm_main.gm_begin_major_compiler_stage(GMSTAGE_INDEPENDENT_OPT, "Indep-Opt");
		{
			ok = IND_OPT.do_local_optimize();
			if (!ok)
				System.exit(1);
		}
		gm_main.gm_end_major_compiler_stage();

		// -------------------------------------
		// Backend-Specific Code Modification
		// -------------------------------------
		gm_main.gm_begin_major_compiler_stage(GMSTAGE_BACKEND_OPT, "Backend Transform");
		{
			ok = BACK_END.do_local_optimize();
			if (!ok)
				System.exit(1);
		}
		gm_main.gm_end_major_compiler_stage();

		// -------------------------------------
		// Library specific Backend-Specific Code Modification
		// -------------------------------------
		gm_main.gm_begin_major_compiler_stage(GMSTAGE_LIBRARY_OPT, "Backend-Lib Transform");
		{
			ok = BACK_END.do_local_optimize_lib();
			if (!ok)
				System.exit(1);
		}
		gm_main.gm_end_major_compiler_stage();

		// -------------------------------------------------
		// Final Code Generation
		// -------------------------------------------------
		if (OPTIONS.get_arg_string(gm_argopts.GMARGFLAG_OUTDIR) == null)
			BACK_END.setTargetDir(".");
		else
			BACK_END.setTargetDir(OPTIONS.get_arg_string(gm_argopts.GMARGFLAG_OUTDIR));
		BACK_END.setFileName(Path.getFilename());

		gm_main.gm_begin_major_compiler_stage(GMSTAGE_CODEGEN, "Code Generation");
		{
			ok = BACK_END.do_generate();
			if (!ok)
				System.exit(1);
		}
		gm_main.gm_end_major_compiler_stage();
	}
}