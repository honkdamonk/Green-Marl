package inc;

import frontend.GlobalMembersGm_typecheck_oprules;

public class GlobalMembersGm_defs {

	@Deprecated
	public static boolean gm_is_foreign_expr_type(GMTYPE_T i) {
		return (i == GMTYPE_T.GMTYPE_FOREIGN_EXPR);
	}

	@Deprecated
	public static boolean gm_is_integer_type(GMTYPE_T i) {
		return (i == GMTYPE_T.GMTYPE_INT) || (i == GMTYPE_T.GMTYPE_LONG) || (i == GMTYPE_T.GMTYPE_BYTE) || (i == GMTYPE_T.GMTYPE_SHORT);
	}

	@Deprecated
	public static boolean gm_is_float_type(GMTYPE_T i) {
		return (i == GMTYPE_T.GMTYPE_FLOAT) || (i == GMTYPE_T.GMTYPE_DOUBLE);
	}

	@Deprecated
	public static boolean gm_is_unknown_type(GMTYPE_T i) {
		return (i == GMTYPE_T.GMTYPE_UNKNOWN) || (i == GMTYPE_T.GMTYPE_UNKNOWN_NUMERIC);
	}

	@Deprecated
	public static boolean gm_is_numeric_type(GMTYPE_T i) {
		return GlobalMembersGm_defs.gm_is_integer_type(i) || GlobalMembersGm_defs.gm_is_float_type(i);
	}

	@Deprecated
	public static boolean gm_is_nodeedge_type(GMTYPE_T i) {
		return (i == GMTYPE_T.GMTYPE_NODE) || (i == GMTYPE_T.GMTYPE_EDGE);
	}

	@Deprecated
	public static boolean gm_is_node_type(GMTYPE_T i) {
		return (i == GMTYPE_T.GMTYPE_NODE);
	}

	@Deprecated
	public static boolean gm_is_edge_type(GMTYPE_T i) {
		return (i == GMTYPE_T.GMTYPE_EDGE);
	}

	@Deprecated
	public static boolean gm_is_int_type(GMTYPE_T i) {
		return (i == GMTYPE_T.GMTYPE_INT);
	}

	@Deprecated
	public static boolean gm_is_long_type(GMTYPE_T i) {
		return (i == GMTYPE_T.GMTYPE_LONG);
	}

	@Deprecated
	public static boolean gm_is_nil_type(GMTYPE_T i) {
		return (i == GMTYPE_T.GMTYPE_NIL_UNKNOWN) || (i == GMTYPE_T.GMTYPE_NIL_NODE) || (i == GMTYPE_T.GMTYPE_NIL_EDGE);
	}

	@Deprecated
	public static boolean gm_is_all_graph_node_iter_type(GMTYPE_T i) {
		return (i == GMTYPE_T.GMTYPE_NODEITER_ALL) || (i == GMTYPE_T.GMTYPE_NODEITER_BFS); // [XXX]
																							// to
																							// be
																							// finxed
	}

	@Deprecated
	public static boolean gm_is_all_graph_edge_iter_type(GMTYPE_T i) {
		return (i == GMTYPE_T.GMTYPE_EDGEITER_ALL) || (i == GMTYPE_T.GMTYPE_EDGEITER_BFS);
	}

	@Deprecated
	public static boolean gm_is_all_graph_iter_type(GMTYPE_T i) {
		return GlobalMembersGm_defs.gm_is_all_graph_node_iter_type(i) || GlobalMembersGm_defs.gm_is_all_graph_edge_iter_type(i);
	}

	@Deprecated
	public static boolean gm_is_inout_nbr_node_iter_type(GMTYPE_T i) {
		return (i == GMTYPE_T.GMTYPE_NODEITER_NBRS) || (i == GMTYPE_T.GMTYPE_NODEITER_IN_NBRS);
	}

	@Deprecated
	public static boolean gm_is_any_nbr_node_iter_type(GMTYPE_T i) {
		return (i == GMTYPE_T.GMTYPE_NODEITER_NBRS) || (i == GMTYPE_T.GMTYPE_NODEITER_IN_NBRS) || (i == GMTYPE_T.GMTYPE_NODEITER_UP_NBRS)
				|| (i == GMTYPE_T.GMTYPE_NODEITER_DOWN_NBRS) || (i == GMTYPE_T.GMTYPE_NODEITER_COMMON_NBRS);
	}

