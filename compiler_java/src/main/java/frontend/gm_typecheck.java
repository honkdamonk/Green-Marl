package frontend;

import tangible.RefObject;
import inc.GMTYPE_T;
import inc.GM_OPS_T;

public class gm_typecheck {

	public static final boolean GM_READ_AVAILABLE = true;
	public static final boolean GM_READ_NOT_AVAILABLE = false;
	public static final boolean GM_WRITE_AVAILABLE = true;
	public static final boolean GM_WRITE_NOT_AVAILABLE = false;

	// ----------------------------------------------------------------------------------------------------------------
	// Utility functions (Type summary)
	// ----------------------------------------------------------------------------------------------------------------

	/** check the byte size of two numeric type */
	public static int gm_compare_numeric_type_size(GMTYPE_T t1, GMTYPE_T t2) {
		// GMTYPE_... is defined as small to larger
		return t1.subtract(t2); // +:t1 > t2 , 0:t2==t2, -:t1 < t2
	}

	/** check the size (in Bytes) of two numeric types */
	public static GMTYPE_T gm_get_larger_type(GMTYPE_T t1, GMTYPE_T t2) {
		if (gm_compare_numeric_type_size(t1, t2) > 0)
			return t1;
		else
			return t2;
	}

	/** determine resulting type of numeric operation A (+,-,*,/) B */
	public static GMTYPE_T gm_determine_result_type(GMTYPE_T t1, GMTYPE_T t2) {
		// assumption. t1/t2 is compatible
		if (t1 == t2)
			return t1;
		else if (t1.is_inf_type())
			return t1;
		else if (t2.is_inf_type())
			return t2;
		else if (t1.is_numeric_type()) {
			if (t1.is_float_type() == t2.is_float_type())
				return gm_typecheck.gm_get_larger_type(t1, t2);
			else if (t1.is_float_type())
				return t1;
			else
				return t2;
		} else if (t1.is_iter_type())
			return t2;
		else if (t1.is_iter_type())
			return t1;
		else {
			assert false;
			return GMTYPE_T.GMTYPE_INVALID;
		}
	}

	public static boolean gm_check_compatible_types(GMTYPE_T t1, GMTYPE_T t2, gm_type_compatible_t for_what) {
		if (t1 == t2)
			return true;

		// ----------------------------------------------------------
		// ASSUMTION
		// let t1 be the 'smaller' type (ordering by GM_XXX_TYPE enumeration)
		// GRAPH -> PROP -> NODE-EDGE/ITER -> NUMERIC -> BOOL -> INF (see
		// gm_frontend_api.h)
		// ----------------------------------------------------------
		if (t2.isSmallerThan(t1)) {
			GMTYPE_T t3;
			t3 = t1;
			t1 = t2;
			t2 = t3;
		}

		if (t1.is_node_compatible_type()) {
			if (for_what == gm_type_compatible_t.FOR_BOP)
				return false;
			else
				return t2.is_node_compatible_type();
		}

		if (t1.is_edge_compatible_type()) {
			if (for_what == gm_type_compatible_t.FOR_BOP)
				return false;
			else
				return t2.is_edge_compatible_type();
		}

		if (t1.is_numeric_type()) {
			if (for_what == gm_type_compatible_t.FOR_BOP)
				return t2.is_numeric_type();
			else {
				// it is possible to assign INF to numeric
				return t2.is_numeric_type() || t2.is_inf_type();
			}
		}

		if (t1.is_boolean_type())
			return (t2.is_boolean_type());
		if (t1.is_inf_type())
			return t2.is_inf_type();

		// printf("unexpected type = %s\n", gm_get_type_string(t1));
		// assert(false);
		return false;
	}

	public static boolean gm_is_compatible_type(GM_OPS_T op, GMTYPE_T t1, GMTYPE_T t2, RefObject<Integer> op_result_type, RefObject<GMTYPE_T> t1_coerced,
			RefObject<GMTYPE_T> t2_coerced, RefObject<Boolean> t1_coerced_lost_precision, RefObject<Boolean> t2_coerced_lost_precision) {

		return false;
	}

	public static boolean gm_is_compatible_type_for_assign(GMTYPE_T t_lhs, GMTYPE_T t_rhs, RefObject<GMTYPE_T> t_new_rhs, RefObject<Boolean> warn_ref) {
		RefObject<GMTYPE_T> dummy1 = new RefObject<GMTYPE_T>(null);
		RefObject<GMTYPE_T> dummy2 = new RefObject<GMTYPE_T>(null);
		RefObject<Boolean> dummy3 = new RefObject<Boolean>(null);
		return Oprules.gm_is_compatible_type(GM_OPS_T.GMOP_ASSIGN, t_lhs, t_rhs, dummy1, dummy2, t_new_rhs, dummy3, warn_ref);
	}

	public static boolean gm_is_compatible_type_for_assign(GMTYPE_T lhs, GMTYPE_T rhs) {
		return gm_check_compatible_types(lhs, rhs, gm_type_compatible_t.FOR_ASSIGN);
	}

	public static boolean gm_is_compatible_type_for_eq(GMTYPE_T t1, GMTYPE_T t2) {
		return gm_check_compatible_types(t1, t2, gm_type_compatible_t.FOR_EQ);
	}

	public static boolean gm_is_compatible_type_for_less(GMTYPE_T t1, GMTYPE_T t2) {
		return gm_check_compatible_types(t1, t2, gm_type_compatible_t.FOR_LESS);
	}

	public static boolean gm_is_compatible_type_for_biop(GMTYPE_T t1, GMTYPE_T t2) {
		return gm_check_compatible_types(t1, t2, gm_type_compatible_t.FOR_BOP);
	}

}