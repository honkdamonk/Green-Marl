package backend_gps;

import java.util.HashMap;

import tangible.Pair;

import common.gm_method_id_t;

import frontend.gm_symtab_entry;
import inc.gm_compile_step;
import ast.ast_procdef;
import ast.ast_sent;
import ast.ast_sentblock;

public class gm_gps_opt_simplify_expr1 extends gm_compile_step {
	private gm_gps_opt_simplify_expr1() {
		set_description("Seperating builtin-calls from outer-loop driver");
	}

	public void process(ast_procdef p) {
		gps_opt_simplify_outer_builtin_t T = new gps_opt_simplify_outer_builtin_t();
		p.traverse_both(T);
		T.post_process();
	}

	@Override
	public gm_compile_step get_instance() {
		return new gm_gps_opt_simplify_expr1();
	}

	public static gm_compile_step get_factory() {
		return new gm_gps_opt_simplify_expr1();
	}

	/**
	 * ---------------------------------------------------- Foreach(n:G.Nodes)
	 * Foreach(t:n.Nbrs) { LHS = .. n..Builtin() + ... + ... } ==>
	 * Foreach(n:G.Nodes) Foreach(t:n.Nbrs) { TEMP = n.Builtin(); LHS = ... TEMP
	 * + ... + ... } ----------------------------------------------------
	 */
	public static boolean contains_built_in_through_driver(ast_sent s, gm_symtab_entry e) {
		gps_opt_check_contain_builtin_through_t T = new gps_opt_check_contain_builtin_through_t(e);
		s.traverse_pre(T);

		return T.has_it();
	}

	public static void replace_built_in(ast_sent s, gm_symtab_entry e, ast_sentblock scope,
			HashMap<Pair<ast_sentblock, gm_method_id_t>, gm_symtab_entry> already_defined_map) {
		gps_opt_replace_builtin_t T = new gps_opt_replace_builtin_t(e, scope, already_defined_map);
		s.traverse_post(T);

	}
}