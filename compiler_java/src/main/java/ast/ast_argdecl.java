package ast;

import common.GlobalMembersGm_dumptree;

public class ast_argdecl extends ast_node {
	
	public void dispose() {
		if (idlist != null)
			idlist.dispose();
		if (type != null)
			type.dispose();
	}

	private ast_argdecl() {
		super(AST_NODE_TYPE.AST_ARGDECL);
		this.tc_finished = false;
		this.idlist = null;
		this.type = null;
	}

	public static ast_argdecl new_argdecl(ast_idlist id, ast_typedecl type) {
		ast_argdecl d = new ast_argdecl();
		d.idlist = id;
		d.type = type;
		id.set_parent(d);
		type.set_parent(d);
		return d;
	}

	public void reproduce(int ind_level) {
		idlist.reproduce(0);
		Out.push(" : ");
		type.reproduce(0);
	}

	public void dump_tree(int ind_level) {
		assert parent != null;
		GlobalMembersGm_dumptree.IND(ind_level);
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

	private ast_idlist idlist;
	private ast_typedecl type;
	private boolean tc_finished; // is typecheck finished?
}