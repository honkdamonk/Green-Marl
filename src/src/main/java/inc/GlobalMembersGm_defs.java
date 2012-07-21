package inc;

import frontend.GlobalMembersGm_typecheck_oprules;

public class GlobalMembersGm_defs
{

	public static boolean gm_is_foreign_expr_type(int i)
	{
		return (i == GMTYPE_T.GMTYPE_FOREIGN_EXPR.getValue());
	}
	public static boolean gm_is_integer_type(int i)
	{
		return (i == GMTYPE_T.GMTYPE_INT.getValue()) || (i == GMTYPE_T.GMTYPE_LONG.getValue()) || (i == GMTYPE_T.GMTYPE_BYTE.getValue()) || (i == GMTYPE_T.GMTYPE_SHORT.getValue());
	}
	public static boolean gm_is_float_type(int i)
	{
		return (i == GMTYPE_T.GMTYPE_FLOAT.getValue()) || (i == GMTYPE_T.GMTYPE_DOUBLE.getValue());
	}
	public static boolean gm_is_unknown_type(int i)
	{
		return (i == GMTYPE_T.GMTYPE_UNKNOWN.getValue()) || (i == GMTYPE_T.GMTYPE_UNKNOWN_NUMERIC.getValue());
	}
	public static boolean gm_is_numeric_type(int i)
	{
		return GlobalMembersGm_defs.gm_is_integer_type(i) || GlobalMembersGm_defs.gm_is_float_type(i);
	}
	public static boolean gm_is_nodeedge_type(int i)
	{
		return (i == GMTYPE_T.GMTYPE_NODE.getValue()) || (i == GMTYPE_T.GMTYPE_EDGE.getValue());
	}
	public static boolean gm_is_node_type(int i)
	{
		return (i == GMTYPE_T.GMTYPE_NODE.getValue());
	}
	public static boolean gm_is_edge_type(int i)
	{
		return (i == GMTYPE_T.GMTYPE_EDGE.getValue());
	}
	public static boolean gm_is_int_type(int i)
	{
		return (i == GMTYPE_T.GMTYPE_INT.getValue());
	}
	public static boolean gm_is_long_type(int i)
	{
		return (i == GMTYPE_T.GMTYPE_LONG.getValue());
	}
	public static boolean gm_is_nil_type(int i)
	{
		return (i == GMTYPE_T.GMTYPE_NIL_UNKNOWN.getValue()) || (i == GMTYPE_T.GMTYPE_NIL_NODE.getValue()) || (i == GMTYPE_T.GMTYPE_NIL_EDGE.getValue());
	}

	public static boolean gm_is_all_graph_node_iter_type(int i)
	{
		return (i == GMTYPE_T.GMTYPE_NODEITER_ALL.getValue()) || (i == GMTYPE_T.GMTYPE_NODEITER_BFS.getValue()); // [XXX] to be finxed
	}

	public static boolean gm_is_all_graph_edge_iter_type(int i)
	{
		return (i == GMTYPE_T.GMTYPE_EDGEITER_ALL.getValue()) || (i == GMTYPE_T.GMTYPE_EDGEITER_BFS.getValue());
	}

	public static boolean gm_is_all_graph_iter_type(int i)
	{
		return GlobalMembersGm_defs.gm_is_all_graph_node_iter_type(i) || GlobalMembersGm_defs.gm_is_all_graph_edge_iter_type(i);
	}

	public static boolean gm_is_inout_nbr_node_iter_type(int i)
	{
		return (i == GMTYPE_T.GMTYPE_NODEITER_NBRS.getValue()) || (i == GMTYPE_T.GMTYPE_NODEITER_IN_NBRS.getValue());
	}

