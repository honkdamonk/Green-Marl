package backend_gps;

import inc.GMEXPR_CLASS;
import inc.GMTYPE_T;

import java.util.HashMap;
import java.util.Map;

import tangible.Pair;
import ast.ast_node_type;
import ast.ast_expr;
import ast.ast_expr_builtin;
import ast.ast_id;
import ast.ast_sentblock;

import common.gm_add_symbol;
import common.gm_main;
import common.gm_apply;
import common.gm_method_id_t;

import frontend.gm_symtab_entry;

public class gps_opt_replace_builtin_t extends gm_apply {
	
	private gm_symtab_entry sym;
	private ast_sentblock sb;
	private Map<Pair<ast_sentblock, gm_method_id_t>, gm_symtab_entry> map;
	
	public gps_opt_replace_builtin_t(gm_symtab_entry drv, ast_sentblock scope, HashMap<Pair<ast_sentblock, gm_method_id_t>, gm_symtab_entry> M) {
		map = new HashMap<Pair<ast_sentblock, gm_method_id_t>, gm_symtab_entry>(M);
		set_for_expr(true);
		sym = drv;
		sb = scope;
	}

	@Override
	public boolean apply(ast_expr e) {
		if (e.is_builtin()) {
			ast_expr_builtin b = (ast_expr_builtin) e;
			if (b.get_driver().getSymInfo() == sym) {
				assert b.get_args().size() == 0;
				// later for arguments

				// see if this has been defined
				Pair<ast_sentblock, gm_method_id_t> P = new Pair<ast_sentblock, gm_method_id_t>(sb, b.get_builtin_def().get_method_id());
				gm_symtab_entry target = null;
				if (!map.containsKey(P)) {
					String temp_name = gm_main.FE.voca_temp_name_and_add("tmp");
					GMTYPE_T type = b.get_builtin_def().get_result_type_summary();
					if (type.is_prim_type()) {
						target = gm_add_symbol.gm_add_new_symbol_primtype(sb, type, temp_name);
					} else if (type.is_nodeedge_type()) {
						target = gm_add_symbol.gm_add_new_symbol_nodeedge_type(sb, type, sym.getType().get_target_graph_sym(), temp_name);
					} else {
						assert false;
					}
					map.put(P, target);
					temp_name = null;
				} else {
					target = map.get(P);
				}
				assert target != null;

				// tricky.
				// change this call into ID
				e.set_nodetype(ast_node_type.AST_EXPR);
				e.set_expr_class(GMEXPR_CLASS.GMEXPR_ID);
				ast_id new_id = target.getId().copy(true);
				e.set_id(new_id);
			}
		}
		return true;
	}
}