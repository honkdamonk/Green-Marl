import ast.AST_NODE_TYPE;
import ast.ast_bfs;
import ast.ast_expr;
import ast.ast_expr_reduce;
import ast.ast_foreach;
import ast.ast_if;
import ast.ast_node;
import ast.ast_procdef;
import ast.ast_sent;
import ast.ast_sentblock;
import ast.ast_while;
import frontend.gm_scope;

public class GlobalMembersGm_transform_helper
{
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define TO_STR(X) #X
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define DEF_STRING(X) static const char *X = "X"
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define GM_COMPILE_STEP(CLASS, DESC) class CLASS : public gm_compile_step { private: CLASS() {set_description(DESC);}public: virtual void process(ast_procdef*p); virtual gm_compile_step* get_instance(){return new CLASS();} static gm_compile_step* get_factory(){return new CLASS();} };
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define GM_COMPILE_STEP_FACTORY(CLASS) CLASS::get_factory()


	public static final boolean GM_FIX_SYMTAB = true;
	public static final boolean GM_NOFIX_SYMTAB = false;
public static void gm_make_it_belong_to_sentblock(ast_sent s)
{
	gm_make_it_belong_to_sentblock(s, true);
}

	//-----------------------------------------------
	// helper functions for code transformation
	// This functions helps to rip off a certain IR from the code
	// and/or to add it into another IR.
	// [NOTE] (variable/field declarations) cannot be moved with these 
	// 
	// The functions makes sure IR strucuture is valid after transformation,including
	// symtab hierarchy.
	// 
	// If this transform is called prior to typecheck, set fix_symtab_entry as false.
	//-----------------------------------------------

	//--------------------------------------------------------------------
	// make the current statement belong to a sentence block, if not already.
	// (e.g. if (cond) s; ==> if (cond) {s;})
	// If already in a sentblock
	// (case. a) do nothing. => ...belong_to_sentblock(s)
	// (case. b) make it a nested sentence block.  ==> ...belong_to_sentblock_nested(s)
	//--------------------------------------------------------------------
//C++ TO JAVA CONVERTER NOTE: Java does not allow default values for parameters. Overloaded methods are inserted above.
//ORIGINAL LINE: void gm_make_it_belong_to_sentblock(ast_sent* s, boolean need_fix_symtab = true)
	public static void gm_make_it_belong_to_sentblock(ast_sent s, boolean need_fix_symtab)
	{
		GlobalMembersGm_transform_helper.gm_make_it_belong_to_sentblock_main(s, false, need_fix_symtab);
	}
public static void gm_make_it_belong_to_sentblock_nested(ast_sent s)
{
	gm_make_it_belong_to_sentblock_nested(s, true);
}
//C++ TO JAVA CONVERTER NOTE: Java does not allow default values for parameters. Overloaded methods are inserted above.
//ORIGINAL LINE: void gm_make_it_belong_to_sentblock_nested(ast_sent *s, boolean need_fix_symtab = true)
	public static void gm_make_it_belong_to_sentblock_nested(ast_sent s, boolean need_fix_symtab)
	{
		// similar to previous function. But if the up-node is already a sent block,
		GlobalMembersGm_transform_helper.gm_make_it_belong_to_sentblock_main(s, true, need_fix_symtab);
	}
public static void gm_ripoff_sent(ast_sent target)
{
	gm_ripoff_sent(target, true);
}

	//--------------------------------------------------------------------
	// rip-off a sentence from its parent.
	// do clean-up for symtab. Make sure the parent have at least one (empty) sentence.
	// e.g. if (A) s;  ==> if (A) {}
	//--------------------------------------------------------------------
//C++ TO JAVA CONVERTER NOTE: Java does not allow default values for parameters. Overloaded methods are inserted above.
//ORIGINAL LINE: void gm_ripoff_sent(ast_sent* target, boolean need_fix_symtab = true)
	public static void gm_ripoff_sent(ast_sent target, boolean need_fix_symtab)
	{
		// make sure that target belongs to a sent block
		GlobalMembersGm_transform_helper.gm_make_it_belong_to_sentblock(target, need_fix_symtab);

		ast_sentblock sb = (ast_sentblock) target.get_parent();
		assert sb.get_nodetype() == AST_NODE_TYPE.AST_SENTBLOCK;

		if (need_fix_symtab) // rip-off
		{
			GlobalMembersGm_transform_helper.gm_ripoff_upper_scope(target);
		}

		java.util.LinkedList<ast_sent> sents = sb.get_sents();
		java.util.Iterator<ast_sent> i;
		;
		target.set_parent(null);

		for (i = sents.iterator(); i.hasNext();)
		{
			if (i.next() == target)
				break;
		}
		assert i.hasNext();
//C++ TO JAVA CONVERTER TODO TASK: There is no direct equivalent to the STL list 'erase' method in Java:
		sents.erase(i);
	}
public static void gm_add_sent_begin(ast_sent current, ast_sent target)
{
	gm_add_sent_begin(current, target, true);
}