	public static boolean gm_is_any_nbr_node_iter_type(int i)
	{
		return (i == GMTYPE_T.GMTYPE_NODEITER_NBRS.getValue()) || (i == GMTYPE_T.GMTYPE_NODEITER_IN_NBRS.getValue()) || (i == GMTYPE_T.GMTYPE_NODEITER_UP_NBRS.getValue()) || (i == GMTYPE_T.GMTYPE_NODEITER_DOWN_NBRS.getValue()) || (i == GMTYPE_T.GMTYPE_NODEITER_COMMON_NBRS.getValue());
	}

	public static boolean gm_is_any_nbr_edge_iter_type(int i)
	{
		return (i == GMTYPE_T.GMTYPE_EDGEITER_NBRS.getValue()) || (i == GMTYPE_T.GMTYPE_EDGEITER_IN_NBRS.getValue()) || (i == GMTYPE_T.GMTYPE_EDGEITER_UP_NBRS.getValue()) || (i == GMTYPE_T.GMTYPE_EDGEITER_DOWN_NBRS.getValue());
	}

	public static boolean gm_is_any_nbr_iter_type(int i)
	{
		return GlobalMembersGm_defs.gm_is_any_nbr_edge_iter_type(i) || GlobalMembersGm_defs.gm_is_any_nbr_node_iter_type(i);
	}

	public static boolean gm_is_common_nbr_iter_type(int i)
	{
		return (i == GMTYPE_T.GMTYPE_NODEITER_COMMON_NBRS.getValue());
	}

	public static boolean gm_is_node_iter_type(int i)
	{
		return GlobalMembersGm_defs.gm_is_all_graph_node_iter_type(i) || GlobalMembersGm_defs.gm_is_any_nbr_node_iter_type(i);
	}

	public static boolean gm_is_edge_iter_type(int i)
	{
		return GlobalMembersGm_defs.gm_is_all_graph_edge_iter_type(i) || GlobalMembersGm_defs.gm_is_any_nbr_edge_iter_type(i);
	}

	public static boolean gm_is_node_collection_iter_type(int i)
	{
		return ((i == GMTYPE_T.GMTYPE_NODEITER_SET.getValue()) || (i == GMTYPE_T.GMTYPE_NODEITER_SEQ.getValue()) || (i == GMTYPE_T.GMTYPE_NODEITER_ORDER.getValue()));
	}

	public static boolean gm_is_edge_collection_iter_type(int i)
	{
		return ((i == GMTYPE_T.GMTYPE_EDGEITER_SET.getValue()) || (i == GMTYPE_T.GMTYPE_EDGEITER_SEQ.getValue()) || (i == GMTYPE_T.GMTYPE_EDGEITER_ORDER.getValue()));
	}

	public static boolean gm_is_unknown_collection_iter_type(int i)
	{
		return (i == GMTYPE_T.GMTYPE_ITER_ANY.getValue());
	}

	public static boolean gm_is_collection_of_set_iter_type(int i)
	{
		return i == GMTYPE_T.GMTYPE_COLLECTIONITER_SET.getValue();
	}

	public static boolean gm_is_collection_of_seq_iter_type(int i)
	{
		return i == GMTYPE_T.GMTYPE_COLLECTIONITER_SEQ.getValue();
	}

	public static boolean gm_is_collection_of_order_iter_type(int i)
	{
		return i == GMTYPE_T.GMTYPE_COLLECTIONITER_ORDER.getValue();
	}

	public static boolean gm_is_collection_of_collection_iter_type(int i)
	{
		return GlobalMembersGm_defs.gm_is_collection_of_set_iter_type(i) || GlobalMembersGm_defs.gm_is_collection_of_order_iter_type(i) || GlobalMembersGm_defs.gm_is_collection_of_seq_iter_type(i);
	}

	public static boolean gm_is_collection_iter_type(int i)
	{
		return GlobalMembersGm_defs.gm_is_node_collection_iter_type(i) || GlobalMembersGm_defs.gm_is_edge_collection_iter_type(i) || GlobalMembersGm_defs.gm_is_unknown_collection_iter_type(i) || GlobalMembersGm_defs.gm_is_collection_of_collection_iter_type(i);
	}

