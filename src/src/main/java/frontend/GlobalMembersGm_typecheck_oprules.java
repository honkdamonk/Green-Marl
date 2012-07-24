package frontend;

import inc.GMTYPE_T;

import inc.GM_OPS_T;
import inc.GlobalMembersGm_defs;
import tangible.RefObject;

import static frontend.gm_operator_result_t.RESULT_LEFT;
import static frontend.gm_operator_coercion_t.COERCION_RIGHT;
import static frontend.gm_operator_coercion_t.COERCION_ALL;
import static frontend.gm_operator_coercion_t.COERCION_NO;

public class GlobalMembersGm_typecheck_oprules {

	public static java.util.ArrayList<gm_type_rule> GM_TYPE_RULES = new java.util.ArrayList<gm_type_rule>();

	// C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced
	// in-line:
	// /#define NEW_RULE(O, T1,T2, TR, C) { gm_type_rule R(O, T1, T2, TR, C);
	// GM_TYPE_RULES.push_back(R); }

	public static void init_op_rules() {
		{
			gm_type_rule R = new gm_type_rule(gm_operator_t.INT_OP, gm_operator_type_class_t.T_INT, gm_operator_type_class_t.T_INT,
					gm_operator_result_t.RESULT_COERCION, COERCION_ALL);
			GM_TYPE_RULES.add(R);
		}
		;
		{
			gm_type_rule R = new gm_type_rule(gm_operator_t.NUMERIC_OP, gm_operator_type_class_t.T_NUMERIC, gm_operator_type_class_t.T_NUMERIC,
					gm_operator_result_t.RESULT_COERCION, COERCION_ALL);
			GM_TYPE_RULES.add(R);
		}
		;
		{
			gm_type_rule R = new gm_type_rule(gm_operator_t.BOOL_OP, gm_operator_type_class_t.T_BOOL, gm_operator_type_class_t.T_BOOL,
					gm_operator_result_t.RESULT_BOOL, COERCION_NO);
			GM_TYPE_RULES.add(R);
		}
		;
		{
			gm_type_rule R = new gm_type_rule(gm_operator_t.COMP_OP, gm_operator_type_class_t.T_NUMERIC_INF, gm_operator_type_class_t.T_NUMERIC_INF,
					gm_operator_result_t.RESULT_BOOL, COERCION_ALL);
			GM_TYPE_RULES.add(R);
		}
		;
		{
			gm_type_rule R = new gm_type_rule(gm_operator_t.COMP_OP, gm_operator_type_class_t.T_COMPATIBLE, gm_operator_type_class_t.T_COMPATIBLE,
					gm_operator_result_t.RESULT_BOOL, COERCION_NO);
			GM_TYPE_RULES.add(R);
		}
		;

		{
			gm_type_rule R = new gm_type_rule(gm_operator_t.EQ_OP, gm_operator_type_class_t.T_NUMERIC_INF, gm_operator_type_class_t.T_NUMERIC_INF,
					gm_operator_result_t.RESULT_BOOL, COERCION_ALL);
			GM_TYPE_RULES.add(R);
		}
		;
		{
			gm_type_rule R = new gm_type_rule(gm_operator_t.EQ_OP, gm_operator_type_class_t.T_COMPATIBLE, gm_operator_type_class_t.T_COMPATIBLE,
					gm_operator_result_t.RESULT_BOOL, COERCION_NO);
			GM_TYPE_RULES.add(R);
		}
		;

		{
			gm_type_rule R = new gm_type_rule(gm_operator_t.TER_OP, gm_operator_type_class_t.T_COMPATIBLE, gm_operator_type_class_t.T_COMPATIBLE, RESULT_LEFT,
					gm_operator_coercion_t.COERCION_NO);
			GM_TYPE_RULES.add(R);
		}
		;
		{
			gm_type_rule R = new gm_type_rule(gm_operator_t.TER_OP, gm_operator_type_class_t.T_NUMERIC_INF, gm_operator_type_class_t.T_NUMERIC_INF,
					RESULT_LEFT, gm_operator_coercion_t.COERCION_ALL);
			GM_TYPE_RULES.add(R);
		}
		;

		{
			gm_type_rule R = new gm_type_rule(gm_operator_t.ASSIGN_OP, gm_operator_type_class_t.T_NUMERIC_INF, gm_operator_type_class_t.T_NUMERIC_INF,
					RESULT_LEFT, COERCION_RIGHT);
			GM_TYPE_RULES.add(R);
		}
		;
		{
			gm_type_rule R = new gm_type_rule(gm_operator_t.ASSIGN_OP, gm_operator_type_class_t.T_COMPATIBLE, gm_operator_type_class_t.T_COMPATIBLE,
					RESULT_LEFT, COERCION_RIGHT);
			GM_TYPE_RULES.add(R);
		}
		;

	}

