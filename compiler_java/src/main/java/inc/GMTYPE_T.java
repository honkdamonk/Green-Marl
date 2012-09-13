package inc;

import java.util.HashMap;

import tangible.RefObject;
import frontend.Oprules;

public enum GMTYPE_T {
	GMTYPE_GRAPH(0), //
	GMTYPE_UGRAPH(1), //
	GMTYPE_NODEPROP(2), //
	GMTYPE_EDGEPROP(3), //
	GMTYPE_NODE(4), //
	GMTYPE_EDGE(5), //
	GMTYPE_NSET(6), //
	GMTYPE_NSEQ(7), //
	GMTYPE_NORDER(8), //
	GMTYPE_ESET(9), //
	GMTYPE_ESEQ(10), //
	GMTYPE_EORDER(11), //
	GMTYPE_COLLECTION(12), //
	GMTYPE_MAP(13), //

	// iterators
	GMTYPE_NODEITER_ALL(100), //
	GMTYPE_NODEITER_NBRS(101), // out nbr
	GMTYPE_NODEITER_IN_NBRS(102), // in nbr
	GMTYPE_NODEITER_BFS(103), // bfs
	GMTYPE_NODEITER_UP_NBRS(104), // up nbr
	GMTYPE_NODEITER_DOWN_NBRS(105), // doen nbr
	GMTYPE_NODEITER_SET(106), // set
	GMTYPE_NODEITER_SEQ(107), // sequence
	GMTYPE_NODEITER_ORDER(108), // order

	GMTYPE_NODEITER_COMMON_NBRS(109), // common neighbors

	GMTYPE_COLLECTIONITER_SET(110), // iterator over collection of collection
	GMTYPE_COLLECTIONITER_ORDER(111), //
	GMTYPE_COLLECTIONITER_SEQ(112),

	GMTYPE_EDGEITER_ALL(200), //
	GMTYPE_EDGEITER_NBRS(201), //
	GMTYPE_EDGEITER_IN_NBRS(202), //
	GMTYPE_EDGEITER_BFS(203), //
	GMTYPE_EDGEITER_UP_NBRS(204), //
	GMTYPE_EDGEITER_DOWN_NBRS(205), //
	GMTYPE_EDGEITER_SET(206), // set
	GMTYPE_EDGEITER_SEQ(207), // sequence
	GMTYPE_EDGEITER_ORDER(208), // order

	GMTYPE_PROPERTYITER_SET(209), //
	GMTYPE_PROPERTYITER_SEQ(210), //
	GMTYPE_PROPERTYITER_ORDER(211), //

	GMTYPE_BIT(1000), // 1b (for future extension)
	GMTYPE_BYTE(1001), // 1B (for future extension)
	GMTYPE_SHORT(1002), // 2B (for future extension)
	GMTYPE_INT(1003), // 4B
	GMTYPE_LONG(1004), // 8B
	GMTYPE_FLOAT(1005), // 4B
	GMTYPE_DOUBLE(1006), // 8B
	GMTYPE_BOOL(1007), //
	GMTYPE_INF(1008), // PLUS INF or MINUS INF
	GMTYPE_INF_INT(1009), //
	GMTYPE_INF_LONG(1010), //
	GMTYPE_INF_FLOAT(1011), //
	GMTYPE_INF_DOUBLE(1012), //
	GMTYPE_NIL_UNKNOWN(1013), //
	GMTYPE_NIL_NODE(1014), //
	GMTYPE_NIL_EDGE(1015), //

	/** foreign type. Can be matched with any */
	GMTYPE_FOREIGN_EXPR(1016),

	/**
	 * expression whose type is not identified yet (variable before
	 * typechecking)
	 */
	GMTYPE_UNKNOWN(9999),

	/** expression whose type should be numeric, size not determined yet */
	GMTYPE_UNKNOWN_NUMERIC(10000),

