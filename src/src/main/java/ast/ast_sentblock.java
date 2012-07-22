package ast;

import common.GlobalMembersGm_dumptree;
import common.gm_apply;

public class ast_sentblock extends ast_sent {
	public void dispose() {
		// java.util.Iterator<ast_sent> it;
		// for (it = sents.iterator(); it.hasNext();)
		// {
		// it.next() = null;
		// }
		delete_symtabs();
	}

	public static ast_sentblock new_sentblock() {
		return new ast_sentblock();
	}

	public final void add_sent(ast_sent s) {
		sents.addLast(s);
		s.set_parent(this);
	}

	public void reproduce(int ind_level) {
		Out.pushln("{");
		for (ast_sent s : sents)
			s.reproduce(0);
		Out.pushln("}");
	}

	public void dump_tree(int ind_level) {
		assert parent != null;
		GlobalMembersGm_dumptree.IND(ind_level);
		System.out.print("[ \n");
		for (ast_sent s : sents) {
			s.dump_tree(ind_level + 1);
			System.out.print("\n");
		}
		GlobalMembersGm_dumptree.IND(ind_level);
		System.out.print("]");
	}

	public void traverse_sent(gm_apply a, boolean is_post, boolean is_pre) {
		// a->begin_context(this);
		if (a.is_traverse_local_expr_only())
			return;

		java.util.LinkedList<ast_sent> sents = get_sents();
		java.util.Iterator<ast_sent> i;
		for (i = sents.iterator(); i.hasNext();)
			(i.next()).traverse(a, is_post, is_pre);

		// a->end_context(this);
	}

	@Override
	public boolean has_scope() {
		return true;
	}

	public final java.util.LinkedList<ast_sent> get_sents() {
		return sents;
	}

	private ast_sentblock() {
		super(AST_NODE_TYPE.AST_SENTBLOCK);
		create_symtabs();
	}

	private java.util.LinkedList<ast_sent> sents = new java.util.LinkedList<ast_sent>();

}