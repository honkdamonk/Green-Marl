package backend_gps;

import java.util.HashMap;

import ast.ast_foreach;
import ast.ast_node;

public class BackendGpsGlobal {

	public static void gm_gps_find_double_nested_loops(ast_node p, HashMap<ast_foreach, ast_foreach> MAP) {
		gps_opt_find_nested_loops_t T = new gps_opt_find_nested_loops_t(MAP);
		p.traverse_both(T);
	}

}