	/** iterator to some collection. resolved after type checking */
	GMTYPE_ITER_ANY(10001), //
	GMTYPE_ITER_UNDERSPECIFIED(10002), //

	GMTYPE_VOID(10003), //
	GMTYPE_GENERIC(10004), //
	GMTYPE_INVALID(99999); //

	private int intValue;
	private static HashMap<Integer, GMTYPE_T> mappings;

	private static HashMap<Integer, GMTYPE_T> getMappings() {
		if (mappings == null) {
			synchronized (GMTYPE_T.class) {
				if (mappings == null) {
					mappings = new HashMap<Integer, GMTYPE_T>();
				}
			}
		}
		return mappings;
	}

	public boolean isSmallerThan(GMTYPE_T other) {
		return getValue() < other.getValue();
	}

	public boolean isGreaterThan(GMTYPE_T other) {
		return getValue() > other.getValue();
	}

	public int subtract(GMTYPE_T other) {
		return getValue() - other.getValue();
	}

	private GMTYPE_T(int value) {
		intValue = value;
		getMappings().put(value, this);
	}

	public int getValue() {
		return intValue;
	}

	public static GMTYPE_T forValue(int value) {
		return getMappings().get(value);
	}

	public boolean is_graph_type() {
		return this == GMTYPE_GRAPH;
	}

	public boolean is_nodeedge_type() {
		return (this == GMTYPE_NODE) || (this == GMTYPE_EDGE);
	}

	public boolean is_iter_type() {
		return is_node_iter_type() || is_edge_iter_type() || is_collection_iter_type();
	}

	public boolean is_node_type() {
		return this == GMTYPE_NODE;
	}

	public boolean is_node_iter_type() {
		return is_all_graph_node_iter_type() || is_any_nbr_node_iter_type();
	}

	public boolean is_edge_iter_type() {
		return is_all_graph_edge_iter_type() || is_any_nbr_edge_iter_type();
	}

	public boolean is_node_compatible_type() {
		return is_node_type() || is_node_iter_type() || is_node_collection_iter_type() || is_nil_type();
	}

	public boolean is_edge_compatible_type() {
		return is_edge_type() || is_edge_iter_type() || is_edge_collection_iter_type() || is_nil_type();
	}

	public boolean is_collection_type() {
		return is_node_collection_type() || is_edge_collection_type() || is_collection_of_collection_type();
	}

	public boolean is_node_collection_type() {
		return is_node_set_type() || is_node_order_type() || is_node_sequence_type();
	}

	public boolean is_collection_of_collection_type() {
		return this == GMTYPE_COLLECTION;
	}

	public boolean is_set_collection_type() {
		return is_node_set_type() || is_edge_set_type();
	}

	public boolean is_order_collection_type() {
		return is_node_order_type() || is_edge_order_type();
	}

	public boolean is_sequence_collection_type() {
		return is_node_sequence_type() || is_edge_sequence_type();
	}

	public boolean is_prim_type() {
		return is_numeric_type() || is_boolean_type();
	}

	public boolean is_numeric_type() {
		return is_integer_type() || is_float_type();
	}

	public boolean is_foreign_expr_type() {
		return (this == GMTYPE_FOREIGN_EXPR);
	}

	public boolean is_integer_type() {
		return (this == GMTYPE_INT) || (this == GMTYPE_LONG) || (this == GMTYPE_BYTE) || (this == GMTYPE_SHORT);
	}

	public boolean is_float_type() {
		return (this == GMTYPE_FLOAT) || (this == GMTYPE_DOUBLE);
	}

	public boolean is_unknown_type() {
		return (this == GMTYPE_UNKNOWN) || (this == GMTYPE_UNKNOWN_NUMERIC);
	}

	public boolean is_iteration_on_collection() {
		return is_collection_iter_type();
	}

	public boolean is_common_nbr_iter_type() {
		return this == GMTYPE_NODEITER_COMMON_NBRS;
	}

