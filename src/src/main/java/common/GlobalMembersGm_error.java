package common;

import ast.ast_id;
import frontend.gm_symtab_entry;

public class GlobalMembersGm_error {
	public static void gm_type_error(GM_ERRORS_AND_WARNINGS errno, ast_id id, String str1) {
		gm_type_error(errno, id, str1, "");
	}

	public static void gm_type_error(GM_ERRORS_AND_WARNINGS errno, ast_id id) {
		gm_type_error(errno, id, "", "");
	}

	// C++ TO JAVA CONVERTER NOTE: Java does not allow default values for
	// parameters. Overloaded methods are inserted above.
	// ORIGINAL LINE: void gm_type_error(int errno, ast_id* id, String str1 =
	// "", String str2 = "")
	public static void gm_type_error(GM_ERRORS_AND_WARNINGS errno, ast_id id, String str1, String str2) {
		GlobalMembersGm_error.gm_print_error_header();
		if (curr_file != null)
			System.out.printf("%s:", curr_file);
		System.out.printf("%d: %d: error: ", id.get_line(), id.get_col());
		switch (errno) {
		case GM_ERROR_INVALID_ITERATOR_FOR_RARROW:
			System.out.printf("%s cannot be used in Edge() syntax.\n", id.get_orgname());
			break;
		case GM_ERROR_INVALID_GROUP_DRIVER:
			System.out.printf("%s cannot be used outside group assignment.\n", id.get_orgname());
			break;
		case GM_ERROR_UNDEFINED:
			System.out.printf("%s is not defined.\n", id.get_orgname());
			break;
		case GM_ERROR_UNDEFINED_FIELD:
			System.out.printf("Property name %s is not defined.\n", id.get_orgname());
			break;
		// case GM_ERROR_MULTIPLE_TARGET:
		// printf("%s is bound to multiple graphs.\n", id->get_orgname());
		// break;
		case GM_ERROR_WRONG_PROPERTY:
			System.out.printf("%s is not defined as a %s\n", id.get_orgname(), str1);
			break;
		case GM_ERROR_NONGRAPH_FIELD:
			System.out.printf("%s is neither node, edge, nor graph.\n", id.get_orgname());
			break;
		case GM_ERROR_READONLY:
			System.out.printf("It is not allowed to write into %s \n", id.get_orgname());
			break;
		case GM_ERROR_NEED_ORDER:
			System.out.printf("Need ordered set for reverse-order iteration (%s is not)\n", id.get_orgname());
			break;
		// case GM_ERROR_INVALID_ITERATOR:
		// printf("Iterator not valid for the source %s \n", id->get_orgname());
		// break;

		case GM_ERROR_NEED_NODE_ITERATION:
			System.out.printf("Iteration should start from a node instead of %s\n", id.get_orgname());
			break;
		case GM_ERROR_NEED_BFS_ITERATION:
			System.out.printf("Leveled Iteration should start from a bfs iterator instead of %s \n", id.get_orgname());
			break;

		case GM_ERROR_NEED_ITERATOR:
			System.out.printf("%s is not an iterator name\n", id.get_orgname());
			break;

		case GM_ERROR_DEFAULT_GRAPH_AMBIGUOUS:
			System.out.print("More than one graph present. Explicit binding of nodes, edges, properties and collections is required!\n");
			break;

		case GM_ERROR_UNKNOWN:
		default:
			assert false;
			System.out.print("Unknown error 1\n");
			break;
		}
	}

