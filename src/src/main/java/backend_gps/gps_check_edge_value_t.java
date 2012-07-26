package backend_gps;
import static common.GM_ERRORS_AND_WARNINGS.GM_ERROR_GPS_EDGE_READ_RANDOM;
import static common.GM_ERRORS_AND_WARNINGS.GM_ERROR_GPS_EDGE_SEND_VERSIONS;
import static common.GM_ERRORS_AND_WARNINGS.GM_ERROR_GPS_EDGE_WRITE_CONDITIONAL;
import static common.GM_ERRORS_AND_WARNINGS.GM_ERROR_GPS_EDGE_WRITE_RHS;
import static inc.GlobalMembersGm_backend_gps.GPS_FLAG_EDGE_DEFINED_INNER;
import static inc.GlobalMembersGm_backend_gps.GPS_FLAG_EDGE_DEFINING_INNER;
import static inc.GlobalMembersGm_backend_gps.GPS_FLAG_EDGE_DEFINING_WRITE;
import static inc.GlobalMembersGm_backend_gps.GPS_FLAG_IS_INNER_LOOP;
import static inc.GlobalMembersGm_backend_gps.GPS_INT_EXPR_SCOPE;
import static inc.GlobalMembersGm_backend_gps.GPS_LIST_EDGE_PROP_WRITE;
import ast.AST_NODE_TYPE;
import ast.ast_assign;
import ast.ast_expr;
import ast.ast_expr_builtin;
import ast.ast_field;
import ast.ast_foreach;
import ast.ast_id;
import ast.ast_sent;

import common.GlobalMembersGm_error;
import common.gm_apply;
import common.gm_method_id_t;

import frontend.SYMTAB_TYPES;
import frontend.gm_symtab_entry;

public class gps_check_edge_value_t extends gm_apply {
	public gps_check_edge_value_t() {
		set_separate_post_apply(true);
		set_for_symtab(true);
		set_for_sent(true);
		set_for_expr(true);
		inner_iter = null;
		inner_loop = null;
		_error = false;
		target_is_edge_prop = false;
	}

	public final boolean is_error() {
		return _error;
	}

	public final void set_error(boolean b) {
		_error = b;
	}

	@Override
	public boolean apply(gm_symtab_entry e, SYMTAB_TYPES type) {
		if (e.getType().is_edge() && (inner_loop != null)) {
			e.add_info_bool(GPS_FLAG_EDGE_DEFINED_INNER, true);
			inner_loop.add_info_bool(GPS_FLAG_EDGE_DEFINING_INNER, true);
		}
		return true;
	}

	@Override
	public boolean apply(ast_sent s) {
		if (s.get_nodetype() == AST_NODE_TYPE.AST_FOREACH) {
			ast_foreach fe = (ast_foreach) s;
			if (fe.find_info_bool(GPS_FLAG_IS_INNER_LOOP)) {
				inner_iter = fe.get_iterator().getSymInfo();
				inner_loop = fe;
			}
		} else if (s.get_nodetype() == AST_NODE_TYPE.AST_ASSIGN) {
			ast_assign a = (ast_assign) s;
			if (!a.is_target_scalar()) {
				gm_symtab_entry sym = a.get_lhs_field().get_first().getSymInfo();
				if (sym.getType().is_edge_compatible()) {

					if (sym.find_info_bool(GPS_FLAG_EDGE_DEFINED_INNER)) {
						ast_sent parent = (ast_sent) s.get_parent();

						// check if conditional write
						boolean conditional = false;
						while (true) {
							if (parent == inner_loop)
								break;

							if ((parent.get_nodetype() == AST_NODE_TYPE.AST_WHILE) || (parent.get_nodetype() == AST_NODE_TYPE.AST_IF)
									|| (parent.get_nodetype() == AST_NODE_TYPE.AST_FOREACH)) {
								conditional = true;
								break;
							}
							parent = (ast_sent) parent.get_parent();
							assert parent != null;
						}

						if (conditional) {
							GlobalMembersGm_error.gm_backend_error(GM_ERROR_GPS_EDGE_WRITE_CONDITIONAL, a.get_lhs_field().get_line(), a.get_lhs_field()
									.get_col());
							set_error(true);
						}

						target_is_edge_prop = true;

						// add write symbol
						assert inner_loop != null;
						inner_loop.add_info_list_element(GPS_LIST_EDGE_PROP_WRITE, s);

						gm_symtab_entry target = a.get_lhs_field().get_second().getSymInfo();
						boolean b = GlobalMembersGm_gps_new_check_edge_value.manage_edge_prop_access_state(inner_loop, target,
								GlobalMembersGm_gps_new_check_edge_value.WRITING);
						assert b == false;

						// [TODO]
						// grouped assignment?

					} else {
						/*
						 * gm_backend_error(GM_ERROR_GPS_EDGE_WRITE_RANDOM,
						 * a->get_lhs_field()->get_line(),
						 * a->get_lhs_field()->get_col()); set_error(true);
						 */
					}
				}
			} // lhs scala
			else {
				gm_symtab_entry sym = a.get_lhs_scala().getSymInfo();
				if (sym.getType().is_edge()) {
					if (sym.find_info_bool(GPS_FLAG_EDGE_DEFINED_INNER)) {
						// check rhs is to - edge
						ast_expr rhs = a.get_rhs();
						if (rhs.is_builtin()) {
							ast_expr_builtin b_rhs = (ast_expr_builtin) rhs;
							gm_symtab_entry drv = b_rhs.get_driver().getSymInfo();
							gm_method_id_t f_id = b_rhs.get_builtin_def().get_method_id();
							if (f_id == gm_method_id_t.GM_BLTIN_NODE_TO_EDGE) {
								a.add_info_bool(GPS_FLAG_EDGE_DEFINING_WRITE, true);
							}
						}

						/*
						 * if (error) { set_error(error);
						 * gm_backend_error(GM_ERROR_GPS_EDGE_WRITE_RANDOM,
						 * a->get_lhs_scala()->get_line(),
						 * a->get_lhs_scala()->get_col()); }
						 */

					}
				}
			}
		}

		return true;
	}