	public static boolean gm_is_property_iter_set_type(int i)
	{
		return i == GMTYPE_T.GMTYPE_PROPERTYITER_SET.getValue();
	}

	public static boolean gm_is_property_iter_seq_type(int i)
	{
		return i == GMTYPE_T.GMTYPE_PROPERTYITER_SEQ.getValue();
	}

	public static boolean gm_is_property_iter_order_type(int i)
	{
		return i == GMTYPE_T.GMTYPE_PROPERTYITER_ORDER.getValue();
	}

	public static boolean gm_is_property_iter_type(int i)
	{
		return GlobalMembersGm_defs.gm_is_property_iter_order_type(i) || GlobalMembersGm_defs.gm_is_property_iter_seq_type(i) || GlobalMembersGm_defs.gm_is_property_iter_set_type(i);
	}

	public static boolean gm_is_node_compatible_type(int i)
	{
		return GlobalMembersGm_defs.gm_is_node_type(i) || GlobalMembersGm_defs.gm_is_node_iter_type(i) || GlobalMembersGm_defs.gm_is_node_collection_iter_type(i) || GlobalMembersGm_defs.gm_is_nil_type(i);
	}
	public static boolean gm_is_edge_compatible_type(int i)
	{
		return GlobalMembersGm_defs.gm_is_edge_type(i) || GlobalMembersGm_defs.gm_is_edge_iter_type(i) || GlobalMembersGm_defs.gm_is_edge_collection_iter_type(i) || GlobalMembersGm_defs.gm_is_nil_type(i);
	}
	public static boolean gm_is_node_edge_compatible_type(int i)
	{
		return GlobalMembersGm_defs.gm_is_node_compatible_type(i) || GlobalMembersGm_defs.gm_is_edge_compatible_type(i);
	}
	public static boolean gm_is_iter_type(int i)
	{
		return GlobalMembersGm_defs.gm_is_node_iter_type(i) || GlobalMembersGm_defs.gm_is_edge_iter_type(i) || GlobalMembersGm_defs.gm_is_collection_iter_type(i);
	}
	public static boolean gm_is_boolean_type(int i)
	{
		return (i == GMTYPE_T.GMTYPE_BOOL.getValue());
	}
	public static boolean gm_is_unknonwn_type(int i)
	{
		return (i == GMTYPE_T.GMTYPE_UNKNOWN.getValue()) || (i == GMTYPE_T.GMTYPE_UNKNOWN_NUMERIC.getValue());
	}
	public static boolean gm_is_void_type(int i)
	{
		return (i == GMTYPE_T.GMTYPE_VOID.getValue());
	}
	public static boolean gm_is_prim_type(int i)
	{
		return GlobalMembersGm_defs.gm_is_numeric_type(i) || GlobalMembersGm_defs.gm_is_boolean_type(i);
	}
	public static boolean gm_is_graph_type(int i)
	{
		return (i == GMTYPE_T.GMTYPE_GRAPH.getValue());
	}
	public static boolean gm_is_node_property_type(int i)
	{
		return (i == GMTYPE_T.GMTYPE_NODEPROP.getValue());
	}
	public static boolean gm_is_edge_property_type(int i)
	{
		return (i == GMTYPE_T.GMTYPE_EDGEPROP.getValue());
	}
	public static boolean gm_is_property_type(int i)
	{
		return GlobalMembersGm_defs.gm_is_node_property_type(i) || GlobalMembersGm_defs.gm_is_edge_property_type(i);
	}