	public static void gm_type_error(GM_ERRORS_AND_WARNINGS errno, ast_id id1, ast_id id2) {
		GlobalMembersGm_error.gm_print_error_header();
		if (curr_file != null)
			System.out.printf("%s:", curr_file);
		System.out.printf("%d: %d: error: ", id1.get_line(), id1.get_col());
		switch (errno) {
		case GM_ERROR_NONGRAPH_TARGET:
			System.out.printf("%s is not a graph type object\n", id1.get_orgname());
			break;
		case GM_ERROR_NONSET_TARGET:
			System.out.printf("%s is not a collection type object\n", id1.get_orgname());
			break;
		case GM_ERROR_NONNODE_TARGET:
			System.out.printf("%s is not a node-compatible type object\n", id1.get_orgname());
			break;
		case GM_ERROR_TARGET_GRAPH_MISMATCH:
			System.out.printf("%s and %s are not bound to the same graph\n", id1.get_orgname(), id2.get_orgname());
			break;
		case GM_ERROR_UNDEFINED_FIELD_GRAPH:
			System.out.printf("Property %s is not defined for graph %s.\n", id1.get_orgname(), id2.get_orgname());
			break;
		case GM_ERROR_DUPLICATE:
			System.out.printf("%s is defined more than one time. (First defined in line %d : %d)\n", id1.get_orgname(), id2.get_line(), id2.get_col());
			break;
		case GM_ERROR_TARGET_MISMATCH:
			System.out.printf("Target Graphs mismatches %s, %s.\n", id1.get_orgname(), id2.get_orgname());
			break;

		case GM_ERROR_DEFAULT_GRAPH_AMBIGUOUS:
			System.out.print("More than one graph present. Explicit binding of nodes, edges, properties and collections is required!\n");
			break;

		case GM_ERROR_UNKNOWN:
		default:
			System.out.print("Unknown error 3\n");
			break;
		}
	}

	public static void gm_type_error(GM_ERRORS_AND_WARNINGS errno, int l, int c, String str1, String str2) {
		gm_type_error(errno, l, c, str1, str2, "");
	}

	public static void gm_type_error(GM_ERRORS_AND_WARNINGS errno, int l, int c, String str1) {
		gm_type_error(errno, l, c, str1, "", "");
	}

	public static void gm_type_error(GM_ERRORS_AND_WARNINGS errno, int l, int c) {
		gm_type_error(errno, l, c, "", "", "");
	}

