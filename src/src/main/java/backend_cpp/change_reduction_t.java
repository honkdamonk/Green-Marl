package backend_cpp;

import ast.AST_NODE_TYPE;
import ast.ast_assign;
import ast.ast_id;
import ast.ast_node;
import ast.ast_sent;

import common.GlobalMembersGm_transform_helper;
import common.gm_apply;

import frontend.GlobalMembersGm_fixup_bound_symbol;
import frontend.gm_symtab_entry;

public class change_reduction_t extends gm_apply {
	public final void set_map(java.util.HashMap<gm_symtab_entry, gm_symtab_entry> m) {
		symbol_map = m;
	}

	@Override
	public boolean apply(ast_sent s) {
		if (s.get_nodetype() != AST_NODE_TYPE.AST_ASSIGN)
			return true;
		ast_assign a = (ast_assign) s;
		if (!a.is_reduce_assign())
			return true;
		if (!a.is_target_scalar())
			return true;

		ast_id lhs = a.get_lhs_scala();

		java.util.Iterator<gm_symtab_entry, gm_symtab_entry> I;
		// C++ TO JAVA CONVERTER WARNING: The following line was determined to
		// be a copy assignment (rather than a reference assignment) - this
		// should be verified and a 'copyFrom' method should be created if it
		// does not yet exist:
		// ORIGINAL LINE: I = symbol_map->find(lhs->getSymInfo());
		I.copyFrom(symbol_map.find(lhs.getSymInfo()));
		if (I == symbol_map.end()) // not target
			return true;

		gm_symtab_entry new_target = I.next().getValue();

		// change lhs symbol
		lhs.setSymInfo(new_target);
		if (a.is_argminmax_assign()) {
			java.util.LinkedList<ast_node> L_old = a.get_lhs_list();
			for (ast_node n : L_old) {
				assert n.get_nodetype() == AST_NODE_TYPE.AST_ID;
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
			GlobalMembersGm_transform_helper.gm_make_it_belong_to_sentblock(a);
			GlobalMembersGm_fixup_bound_symbol.gm_make_normal_assign(a);
		}
	}

	private java.util.HashMap<gm_symtab_entry, gm_symtab_entry> symbol_map;
	private java.util.LinkedList<ast_assign> to_normals = new java.util.LinkedList<ast_assign>();
}
// supplimental lhs for argmin/argmax - new symbols - old symbols

// bool gm_cpp_gen::optimize_reduction(ast_procdef *p)
