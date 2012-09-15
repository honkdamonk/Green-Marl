package opt;

import inc.gm_type;
import inc.gm_ops;
import inc.gm_reduce;
import inc.gm_assignment;
import tangible.RefObject;
import ast.ast_assign;
import ast.ast_expr;
import ast.ast_expr_reduce;
import ast.ast_sent;
import ast.ast_sentblock;

import common.gm_add_symbol;
import common.gm_method_id;
import common.gm_transform_helper;

import frontend.gm_symtab_entry;

public class gm_syntax_sugar2 {

	// ----------------------------------------------------
	// syntax sugar elimination (after type resolution)
	// Reduce op(e.g. Sum) --> initialization + foreach + reduce assign(e.g. +=)
	// ----------------------------------------------------

	// ====================================================================
	// static functions in this file
	public static gm_symtab_entry insert_def_and_init_before(String vname, gm_type prim_type, ast_sent curr, ast_expr default_val) {
		// -------------------------------------------------------------
		// assumption:
		// A. vname does not conflict upward or downward
		// B. default_val has well ripped-off. (i.e. top scope is null)
		// C. default_val has correct type_summary
		// -------------------------------------------------------------
		assert prim_type.is_prim_type();

		// -------------------------------------------------------------
		// 1. find enclosing sentence block
		// -------------------------------------------------------------
		gm_transform_helper.gm_make_it_belong_to_sentblock(curr);
		ast_sentblock sb = (ast_sentblock) curr.get_parent();

		// -------------------------------------------------------------
		// 2. Add new symbol to the current bound
		// -------------------------------------------------------------
		gm_symtab_entry e = gm_add_symbol.gm_add_new_symbol_primtype(sb, prim_type, vname);

		// -------------------------------------------------------------
		// 3. add initialization sentence
		// -------------------------------------------------------------
		if (default_val != null) {
			// prinf("def_val = %p, new_id = %p\n", default_val, new_id);
			// assert(gm_is_compatible_type_for_assign(prim_type,
			// default_val->get_type_summary()));
			ast_assign init_a = ast_assign.new_assign_scala(e.getId().copy(true), default_val, gm_assignment.GMASSIGN_NORMAL);
			gm_transform_helper.gm_add_sent_before(curr, init_a);
		}

		return e;
	}

	public static void replace_avg_to_varaible(ast_sent s, ast_expr rhs, gm_symtab_entry e) {
		replace_avg_to_varaible_t T = new replace_avg_to_varaible_t(rhs, e);
		gm_transform_helper.gm_replace_expr_general(s, T);
	}

	// static void mark_

	public static String OPT_FLAG_NESTED_REDUCTION = "OPT_FLAG_NESTED_REDUCTION";
	public static String OPT_SYM_NESTED_REDUCTION_TARGET = "OPT_SYM_NESTED_REDUCTION_TARGET";
	public static String OPT_SYM_NESTED_REDUCTION_BOUND = "OPT_SYM_NESTED_REDUCTION_BOUND";
	public static String OPT_SB_NESTED_REDUCTION_SCOPE = "OPT_SB_NESTED_REDUCTION_SCOPE";

	public static gm_method_id find_count_function(gm_type source_type, gm_type iter_type) {
		if (source_type.is_graph_type()) {
			if (iter_type.is_all_graph_node_iter_type()) {
				return gm_method_id.GM_BLTIN_GRAPH_NUM_NODES;
			} else if (iter_type.is_all_graph_node_iter_type()) {
				return gm_method_id.GM_BLTIN_GRAPH_NUM_EDGES;
			}
		} else if (source_type.is_node_compatible_type()) {
			if (iter_type == gm_type.GMTYPE_NODEITER_IN_NBRS) {
				return gm_method_id.GM_BLTIN_NODE_IN_DEGREE;
			} else if (iter_type == gm_type.GMTYPE_NODEITER_NBRS) {
				return gm_method_id.GM_BLTIN_NODE_DEGREE;
			}
		} else if (source_type.is_collection_type()) {
			if (iter_type.is_collection_iter_type())
				return gm_method_id.GM_BLTIN_SET_SIZE;
		}

		return gm_method_id.GM_BLTIN_END;
	}