	public static boolean gm_is_inf_type(int i)
	{
		return (i == GMTYPE_T.GMTYPE_INF.getValue()) || (i == GMTYPE_T.GMTYPE_INF_INT.getValue()) || (i == GMTYPE_T.GMTYPE_INF_LONG.getValue()) || (i == GMTYPE_T.GMTYPE_INF_FLOAT.getValue()) || (i == GMTYPE_T.GMTYPE_INF_FLOAT.getValue());
	}
	public static boolean gm_is_inf_type_unsized(int i)
	{
		return (i == GMTYPE_T.GMTYPE_INF.getValue());
	}
	public static boolean gm_is_inf_type_sized(int i)
	{
		return GlobalMembersGm_defs.gm_is_inf_type(i) && !GlobalMembersGm_defs.gm_is_inf_type_unsized(i);
	}
	public static int gm_get_sized_inf_type(int i)
	{
		if (i == GMTYPE_T.GMTYPE_INT.getValue())
			return GMTYPE_T.GMTYPE_INF_INT;
		else if (i == GMTYPE_T.GMTYPE_LONG.getValue())
			return GMTYPE_T.GMTYPE_INF_LONG;
		else if (i == GMTYPE_T.GMTYPE_FLOAT.getValue())
			return GMTYPE_T.GMTYPE_INF_FLOAT;
		else if (i == GMTYPE_T.GMTYPE_DOUBLE.getValue())
			return GMTYPE_T.GMTYPE_INF_DOUBLE;
		else
		{
			assert false;
			return 0;
		}
	}

	public static boolean gm_is_node_set_type(int i)
	{
		return (i == GMTYPE_T.GMTYPE_NSET.getValue());
	}
	public static boolean gm_is_node_order_type(int i)
	{
		return (i == GMTYPE_T.GMTYPE_NORDER.getValue());
	}
	public static boolean gm_is_node_sequence_type(int i)
	{
		return (i == GMTYPE_T.GMTYPE_NSEQ.getValue());
	}
	public static boolean gm_is_edge_set_type(int i)
	{
		return (i == GMTYPE_T.GMTYPE_ESET.getValue());
	}
	public static boolean gm_is_edge_order_type(int i)
	{
		return (i == GMTYPE_T.GMTYPE_EORDER.getValue());
	}
	public static boolean gm_is_edge_sequence_type(int i)
	{
		return (i == GMTYPE_T.GMTYPE_ESEQ.getValue());
	}
	public static boolean gm_is_node_collection_type(int i)
	{
		return GlobalMembersGm_defs.gm_is_node_set_type(i) || GlobalMembersGm_defs.gm_is_node_order_type(i) || GlobalMembersGm_defs.gm_is_node_sequence_type(i);
	}

	public static boolean gm_is_edge_collection_type(int i)
	{
		return GlobalMembersGm_defs.gm_is_edge_set_type(i) || GlobalMembersGm_defs.gm_is_edge_order_type(i) || GlobalMembersGm_defs.gm_is_edge_sequence_type(i);
	}

	public static boolean gm_is_set_collection_type(int i)
	{
		return GlobalMembersGm_defs.gm_is_node_set_type(i) || GlobalMembersGm_defs.gm_is_edge_set_type(i);
	}

	public static boolean gm_is_order_collection_type(int i)
	{
		return GlobalMembersGm_defs.gm_is_node_order_type(i) || GlobalMembersGm_defs.gm_is_edge_order_type(i);
	}

	public static boolean gm_is_sequence_collection_type(int i)
	{
		return GlobalMembersGm_defs.gm_is_node_sequence_type(i) || GlobalMembersGm_defs.gm_is_edge_sequence_type(i);
	}

	public static boolean gm_is_collection_of_collection_type(int type)
	{
		return type == GMTYPE_T.GMTYPE_COLLECTION.getValue();
	}

	public static boolean gm_is_collection_type(int i)
	{
		return GlobalMembersGm_defs.gm_is_node_collection_type(i) || GlobalMembersGm_defs.gm_is_edge_collection_type(i) || GlobalMembersGm_defs.gm_is_collection_of_collection_type(i);
	}

