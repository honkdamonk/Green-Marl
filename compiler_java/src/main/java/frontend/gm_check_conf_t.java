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
import ast.ast_bfs;
import ast.ast_foreach;
import ast.ast_sent;
import ast.gm_rwinfo_list;
import ast.gm_rwinfo_map;

import common.GM_ERRORS_AND_WARNINGS;
import common.gm_error;
import common.gm_apply;

/**
* Traverse a subtree S. Assuming S is executed in parallel.
* Find and report any conflicts in S.
*/
public class gm_check_conf_t extends gm_apply {
	
	boolean is_okay;
	private LinkedList<conf_info_t> Report = new LinkedList<conf_info_t>();

	public gm_check_conf_t() {
		is_okay = true;
		set_for_sent(true);
	}

	@Override
	public boolean apply(ast_sent s) {
		if (s.get_nodetype() == AST_NODE_TYPE.AST_FOREACH) {
			ast_foreach fe = (ast_foreach) s;
			if (fe.is_sequential())
				return true;

			ast_sent body = fe.get_body();

			gm_rwinfo_map R = GlobalMembersGm_rw_analysis.get_rwinfo_sets(fe).read_set; // body
																															// +
																															// filter
			gm_rwinfo_map W = GlobalMembersGm_rw_analysis.get_rwinfo_sets(body).write_set;
			gm_rwinfo_map D = GlobalMembersGm_rw_analysis.get_rwinfo_sets(body).reduce_set;
			gm_rwinfo_map M = GlobalMembersGm_rw_analysis.get_rwinfo_sets(body).mutate_set;

			check_rw_conf_error(R, W, gm_conflict_t.RW_CONFLICT, Report); // R-W																												// (warning)
			check_rw_conf_error(W, W, gm_conflict_t.WW_CONFLICT, Report); // W-W																												// (warning)
			check_rw_conf_error(R, M, gm_conflict_t.RM_CONFLICT, Report); // R-M																												// (warning)
			check_rw_conf_error(M, M, gm_conflict_t.MM_CONFLICT, Report); // M-M
			//(warning)
			is_okay = is_okay && check_rw_conf_error(R, D, gm_conflict_t.RD_CONFLICT, Report); // R-D
			is_okay = is_okay && check_rw_conf_error(W, D, gm_conflict_t.WD_CONFLICT, Report); // W-D
			is_okay = is_okay && check_rw_conf_error(W, M, gm_conflict_t.WM_CONFLICT, Report); // W-M

		} else if (s.get_nodetype() == AST_NODE_TYPE.AST_BFS) {

			// [TODO] consideration for DFS

			ast_bfs bfs = (ast_bfs) s;
			if (bfs.is_sequential())
				return true;

			// ------------------------------------
			// reconstruct read-set filter (WHY?)
			// ------------------------------------
			GMTYPE_T iter_type = bfs.get_iter_type(); // should be
														// GMTYPE_NODEITER_BFS
			gm_symtab_entry it = bfs.get_source().getSymInfo();
			gm_rwinfo_map R_filter = new gm_rwinfo_map();
			if (bfs.get_navigator() != null) {
				range_cond_t R = new range_cond_t(gm_range_type_t.GM_RANGE_LEVEL_DOWN, true);
				GlobalMembersGm_rw_analysis.Default_DriverMap.put(it, R);
				GlobalMembersGm_rw_analysis.traverse_expr_for_readset_adding(bfs.get_navigator(), R_filter);
				GlobalMembersGm_rw_analysis.Default_DriverMap.remove(it);
			}
			if (bfs.get_f_filter() != null) {
				range_cond_t R = new range_cond_t(GlobalMembersGm_rw_analysis.gm_get_range_from_itertype(iter_type), true);
				GlobalMembersGm_rw_analysis.Default_DriverMap.put(it, R);
				GlobalMembersGm_rw_analysis.traverse_expr_for_readset_adding(bfs.get_f_filter(), R_filter);
				GlobalMembersGm_rw_analysis.Default_DriverMap.remove(it);
			}
			if (bfs.get_b_filter() != null) {
				range_cond_t R = new range_cond_t(GlobalMembersGm_rw_analysis.gm_get_range_from_itertype(iter_type), true);
				GlobalMembersGm_rw_analysis.Default_DriverMap.put(it, R);
				GlobalMembersGm_rw_analysis.traverse_expr_for_readset_adding(bfs.get_b_filter(), R_filter);
				GlobalMembersGm_rw_analysis.Default_DriverMap.remove(it);

			}

			// ---------------------------------------------
			// forward body
			// ---------------------------------------------
			if (bfs.get_fbody() != null) {
				ast_sent body = bfs.get_fbody();

				gm_rwinfo_map R = GlobalMembersGm_rw_analysis.get_rwinfo_sets(body).read_set;
				gm_rwinfo_map W = GlobalMembersGm_rw_analysis.get_rwinfo_sets(body).write_set;
				gm_rwinfo_map D = GlobalMembersGm_rw_analysis.get_rwinfo_sets(body).reduce_set;
				gm_rwinfo_map M = GlobalMembersGm_rw_analysis.get_rwinfo_sets(body).mutate_set;

				check_rw_conf_error(R, W, gm_conflict_t.RW_CONFLICT, Report); // R-W																													// (warning)
				check_rw_conf_error(R_filter, W, gm_conflict_t.RW_CONFLICT, Report); // R-W																														// (warning)
				check_rw_conf_error(W, W, gm_conflict_t.WW_CONFLICT, Report); // W-W																													// (warning)
				check_rw_conf_error(R, M, gm_conflict_t.RM_CONFLICT, Report); // R-M																													// (warning)
				check_rw_conf_error(M, M, gm_conflict_t.MM_CONFLICT, Report); // M-M
																													// (warning)
				is_okay = is_okay && check_rw_conf_error(R, D, gm_conflict_t.RD_CONFLICT, Report); // R-D
				is_okay = is_okay && check_rw_conf_error(R_filter, D, gm_conflict_t.RD_CONFLICT, Report); // R-D
				is_okay = is_okay && check_rw_conf_error(W, D, gm_conflict_t.WD_CONFLICT, Report); // W-D
				is_okay = is_okay && check_rw_conf_error(W, M, gm_conflict_t.WM_CONFLICT, Report); // W-M
			}

			// ---------------------------------------------
			// backward body
			// ---------------------------------------------
			if (bfs.get_bbody() != null) {
				ast_sent body = bfs.get_bbody();

				gm_rwinfo_map R = GlobalMembersGm_rw_analysis.get_rwinfo_sets(body).read_set;
				gm_rwinfo_map W = GlobalMembersGm_rw_analysis.get_rwinfo_sets(body).write_set;
				gm_rwinfo_map D = GlobalMembersGm_rw_analysis.get_rwinfo_sets(body).reduce_set;
				gm_rwinfo_map M = GlobalMembersGm_rw_analysis.get_rwinfo_sets(body).mutate_set;

				// printf("R:");gm_print_rwinfo_set(R);
				// printf("D:");gm_print_rwinfo_set(D);

				check_rw_conf_error(R, W, gm_conflict_t.RW_CONFLICT, Report); // R-W																													// (warning)
				check_rw_conf_error(R_filter, W, gm_conflict_t.RW_CONFLICT, Report); // R-W																														// (warning)
				check_rw_conf_error(W, W, gm_conflict_t.WW_CONFLICT, Report); // W-W																													// (warning)
				check_rw_conf_error(R, M, gm_conflict_t.RM_CONFLICT, Report); // R-M																													// (warning)
				check_rw_conf_error(M, M, gm_conflict_t.MM_CONFLICT, Report); // M-M
																													// (warning)
				is_okay = is_okay && check_rw_conf_error(R, D, gm_conflict_t.RD_CONFLICT, Report); // R-D
				is_okay = is_okay && check_rw_conf_error(R_filter, D, gm_conflict_t.RD_CONFLICT, Report); // R-D
				is_okay = is_okay && check_rw_conf_error(W, D, gm_conflict_t.WD_CONFLICT, Report); // W-D
				is_okay = is_okay && check_rw_conf_error(W, M, gm_conflict_t.WM_CONFLICT, Report); // W-M
			}
		}

		return true;
	}
	