	// ------------------------------------------------------------------------------
	// Optimization for nested reduction
	// _S = Sum(x) { Sum(y) { Sum(z){ Sum(w){a}
	// * b
	// }
	// }
	// +c
	// }
	// X : has_nested: yes (because rhs = Sum(Y) + c)
	// is_nested: no
	// has_other_rhs: yes (becuase of + c)
	//
	// Y : has_nested: yes (becuase rhs = Sum(z))
	// is_nested: yes (because inside Sum (x) )
	// has_other_rhs: no (becuase rhs = Sum(z) only)
	//
	// Z : has_nested: no (because rhs = Sum(a)*b)
	// is_nested: yes (because inside Sum(y)
	// has_other_rhs: N/A
	//
	// W : has_nested: no
	// is_nested: no
	// has_other_rhs: N/A
	//
	// ==> result
	//
	// _S0 = 0;
	// Foreach(x) {
	// Foreach(y) {
	// Foreach(z) {
	// _S1 = 0;
	// Foreach(w) {
	// _S1 += a;
	// }
	// _S0 += _S1*b; @x//
	// }
	// }
	// _S0 += c; @x
	// }

	public static boolean check_is_reduce_op(gm_reduce rtype, gm_ops op) {
		if ((rtype == gm_reduce.GMREDUCE_PLUS) && (op == gm_ops.GMOP_ADD))
			return true;
		else if ((rtype == gm_reduce.GMREDUCE_MULT) && (op == gm_ops.GMOP_MULT))
			return true;
		else if ((rtype == gm_reduce.GMREDUCE_MIN) && (op == gm_ops.GMOP_MIN))
			return true;
		else if ((rtype == gm_reduce.GMREDUCE_MAX) && (op == gm_ops.GMOP_MAX))
			return true;
		else if ((rtype == gm_reduce.GMREDUCE_OR) && (op == gm_ops.GMOP_OR))
			return true;
		else if ((rtype == gm_reduce.GMREDUCE_AND) && (op == gm_ops.GMOP_AND))
			return true;
		return false;
	}

	public static boolean check_has_nested(ast_expr body, gm_reduce rtype, RefObject<Boolean> has_other_rhs, RefObject<ast_expr_reduce> b1_ref,
			RefObject<ast_expr_reduce> b2_ref) {
		ast_expr_reduce b1 = null;
		ast_expr_reduce b2 = null;
		try {
			if (rtype == gm_reduce.GMREDUCE_AVG)
				return false;

			// ---------------------------------
			// case 1
			// SUM ( SUM)
			// case 2
			// SUM ( SUM + SUM)
			// case 3
			// SUM ( SUM + alpha)
			// ---------------------------------
			has_other_rhs.argvalue = false;

			if (((body).is_reduction() && (((ast_expr_reduce) (body)).get_reduce_type() == rtype))) {
				b1 = (ast_expr_reduce) body;
				return true;
			} else if (body.is_biop()) {
				gm_ops op = body.get_optype();
				if (gm_syntax_sugar2.check_is_reduce_op(rtype, op)) {
					// check each argument
					if (((body.get_left_op()).is_reduction() && (((ast_expr_reduce) (body.get_left_op())).get_reduce_type() == rtype))) {
						b1 = (ast_expr_reduce) body.get_left_op();
					}
					if (((body.get_right_op()).is_reduction() && (((ast_expr_reduce) (body.get_right_op())).get_reduce_type() == rtype))) {
						b2 = (ast_expr_reduce) body.get_right_op();
					}

					boolean is_nested = (b1 != null) || (b2 != null);
					has_other_rhs.argvalue = is_nested && ((b1 == null) || (b2 == null));

					return is_nested;
				}

				return false;
			}

			return false;
		} finally {
			b1_ref.argvalue = b1;
			b2_ref.argvalue = b2;
		}
	}
}