	public static boolean gm_is_sequential_collection_type(int i)
	{
		return GlobalMembersGm_defs.gm_is_sequence_collection_type(i) || GlobalMembersGm_defs.gm_is_order_collection_type(i);
	}

	// node set -> nodeset iter
	// edge set -> edgeset iter ...
	public static int gm_get_natural_collection_iterator(int src_type)
	{
		if (src_type == GMTYPE_T.GMTYPE_NSET.getValue())
			return GMTYPE_T.GMTYPE_NODEITER_SET;
		else if (src_type == GMTYPE_T.GMTYPE_NSEQ.getValue())
			return GMTYPE_T.GMTYPE_NODEITER_SEQ;
		else if (src_type == GMTYPE_T.GMTYPE_NORDER.getValue())
			return GMTYPE_T.GMTYPE_NODEITER_ORDER;
		else if (src_type == GMTYPE_T.GMTYPE_ESET.getValue())
			return GMTYPE_T.GMTYPE_NODEITER_SET;
		else if (src_type == GMTYPE_T.GMTYPE_NSEQ.getValue())
			return GMTYPE_T.GMTYPE_NODEITER_SEQ;
		else if (src_type == GMTYPE_T.GMTYPE_EORDER.getValue())
			return GMTYPE_T.GMTYPE_NODEITER_ORDER;
		else if (src_type == GMTYPE_T.GMTYPE_COLLECTION.getValue())
			return GMTYPE_T.GMTYPE_ITER_UNDERSPECIFIED; //handle that later
		else
		{
			assert false;
			return GMTYPE_T.GMTYPE_INVALID;
		}
	}