	@Deprecated
	public static boolean gm_is_any_nbr_edge_iter_type(GMTYPE_T i) {
		return (i == GMTYPE_T.GMTYPE_EDGEITER_NBRS) || (i == GMTYPE_T.GMTYPE_EDGEITER_IN_NBRS) || (i == GMTYPE_T.GMTYPE_EDGEITER_UP_NBRS)
				|| (i == GMTYPE_T.GMTYPE_EDGEITER_DOWN_NBRS);
	}

	@Deprecated
	public static boolean gm_is_any_nbr_iter_type(GMTYPE_T i) {
		return GlobalMembersGm_defs.gm_is_any_nbr_edge_iter_type(i) || GlobalMembersGm_defs.gm_is_any_nbr_node_iter_type(i);
	}

	@Deprecated
	public static boolean gm_is_common_nbr_iter_type(GMTYPE_T i) {
		return (i == GMTYPE_T.GMTYPE_NODEITER_COMMON_NBRS);
	}

	@Deprecated
	public static boolean gm_is_node_iter_type(GMTYPE_T i) {
		return GlobalMembersGm_defs.gm_is_all_graph_node_iter_type(i) || GlobalMembersGm_defs.gm_is_any_nbr_node_iter_type(i);
	}

	@Deprecated
	public static boolean gm_is_edge_iter_type(GMTYPE_T i) {
		return GlobalMembersGm_defs.gm_is_all_graph_edge_iter_type(i) || GlobalMembersGm_defs.gm_is_any_nbr_edge_iter_type(i);
	}

	@Deprecated
	public static boolean gm_is_node_collection_iter_type(GMTYPE_T i) {
		return ((i == GMTYPE_T.GMTYPE_NODEITER_SET) || (i == GMTYPE_T.GMTYPE_NODEITER_SEQ) || (i == GMTYPE_T.GMTYPE_NODEITER_ORDER));
	}

	@Deprecated
	public static boolean gm_is_edge_collection_iter_type(GMTYPE_T i) {
		return ((i == GMTYPE_T.GMTYPE_EDGEITER_SET) || (i == GMTYPE_T.GMTYPE_EDGEITER_SEQ) || (i == GMTYPE_T.GMTYPE_EDGEITER_ORDER));
	}

	@Deprecated
	public static boolean gm_is_unknown_collection_iter_type(GMTYPE_T i) {
		return (i == GMTYPE_T.GMTYPE_ITER_ANY);
	}

	@Deprecated
	public static boolean gm_is_collection_of_set_iter_type(GMTYPE_T i) {
		return i == GMTYPE_T.GMTYPE_COLLECTIONITER_SET;
	}

	@Deprecated
	public static boolean gm_is_collection_of_seq_iter_type(GMTYPE_T i) {
		return i == GMTYPE_T.GMTYPE_COLLECTIONITER_SEQ;
	}

	@Deprecated
	public static boolean gm_is_collection_of_order_iter_type(GMTYPE_T i) {
		return i == GMTYPE_T.GMTYPE_COLLECTIONITER_ORDER;
	}

	@Deprecated
	public static boolean gm_is_collection_of_collection_iter_type(GMTYPE_T i) {
		return GlobalMembersGm_defs.gm_is_collection_of_set_iter_type(i) || GlobalMembersGm_defs.gm_is_collection_of_order_iter_type(i)
				|| GlobalMembersGm_defs.gm_is_collection_of_seq_iter_type(i);
	}

	@Deprecated
	public static boolean gm_is_collection_iter_type(GMTYPE_T i) {
		return GlobalMembersGm_defs.gm_is_node_collection_iter_type(i) || GlobalMembersGm_defs.gm_is_edge_collection_iter_type(i)
				|| GlobalMembersGm_defs.gm_is_unknown_collection_iter_type(i) || GlobalMembersGm_defs.gm_is_collection_of_collection_iter_type(i);
	}

	@Deprecated
	public static boolean gm_is_property_iter_set_type(GMTYPE_T i) {
		return i == GMTYPE_T.GMTYPE_PROPERTYITER_SET;
	}

	@Deprecated
	public static boolean gm_is_property_iter_seq_type(GMTYPE_T i) {
		return i == GMTYPE_T.GMTYPE_PROPERTYITER_SEQ;
	}

