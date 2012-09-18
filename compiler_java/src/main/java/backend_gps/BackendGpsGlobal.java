package backend_gps;

import java.util.Map;

import ast.ast_foreach;
import ast.ast_node;

class BackendGpsGlobal {

	static void gm_gps_find_double_nested_loops(ast_node p, Map<ast_foreach, ast_foreach> MAP) {
		gps_opt_find_nested_loops_t T = new gps_opt_find_nested_loops_t(MAP);
		p.traverse_both(T);
	}

}