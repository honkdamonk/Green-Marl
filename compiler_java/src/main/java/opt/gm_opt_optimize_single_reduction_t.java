package opt;

import java.util.LinkedList;

import ast.AST_NODE_TYPE;
import ast.ast_assign;
import ast.ast_bfs;
import ast.ast_foreach;
import ast.ast_node;
import ast.ast_sent;

import common.gm_transform_helper;
import common.gm_apply;

import frontend.FrontendGlobal;
import frontend.gm_symtab_entry;

//-------------------------------------------------------------------
//  At each foreach or bfs
//    check bound symbols
//    if (1) every reduction assignment is (1) sigle-threaded or (2) non-conflicting
//
//  ==> change it into normal 
//-------------------------------------------------------------------
//
// optimize single-threaded reduction
public class gm_opt_optimize_single_reduction_t extends gm_apply {

	private LinkedList<ast_assign> targets = new LinkedList<ast_assign>();

	public gm_opt_optimize_single_reduction_t() {
		set_for_sent(true);
	}

	@Override
	public boolean apply(ast_sent s) {

		if (s.get_nodetype() == AST_NODE_TYPE.AST_ASSIGN) {
			ast_assign a = (ast_assign) s;
			if (a.is_reduce_assign()) {
				
				if(a.is_target_map_entry()) return true;
				
				assert a.get_bound() != null;
				gm_symtab_entry bound = a.get_bound().getSymInfo();

				// go up and check if it meets only single-threaded loops until
				// top
				ast_node n = a.get_parent();
				boolean found = false;
				boolean single = true;
				while (!found && single) {
					assert n != null;
					if (n.get_nodetype() == AST_NODE_TYPE.AST_FOREACH) {
						ast_foreach fe = (ast_foreach) n;
						if (!fe.is_sequential())
							single = false;
						if (fe.get_iterator().getSymInfo() == bound)
							found = true;
					} else if (n.get_nodetype() == AST_NODE_TYPE.AST_BFS) {
						ast_bfs bfs = (ast_bfs) n;
						if (bfs.is_bfs())
							single = false;
						if (bfs.get_iterator().getSymInfo() == bound)
							found = true;
						// what was iterator 2 again?
						if (bfs.get_iterator2().getSymInfo() == bound) 
							found = true;
					}
					n = n.get_parent();
				}
				if (single)
					targets.addLast(a);
			}

		}
		return true;
	}

	public final void post_process() {
		for (ast_assign a : targets) {
			assert a.is_reduce_assign();
			gm_transform_helper.gm_make_it_belong_to_sentblock(a);
			FrontendGlobal.gm_make_normal_assign(a);
		}
	}

}