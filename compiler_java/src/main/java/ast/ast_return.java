package ast;

import common.gm_apply;
import common.gm_dumptree;

public class ast_return extends ast_sent {

	protected ast_expr expr = null;

	protected ast_return() {
		super(ast_node_type.AST_RETURN);
	}

	public static ast_return new_return(ast_expr e) {
		ast_return R = new ast_return();
		R.expr = e;
		if (e != null)
			e.set_parent(R);
		return R;
	}

	public final ast_expr get_expr() {
		return expr;
	}

	public final void set_expr(ast_expr e) {
		expr = e;
		if (e != null)
			e.set_parent(this);
	}

	@Override
	public void reproduce(int ind_level) {
		Out.push("Return ");
		if (expr != null)
			expr.reproduce(0);
		Out.pushln(";");
	}

	@Override
	public void dump_tree(int ind_level) {
		gm_dumptree.IND(ind_level);
		System.out.print("[Return ");
		if (expr != null) {
			System.out.print("\n");
			expr.dump_tree(ind_level + 1);
			System.out.print("\n");
			gm_dumptree.IND(ind_level);
		}
		System.out.print("]");
	}

	@Override
	public void traverse_sent(gm_apply a, boolean is_post, boolean is_pre) {
		if (get_expr() != null)
			get_expr().traverse(a, is_post, is_pre);
	}
}