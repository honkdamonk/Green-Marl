package backend_gps;

import static backend_gps.GPSConstants.GPS_FLAG_EDGE_DEFINED_INNER;
import static backend_gps.GPSConstants.GPS_FLAG_IS_INNER_LOOP;
import static backend_gps.GPSConstants.GPS_FLAG_IS_OUTER_LOOP;
import frontend.SYMTAB_TYPES;
import frontend.gm_range_type_t;
import frontend.gm_rw_analysis;
import frontend.gm_rwinfo;
import frontend.gm_rwinfo_sets;
import frontend.gm_symtab_entry;
import inc.gps_apply_bb_ast;
import ast.AST_NODE_TYPE;
import ast.ast_expr_builtin;
import ast.ast_field;
import ast.ast_foreach;
import ast.ast_id;
import ast.ast_node;
import ast.ast_sent;

import common.gm_reproduce;

//-------------------------------------------------------
// simple RW info; believe every access is random
//-------------------------------------------------------
public class gm_gps_find_rwinfo_simple extends gps_apply_bb_ast {

	protected gm_rwinfo_sets S;
	protected ast_foreach outer_loop = null;
	protected ast_foreach inner_loop = null;

	// find
	public gm_gps_find_rwinfo_simple(gm_rwinfo_sets _SS) {
		S = _SS;
		set_for_rhs(true);
		set_for_lhs(true);
		set_for_sent(true);
		set_for_builtin(true);
		set_separate_post_apply(true);
	}

	@Override
	public boolean apply(ast_sent s) {
		if (s.get_nodetype() == AST_NODE_TYPE.AST_FOREACH) {
			if (s.find_info_bool(GPS_FLAG_IS_OUTER_LOOP)) {
				outer_loop = (ast_foreach) s;
			} else if (s.find_info_bool(GPS_FLAG_IS_INNER_LOOP)) {
				inner_loop = (ast_foreach) s;
				assert outer_loop != null;
			}
		}

		return true;
	}

	@Override
	public boolean apply2(ast_sent s) {
		if (s.get_nodetype() == AST_NODE_TYPE.AST_FOREACH) {
			if (s.find_info_bool(GPS_FLAG_IS_OUTER_LOOP)) {
				outer_loop = null;
			} else if (s.find_info_bool(GPS_FLAG_IS_INNER_LOOP)) {
				inner_loop = null;
			}
		}

		return true;
	}

	@Override
	public boolean apply_lhs(ast_id id) {
		// -----------------------------------------------------
		// write to LHS
		//   int x = 0; <- add
		//   Foreach(n) {
		//     int y = 0; <- add
		//     x = 0; <- add
		//     Foreach(t) {
		//       int z = 0; <- add if receiver
		//       y = 0; should not happen
		//       x = 0; <- add if receiver
		//     }
		//   }
		// -----------------------------------------------------
		gps_syminfo syminfo = gps_get_global_syminfo(id);
		if (syminfo == null) {
			return true;
		}
		if (is_inner_loop()) {
			assert !syminfo.is_scoped_outer();
			if (!is_under_receiver_traverse())
				return true;
		}

		// add to write-set
		gm_rwinfo entry = gm_rwinfo.new_scala_inst(id);
		gm_rw_analysis.gm_add_rwinfo_to_set(S.write_set, id.getSymInfo(), entry, false);

		return true;
	}

	@Override
	public boolean apply_lhs(ast_field field) {
		// -----------------------------------------------------
		// write to LHS
		//   x.A = 0; should not happen
		//   Foreach(n) {
		//     n.A = 0; <- add (linear)
		//     x.A = 0; <- add if receiver
		//     Foreach(t) {
		//       t.A = 0; <- add if receiver
		//       n.A = 0; should not happen
		//       x.A = 0; <- add if receiver (should not happen?)
		//     }
		//   }
		// -----------------------------------------------------
		gm_symtab_entry drv = field.get_first().getSymInfo();
		boolean is_random = true;
		if (is_inner_loop()) {
			assert drv != outer_loop.get_iterator().getSymInfo();
			if (!is_under_receiver_traverse()) {
				return true;
			}
		} else if (is_outer_loop_only()) {
			if (drv == outer_loop.get_iterator().getSymInfo()) {
				is_random = false;
			} else if (!is_under_receiver_traverse())
				return true;
		} // global scope
		else {
			if (is_under_receiver_traverse()) // random write receiving
			{

			} else {
				assert false;
			}
		}

		// add to write-set
		gm_rwinfo entry = gm_rwinfo.new_field_inst(null, field.get_second());
		entry.access_range = (is_random) ? gm_range_type_t.GM_RANGE_RANDOM : gm_range_type_t.GM_RANGE_LINEAR;

		gm_rw_analysis.gm_add_rwinfo_to_set(S.write_set, field.get_second().getSymInfo(), entry, false);

		return true;
	}

