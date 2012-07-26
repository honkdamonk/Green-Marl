package frontend;

import static common.GM_ERRORS_AND_WARNINGS.GM_ERROR_MUTATE_MUTATE_CONFLICT;
import static common.GM_ERRORS_AND_WARNINGS.GM_ERROR_READ_MUTATE_CONFLICT;
import static common.GM_ERRORS_AND_WARNINGS.GM_ERROR_READ_REDUCE_CONFLICT;
import static common.GM_ERRORS_AND_WARNINGS.GM_ERROR_READ_WRITE_CONFLICT;
import static common.GM_ERRORS_AND_WARNINGS.GM_ERROR_WRITE_MUTATE_CONFLICT;
import static common.GM_ERRORS_AND_WARNINGS.GM_ERROR_WRITE_REDUCE_CONFLICT;
import static common.GM_ERRORS_AND_WARNINGS.GM_ERROR_WRITE_WRITE_CONFLICT;
import static frontend.gm_conflict_t.MM_CONFLICT;
import static frontend.gm_conflict_t.RD_CONFLICT;
import static frontend.gm_conflict_t.RM_CONFLICT;
import static frontend.gm_conflict_t.RW_CONFLICT;
import static frontend.gm_conflict_t.WM_CONFLICT;
import static frontend.gm_conflict_t.WW_CONFLICT;
import static frontend.gm_range_type_t.GM_RANGE_LEVEL;
import static frontend.gm_range_type_t.GM_RANGE_LEVEL_DOWN;
import static frontend.gm_range_type_t.GM_RANGE_LEVEL_UP;
import inc.GMTYPE_T;
import inc.GM_REDUCE_T;

import java.util.LinkedList;

import tangible.RefObject;
import ast.AST_NODE_TYPE;
import ast.ast_sent;
import ast.gm_rwinfo_list;
import ast.gm_rwinfo_map;

import common.GM_ERRORS_AND_WARNINGS;
import common.GlobalMembersGm_error;

public class GlobalMembersGm_rw_analysis_check2 {
	// C++ TO JAVA CONVERTER NOTE: 'extern' variable declarations are not
	// required in Java:
	// extern HashMap<gm_symtab_entry*, range_cond_t> Default_DriverMap;
	// extern void traverse_expr_for_readset_adding(ast_expr e,
	// gm_rwinfo_map rset,
	// HashMap<gm_symtab_entry, range_cond_t> DrvMap);
	public static boolean is_reported(LinkedList<conf_info_t> errors, gm_symtab_entry t, gm_symtab_entry b, gm_conflict_t y) {
		for (conf_info_t db : errors) {
			if ((db.sym1 == t) && (db.sym2 == b) && (db.conflict_type == y))
				return true;
		}
		return false;
	}

	public static void add_report(LinkedList<conf_info_t> errors, gm_symtab_entry t, gm_symtab_entry b, gm_conflict_t conf_type) {
		conf_info_t T = new conf_info_t();
		T.sym1 = t;
		T.sym2 = b;
		T.conflict_type = conf_type;
		errors.addLast(T);
	}

	// ---------------------------------------------------------
	// [Read-Write conflict]
	// Foreach(t: G.Nodes) {
	// Forach(u: t.Nbrs) {
	// t.C += u.A; // read A (random)
	// }
	// t.A = t.B + 3; // write A (linear) --> Error
	// }
	//
	// Foreach(t: G.Nodes) {
	// Foreach(u: t.Nbrs)
	// u.A <= t.A; // defer
	// t.A = t.B + 3; // write (okay)
	// }
	//
	// BFS(t:G.nodes) {
	// t.B = Sum(u:G.UpNbrs) u.A; // read A (LEV +1)
	// t.A = t.B + 3; // write A (LEV) --> Okay
	// }
	// ---------------------------------------------------------
	// [Write-Write conflict]
	// Foreach(t: G.Nodes) {
	// Forach(u: t.Nbrs) {
	// u.A += t.A + u.B; // write A (random) [-->Error]
	// }
	// }
	//
	// BFS(t:G.nodes) {
	// t.B = Sum(u:G.UpNbrs) u.A; // read A (LEV +1)
	// t.A = t.B + 3; // write A (LEV) --> Okay
	// }
	// ----------------------------------------------------------