	public boolean is_iteration_on_neighbors_compatible() {
		return is_any_nbr_node_iter_type();
	}

	public boolean is_node_set_type() {
		return (this == GMTYPE_NSET);
	}

	public boolean is_node_order_type() {
		return (this == GMTYPE_NORDER);
	}

	public boolean is_node_sequence_type() {
		return (this == GMTYPE_NSEQ);
	}

	public boolean is_edge_set_type() {
		return (this == GMTYPE_ESET);
	}

	public boolean is_edge_order_type() {
		return (this == GMTYPE_EORDER);
	}

	public boolean is_edge_sequence_type() {
		return (this == GMTYPE_ESEQ);
	}

	public boolean is_edge_collection_type() {
		return is_edge_set_type() || is_edge_order_type() || is_edge_sequence_type();
	}

	public boolean is_sequential_collection_type() {
		return is_sequence_collection_type() || is_order_collection_type();
	}
	
	public boolean is_map_type() {
		return this == GMTYPE_MAP;
	}

	// node set -> nodeset iter
	// edge set -> edgeset iter ...
	public GMTYPE_T get_natural_collection_iterator() {
		if (this == GMTYPE_NSET)
			return GMTYPE_NODEITER_SET;
		else if (this == GMTYPE_NSEQ)
			return GMTYPE_NODEITER_SEQ;
		else if (this == GMTYPE_NORDER)
			return GMTYPE_NODEITER_ORDER;
		else if (this == GMTYPE_ESET)
			return GMTYPE_NODEITER_SET;
		else if (this == GMTYPE_NSEQ)
			return GMTYPE_NODEITER_SEQ;
		else if (this == GMTYPE_EORDER)
			return GMTYPE_NODEITER_ORDER;
		else if (this == GMTYPE_COLLECTION)
			return GMTYPE_ITER_UNDERSPECIFIED; // handle that later
		else {
			assert false;
			return GMTYPE_INVALID;
		}
	}

	public GMTYPE_T get_specified_collection_iterator() {
		switch (this) {
		case GMTYPE_NSET:
		case GMTYPE_ESET:
			return GMTYPE_COLLECTIONITER_SET;
		case GMTYPE_NSEQ:
		case GMTYPE_ESEQ:
			return GMTYPE_COLLECTIONITER_SEQ;
		case GMTYPE_NORDER:
		case GMTYPE_EORDER:
			return GMTYPE_COLLECTIONITER_ORDER;
		default:
			assert false;
			return GMTYPE_INVALID;
		}
	}

	// return true if this type has a target graph
	public boolean has_target_graph_type() {
		return is_node_edge_compatible_type() || is_collection_type() || is_collection_of_collection_type();
		// any || node-edge || iterator || (including collection iterator)
	}

	public boolean is_node_edge_compatible_type() {
		return is_node_compatible_type() || is_edge_compatible_type();
	}

	public boolean equals(GMTYPE_T other) {
		return this == other;
	}

	public static boolean is_same_node_or_edge_compatible_type(GMTYPE_T i1, GMTYPE_T i2) {
		return (i1.is_node_compatible_type() && i2.is_node_compatible_type()) || (i1.is_edge_compatible_type() && i2.is_edge_compatible_type());
	}

	public static boolean collection_of_collection_compatible_type(GMTYPE_T def_src, GMTYPE_T source_type) {
		return def_src.is_order_collection_type() && source_type.is_collection_of_collection_type();
	}

	public boolean is_iteration_on_property() {
		return is_property_iter_type();
	}

	public boolean is_iteration_on_set() {
		return (this == GMTYPE_NODEITER_SET) || (this == GMTYPE_EDGEITER_SET);
	}

	public boolean is_iteration_on_order() {
		return (this == GMTYPE_NODEITER_ORDER) || (this == GMTYPE_EDGEITER_ORDER);
	}

