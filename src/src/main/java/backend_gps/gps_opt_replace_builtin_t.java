package backend_gps;

import inc.GMEXPR_CLASS;
import inc.GMTYPE_T;
import tangible.RefObject;
import ast.AST_NODE_TYPE;
import ast.ast_expr;
import ast.ast_expr_builtin;
import ast.ast_id;
import ast.ast_sentblock;

import common.GlobalMembersGm_add_symbol;
import common.GlobalMembersGm_main;
import common.gm_apply;

import frontend.gm_symtab_entry;

public class gps_opt_replace_builtin_t extends gm_apply {
	public gps_opt_replace_builtin_t(gm_symtab_entry drv, ast_sentblock scope, java.util.HashMap<std.pair<ast_sentblock, Integer>, gm_symtab_entry> M) {
		this.map = new java.util.HashMap<std.pair<ast_sentblock, Integer>, gm_symtab_entry>(M);
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
				std.pair<ast_sentblock, Integer> P = new std.pair<ast_sentblock, Integer>(sb, b.get_builtin_def().get_method_id());
				gm_symtab_entry target = null;
				if (!map.containsKey(P)) {
					String temp_name = GlobalMembersGm_main.FE.voca_temp_name_and_add("tmp");
					GMTYPE_T type = b.get_builtin_def().get_result_type_summary();
					if (type.is_prim_type()) {
						target = GlobalMembersGm_add_symbol.gm_add_new_symbol_primtype(sb, type, new RefObject<String>(temp_name));
					} else if (type.is_nodeedge_type()) {
						target = GlobalMembersGm_add_symbol.gm_add_new_symbol_nodeedge_type(sb, type, sym.getType().get_target_graph_sym(), new RefObject<String>(temp_name));
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
				e.set_nodetype(AST_NODE_TYPE.AST_EXPR);
				e.set_expr_class(GMEXPR_CLASS.GMEXPR_ID);
				ast_id new_id = target.getId().copy(true);
				e.set_id(new_id);
			}
		}
		return true;
	}

	private gm_symtab_entry sym;
	private ast_sentblock sb;
	private java.util.HashMap<std.pair<ast_sentblock, Integer>, gm_symtab_entry> map;
}