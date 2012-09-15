package backend_cpp;

import frontend.gm_range_type;
import frontend.gm_rw_analysis;
import frontend.gm_rw_analysis_check2;
import frontend.gm_rwinfo;
import frontend.gm_rwinfo_sets;
import frontend.gm_symtab;
import frontend.gm_symtab_entry;
import inc.gm_compile_step;
import inc.gm_type;

import java.util.Iterator;
import java.util.LinkedList;

import tangible.RefObject;
import ast.ast_assign;
import ast.ast_expr;
import ast.ast_field;
import ast.ast_foreach;
import ast.ast_id;
import ast.ast_if;
import ast.ast_node_type;
import ast.ast_procdef;
import ast.ast_sent;
import ast.ast_sentblock;
import ast.ast_typedecl;
import ast.gm_rwinfo_list;
import ast.gm_rwinfo_map;

import common.gm_add_symbol;
import common.gm_main;
import common.gm_new_sents_after_tc;
import common.gm_transform_helper;

public class gm_cpp_opt_defer extends gm_compile_step {

	private gm_cpp_opt_defer() {
		set_description("Handle deferred writes");
	}

	@Override
	public void process(ast_procdef proc) {
		LinkedList<gm_symtab_entry> S = new LinkedList<gm_symtab_entry>();
		LinkedList<ast_foreach> F = new LinkedList<ast_foreach>();
		// return found defer
		boolean b = find_deferred_writes(proc, S, F);
		if (b) {
			post_process_deferred_writes(S, F);
			gm_rw_analysis.gm_redo_rw_analysis(proc.get_body());
		}

		set_affected(b);
	}

	@Override
	public gm_compile_step get_instance() {
		return new gm_cpp_opt_defer();
	}

	public static gm_compile_step get_factory() {
		return new gm_cpp_opt_defer();
	}

	private static boolean find_deferred_writes(ast_procdef proc, LinkedList<gm_symtab_entry> target_syms, LinkedList<ast_foreach> target_foreach) {
		gm_defer_write T = new gm_defer_write();
		T.set_targets(target_syms, target_foreach);
		boolean b = T.find_deferred_writes(proc);
		return b;
	}

	/**
	 * process deferred writes in following ways. <li>add symbol-def for A_new
	 * <li>add initializer<br>
	 * (apply optimization: conditional initializer) <li>add updater
	 */
	private static void post_process_deferred_writes(LinkedList<gm_symtab_entry> target_syms, LinkedList<ast_foreach> target_foreach) {
		assert target_syms.size() == target_foreach.size();
		Iterator<gm_symtab_entry> i = target_syms.iterator();
		Iterator<ast_foreach> j = target_foreach.iterator();
		while (i.hasNext()) {
			ast_foreach fe = j.next();
			gm_symtab_entry old_dest = i.next();
			ast_typedecl type = old_dest.getType();
			ast_id id = old_dest.getId();

			assert type.is_property();
			// [TODO] hack. Think about deferred-write to scalar, later.
			// make sure fe belongs to a sentblock
			gm_transform_helper.gm_make_it_belong_to_sentblock(fe);

			// ---------------------------------------
			// add entry declaration
			// [todo] check name conflict
			// ---------------------------------------
			boolean is_nodeprop = type.is_node_property();
			gm_type target_type = type.getTargetTypeSummary();
			assert target_type.is_prim_type();
			ast_sentblock scope = gm_add_symbol.gm_find_upscope(fe);
			gm_symtab_entry target_graph = type.get_target_graph_sym();

			String fname = gm_main.FE.voca_temp_name_and_add(id.get_orgname(), "_nxt");
			gm_symtab_entry new_dest = gm_add_symbol.gm_add_new_symbol_property(scope, target_type, is_nodeprop, target_graph, fname);
			fname = null;

			// --------------------------------------
			// replace deferred assignment
			// --------------------------------------
			replace_deferred_assignment(fe.get_body(), old_dest, new_dest);

			// ---------------------------------------
			// add initializer (if required)
			// ---------------------------------------
			ast_id src = target_graph.getId();
			assert src.getSymInfo() != null;

			boolean need_initializer = true;
			if (fe.get_iter_type().is_all_graph_iter_type()) {
				gm_rwinfo_sets sets = gm_rw_analysis.gm_get_rwinfo_sets(fe);
				gm_rwinfo_map W = sets.write_set;
				assert W.containsKey(old_dest);
				gm_rwinfo_list L = W.get(old_dest);
				for (gm_rwinfo info : L) {
					if ((info.access_range == gm_range_type.GM_RANGE_LINEAR) && (info.always)) {
						need_initializer = false;
						break;
					}
				}
			}

			ast_foreach init = create_initializer(src.copy(true), is_nodeprop, old_dest, new_dest);

			ast_sent seq_loop = null;
			RefObject<ast_sent> seq_loop_wrapper = new RefObject<ast_sent>(seq_loop);
			boolean check_result = check_conditional_initialize(fe, old_dest, seq_loop_wrapper);
			seq_loop = seq_loop_wrapper.argvalue;
			if (check_result) {
				// ---------------------------------------
				// add conditional initializer
				// Do {
				// ... // X not used
				// Foreach {
				// a.X <= <expr with X>
				// }
				// ... // X not used
				// } While(...)
				//
				// ==>
				//
				// define X_new;
				// _first = true;
				// Do {
				// ... //
				// If (_first) {
				// Init X_new from X;
				// _first = false;
				// }
				// Foreach {
				// a.X_new = <expr with X>
				// }
				// Update X from X_new
				// }
				// ---------------------------------------
				if (need_initializer)
					add_conditional_initialize(seq_loop, fe, init, old_dest, new_dest);
			} else {
				gm_transform_helper.gm_add_sent_before(fe, init);
			}

			// ---------------------------------------
			// add updater
			// ---------------------------------------
			ast_foreach update = create_updater(src.copy(true), is_nodeprop, old_dest, new_dest);
			gm_transform_helper.gm_add_sent_after(fe, update);
		}
	}