	// ----------------------------------------------------------
	// Examine two lists (for the same symbol)
	// returns true if conflict
	// - If they have different level, they do not conflict
	// - (R-W, W-W) If they have the same driver, they do not conflict
	// - (R-D) => If D is 'deferring write', no conflict
	// ----------------------------------------------------------
	public static int check_leveled_access(gm_rwinfo e1) {
		// -----------------------
		// -1: no level
		// 0: lev-1
		// 1: lev
		// 2: lev+1
		// -----------------------
		int lev = -1;
		gm_range_type_t a_range;
		if (e1.driver == null) {
			a_range = e1.access_range;
		} else {
			GMTYPE_T t = e1.driver.getType().get_typeid();
			a_range = GlobalMembersGm_rw_analysis.gm_get_range_from_itertype(t);
		}

		if (a_range == GM_RANGE_LEVEL)
			lev = 1;
		else if (a_range == GM_RANGE_LEVEL_UP)
			lev = 2;
		else if (a_range == GM_RANGE_LEVEL_DOWN)
			lev = 0;

		return lev;
	}

	public static boolean check_if_conflict(gm_rwinfo_list l1, gm_rwinfo_list l2, RefObject<gm_rwinfo> e1_ref, RefObject<gm_rwinfo> e2_ref,
			gm_conflict_t conf_type) {
		java.util.Iterator<gm_rwinfo> i1;
		java.util.Iterator<gm_rwinfo> i2;
		gm_rwinfo e1 = null;
		gm_rwinfo e2 = null;
		for (i1 = l1.iterator(); i1.hasNext();) {
			e1 = i1.next();
			for (i2 = l2.iterator(); i2.hasNext();) {
				e2 = i2.next();
				// check if different level
				int lev1 = GlobalMembersGm_rw_analysis_check2.check_leveled_access(e1);
				int lev2 = GlobalMembersGm_rw_analysis_check2.check_leveled_access(e2);
				if ((lev1 >= 0) && (lev2 >= 0) && (lev1 != lev2)) // different
																	// level
					continue;

				if ((conf_type == RW_CONFLICT) || (conf_type == WW_CONFLICT) || (conf_type == RM_CONFLICT) || (conf_type == WM_CONFLICT)) {
					if ((e1.driver != null) && (e1.driver == e2.driver))
						continue;
				}
				if (conf_type == RD_CONFLICT) {
					if (e2.reduce_op == GM_REDUCE_T.GMREDUCE_DEFER)
						continue;
					System.out.printf("%d lev1 = %d, %d lev2 = %d\n", e1.access_range, lev1, e2.access_range, lev2);
					assert false;
				}
				if (conf_type == MM_CONFLICT) {
					if (e1.mutate_direction == e2.mutate_direction)
						continue;
				}

				// printf("lev1 = %d, lev2 = %d\n", lev1, lev2);
				e1_ref.argvalue = e1;
				e2_ref.argvalue = e2;
				return true; // found conflict!
			}
		}
		e1_ref.argvalue = e1;
		e2_ref.argvalue = e2;
		return false; // no conflict
	}