	@Deprecated
	public static boolean gm_is_property_iter_order_type(GMTYPE_T i) {
		return i == GMTYPE_T.GMTYPE_PROPERTYITER_ORDER;
	}

	@Deprecated
	public static boolean gm_is_property_iter_type(GMTYPE_T i) {
		return GlobalMembersGm_defs.gm_is_property_iter_order_type(i) || GlobalMembersGm_defs.gm_is_property_iter_seq_type(i)
				|| GlobalMembersGm_defs.gm_is_property_iter_set_type(i);
	}

	@Deprecated
	public static boolean gm_is_node_compatible_type(GMTYPE_T i) {
		return GlobalMembersGm_defs.gm_is_node_type(i) || GlobalMembersGm_defs.gm_is_node_iter_type(i)
				|| GlobalMembersGm_defs.gm_is_node_collection_iter_type(i) || GlobalMembersGm_defs.gm_is_nil_type(i);
	}

	@Deprecated
	public static boolean gm_is_edge_compatible_type(GMTYPE_T i) {
		return GlobalMembersGm_defs.gm_is_edge_type(i) || GlobalMembersGm_defs.gm_is_edge_iter_type(i)
				|| GlobalMembersGm_defs.gm_is_edge_collection_iter_type(i) || GlobalMembersGm_defs.gm_is_nil_type(i);
	}

	@Deprecated
	public static boolean gm_is_node_edge_compatible_type(GMTYPE_T i) {
		return GlobalMembersGm_defs.gm_is_node_compatible_type(i) || GlobalMembersGm_defs.gm_is_edge_compatible_type(i);
	}

	@Deprecated
	public static boolean gm_is_iter_type(GMTYPE_T i) {
		return GlobalMembersGm_defs.gm_is_node_iter_type(i) || GlobalMembersGm_defs.gm_is_edge_iter_type(i)
				|| GlobalMembersGm_defs.gm_is_collection_iter_type(i);
	}

	@Deprecated
	public static boolean gm_is_boolean_type(GMTYPE_T i) {
		return (i == GMTYPE_T.GMTYPE_BOOL);
	}

	@Deprecated
	public static boolean gm_is_unknonwn_type(GMTYPE_T i) {
		return (i == GMTYPE_T.GMTYPE_UNKNOWN) || (i == GMTYPE_T.GMTYPE_UNKNOWN_NUMERIC);
	}

	@Deprecated
	public static boolean gm_is_void_type(GMTYPE_T i) {
		return (i == GMTYPE_T.GMTYPE_VOID);
	}

	@Deprecated
	public static boolean gm_is_prim_type(GMTYPE_T i) {
		return GlobalMembersGm_defs.gm_is_numeric_type(i) || GlobalMembersGm_defs.gm_is_boolean_type(i);
	}

	@Deprecated
	public static boolean gm_is_graph_type(GMTYPE_T i) {
		return (i == GMTYPE_T.GMTYPE_GRAPH);
	}

	@Deprecated
	public static boolean gm_is_node_property_type(GMTYPE_T i) {
		return (i == GMTYPE_T.GMTYPE_NODEPROP);
	}

	@Deprecated
	public static boolean gm_is_edge_property_type(GMTYPE_T i) {
		return (i == GMTYPE_T.GMTYPE_EDGEPROP);
	}

	@Deprecated
	public static boolean gm_is_property_type(GMTYPE_T i) {
		return GlobalMembersGm_defs.gm_is_node_property_type(i) || GlobalMembersGm_defs.gm_is_edge_property_type(i);
	}

	@Deprecated
	public static boolean gm_is_inf_type(GMTYPE_T i) {
		return (i == GMTYPE_T.GMTYPE_INF) || (i == GMTYPE_T.GMTYPE_INF_INT) || (i == GMTYPE_T.GMTYPE_INF_LONG) || (i == GMTYPE_T.GMTYPE_INF_FLOAT)
				|| (i == GMTYPE_T.GMTYPE_INF_FLOAT);
	}

	@Deprecated
	public static boolean gm_is_inf_type_unsized(GMTYPE_T i) {
		return (i == GMTYPE_T.GMTYPE_INF);
	}