	// C++ TO JAVA CONVERTER NOTE: Access declarations are not available in
	// Java:
	// ;

	public static boolean in_typeclass(gm_operator_type_class_t type1, GMTYPE_T t1) {
		if (type1 == gm_operator_type_class_t.T_INT) {
			return t1.is_long_type() || t1.is_int_type();
		}
		if (type1 == gm_operator_type_class_t.T_NUMERIC) {
			return t1.is_numeric_type();
		}
		if (type1 == gm_operator_type_class_t.T_NUMERIC_INF) {
			return t1.is_inf_type() || t1.is_numeric_type();
		}
		if (type1 == gm_operator_type_class_t.T_BOOL) {
			return t1.is_boolean_type();
		}
		assert false;
		return false;
	}

	public static boolean in_opclass(gm_operator_t opclass, GM_OPS_T op) {
		if (opclass == gm_operator_t.INT_OP) {
			return (op == GM_OPS_T.GMOP_MOD);
		}
		if (opclass == gm_operator_t.NUMERIC_OP) {
			return op.is_numeric_op();
		}
		if (opclass == gm_operator_t.BOOL_OP) {
			return op.is_boolean_op();
		}
		if (opclass == gm_operator_t.COMP_OP) {
			return op.is_less_op();
		}
		if (opclass == gm_operator_t.EQ_OP) {
			return op.is_eq_op();
		}
		if (opclass == gm_operator_t.TER_OP) {
			return op.is_ternary_op();
		}
		if (opclass == gm_operator_t.ASSIGN_OP) {
			return (op == GM_OPS_T.GMOP_ASSIGN);
		}
		assert false;
		return false;
	}

	public static boolean is_applicable_rule(gm_type_rule R, GM_OPS_T op, GMTYPE_T t1, GMTYPE_T t2) {
		if (GlobalMembersGm_typecheck_oprules.in_opclass(R.opclass, op)) {
			if (R.type1 == gm_operator_type_class_t.T_COMPATIBLE)
				return t1.equals(t2) || GMTYPE_T.is_same_node_or_edge_compatible_type(t1, t2);
			else {
				boolean b = GlobalMembersGm_typecheck_oprules.in_typeclass(R.type1, t1) && GlobalMembersGm_typecheck_oprules.in_typeclass(R.type2, t2);

				return b;
			}
		}
		return false;
	}

	public static boolean gm_is_t2_larger_than_t1(GMTYPE_T t1, GMTYPE_T t2) {
		if ((t1 == GMTYPE_T.GMTYPE_INT) && (t2 == GMTYPE_T.GMTYPE_LONG))
			return true;
		if ((t1 == GMTYPE_T.GMTYPE_FLOAT) && (t2 == GMTYPE_T.GMTYPE_DOUBLE))
			return true;
		return false;
	}

