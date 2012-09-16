package backend_gps;

public interface GPSConstants {
	
	public static final String GPS_TAG_COMM_ID = "GPS_TAG_COMM_ID";

	// string used in code generator
	public static final String GPS_FLAG_USE_REVERSE_EDGE = "GPS_FLAG_USE_REVERSE_EDGE";
	public static final String GPS_FLAG_USE_IN_DEGREE = "GPS_FLAG_USE_IN_DEGREE";
	public static final String GPS_FLAG_COMM_SYMBOL = "GPS_FLAG_COMM_SYMBOL";
	//where:check_random_access, to:assign_statement, what:ptr to sent block
	public static final String GPS_FLAG_SENT_BLOCK_FOR_RANDOM_WRITE_ASSIGN = "GPS_FLAG_SENT_BLOCK_FOR_RANDOM_WRITE_ASSIGN";
	//where:check_random_access, to:sentblock,what:set of symbols that are random-write driver
	public static final String GPS_FLAG_RANDOM_WRITE_SYMBOLS_FOR_SB = "GPS_FLAG_RANDOM_WRITE_SYMBOLS_FOR_SB";
	public static final String GPS_FLAG_USE_EDGE_PROP = "GPS_FLAG_USE_EDGE_PROP";
	// edge which is used inside in a inner loop
	public static final String GPS_FLAG_EDGE_DEFINED_INNER = "GPS_FLAG_EDGE_DEFINED_INNER";
	public static final String GPS_FLAG_EDGE_DEFINING_WRITE = "GPS_FLAG_EDGE_DEFINING_WRITE";
	// inner loops that contains edges
	public static final String GPS_FLAG_EDGE_DEFINING_INNER = "GPS_FLAG_EDGE_DEFINING_INNER";
	public static final String GPS_MAP_EDGE_PROP_ACCESS = "GPS_MAP_EDGE_PROP_ACCESS";
	public static final String GPS_LIST_EDGE_PROP_WRITE = "GPS_LIST_EDGE_PROP_WRITE";
	public static final String GPS_FLAG_NODE_VALUE_INIT = "GPS_FLAG_NODE_VALUE_INIT";

	// used for intra-loop merging
	public static final String GPS_FLAG_WHILE_HEAD = "GPS_FLAG_WHILE_HEAD";
	public static final String GPS_FLAG_WHILE_TAIL = "GPS_FLAG_WHILE_TAIL";

	// an outerloop that has communication
	public static final String GPS_FLAG_HAS_COMMUNICATION = "GPS_FLAG_HAS_COMMUNICATION";
	// an outerloop that random has communication
	public static final String GPS_FLAG_HAS_COMMUNICATION_RANDOM = "GPS_FLAG_HAS_COMMUNICATION_RANDOM";
	// target: iterator or loop.
	public static final String GPS_FLAG_IS_OUTER_LOOP = "GPS_FLAG_IS_OUTER_LOOP";
	// target: iterator or loop.
	public static final String GPS_FLAG_IS_INNER_LOOP = "GPS_FLAG_IS_INNER_LOOP";
	// target: edge scalar variable
	public static final String GPS_FLAG_IS_EDGE_ITERATOR = "GPS_FLAG_IS_EDGE_ITERATOR";
	public static final String GPS_FLAG_IS_INTRA_MERGED_CONDITIONAL = "GPS_FLAG_IS_INTRA_MERGED_CONDITIONAL";
	public static final String GPS_INT_INTRA_MERGED_CONDITIONAL_NO = "GPS_INT_INTRA_MERGED_CONDITIONAL_NO";
	public static final String GPS_LIST_INTRA_MERGED_CONDITIONAL = "GPS_LIST_INTRA_MERGED_CONDITIONAL";
	// target: assign statement, rewrite_rhs.cc
	public static final String GPS_FLAG_COMM_DEF_ASSIGN = "GPS_FLAG_COMM_DEF_ASSIGN";

	// target: ast_bfs, gps_opt_tranform.bfs
	public static final String GPS_FLAG_HAS_DOWN_NBRS = "GPS_FLAG_HAS_DOWN_NBRS";

	public static final int GPS_PREPARE_STEP1 = 100000;
	public static final int GPS_PREPARE_STEP2 = 100001;
	public static final String GPS_RET_VALUE = "_ret_value";
	public static final String GPS_REV_NODE_ID = "_revNodeId";
	public static final String GPS_DUMMY_ID = "_remoteNodeId";
	public static final String GPS_NAME_IN_DEGREE_PROP = "_in_degree";
	public static final String GPS_INTRA_MERGE_IS_FIRST = "_is_first_";
	public static final String GPS_KEY_FOR_STATE = "\"__gm_gps_state\"";
	public static final String STATE_SHORT_CUT = "_this";

	// where each assignment is destinated
	public static final String GPS_INT_ASSIGN_SCOPE = "GPS_INT_ASSIGN_SCOPE";
	// where sub-expression is dependent on
	public static final String GPS_INT_EXPR_SCOPE = "GPS_INT_EXPR_SCOPE";
	// where each symbol is defined (in syntax)
	public static final String GPS_INT_SYMBOL_SCOPE = "GPS_INT_SYMBOL_SCOPE";
	// where each statement is located (in syntax)
	public static final String GPS_INT_SYNTAX_CONTEXT = "GPS_INT_SYNTAX_CONTEXT";

}