	// ------------------------------------------------------------
	// [todo] Handling Scalar conflict (need scope info as well)
	// check if two sets may conflict with each other
	// return is_okay.
	// ------------------------------------------------------------
	public static boolean check_rw_conf_error(gm_rwinfo_map S1, gm_rwinfo_map S2, gm_conflict_t conf_type, LinkedList<conf_info_t> Report) {
		boolean is_okay = true;
		boolean is_warning;
		GM_ERRORS_AND_WARNINGS error_code;
		switch (conf_type) {
		case RW_CONFLICT:
			error_code = GM_ERROR_READ_WRITE_CONFLICT;
			is_warning = true;
			break;
		case WW_CONFLICT:
			error_code = GM_ERROR_WRITE_WRITE_CONFLICT;
			is_warning = true;
			break;
		case RD_CONFLICT:
			error_code = GM_ERROR_READ_REDUCE_CONFLICT;
			is_warning = false;
			break;
		case WD_CONFLICT:
			error_code = GM_ERROR_WRITE_REDUCE_CONFLICT;
			is_warning = false;
			break;
		case RM_CONFLICT:
			error_code = GM_ERROR_READ_MUTATE_CONFLICT;
			is_warning = true;
			break;
		case WM_CONFLICT:
			error_code = GM_ERROR_WRITE_MUTATE_CONFLICT;
			is_warning = false;
			break;
		case MM_CONFLICT:
			error_code = GM_ERROR_MUTATE_MUTATE_CONFLICT;
			is_warning = true;
			break;
		default:
			assert false;
			throw new AssertionError();
		}

		for (gm_symtab_entry sym1 : S1.keySet()) {
			gm_rwinfo_list list1 = S1.get(sym1);
			gm_rwinfo e1 = null;

			// Damn o.O if (!sym1->getType()->is_property()) continue; // todo
			// 'scalar' check

			for (gm_symtab_entry sym2 : S2.keySet()) {
				gm_rwinfo_list list2 = S2.get(sym2);
				gm_rwinfo e2 = null;

				// find same symbol
				if (sym1 != sym2)
					continue;

				// find if they conflict
				RefObject<gm_rwinfo> e1_ref = new RefObject<gm_rwinfo>(e1);
				RefObject<gm_rwinfo> e2_ref = new RefObject<gm_rwinfo>(e2);
				boolean is_error_or_warn = GlobalMembersGm_rw_analysis_check2.check_if_conflict(list1, list2, e1_ref, e2_ref, conf_type);
				e1 = e1_ref.argvalue;
				e2 = e2_ref.argvalue;
				if (!is_warning)
					is_okay = is_okay && !is_error_or_warn;

				// find if they report
				if (is_error_or_warn) {
					if (!GlobalMembersGm_rw_analysis_check2.is_reported(Report, sym1, sym2, conf_type)) {
						GlobalMembersGm_rw_analysis_check2.add_report(Report, sym1, sym2, conf_type);
						GlobalMembersGm_error.gm_conf_error(error_code, sym1, e1.location, e2.location, is_warning);
					}
				}
			}
		}
		return is_okay;
	}

	public static boolean gm_check_parall_conflict_error(ast_sent b) {
		gm_check_conf_t T = new gm_check_conf_t();
		b.traverse_post(T); // post apply
		return T.is_okay;
	}

	// ==================================================================
	// For depenendcy detection
	// ==================================================================
	public static boolean gm_does_intersect(gm_rwinfo_map S1, gm_rwinfo_map S2) {
		return gm_does_intersect(S1, S2, false);
	}

