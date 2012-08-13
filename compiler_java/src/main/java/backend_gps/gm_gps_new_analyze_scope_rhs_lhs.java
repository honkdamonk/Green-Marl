package backend_gps;

import inc.gm_compile_step;
import ast.ast_procdef;

public class gm_gps_new_analyze_scope_rhs_lhs extends gm_compile_step {
	
	private gm_gps_new_analyze_scope_rhs_lhs() {
		set_description("Analyzing scope of rhs and lhs");
	}

	public void process(ast_procdef proc) {
		gm_gps_do_new_analysis_rhs_lhs(proc);
		return;
	}

	@Override
	public gm_compile_step get_instance() {
		return new gm_gps_new_analyze_scope_rhs_lhs();
	}

	public static gm_compile_step get_factory() {
		return new gm_gps_new_analyze_scope_rhs_lhs();
	}
	
	// -----------------------------------------------------------------------------------------------------------------------
	// Analysis the 'scope' of each RHS, and LHS
	// -----------------------------------------------------------------------------------------------------------------------
	// * Check the scope of each expression: GLOBAL, OUT, IN, RANDOM, EDGE
	// * Check the destination of each assignment: GLOBAL, OUT, IN, RANDOM, EDGE
	//
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

	public static void gm_gps_do_new_analysis_rhs_lhs(ast_procdef proc) {
		gm_gps_new_analysis_rhs_lhs_t T = new gm_gps_new_analysis_rhs_lhs_t();
		proc.traverse_post(T);
	}
}