	@Deprecated
	public static boolean gm_is_inf_type_sized(GMTYPE_T i) {
		return GlobalMembersGm_defs.gm_is_inf_type(i) && !GlobalMembersGm_defs.gm_is_inf_type_unsized(i);
	}

	@Deprecated
	public static GMTYPE_T gm_get_sized_inf_type(GMTYPE_T i) {
		if (i == GMTYPE_T.GMTYPE_INT)
			return GMTYPE_T.GMTYPE_INF_INT;
		else if (i == GMTYPE_T.GMTYPE_LONG)
			return GMTYPE_T.GMTYPE_INF_LONG;
		else if (i == GMTYPE_T.GMTYPE_FLOAT)
			return GMTYPE_T.GMTYPE_INF_FLOAT;
		else if (i == GMTYPE_T.GMTYPE_DOUBLE)
			return GMTYPE_T.GMTYPE_INF_DOUBLE;
		else {
			assert false;
			return GMTYPE_T.GMTYPE_INVALID;
		}
	}

	@Deprecated
	public static boolean gm_is_node_set_type(GMTYPE_T i) {
		return (i == GMTYPE_T.GMTYPE_NSET);
	}

	@Deprecated
	public static boolean gm_is_node_order_type(GMTYPE_T i) {
		return (i == GMTYPE_T.GMTYPE_NORDER);
	}

	@Deprecated
	public static boolean gm_is_node_sequence_type(GMTYPE_T i) {
		return (i == GMTYPE_T.GMTYPE_NSEQ);
	}

	@Deprecated
	public static boolean gm_is_edge_set_type(GMTYPE_T i) {
		return (i == GMTYPE_T.GMTYPE_ESET);
	}

	@Deprecated
	public static boolean gm_is_edge_order_type(GMTYPE_T i) {
		return (i == GMTYPE_T.GMTYPE_EORDER);
	}

	@Deprecated
	public static boolean gm_is_edge_sequence_type(GMTYPE_T i) {
		return (i == GMTYPE_T.GMTYPE_ESEQ);
	}

	@Deprecated
	public static boolean gm_is_node_collection_type(GMTYPE_T i) {
		return GlobalMembersGm_defs.gm_is_node_set_type(i) || GlobalMembersGm_defs.gm_is_node_order_type(i) || GlobalMembersGm_defs.gm_is_node_sequence_type(i);
	}

	@Deprecated
	public static boolean gm_is_edge_collection_type(GMTYPE_T i) {
		return GlobalMembersGm_defs.gm_is_edge_set_type(i) || GlobalMembersGm_defs.gm_is_edge_order_type(i) || GlobalMembersGm_defs.gm_is_edge_sequence_type(i);
	}

	@Deprecated
	public static boolean gm_is_set_collection_type(GMTYPE_T i) {
		return GlobalMembersGm_defs.gm_is_node_set_type(i) || GlobalMembersGm_defs.gm_is_edge_set_type(i);
	}

	@Deprecated
	public static boolean gm_is_order_collection_type(GMTYPE_T i) {
		return GlobalMembersGm_defs.gm_is_node_order_type(i) || GlobalMembersGm_defs.gm_is_edge_order_type(i);
	}

	@Deprecated
	public static boolean gm_is_sequence_collection_type(GMTYPE_T i) {
		return GlobalMembersGm_defs.gm_is_node_sequence_type(i) || GlobalMembersGm_defs.gm_is_edge_sequence_type(i);
	}

	@Deprecated
	public static boolean gm_is_collection_of_collection_type(GMTYPE_T type) {
		return type == GMTYPE_T.GMTYPE_COLLECTION;
	}

	@Deprecated
	public static boolean gm_is_collection_type(GMTYPE_T i) {
		return GlobalMembersGm_defs.gm_is_node_collection_type(i) || GlobalMembersGm_defs.gm_is_edge_collection_type(i)
				|| GlobalMembersGm_defs.gm_is_collection_of_collection_type(i);
	}

	@Deprecated
	public static boolean gm_is_sequential_collection_type(GMTYPE_T i) {
		return GlobalMembersGm_defs.gm_is_sequence_collection_type(i) || GlobalMembersGm_defs.gm_is_order_collection_type(i);
	}