	//--------------------------------------------------------------------
	// add new sentence at the (start,end) of the sentence block where current belongs to
	//--------------------------------------------------------------------
//C++ TO JAVA CONVERTER NOTE: Java does not allow default values for parameters. Overloaded methods are inserted above.
//ORIGINAL LINE: void gm_add_sent_begin(ast_sent* current, ast_sent* target, boolean need_fix_symtab = true)
	public static void gm_add_sent_begin(ast_sent current, ast_sent target, boolean need_fix_symtab)
	{
		GlobalMembersGm_transform_helper.gm_add_sent(current, target, gm_insert_location_t.GM_INSERT_BEGIN, need_fix_symtab);
	}
public static void gm_add_sent_end(ast_sent current, ast_sent target)
{
	gm_add_sent_end(current, target, true);
}
//C++ TO JAVA CONVERTER NOTE: Java does not allow default values for parameters. Overloaded methods are inserted above.
//ORIGINAL LINE: void gm_add_sent_end(ast_sent* current, ast_sent* target, boolean need_fix_symtab = true)
	public static void gm_add_sent_end(ast_sent current, ast_sent target, boolean need_fix_symtab)
	{
		GlobalMembersGm_transform_helper.gm_add_sent(current, target, gm_insert_location_t.GM_INSERT_BEGIN, need_fix_symtab);
	}
public static void gm_add_sent_before(ast_sent current, ast_sent target)
{
	gm_add_sent_before(current, target, true);
}

	//--------------------------------------------------------------------
	// add new sentence right before/after the current (in the same sentence block)
	//--------------------------------------------------------------------
//C++ TO JAVA CONVERTER NOTE: Java does not allow default values for parameters. Overloaded methods are inserted above.
//ORIGINAL LINE: void gm_add_sent_before(ast_sent* current, ast_sent* target, boolean need_fix_symtab = true)
	public static void gm_add_sent_before(ast_sent current, ast_sent target, boolean need_fix_symtab)
	{
		GlobalMembersGm_transform_helper.gm_add_sent(current, target, gm_insert_location_t.GM_INSERT_BEFORE, need_fix_symtab);
	}
public static void gm_add_sent_after(ast_sent current, ast_sent target)
{
	gm_add_sent_after(current, target, true);
}
//C++ TO JAVA CONVERTER NOTE: Java does not allow default values for parameters. Overloaded methods are inserted above.
//ORIGINAL LINE: void gm_add_sent_after(ast_sent* current, ast_sent* target, boolean need_fix_symtab = true)
	public static void gm_add_sent_after(ast_sent current, ast_sent target, boolean need_fix_symtab)
	{
		GlobalMembersGm_transform_helper.gm_add_sent(current, target, gm_insert_location_t.GM_INSERT_AFTER, need_fix_symtab);
	}
public static void gm_insert_sent_begin_of_sb(ast_sentblock sb, ast_sent target)
{
	gm_insert_sent_begin_of_sb(sb, target, true);
}

