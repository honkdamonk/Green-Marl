package frontend;

import static common.gm_errors_and_warnings.GM_ERROR_DOUBLE_BOUND_ITOR;
import static common.gm_errors_and_warnings.GM_ERROR_DOUBLE_BOUND_OP;
import inc.gm_reduce;

import java.util.LinkedList;

import tangible.RefObject;
import ast.ast_bfs;
import ast.ast_foreach;
import ast.ast_id;
import ast.ast_node;
import ast.ast_node_type;
import ast.gm_rwinfo_list;
import ast.gm_rwinfo_map;

import common.gm_apply;
import common.gm_error;

public class gm_check_reduce_error_t extends gm_apply {

	public boolean is_okay = true;
	// all the bounded syms in the current scope
	public final LinkedList<bound_info_t> B_scope = new LinkedList<bound_info_t>();

	@Override
	public void begin_context(ast_node n) {
		// [hack] bfs body is always sent-block.
		// check If I am bfs body (forward/reverse)
		// add bound-set to the context
		if ((n.get_nodetype() == ast_node_type.AST_SENTBLOCK) && (n.get_parent() != null)) {
			ast_node t = n.get_parent();
			if (t.get_nodetype() != ast_node_type.AST_BFS)
				return;
			ast_bfs bfs = (ast_bfs) t;
			if (n == bfs.get_fbody()) {
				gm_rwinfo_map B_fw = gm_rw_analysis.gm_get_bound_set_info(bfs).bound_set;

				// check bound error
				is_okay = check_add_and_report_conflicts(B_scope, B_fw) && is_okay;

			} else if (n == bfs.get_bbody()) {
				gm_rwinfo_map B_bw = gm_rw_analysis.gm_get_bound_set_info(bfs).bound_set;
				is_okay = check_add_and_report_conflicts(B_scope, B_bw) && is_okay;
			} else {
				assert false;
			}
		} else if (n.get_nodetype() == ast_node_type.AST_FOREACH) {
			ast_foreach fe = (ast_foreach) n;
			gm_rwinfo_map B = gm_rw_analysis.gm_get_bound_set_info(fe).bound_set;
			is_okay = check_add_and_report_conflicts(B_scope, B) && is_okay;
		}
	}

	@Override
	public void end_context(ast_node n) {
		if ((n.get_nodetype() == ast_node_type.AST_SENTBLOCK) && (n.get_parent() != null)) {
			ast_node t = n.get_parent();
			if (t.get_nodetype() != ast_node_type.AST_BFS)
				return;
			ast_bfs bfs = (ast_bfs) t;
			if (n == bfs.get_fbody()) {
				gm_rwinfo_map B_fw = gm_rw_analysis.gm_get_bound_set_info(bfs).bound_set;
				remove_all(B_scope, B_fw);
			} else if (n == bfs.get_bbody()) {
				gm_rwinfo_map B_bw = gm_rw_analysis.gm_get_bound_set_info(bfs).bound_set;
				remove_all(B_scope, B_bw);
			} else {
				assert false;
			}
		} else if (n.get_nodetype() == ast_node_type.AST_FOREACH) {
			ast_foreach fe = (ast_foreach) n;
			gm_rwinfo_map B = gm_rw_analysis.gm_get_bound_set_info(fe).bound_set;
			remove_all(B_scope, B);
		}
	}

	private static void remove_all(LinkedList<bound_info_t> L, gm_rwinfo_map B) {
		for (gm_symtab_entry e : B.keySet()) {
			gm_rwinfo_list l = B.get(e);
			for (gm_rwinfo jj : l) {
				remove_bound(L, e, jj.bound_symbol, jj.reduce_op);
			}
		}
	}

	private static void add_bound(LinkedList<bound_info_t> L, gm_symtab_entry t, gm_symtab_entry b, gm_reduce r_type) {
		bound_info_t T = new bound_info_t();
		T.target = t;
		T.bound = b;
		T.reduce_type = r_type;
		L.addLast(T);
	}

	private static void remove_bound(LinkedList<bound_info_t> L, gm_symtab_entry t, gm_symtab_entry b, gm_reduce r_type) {
		for (bound_info_t db : L) {
			if ((db.target == t) && (db.reduce_type == r_type) && (db.bound == b)) {
				L.remove(db);
				return;
			}
		}
	}

	private static boolean is_conflict(LinkedList<bound_info_t> L, gm_symtab_entry t, gm_symtab_entry b, gm_reduce r_type, RefObject<Boolean> is_bound_error,
			RefObject<Boolean> is_type_error) {
		is_type_error.argvalue = false;
		is_bound_error.argvalue = false;
		for (bound_info_t db : L) {
			if (db.target == t) {
				if (db.bound != b) {
					is_bound_error.argvalue = true;
					return true;
				} else if (db.reduce_type != r_type) {
					is_type_error.argvalue = true;
					return true;
				}
			}
		}
		return false;
	}

	/** returns is_okay */
	private static boolean check_add_and_report_conflicts(LinkedList<bound_info_t> L, gm_rwinfo_map B) {
		for (gm_symtab_entry e : B.keySet()) {
			gm_rwinfo_list l = B.get(e);
			for (gm_rwinfo jj : l) {
				boolean is_bound_error = false;
				boolean is_type_error = false;
				assert jj.bound_symbol != null;
				assert jj.reduce_op != gm_reduce.GMREDUCE_NULL;
				RefObject<Boolean> tempRef_is_bound_error = new RefObject<Boolean>(is_bound_error);
				RefObject<Boolean> tempRef_is_type_error = new RefObject<Boolean>(is_type_error);
				boolean tempVar = is_conflict(L, e, jj.bound_symbol, jj.reduce_op, tempRef_is_bound_error, tempRef_is_type_error);
				is_bound_error = tempRef_is_bound_error.argvalue;
				is_type_error = tempRef_is_type_error.argvalue;
				if (tempVar) {
					ast_id loc = jj.location;
					if (is_bound_error) {
						gm_error.gm_type_error(GM_ERROR_DOUBLE_BOUND_ITOR, loc.get_line(), loc.get_col(), jj.bound_symbol.getId().get_orgname());
						return false;
					}
					if (is_type_error) {
						gm_error.gm_type_error(GM_ERROR_DOUBLE_BOUND_OP, loc.get_line(), loc.get_col(), jj.reduce_op.get_reduce_string());
						return false;
					}
				} else {
					add_bound(L, e, jj.bound_symbol, jj.reduce_op);
				}
			}
		}
		return true;
	}

}
