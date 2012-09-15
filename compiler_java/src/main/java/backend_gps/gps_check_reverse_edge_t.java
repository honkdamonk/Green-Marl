package backend_gps;

import ast.ast_node_type;
import ast.ast_expr;
import ast.ast_expr_builtin;
import ast.ast_foreach;
import ast.ast_sent;

import common.gm_apply;
import common.gm_method_id;

import frontend.gm_symtab_entry;

public class gps_check_reverse_edge_t extends gm_apply {
	
	private boolean r_edge = false;
	private boolean r_degree = false;
	private gm_symtab_entry target_graph = null;
	
	public gps_check_reverse_edge_t() {
		set_for_sent(true);
		set_for_expr(true);
	}

	@Override
	public final boolean apply(ast_sent s) {
		if (s.get_nodetype() == ast_node_type.AST_FOREACH) {
			ast_foreach fe = (ast_foreach) s;
			if (fe.get_iter_type().is_iteration_use_reverse()) {
				target_graph = fe.get_iterator().getTypeInfo().get_target_graph_sym();
				r_edge = true;
			}
		}
		return true;
	}

	@Override
	public final boolean apply(ast_expr e) {
		if (e.is_builtin()) {
			ast_expr_builtin b = (ast_expr_builtin) e;
			if (b.get_builtin_def().get_method_id() == gm_method_id.GM_BLTIN_NODE_IN_DEGREE) {
				target_graph = b.get_driver().getTypeInfo().get_target_graph_sym();
				r_degree = true;
			}
		}
		return true;
	}

	public final boolean use_in_degree() {
		return r_degree;
	}

	public final boolean use_rev_edge() {
		return r_edge;
	}

	public final gm_symtab_entry get_target_graph() {
		return target_graph;
	}

}