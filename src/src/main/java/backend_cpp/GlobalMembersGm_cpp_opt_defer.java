package backend_cpp;

import ast.AST_NODE_TYPE;
import ast.ast_assign;
import ast.ast_expr;
import ast.ast_field;
import ast.ast_foreach;
import ast.ast_id;
import ast.ast_if;
import ast.ast_procdef;
import ast.ast_sent;
import ast.ast_sentblock;
import ast.ast_typedecl;
import frontend.GlobalMembersGm_rw_analysis;
import frontend.GlobalMembersGm_rw_analysis_check2;
import frontend.gm_range_type_t;
import frontend.gm_rwinfo;
import frontend.gm_rwinfo_sets;
import frontend.gm_symtab;
import frontend.gm_symtab_entry;
import inc.GMTYPE_T;
import inc.GlobalMembersGm_defs;

import common.GlobalMembersGm_add_symbol;
import common.GlobalMembersGm_main;
import common.GlobalMembersGm_new_sents_after_tc;
import common.GlobalMembersGm_transform_helper;

public class GlobalMembersGm_cpp_opt_defer
{
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define TO_STR(X) #X
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define DEF_STRING(X) static const char *X = "X"
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define GM_COMPILE_STEP(CLASS, DESC) class CLASS : public gm_compile_step { private: CLASS() {set_description(DESC);}public: virtual void process(ast_procdef*p); virtual gm_compile_step* get_instance(){return new CLASS();} static gm_compile_step* get_factory(){return new CLASS();} };
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define GM_COMPILE_STEP_FACTORY(CLASS) CLASS::get_factory()

	public static boolean find_deferred_writes(ast_procdef proc, java.util.LinkedList<gm_symtab_entry> target_syms, java.util.LinkedList<ast_foreach> target_foreach)
	{
		gm_defer_write T = new gm_defer_write();
		T.set_targets(target_syms, target_foreach);
		boolean b = T.find_deferred_writes(proc);
		return b;
	}

//-----------------------------------------------------------------------
// process deferred writes in following ways.
//   - add symbol-def for A_new
//   - add initializer
//     (apply optimization: conditional initializer)
//   - add updater
//-----------------------------------------------------------------------
	public static void post_process_deferred_writes(java.util.LinkedList<gm_symtab_entry> target_syms, java.util.LinkedList<ast_foreach> target_foreach)
	{
		assert target_syms.size() == target_foreach.size();
		java.util.Iterator<gm_symtab_entry> i = target_syms.iterator();
		java.util.Iterator<ast_foreach> j = target_foreach.iterator();
		for (; i.hasNext(); i++, j++)
		{

			gm_symtab_entry old_dest = i.next();
			ast_typedecl type = (i.next()).getType();
			ast_id id = (i.next()).getId();
			ast_foreach fe = j.next();

			assert type.is_property();
			// [TODO] hack. Think about deferred-write to scalar, later.
			GlobalMembersGm_transform_helper.gm_make_it_belong_to_sentblock(fe); // make sure fe belongs to a sentblock

			//---------------------------------------
			// add entry declaration
			// [todo] check name conflict
			//---------------------------------------
			boolean is_nodeprop = type.is_node_property();
			int target_type = type.getTargetTypeSummary();
			assert GlobalMembersGm_defs.gm_is_prim_type(target_type);
			ast_sentblock scope = GlobalMembersGm_add_symbol.gm_find_upscope(fe);
			gm_symtab_entry target_graph = type.get_target_graph_sym();

			byte[] fname = (String) GlobalMembersGm_main.FE.voca_temp_name_and_add(id.get_orgname(), "_nxt");
		tangible.RefObject<String> tempRef_fname = new tangible.RefObject<String>(fname);
			gm_symtab_entry new_dest = GlobalMembersGm_add_symbol.gm_add_new_symbol_property(scope, target_type, is_nodeprop, target_graph, tempRef_fname);
			fname = tempRef_fname.argvalue;
			fname = null;

			//--------------------------------------
			// replace deferred assignment
			//--------------------------------------
			GlobalMembersGm_cpp_opt_defer.replace_deferred_assignment(fe.get_body(), old_dest, new_dest);

			//---------------------------------------
			// add initializer (if required)
			//---------------------------------------
			ast_id src = target_graph.getId();
			assert src.getSymInfo() != null;

			boolean need_initializer = true;
			if (GlobalMembersGm_defs.gm_is_all_graph_iter_type(fe.get_iter_type()))
			{
				gm_rwinfo_sets sets = GlobalMembersGm_rw_analysis.gm_get_rwinfo_sets(fe);
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy constructor call - this should be verified and a copy constructor should be created if it does not yet exist:
//ORIGINAL LINE: java.util.HashMap<gm_symtab_entry*, java.util.LinkedList<gm_rwinfo*>*>& W = sets->write_set;
				java.util.HashMap<gm_symtab_entry, java.util.LinkedList<gm_rwinfo>> W = new java.util.HashMap(sets.write_set);
				assert W.containsKey(old_dest);
				java.util.LinkedList<gm_rwinfo> L = W.get(old_dest);
				java.util.Iterator<gm_rwinfo> I;
				for (I = L.iterator(); I.hasNext();)
				{
					gm_rwinfo info = I.next();
					if ((info.access_range == gm_range_type_t.GM_RANGE_LINEAR.getValue()) && (info.always))
					{
						need_initializer = false;
						break;
					}
				}
			}

			ast_foreach init = GlobalMembersGm_cpp_opt_defer.create_initializer(src.copy(true), is_nodeprop, old_dest, new_dest);

			ast_sent seq_loop = null;
			if (GlobalMembersGm_cpp_opt_defer.check_conditional_initialize(fe, old_dest, seq_loop))
			{
				//---------------------------------------
				// add conditional initializer
				// Do {
				//    ... // X not used
				//    Foreach {
				//      a.X <= <expr with X>
				//    }
				//    ... // X not used
				// } While(...)
				//
				// ==>
				//
				// define X_new;
				// _first = true;
				// Do {
				//   ... //
				//   If (_first) {
				//      Init X_new from X;
				//      _first = false;
				//   }
				//   Foreach {
				//      a.X_new = <expr with X>
				//   }
				//   Update X from X_new
				// }
				//---------------------------------------
				if (need_initializer)
					GlobalMembersGm_cpp_opt_defer.add_conditional_initialize(seq_loop, fe, init, old_dest, new_dest);
			}
			else
			{
				GlobalMembersGm_transform_helper.gm_add_sent_before(fe, init);
			}

			//---------------------------------------
			// add updater
			//---------------------------------------
			ast_foreach update = GlobalMembersGm_cpp_opt_defer.create_updater(src.copy(true), is_nodeprop, old_dest, new_dest);
			GlobalMembersGm_transform_helper.gm_add_sent_after(fe, update);
		}
	}