	// C++ TO JAVA CONVERTER NOTE: Java does not allow default values for
	// parameters. Overloaded methods are inserted above.
	// ORIGINAL LINE: void gm_type_error(int errno, int l, int c, String str1 =
	// "", String str2 = "", String str3 = "")
	public static void gm_type_error(GM_ERRORS_AND_WARNINGS errno, int l, int c, String str1, String str2, String str3) {
		GlobalMembersGm_error.gm_print_error_header();

		if (curr_file != null)
			System.out.printf("%s:", curr_file);
		System.out.printf("%d: %d: error: ", l, c);
		switch (errno) {
		case GM_ERROR_PAR_RETURN:
			System.out.print("return inside parallel consistency\n");
			break;
		case GM_ERROR_GROUP_REDUCTION:
			System.out.print("Group assignment cannot be a reduction\n");
			break;
		case GM_ERROR_INVALID_ARGMAX_COUNT:
			System.out.printf("Number of lhs does not match to number of rhs: %s\n", str1);
			break;
		case GM_ERROR_INCONSISTENT_ARGMAX:
			System.out.print("LHS list of argmiax assignment is not consistent; They should be all scalar or have same driver\n");
			break;

		case GM_ERROR_NESTED_BFS:
			System.out.print("Currently, nested bfs/dfs is not supported\n");
			break;
		case GM_ERROR_NEED_PRIMITIVE:
			System.out.print("need primitive type.\n");
			break;
		case GM_ERROR_INVALID_OUTPUT_TYPE:
			System.out.print("Invalid type for an output parameter, or return.\n");
			break;
		case GM_ERROR_INVALID_BUILTIN:
			System.out.printf("Invalid built-in:%s\n", str1);
			break;
		case GM_ERROR_INVALID_BUILTIN_ARG_COUNT:
			System.out.printf("Argument number mismatch for built-in:%s\n", str1);
			break;
		case GM_ERROR_INVALID_BUILTIN_ARG_TYPE:
			System.out.printf("type mismatch for built-in:%s, arg_no:%s \n", str1, str2);
			break;
		case GM_ERROR_OPERATOR_MISMATCH:
			System.out.printf("Operator %s applied to an unsupported type (%s)\n", str1, str2);
			break;
		case GM_ERROR_OPERATOR_MISMATCH2:
			System.out.printf("Operator %s cannot be applied to (%s, %s)\n", str1, str2, str3);
			break;
		case GM_ERROR_TYPE_CONVERSION:
			System.out.print("Type conversion can be only applied to primitive types\n");
			break;
		case GM_ERROR_TYPE_CONVERSION_BOOL_NUM:
			System.out.print("Type conversion cannot be  applied between numeric and boolean types\n");
			break;
		case GM_ERROR_TARGET_MISMATCH:
			System.out.print("Assignment to different Graphs\n");
			break;
		case GM_ERROR_ASSIGN_TYPE_MISMATCH:
			System.out.printf("Typemismatch in Assignment. LHS:%s, RHS:%s \n", str1, str2);
			break;
		case GM_ERROR_COMPARE_MISMATCH:
			System.out.printf("Typemismatch in Comparison. LHS:%s, RHS:%s \n", str1, str2);
			break;
		case GM_ERROR_NEED_BOOLEAN:
			System.out.print("Need boolean expression.\n");
			break;
		case GM_ERROR_UNBOUND_REDUCE:
			System.out.print("Cannot determine bound to Reduce(Defer) assignment\n");
			break;

		case GM_ERROR_DOUBLE_BOUND_ITOR:
			System.out.printf("Reduce(Defer) Target already bound to a different iterator: %s\n", str1);
			break;
		case GM_ERROR_DOUBLE_BOUND_OP:
			System.out.printf("Reduce(Defer) Target already bound to a different operator: %s\n", str1);
			break;
		case GM_ERROR_GRAPH_REDUCE:
			System.out.printf("Can not do reduce (defer) assignment to graph variable :%s\n", str1);
			break;
		case GM_ERROR_GROUP_MISMATCH:
			System.out.print("Group assignment error (assigning node_prop into edge_prop or vice versa)\n");
			break;
		case GM_ERROR_RETURN_FOR_VOID:
			System.out.print("Cannot have return value for void procedure\n");
			break;
		case GM_ERROR_NO_VOID_RETURN:
			System.out.print("Need Return Value\n");
			break;
		case GM_ERROR_RETURN_MISMATCH:
			System.out.printf("Return type mismatch: required(%s), found(%s)\n", str1, str2);
			break;

		case GM_ERROR_REQUIRE_BOOLEAN_REDUCE:
			System.out.print("Boolean type required for reduction\n");
			break;
		case GM_ERROR_REQUIRE_NUMERIC_REDUCE:
			System.out.print("Numeric type required for reduction\n");
			break;

		default:
			System.out.print("Unknown error 2\n");
			break;
		}
	}

	// extern void gm_type_error(int errnumber, String str);

	// extern void gm_conf_error(int errnumber, gm_symtab_entry* target, ast_id*
	// evidence1);
	public static void gm_conf_error(GM_ERRORS_AND_WARNINGS errno, gm_symtab_entry target, ast_id ev1, ast_id ev2, boolean is_warning) {
		GlobalMembersGm_error.gm_print_error_header();
		if (curr_file != null)
			System.out.printf("%s:", curr_file);

		if (is_warning)
			System.out.printf("%d: %d: warn: ", ev1.get_line(), ev1.get_col());
		else
			System.out.printf("%d: %d: error: ", ev1.get_line(), ev1.get_col());

		ast_id target_id = target.getId();
		String name = target_id.get_orgname();

		switch (errno) {
		case GM_ERROR_READ_WRITE_CONFLICT:
			System.out.printf("Property %s may have read-write conflict: read at line:%d, write at line:%d\n", name, ev1.get_line(), ev2.get_line());
			break;
		case GM_ERROR_WRITE_WRITE_CONFLICT:
			System.out.printf("Property %s may have write-write conflict: write at line:%d, write at line:%d\n", name, ev1.get_line(), ev2.get_line());
			break;
		case GM_ERROR_READ_REDUCE_CONFLICT:
			System.out.printf("Property %s may have read-reduce conflict: read at line:%d, reduce at line:%d\n", name, ev1.get_line(), ev2.get_line());
			break;
		case GM_ERROR_WRITE_REDUCE_CONFLICT:
			System.out.printf("Property %s may have write-reduce conflict: write at line:%d, reduce at line:%d\n", name, ev1.get_line(), ev2.get_line());
			break;
		case GM_ERROR_READ_MUTATE_CONFLICT:
			System.out.printf("Property %s may have read-mutate conflict: read at line:%d, mutate at line:%d\n", name, ev1.get_line(), ev2.get_line());
			break;
		case GM_ERROR_WRITE_MUTATE_CONFLICT:
			System.out.printf("Property %s may have write-mutate conflict: write at line:%d, mutate at line:%d\n", name, ev1.get_line(), ev2.get_line());
			break;
		case GM_ERROR_MUTATE_MUTATE_CONFLICT:
			System.out.printf("Property %s may have mutate-mutate conflict: mutate at line:%d, mutate at line:%d\n", name, ev1.get_line(), ev2.get_line());
			break;
		case GM_ERROR_UNKNOWN:
		default:
			assert false;
			System.out.print("Unknown error 3\n");
			break;
		}
	}

