package frontend;

import static ast.AST_NODE_TYPE.AST_SENTBLOCK;
import static ast.AST_NODE_TYPE.AST_VARDECL;

import java.util.LinkedList;

import ast.ast_procdef;
import ast.ast_sent;
import ast.ast_sentblock;
import ast.ast_vardecl;

import common.gm_apply;
import common.gm_transform_helper;
import common.gm_traverse;

public class remove_vardecl_t extends gm_apply {

	// POST Apply
	@Override
	public boolean apply(ast_sent b) {
		if (b.get_nodetype() != AST_SENTBLOCK)
			return true;
		ast_sentblock sb = (ast_sentblock) b;
		LinkedList<ast_sent> sents = sb.get_sents(); // need a copy
		LinkedList<ast_sent> stack = new LinkedList<ast_sent>();

		// --------------------------------------------
		// 1. find all var-decls
		// 3. delete var-decl
		// --------------------------------------------
		for (ast_sent z : sents) {
			if (z.get_nodetype() != AST_VARDECL)
				continue;
			ast_vardecl v = (ast_vardecl) z;

			stack.addLast(v);
		}

		// 3. delete var-decl
		for (ast_sent z : stack) {
			// now delete
			gm_transform_helper.gm_ripoff_sent(z, false);
			if (z != null)
				z.dispose();
		}
		return true;
	}

	public final void do_removal(ast_procdef p) {
		set_all(false);
		set_for_sent(true);
		gm_traverse.gm_traverse_sents(p, this, gm_traverse.GM_POST_APPLY);
	}
}