	/**
	* [todo] Handling Scalar conflict (need scope info as well)
	* check if two sets may conflict with each other
	* @return is_okay.
	*/
	private static boolean check_rw_conf_error(gm_rwinfo_map S1, gm_rwinfo_map S2, gm_conflict_t conf_type, LinkedList<conf_info_t> Report) {
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
				boolean is_error_or_warn = check_if_conflict(list1, list2, e1_ref, e2_ref, conf_type);
				e1 = e1_ref.argvalue;
				e2 = e2_ref.argvalue;
				if (!is_warning)
					is_okay = is_okay && !is_error_or_warn;

				// find if they report
				if (is_error_or_warn) {
					if (!is_reported(Report, sym1, sym2, conf_type)) {
						add_report(Report, sym1, sym2, conf_type);
						gm_error.gm_conf_error(error_code, sym1, e1.location, e2.location, is_warning);
					}
				}
			}
		}
		return is_okay;
	}
	
	private static boolean is_reported(LinkedList<conf_info_t> errors, gm_symtab_entry t, gm_symtab_entry b, gm_conflict_t y) {
		for (conf_info_t db : errors) {
			if ((db.sym1 == t) && (db.sym2 == b) && (db.conflict_type == y))
				return true;
		}
		return false;
	}

	private static void add_report(LinkedList<conf_info_t> errors, gm_symtab_entry t, gm_symtab_entry b, gm_conflict_t conf_type) {
		conf_info_t T = new conf_info_t();
		T.sym1 = t;
		T.sym2 = b;
		T.conflict_type = conf_type;
		errors.addLast(T);
	}
	
	/**
	* Examine two lists (for the same symbol)
	* @return true if conflict
	* <li>If they have different level, they do not conflict
	* <li>(R-W, W-W) If they have the same driver, they do not conflict
	* <li>(R-D) => If D is 'deferring write', no conflict
	*/
	private static int check_leveled_access(gm_rwinfo e1) {
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

	private static boolean check_if_conflict(gm_rwinfo_list l1, gm_rwinfo_list l2, RefObject<gm_rwinfo> e1_ref, RefObject<gm_rwinfo> e2_ref,
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
				int lev1 = check_leveled_access(e1);
				int lev2 = check_leveled_access(e2);
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

}