	//--------------------------------------------------------------------
	// similar to add_sent_*. But explicitly give the sentence bock
	//--------------------------------------------------------------------
//C++ TO JAVA CONVERTER NOTE: Java does not allow default values for parameters. Overloaded methods are inserted above.
//ORIGINAL LINE: void gm_insert_sent_begin_of_sb(ast_sentblock* sb, ast_sent* target, boolean need_fix_symtab = true)
	public static void gm_insert_sent_begin_of_sb(ast_sentblock sb, ast_sent target, boolean need_fix_symtab)
	{
		GlobalMembersGm_transform_helper.gm_insert_sent_in_sb(sb, target, gm_insert_location_t.GM_INSERT_BEGIN, need_fix_symtab);
	}
public static void gm_insert_sent_end_of_sb(ast_sentblock sb, ast_sent target)
{
	gm_insert_sent_end_of_sb(sb, target, true);
}
//C++ TO JAVA CONVERTER NOTE: Java does not allow default values for parameters. Overloaded methods are inserted above.
//ORIGINAL LINE: void gm_insert_sent_end_of_sb(ast_sentblock* sb, ast_sent* target, boolean need_fix_symtab = true)
	public static void gm_insert_sent_end_of_sb(ast_sentblock sb, ast_sent target, boolean need_fix_symtab)
	{
		GlobalMembersGm_transform_helper.gm_insert_sent_in_sb(sb, target, gm_insert_location_t.GM_INSERT_END, need_fix_symtab);
	}
public static void gm_insert_sent_body_begin(ast_foreach fe, ast_sent target)
{
	gm_insert_sent_body_begin(fe, target, true);
}
//C++ TO JAVA CONVERTER NOTE: Java does not allow default values for parameters. Overloaded methods are inserted above.
//ORIGINAL LINE: void gm_insert_sent_body_begin(ast_foreach* fe, ast_sent* target, boolean need_fix_symtab = true)
	public static void gm_insert_sent_body_begin(ast_foreach fe, ast_sent target, boolean need_fix_symtab)
	{
		ast_sent s = fe.get_body();
		if (s.get_nodetype() == AST_NODE_TYPE.AST_SENTBLOCK)
		{
			GlobalMembersGm_transform_helper.gm_insert_sent_begin_of_sb((ast_sentblock) s, target, need_fix_symtab);
		}
		else
		{
			GlobalMembersGm_transform_helper.gm_add_sent_begin(s, target, need_fix_symtab);
		}
	}
public static void gm_insert_sent_body_end(ast_foreach fe, ast_sent target)
{
	gm_insert_sent_body_end(fe, target, true);
}
//C++ TO JAVA CONVERTER NOTE: Java does not allow default values for parameters. Overloaded methods are inserted above.
//ORIGINAL LINE: void gm_insert_sent_body_end(ast_foreach* fe, ast_sent* target, boolean need_fix_symtab = true)
	public static void gm_insert_sent_body_end(ast_foreach fe, ast_sent target, boolean need_fix_symtab)
	{
		ast_sent s = fe.get_body();
		if (s.get_nodetype() == AST_NODE_TYPE.AST_SENTBLOCK)
		{
			GlobalMembersGm_transform_helper.gm_insert_sent_end_of_sb((ast_sentblock) s, target, need_fix_symtab);
		}
		else
		{
			GlobalMembersGm_transform_helper.gm_add_sent_end(s, target, need_fix_symtab);
		}
	}

	//------------------------------------------------------------
	// Scope management
	//------------------------------------------------------------
	public static void gm_replace_upper_scope(ast_node n, gm_scope old_scope, gm_scope new_scope)
	{
		replace_upper_scope R = new replace_upper_scope();
		R.set_old_scope(old_scope);
		R.set_new_scope(new_scope);
		n.traverse(R, false, true); // PRE visit
	}
	public static void gm_ripoff_upper_scope(ast_node n)
	{
		gm_scope s = new gm_scope();
		GlobalMembersGm_transform_helper.find_enclosing_scope(n, s);

		ripoff_upper_scope R = new ripoff_upper_scope();
		R.set_scope_to_remove(s);

		n.traverse(R, false, true); //PRE visit
	}
	public static void gm_put_new_upper_scope_on_null(ast_node n, gm_scope new_s)
	{
		putnew_upper_scope_on_null N = new putnew_upper_scope_on_null();
		N.set_scope_to_put(new_s);

		n.traverse(N, false, true);
	}
	public static void gm_put_new_upper_scope(ast_node n, gm_scope new_s)
	{
		gm_scope old_s = new gm_scope();
		GlobalMembersGm_transform_helper.find_enclosing_scope(n, old_s);
		GlobalMembersGm_transform_helper.gm_replace_upper_scope(n, old_s, new_s);
	}

	// re-construct scope
	public static void gm_reconstruct_scope(ast_node top)
	{
		assert top.has_scope();
		gm_reconstruct_scope_t T = new gm_reconstruct_scope_t(top);
		top.traverse_pre(T);
	}