	// node set -> nodeset iter
	// edge set -> edgeset iter ...
	@Deprecated
	public static GMTYPE_T gm_get_natural_collection_iterator(GMTYPE_T src_type) {
		if (src_type == GMTYPE_T.GMTYPE_NSET)
			return GMTYPE_T.GMTYPE_NODEITER_SET;
		else if (src_type == GMTYPE_T.GMTYPE_NSEQ)
			return GMTYPE_T.GMTYPE_NODEITER_SEQ;
		else if (src_type == GMTYPE_T.GMTYPE_NORDER)
			return GMTYPE_T.GMTYPE_NODEITER_ORDER;
		else if (src_type == GMTYPE_T.GMTYPE_ESET)
			return GMTYPE_T.GMTYPE_NODEITER_SET;
		else if (src_type == GMTYPE_T.GMTYPE_NSEQ)
			return GMTYPE_T.GMTYPE_NODEITER_SEQ;
		else if (src_type == GMTYPE_T.GMTYPE_EORDER)
			return GMTYPE_T.GMTYPE_NODEITER_ORDER;
		else if (src_type == GMTYPE_T.GMTYPE_COLLECTION)
			return GMTYPE_T.GMTYPE_ITER_UNDERSPECIFIED; // handle that later
		else {
			assert false;
			return GMTYPE_T.GMTYPE_INVALID;
		}
	}

	@Deprecated
	public static GMTYPE_T gm_get_specified_collection_iterator(GMTYPE_T type) {
		switch (type) {
		case GMTYPE_NSET:
		case GMTYPE_ESET:
			return GMTYPE_T.GMTYPE_COLLECTIONITER_SET;
		case GMTYPE_NSEQ:
		case GMTYPE_ESEQ:
			return GMTYPE_T.GMTYPE_COLLECTIONITER_SEQ;
		case GMTYPE_NORDER:
		case GMTYPE_EORDER:
			return GMTYPE_T.GMTYPE_COLLECTIONITER_ORDER;
		default:
			assert false;
			return GMTYPE_T.GMTYPE_INVALID;
		}
	}

	// return true if this type has a target graph
	@Deprecated
	public static boolean gm_has_target_graph_type(GMTYPE_T t) {
		return GlobalMembersGm_defs.gm_is_node_edge_compatible_type(t) || GlobalMembersGm_defs.gm_is_collection_type(t)
				|| GlobalMembersGm_defs.gm_is_collection_of_collection_type(t); // any
																				// node-edge
																				// iterator
																				// (including
																				// collection
																				// iterator)
	}

	@Deprecated
	public static boolean gm_is_same_type(GMTYPE_T i1, GMTYPE_T i2) {
		return (i1 == i2);
	}

	@Deprecated
	public static boolean gm_is_same_node_or_edge_compatible_type(GMTYPE_T i1, GMTYPE_T i2) {
		return (GlobalMembersGm_defs.gm_is_node_compatible_type(i1) && GlobalMembersGm_defs.gm_is_node_compatible_type(i2))
				|| (GlobalMembersGm_defs.gm_is_edge_compatible_type(i1) && GlobalMembersGm_defs.gm_is_edge_compatible_type(i2));
	}

	@Deprecated
	public static boolean gm_collection_of_collection_compatible_type(GMTYPE_T def_src, GMTYPE_T source_type) {
		return GlobalMembersGm_defs.gm_is_order_collection_type(def_src) && GlobalMembersGm_defs.gm_is_collection_of_collection_type(source_type);
	}

	// see http://cppreference.com/wiki/language/operator_precedence
	public static int[] GM_OPPRED_LEVEL = { 2, 3, 5, 5, 5, 2, 2, 6, 6, 13, 13, 3, 9, 9, 8, 8, 8, 8, 2, 15, 99 }; // ABS
																													// (not
																													// in
																													// cpp)

	@Deprecated
	public static boolean gm_is_numeric_op(GM_OPS_T i) {
		return (i == GM_OPS_T.GMOP_MULT) || (i == GM_OPS_T.GMOP_DIV) || (i == GM_OPS_T.GMOP_MOD) || (i == GM_OPS_T.GMOP_ADD) || (i == GM_OPS_T.GMOP_SUB)
				|| (i == GM_OPS_T.GMOP_NEG) || (i == GM_OPS_T.GMOP_ABS) || (i == GM_OPS_T.GMOP_MAX) || (i == GM_OPS_T.GMOP_MIN);
	}

