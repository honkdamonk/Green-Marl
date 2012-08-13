package backend_cpp;

import inc.GM_REDUCE_T;

import java.util.LinkedList;

import ast.AST_NODE_TYPE;
import ast.ast_foreach;
import ast.ast_procdef;
import ast.ast_sent;
import ast.gm_rwinfo_list;
import ast.gm_rwinfo_map;

import common.GlobalMembersGm_traverse;
import common.gm_apply;

import frontend.GlobalMembersGm_rw_analysis;
import frontend.gm_bound_set_info;
import frontend.gm_rwinfo;
import frontend.gm_symtab_entry;

//-----------------------------------------------------
// (2-phase update)
// 1. Find deferred writes.
// 2. Process all deferred writes.
// 3. Redo RW analysis
// 4. (apply loop merge?)
//-----------------------------------------------------
/*
 bool gm_cpp_gen::deferred_write(ast_procdef* proc)
 {
 std::list<gm_symtab_entry*> S;
 std::list<ast_foreach*> F;
 bool b = find_deferred_writes(proc, S, F);
 if (b) {
 post_process_deferred_writes(S,F);

 gm_redo_rw_analysis(proc->get_body());
 }

 return true;
 }
 */

//---------------------------------------------
// detect all deferred writes in the code
//---------------------------------------------
public class gm_defer_write extends gm_apply {
	
	protected LinkedList<gm_symtab_entry> target_syms;
	protected LinkedList<ast_foreach> target_foreach;
	protected boolean has_found;

	// do post-apply
	public final boolean apply(ast_sent s) {
		
		if (s.get_nodetype() != AST_NODE_TYPE.AST_FOREACH)
			return true;
		ast_foreach fe = (ast_foreach) s;

		// check if it has any deferred assignments are bound to this FE.
		gm_bound_set_info B = GlobalMembersGm_rw_analysis.gm_get_bound_set_info(fe);
		assert B != null;
		gm_rwinfo_map BSET = B.bound_set;

		for (gm_symtab_entry e : BSET.keySet()) {
			assert e != null;
			gm_rwinfo_list l = BSET.get(e);
			assert l != null;
			boolean is_deferred = false;
			for (gm_rwinfo I : l) {
				if (I.reduce_op == GM_REDUCE_T.GMREDUCE_DEFER) {
					is_deferred = true;
					has_found = true;
					break; // no need to iterate j
				}
			}
			if (is_deferred) {
				target_syms.addLast(e);
				target_foreach.addLast(fe);
			}
		}
		return true;
	}

	public final boolean find_deferred_writes(ast_procdef s) {
		has_found = false;
		GlobalMembersGm_traverse.gm_traverse_sents(s, this, GlobalMembersGm_traverse.GM_POST_APPLY);
		return has_found;
	}

	public final void set_targets(LinkedList<gm_symtab_entry> S, LinkedList<ast_foreach> F) {
		target_syms = S;
		target_foreach = F;
	}

}