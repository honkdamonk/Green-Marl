package backend_gps;
import static inc.gps_apply_bb.GPS_TAG_BB_USAGE;
import ast.ast_extra_info;
import ast.ast_procdef;
import frontend.gm_symtab_entry;

public class GlobalMembersGm_gps_new_analysis_scope_sent_var {
	// -----------------------------------------------------------------------------------------------------------------------
	// < Step 1>
	// * Check the scope of each variable: GLOBAL, OUT, IN
	// * Check the context of each statement: GLOBAL, OUT, IN
	//
	// Int x; // global scope
	// Int x2;
	//
	// Foreach(s: G.Nodes) { // outer loop
	// Int y;
	// Int y2;
	//
	// If (s.A + x > 0) // (s.A + x > 0) ==> EXPR_OUT
	// x2 += s.B; // assignment: PREFIX_COND_OUT, lhs: GLOBAL, rhs: OUT,
	//
	// Foreach(t: s.Nbrs) { // inner loop
	// Int z;
	// Int z2;
	// }
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