	// return false if coercion cannot be done.
	// (lose of precision should be checked separately)
	public static void apply_coercion(int c_type, GMTYPE_T t1, GMTYPE_T t2, RefObject<GMTYPE_T> t1_new, RefObject<GMTYPE_T> t2_new, RefObject<Boolean> t1_warn,
			RefObject<Boolean> t2_warn) {
		t1_new.argvalue = t1;
		t2_new.argvalue = t2;
		t1_warn.argvalue = false;
		t2_warn.argvalue = false;
		if (t1 == t2)
			return;

		if (c_type == gm_operator_coercion_t.COERCION_NO.getValue())
			return;

		// left or right can be converted
		if (c_type == gm_operator_coercion_t.COERCION_ALL.getValue()) {
			if (t1.is_inf_type()) {
				t1_new.argvalue = t2;
				return;
			}
			if (t2.is_inf_type()) {
				t2_new.argvalue = t1;
				return;
			}

			// type-up. (i.e. INT -> LONG)
			if (GlobalMembersGm_typecheck_oprules.gm_is_t2_larger_than_t1(t1, t2)) {
				t1_new.argvalue = t2;
				return;
			}
			if (GlobalMembersGm_typecheck_oprules.gm_is_t2_larger_than_t1(t2, t1)) {
				t2_new.argvalue = t1;
				return;
			}

			// crossing boundary (i.e. <int/long> <-> <float/double>))))
			if (t1.is_float_type()) {
				t2_new.argvalue = t1;
				t2_warn.argvalue = true;

			} else {
				assert t2.is_float_type();
				t1_new.argvalue = t2;
				t1_warn.argvalue = true;
			}

			return;
		}

		// only rhs is allowed to be coerced.
		if (c_type == gm_operator_coercion_t.COERCION_RIGHT.getValue()) {
			if (t2.is_inf_type()) {
				t2_new.argvalue = t1;
				return;
			}

			t2_new.argvalue = t1;

			if ((t1 != t2) && (!GlobalMembersGm_typecheck_oprules.gm_is_t2_larger_than_t1(t2, t1))) {
				t2_warn.argvalue = true;
			}
			return;
		}

		assert false;
		return;
	}

	public static boolean gm_is_compatible_type(GM_OPS_T op, GMTYPE_T t1, GMTYPE_T t2, RefObject<GMTYPE_T> op_result_type, RefObject<GMTYPE_T> t1_new,
			RefObject<Integer> t2_new, RefObject<Boolean> t1_warn, RefObject<Boolean> t2_warn) {
		// special case
		if (t1.is_foreign_expr_type() && t2.is_foreign_expr_type()) {
			op_result_type.argvalue = t1;
			t1_warn.argvalue = t2_warn.argvalue = false;
			return true;
		}

		if (t1.is_foreign_expr_type()) {
			t1 = t2;
		} // believe that foreign-expression is type-compatible
		if (t2.is_foreign_expr_type()) {
			t2 = t1;
		}

		if ((t1.is_set_collection_type() && t2.is_collection_of_set_iter_type()) || (t1.is_sequence_collection_type() && t2.is_collection_of_seq_iter_type())
				|| (t1.is_order_collection_type() && t2.is_collection_of_order_iter_type()))
			t2 = t1;

		for (int i = 0; i < (int) GM_TYPE_RULES.size(); i++) {
			gm_type_rule R = GM_TYPE_RULES.get(i);

			if (GlobalMembersGm_typecheck_oprules.is_applicable_rule(R, op, t1, t2)) {
				// apply coercion
				RefObject<GMTYPE_T> tempRef_t1_new = new RefObject<GMTYPE_T>(t1_new);
				RefObject<Integer> tempRef_t2_new = new RefObject<Integer>(t2_new);
				RefObject<Boolean> tempRef_t1_warn = new RefObject<Boolean>(t1_warn);
				RefObject<Boolean> tempRef_t2_warn = new RefObject<Boolean>(t2_warn);
				GlobalMembersGm_typecheck_oprules.apply_coercion(R.coercion_rule, t1, t2, tempRef_t1_new, tempRef_t2_new, tempRef_t1_warn, tempRef_t2_warn);
				t1_new.argvalue = tempRef_t1_new.argvalue;
				t2_new.argvalue = tempRef_t2_new.argvalue;
				t1_warn.argvalue = tempRef_t1_warn.argvalue;
				t2_warn.argvalue = tempRef_t2_warn.argvalue;

				// get result type
				if (R.result_type == gm_operator_result_t.RESULT_BOOL) {
					op_result_type.argvalue = GMTYPE_T.GMTYPE_BOOL;
				} else {
					op_result_type.argvalue = t1_new.argvalue; // always LHS
																// type after
																// coercion
				}

				return true;
			}
		}

		return false;
	}
}