package ast;

import static ast.AST_NODE_TYPE.AST_VARDECL;

import common.gm_apply;
import common.gm_dumptree;
import common.gm_traverse;

public class ast_vardecl extends ast_sent {

	private ast_idlist idlist = null;
	private ast_typedecl type = null;
	private ast_expr init_expr = null; // for syntax sugar.
	private boolean tc_finished = false;

	private ast_vardecl() {
		super(AST_VARDECL);
	}

	public final void set_typechecked(boolean b) {
		tc_finished = b;
	}

	public static ast_vardecl new_vardecl(ast_typedecl type, ast_idlist id) {
		ast_vardecl d = new ast_vardecl();
		d.idlist = id;
		d.type = type;
		id.set_parent(d);
		type.set_parent(d);
		return d;
	}

	public static ast_vardecl new_vardecl(ast_typedecl type, ast_id id) {
		ast_vardecl d = new ast_vardecl();
		ast_idlist idl = new ast_idlist();
		idl.add_id(id);
		d.idlist = idl;
		d.type = type;
		idl.set_parent(d);
		type.set_parent(d);
		return d;
	}

	public static ast_vardecl new_vardecl_init(ast_typedecl type, ast_id id, ast_expr init) {
		ast_vardecl d = new ast_vardecl();
		ast_idlist idl = new ast_idlist();
		idl.add_id(id);
		d.idlist = idl;
		d.type = type;
		d.init_expr = init;
		id.set_parent(d);
		type.set_parent(d);
		if (init != null)
			init.set_parent(d);
		id.set_instant_assigned(check_instant_assignment(type, init));
		return d;
	}

	@Override
	public void traverse_sent(gm_apply a, boolean is_post, boolean is_pre) {
		boolean for_id = a.is_for_id();
		if (for_id) {
			ast_idlist idl = get_idlist();

			if (is_pre)
				idl.apply_id(a, gm_traverse.PRE_APPLY);

			if (is_post)
				idl.apply_id(a, gm_traverse.POST_APPLY);
		}
	}

	@Override
	public void reproduce(int ind_level) {
		type.reproduce(0);
		Out.SPC();
		idlist.reproduce(0);
		Out.pushln(";");
	}

	@Override
	public void dump_tree(int ind_level) {
		gm_dumptree.IND(ind_level);
		assert parent != null;
		System.out.print("[DECL ");
		System.out.print("\n");
		type.dump_tree(ind_level + 1);
		System.out.print("\n");
		idlist.dump_tree(ind_level + 1);

		System.out.print("\n");
		gm_dumptree.IND(ind_level);
		System.out.print("]");
	}

	public final ast_idlist get_idlist() {
		return idlist;
	}

	public final ast_typedecl get_type() {
		if (!tc_finished)
			return type; // obtain type from syntax
		else
			return idlist.get_item(0).getTypeInfo(); // obtain type from symbol
														// table
	}

	public final ast_expr get_init() {
		return init_expr;
	}

	public final void set_init(ast_expr v) {
		init_expr = v;
		if (v != null)
			v.set_parent(this);
	}

	public final boolean is_tc_finished() {
		return tc_finished;
	}

	public final void set_tc_finished(boolean b) {
		tc_finished = b;
	} // who calls it?

	private static boolean check_instant_assignment(ast_typedecl type, ast_expr init) {

		if (init == null || type == null)
			return false;
		if (!type.is_collection())
			return false;
		if (!init.is_field())
			return false;
		return true;
	}
}