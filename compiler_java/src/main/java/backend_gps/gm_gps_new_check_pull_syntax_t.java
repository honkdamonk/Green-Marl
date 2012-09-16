package backend_gps;

import static backend_gps.GPSConstants.GPS_FLAG_IS_EDGE_ITERATOR;
import static backend_gps.GPSConstants.GPS_FLAG_IS_INNER_LOOP;
import static backend_gps.GPSConstants.GPS_FLAG_IS_OUTER_LOOP;
import static backend_gps.GPSConstants.GPS_INT_SYMBOL_SCOPE;
import static backend_gps.GPSConstants.GPS_INT_SYNTAX_CONTEXT;
import ast.ast_assign;
import ast.ast_field;
import ast.ast_id;
import ast.ast_node;
import ast.ast_node_type;
import ast.ast_sent;

import common.gm_apply;
import common.gm_error;
import common.gm_errors_and_warnings;

import frontend.gm_symtab_entry;

//------------------------------------------------------------------------
//   Check if there is 'pulling' syntax
//          Foreach (s: G.Nodes)
//            Foreach(t: s.Nbrs)
//                s.A = .... ; // error PULL  
//------------------------------------------------------------------------
public class gm_gps_new_check_pull_syntax_t extends gm_apply {

	private boolean _error = false;

	public gm_gps_new_check_pull_syntax_t() {
		set_for_sent(true);
	}

	// write to OUT_SCOPE in INNER_LOOP is an error
	@Override
	public final boolean apply(ast_sent s) {
		if (s.get_nodetype() == ast_node_type.AST_ASSIGN) {
			ast_assign a = (ast_assign) s;
			gm_gps_new_scope_analysis context = (gm_gps_new_scope_analysis) s.find_info_obj(GPS_INT_SYNTAX_CONTEXT);
			gm_gps_new_scope_analysis scope;
			if (a.is_target_scalar()) {
				scope = get_scope_from_id(a.get_lhs_scala().getSymInfo());
			} else {
				scope = get_scope_from_driver(a.get_lhs_field().get_first().getSymInfo());
			}

			if (a.has_lhs_list()) {
				for (ast_node n : a.get_lhs_list()) {
					gm_gps_new_scope_analysis scope2;
					if (n.get_nodetype() == ast_node_type.AST_ID) {
						scope2 = get_scope_from_id(((ast_id) n).getSymInfo());
					} else {
						scope2 = get_scope_from_driver(((ast_field) n).get_first().getSymInfo());
					}

					assert scope == scope2;
				}
			}

			// writing to out-scope inside inner-loop.
			if ((context == gm_gps_new_scope_analysis.GPS_NEW_SCOPE_IN) && (scope == gm_gps_new_scope_analysis.GPS_NEW_SCOPE_OUT)) {
				gm_error.gm_backend_error(gm_errors_and_warnings.GM_ERROR_GPS_PULL_SYNTAX, s.get_line(), s.get_col());
				_error = true;
			}
		} else if (s.get_nodetype() == ast_node_type.AST_CALL) {
			assert false;
		} else if (s.get_nodetype() == ast_node_type.AST_FOREIGN) {
			assert false;
			// should check out-scope is modified
		}
		return true;
	}

	public final boolean is_error() {
		return _error;
	}

	private gm_gps_new_scope_analysis get_scope_from_id(gm_symtab_entry e) {
		return (gm_gps_new_scope_analysis) e.find_info_obj(GPS_INT_SYMBOL_SCOPE);
	}

	private gm_gps_new_scope_analysis get_scope_from_driver(gm_symtab_entry e) {
		return get_scope_from_driver(e, false);
	}

	private gm_gps_new_scope_analysis get_scope_from_driver(gm_symtab_entry e, boolean is_rarrow) {
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