	public boolean is_iteration_on_sequence() {
		return (this == GMTYPE_NODEITER_SEQ) || (this == GMTYPE_EDGEITER_SEQ);
	}

	public boolean is_iteration_on_all_graph() {
		return is_all_graph_iter_type();
	}

	public boolean is_iteration_on_out_neighbors() {
		return (this == GMTYPE_EDGEITER_NBRS) || (this == GMTYPE_NODEITER_NBRS);
	}

	public boolean is_iteration_on_in_neighbors() {
		return (this == GMTYPE_EDGEITER_IN_NBRS) || (this == GMTYPE_NODEITER_IN_NBRS);
	}

	public boolean is_iteration_on_up_neighbors() {
		return (this == GMTYPE_EDGEITER_UP_NBRS) || (this == GMTYPE_NODEITER_UP_NBRS);
	}

	public boolean is_iteration_on_down_neighbors() {
		return (this == GMTYPE_EDGEITER_DOWN_NBRS) || (this == GMTYPE_NODEITER_DOWN_NBRS);
	}

	public boolean is_iteration_use_reverse() {
		return is_iteration_on_in_neighbors() || is_iteration_on_up_neighbors();
	}

	public boolean is_iteration_bfs() {
		return (this == GMTYPE_EDGEITER_BFS) || (this == GMTYPE_NODEITER_BFS);
	}

	public boolean is_iteration_on_nodes() {
		return is_node_iter_type();
	}

	public boolean is_iteration_on_edges() {
		return is_edge_iter_type();
	}

	public boolean is_iteration_on_updown_levels() {
		return is_iteration_on_up_neighbors() || is_iteration_on_down_neighbors();
	}

	public boolean is_collection_iter_type() {
		return is_node_collection_iter_type() || is_edge_collection_iter_type() || is_unknown_collection_iter_type() || is_collection_of_collection_iter_type();
	}

	public boolean is_property_iter_set_type() {
		return this == GMTYPE_PROPERTYITER_SET;
	}

	public boolean is_property_iter_seq_type() {
		return this == GMTYPE_PROPERTYITER_SEQ;
	}

	public boolean is_property_iter_order_type() {
		return this == GMTYPE_PROPERTYITER_ORDER;
	}

	public boolean is_property_iter_type() {
		return is_property_iter_order_type() || is_property_iter_seq_type() || is_property_iter_set_type();
	}

	public boolean is_boolean_type() {
		return this == GMTYPE_BOOL;
	}

	public boolean is_unknonwn_type() {
		return (this == GMTYPE_UNKNOWN) || (this == GMTYPE_UNKNOWN_NUMERIC);
	}

	public boolean is_void_type() {
		return (this == GMTYPE_VOID);
	}

	public boolean is_node_property_type() {
		return (this == GMTYPE_NODEPROP);
	}

	public boolean is_edge_property_type() {
		return (this == GMTYPE_EDGEPROP);
	}

	public boolean is_property_type() {
		return is_node_property_type() || is_edge_property_type();
	}

	public boolean is_inf_type() {
		return (this == GMTYPE_INF) || (this == GMTYPE_INF_INT) || (this == GMTYPE_INF_LONG) || (this == GMTYPE_INF_FLOAT) || (this == GMTYPE_INF_FLOAT);
	}

	public boolean is_inf_type_unsized() {
		return (this == GMTYPE_INF);
	}

	public boolean is_inf_type_sized() {
		return is_inf_type() && !is_inf_type_unsized();
	}

	public GMTYPE_T get_sized_inf_type() {
		switch (this) {
		case GMTYPE_INT:
			return GMTYPE_INF_INT;
		case GMTYPE_LONG:
			return GMTYPE_INF_LONG;
		case GMTYPE_FLOAT:
			return GMTYPE_INF_FLOAT;
		case GMTYPE_DOUBLE:
			return GMTYPE_INF_DOUBLE;
		default:
			assert false;
			return GMTYPE_INVALID;
		}
	}