	// random edge read is not allowed.
	@Override
	public boolean apply(ast_expr e) {
		// -----------------------------------------------
		// Edge f = ...
		// Foreach (t: G.Nodes) {
		// Foreach (n: t.Nbrs) {
		// Edge e = n.ToEdge();
		// Int x = f.A; // (case 1) random reading
		// e.A = n.X; // (case 2) inner scoped rhs
		//
		// ... = e.A;
		// e.A = ...; // (case 3) sending two versions
		// ... = e.A;
		//
		// }
		// }
		// -----------------------------------------------

		// checking of (case 2)
		if (target_is_edge_prop) {
			if ((e.find_info_bool(GPS_INT_EXPR_SCOPE) == gm_gps_new_scope_analysis_t.GPS_NEW_SCOPE_IN)
					|| (e.find_info_bool(GPS_INT_EXPR_SCOPE) == gm_gps_new_scope_analysis_t.GPS_NEW_SCOPE_RANDOM)) {
				if (e.is_field()) {
					ast_field f = e.get_field();
					GlobalMembersGm_error.gm_backend_error(GM_ERROR_GPS_EDGE_WRITE_RHS, f.get_line(), f.get_col(), f.get_first().get_orgname());
					set_error(true);
				} else if (e.is_id()) {
					ast_id f = e.get_id();
					GlobalMembersGm_error.gm_backend_error(GM_ERROR_GPS_EDGE_WRITE_RHS, f.get_line(), f.get_col(), f.get_orgname());
					set_error(true);
				}
			}
		}

		if (e.is_field()) {
			ast_field f = e.get_field();
			if (f.getSourceTypeInfo().is_edge_compatible()) {
				// check if random reading (case 1)
				if (!f.get_first().getSymInfo().find_info_bool(GPS_FLAG_EDGE_DEFINED_INNER)) {
					GlobalMembersGm_error.gm_backend_error(GM_ERROR_GPS_EDGE_READ_RANDOM, f.get_line(), f.get_col());
					set_error(true);
				} else {
					// (case 3)
					boolean b = GlobalMembersGm_gps_new_check_edge_value.manage_edge_prop_access_state(inner_loop, f.get_second().getSymInfo(),
							GlobalMembersGm_gps_new_check_edge_value.SENDING);

					if (b) {
						GlobalMembersGm_error.gm_backend_error(GM_ERROR_GPS_EDGE_SEND_VERSIONS, f.get_line(), f.get_col(), f.get_first().get_orgname());
						set_error(true);
					}
				}
			}
		}

		return true;
	}

	@Override
	public boolean apply2(ast_sent s) {
		if (s.get_nodetype() == AST_NODE_TYPE.AST_FOREACH) {
			if (((ast_foreach) s) == inner_loop) {
				inner_loop = null;
				inner_iter = null;
			}
		} else if (s.get_nodetype() == AST_NODE_TYPE.AST_ASSIGN) {
			target_is_edge_prop = false;
		}
		return true;
	}

	private gm_symtab_entry inner_iter;
	private ast_foreach inner_loop;
	private boolean target_is_edge_prop;
	private boolean _error;
}