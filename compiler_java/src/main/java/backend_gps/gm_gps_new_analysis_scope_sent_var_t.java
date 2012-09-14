package backend_gps;

import static backend_gps.GPSConstants.GPS_FLAG_IS_EDGE_ITERATOR;
import static backend_gps.GPSConstants.GPS_FLAG_IS_INNER_LOOP;
import static backend_gps.GPSConstants.GPS_FLAG_IS_OUTER_LOOP;
import static backend_gps.GPSConstants.GPS_INT_SYMBOL_SCOPE;
import static backend_gps.GPSConstants.GPS_INT_SYNTAX_CONTEXT;
import static inc.gps_apply_bb.GPS_TAG_BB_USAGE;
import inc.GMTYPE_T;
import ast.AST_NODE_TYPE;
import ast.ast_assign;
import ast.ast_expr;
import ast.ast_expr_builtin;
import ast.ast_extra_info;
import ast.ast_foreach;
import ast.ast_procdef;
import ast.ast_sent;

import common.gm_apply;
import common.gm_method_id_t;

import frontend.SYMTAB_TYPES;
import frontend.gm_symtab_entry;

public class gm_gps_new_analysis_scope_sent_var_t extends gm_apply {

	private ast_foreach outer_loop = null;
	private ast_foreach inner_loop = null;
	private gm_gps_new_scope_analysis_t current_scope = gm_gps_new_scope_analysis_t.GPS_NEW_SCOPE_GLOBAL;

	public gm_gps_new_analysis_scope_sent_var_t() {
		set_separate_post_apply(true);
		set_for_sent(true);
		set_for_symtab(true);
	}

	public final boolean apply(gm_symtab_entry e, SYMTAB_TYPES symtab_type) {
		e.add_info_int(GPS_INT_SYMBOL_SCOPE, current_scope.getValue());

		// ---------------------------------------------------------------------------
		// This information is redundant at this moment. Need to be clear up
		// ---------------------------------------------------------------------------
		gm_gps_scope_t s;
		switch (current_scope) {
		case GPS_NEW_SCOPE_GLOBAL:
			s = gm_gps_scope_t.GPS_SCOPE_GLOBAL;
			break;
		case GPS_NEW_SCOPE_IN:
			s = gm_gps_scope_t.GPS_SCOPE_INNER;
			break;
		case GPS_NEW_SCOPE_EDGE:
		case GPS_NEW_SCOPE_RANDOM:
		case GPS_NEW_SCOPE_OUT:
		default:
			s = gm_gps_scope_t.GPS_SCOPE_OUTER;
			break;
		}
		add_syminfo_struct(e, (symtab_type != SYMTAB_TYPES.GM_SYMTAB_FIELD), s);

		return true;
	}

	public final boolean apply(ast_sent s) {
		s.add_info_int(GPS_INT_SYNTAX_CONTEXT, current_scope.getValue());

		if (s.get_nodetype() == AST_NODE_TYPE.AST_FOREACH) {
			ast_foreach fe = (ast_foreach) s;
			if (outer_loop == null) {
				assert fe.get_iterator().getTypeSummary() == GMTYPE_T.GMTYPE_NODEITER_ALL;
				outer_loop = fe;
				outer_loop.add_info_bool(GPS_FLAG_IS_OUTER_LOOP, true);
				outer_loop.get_iterator().getSymInfo().add_info_int(GPS_INT_SYMBOL_SCOPE, gm_gps_new_scope_analysis_t.GPS_NEW_SCOPE_OUT.getValue());
				outer_loop.get_iterator().getSymInfo().add_info_bool(GPS_FLAG_IS_OUTER_LOOP, true);
				current_scope = gm_gps_new_scope_analysis_t.GPS_NEW_SCOPE_OUT;
			} else if (inner_loop == null) {
				assert (fe.get_iterator().getTypeSummary() == GMTYPE_T.GMTYPE_NODEITER_NBRS)
						|| (fe.get_iterator().getTypeSummary() == GMTYPE_T.GMTYPE_NODEITER_IN_NBRS);
				inner_loop = fe;
				inner_loop.add_info_bool(GPS_FLAG_IS_INNER_LOOP, true);
				inner_loop.get_iterator().getSymInfo().add_info_int(GPS_INT_SYMBOL_SCOPE, gm_gps_new_scope_analysis_t.GPS_NEW_SCOPE_IN.getValue());
				inner_loop.get_iterator().getSymInfo().add_info_bool(GPS_FLAG_IS_INNER_LOOP, true);
				current_scope = gm_gps_new_scope_analysis_t.GPS_NEW_SCOPE_IN;
			} else {
				assert false;
			}
		} else if (s.get_nodetype() == AST_NODE_TYPE.AST_ASSIGN) {
			ast_assign a = (ast_assign) s;
			if (a.is_target_scalar() && a.get_lhs_scala().getTypeInfo().is_edge_compatible()) {
				if (current_scope == gm_gps_new_scope_analysis_t.GPS_NEW_SCOPE_IN) {
					// check rhs
					ast_expr rhs = a.get_rhs();
					assert rhs.is_builtin();
					ast_expr_builtin b = (ast_expr_builtin) rhs;

					if ((b.get_driver().getSymInfo().find_info_bool(GPS_FLAG_IS_INNER_LOOP))
							&& (b.get_builtin_def().get_method_id() == gm_method_id_t.GM_BLTIN_NODE_TO_EDGE)) {
						a.get_lhs_scala().getSymInfo().add_info_bool(GPS_FLAG_IS_EDGE_ITERATOR, true);
					}
				}
			}
		}

		return true;
	}

	public final boolean apply2(ast_sent s) {
		if (s == outer_loop) {
			current_scope = gm_gps_new_scope_analysis_t.GPS_NEW_SCOPE_GLOBAL;
			outer_loop = null;
		} else if (s == inner_loop) {
			current_scope = gm_gps_new_scope_analysis_t.GPS_NEW_SCOPE_OUT;
			inner_loop = null;
		}
		return true;
	}

	// -----------------------------------------------------------------------------------------------------------------------
	// < Step 1>
	// * Check the scope of each variable: GLOBAL, OUT, IN
	// * Check the context of each statement: GLOBAL, OUT, IN
	//
	// Int x; // global scope
	// Int x2;
	//
	// Foreach(s: G.Nodes) { // outer loop
	//   Int y;
	//   Int y2;
	//
	//   If (s.A + x > 0) // (s.A + x > 0) ==> EXPR_OUT
	//     x2 += s.B; // assignment: PREFIX_COND_OUT, lhs: GLOBAL, rhs: OUT,
	//
	//   Foreach(t: s.Nbrs) { // inner loop
	//     Int z;
	//     Int z2;
	//   }
	// }
	// -----------------------------------------------------------------------------------------------------------------------
	public static void add_syminfo_struct(gm_symtab_entry sym, boolean is_scalar, gm_gps_scope_t scope) {
		ast_extra_info info = sym.find_info(GPS_TAG_BB_USAGE);
		gps_syminfo syminfo;
		if (info == null) {
			syminfo = new gps_syminfo(is_scalar);
			sym.add_info(GPS_TAG_BB_USAGE, syminfo);
		} else {
			syminfo = (gps_syminfo) info;
		}

		syminfo.set_scope(scope);
	}

	public static void gm_gps_do_new_analysis_scope_sent_var(ast_procdef proc) {
		// find defined scope
		gm_gps_new_analysis_scope_sent_var_t T = new gm_gps_new_analysis_scope_sent_var_t();
		proc.traverse_both(T);
	}
}