	public static boolean gm_does_intersect(gm_rwinfo_map S1, gm_rwinfo_map S2, boolean regard_mutate_direction) {

		for (gm_symtab_entry e : S1.keySet()) {
			if (S2.containsKey(e)) {
				if (regard_mutate_direction) {
					if (e.find_info_int("GM_BLTIN_INFO_MUTATING") != S2.find(e).next().getKey().find_info_int("GM_BLTIN_INFO_MUTATING")) {
						return true;
					}
				} else {
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

	public static boolean gm_has_dependency(ast_sent P, ast_sent Q) {
		assert P.get_nodetype() != AST_NODE_TYPE.AST_VARDECL; // temporary hack

		// ---------------------------------------------------------
		// note: reduced set does not make dependency! (how great!)
		// [todo] consideration of modified set.
		// ---------------------------------------------------------
		gm_rwinfo_sets P_SET = GlobalMembersGm_rw_analysis.get_rwinfo_sets(P);
		gm_rwinfo_sets Q_SET = GlobalMembersGm_rw_analysis.get_rwinfo_sets(Q);
		return GlobalMembersGm_rw_analysis_check2.gm_has_dependency(P_SET, Q_SET);
	}

	public static boolean gm_has_dependency(gm_rwinfo_sets P_SET, gm_rwinfo_sets Q_SET) {

		gm_rwinfo_map P_R = new gm_rwinfo_map(P_SET.read_set);
		gm_rwinfo_map P_W = new gm_rwinfo_map(P_SET.write_set);
		gm_rwinfo_map P_M = new gm_rwinfo_map(P_SET.mutate_set);
		gm_rwinfo_map Q_R = new gm_rwinfo_map(Q_SET.read_set);
		gm_rwinfo_map Q_W = new gm_rwinfo_map(Q_SET.write_set);
		gm_rwinfo_map Q_M = new gm_rwinfo_map(Q_SET.mutate_set);

		// true dependency
		if (GlobalMembersGm_rw_analysis_check2.gm_does_intersect(P_W, Q_R, false))
			return true;
		// anti dependency
		if (GlobalMembersGm_rw_analysis_check2.gm_does_intersect(P_R, Q_W, false))
			return true;
		// output dep
		if (GlobalMembersGm_rw_analysis_check2.gm_does_intersect(P_W, Q_W, false))
			return true;
		// write & muate => dependency
		if (GlobalMembersGm_rw_analysis_check2.gm_does_intersect(P_W, Q_M, false))
			return true;
		if (GlobalMembersGm_rw_analysis_check2.gm_does_intersect(P_M, Q_W, false))
			return true;
		// read & mutate => dependency
		if (GlobalMembersGm_rw_analysis_check2.gm_does_intersect(P_R, Q_M, false))
			return true;
		if (GlobalMembersGm_rw_analysis_check2.gm_does_intersect(P_M, Q_R, false))
			return true;
		// mutate & mutate => it depends on mutate_direction
		if (GlobalMembersGm_rw_analysis_check2.gm_does_intersect(P_M, Q_M, true))
			return true;

		return false;
	}

	public static gm_rwinfo_map gm_get_reduce_set(ast_sent S) {
		assert S != null;
		return GlobalMembersGm_rw_analysis.get_rwinfo_sets(S).reduce_set;
	}

	public static gm_rwinfo_map gm_get_write_set(ast_sent S) {
		assert S != null;
		return GlobalMembersGm_rw_analysis.get_rwinfo_sets(S).write_set;
	}

	public static boolean gm_is_modified(ast_sent S, gm_symtab_entry e) {

		gm_rwinfo_map W = GlobalMembersGm_rw_analysis_check2.gm_get_write_set(S);
		for (gm_symtab_entry w_sym : W.keySet()) {
			if (e == w_sym)
				return true;
		}
		return false;
	}

	public static boolean gm_is_modified_with_condition(ast_sent S, gm_symtab_entry e, gm_rwinfo_query Q) {
		assert Q != null;
		gm_rwinfo_map W = GlobalMembersGm_rw_analysis_check2.gm_get_write_set(S);
		for (gm_symtab_entry w_sym : W.keySet()) {
			if (e != w_sym)
				continue;

			// find exact match
			gm_rwinfo_list list = W.get(w_sym);
			for (gm_rwinfo R : list) {
				if (Q._check_range && (Q.range != R.access_range)) {
					continue;
				}
				if (Q._check_driver && (Q.driver != R.driver)) {
					continue;
				}
				if (Q._check_always && (Q.always != R.always)) {
					continue;
				}
				if (Q._check_reduceop && (Q.reduce_op != R.reduce_op)) {
					continue;
				}
				if (Q._check_bound && (Q.bound != R.bound_symbol)) {
					continue;
				}
				return true; // exact match
			}
			return false; // no exact match
		}

		return false;
	}

	// -----------------------------------------------------
	// For debug
	// -----------------------------------------------------
	public static void gm_print_rwinfo_set(gm_rwinfo_map m) {
		boolean first = true;
		for (gm_symtab_entry e : m.keySet()) {
			gm_rwinfo_list l = m.get(e);
			if (first)
				first = false;
			else
				System.out.print(",");

			if (e.getType().is_property())
				System.out.printf("{%s(%s):", e.getId().get_orgname(), e.getType().get_target_graph_id().get_orgname());
			else
				System.out.printf("{%s:", e.getId().get_orgname());

			boolean _first = true;
			for (gm_rwinfo info : l) {
				if (_first)
					_first = false;
				else
					System.out.print(",");
				info.print();
			}
			System.out.print("}");
		}
		System.out.print("\n");
	}
}