	//------------------------------------------------------------
	// Symbol addition and creation
	//------------------------------------------------------------
	// Find an upscope where I can add some symbol defs
	//ast_sentblock gm_find_upscope(ast_sent s);
	// Add symbols into some scope
	//gm_symtab_entry gm_add_new_symbol_primtype(ast_sentblock sb, int primtype, tangible.RefObject<String> new_vname); // assumtpion: no name-conflict.
	//gm_symtab_entry gm_add_new_symbol_property(ast_sentblock sb, int primtype, boolean is_nodeprop, gm_symtab_entry target_graph, tangible.RefObject<String> new_vname); // assumtpion: no name-conflict.
	//gm_symtab_entry gm_add_new_symbol_nodeedge_type(ast_sentblock sb, int nodeedge_type, gm_symtab_entry target_graph, tangible.RefObject<String> new_vname); // assumtpion: no name-conflict.

	// returns sentblock that defines the given entry
	//ast_sentblock gm_find_defining_sentblock_up(ast_node begin, gm_symtab_entry e, boolean is_property);

	//------------------------------------------------------------
	// Replace every symbol access
	// e.g> source: x , target: _z
	//      x = y + 1 ;  
	// ==>  _z = y + 1;
	// return true if replaced at least on instance
	// caller have to gaurantee that target symbol does not break scope rule
	//------------------------------------------------------------
	//boolean gm_replace_symbol_access_scalar_scalar(ast_node top, gm_symtab_entry src, gm_symtab_entry target, boolean chage_rhs, boolean change_lhs);
	//boolean gm_replace_symbol_access_scalar_field(ast_node top, gm_symtab_entry src, gm_symtab_entry t_drv, gm_symtab_entry target, boolean chage_rhs, boolean change_lhs);
	//boolean gm_replace_symbol_access_field_scalar(ast_node top, gm_symtab_entry src_drv, gm_symtab_entry src, gm_symtab_entry target, boolean chage_rhs, boolean change_lhs);
	//boolean gm_replace_symbol_access_field_field(ast_node top, gm_symtab_entry src_drv, gm_symtab_entry src, gm_symtab_entry t_drv, gm_symtab_entry target, boolean chage_rhs, boolean change_lhs);

	//------------------------------------------------------------
	// move a symbol one (or more) scope up, to another sentblock
	// returns the sentblock that the symbol belongs newly.
	// returns NULL if symbol already at the top
	// name conflict is resolved inside.
	//------------------------------------------------------------
	//ast_sentblock gm_move_symbol_up(gm_symtab_entry e, gm_symtab old_tab, boolean is_scalar);

	// [assumption] new_tab belongs to a sentence block
	// name conflict is resolved inside.
	//void gm_move_symbol_into(gm_symtab_entry e, gm_symtab old_tab, gm_symtab new_tab, boolean is_scalar);

	// remove set of symbols definitions in the given AST
	// caller should make sure that deleted symbols are not used anymore
	//void gm_remove_symbols(ast_node top, java.util.HashSet<gm_symtab_entry> S);
	//void gm_remove_symbol(ast_node top, gm_symtab_entry sym);

	//------------------------------------------------------------
	// Node creation after type-check
	//------------------------------------------------------------

	// note:see gm_new_foreach_after_tc.cc for assumptions about the argument and output.
	//ast_foreach gm_new_foreach_after_tc(ast_id it, ast_id src, ast_sent body, int iter_type);

	//ast_expr_reduce gm_new_expr_reduce_after_tc(ast_id it, ast_id src, ast_expr body, ast_expr filter, int iter_type);

	// 'bottom' symbol for reduction
	//ast_expr gm_new_bottom_symbol(int reduce_type, int lhs_type);

	//------------------------------------------------------------
	// Ohter helpers
	//------------------------------------------------------------
	// replace expression old_e with new_e.
	// [the routine expects that there is only 1 instance of old_e inside target top expreesion]
	// note: symtab hierarchy is *not* re-validated by this routine
	// (thus be careful if new_e contains Sum/Product...)
	public static boolean gm_replace_subexpr(ast_expr target, ast_expr old_e, ast_expr new_e)
	{
		assert target != old_e;

		replace_subexpr_A T = new replace_subexpr_A(old_e, new_e);
		target.traverse(T, true, false); // traverse it with 'post-apply'
		return T.has_found();
	}

