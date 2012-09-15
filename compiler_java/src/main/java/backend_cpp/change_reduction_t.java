package backend_cpp;

import java.util.HashMap;
import java.util.LinkedList;

import ast.ast_node_type;
import ast.ast_assign;
import ast.ast_id;
import ast.ast_node;
import ast.ast_sent;

import common.gm_apply;
import common.gm_transform_helper;

import frontend.FrontendGlobal;
import frontend.gm_symtab_entry;

public class change_reduction_t extends gm_apply {
	
	private HashMap<gm_symtab_entry, gm_symtab_entry> symbol_map;
	private LinkedList<ast_assign> to_normals = new LinkedList<ast_assign>();
	
	public final void set_map(HashMap<gm_symtab_entry, gm_symtab_entry> m) {
		symbol_map = m;
	}

	@Override
	public boolean apply(ast_sent s) {
		if (s.get_nodetype() != ast_node_type.AST_ASSIGN)
			return true;
		ast_assign a = (ast_assign) s;
		if (!a.is_reduce_assign())
			return true;
		if (!a.is_target_scalar())
			return true;

		ast_id lhs = a.get_lhs_scala();

		if (!symbol_map.containsKey(lhs.getSymInfo())) // not target
			return true;

		gm_symtab_entry new_target = symbol_map.get(lhs.getSymInfo());

		// change lhs symbol
		lhs.setSymInfo(new_target);
		if (a.is_argminmax_assign()) {
			LinkedList<ast_node> L_old = a.get_lhs_list();
			for (ast_node n : L_old) {
				assert n.get_nodetype() == ast_node_type.AST_ID;
				ast_id id = (ast_id) n;
				gm_symtab_entry old_e = id.getSymInfo();
				gm_symtab_entry new_e = symbol_map.get(old_e);
				assert new_e != null;
				id.setSymInfo(new_e);
			}
		}

		// change to normal write
		to_normals.addLast(a);

		return true;
	}

	public final void post_process() {
		for (ast_assign a : to_normals) {
			gm_transform_helper.gm_make_it_belong_to_sentblock(a);
			FrontendGlobal.gm_make_normal_assign(a);
		}
	}

}
// supplimental lhs for argmin/argmax - new symbols - old symbols