	public static int gm_get_specified_collection_iterator(int type)
	{
		switch (type)
		{
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
	public static boolean gm_has_target_graph_type(int t)
	{
		return GlobalMembersGm_defs.gm_is_node_edge_compatible_type(t) || GlobalMembersGm_defs.gm_is_collection_type(t) || GlobalMembersGm_defs.gm_is_collection_of_collection_type(t); // any node-edge iterator (including collection iterator)
	}

	public static boolean gm_is_same_type(int i1, int i2)
	{
		return (i1 == i2);
	}

	public static boolean gm_is_same_node_or_edge_compatible_type(int i1, int i2)
	{
		return (GlobalMembersGm_defs.gm_is_node_compatible_type(i1) && GlobalMembersGm_defs.gm_is_node_compatible_type(i2)) || (GlobalMembersGm_defs.gm_is_edge_compatible_type(i1) && GlobalMembersGm_defs.gm_is_edge_compatible_type(i2));
	}

	public static boolean gm_collection_of_collection_compatible_type(int def_src, int source_type)
	{
		return GlobalMembersGm_defs.gm_is_order_collection_type(def_src) && GlobalMembersGm_defs.gm_is_collection_of_collection_type(source_type);
	}

	// see http://cppreference.com/wiki/language/operator_precedence
	public static int[] GM_OPPRED_LEVEL = {2, 3, 5, 5, 5, 2, 2, 6, 6, 13, 13, 3, 9, 9, 8, 8, 8, 8, 2, 15, 99}; // ABS (not in cpp)

	public static boolean gm_is_numeric_op(int i)
	{
		return (i == GM_OPS_T.GMOP_MULT.getValue()) || (i == GM_OPS_T.GMOP_DIV.getValue()) || (i == GM_OPS_T.GMOP_MOD.getValue()) || (i == GM_OPS_T.GMOP_ADD.getValue()) || (i == GM_OPS_T.GMOP_SUB.getValue()) || (i == GM_OPS_T.GMOP_NEG.getValue()) || (i == GM_OPS_T.GMOP_ABS.getValue()) || (i == GM_OPS_T.GMOP_MAX.getValue()) || (i == GM_OPS_T.GMOP_MIN.getValue());
	}
	public static boolean gm_is_boolean_op(int i)
	{
		return (i == GM_OPS_T.GMOP_NOT.getValue()) || (i == GM_OPS_T.GMOP_AND.getValue()) || (i == GM_OPS_T.GMOP_OR.getValue());
	}
	public static boolean gm_is_eq_op(int i)
	{
		return (i == GM_OPS_T.GMOP_EQ.getValue()) || (i == GM_OPS_T.GMOP_NEQ.getValue());
	}
	public static boolean gm_is_less_op(int i)
	{
		return (i == GM_OPS_T.GMOP_GT.getValue()) || (i == GM_OPS_T.GMOP_LT.getValue()) || (i == GM_OPS_T.GMOP_GE.getValue()) || (i == GM_OPS_T.GMOP_LE.getValue());
	}
	public static boolean gm_is_eq_or_less_op(int i)
	{
		return GlobalMembersGm_defs.gm_is_eq_op(i) || GlobalMembersGm_defs.gm_is_less_op(i);
	}
	public static boolean gm_is_ternary_op(int i)
	{
		return (i == GM_OPS_T.GMOP_TER.getValue());
	}

	// checking to apply op (including assignment) between two types.
	// (not including target-graph checking)
	//boolean gm_is_compatible_type(int op, int t1, int t2, tangible.RefObject<int> op_result_type, tangible.RefObject<int> t1_coerced, tangible.RefObject<int> t2_coerced, tangible.RefObject<boolean> t1_coerced_lost_precision, tangible.RefObject<boolean> t2_coerced_lost_precision);

	public static boolean gm_is_compatible_type_for_assign(int t_lhs, int t_rhs, tangible.RefObject<Integer> t_new_rhs, tangible.RefObject<Boolean> warning)
	{
		int dummy1;
		int dummy2;
		boolean dummy_b;
	tangible.RefObject<Integer> tempRef_dummy1 = new tangible.RefObject<Integer>(dummy1);
	tangible.RefObject<Integer> tempRef_dummy2 = new tangible.RefObject<Integer>(dummy2);
	tangible.RefObject<Boolean> tempRef_dummy_b = new tangible.RefObject<Boolean>(dummy_b);
		return GlobalMembersGm_typecheck_oprules.gm_is_compatible_type(GM_OPS_T.GMOP_ASSIGN, t_lhs, t_rhs, tempRef_dummy1, tempRef_dummy2, t_new_rhs, tempRef_dummy_b, warning);
		dummy1 = tempRef_dummy1.argvalue;
		dummy2 = tempRef_dummy2.argvalue;
		dummy_b = tempRef_dummy_b.argvalue;
	}

	public static boolean gm_is_strict_reduce_op(int t)
	{
		return (t == GM_REDUCE_T.GMREDUCE_PLUS.getValue()) || (t == GM_REDUCE_T.GMREDUCE_MULT.getValue()) || (t == GM_REDUCE_T.GMREDUCE_MIN.getValue()) || (t == GM_REDUCE_T.GMREDUCE_MAX.getValue()) || (t == GM_REDUCE_T.GMREDUCE_AND.getValue()) || (t == GM_REDUCE_T.GMREDUCE_OR.getValue()) || (t == GM_REDUCE_T.GMREDUCE_AVG.getValue());
	}
	public static boolean gm_is_numeric_reduce_op(int t)
	{
		return (t == GM_REDUCE_T.GMREDUCE_PLUS.getValue()) || (t == GM_REDUCE_T.GMREDUCE_MULT.getValue()) || (t == GM_REDUCE_T.GMREDUCE_MIN.getValue()) || (t == GM_REDUCE_T.GMREDUCE_MAX.getValue()) || (t == GM_REDUCE_T.GMREDUCE_AVG.getValue());
	}
	public static boolean gm_is_boolean_reduce_op(int t)
	{
		return (t == GM_REDUCE_T.GMREDUCE_AND.getValue()) || (t == GM_REDUCE_T.GMREDUCE_OR.getValue());
	}

	// todo: clarify following macros
	public static boolean gm_is_iteration_on_collection(int itype)
	{
		return GlobalMembersGm_defs.gm_is_collection_iter_type(itype);
	}

	public static boolean gm_is_iteration_on_property(int iterType)
	{
		return GlobalMembersGm_defs.gm_is_property_iter_type(iterType);
	}

	public static boolean gm_is_iteration_on_set(int itype)
	{
		return (itype == GMTYPE_T.GMTYPE_NODEITER_SET.getValue()) || (itype == GMTYPE_T.GMTYPE_EDGEITER_SET.getValue());
	}
	public static boolean gm_is_iteration_on_order(int itype)
	{
		return (itype == GMTYPE_T.GMTYPE_NODEITER_ORDER.getValue()) || (itype == GMTYPE_T.GMTYPE_EDGEITER_ORDER.getValue());
	}
	public static boolean gm_is_iteration_on_sequence(int itype)
	{
		return (itype == GMTYPE_T.GMTYPE_NODEITER_SEQ.getValue()) || (itype == GMTYPE_T.GMTYPE_EDGEITER_SEQ.getValue());
	}
	public static boolean gm_is_iteration_on_all_graph(int itype)
	{
		return GlobalMembersGm_defs.gm_is_all_graph_iter_type(itype);
	}
	public static boolean gm_is_iteration_on_out_neighbors(int itype)
	{
		return (itype == GMTYPE_T.GMTYPE_EDGEITER_NBRS.getValue()) || (itype == GMTYPE_T.GMTYPE_NODEITER_NBRS.getValue());
	}
	public static boolean gm_is_iteration_on_in_neighbors(int itype)
	{
		return (itype == GMTYPE_T.GMTYPE_EDGEITER_IN_NBRS.getValue()) || (itype == GMTYPE_T.GMTYPE_NODEITER_IN_NBRS.getValue());
	}
	public static boolean gm_is_iteration_on_up_neighbors(int itype)
	{
		return (itype == GMTYPE_T.GMTYPE_EDGEITER_UP_NBRS.getValue()) || (itype == GMTYPE_T.GMTYPE_NODEITER_UP_NBRS.getValue());
	}
	public static boolean gm_is_iteration_on_down_neighbors(int itype)
	{
		return (itype == GMTYPE_T.GMTYPE_EDGEITER_DOWN_NBRS.getValue()) || (itype == GMTYPE_T.GMTYPE_NODEITER_DOWN_NBRS.getValue());
	}
	public static boolean gm_is_iteration_use_reverse(int itype)
	{
		return GlobalMembersGm_defs.gm_is_iteration_on_in_neighbors(itype) || GlobalMembersGm_defs.gm_is_iteration_on_up_neighbors(itype);
	}
	public static boolean gm_is_iteration_bfs(int itype)
	{
		return (itype == GMTYPE_T.GMTYPE_EDGEITER_BFS.getValue()) || (itype == GMTYPE_T.GMTYPE_NODEITER_BFS.getValue());
	}
	public static boolean gm_is_iteration_on_nodes(int itype)
	{
		return GlobalMembersGm_defs.gm_is_node_iter_type(itype);
	}
	public static boolean gm_is_iteration_on_edges(int itype)
	{
		return GlobalMembersGm_defs.gm_is_edge_iter_type(itype);
	}
	public static boolean gm_is_iteration_on_updown_levels(int itype)
	{
		return GlobalMembersGm_defs.gm_is_iteration_on_up_neighbors(itype) || GlobalMembersGm_defs.gm_is_iteration_on_down_neighbors(itype);
	}
	public static boolean gm_is_iteration_on_neighbors_compatible(int itype)
	{
		return GlobalMembersGm_defs.gm_is_any_nbr_node_iter_type(itype);
	}

	public static boolean gm_is_collection_access_none(int i)
	{
		return (i == GM_ACCESS_T.GMACCESS_NONE.getValue());
	}
}