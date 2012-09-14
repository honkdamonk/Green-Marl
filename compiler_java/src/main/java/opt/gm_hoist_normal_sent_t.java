package opt;

import static ast.AST_NODE_TYPE.AST_ASSIGN;

import java.util.LinkedList;

import ast.AST_NODE_TYPE;
import ast.ast_assign;
import ast.ast_sent;
import ast.ast_sentblock;

import common.gm_apply;
import common.gm_transform_helper;

import frontend.gm_rw_analysis_check2;

//------------------------------------------------------
// hoist up target-type sentence as far as possible 
// (in the same sent block)
// target-type should not be a var-decl
//------------------------------------------------------
abstract class gm_hoist_normal_sent_t extends gm_apply {

	/** if s is a sentence, you want to hoist up */
	protected abstract boolean check_target(ast_sent s);

	/** if p is a trivial precedessor to your target */
	protected abstract boolean check_trivial_pred(ast_sent p);

	@Override
	public boolean apply(ast_sent s) {
		if (s.get_nodetype() != AST_NODE_TYPE.AST_SENTBLOCK)
			return true;

		ast_sentblock sb = (ast_sentblock) s;

		// make a copy of sentence list (right?)
		LinkedList<ast_sent> sents = sb.get_sents();
		for (int i = 0; i < sents.size(); i++) {
			ast_sent target = sents.get(i);
			// --------------------------------------
			// find target assign sentence
			// --------------------------------------
			if (target.get_nodetype() != AST_ASSIGN || !check_target((ast_assign) target))
				continue;

			LinkedList<ast_sent> sents2 = sb.get_sents(); // most up-to-date
															// list
			LinkedList<ast_sent> stack = new LinkedList<ast_sent>();
			ast_sent top_position = null;

			// --------------------------------------
			// now find the possible upmost position
			// --------------------------------------
			for (ast_sent S : sents2) {
				if (S == target)
					break;
				if (stack.size() == 0) {
					// does not need to add into the queue
					if (check_trivial_pred(S)) {
						top_position = S;
						continue;
					}
				}
				stack.addFirst(S);
			}

			// ------------------------------------------------------------------------
			// stack contains list of sentences that have to check
			// top_position points the end of sentences that should precede
			// target.
			//
			// Check dependency
			// ------------------------------------------------------------------------
			while (stack.size() > 0) {
				ast_sent S = stack.getFirst();
				stack.removeFirst();
				if (gm_rw_analysis_check2.gm_has_dependency(S, target)) {
					top_position = S;
					break;
				}
			}

			gm_transform_helper.gm_ripoff_sent(target, gm_transform_helper.GM_NOFIX_SYMTAB);
			if (top_position == null) // move it to the beginning
			{
				gm_transform_helper.gm_insert_sent_begin_of_sb(sb, target, gm_transform_helper.GM_NOFIX_SYMTAB);
			} // move to the top
			else {
				gm_transform_helper.gm_add_sent_after(top_position, target, gm_transform_helper.GM_NOFIX_SYMTAB);
			}
		}
		return true;
	}

}