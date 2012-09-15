package ast;

import static ast.ast_node_type.AST_WHILE;

import common.gm_apply;
import common.gm_dumptree;

public class ast_while extends ast_sent {

	protected ast_sentblock body = null;
	protected ast_expr cond = null;

	protected ast_while(ast_expr c, ast_sentblock s) {
		super(AST_WHILE);
		cond = c;
		body = s;
		c.set_parent(this);
		s.set_parent(this);
	}

	public static ast_while new_while(ast_expr c, ast_sentblock s) {
		return new ast_while(c, s);
	}

	public static ast_while new_do_while(ast_expr c, ast_sentblock s) {
		return new ast_do_while(c, s);
	}

	@Override
	public void reproduce(int ind_level) {
		Out.push("While (");
		cond.reproduce(0);
		Out.pushln(" )");
		body.reproduce(ind_level); // body is always sentence blco
	}

	@Override
	public void dump_tree(int ind_level) {
		gm_dumptree.IND(ind_level);
		assert parent != null;
		System.out.print("[WHILE ");
		cond.dump_tree(0);
		System.out.print("  BODY: \n");
		body.dump_tree(ind_level + 1);
		System.out.print("\n");
		gm_dumptree.IND(ind_level);
		System.out.print("]");
	}

	@Override
	public void traverse_sent(gm_apply a, boolean is_post, boolean is_pre) {
		// traverse only
		get_cond().traverse(a, is_post, is_pre);
		if (!a.is_traverse_local_expr_only()) {
			get_body().traverse(a, is_post, is_pre);
		}
	}

	public final ast_sent get_body() {
		return body;
	}

	public final ast_expr get_cond() {
		return cond;
	}

	public boolean is_do_while() {
		return true;
	}

	public final void set_body(ast_sentblock s) {
		body = s;
	}

	public final void set_cond(ast_expr e) {
		cond = e;
		if (e != null) {
			e.set_parent(this);
		}
	}

}