	@Deprecated
	public static boolean gm_is_boolean_op(GM_OPS_T i) {
		return (i == GM_OPS_T.GMOP_NOT) || (i == GM_OPS_T.GMOP_AND) || (i == GM_OPS_T.GMOP_OR);
	}

	@Deprecated
	public static boolean gm_is_eq_op(GM_OPS_T i) {
		return (i == GM_OPS_T.GMOP_EQ) || (i == GM_OPS_T.GMOP_NEQ);
	}

	@Deprecated
	public static boolean gm_is_less_op(GM_OPS_T i) {
		return (i == GM_OPS_T.GMOP_GT) || (i == GM_OPS_T.GMOP_LT) || (i == GM_OPS_T.GMOP_GE) || (i == GM_OPS_T.GMOP_LE);
	}

	@Deprecated
	public static boolean gm_is_eq_or_less_op(GM_OPS_T i) {
		return GlobalMembersGm_defs.gm_is_eq_op(i) || GlobalMembersGm_defs.gm_is_less_op(i);
	}

	@Deprecated
	public static boolean gm_is_ternary_op(GM_OPS_T i) {
		return (i == GM_OPS_T.GMOP_TER);
	}

	// checking to apply op (including assignment) between two types.
	// (not including target-graph checking)
	// boolean gm_is_compatible_type(int op, int t1, int t2,
	// tangible.RefObject<int> op_result_type, tangible.RefObject<int>
	// t1_coerced, tangible.RefObject<int> t2_coerced,
	// tangible.RefObject<boolean> t1_coerced_lost_precision,
	// tangible.RefObject<boolean> t2_coerced_lost_precision);

	public static boolean gm_is_compatible_type_for_assign(GMTYPE_T t_lhs, GMTYPE_T t_rhs, tangible.RefObject<Integer> t_new_rhs,
			tangible.RefObject<Boolean> warning) {
		// int dummy1;
		// int dummy2;
		// boolean dummy_b;
		tangible.RefObject<GMTYPE_T> tempRef_dummy1 = new tangible.RefObject<GMTYPE_T>(GMTYPE_T.GMTYPE_INVALID);
		tangible.RefObject<GMTYPE_T> tempRef_dummy2 = new tangible.RefObject<GMTYPE_T>(GMTYPE_T.GMTYPE_INVALID);
		tangible.RefObject<Boolean> tempRef_dummy_b = new tangible.RefObject<Boolean>(true);
		return GlobalMembersGm_typecheck_oprules.gm_is_compatible_type(GM_OPS_T.GMOP_ASSIGN, t_lhs, t_rhs, tempRef_dummy1, tempRef_dummy2, t_new_rhs,
				tempRef_dummy_b, warning);
		// dummy1 = tempRef_dummy1.argvalue;
		// dummy2 = tempRef_dummy2.argvalue;
		// dummy_b = tempRef_dummy_b.argvalue; dont need that?
	}

	@Deprecated
	public static boolean gm_is_strict_reduce_op(GM_REDUCE_T t) {
		return (t == GM_REDUCE_T.GMREDUCE_PLUS) || (t == GM_REDUCE_T.GMREDUCE_MULT) || (t == GM_REDUCE_T.GMREDUCE_MIN) || (t == GM_REDUCE_T.GMREDUCE_MAX)
				|| (t == GM_REDUCE_T.GMREDUCE_AND) || (t == GM_REDUCE_T.GMREDUCE_OR) || (t == GM_REDUCE_T.GMREDUCE_AVG);
	}

	@Deprecated
	public static boolean gm_is_numeric_reduce_op(GM_REDUCE_T t) {
		return (t == GM_REDUCE_T.GMREDUCE_PLUS) || (t == GM_REDUCE_T.GMREDUCE_MULT) || (t == GM_REDUCE_T.GMREDUCE_MIN) || (t == GM_REDUCE_T.GMREDUCE_MAX)
				|| (t == GM_REDUCE_T.GMREDUCE_AVG);
	}

	@Deprecated
	public static boolean gm_is_boolean_reduce_op(GM_REDUCE_T t) {
		return (t == GM_REDUCE_T.GMREDUCE_AND) || (t == GM_REDUCE_T.GMREDUCE_OR);
	}