	private static ast_foreach create_initializer(ast_id src, boolean is_nodeprop, gm_symtab_entry old_dest, gm_symtab_entry new_dest) {
		return create_init_or_update(src, is_nodeprop, old_dest, new_dest, true);
	}

	private static ast_foreach create_updater(ast_id src, boolean is_nodeprop, gm_symtab_entry old_dest, gm_symtab_entry new_dest) {
		return create_init_or_update(src, is_nodeprop, old_dest, new_dest, false);
	}

	private static void replace_deferred_assignment(ast_sent s, gm_symtab_entry target_old, gm_symtab_entry target_new) {
		gm_replace_da_t T = new gm_replace_da_t();
		T.replace_da(target_old, target_new, s);
	}

	private static boolean check_conditional_initialize(ast_foreach target_fe, gm_symtab_entry target_old, RefObject<ast_sent> seq_loop) {
		// 1. check if target modifies *old_target* linearly and
		// unconditionally.
		// (note: RW-analysis for target_fe has not updated.)
		if (!gm_rw_analysis.gm_is_modified_always_linearly(target_fe, target_old))
			return false;

		// 2. check if target_fe is inside a seq_loop. (unconditionally)
		seq_loop.argvalue = gm_transform_helper.gm_find_enclosing_seq_loop(target_fe);
		if (seq_loop.argvalue == null)
			return false;

		// 3. check if target is not modified elswhere inside the seq_loop scope
		return !check_if_modified_elsewhere(target_old, target_fe, seq_loop.argvalue); // temp
	}

	private static void add_conditional_initialize(ast_sent seq_loop, ast_foreach target_fe, ast_foreach init, gm_symtab_entry target_old,
			gm_symtab_entry target_new) {
		// make sure fe belongs to a sentblock
		gm_transform_helper.gm_make_it_belong_to_sentblock(seq_loop);

		// ------------------------------------------------------
		// move up target_new definition over sequential loop
		// ------------------------------------------------------
		ast_sentblock up = gm_add_symbol.gm_find_upscope(target_fe);
		gm_symtab up_symtab = up.get_symtab_field();
		ast_sentblock upup = gm_add_symbol.gm_find_upscope(seq_loop);
		gm_symtab upup_symtab_f = upup.get_symtab_field();
		gm_add_symbol.gm_move_symbol_into(target_new, up_symtab, upup_symtab_f, false);

		// --------------------------------------------
		// create first-access flag and init into upup
		// { // <- upup-scope
		// Bool is_first;
		// is_first = true;
		// do { ... // <- up-scope
		// Foreach() { ...} // target FE
		// } while
		// }
		// --------------------------------------------
		upup.get_symtab_var(); // to assert it has scope
		String flag_name = gm_main.FE.voca_temp_name_and_add("is_first");
		gm_symtab_entry flag_sym = gm_add_symbol.gm_add_new_symbol_primtype(upup, gm_type.GMTYPE_BOOL, flag_name); // symbol
		ast_id lhs = flag_sym.getId().copy(true);
		ast_expr rhs = ast_expr.new_bval_expr(true);
		ast_assign a_init = ast_assign.new_assign_scala(lhs, rhs); // "is_first = true"
		// no need to fix symtab for assign.
		gm_transform_helper.gm_insert_sent_begin_of_sb(upup, a_init, false);

		// -------------------------------------------
		// create conditional init:
		// if (is_first) {
		// is_first = false;
		// <init>
		// }
		// <target_fe>
		// -------------------------------------------
		ast_expr cond = ast_expr.new_id_expr(flag_sym.getId().copy(true));
		ast_sentblock then_clause = ast_sentblock.new_sentblock();
		ast_if cond_init = ast_if.new_if(cond, then_clause, null);

		lhs = flag_sym.getId().copy(true);
		rhs = ast_expr.new_bval_expr(false);
		ast_assign a_set = ast_assign.new_assign_scala(lhs, rhs);
		then_clause.add_sent(a_set);
		then_clause.add_sent(init);

		// ---------------------------------------------
		// Add cond-init
		// ---------------------------------------------
		gm_transform_helper.gm_add_sent_before(target_fe, cond_init);

		return;
	}