	// read to outer scope belongs to 'sending' state
	// read to inner scope belongs to 'receiving' state
	@Override
	public boolean apply_rhs(ast_id id) {
		// -----------------------------------------------------
		// RHS
		//   int x; ... = x; <- add
		//   Foreach(n) {
		//     int y; ... = y; <- add
		//     ... = x; <- add
		//     Foreach(t) {
		//       int z; ... = z; <- add if receiver
		//       ... = y; <- add if sender
		//       ... = x ; <- add if receiver
		//     }
		//   }
		// -----------------------------------------------------
		gps_syminfo syminfo = gps_get_global_syminfo(id);
		if (syminfo == null) {
			return true;
		}
		if (is_inner_loop()) {
			if (syminfo.is_scoped_outer()) {
				if (is_under_receiver_traverse())
					return true;
			} else {
				if (!is_under_receiver_traverse())
					return true;
			}
			// [todo: random write rhs]
		}

		gm_rwinfo entry = gm_rwinfo.new_scala_inst(id);
		gm_rw_analysis.gm_add_rwinfo_to_set(S.read_set, id.getSymInfo(), entry, false);

		return true;
	}

	@Override
	public boolean apply_rhs(ast_field field) {
		// -----------------------------------------------------
		// RHS
		// ... = x.A; should not happen
		// Foreach(n) {
		// ... = n.A; <- add
		// ... = x.A; should not happen
		// Foreach(t) {
		// ... = t.A; <- add if receiver
		// ... = n.A; <- add if sender
		// ... = x.A ; should not happen
		// }
		// }
		// -----------------------------------------------------

		// random write lhs
		gm_symtab_entry drv = field.get_first().getSymInfo();
		boolean is_random = true;
		if (is_inner_loop()) {
			if (drv == inner_loop.get_iterator().getSymInfo()) {
				if (!is_under_receiver_traverse())
					return true;

			} else if (drv == outer_loop.get_iterator().getSymInfo()) {
				if (is_under_receiver_traverse())
					return true;
				is_random = false;
			} else if (drv.find_info_bool(GPS_FLAG_EDGE_DEFINED_INNER)) {
				if (is_under_receiver_traverse())
					return true;
			} else {
				System.out.printf("driver = %s outer_loop:%s\n", drv.getId().get_orgname(), outer_loop.get_iterator().get_genname());
				ast_node n = field;
				while (!n.is_sentence())
					n = n.get_parent();
				n.reproduce(0);
				gm_reproduce.gm_flush_reproduce();
				assert false;
			}
		} else if (is_outer_loop_only()) {
			if (drv != outer_loop.get_iterator().getSymInfo()) {
				assert false;
			}
			is_random = false;
		}

		gm_rwinfo entry = gm_rwinfo.new_field_inst(null, field.get_second());
		entry.access_range = (is_random) ? gm_range_type_t.GM_RANGE_RANDOM : gm_range_type_t.GM_RANGE_LINEAR;
		gm_rw_analysis.gm_add_rwinfo_to_set(S.read_set, field.get_second().getSymInfo(), entry, false);
		return true;
	}

	@Override
	public boolean apply_builtin(ast_expr_builtin b) {
		// [XXX] to be added
		return true;
	}

	@Override
	public boolean apply2(gm_symtab_entry e, SYMTAB_TYPES symtab_type) {
		// remove from S
		return true;
	}

	protected final boolean is_inner_loop() {
		return inner_loop != null;
	}

	protected final boolean is_outer_loop_or_inner() {
		return outer_loop != null;
	}

	protected final boolean is_outer_loop_only() {
		return is_outer_loop_or_inner() && !is_inner_loop();
	}

}