package backend_gps;

import ast.ast_expr;
import ast.ast_expr_builtin;

import common.gm_builtin_def;
import common.gm_expr_replacement_t;
import common.gm_method_id;

import frontend.gm_symtab_entry;

public class gm_gps_opt_replace_from_to_builtin_t extends gm_expr_replacement_t {
	
	private gm_symtab_entry out;
	private gm_symtab_entry in;
	private gm_symtab_entry edge;
	
	public gm_gps_opt_replace_from_to_builtin_t(gm_symtab_entry old_edge, gm_symtab_entry outer, gm_symtab_entry inner) {
		out = outer;
		in = inner;
		edge = old_edge;
	}

	@Override
	public boolean is_target(ast_expr e) {
		if (e.is_builtin()) {
			ast_expr_builtin b = (ast_expr_builtin) e;
			if (b.get_driver() == null)
				return false;
			if (b.get_driver().getSymInfo() != edge)
				return false;

			gm_builtin_def D = b.get_builtin_def();
			if ((D.get_method_id() == gm_method_id.GM_BLTIN_EDGE_FROM) || (D.get_method_id() == gm_method_id.GM_BLTIN_EDGE_TO)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public ast_expr create_new_expr(ast_expr target, tangible.RefObject<Boolean> destroy_target_after) {
		assert target.is_builtin();
		ast_expr_builtin b = (ast_expr_builtin) target;
		ast_expr new_expr = null;
		gm_builtin_def D = b.get_builtin_def();
		if (D.get_method_id() == gm_method_id.GM_BLTIN_EDGE_FROM) {
			new_expr = ast_expr.new_id_expr(out.getId().copy(true));
		} else if (D.get_method_id() == gm_method_id.GM_BLTIN_EDGE_TO) {
			new_expr = ast_expr.new_id_expr(in.getId().copy(true));
		} else {
			assert false;
		}

		destroy_target_after.argvalue = true;
		return new_expr;
	}

}