	public boolean is_inout_nbr_node_iter_type() {
		return (this == GMTYPE_NODEITER_NBRS) || (this == GMTYPE_NODEITER_IN_NBRS);
	}

	public boolean is_any_nbr_node_iter_type() {
		return (this == GMTYPE_NODEITER_NBRS) || (this == GMTYPE_NODEITER_IN_NBRS) || (this == GMTYPE_NODEITER_UP_NBRS) || (this == GMTYPE_NODEITER_DOWN_NBRS)
				|| (this == GMTYPE_NODEITER_COMMON_NBRS);
	}

	public boolean is_any_nbr_edge_iter_type() {
		return (this == GMTYPE_EDGEITER_NBRS) || (this == GMTYPE_EDGEITER_IN_NBRS) || (this == GMTYPE_EDGEITER_UP_NBRS) || (this == GMTYPE_EDGEITER_DOWN_NBRS);
	}

	public boolean is_any_nbr_iter_type() {
		return is_any_nbr_edge_iter_type() || is_any_nbr_node_iter_type();
	}

	public boolean is_edge_type() {
		return (this == GMTYPE_EDGE);
	}

	public boolean is_int_type() {
		return (this == GMTYPE_INT);
	}

	public boolean is_long_type() {
		return (this == GMTYPE_LONG);
	}

	public boolean is_nil_type() {
		return (this == GMTYPE_NIL_UNKNOWN) || (this == GMTYPE_NIL_NODE) || (this == GMTYPE_NIL_EDGE);
	}

	public boolean is_all_graph_node_iter_type() {
		// [XXX] to be finxed
		return (this == GMTYPE_NODEITER_ALL) || (this == GMTYPE_NODEITER_BFS);
	}

	public boolean is_all_graph_edge_iter_type() {
		return (this == GMTYPE_EDGEITER_ALL) || (this == GMTYPE_EDGEITER_BFS);
	}

	public boolean is_all_graph_iter_type() {
		return is_all_graph_node_iter_type() || is_all_graph_edge_iter_type();
	}

	public boolean is_node_collection_iter_type() {
		return ((this == GMTYPE_NODEITER_SET) || (this == GMTYPE_NODEITER_SEQ) || (this == GMTYPE_NODEITER_ORDER));
	}

	public boolean is_edge_collection_iter_type() {
		return ((this == GMTYPE_EDGEITER_SET) || (this == GMTYPE_EDGEITER_SEQ) || (this == GMTYPE_EDGEITER_ORDER));
	}

	public boolean is_unknown_collection_iter_type() {
		return (this == GMTYPE_ITER_ANY);
	}

	public boolean is_collection_of_set_iter_type() {
		return this == GMTYPE_COLLECTIONITER_SET;
	}

	public boolean is_collection_of_seq_iter_type() {
		return this == GMTYPE_COLLECTIONITER_SEQ;
	}

	public boolean is_collection_of_order_iter_type() {
		return this == GMTYPE_COLLECTIONITER_ORDER;
	}

	public boolean is_collection_of_collection_iter_type() {
		return is_collection_of_set_iter_type() || is_collection_of_order_iter_type() || is_collection_of_seq_iter_type()
				|| is_collection_of_collection_iter_type();
	}

	public boolean can_be_key_type() {
		return is_prim_type() || is_nodeedge_type();
	}

	public boolean can_be_value_type() {
		return is_prim_type() || is_nodeedge_type();
	}

