package backend_gps;

import static backend_gps.GPSConstants.GPS_FLAG_IS_EDGE_ITERATOR;
import static backend_gps.GPSConstants.GPS_FLAG_IS_INNER_LOOP;
import static backend_gps.GPSConstants.GPS_FLAG_IS_OUTER_LOOP;
import static backend_gps.GPSConstants.GPS_INT_EXPR_SCOPE;
import static backend_gps.GPSConstants.GPS_INT_SYMBOL_SCOPE;

import java.util.LinkedList;

import ast.ast_expr;
import ast.ast_expr_builtin;
import ast.ast_id;

import common.gm_apply;

import frontend.gm_symtab_entry;

//---------------------------------------------------------------------
// Find scope of each expression
//---------------------------------------------------------------------
public class gm_gps_new_analysis_rhs_lhs_t extends gm_apply {
	
	public gm_gps_new_analysis_rhs_lhs_t() {
		set_for_expr(true);
	}

	public final boolean apply(ast_expr e) {
		int t;
		int l;
		int r;
		gm_gps_new_scope_analysis scope;

		switch (e.get_opclass()) {

		case GMEXPR_IVAL:
		case GMEXPR_FVAL:
		case GMEXPR_BVAL:
		case GMEXPR_INF:
		case GMEXPR_NIL:
			e.add_info_int(GPS_INT_EXPR_SCOPE, gm_gps_new_scope_analysis.GPS_NEW_SCOPE_GLOBAL.getValue());
			break;
		case GMEXPR_ID:
			scope = get_scope_from_id(e.get_id().getSymInfo());
			e.add_info_int(GPS_INT_EXPR_SCOPE, scope.getValue());
			break;

		case GMEXPR_FIELD:
			scope = get_scope_from_driver(e.get_field().get_first().getSymInfo());
			e.add_info_int(GPS_INT_EXPR_SCOPE, scope.getValue());
			break;

		case GMEXPR_UOP:
		case GMEXPR_LUOP:
			e.add_info_int(GPS_INT_EXPR_SCOPE, e.get_left_op().find_info_int(GPS_INT_EXPR_SCOPE));
			break;

		case GMEXPR_BIOP:
		case GMEXPR_LBIOP:
		case GMEXPR_COMP:
			l = e.get_left_op().find_info_int(GPS_INT_EXPR_SCOPE);
			r = e.get_right_op().find_info_int(GPS_INT_EXPR_SCOPE);
			gm_gps_new_scope_analysis lx = gm_gps_new_scope_analysis.forValue(l);
			gm_gps_new_scope_analysis rx = gm_gps_new_scope_analysis.forValue(r);
			e.add_info_int(GPS_INT_EXPR_SCOPE, gm_gps_new_scope_analysis.get_more_restricted_scope(lx, rx).getValue());
			break;

		case GMEXPR_TER:
			l = e.get_left_op().find_info_int(GPS_INT_EXPR_SCOPE);
			r = e.get_right_op().find_info_int(GPS_INT_EXPR_SCOPE);
			t = e.get_cond_op().find_info_int(GPS_INT_EXPR_SCOPE);
			gm_gps_new_scope_analysis lx2 = gm_gps_new_scope_analysis.forValue(l);
			gm_gps_new_scope_analysis rx2 = gm_gps_new_scope_analysis.forValue(r);
			gm_gps_new_scope_analysis tx = gm_gps_new_scope_analysis.forValue(t);
			e.add_info_int(GPS_INT_EXPR_SCOPE,
					gm_gps_new_scope_analysis.get_more_restricted_scope(tx, gm_gps_new_scope_analysis.get_more_restricted_scope(lx2, rx2)).getValue());
			break;

		case GMEXPR_BUILTIN: {
			ast_expr_builtin b = (ast_expr_builtin) e;
			ast_id i = b.get_driver();
			if (i == null) {
				e.add_info_int(GPS_INT_EXPR_SCOPE, gm_gps_new_scope_analysis.GPS_NEW_SCOPE_GLOBAL.getValue());
				break;
			}

			// scope from driver
			gm_gps_new_scope_analysis t2 = get_scope_from_driver(i.getSymInfo());

			// scope of arguments
			LinkedList<ast_expr> L = b.get_args();
			for (ast_expr ee : L) {
				t2 = gm_gps_new_scope_analysis.get_more_restricted_scope(t2, gm_gps_new_scope_analysis.forValue(ee.find_info_int(GPS_INT_EXPR_SCOPE)));
			}

			e.add_info_int(GPS_INT_EXPR_SCOPE, t2.getValue());
		}
			break;

		case GMEXPR_REDUCE:
		case GMEXPR_FOREIGN:
		default:
			assert false;
			break;
		}
		return true;
	}

	public final gm_gps_new_scope_analysis get_scope_from_id(gm_symtab_entry e) {
		return gm_gps_new_scope_analysis.forValue(e.find_info_int(GPS_INT_SYMBOL_SCOPE));
	}

	public final gm_gps_new_scope_analysis get_scope_from_driver(gm_symtab_entry e) {
		return get_scope_from_driver(e, false);
	}

	public final gm_gps_new_scope_analysis get_scope_from_driver(gm_symtab_entry e, boolean is_rarrow) {
		if (e.find_info_bool(GPS_FLAG_IS_INNER_LOOP))
			return gm_gps_new_scope_analysis.GPS_NEW_SCOPE_IN;
		else if (e.find_info_bool(GPS_FLAG_IS_OUTER_LOOP))
			return gm_gps_new_scope_analysis.GPS_NEW_SCOPE_OUT;
		else if (e.find_info_bool(GPS_FLAG_IS_EDGE_ITERATOR))
			return gm_gps_new_scope_analysis.GPS_NEW_SCOPE_EDGE;
		else
			return gm_gps_new_scope_analysis.GPS_NEW_SCOPE_RANDOM;
	}

}