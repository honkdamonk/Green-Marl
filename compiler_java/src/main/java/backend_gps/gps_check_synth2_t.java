package backend_gps;

import ast.ast_expr;
import ast.ast_expr_builtin;
import ast.ast_node_type;

import common.gm_apply;
import common.gm_builtin_def;
import common.gm_error;
import common.gm_errors_and_warnings;

public class gps_check_synth2_t extends gm_apply {

	private boolean _error = false;
	private boolean _rand_used = false;

	public gps_check_synth2_t() {
		set_for_expr(true);
	}

	public final boolean is_error() {
		return _error;
	}

	public final boolean is_rand_used() {
		return _rand_used;
	}

	@Override
	public boolean apply(ast_expr e) {
		if (e.get_nodetype() == ast_node_type.AST_EXPR_BUILTIN) {
			ast_expr_builtin be = (ast_expr_builtin) e;
			gm_builtin_def def = be.get_builtin_def();
			switch (def.get_method_id()) {
			case GM_BLTIN_TOP_DRAND: // rand function
			case GM_BLTIN_TOP_IRAND: // rand function
			case GM_BLTIN_GRAPH_RAND_NODE:
				_rand_used = true;
				break;

			/*
			 * case GM_BLTIN_GRAPH_NUM_EDGES:
			 */
			case GM_BLTIN_GRAPH_NUM_NODES:
			case GM_BLTIN_NODE_DEGREE:
			case GM_BLTIN_NODE_IN_DEGREE:
			case GM_BLTIN_NODE_TO_EDGE:
				break;

			case GM_BLTIN_TOP_LOG: // log function
			case GM_BLTIN_TOP_EXP: // exp function
			case GM_BLTIN_TOP_POW: // pow function
				break;
			default:
				gm_error.gm_backend_error(gm_errors_and_warnings.GM_ERROR_GPS_UNSUPPORTED_OP, e.get_line(), e.get_col(), "Builtin (function)");
				_error = true;
				break;
			}
		}
		return true;
	}

}