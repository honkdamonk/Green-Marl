package backend_cpp;

import java.util.HashSet;
import java.util.LinkedList;

import ast.AST_NODE_TYPE;
import ast.ast_bfs;
import ast.ast_expr;
import ast.ast_expr_builtin;
import ast.ast_extra_info_set;
import ast.ast_id;
import ast.ast_procdef;
import ast.ast_sent;
import ast.gm_rwinfo_list;
import ast.gm_rwinfo_map;

import common.gm_main;
import common.gm_apply;

import frontend.gm_rw_analysis;
import frontend.gm_rwinfo;
import frontend.gm_rwinfo_sets;
import frontend.gm_symtab_entry;

//---------------------------------------------------------------
// Checking for BFS or DFS
//---------------------------------------------------------------
// (1) Check BFS-MAIN
//   Find every bfs in the procedure
//   - save sym entries of variables that are used inside each bfs
//   - give a name to each bfs call-site
// (2) Check BFS Built-In
//   For every bfs
//   - save sym entries of collections that are used inside each bfs
//---------------------------------------------------------------
public class check_bfs_main_t extends gm_apply {

	public ast_bfs current_bfs = null;
	public ast_procdef proc;
	public LinkedList<ast_sent> bfs_lists = new LinkedList<ast_sent>();
	public boolean has_bfs = false;
	public boolean in_bfs = false;

	public check_bfs_main_t(ast_procdef p) {
		this.proc = p;
		set_for_sent(true);
		set_for_expr(true);
		set_separate_post_apply(true);
	}

	public final void process_rwinfo(gm_rwinfo_map MAP, HashSet<Object> SET) {
		for (gm_symtab_entry e : MAP.keySet()) {
			SET.add(e);

			gm_rwinfo_list use = MAP.get(e);
			assert (use != null);
			for (gm_rwinfo rwinfo : use) {
				gm_symtab_entry driver = rwinfo.driver;
				if (driver != null) {
					SET.add(driver);
					ast_id g = driver.getType().get_target_graph_id();
					ast_id c = driver.getType().get_target_collection_id();
					if (g != null)
						SET.add(g.getSymInfo());
					if (c != null)
						SET.add(c.getSymInfo());
				}
			}
		}
	}

	public final boolean apply(ast_sent s) {
		if (s.get_nodetype() == AST_NODE_TYPE.AST_BFS) {
			ast_extra_info_set syms = new ast_extra_info_set();
			java.util.HashSet<Object> S = syms.get_set();

			// insert graph symbol at the first
			gm_symtab_entry graph = ((ast_bfs) s).get_root().getTypeInfo().get_target_graph_sym();

			S.add(graph);

			// are symbols that are read/writen inside bfs
			gm_rwinfo_sets RWINFO = gm_rw_analysis.gm_get_rwinfo_sets(s);
			assert RWINFO != null;

			gm_rwinfo_map RS = RWINFO.read_set;
			gm_rwinfo_map WS = RWINFO.write_set;
			gm_rwinfo_map DS = RWINFO.reduce_set;

			process_rwinfo(RS, S);
			process_rwinfo(WS, S);
			process_rwinfo(DS, S);

			s.add_info(gm_cpp_gen.CPPBE_INFO_BFS_SYMBOLS, syms);
			has_bfs = true;
			ast_bfs bfs = (ast_bfs) s;

			String temp = String.format("%s", proc.get_procname().get_genname());
			String suffix = bfs.is_bfs() ? "_bfs" : "_dfs";
			String c = gm_main.FE.voca_temp_name_and_add(temp, suffix);

			s.add_info_string(gm_cpp_gen.CPPBE_INFO_BFS_NAME, c);

			bfs_lists.addLast(s);

			assert in_bfs == false;
			in_bfs = true;
			current_bfs = (ast_bfs) s;
		}

		return true;
	}

	// [XXX]: should be merged with 'improved RW' analysis (to be done)
	public final boolean apply(ast_expr e) {
		if (!in_bfs)
			return true;
		if (e.is_builtin()) {
			ast_expr_builtin bin = (ast_expr_builtin) e;
			ast_id driver = bin.get_driver();
			if (driver != null) {
				HashSet<Object> SET = ((ast_extra_info_set) current_bfs.find_info(gm_cpp_gen.CPPBE_INFO_BFS_SYMBOLS)).get_set();
				SET.add(driver.getSymInfo());
			}
		}
		return true;
	}

	public final boolean apply2(ast_sent s) {
		if (s.get_nodetype() == AST_NODE_TYPE.AST_BFS) {
			in_bfs = false;
		}
		return true;
	}

}