package opt;

import java.util.HashMap;
import java.util.LinkedList;

import tangible.Pair;
import ast.AST_NODE_TYPE;
import ast.ast_bfs;
import ast.ast_foreach;
import ast.ast_sent;

import common.gm_apply;

import frontend.SYMTAB_TYPES;
import frontend.gm_symtab;
import frontend.gm_symtab_entry;

//---------------------------------------------
// example
// {
//   while(a) {
//     A;
//     while(b) {
//       B;
//       if (c) {
//         C;
//         while(d) {
//            D;
//         }
//       }
//     }
//  }
//}
// ==>
// example
// {
//   A,B;
//   while(a) {
//     while(b) {
//       if(c) {
//        C,D;
//        while(d) {
// } } }  }

public class gm_moveup_propdecl_t extends gm_apply {
	// state
	// OUT_NIL ; outside. not top found yet
	// OUT_TOP ; outside and top found
	// LOOP ; inside a sequential loop
	private static final int OUT_NIL = 0;
	private static final int OUT_TOP = 1;
	private static final int IN_LOOP = 2;
	// NIL -> TOP -> IN
	// <-----+ +
	// <------------+

	private LinkedList<Integer> stack_state = new LinkedList<Integer>();
	private LinkedList<gm_symtab> stack_top_scope = new LinkedList<gm_symtab>();
	private LinkedList<ast_sent> stack_pushed_node = new LinkedList<ast_sent>();
	private HashMap<gm_symtab_entry, Pair<gm_symtab, gm_symtab>> movements = new HashMap<gm_symtab_entry, Pair<gm_symtab, gm_symtab>>(); // entry
																																			// ->
																																			// (from_symtab,
																																			// to_symtab)

	private int curr_state;
	private gm_symtab curr_top_scope;
	private ast_sent curr_pushed_node;
	private gm_symtab this_scope;

	public gm_moveup_propdecl_t() {
		set_for_sent(true);
		set_for_symtab(true);
		set_separate_post_apply(true);

		curr_state = OUT_NIL;
		curr_top_scope = null;
		curr_pushed_node = null;
		this_scope = null;
	}

	// --------------------------------------------
	// called sequence for a node
	// [begin_context]
	// apply(sent)
	// apply(symtab, symtab_entry)
	// ... recursive traverse
	// apply2(symtab, symtab_entry)
	// apply2(sent)
	// [end_context]
	// --------------------------------------------
	@Override
	public boolean apply(ast_sent s) {
		boolean to_push = false;
		int new_state = curr_state;
		gm_symtab new_top_scope = null;

		boolean to_nil = false;
		boolean to_loop = false;
		boolean to_top = false;
		AST_NODE_TYPE nt = s.get_nodetype();
		if (nt == AST_NODE_TYPE.AST_SENTBLOCK)
			to_top = true;
		else if (nt == AST_NODE_TYPE.AST_IF)
			to_nil = true;
		else if (nt == AST_NODE_TYPE.AST_WHILE)
			to_loop = true;
		else if (nt == AST_NODE_TYPE.AST_FOREACH) {
			ast_foreach fe = (ast_foreach) s;
			if (fe.is_parallel())
				to_nil = true;
			else
				to_loop = true;
		} else if (nt == AST_NODE_TYPE.AST_BFS) {
			ast_bfs bfs = (ast_bfs) s;
			if (bfs.is_parallel())
				to_nil = true;
			else
				to_loop = true;
		}

		// ------------------------------------
		// state machine
		// ------------------------------------
		switch (curr_state) {
		case OUT_NIL:
			if (to_top) {
				to_push = true;
				new_state = OUT_TOP;
				// this_scope has not yet set, at this moment
				new_top_scope = s.get_symtab_field();
			}
			break;
		case OUT_TOP:
			if (to_nil) {
				to_push = true;
				new_state = OUT_NIL;
			} else if (to_loop) {
				to_push = true;
				new_state = IN_LOOP;
			}
			break;
		case IN_LOOP:
			if (to_nil) {
				to_push = true;
				new_state = OUT_NIL;
			}
			break;
		}

		// start a new state and push stacks
		if (to_push) {
			stack_state.addLast(curr_state);
			stack_pushed_node.addLast(curr_pushed_node);
			curr_state = new_state;
			curr_pushed_node = s;

			if (new_state == OUT_TOP) {
				stack_top_scope.addLast(curr_top_scope);
				curr_top_scope = new_top_scope;
			}

		}
		return true;
	}

	@Override
	public boolean apply2(ast_sent s) {
		// if I'm the one puhsed the state
		// pop it out
		if (curr_pushed_node == s) {
			if (curr_state == OUT_TOP) {
				curr_top_scope = stack_top_scope.getLast();
				stack_top_scope.removeLast();
			}

			curr_state = stack_state.getLast();
			curr_pushed_node = stack_pushed_node.getLast();

			stack_state.removeLast();
			stack_pushed_node.removeLast();
		}
		return true;
	}

	@Override
	public boolean apply(gm_symtab tab, SYMTAB_TYPES type) {
		if (type != SYMTAB_TYPES.GM_SYMTAB_FIELD)
			return true;
		this_scope = tab;
		return true;
	}

	@Override
	public boolean apply(gm_symtab_entry e, SYMTAB_TYPES type) {
		if (type != SYMTAB_TYPES.GM_SYMTAB_FIELD)
			return true;
		if (curr_state == IN_LOOP) {
			save_target(e, this_scope, curr_top_scope);
		}
		return true;
	}

	public final void save_target(gm_symtab_entry t, gm_symtab from, gm_symtab to) {
		Pair<gm_symtab, gm_symtab> T = new Pair<gm_symtab, gm_symtab>(from, to);
		movements.put(t, T);
	}

	public final void post_process() {
		for (gm_symtab_entry e : movements.keySet()) {
			gm_symtab from = movements.get(e).first;
			gm_symtab to = movements.get(e).second;

			assert !to.is_entry_in_the_tab(e);
			assert from.is_entry_in_the_tab(e);

			from.remove_entry_in_the_tab(e);
			to.add_symbol(e);
		}
	}
}
// bool gm_independent_optimize::do_moveup_propdecl(ast_procdef* p)
