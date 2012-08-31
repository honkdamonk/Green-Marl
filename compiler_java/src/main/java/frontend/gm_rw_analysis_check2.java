package frontend;

import ast.AST_NODE_TYPE;
import ast.ast_sent;
import ast.gm_rwinfo_map;

public class gm_rw_analysis_check2 {
		
	// ==================================================================
	// For depenendcy detection
	// ==================================================================
	public static boolean gm_does_intersect(gm_rwinfo_map S1, gm_rwinfo_map S2) {
		return gm_does_intersect(S1, S2, false);
	}

	public static boolean gm_does_intersect(gm_rwinfo_map S1, gm_rwinfo_map S2, boolean regard_mutate_direction) {

		for (gm_symtab_entry e : S1.keySet()) {
			if (S2.containsKey(e)) {
				if (!regard_mutate_direction) {
					return true;
				}
			}

			// access through driver while driver is modified
			/*
			 * if (e->getType()->is_nodeedge()) { gm_rwinfo_map::iterator j;
			 * for(j=S2.begin();j!=S2.end();j++) { gm_symtab_entry* e2 =
			 * i->first; if (!e2->getType()->is_property()) continue;
			 * gm_rwinfo_list* l = i->second; gm_rwinfo_list::iterator J; for(J
			 * = l->begin(); J!= l->end(); J++) { gm_rwinfo* access_info = *J;
			 * if (access_info->driver == e) return true; } } }
			 */
		}

		return false;
	}

	public static gm_rwinfo_map gm_get_write_set(ast_sent S) {
		assert S != null;
		return gm_rw_analysis.get_rwinfo_sets(S).write_set;
	}
	
	public static boolean gm_has_dependency(gm_rwinfo_sets P_SET, gm_rwinfo_sets Q_SET) {

		gm_rwinfo_map P_R = P_SET.read_set;
		gm_rwinfo_map P_W = P_SET.write_set;
		gm_rwinfo_map P_M = P_SET.mutate_set;
		gm_rwinfo_map Q_R = Q_SET.read_set;
		gm_rwinfo_map Q_W = Q_SET.write_set;
		gm_rwinfo_map Q_M = Q_SET.mutate_set;

		// true dependency
		if (gm_does_intersect(P_W, Q_R, false))
			return true;
		// anti dependency
		if (gm_does_intersect(P_R, Q_W, false))
			return true;
		// output dep
		if (gm_does_intersect(P_W, Q_W, false))
			return true;
		// write & muate => dependency
		if (gm_does_intersect(P_W, Q_M, false))
			return true;
		if (gm_does_intersect(P_M, Q_W, false))
			return true;
		// read & mutate => dependency
		if (gm_does_intersect(P_R, Q_M, false))
			return true;
		if (gm_does_intersect(P_M, Q_R, false))
			return true;
		// mutate & mutate => it depends on mutate_direction
		if (gm_does_intersect(P_M, Q_M, true))
			return true;

		return false;
	}
	
	public static boolean gm_has_dependency(ast_sent P, ast_sent Q) {
		assert P.get_nodetype() != AST_NODE_TYPE.AST_VARDECL; // temporary hack

		// ---------------------------------------------------------
		// note: reduced set does not make dependency! (how great!)
		// [todo] consideration of modified set.
		// ---------------------------------------------------------
		gm_rwinfo_sets P_SET = gm_rw_analysis.get_rwinfo_sets(P);
		gm_rwinfo_sets Q_SET = gm_rw_analysis.get_rwinfo_sets(Q);
		return gm_has_dependency(P_SET, Q_SET);
	}
}