	// todo: should be differend error routines for different back-ends
	public static void gm_backend_error(GM_ERRORS_AND_WARNINGS errno, String str1) {
		gm_backend_error(errno, str1, "");
	}

	// extern void gm_conf_warning(int errnumber, gm_symtab_entry* target,
	// ast_id* evidence1, ast_id* evidence2);

	// C++ TO JAVA CONVERTER NOTE: Java does not allow default values for
	// parameters. Overloaded methods are inserted above.
	// ORIGINAL LINE: void gm_backend_error(int errno, String str1, String str2
	// = "")
	public static void gm_backend_error(GM_ERRORS_AND_WARNINGS errno, String str1, String str2) {
		GlobalMembersGm_error.gm_print_error_header();
		if (curr_file != null)
			System.out.printf("%s:", curr_file);
		switch (errno) {
		case GM_ERROR_FILEWRITE_ERROR:
			System.out.printf("Error: cannot open file %s for write\n", str1);
			break;
		case GM_ERROR_GPS_NUM_PROCS:
			System.out.print("Error: There must be one and only one procedure\n");
			break;
		case GM_ERROR_GPS_PROC_NAME:
			System.out.printf("Error: The name of the procedure(%s) must match with the name of file (%s)\n", str1, str2);
			break;
		default:
			assert false;
			System.out.print("Unknown backend error\n");
			break;
		}
	}

	public static void gm_backend_error(GM_ERRORS_AND_WARNINGS errno, int l, int c) {
		gm_backend_error(errno, l, c, "");
	}

