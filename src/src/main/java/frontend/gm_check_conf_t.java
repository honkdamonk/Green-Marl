package frontend;

import inc.GMTYPE_T;

import java.util.HashMap;
import java.util.LinkedList;

import ast.AST_NODE_TYPE;
import ast.ast_bfs;
import ast.ast_foreach;
import ast.ast_sent;

import common.gm_apply;

//-----------------------------------------------------------------------
// Traverse a subtree S. Assuming S is executed in parallel.
// Find and report any conflicts in S.
//-----------------------------------------------------------------------
public class gm_check_conf_t extends gm_apply {

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

			HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> R = GlobalMembersGm_rw_analysis.get_rwinfo_sets(fe).read_set; // body
																															// +
																															// filter
			HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> W = GlobalMembersGm_rw_analysis.get_rwinfo_sets(body).write_set;
			HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> D = GlobalMembersGm_rw_analysis.get_rwinfo_sets(body).reduce_set;
			HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> M = GlobalMembersGm_rw_analysis.get_rwinfo_sets(body).mutate_set;

			GlobalMembersGm_rw_analysis_check2.check_rw_conf_error(R, W, gm_conflict_t.RW_CONFLICT, Report); // R-W
																												// (warning)
			GlobalMembersGm_rw_analysis_check2.check_rw_conf_error(W, W, gm_conflict_t.WW_CONFLICT, Report); // W-W
																												// (warning)
			GlobalMembersGm_rw_analysis_check2.check_rw_conf_error(R, M, gm_conflict_t.RM_CONFLICT, Report); // R-M
																												// (warning)
			GlobalMembersGm_rw_analysis_check2.check_rw_conf_error(M, M, gm_conflict_t.MM_CONFLICT, Report); // M-M
																												// (warning)
			is_okay = is_okay && GlobalMembersGm_rw_analysis_check2.check_rw_conf_error(R, D, gm_conflict_t.RD_CONFLICT, Report); // R-D
			is_okay = is_okay && GlobalMembersGm_rw_analysis_check2.check_rw_conf_error(W, D, gm_conflict_t.WD_CONFLICT, Report); // W-D
			is_okay = is_okay && GlobalMembersGm_rw_analysis_check2.check_rw_conf_error(W, M, gm_conflict_t.WM_CONFLICT, Report); // W-M

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
			HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> R_filter = new HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>>();
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

				HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> R = GlobalMembersGm_rw_analysis.get_rwinfo_sets(body).read_set;
				HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> W = GlobalMembersGm_rw_analysis.get_rwinfo_sets(body).write_set;
				HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> D = GlobalMembersGm_rw_analysis.get_rwinfo_sets(body).reduce_set;
				HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> M = GlobalMembersGm_rw_analysis.get_rwinfo_sets(body).mutate_set;

				GlobalMembersGm_rw_analysis_check2.check_rw_conf_error(R, W, gm_conflict_t.RW_CONFLICT, Report); // R-W
																													// (warning)
				GlobalMembersGm_rw_analysis_check2.check_rw_conf_error(R_filter, W, gm_conflict_t.RW_CONFLICT, Report); // R-W
																														// (warning)
				GlobalMembersGm_rw_analysis_check2.check_rw_conf_error(W, W, gm_conflict_t.WW_CONFLICT, Report); // W-W
																													// (warning)
				GlobalMembersGm_rw_analysis_check2.check_rw_conf_error(R, M, gm_conflict_t.RM_CONFLICT, Report); // R-M
																													// (warning)
				GlobalMembersGm_rw_analysis_check2.check_rw_conf_error(M, M, gm_conflict_t.MM_CONFLICT, Report); // M-M
																													// (warning)
				is_okay = is_okay && GlobalMembersGm_rw_analysis_check2.check_rw_conf_error(R, D, gm_conflict_t.RD_CONFLICT, Report); // R-D
				is_okay = is_okay && GlobalMembersGm_rw_analysis_check2.check_rw_conf_error(R_filter, D, gm_conflict_t.RD_CONFLICT, Report); // R-D
				is_okay = is_okay && GlobalMembersGm_rw_analysis_check2.check_rw_conf_error(W, D, gm_conflict_t.WD_CONFLICT, Report); // W-D
				is_okay = is_okay && GlobalMembersGm_rw_analysis_check2.check_rw_conf_error(W, M, gm_conflict_t.WM_CONFLICT, Report); // W-M
			}

			// ---------------------------------------------
			// backward body
			// ---------------------------------------------
			if (bfs.get_bbody() != null) {
				ast_sent body = bfs.get_bbody();

				HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> R = GlobalMembersGm_rw_analysis.get_rwinfo_sets(body).read_set;
				HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> W = GlobalMembersGm_rw_analysis.get_rwinfo_sets(body).write_set;
				HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> D = GlobalMembersGm_rw_analysis.get_rwinfo_sets(body).reduce_set;
				HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> M = GlobalMembersGm_rw_analysis.get_rwinfo_sets(body).mutate_set;

				// printf("R:");gm_print_rwinfo_set(R);
				// printf("D:");gm_print_rwinfo_set(D);

				GlobalMembersGm_rw_analysis_check2.check_rw_conf_error(R, W, gm_conflict_t.RW_CONFLICT, Report); // R-W
																													// (warning)
				GlobalMembersGm_rw_analysis_check2.check_rw_conf_error(R_filter, W, gm_conflict_t.RW_CONFLICT, Report); // R-W
																														// (warning)
				GlobalMembersGm_rw_analysis_check2.check_rw_conf_error(W, W, gm_conflict_t.WW_CONFLICT, Report); // W-W
																													// (warning)
				GlobalMembersGm_rw_analysis_check2.check_rw_conf_error(R, M, gm_conflict_t.RM_CONFLICT, Report); // R-M
																													// (warning)
				GlobalMembersGm_rw_analysis_check2.check_rw_conf_error(M, M, gm_conflict_t.MM_CONFLICT, Report); // M-M
																													// (warning)
				is_okay = is_okay && GlobalMembersGm_rw_analysis_check2.check_rw_conf_error(R, D, gm_conflict_t.RD_CONFLICT, Report); // R-D
				is_okay = is_okay && GlobalMembersGm_rw_analysis_check2.check_rw_conf_error(R_filter, D, gm_conflict_t.RD_CONFLICT, Report); // R-D
				is_okay = is_okay && GlobalMembersGm_rw_analysis_check2.check_rw_conf_error(W, D, gm_conflict_t.WD_CONFLICT, Report); // W-D
				is_okay = is_okay && GlobalMembersGm_rw_analysis_check2.check_rw_conf_error(W, M, gm_conflict_t.WM_CONFLICT, Report); // W-M
			}
		}

		return true;
	}

	public boolean is_okay;

	private LinkedList<conf_info_t> Report = new LinkedList<conf_info_t>();

}
// =========================================================
// called from gm_typecheck.cc
// =========================================================

