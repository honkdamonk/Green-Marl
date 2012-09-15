package backend_gps;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import ast.ast_assign;
import ast.ast_foreach;
import ast.ast_id;
import ast.ast_node;
import ast.ast_node_type;
import ast.ast_sent;

import common.gm_apply;

import frontend.gm_symtab_entry;
import frontend.symtab_types;

//----------------------------------------------------
// Foreach (n: G.Nodes) {
//   Int S;
//   S = 0;
//   Foreach( t: n.InNbrs) {
//       S += t.A;
//   }
//   n.B = S * alpha + beta;
// }
// ===>  
// N_P<Int>(G) _tmp_S;
// Foreach (n: G.Nodes) {
//   n._tmp_S = 0;
//   Foreach( t: n.InNbrs) {
//       n._tmp_S += t.A;
//   }
//   n.B = n._tmp_S * alpha + beta;
// }
//----------------------------------------------------

public class gps_opt_find_scalar_replace_target_t extends gm_apply {

	public gps_opt_find_scalar_replace_target_t(HashMap<ast_foreach, ast_foreach> M) {
		this.MAP = new HashMap<ast_foreach, ast_foreach>(M);
		set_for_sent(true);
		set_for_symtab(true);
		set_separate_post_apply(true);
		level = 0;
		outloop = null;
		inloop = null;
	}

	@Override
	public boolean apply(gm_symtab_entry e, symtab_types symtab_type) {
		// find scalar variables defined in the first level
		if ((level == 1) && (symtab_type == symtab_types.GM_SYMTAB_VAR)) {
			if (e.getType().is_primitive())
				potential_target_syms.add(e);
		}
		return true;
	}

	@Override
	public boolean apply(ast_sent s) {
		// level management
		if (s.get_nodetype() == ast_node_type.AST_FOREACH) {
			ast_foreach fe = (ast_foreach) s;
			if (level == 0) {
				if (MAP.containsKey(fe)) {
					level = 1;
					outloop = fe;
				}
			} else if (level == 1) {
				if (MAP.containsKey(fe)) {
					level = 2;
					inloop = fe;
				}
			}
		}

		else if ((level == 2) && (s.get_nodetype() == ast_node_type.AST_ASSIGN)) {
			ast_assign a = (ast_assign) s;
			if (a.is_target_scalar()) {
				// check if LHS is potential target
				gm_symtab_entry target = a.get_lhs_scala().getSymInfo();
				if (potential_target_syms.contains(target)) {
					target_syms.put(target, outloop); // found target
				}

				if (a.has_lhs_list()) {
					LinkedList<ast_node> lhs_list = a.get_lhs_list();
					for (ast_node n : lhs_list) {
						if (n.get_nodetype() != ast_node_type.AST_ID)
							continue;
						ast_id id = (ast_id) n;
						target = id.getSymInfo();
						if (potential_target_syms.contains(target)) // found
																	// target
							target_syms.put(target, outloop);
					}
				}
			}
		}
		return true;
	}

	@Override
	public boolean apply2(ast_sent s) {
		if (s.get_nodetype() == ast_node_type.AST_FOREACH) {
			ast_foreach fe = (ast_foreach) s;
			if ((level == 2) && (inloop == fe)) {
				level = 1;
			} else if ((level == 1) && (outloop = fe) != null) {
				level = 0;
			}
		}
		return true;
	}

	public final HashMap<gm_symtab_entry, ast_foreach> get_target_syms_and_outer_loop() {
		return target_syms;
	}

	private HashMap<ast_foreach, ast_foreach> MAP;
	private HashSet<gm_symtab_entry> potential_target_syms = new HashSet<gm_symtab_entry>();
	private HashMap<gm_symtab_entry, ast_foreach> target_syms = new HashMap<gm_symtab_entry, ast_foreach>();
	private ast_foreach outloop;
	private ast_foreach inloop;
	private int level;
}