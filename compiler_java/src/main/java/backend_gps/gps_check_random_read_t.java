package backend_gps;

import static backend_gps.GPSConstants.GPS_INT_EXPR_SCOPE;
import frontend.gm_symtab_entry;
import inc.gm_expr_class;
import ast.ast_expr;
import ast.ast_expr_builtin;

import common.gm_apply;
import common.gm_error;
import common.gm_errors_and_warnings;

//-----------------------------------------------------------------
// Check random access
//-----------------------------------------------------------------

public class gps_check_random_read_t extends gm_apply {

	private boolean _error = false;

	public gps_check_random_read_t() {
		set_for_expr(true);
	}

	public final boolean is_error() {
		return _error;
	}

	public final boolean apply(ast_expr f) {
		// random read always happens by field or builtin
		if ((f.get_opclass() == gm_expr_class.GMEXPR_FIELD) || (f.get_opclass() == gm_expr_class.GMEXPR_BUILTIN)) {
			if (f.find_info_int(GPS_INT_EXPR_SCOPE) == gm_gps_new_scope_analysis.GPS_NEW_SCOPE_RANDOM.getValue()) {
				gm_symtab_entry driver = (f.get_opclass() == gm_expr_class.GMEXPR_FIELD) ? f.get_field().get_first().getSymInfo() : ((ast_expr_builtin) f)
						.get_driver().getSymInfo();

				if (driver.getType().is_graph())
					return true;

				// Random Read
				if ((f.get_opclass() == gm_expr_class.GMEXPR_FIELD))
					System.out.printf("%s.%s\n", f.get_field().get_first().get_genname(), f.get_field().get_second().get_genname());
				else
					System.out.printf("%s->..()\n", ((ast_expr_builtin) f).get_driver().get_genname());
				gm_error.gm_backend_error(gm_errors_and_warnings.GM_ERROR_GPS_RANDOM_NODE_READ, f.get_line(), f.get_col(), "");
				_error = true;
			}
		}
		return true;
	}

}