	// C++ TO JAVA CONVERTER NOTE: Java does not allow default values for
	// parameters. Overloaded methods are inserted above.
	// ORIGINAL LINE: void gm_backend_error(int errno, int l, int c, String str1
	// = "")
	public static void gm_backend_error(GM_ERRORS_AND_WARNINGS errno, int l, int c, String str1) {
		GlobalMembersGm_error.gm_print_error_header();
		if (curr_file != null)
			System.out.printf("%s:", curr_file);
		System.out.printf("%d: %d: error: ", l, c);
		switch (errno) {
		case GM_ERROR_GPS_EDGE_SEND_VERSIONS:
			System.out.printf("Communicating multiple versions of edge value in one message: %s\n", str1);
			break;
		case GM_ERROR_GPS_EDGE_WRITE_RHS:
			System.out.printf("Unacceptible driver for rhs in edge property writing: %s\n", str1);
			break;
		case GM_ERROR_GPS_EDGE_WRITE_CONDITIONAL:
			System.out.print("Write to edge property should not be conditional.\n");
			break;
		case GM_ERROR_GPS_EDGE_INIT:
			System.out.print("Edge variable should be initialize only to out_nbr.ToEdge().\n");
			break;
		case GM_ERROR_GPS_EDGE_WRITE_RANDOM:
			System.out.print("Random writing of edge values is not supported.\n");
			break;
		case GM_ERROR_GPS_EDGE_READ_RANDOM:
			System.out.print("Random reading of edge values is not supported.\n");
			break;
		case GM_ERROR_GPS_RANDOM_NODE_WRITE_CONDITIONAL:
			System.out.print("Random node write cannot happen inside a conditional block\n");
			break;
		case GM_ERROR_GPS_RANDOM_NODE_WRITE_USE_SCOPE:
			System.out.print("Random node write should happen in the outer loop\n");
			break;
		case GM_ERROR_GPS_RANDOM_NODE_WRITE_DEF_SCOPE:
			System.out.print("Random node write should destinated to a out-scoped node variable\n");
			break;
		case GM_ERROR_GPS_RANDOM_NODE_WRITE_REDEF:
			System.out.print("Random node destination has been re-defined\n");
			break;
		case GM_ERROR_GPS_RANDOM_NODE_READ:
			System.out.print("Random node read is not supported\n");
			break;
		case GM_ERROR_GPS_RANDOM_NODE_WRITE:
			System.out.print("Random node write is not supported\n");
			break;
		case GM_ERROR_GPS_UNSUPPORTED_OP:
			System.out.printf("%s operation is not supported\n", str1);
			break;
		case GM_ERROR_GPS_UNSUPPORTED_RANGE_MASTER:
			System.out.print("Only node-wide parallel iteration is supported in master mode\n");
			break;
		case GM_ERROR_GPS_UNSUPPORTED_RANGE_VERTEX:
			System.out.print("Only neighbor-wide iteration is supported in vertex mode\n");
			break;
		case GM_ERROR_GPS_NEED_PARALLEL:
			System.out.print("Only parallel iteration is avaiable\n");
			break;
		case GM_ERROR_GPS_NBR_LOOP_INSIDE_WHILE:
			System.out.print("Inner loop cannot be inside extra loop or if statement.\n");
			break;
		case GM_ERROR_GPS_UNSUPPORTED_COLLECTION:
			System.out.printf("%s is an unsupported collection type\n", str1);
			break;
		case GM_ERROR_GPS_NO_GRAPH:
			System.out.print("There should be at least one graph defined at the entry function\n");
			break;
		case GM_ERROR_GPS_MULTIPLE_GRAPH:
			System.out.print("There should only one graph defined at the entry function\n");
			break;
		case GM_ERROR_GPS_PULL_SYNTAX:
			System.out.print("Syntax is based in 'Pulling' and cannot be transformed into 'Pushing'\n");
			break;
		case GM_ERROR_GPS_NBR_LOOP_TOO_DEEP:
			System.out.print("Loop depth too deep\n");
			break;
		case GM_ERROR_GPS_MULTIPLE_INNER_LOOP:
			System.out.print("There can be only one inner loop\n");
			break;
		case GM_ERROR_CPP_UNSUPPORTED_SYNTAX:
			System.out.printf("The compiler does not support nested %s\n", str1);
			break;
		default:
			return; // added to remove compiler warning
		}
	}

	public static void gm_set_current_filename(tangible.RefObject<String> fname) {
		curr_file = fname.argvalue;
	}

	public static String gm_get_current_filename() {
		return curr_file;
	}

	public static void gm_set_curr_procname(tangible.RefObject<String> pname) {
		curr_proc = pname.argvalue;
		need_print = true;
	}

	public static String curr_file = null;
	public static String curr_proc = null;
	public static boolean need_print = true;
	public static boolean parse_error = false;

	public static void GM_set_parse_error(boolean b) {
		parse_error = b;
	}

	public static boolean GM_is_parse_error() {
		return parse_error;
	}

	public static void gm_print_error_header() {
		if (need_print == false)
			return;
		if (curr_file != null)
			System.out.printf("%s:", curr_file);
		if (curr_proc == null)
			System.out.print("At top level:\n");
		else
			System.out.printf("In procedure %s:\n", curr_proc);
		need_print = false;
	}
}