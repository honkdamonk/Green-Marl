package frontend;

import static frontend.gm_operator.ASSIGN_OP;
import static frontend.gm_operator.BOOL_OP;
import static frontend.gm_operator.COMP_OP;
import static frontend.gm_operator.EQ_OP;
import static frontend.gm_operator.INT_OP;
import static frontend.gm_operator.NUMERIC_OP;
import static frontend.gm_operator.TER_OP;
import static frontend.gm_operator_coercion.COERCION_ALL;
import static frontend.gm_operator_coercion.COERCION_NO;
import static frontend.gm_operator_coercion.COERCION_RIGHT;
import static frontend.gm_operator_result.RESULT_BOOL;
import static frontend.gm_operator_result.RESULT_COERCION;
import static frontend.gm_operator_result.RESULT_LEFT;
import static frontend.gm_operator_type_class.T_BOOL;
import static frontend.gm_operator_type_class.T_COMPATIBLE;
import static frontend.gm_operator_type_class.T_INT;
import static frontend.gm_operator_type_class.T_NUMERIC;
import static frontend.gm_operator_type_class.T_NUMERIC_INF;
import inc.gm_ops;
import inc.gm_type;

import java.util.ArrayList;

import tangible.RefObject;

public class Oprules {

	private static final ArrayList<gm_type_rule> GM_TYPE_RULES = new ArrayList<gm_type_rule>();

	static {
		GM_TYPE_RULES.add(new gm_type_rule(INT_OP, T_INT, T_INT, RESULT_COERCION, COERCION_ALL));
		GM_TYPE_RULES.add(new gm_type_rule(NUMERIC_OP, T_NUMERIC, T_NUMERIC, RESULT_COERCION, COERCION_ALL));
		GM_TYPE_RULES.add(new gm_type_rule(BOOL_OP, T_BOOL, T_BOOL, RESULT_BOOL, COERCION_NO));
		GM_TYPE_RULES.add(new gm_type_rule(COMP_OP, T_NUMERIC_INF, T_NUMERIC_INF, RESULT_BOOL, COERCION_ALL));
		GM_TYPE_RULES.add(new gm_type_rule(COMP_OP, T_COMPATIBLE, T_COMPATIBLE, RESULT_BOOL, COERCION_NO));
		GM_TYPE_RULES.add(new gm_type_rule(EQ_OP, T_NUMERIC_INF, T_NUMERIC_INF, RESULT_BOOL, COERCION_ALL));
		GM_TYPE_RULES.add(new gm_type_rule(EQ_OP, T_COMPATIBLE, T_COMPATIBLE, RESULT_BOOL, COERCION_NO));
		GM_TYPE_RULES.add(new gm_type_rule(TER_OP, T_COMPATIBLE, T_COMPATIBLE, RESULT_LEFT, COERCION_NO));
		GM_TYPE_RULES.add(new gm_type_rule(TER_OP, T_NUMERIC_INF, T_NUMERIC_INF, RESULT_LEFT, COERCION_ALL));
		GM_TYPE_RULES.add(new gm_type_rule(ASSIGN_OP, T_NUMERIC_INF, T_NUMERIC_INF, RESULT_LEFT, COERCION_RIGHT));
		GM_TYPE_RULES.add(new gm_type_rule(ASSIGN_OP, T_COMPATIBLE, T_COMPATIBLE, RESULT_LEFT, COERCION_RIGHT));
	}
	
	private static boolean in_typeclass(gm_operator_type_class type1, gm_type t1) {
		switch (type1) {
		case T_INT:
			return t1.is_long_type() || t1.is_int_type();
		case T_NUMERIC:
			return t1.is_numeric_type();
		case T_NUMERIC_INF:
			return t1.is_inf_type() || t1.is_numeric_type();
		case T_BOOL:
			return t1.is_boolean_type();
		default:
			assert false;
			return false;
		}
	}

