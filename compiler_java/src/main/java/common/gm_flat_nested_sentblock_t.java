package common;

import java.util.LinkedList;

import ast.ast_node_type;
import ast.ast_sent;
import ast.ast_sentblock;

public class gm_flat_nested_sentblock_t extends gm_apply {
	
	private final LinkedList<ast_sentblock> targets = new LinkedList<ast_sentblock>();
	
	// requires 'post' apply
	public gm_flat_nested_sentblock_t() {
		set_for_sent(true);
	}

	@Override
	public final boolean apply(ast_sent s) {
		// parent should be another sentblock
		if ((s.get_parent() == null) || (s.get_parent().get_nodetype() != ast_node_type.AST_SENTBLOCK))
			return true;

		if (s.get_nodetype() == ast_node_type.AST_SENTBLOCK) {
			ast_sentblock sb = (ast_sentblock) s;

			// flat only if no new symbol is defined
			if ((sb.get_symtab_field().get_num_symbols() == 0) && (sb.get_symtab_var().get_num_symbols() == 0) && (sb.get_symtab_proc().get_num_symbols() == 0)) {
				targets.addLast(sb);
			}
		}

		return true;
	}

	public final void post_process() {
		for (ast_sentblock sb : targets) {
			ast_sentblock parent = (ast_sentblock) sb.get_parent();

			LinkedList<ast_sent> parent_sents = parent.get_sents();
			LinkedList<ast_sent> my_sents = sb.get_sents();

			// prepare moving mine to parent's
			for (ast_sent s : my_sents) {
				s.set_parent(parent);
			}

			// TODO not tested!
			// find location
			int index = parent_sents.indexOf(sb);
			// move my_sents to parent
			parent_sents.addAll(index + 1, my_sents);
			parent_sents.remove(index);

			// delete SB
			if (sb != null)
				sb.dispose();
		}
	}

}