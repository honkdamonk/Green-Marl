package opt;

import tangible.RefObject;
import inc.GMTYPE_T;
import inc.GM_OPS_T;
import inc.GM_REDUCE_T;
import inc.gm_assignment_t;
import ast.ast_assign;
import ast.ast_expr;
import ast.ast_expr_reduce;
import ast.ast_sent;
import ast.ast_sentblock;

import common.gm_add_symbol;
import common.gm_transform_helper;
import common.gm_method_id_t;

import frontend.gm_symtab_entry;

public class GlobalMembersGm_syntax_sugar2 {

	// ----------------------------------------------------
	// syntax sugar elimination (after type resolution)
	// Reduce op(e.g. Sum) --> initialization + foreach + reduce assign(e.g. +=)
	// ----------------------------------------------------

	// ====================================================================
	// static functions in this file
	public static gm_symtab_entry insert_def_and_init_before(String vname, GMTYPE_T prim_type, ast_sent curr, ast_expr default_val) {
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
		gm_symtab_entry e = gm_add_symbol.gm_add_new_symbol_primtype(sb, prim_type, new tangible.RefObject<String>(vname));

		// -------------------------------------------------------------
		// 3. add initialization sentence
		// -------------------------------------------------------------
		if (default_val != null) {
			// prinf("def_val = %p, new_id = %p\n", default_val, new_id);
			// assert(gm_is_compatible_type_for_assign(prim_type,
			// default_val->get_type_summary()));
			ast_assign init_a = ast_assign.new_assign_scala(e.getId().copy(true), default_val, gm_assignment_t.GMASSIGN_NORMAL);
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

	public static gm_method_id_t find_count_function(GMTYPE_T source_type, GMTYPE_T iter_type) {
		if (source_type.is_graph_type()) {
			if (iter_type.is_all_graph_node_iter_type()) {
				return gm_method_id_t.GM_BLTIN_GRAPH_NUM_NODES;
			} else if (iter_type.is_all_graph_node_iter_type()) {
				return gm_method_id_t.GM_BLTIN_GRAPH_NUM_EDGES;
			}
		} else if (source_type.is_node_compatible_type()) {
			if (iter_type == GMTYPE_T.GMTYPE_NODEITER_IN_NBRS) {
				return gm_method_id_t.GM_BLTIN_NODE_IN_DEGREE;
			} else if (iter_type == GMTYPE_T.GMTYPE_NODEITER_NBRS) {
				return gm_method_id_t.GM_BLTIN_NODE_DEGREE;
			}
		} else if (source_type.is_collection_type()) {
			if (iter_type.is_collection_iter_type())
				return gm_method_id_t.GM_BLTIN_SET_SIZE;
		}

		return gm_method_id_t.GM_BLTIN_END;
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

	public static boolean check_is_reduce_op(GM_REDUCE_T rtype, GM_OPS_T op) {
		if ((rtype == GM_REDUCE_T.GMREDUCE_PLUS) && (op == GM_OPS_T.GMOP_ADD))
			return true;
		else if ((rtype == GM_REDUCE_T.GMREDUCE_MULT) && (op == GM_OPS_T.GMOP_MULT))
			return true;
		else if ((rtype == GM_REDUCE_T.GMREDUCE_MIN) && (op == GM_OPS_T.GMOP_MIN))
			return true;
		else if ((rtype == GM_REDUCE_T.GMREDUCE_MAX) && (op == GM_OPS_T.GMOP_MAX))
			return true;
		else if ((rtype == GM_REDUCE_T.GMREDUCE_OR) && (op == GM_OPS_T.GMOP_OR))
			return true;
		else if ((rtype == GM_REDUCE_T.GMREDUCE_AND) && (op == GM_OPS_T.GMOP_AND))
			return true;
		return false;
	}

	public static boolean check_has_nested(ast_expr body, GM_REDUCE_T rtype, RefObject<Boolean> has_other_rhs, RefObject<ast_expr_reduce> b1_ref,
			RefObject<ast_expr_reduce> b2_ref) {
		ast_expr_reduce b1 = null;
		ast_expr_reduce b2 = null;
		try {
			if (rtype == GM_REDUCE_T.GMREDUCE_AVG)
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

			// C++ TO JAVA CONVERTER NOTE: The following #define macro was
			// replaced
			// in-line:
			// /#define CHECK_SAME_REDUCTION(expr, rtype)
			// ((expr)->is_reduction() &&
			// (((ast_expr_reduce*)(expr))->get_reduce_type() == rtype))
			if (((body).is_reduction() && (((ast_expr_reduce) (body)).get_reduce_type() == rtype))) {
				b1 = (ast_expr_reduce) body;
				return true;
			} else if (body.is_biop()) {
				GM_OPS_T op = body.get_optype();
				if (GlobalMembersGm_syntax_sugar2.check_is_reduce_op(rtype, op)) {
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