package ast;

import common.gm_dumptree;

public class ast_argdecl extends ast_node {

	private ast_idlist idlist = null;
	private ast_typedecl type = null;
	private boolean tc_finished = false; // is typecheck finished?

	private ast_argdecl() {
		super(ast_node_type.AST_ARGDECL);
	}

	public static ast_argdecl new_argdecl(ast_idlist id, ast_typedecl type) {
		ast_argdecl d = new ast_argdecl();
		d.idlist = id;
		d.type = type;
		id.set_parent(d);
		type.set_parent(d);
		return d;
	}

	@Override
	public void reproduce(int ind_level) {
		idlist.reproduce(0);
		Out.push(" : ");
		type.reproduce(0);
	}

	@Override
	public void dump_tree(int ind_level) {
		assert parent != null;
		gm_dumptree.IND(ind_level);
		System.out.print("[");
		idlist.dump_tree(0);
		System.out.print(" : \n");
		type.dump_tree(ind_level + 1);
		System.out.print("]");
	}

	public final ast_typedecl get_type() {
		if (!tc_finished) {
			return type; // obtain type from syntax
		} else {
			return idlist.get_item(0).getTypeInfo(); // obtain type from symbol
														// table
		}
	}

	public final ast_idlist get_idlist() {
		return idlist;
	}

}