	@Deprecated
	public static boolean gm_is_iteration_on_collection(GMTYPE_T itype) {
		return GlobalMembersGm_defs.gm_is_collection_iter_type(itype);
	}

	@Deprecated
	public static boolean gm_is_iteration_on_property(GMTYPE_T iterType) {
		return GlobalMembersGm_defs.gm_is_property_iter_type(iterType);
	}

	@Deprecated
	public static boolean gm_is_iteration_on_set(GMTYPE_T itype) {
		return (itype == GMTYPE_T.GMTYPE_NODEITER_SET) || (itype == GMTYPE_T.GMTYPE_EDGEITER_SET);
	}

	@Deprecated
	public static boolean gm_is_iteration_on_order(GMTYPE_T itype) {
		return (itype == GMTYPE_T.GMTYPE_NODEITER_ORDER) || (itype == GMTYPE_T.GMTYPE_EDGEITER_ORDER);
	}

	@Deprecated
	public static boolean gm_is_iteration_on_sequence(GMTYPE_T itype) {
		return (itype == GMTYPE_T.GMTYPE_NODEITER_SEQ) || (itype == GMTYPE_T.GMTYPE_EDGEITER_SEQ);
	}

	@Deprecated
	public static boolean gm_is_iteration_on_all_graph(GMTYPE_T itype) {
		return GlobalMembersGm_defs.gm_is_all_graph_iter_type(itype);
	}

	@Deprecated
	public static boolean gm_is_iteration_on_out_neighbors(GMTYPE_T itype) {
		return (itype == GMTYPE_T.GMTYPE_EDGEITER_NBRS) || (itype == GMTYPE_T.GMTYPE_NODEITER_NBRS);
	}

	@Deprecated
	public static boolean gm_is_iteration_on_in_neighbors(GMTYPE_T itype) {
		return (itype == GMTYPE_T.GMTYPE_EDGEITER_IN_NBRS) || (itype == GMTYPE_T.GMTYPE_NODEITER_IN_NBRS);
	}

	@Deprecated
	public static boolean gm_is_iteration_on_up_neighbors(GMTYPE_T itype) {
		return (itype == GMTYPE_T.GMTYPE_EDGEITER_UP_NBRS) || (itype == GMTYPE_T.GMTYPE_NODEITER_UP_NBRS);
	}

	@Deprecated
	public static boolean gm_is_iteration_on_down_neighbors(GMTYPE_T itype) {
		return (itype == GMTYPE_T.GMTYPE_EDGEITER_DOWN_NBRS) || (itype == GMTYPE_T.GMTYPE_NODEITER_DOWN_NBRS);
	}

	@Deprecated
	public static boolean gm_is_iteration_use_reverse(GMTYPE_T itype) {
		return GlobalMembersGm_defs.gm_is_iteration_on_in_neighbors(itype) || GlobalMembersGm_defs.gm_is_iteration_on_up_neighbors(itype);
	}

	@Deprecated
	public static boolean gm_is_iteration_bfs(GMTYPE_T itype) {
		return (itype == GMTYPE_T.GMTYPE_EDGEITER_BFS) || (itype == GMTYPE_T.GMTYPE_NODEITER_BFS);
	}

	@Deprecated
	public static boolean gm_is_iteration_on_nodes(GMTYPE_T itype) {
		return GlobalMembersGm_defs.gm_is_node_iter_type(itype);
	}

	@Deprecated
	public static boolean gm_is_iteration_on_edges(GMTYPE_T itype) {
		return GlobalMembersGm_defs.gm_is_edge_iter_type(itype);
	}

	@Deprecated
	public static boolean gm_is_iteration_on_updown_levels(GMTYPE_T itype) {
		return GlobalMembersGm_defs.gm_is_iteration_on_up_neighbors(itype) || GlobalMembersGm_defs.gm_is_iteration_on_down_neighbors(itype);
	}

	@Deprecated
	public static boolean gm_is_iteration_on_neighbors_compatible(GMTYPE_T itype) {
		return GlobalMembersGm_defs.gm_is_any_nbr_node_iter_type(itype);
	}

	@Deprecated
	public static boolean gm_is_collection_access_none(GM_ACCESS_T i) {
		return (i == GM_ACCESS_T.GMACCESS_NONE);
	}
}