	private static boolean in_opclass(gm_operator opclass, gm_ops op) {
		switch (opclass) {
		case INT_OP:
			return (op == gm_ops.GMOP_MOD);
		case NUMERIC_OP:
			return op.is_numeric_op();
		case BOOL_OP:
			return op.is_boolean_op();
		case COMP_OP:
			return op.is_less_op();
		case EQ_OP:
			return op.is_eq_op();
		case TER_OP:
			return op.is_ternary_op();
		case ASSIGN_OP:
			return (op == gm_ops.GMOP_ASSIGN);
		default:
			assert false;
			return false;
		}
	}

	private static boolean is_applicable_rule(gm_type_rule R, gm_ops op, gm_type t1, gm_type t2) {
		if (Oprules.in_opclass(R.opclass, op)) {
			if (R.type1 == T_COMPATIBLE)
				return t1.equals(t2) || gm_type.is_same_node_or_edge_compatible_type(t1, t2);
			else
				return Oprules.in_typeclass(R.type1, t1) && Oprules.in_typeclass(R.type2, t2);
		}
		return false;
	}

	/**
	 * return false if coercion cannot be done. (lose of precision should be
	 * checked separately)
	 * */
	private static void apply_coercion(gm_operator_coercion coercion_rule, gm_type t1, gm_type t2, RefObject<gm_type> t1_new, RefObject<gm_type> t2_new,
			RefObject<Boolean> t1_warn, RefObject<Boolean> t2_warn) {
		t1_new.argvalue = t1;
		t2_new.argvalue = t2;
		t1_warn.argvalue = false;
		t2_warn.argvalue = false;
		if (t1 == t2)
			return;

		if (coercion_rule == COERCION_NO)
			return;

		// left or right can be converted
		if (coercion_rule == COERCION_ALL) {
			if (t1.is_inf_type()) {
				t1_new.argvalue = t2;
				return;
			}
			if (t2.is_inf_type()) {
				t2_new.argvalue = t1;
				return;
			}

			// type-up. (i.e. INT -> LONG)
			if (gm_type.is_t2_larger_than_t1(t1, t2)) {
				t1_new.argvalue = t2;
				return;
			}
			if (gm_type.is_t2_larger_than_t1(t2, t1)) {
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
		if (coercion_rule == COERCION_RIGHT) {
			if (t2.is_inf_type()) {
				t2_new.argvalue = t1;
				return;
			}

			t2_new.argvalue = t1;

			if ((t1 != t2) && (!gm_type.is_t2_larger_than_t1(t2, t1))) {
				t2_warn.argvalue = true;
			}
			return;
		}

		assert false;
		return;
	}

	public static boolean gm_is_compatible_type(gm_ops op, gm_type t1, gm_type t2, RefObject<gm_type> op_result_type, RefObject<gm_type> t1_new,
			RefObject<gm_type> t2_new, RefObject<Boolean> t1_warn, RefObject<Boolean> t2_warn) {
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

			if (Oprules.is_applicable_rule(R, op, t1, t2)) {
				// apply coercion
				RefObject<gm_type> tempRef_t1_new = t1_new;
				RefObject<gm_type> tempRef_t2_new = t2_new;
				RefObject<Boolean> tempRef_t1_warn = t1_warn;
				RefObject<Boolean> tempRef_t2_warn = t2_warn;
				Oprules.apply_coercion(R.coercion_rule, t1, t2, tempRef_t1_new, tempRef_t2_new, tempRef_t1_warn, tempRef_t2_warn);
				t1_new.argvalue = tempRef_t1_new.argvalue;
				t2_new.argvalue = tempRef_t2_new.argvalue;
				t1_warn.argvalue = tempRef_t1_warn.argvalue;
				t2_warn.argvalue = tempRef_t2_warn.argvalue;

				// get result type
				if (R.result_type == RESULT_BOOL) {
					op_result_type.argvalue = gm_type.GMTYPE_BOOL;
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