package opt;

import ast.AST_NODE_TYPE;
import ast.ast_bfs;
import ast.ast_foreach;
import ast.ast_sent;

import common.gm_apply;

import frontend.SYMTAB_TYPES;
import frontend.gm_symtab;
import frontend.gm_symtab_entry;

//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define TO_STR(X) #X
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define DEF_STRING(X) static const char *X = "X"
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP(CLASS, DESC) class CLASS : public gm_compile_step { private: CLASS() {set_description(DESC);}public: virtual void process(ast_procdef*p); virtual gm_compile_step* get_instance(){return new CLASS();} static gm_compile_step* get_factory(){return new CLASS();} };
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP_FACTORY(CLASS) CLASS::get_factory()

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

public class gm_moveup_propdecl_t extends gm_apply
{
	// state
	//  OUT_NIL   ; outside. not top found yet
	//  OUT_TOP   ; outside and top found
	//  LOOP      ; inside a sequential loop
	private static final int OUT_NIL = 0;
	private static final int OUT_TOP = 1;
	private static final int IN_LOOP = 2;
	// NIL -> TOP -> IN 
	//  <-----+      +
	//  <------------+

	private java.util.LinkedList<Integer> stack_state = new java.util.LinkedList<Integer>();
	private java.util.LinkedList<gm_symtab> stack_top_scope = new java.util.LinkedList<gm_symtab>();
	private java.util.LinkedList<ast_sent> stack_pushed_node = new java.util.LinkedList<ast_sent>();
	private java.util.HashMap<gm_symtab_entry, std.pair<gm_symtab, gm_symtab>> movements = new java.util.HashMap<gm_symtab_entry, std.pair<gm_symtab, gm_symtab>>(); // entry -> (from_symtab, to_symtab)

	private int curr_state;
	private gm_symtab curr_top_scope;
	private ast_sent curr_pushed_node;
	private gm_symtab this_scope;

	public gm_moveup_propdecl_t()
	{
		set_for_sent(true);
		set_for_symtab(true);
		set_separate_post_apply(true);

		curr_state = OUT_NIL;
		curr_top_scope = null;
		curr_pushed_node = null;
		this_scope = null;
	}

	//--------------------------------------------
	// called sequence for a node
	//    [begin_context] 
	//    apply(sent)
	//    apply(symtab, symtab_entry)
	//      ... recursive traverse
	//    apply2(symtab, symtab_entry)
	//    apply2(sent)
	//    [end_context]
	//--------------------------------------------
	@Override
	public boolean apply(ast_sent s)
	{
		boolean to_push = false;
		int new_state;
		gm_symtab new_top_scope = null;

		boolean to_nil = false;
		boolean to_loop = false;
		boolean to_top = false;
		int nt = (int)s.get_nodetype();
		if (nt == AST_NODE_TYPE.AST_SENTBLOCK.getValue())
			to_top = true;
		else if (nt == AST_NODE_TYPE.AST_IF.getValue())
			to_nil = true;
		else if (nt == AST_NODE_TYPE.AST_WHILE.getValue())
			to_loop = true;
		else if (nt == AST_NODE_TYPE.AST_FOREACH.getValue())
		{
			ast_foreach fe = (ast_foreach) s;
			if (fe.is_parallel())
				to_nil = true;
			else
				to_loop = true;
		}
		else if (nt == AST_NODE_TYPE.AST_BFS.getValue())
		{
			ast_bfs bfs = (ast_bfs) s;
			if (bfs.is_parallel())
				to_nil = true;
			else
				to_loop = true;
		}

		//------------------------------------
		// state machine
		//------------------------------------
		switch (curr_state)
		{
			case OUT_NIL:
				if (to_top)
				{
					to_push = true;
					new_state = OUT_TOP;
					// this_scope has not yet set, at this moment
					new_top_scope = s.get_symtab_field();
				}
				break;
			case OUT_TOP:
				if (to_nil)
				{
					to_push = true;
					new_state = OUT_NIL;
				}
				else if (to_loop)
				{
					to_push = true;
					new_state = IN_LOOP;
				}
				break;
			case IN_LOOP:
				if (to_nil)
				{
					to_push = true;
					new_state = OUT_NIL;
				}
				break;
		}

		// start a new state and push stacks
		if (to_push)
		{
			stack_state.addLast(curr_state);
			stack_pushed_node.addLast(curr_pushed_node);
			curr_state = new_state;
			curr_pushed_node = s;

			if (new_state == OUT_TOP)
			{
				stack_top_scope.addLast(curr_top_scope);
				curr_top_scope = new_top_scope;
			}

		}
		return true;
	}

	@Override
	public boolean apply2(ast_sent s)
	{
		// if I'm the one puhsed the state
		// pop it out
		if (curr_pushed_node == s)
		{
			if (curr_state == OUT_TOP)
			{
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
	public boolean apply(gm_symtab tab, int type)
	{
		if (type != SYMTAB_TYPES.GM_SYMTAB_FIELD.getValue())
			return true;
		this_scope = tab;
		return true;
	}
	@Override
	public boolean apply(gm_symtab_entry e, int type)
	{
		if (type != SYMTAB_TYPES.GM_SYMTAB_FIELD.getValue())
			return true;
		if (curr_state == IN_LOOP)
		{
			save_target(e, this_scope, curr_top_scope);
		}
		return true;
	}

	public final void save_target(gm_symtab_entry t, gm_symtab from, gm_symtab to)
	{
		std.pair<gm_symtab, gm_symtab> T = new std.pair<gm_symtab, gm_symtab>();
		T.first = from;
		T.second = to;
		movements.put(t, T);
	}

	public final void post_process()
	{
		java.util.Iterator<gm_symtab_entry, std.pair<gm_symtab, gm_symtab>> I;
		for (I = movements.iterator(); I.hasNext();)
		{
			gm_symtab_entry e = I.next().getKey();
			gm_symtab from = I.next().getValue().first;
			gm_symtab to = I.next().getValue().second;

			assert!to.is_entry_in_the_tab(e);
			assert from.is_entry_in_the_tab(e);

			from.remove_entry_in_the_tab(e);
			to.add_symbol(e);

		}
	}
}
//bool gm_independent_optimize::do_moveup_propdecl(ast_procdef* p)
