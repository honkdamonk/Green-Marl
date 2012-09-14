package ast;

import common.gm_dumptree;

class ast_do_while extends ast_while {

	protected ast_do_while(ast_expr c, ast_sentblock s) {
		super(c, s);
	}

	@Override
	public boolean is_do_while() {
		return true;
	}

	@Override
	public void reproduce(int ind_level) {
		Out.push("Do ");
		body.reproduce(ind_level); // body is always sentence block
		Out.push("While (");
		cond.reproduce(0);
		Out.pushln(" );");
	}

	@Override
	public void dump_tree(int ind_level) {
		gm_dumptree.IND(ind_level);
		assert parent != null;
		System.out.print("[DO BODY: \n");
		body.dump_tree(ind_level + 1);
		System.out.print(" WHILE ");
		cond.dump_tree(0);
		System.out.print("\n");
		gm_dumptree.IND(ind_level);
		System.out.print("]");
	}

}