	/**
	 * checking to apply op (including assignment) between two types. (not
	 * including target-graph checking) boolean gm_is_compatible_type(int op,
	 * int t1, int t2, RefObject<int> op_result_type, RefObject<int> t1_coerced,
	 * RefObject<int> t2_coerced, RefObject<boolean> t1_coerced_lost_precision,
	 * RefObject<boolean> t2_coerced_lost_precision);
	 */
	public static boolean gm_is_compatible_type_for_assign(GMTYPE_T t_lhs, GMTYPE_T t_rhs, RefObject<GMTYPE_T> t_new_rhs, RefObject<Boolean> warning) {
		RefObject<GMTYPE_T> tempRef_dummy1 = new RefObject<GMTYPE_T>(GMTYPE_INVALID);
		RefObject<GMTYPE_T> tempRef_dummy2 = new RefObject<GMTYPE_T>(GMTYPE_INVALID);
		RefObject<Boolean> tempRef_dummy_b = new RefObject<Boolean>(true);
		return Oprules.gm_is_compatible_type(GM_OPS_T.GMOP_ASSIGN, t_lhs, t_rhs, tempRef_dummy1, tempRef_dummy2, t_new_rhs, tempRef_dummy_b, warning);
	}

	public static boolean is_t2_larger_than_t1(GMTYPE_T t1, GMTYPE_T t2) {
		if ((t1 == GMTYPE_INT) && (t2 == GMTYPE_LONG))
			return true;
		if ((t1 == GMTYPE_FLOAT) && (t2 == GMTYPE_DOUBLE))
			return true;
		return false;
	}

	public static GMTYPE_T get_iter_type_from_set_type(GMTYPE_T set_type) {
		switch (set_type) {
		case GMTYPE_NSET:
			return GMTYPE_NODEITER_SET;
		case GMTYPE_NSEQ:
			return GMTYPE_NODEITER_SEQ;
		case GMTYPE_NORDER:
			return GMTYPE_NODEITER_ORDER;
		case GMTYPE_ESET:
			return GMTYPE_NODEITER_SET;
		case GMTYPE_ESEQ:
			return GMTYPE_NODEITER_SEQ;
		case GMTYPE_EORDER:
			return GMTYPE_NODEITER_ORDER;
		default:
			assert false;
			return GMTYPE_INVALID;
		}
	}

	public String get_type_string() {
		switch (this) {
		case GMTYPE_GRAPH:
			return "Graph";
		case GMTYPE_BYTE:
			return "Byte";
		case GMTYPE_SHORT:
			return "Short";
		case GMTYPE_INT:
			return "Int";
		case GMTYPE_LONG:
			return "Long";
		case GMTYPE_FLOAT:
			return "Float";
		case GMTYPE_DOUBLE:
			return "Double";
		case GMTYPE_NODEPROP:
			return "NP";
		case GMTYPE_EDGEPROP:
			return "EP";
		case GMTYPE_NODE:
			return "Node";
		case GMTYPE_EDGE:
			return "Edge";
		case GMTYPE_NODEITER_ALL:
			return "Node::I(ALL)";
		case GMTYPE_NODEITER_NBRS:
			return "Node::I(NBR)";
		case GMTYPE_NODEITER_IN_NBRS:
			return "Node::I(IN_NBR)";
		case GMTYPE_NODEITER_BFS:
			return "Node::I(BFS)";
		case GMTYPE_NODEITER_UP_NBRS:
			return "Node::I(+1)";
		case GMTYPE_NODEITER_DOWN_NBRS:
			return "Node::I(-1)";
		case GMTYPE_EDGEITER_ALL:
			return "EdgeI";
		case GMTYPE_EDGEITER_NBRS:
			return "EdgeI";
		case GMTYPE_BOOL:
			return "Bool";
		case GMTYPE_NSET:
			return "Node_Set";
		case GMTYPE_NORDER:
			return "Node_Order";
		case GMTYPE_NSEQ:
			return "Node_Sequence";
		case GMTYPE_ESET:
			return "Edge_Set";
		case GMTYPE_EORDER:
			return "Edge_Order";
		case GMTYPE_ESEQ:
			return "Edge_Sequence";
		case GMTYPE_INF:
		case GMTYPE_INF_INT:
		case GMTYPE_INF_LONG:
		case GMTYPE_INF_DOUBLE:
		case GMTYPE_INF_FLOAT:
			return "INF";
		case GMTYPE_NODEITER_SET:
			return "N_S::I";
		case GMTYPE_NODEITER_SEQ:
			return "N_Q::I";
		case GMTYPE_NODEITER_ORDER:
			return "N_O::I";
		case GMTYPE_EDGEITER_SET:
			return "E_S::I";
		case GMTYPE_EDGEITER_SEQ:
			return "E_Q::I";
		case GMTYPE_EDGEITER_ORDER:
			return "E_O::I";
		case GMTYPE_ITER_ANY:
			return "Collection::I";
		case GMTYPE_VOID:
			return "Void";
		case GMTYPE_MAP:
			return "Map";
		default: // printf("%d\n",t); assert(false);
			return "Unknown";
		}
	}