	private static ast_foreach create_init_or_update(ast_id src, boolean is_nodeprop, gm_symtab_entry old_dest, gm_symtab_entry new_dest, boolean is_init) {
		assert src.getSymInfo() != null;

		// -------------------------------
		// create body sentence
		// a.X_new = a.X (init)
		// a.X = a.X_new (update)
		// -------------------------------
		ast_id lhs_driver = ast_id.new_id(null, 0, 0);
		ast_id rhs_driver = ast_id.new_id(null, 0, 0);
		ast_field lhs;
		ast_field rhs;
		if (is_init) {
			ast_id lhs_prop = new_dest.getId().copy(true);
			ast_id rhs_prop = old_dest.getId().copy(true);
			lhs = ast_field.new_field(lhs_driver, lhs_prop);
			rhs = ast_field.new_field(rhs_driver, rhs_prop);
		} else {
			ast_id lhs_prop = old_dest.getId().copy(true);
			ast_id rhs_prop = new_dest.getId().copy(true);
			lhs = ast_field.new_field(lhs_driver, lhs_prop);
			rhs = ast_field.new_field(rhs_driver, rhs_prop);
		}
		ast_expr rhs_expr = ast_expr.new_field_expr(rhs);
		ast_assign a = ast_assign.new_assign_field(lhs, rhs_expr);

		// ------------------------------
		// create foreach statement
		// ------------------------------
		String iter_name = gm_main.FE.voca_temp_name_and_add("i");
		ast_id itor = ast_id.new_id(iter_name, 0, 0);
		gm_type iter_type = is_nodeprop ? gm_type.GMTYPE_NODEITER_ALL : gm_type.GMTYPE_EDGEITER_ALL;
		ast_foreach fe = gm_new_sents_after_tc.gm_new_foreach_after_tc(itor, src, a, iter_type);
		assert itor.getSymInfo() != null;
		iter_name = null;

		// -------------------------------
		// set up symbol info of the body
		// -------------------------------
		lhs_driver.setSymInfo(itor.getSymInfo());
		rhs_driver.setSymInfo(itor.getSymInfo());

		return fe;
	}

	private static boolean check_if_modified_elsewhere(gm_symtab_entry e, ast_sent myself, ast_sent seq_loop) {
		// printf("seq_loop = %p, myself = %p\n", seq_loop, myself);

		if (myself == seq_loop) // not modified elsewhere then my-self
			return false;

		assert myself.get_nodetype() != ast_node_type.AST_IF;

		ast_sent up = (ast_sent) myself.get_parent();
		assert up != null;
		assert up.is_sentence();
		if (up.get_nodetype() == ast_node_type.AST_SENTBLOCK) {
			// todo
			ast_sentblock sb = (ast_sentblock) up;
			LinkedList<ast_sent> sents = sb.get_sents();
			for (ast_sent sent : sents) {
				if (sent == myself)
					continue;
				if (is_modified(sent, e))
					return true;
			}
		}
		// move up one level
		return check_if_modified_elsewhere(e, up, seq_loop);
	}

	private static boolean is_modified(ast_sent S, gm_symtab_entry e) {

		gm_rwinfo_map W = gm_rw_analysis_check2.gm_get_write_set(S);
		for (gm_symtab_entry w_sym : W.keySet()) {
			if (e == w_sym)
				return true;
		}
		return false;
	}
}