	public static boolean gm_replace_expr_general(ast_node top, gm_expr_replacement_t E)
	{
		gm_replace_traverse_t T = new gm_replace_traverse_t(E);
		top.traverse_post(T);

		return T.is_changed();
	}

	//-------------------------------------------------------------------------------- 
	//[defined in gm_resolve_nce.cc]
	// If any sub-scope S has a name conflict with e, rename it. 
	// returns true if name conflict has happend at least once.
	//--------------------------------------------------------------------------------
	//extern boolean gm_resolve_name_conflict(ast_sent s, gm_symtab_entry e, boolean is_scalar);

	//---------------------------------------------------------------------------------------
	// For any id node in the subtree top, replace its symbol-entyr refrence from e_old to e_new.
	// If the new symbol has different orgname(), modify the name (in ID node) as well.
	// [Assumption. e_new is a valid symbol entry that does not break scoping rule.]
	//---------------------------------------------------------------------------------------
	//extern boolean gm_replace_symbol_entry(gm_symtab_entry e_old, gm_symtab_entry e_new, ast_node top);

	//---------------------------------------------------------------
	// Merge subblock P,Q into P
	//  (assumption) type-check has been finished
	//  (assumption) var-decl has been hoisted up
	//  (being done in this function)
	//     - all sentence in Q is moved to P
	//     - name conflicts are resolved.
	//     - symbol table is kept valid
	//     - sentence block Q becomes empty
	//  (caution)
	//     - You have to re-do rw-analysis.
	//---------------------------------------------------------------
	//void gm_merge_sentblock(ast_sentblock P, ast_sentblock Q, boolean delete_Q_after);

//---------------------------------------------------------------
// Check if this sentence is enclosed within a seq-loop.
// (without meeting an if-else branch)
// return NULL -> if not.
// return sent -> enclosing foreach statement
// e.g.
//   Do {
//      s1; // <- yes
//      If (cond) 
//         s2; // <- no
//   } While (cond2);
//---------------------------------------------------------------

	//---------------------------------------------------------------
	// Check if this sentence is enclosed within a seq-loop.
	// (without meeting an if-else branch)
	// return NULL -> if not.
	// return sent -> enclosing foreach statement
	// e.g.
	//   Do {
	//      s1; // <- yes
	//      If (cond) 
	//         s2; // <- no
	//   } While (cond2);
	//---------------------------------------------------------------
	public static ast_sent gm_find_enclosing_seq_loop(ast_node S)
	{
		ast_node up = S.get_parent();
		if (up == null) // NULL means no.
			return null;

		if (up.get_nodetype() == AST_NODE_TYPE.AST_IF) // conditional
			return null;

		if (up.get_nodetype() == AST_NODE_TYPE.AST_WHILE)
		{
			return (ast_sent) up;
		}

		if (up.get_nodetype() == AST_NODE_TYPE.AST_BFS)
		{
			return null;
		}

		if (up.get_nodetype() == AST_NODE_TYPE.AST_FOREACH)
		{
			ast_foreach fe = (ast_foreach) up;
			if (!fe.is_sequential())
				return null;
			else
				return fe;
		}

		return GlobalMembersGm_transform_helper.gm_find_enclosing_seq_loop(up);
	}

//--------------------------------------------------------------------
// check if the last of the sentence block has ended with 'return'
//--------------------------------------------------------------------

	//---------------------------------------------------------
	// For dead code elimination
	//---------------------------------------------------------
	public static boolean gm_check_if_end_with_return(ast_sentblock sb)
	{
		// [XXX]
		// simply check last sentence only
		// should be merged with dead-code elimination
		if (sb.get_sents().getLast() == null)
		{
			return false;
		}
		else if (sb.get_sents().getLast().get_nodetype() == AST_NODE_TYPE.AST_RETURN)
		{
			return true;
		}
		else
		{
			return false;
		}

	}

	//-------------------------------------------------------------
	// (defined in gm_parallel_helper.cc)
	// Current situation: some foreach are set as parallel, and others not.
	// Mark all the sentences correctly if they are under parallel execution.
	// e.g.
	// { // <- entry is sequential or not
	//   A;
	//   Foreach(s) // par
	//   {
	//     B;
	//     Foreach(t) // seq
	//       C;
	//   }
	//   Foreach(u)  // seq
	//      D;
	// -->
	//  B, C will be marked as 'under parallel'
	//  A, D is not.
	//-------------------------------------------------------------
	//extern void gm_mark_sents_under_parallel_execution(ast_sent T, boolean entry_is_seq);

