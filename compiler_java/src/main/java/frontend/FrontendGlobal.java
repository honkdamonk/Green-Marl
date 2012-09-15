package frontend;

import inc.gm_ops;
import inc.gm_reduce;
import inc.gm_assignment;

import java.util.Iterator;
import java.util.LinkedList;

import ast.ast_node_type;
import ast.ast_assign;
import ast.ast_expr;
import ast.ast_field;
import ast.ast_id;
import ast.ast_if;
import ast.ast_node;
import ast.ast_sentblock;

import common.gm_transform_helper;

public final class FrontendGlobal {
	
	private FrontendGlobal() {
		// do not instantiate me
	}
	
	/**
	 * make reduction into a normal assignment e.g. LHS += <expr> --> LHS = LHS
	 * + <expr>
	 * 
	 * from gm_fixup_bound_symbol
	 */
	public static void gm_make_normal_assign(ast_assign a) {
		assert a.is_reduce_assign();

		// assumption: a belongs to a sentence block
		assert a.get_parent().get_nodetype() == ast_node_type.AST_SENTBLOCK;

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
			new_rhs = ast_expr.new_biop_expr(gm_ops.GMOP_ADD, base, org_rhs);
			break;
		case GMREDUCE_MULT:
			new_rhs = ast_expr.new_biop_expr(gm_ops.GMOP_MULT, base, org_rhs);
			break;
		case GMREDUCE_AND:
			new_rhs = ast_expr.new_biop_expr(gm_ops.GMOP_AND, base, org_rhs);
			break;
		case GMREDUCE_OR:
			new_rhs = ast_expr.new_biop_expr(gm_ops.GMOP_OR, base, org_rhs);
			break;
		case GMREDUCE_MIN:
			if (a.is_argminmax_assign())
				new_rhs = org_rhs.copy(true);
			else
				new_rhs = ast_expr.new_biop_expr(gm_ops.GMOP_MIN, base, org_rhs);
			break;
		case GMREDUCE_MAX:
			if (a.is_argminmax_assign())
				new_rhs = org_rhs.copy(true);
			else
				new_rhs = ast_expr.new_biop_expr(gm_ops.GMOP_MAX, base, org_rhs);
			break;
		default:
			assert false;
			break;
		}

		if (!a.is_argminmax_assign()) {
			a.set_rhs(new_rhs);
		} else {

			// <l; l1, l2> min= <r; r1, r2>

			// ==> becomes
			// if (l_copy > r_copy) {
			// l = r;
			// l1 = r1;
			// l2 = r2;
			// }

			// (l>r)
			gm_ops comp = (a.get_reduce_type() == gm_reduce.GMREDUCE_MIN) ? gm_ops.GMOP_GT : gm_ops.GMOP_LT;
			ast_expr cond = ast_expr.new_comp_expr(comp, base, new_rhs);

			// if (l>r) {}
			ast_sentblock sb = ast_sentblock.new_sentblock();
			ast_if iff = ast_if.new_if(cond, sb, null);

			// adding if, in place of original assignment
			gm_transform_helper.gm_add_sent_after(a, iff);
			gm_transform_helper.gm_ripoff_sent(a, true);

			// adding a inside sb
			sb.add_sent(a);

			// adding LHSx = RHSx inside iff
			LinkedList<ast_node> L = a.get_lhs_list();
			LinkedList<ast_expr> R = a.get_rhs_list();
			Iterator<ast_node> I = L.iterator();
			Iterator<ast_expr> J = R.iterator();
			while (I.hasNext()) {
				ast_node l = I.next();
				ast_expr r = J.next();
				ast_assign aa;
				if (l.get_nodetype() == ast_node_type.AST_ID)
					aa = ast_assign.new_assign_scala((ast_id) l, r);
				else if (l.get_nodetype() == ast_node_type.AST_FIELD)
					aa = ast_assign.new_assign_field((ast_field) l, r);
				else {
					assert false;
					throw new AssertionError();
				}
				sb.add_sent(aa);
			}
		}

		a.set_assign_type(gm_assignment.GMASSIGN_NORMAL);
		a.set_reduce_type(gm_reduce.GMREDUCE_NULL);
		ast_id old_iter = a.get_bound(); // assert(old_iter != NULL);
		a.set_bound(null);
		if (old_iter != null)
			if (old_iter != null)
				old_iter.dispose();
	}

}
