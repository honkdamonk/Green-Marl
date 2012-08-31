package backend_gps;

import static backend_gps.GPSConstants.GPS_FLAG_EDGE_DEFINED_INNER;
import static backend_gps.GPSConstants.GPS_FLAG_EDGE_DEFINING_INNER;
import static backend_gps.GPSConstants.GPS_FLAG_EDGE_DEFINING_WRITE;
import static backend_gps.GPSConstants.GPS_FLAG_IS_INNER_LOOP;
import static backend_gps.GPSConstants.GPS_INT_EXPR_SCOPE;
import static backend_gps.GPSConstants.GPS_LIST_EDGE_PROP_WRITE;
import static backend_gps.GPSConstants.GPS_MAP_EDGE_PROP_ACCESS;
import static common.GM_ERRORS_AND_WARNINGS.GM_ERROR_GPS_EDGE_READ_RANDOM;
import static common.GM_ERRORS_AND_WARNINGS.GM_ERROR_GPS_EDGE_SEND_VERSIONS;
import static common.GM_ERRORS_AND_WARNINGS.GM_ERROR_GPS_EDGE_WRITE_CONDITIONAL;
import static common.GM_ERRORS_AND_WARNINGS.GM_ERROR_GPS_EDGE_WRITE_RHS;
import ast.AST_NODE_TYPE;
import ast.ast_assign;
import ast.ast_expr;
import ast.ast_expr_builtin;
import ast.ast_field;
import ast.ast_foreach;
import ast.ast_id;
import ast.ast_sent;

import common.gm_error;
import common.gm_apply;
import common.gm_method_id_t;

import frontend.SYMTAB_TYPES;
import frontend.gm_symtab_entry;

public class gps_check_edge_value_t extends gm_apply {

	private static final int SENDING = 1;
	private static final int WRITING = 2;

	private ast_foreach inner_loop = null;
	private boolean target_is_edge_prop = false;
	private boolean _error = false;

	public gps_check_edge_value_t() {
		set_separate_post_apply(true);
		set_for_symtab(true);
		set_for_sent(true);
		set_for_expr(true);
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
							gm_error.gm_backend_error(GM_ERROR_GPS_EDGE_WRITE_CONDITIONAL, a.get_lhs_field().get_line(), a.get_lhs_field()
									.get_col());
							set_error(true);
						}

						target_is_edge_prop = true;

						// add write symbol
						assert inner_loop != null;
						inner_loop.add_info_list_element(GPS_LIST_EDGE_PROP_WRITE, s);

						gm_symtab_entry target = a.get_lhs_field().get_second().getSymInfo();
						boolean b = manage_edge_prop_access_state(inner_loop, target, WRITING);
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
			if ((e.find_info_int(GPS_INT_EXPR_SCOPE) == gm_gps_new_scope_analysis_t.GPS_NEW_SCOPE_IN.getValue())
					|| (e.find_info_int(GPS_INT_EXPR_SCOPE) == gm_gps_new_scope_analysis_t.GPS_NEW_SCOPE_RANDOM.getValue())) {
				if (e.is_field()) {
					ast_field f = e.get_field();
					gm_error.gm_backend_error(GM_ERROR_GPS_EDGE_WRITE_RHS, f.get_line(), f.get_col(), f.get_first().get_orgname());
					set_error(true);
				} else if (e.is_id()) {
					ast_id f = e.get_id();
					gm_error.gm_backend_error(GM_ERROR_GPS_EDGE_WRITE_RHS, f.get_line(), f.get_col(), f.get_orgname());
					set_error(true);
				}
			}
		}

		if (e.is_field()) {
			ast_field f = e.get_field();
			if (f.getSourceTypeInfo().is_edge_compatible()) {
				// check if random reading (case 1)
				if (!f.get_first().getSymInfo().find_info_bool(GPS_FLAG_EDGE_DEFINED_INNER)) {
					gm_error.gm_backend_error(GM_ERROR_GPS_EDGE_READ_RANDOM, f.get_line(), f.get_col());
					set_error(true);
				} else {
					// (case 3)
					boolean b = manage_edge_prop_access_state(inner_loop, f.get_second().getSymInfo(), SENDING);

					if (b) {
						gm_error.gm_backend_error(GM_ERROR_GPS_EDGE_SEND_VERSIONS, f.get_line(), f.get_col(), f.get_first().get_orgname());
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
			}
		} else if (s.get_nodetype() == AST_NODE_TYPE.AST_ASSIGN) {
			target_is_edge_prop = false;
		}
		return true;
	}

	// return: is_error
	private static boolean manage_edge_prop_access_state(ast_foreach fe, gm_symtab_entry e, int op) {

		assert (op == SENDING) || (op == WRITING);
		gm_gps_edge_access_t curr_state = (gm_gps_edge_access_t) fe.find_info_map_value(GPS_MAP_EDGE_PROP_ACCESS, e);

		// first access
		if (curr_state == null) {
			gm_gps_edge_access_t new_state = (op == SENDING) ? gm_gps_edge_access_t.GPS_ENUM_EDGE_VALUE_SENT : gm_gps_edge_access_t.GPS_ENUM_EDGE_VALUE_WRITE;

			fe.add_info_map_key_value(GPS_MAP_EDGE_PROP_ACCESS, e, new_state);
		} else {
			gm_gps_edge_access_t curr_state_val = curr_state;
			switch (curr_state_val) {
			case GPS_ENUM_EDGE_VALUE_ERROR: // already error
				return false;

			case GPS_ENUM_EDGE_VALUE_WRITE:
				if (op == SENDING)
					curr_state = gm_gps_edge_access_t.GPS_ENUM_EDGE_VALUE_WRITE_SENT;
				return false; // no error

			case GPS_ENUM_EDGE_VALUE_SENT:
				if (op == WRITING)
					curr_state = gm_gps_edge_access_t.GPS_ENUM_EDGE_VALUE_SENT_WRITE;
				return false; // no error

			case GPS_ENUM_EDGE_VALUE_WRITE_SENT:
				if (op == WRITING)
					curr_state = gm_gps_edge_access_t.GPS_ENUM_EDGE_VALUE_SENT_WRITE;
				return false;

			case GPS_ENUM_EDGE_VALUE_SENT_WRITE:
				// sending two versions!
				if (op == SENDING) {
					curr_state = gm_gps_edge_access_t.GPS_ENUM_EDGE_VALUE_ERROR;
					return true; // ERROR
				} else
					return false;
			default:
				assert false;
				break;
			}
		}
		return false;
	}

}