	// Change a reduction assign into normal assign
	//extern void gm_make_normal_assign(ast_assign a);

	// flatten nested sentblock if possible
	//extern void gm_flat_nested_sentblock(ast_node n);

	//extern boolean gm_check_if_end_with_return(ast_sentblock sb);

//--------------------------------------------------------------------
// find the sentence that contains this expression
//--------------------------------------------------------------------
	public static ast_sent gm_find_parent_sentence(ast_expr e)
	{
		ast_node up = e.get_parent();
		if (up == null)
			return null;
		else if (up.is_sentence())
			return (ast_sent) up;
		else if (up.is_expr())
			return GlobalMembersGm_transform_helper.gm_find_parent_sentence((ast_expr) up);
		else
			return null;
	}



	//---------------------------------------------------------
	// helper functions for code transformation
	//---------------------------------------------------------
	public static void find_enclosing_scope(ast_node n, gm_scope s)
	{
		assert n != null;
		if (!n.has_symtab())
		{
			GlobalMembersGm_transform_helper.find_enclosing_scope(n.get_parent(), s);
			return;
		}
		if (n.get_nodetype() == AST_NODE_TYPE.AST_FOREACH)
		{
			ast_foreach fe = (ast_foreach) n;
			fe.get_this_scope(s);
			return;
		}
		else if (n.get_nodetype() == AST_NODE_TYPE.AST_SENTBLOCK)
		{
			ast_foreach sb = (ast_foreach) n;
			sb.get_this_scope(s);
			return;
		}
		else if (n.get_nodetype() == AST_NODE_TYPE.AST_PROCDEF)
		{
			ast_procdef p = (ast_procdef) n;
			p.get_this_scope(s);
			return;
		}
		else if (n.get_nodetype() == AST_NODE_TYPE.AST_EXPR_RDC)
		{
			ast_expr_reduce r = (ast_expr_reduce) n;
			r.get_this_scope(s);
			return;
		}
		else if (n.get_nodetype() == AST_NODE_TYPE.AST_BFS)
		{
			ast_bfs r = (ast_bfs) n;
			r.get_this_scope(s);
			return;
		}
		else
		{
			System.out.printf("node type = %s\n", GlobalMembersGm_misc.gm_get_nodetype_string(n.get_nodetype()));
			assert false;
		}
	}

	//============================================================================================

	public static void gm_make_it_belong_to_sentblock_main(ast_sent s, boolean allow_nesting, boolean need_fix_symtab)
	{
		ast_node up = s.get_parent();
		assert up != null;

		// already belong to a sent block
		if ((up.get_nodetype() == AST_NODE_TYPE.AST_SENTBLOCK) && (!allow_nesting))
			return;

		ast_sentblock sb = ast_sentblock.new_sentblock();

		//---------------------------------------------
		// properly rip-off and fix symbol table
		//---------------------------------------------
		if (need_fix_symtab)
		{
			gm_scope up_scope = new gm_scope();
			GlobalMembersGm_transform_helper.find_enclosing_scope(s, up_scope); // old enclosing scope for s

			GlobalMembersGm_transform_helper.gm_put_new_upper_scope_on_null(sb, up_scope); // new enclosing scope for sb

			gm_scope SB_scope = new gm_scope();
			sb.get_this_scope(SB_scope);

			GlobalMembersGm_transform_helper.gm_replace_upper_scope(s, up_scope, SB_scope); // new enclosing scope for s
		}

		sb.add_sent(s);
		s.set_parent(sb);
		sb.set_parent(up);

		//------------------------------------------------------
		// replace original sentence with sentenceblock 
		//------------------------------------------------------
		if (up.get_nodetype() == AST_NODE_TYPE.AST_FOREACH)
		{
			ast_foreach fe = (ast_foreach) up;
			fe.set_body(sb);
		}
		else if (up.get_nodetype() == AST_NODE_TYPE.AST_IF)
		{
			ast_if iff = (ast_if) up;
			if (iff.get_then() == s)
			{
				iff.set_then(sb);
			}
			else if (iff.get_else() == s)
			{
				iff.set_else(sb);
			}
			else
			{
				assert false;
			}
		}
		else if (up.get_nodetype() == AST_NODE_TYPE.AST_SENTBLOCK)
		{
			ast_sentblock old_sb = (ast_sentblock) up;

			// replace old sentence (s) into new one (sb) in old sb
			java.util.LinkedList<ast_sent> Sents = old_sb.get_sents();
			java.util.Iterator<ast_sent> I = std.find(Sents.iterator(), Sents.end(), s);
			assert I.hasNext();
//C++ TO JAVA CONVERTER TODO TASK: There is no direct equivalent to the STL list 'erase' method in Java:
			I = Sents.erase(I);
//C++ TO JAVA CONVERTER TODO TASK: There is no direct equivalent to the STL list 'insert' method in Java:
			Sents.insert(I, sb);
		}
		else if (up.get_nodetype() == AST_NODE_TYPE.AST_BFS)
		{
			ast_bfs bfs = (ast_bfs) up;
			if (bfs.get_fbody() == s)
			{
				bfs.set_fbody(sb);
			}
			else if (bfs.get_bbody() == s)
			{
				bfs.set_bbody(sb);
			}
			else
			{
				assert false;
			}
		}
		else if (up.get_nodetype() == AST_NODE_TYPE.AST_WHILE)
		{
			ast_while w = (ast_while) up;
			assert w.get_body() == s;
			w.set_body(sb);
		}
		else
		{
			assert false;
		}
	}
public static void gm_add_sent(ast_sent current, ast_sent target, int location)
{
	gm_add_sent(current, target, location, true);
}