	public static ast_foreach create_initializer(ast_id src, boolean is_nodeprop, gm_symtab_entry old_dest, gm_symtab_entry new_dest)
	{
		return GlobalMembersGm_cpp_opt_defer.create_init_or_update(src, is_nodeprop, old_dest, new_dest, true);
	}
	public static ast_foreach create_updater(ast_id src, boolean is_nodeprop, gm_symtab_entry old_dest, gm_symtab_entry new_dest)
	{
		return GlobalMembersGm_cpp_opt_defer.create_init_or_update(src, is_nodeprop, old_dest, new_dest, false);
	}
	public static void replace_deferred_assignment(ast_sent s, gm_symtab_entry target_old, gm_symtab_entry target_new)
	{
		gm_replace_da_t T = new gm_replace_da_t();
		T.replace_da(target_old, target_new, s);
	}
	public static boolean check_conditional_initialize(ast_foreach target_fe, gm_symtab_entry target_old, ast_sent seq_loop)
	{
		// 1. check if target modifies *old_target* linearly and unconditionally.
		// (note: RW-analysis for target_fe has not updated.)
		if (!GlobalMembersGm_rw_analysis.gm_is_modified_always_linearly(target_fe, target_old))
			return false;

		// 2. check if target_fe is inside a seq_loop. (unconditionally)
		seq_loop = GlobalMembersGm_transform_helper.gm_find_enclosing_seq_loop(target_fe);
		if (seq_loop == null)
			return false;

		// 3. check if target is not modified elswhere inside the seq_loop scope
		return !GlobalMembersGm_cpp_opt_defer.check_if_modified_elsewhere(target_old, target_fe, seq_loop); // temp
	}
	public static void add_conditional_initialize(ast_sent seq_loop, ast_foreach target_fe, ast_foreach init, gm_symtab_entry target_old, gm_symtab_entry target_new)
	{

		GlobalMembersGm_transform_helper.gm_make_it_belong_to_sentblock(seq_loop); // make sure fe belongs to a sentblock

		//------------------------------------------------------
		// move up target_new definition over sequential loop
		//------------------------------------------------------
		ast_sentblock up = GlobalMembersGm_add_symbol.gm_find_upscope(target_fe);
		gm_symtab up_symtab = up.get_symtab_field();
		ast_sentblock upup = GlobalMembersGm_add_symbol.gm_find_upscope(seq_loop);
		gm_symtab upup_symtab_f = upup.get_symtab_field();
		GlobalMembersGm_add_symbol.gm_move_symbol_into(target_new, up_symtab, upup_symtab_f, false);

		//--------------------------------------------
		// create first-access flag and init into upup
		// { // <- upup-scope
		//   Bool is_first;
		//   is_first = true;
		//   do { ...  // <- up-scope
		//      Foreach() { ...} // target FE
		//   } while
		// }
		//--------------------------------------------
		gm_symtab upup_symtab_v = upup.get_symtab_var();
		String flag_name = GlobalMembersGm_main.FE.voca_temp_name_and_add("is_first");
		gm_symtab_entry flag_sym = GlobalMembersGm_add_symbol.gm_add_new_symbol_primtype(upup, GMTYPE_T.GMTYPE_BOOL, (String) flag_name); // symbol
		ast_id lhs = flag_sym.getId().copy(true);
		ast_expr rhs = ast_expr.new_bval_expr(true);
		ast_assign a_init = ast_assign.new_assign_scala(lhs, rhs); // "is_first = true"
		GlobalMembersGm_transform_helper.gm_insert_sent_begin_of_sb(upup, a_init, false); // no need to fix symtab for assign.

		//-------------------------------------------
		// create conditional init:
		//     if (is_first) {
		//        is_first = false;
		//        <init>
		//     }
		//     <target_fe>
		//-------------------------------------------
		ast_expr cond = ast_expr.new_id_expr(flag_sym.getId().copy(true));
		ast_sentblock then_clause = ast_sentblock.new_sentblock();
		ast_if cond_init = ast_if.new_if(cond, then_clause, null);

		lhs = flag_sym.getId().copy(true);
		rhs = ast_expr.new_bval_expr(false);
		ast_assign a_set = ast_assign.new_assign_scala(lhs, rhs);
		then_clause.add_sent(a_set);
		then_clause.add_sent(init);

		//---------------------------------------------
		// Add cond-init
		//---------------------------------------------
		GlobalMembersGm_transform_helper.gm_add_sent_before(target_fe, cond_init);

		return;
	}

