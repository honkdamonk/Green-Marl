package ast;

import static ast.ast_node_type.AST_SENTBLOCK;

import java.util.LinkedList;

import common.gm_apply;
import common.gm_dumptree;

public class ast_sentblock extends ast_sent {
	
	private final LinkedList<ast_sent> sents = new LinkedList<ast_sent>();
	
	private ast_sentblock() {
		super(AST_SENTBLOCK);
		create_symtabs();
	}

	public static ast_sentblock new_sentblock() {
		return new ast_sentblock();
	}

	public final void add_sent(ast_sent s) {
		sents.addLast(s);
		s.set_parent(this);
	}

	@Override
	public void reproduce(int ind_level) {
		Out.pushln("{");
		for (ast_sent s : sents)
			s.reproduce(0);
		Out.pushln("}");
	}

	@Override
	public void dump_tree(int ind_level) {
		assert parent != null;
		gm_dumptree.IND(ind_level);
		System.out.print("[ \n");
		for (ast_sent s : sents) {
			s.dump_tree(ind_level + 1);
			System.out.print("\n");
		}
		gm_dumptree.IND(ind_level);
		System.out.print("]");
	}

	@Override
	public void traverse_sent(gm_apply a, boolean is_post, boolean is_pre) {
		// a->begin_context(this);
		if (a.is_traverse_local_expr_only())
			return;

		LinkedList<ast_sent> sents = get_sents();
		for (int i = 0; i < sents.size(); i++) {
			sents.get(i).traverse(a, is_post, is_pre);
		}

		// a->end_context(this);
	}

	@Override
	public boolean has_scope() {
		return true;
	}

	public final LinkedList<ast_sent> get_sents() {
		return sents;
	}

}