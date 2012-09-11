package tangible;

import frontend.gm_rwinfo;
import frontend.gm_symtab_entry;
import inc.GMTYPE_T;
import inc.GM_OPS_T;
import inc.GM_REDUCE_T;
import inc.gm_assignment_t;
import ast.ast_assign;
import ast.ast_expr;
import ast.ast_id;
import ast.gm_rwinfo_list;
import ast.gm_rwinfo_map;

public class ProbablyUnused {

	// From gm_fixup_bound_symbol_old
	public static void gm_make_normal_assign(ast_assign a) {
		// -----------------------------------
		// make it a normal assignment
		// LHS += <expr>
		// -->
		// LHS = LHS + <expr>
		// -----------------------------------
		assert a.is_reduce_assign();

		ast_expr base;
		if (a.is_target_scalar()) {
			base = ast_expr.new_id_expr(a.get_lhs_scala().copy(true));
		} else {
			base = ast_expr.new_field_expr(a.get_lhs_field().copy(true));
		}

		ast_expr org_rhs = a.get_rhs();
		assert org_rhs != null;
		ast_expr new_rhs = null;
		switch (a.get_reduce_type()) {
		case GMREDUCE_PLUS:
			new_rhs = ast_expr.new_biop_expr(GM_OPS_T.GMOP_ADD, base, org_rhs);
			break;
		case GMREDUCE_MULT:
			new_rhs = ast_expr.new_biop_expr(GM_OPS_T.GMOP_MULT, base, org_rhs);
			break;
		case GMREDUCE_MIN:
			new_rhs = ast_expr.new_biop_expr(GM_OPS_T.GMOP_MIN, base, org_rhs);
			break;
		case GMREDUCE_MAX:
			new_rhs = ast_expr.new_biop_expr(GM_OPS_T.GMOP_MAX, base, org_rhs);
			break;
		default:
			assert false;
			break;
		}

		a.set_rhs(new_rhs);
		a.set_assign_type(gm_assignment_t.GMASSIGN_NORMAL);
		a.set_reduce_type(GM_REDUCE_T.GMREDUCE_NULL);
		ast_id old_iter = a.get_bound(); // assert(old_iter != NULL);
		a.set_bound(null);
		if (old_iter != null)
			old_iter.dispose(); // can be null but delete is no harmful
	}

	// ----------------------------------------------------------------------------------------------------------------
	// From gm_typecheck
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
				return gm_get_larger_type(t1, t2);
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

	/** For debug */
	public static void gm_print_rwinfo_set(gm_rwinfo_map m) {
		boolean first = true;
		for (gm_symtab_entry e : m.keySet()) {
			gm_rwinfo_list l = m.get(e);
			if (first)
				first = false;
			else
				System.out.print(",");

			if (e.getType().is_property())
				System.out.printf("{%s(%s):", e.getId().get_orgname(), e.getType().get_target_graph_id().get_orgname());
			else
				System.out.printf("{%s:", e.getId().get_orgname());

			boolean _first = true;
			for (gm_rwinfo info : l) {
				if (_first)
					_first = false;
				else
					System.out.print(",");
				info.print();
			}
			System.out.print("}");
		}
		System.out.print("\n");
	}

}