	public static ast_foreach create_init_or_update(ast_id src, boolean is_nodeprop, gm_symtab_entry old_dest, gm_symtab_entry new_dest, boolean is_init)
	{
		assert src.getSymInfo() != null;

		//-------------------------------
		// create body sentence 
		//    a.X_new = a.X (init)
		//    a.X = a.X_new (update)
		//-------------------------------
		ast_id lhs_driver = ast_id.new_id(null, 0, 0);
		ast_id rhs_driver = ast_id.new_id(null, 0, 0);
		ast_field lhs;
		ast_field rhs;
		if (is_init)
		{
			ast_id lhs_prop = new_dest.getId().copy(true);
			ast_id rhs_prop = old_dest.getId().copy(true);
			lhs = ast_field.new_field(lhs_driver, lhs_prop);
			rhs = ast_field.new_field(rhs_driver, rhs_prop);
		}
		else
		{
			ast_id lhs_prop = old_dest.getId().copy(true);
			ast_id rhs_prop = new_dest.getId().copy(true);
			lhs = ast_field.new_field(lhs_driver, lhs_prop);
			rhs = ast_field.new_field(rhs_driver, rhs_prop);
		}
		ast_expr rhs_expr = ast_expr.new_field_expr(rhs);
		ast_assign a = ast_assign.new_assign_field(lhs, rhs_expr);

		//------------------------------
		// create foreach statement
		//------------------------------
		String iter_name = GlobalMembersGm_main.FE.voca_temp_name_and_add("i");
		ast_id itor = ast_id.new_id(iter_name, 0, 0);
		int iter_type = is_nodeprop ? GMTYPE_T.GMTYPE_NODEITER_ALL : GMTYPE_T.GMTYPE_EDGEITER_ALL;
		ast_foreach fe = GlobalMembersGm_new_sents_after_tc.gm_new_foreach_after_tc(itor, src, a, iter_type);
		assert itor.getSymInfo() != null;
		iter_name = null;

		//-------------------------------
		// set up symbol info of the body
		//-------------------------------
		lhs_driver.setSymInfo(itor.getSymInfo());
		rhs_driver.setSymInfo(itor.getSymInfo());

		return fe;
	}

	public static boolean check_if_modified_elsewhere(gm_symtab_entry e, ast_sent myself, ast_sent seq_loop)
	{
		//printf("seq_loop = %p, myself = %p\n", seq_loop, myself);

		if (myself == seq_loop) // not modified elsewhere then my-self
			return false;

		assert myself.get_nodetype() != AST_NODE_TYPE.AST_IF;

		ast_sent up = (ast_sent) myself.get_parent();
		assert up != null;
		assert up.is_sentence();
		if (up.get_nodetype() == AST_NODE_TYPE.AST_SENTBLOCK)
		{
			// todo
			ast_sentblock sb = (ast_sentblock) up;
			java.util.LinkedList<ast_sent> sents = sb.get_sents();
			java.util.Iterator<ast_sent> I;
			for (I = sents.iterator(); I.hasNext();)
			{
				if (I.next() == myself)
					continue;
				if (GlobalMembersGm_rw_analysis_check2.gm_is_modified(I.next(), e))
					return true;
			}
		}

		return GlobalMembersGm_cpp_opt_defer.check_if_modified_elsewhere(e, up, seq_loop); // move up one-level

	}
}