	// add target to the location, at the same level ast current
	// [assert] target is already 'ripped off' correctly (i.e. has NULL enclosing scope)
	// (i.e. the top-most symtab in the target subtree has no predecessor.)
//C++ TO JAVA CONVERTER NOTE: Java does not allow default values for parameters. Overloaded methods are inserted above.
//ORIGINAL LINE: static void gm_add_sent(ast_sent* current, ast_sent* target, int location, boolean need_fix_symtab = true)
	public static void gm_add_sent(ast_sent current, ast_sent target, int location, boolean need_fix_symtab)
	{
		// make sure that current belongs to a sent block
		GlobalMembersGm_transform_helper.gm_make_it_belong_to_sentblock(current, need_fix_symtab);

		ast_sentblock sb = (ast_sentblock) current.get_parent();
		assert sb.get_nodetype() == AST_NODE_TYPE.AST_SENTBLOCK;

		// fix symtab. (add top to the ripped off sentence tree)
		if (need_fix_symtab)
		{
			gm_scope S = new gm_scope();
			sb.get_this_scope(S);

			GlobalMembersGm_transform_helper.gm_put_new_upper_scope_on_null(target, S);
		}

		java.util.LinkedList<ast_sent> sents = sb.get_sents();
		java.util.Iterator<ast_sent> i;
		;
		target.set_parent(sb);
		switch (location)
		{
			case GM_INSERT_BEGIN:
				sents.addFirst(target);
				break;
			case GM_INSERT_END:
				sents.addLast(target);
				break;
			case GM_INSERT_BEFORE:
			case GM_INSERT_AFTER:
				for (i = sents.iterator(); i.hasNext();)
				{
					if (i.next() == current)
						break;
				}
				assert i.hasNext();
				if (location == gm_insert_location_t.GM_INSERT_AFTER.getValue())
//C++ TO JAVA CONVERTER TODO TASK: There is no direct equivalent to the STL list 'insert' method in Java:
				sents.insert(i, target);
				break;
		}
	}

	public static void gm_insert_sent_in_sb(ast_sentblock sb, ast_sent target, int location, boolean need_fix_symtab)
	{
		// assumption: target has NULL enclosing scope
		if (need_fix_symtab)
		{
			gm_scope S = new gm_scope();
			sb.get_this_scope(S);

			GlobalMembersGm_transform_helper.gm_put_new_upper_scope_on_null(target, S);
		}

		java.util.LinkedList<ast_sent> sents = sb.get_sents();
		target.set_parent(sb);
		switch (location)
		{
			case GM_INSERT_BEGIN:
				sents.addFirst(target);
				break;
			case GM_INSERT_END:
				sents.addLast(target);
				break;
			default:
				assert false;
				break;
		}
	}
}