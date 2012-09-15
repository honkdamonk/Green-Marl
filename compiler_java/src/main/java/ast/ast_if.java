package ast;

import common.gm_apply;
import common.gm_dumptree;

public class ast_if extends ast_sent {

	private ast_sent then_part = null;
	private ast_sent else_part = null;
	private ast_expr cond = null;

	private ast_if() {
		super(ast_node_type.AST_IF);
	}

	public static ast_if new_if(ast_expr c, ast_sent t, ast_sent e) {
		ast_if ifs = new ast_if();
		ifs.then_part = t;
		ifs.else_part = e;
		ifs.cond = c;
		c.set_parent(ifs);
		t.set_parent(ifs);
		if (e != null)
			e.set_parent(ifs);
		return ifs;
	}

	@Override
	public void reproduce(int ind_level) {
		Out.push("If (");
		cond.reproduce(0);
		Out.pushln(")");

		if (then_part.get_nodetype() == ast_node_type.AST_SENTBLOCK) {
			then_part.reproduce(ind_level);
		} else {
			Out.pushIndent();
			then_part.reproduce(ind_level + 1);
			Out.popIndent();
		}

		if (else_part != null) {
			Out.pushln("Else");

			if (then_part.get_nodetype() == ast_node_type.AST_SENTBLOCK) {
				else_part.reproduce(ind_level);
			} else {
				Out.pushIndent();
				else_part.reproduce(ind_level + 1);
				Out.popIndent();
			}
		}
	}

	@Override
	public void dump_tree(int ind_level) {
		gm_dumptree.IND(ind_level);
		assert parent != null;
		System.out.print("[IF ");
		cond.dump_tree(0);
		System.out.print(" THEN:\n ");
		then_part.dump_tree(ind_level + 1);
		if (else_part != null) {
			System.out.print(" ELSE:\n ");
			else_part.dump_tree(ind_level + 1);
		}
		System.out.print("\n");
		gm_dumptree.IND(ind_level);
		System.out.print("]");
	}

	@Override
	public void traverse_sent(gm_apply a, boolean is_post, boolean is_pre) {
		// traverse only
		get_cond().traverse(a, is_post, is_pre);
		if (!a.is_traverse_local_expr_only()) {
			get_then().traverse(a, is_post, is_pre);
			if (get_else() != null) {
				get_else().traverse(a, is_post, is_pre);
			}
		}
	}

	public final ast_sent get_then() {
		return then_part;
	}

	public final ast_sent get_else() {
		return else_part;
	}

	public final ast_expr get_cond() {
		return cond;
	}

	public final void set_then(ast_sent s) {
		if (s != null)
			s.set_parent(this);
		then_part = s;
	}

	public final void set_else(ast_sent s) {
		if (s != null)
			s.set_parent(this);
		else_part = s;
	}

	public final void set_cond(ast_expr c) {
		cond = c;
		if (c != null)
			c.set_parent(this);
	}

}