	public String get_iter_type_string() {
		switch (this) {
		case GMTYPE_NODEITER_ALL:
			return "Nodes";
		case GMTYPE_NODEITER_NBRS:
			return "Nbrs";
		case GMTYPE_EDGEITER_ALL:
			return "Edges";
		case GMTYPE_EDGEITER_NBRS:
			return "Nbr_Edges";
		case GMTYPE_NODEITER_UP_NBRS:
			return "UpNbrs";
		case GMTYPE_NODEITER_DOWN_NBRS:
			return "DownNbrs";
		case GMTYPE_NODEITER_IN_NBRS:
			return "InNbrs";
		case GMTYPE_NODEITER_COMMON_NBRS:
			return "CommonNbrs";
		case GMTYPE_NODEITER_SET:
		case GMTYPE_NODEITER_SEQ:
		case GMTYPE_NODEITER_ORDER:
		case GMTYPE_ITER_ANY:
			return "Items";
		default:
			assert false;
			return "Unknown";
		}
	}

	public static GMTYPE_T gm_get_type_from_string(String s) {
		assert s != null;
		if (s.equals("Graph"))
			return GMTYPE_GRAPH;
		else if (s.equals("Node"))
			return GMTYPE_NODE;
		else if (s.equals("Edge"))
			return GMTYPE_EDGE;
		else if (s.equals("NI_All"))
			return GMTYPE_NODEITER_ALL;
		else if (s.equals("EI_All"))
			return GMTYPE_EDGEITER_ALL;
		else if (s.equals("NI_Out"))
			return GMTYPE_NODEITER_NBRS;
		else if (s.equals("NI_In"))
			return GMTYPE_NODEITER_IN_NBRS;
		else if (s.equals("NI_Up"))
			return GMTYPE_NODEITER_UP_NBRS;
		else if (s.equals("NI_Down"))
			return GMTYPE_NODEITER_DOWN_NBRS;
		else if (s.equals("Int"))
			return GMTYPE_INT;
		else if (s.equals("Long"))
			return GMTYPE_LONG;
		else if (s.equals("Float"))
			return GMTYPE_FLOAT;
		else if (s.equals("Double"))
			return GMTYPE_DOUBLE;
		else if (s.equals("N_S"))
			return GMTYPE_NSET;
		else if (s.equals("E_S"))
			return GMTYPE_ESET;
		else if (s.equals("N_O"))
			return GMTYPE_NORDER;
		else if (s.equals("E_O"))
			return GMTYPE_EORDER;
		else if (s.equals("N_Q"))
			return GMTYPE_NSEQ;
		else if (s.equals("E_Q"))
			return GMTYPE_ESEQ;
		else if (s.equals("Void"))
			return GMTYPE_VOID;
		else if (s.equals("Bool"))
			return GMTYPE_BOOL;
		else if (s.equals("Map"))
			return GMTYPE_MAP;
		else if (s.equals("Generic"))
			return GMTYPE_GENERIC;
		else {
			assert false;
			throw new AssertionError();
		}
	}
}