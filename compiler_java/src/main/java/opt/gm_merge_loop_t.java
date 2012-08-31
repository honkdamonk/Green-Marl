package opt;

import java.util.LinkedList;

import ast.AST_NODE_TYPE;
import ast.ast_foreach;
import ast.ast_sent;
import ast.ast_sentblock;

import common.gm_merge_sentblock;
import common.gm_transform_helper;
import common.gm_apply;

import frontend.GlobalMembersGm_rw_analysis;

public class gm_merge_loop_t extends gm_apply {
	
	protected boolean _changed;
	protected LinkedList<ast_sent> to_be_deleted = new LinkedList<ast_sent>();
	
	@Override
	public boolean apply(ast_sent s) {
		if (s.get_nodetype() != AST_NODE_TYPE.AST_SENTBLOCK)
			return true;

		ast_sentblock sb = (ast_sentblock) s;
		// work with a copyed list TODO it's not a copy!
		LinkedList<ast_sent> sents = sb.get_sents(); 
		
		ast_foreach prev = null;
		for (ast_sent sent : sents) {
			if (prev == null) {
				if (sent.get_nodetype() == AST_NODE_TYPE.AST_FOREACH)
					prev = (ast_foreach) sent;
				continue;
			} else {
				// pick two consecutive foreach blocks.
				// check they are mergeable.
				// If so, merge. delete the second one.
				if (sent.get_nodetype() == AST_NODE_TYPE.AST_FOREACH) {
					ast_foreach curr = (ast_foreach) sent;
					if (GlobalMembersGm_merge_loops.gm_is_mergeable_loops(prev, curr)) {

						// replace curr's iterator with prev's
						GlobalMembersGm_merge_loops.replace_iterator_sym(prev, curr);

						// merge body and delete curr.
						if (prev.get_body().get_nodetype() != AST_NODE_TYPE.AST_SENTBLOCK)
							gm_transform_helper.gm_make_it_belong_to_sentblock(prev.get_body());
						if (curr.get_body().get_nodetype() != AST_NODE_TYPE.AST_SENTBLOCK)
							gm_transform_helper.gm_make_it_belong_to_sentblock(curr.get_body());

						gm_merge_sentblock.gm_merge_sentblock((ast_sentblock) prev.get_body(), (ast_sentblock) curr.get_body());

						// redo-rw-analysis
						GlobalMembersGm_rw_analysis.gm_redo_rw_analysis(prev);

						gm_transform_helper.gm_ripoff_sent(curr, gm_transform_helper.GM_NOFIX_SYMTAB); // it
																																	// will
																																	// be
																																	// deleted
						if (curr != null)
							curr.dispose();

						_changed = true;
					} else {
						prev = curr;
					}
				} else {
					prev = null;
				}
			}
		}
		return true;
	}

	public final boolean is_changed() {
		return _changed;
	}

	public final void do_loop_merge(ast_sentblock top) {
		set_all(false);
		set_for_sent(true);
		_changed = false;
		top.traverse_post(this);

		to_be_deleted.clear();
	}

}
