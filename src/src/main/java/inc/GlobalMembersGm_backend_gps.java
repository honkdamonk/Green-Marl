package inc;


public class GlobalMembersGm_backend_gps
{

	public static String GPS_TAG_COMM_ID = "X";

//C++ TO JAVA CONVERTER NOTE: 'extern' variable declarations are not required in Java:
	//extern gm_gps_gen* PREGEL_BE;

	// string used in code generator
	public static String GPS_FLAG_USE_REVERSE_EDGE = "X";
	public static String GPS_FLAG_USE_IN_DEGREE = "X";
	public static String GPS_FLAG_COMM_SYMBOL = "X";
	//where:check_random_access, to:assign_statement, what:ptr to sent block
	public static String GPS_FLAG_SENT_BLOCK_FOR_RANDOM_WRITE_ASSIGN = "X";
	//where:check_random_access, to:sentblock,what:set of symbols that are random-write driver
	public static String GPS_FLAG_RANDOM_WRITE_SYMBOLS_FOR_SB = "X";
	public static String GPS_FLAG_USE_EDGE_PROP = "X";
	// edge which is used inside in a inner llop
	public static String GPS_FLAG_EDGE_DEFINED_INNER = "X";
	public static String GPS_FLAG_EDGE_DEFINING_WRITE = "X";
	// inner loops that contains edges
	public static String GPS_FLAG_EDGE_DEFINING_INNER = "X";
	public static String GPS_MAP_EDGE_PROP_ACCESS = "X";
	public static String GPS_LIST_EDGE_PROP_WRITE = "X";
	public static String GPS_FLAG_NODE_VALUE_INIT = "X";

	// used for intra-loop merging
	public static String GPS_FLAG_WHILE_HEAD = "X";
	public static String GPS_FLAG_WHILE_TAIL = "X";

	// an outerloop that has communication
	public static String GPS_FLAG_HAS_COMMUNICATION = "X";
	// an outerloop that random has communication
	public static String GPS_FLAG_HAS_COMMUNICATION_RANDOM = "X";
	// target: iterator or loop.
	public static String GPS_FLAG_IS_OUTER_LOOP = "X";
	// target: iterator or loop.
	public static String GPS_FLAG_IS_INNER_LOOP = "X";
	// target: edge scalar variable
	public static String GPS_FLAG_IS_EDGE_ITERATOR = "X";
	public static String GPS_FLAG_IS_INTRA_MERGED_CONDITIONAL = "X";
	public static String GPS_INT_INTRA_MERGED_CONDITIONAL_NO = "X";
	public static String GPS_LIST_INTRA_MERGED_CONDITIONAL = "X";
	// target: assign statement, rewrite_rhs.cc
	public static String GPS_FLAG_COMM_DEF_ASSIGN = "X";

	// target: ast_bfs, gps_opt_tranform.bfs
	public static String GPS_FLAG_HAS_DOWN_NBRS = "X";

	public static final int GPS_PREPARE_STEP1 = 100000;
	public static final int GPS_PREPARE_STEP2 = 100001;
	public static String GPS_RET_VALUE = "_ret_value";
	public static String GPS_REV_NODE_ID = "_revNodeId";
	public static String GPS_DUMMY_ID = "_remoteNodeId";
	public static String GPS_NAME_IN_DEGREE_PROP = "_in_degree";
	public static String GPS_INTRA_MERGE_IS_FIRST = "_is_first_";
	public static String GPS_KEY_FOR_STATE = "\"__gm_gps_state\"";
	public static String STATE_SHORT_CUT = "_this";

	// where each assignment is destinated
	public static String GPS_INT_ASSIGN_SCOPE = "X";
	// where sub-expression is dependent on
	public static String GPS_INT_EXPR_SCOPE = "X";
	// where each symbol is defined (in syntax)
	public static String GPS_INT_SYMBOL_SCOPE = "X";
	// where each statement is located (in syntax)
	public static String GPS_INT_SYNTAX_CONTEXT = "X";
}