package backend_gps;

import frontend.gm_rw_analysis;
import frontend.gm_rw_analysis_check2;
import frontend.gm_range_type_t;
import frontend.gm_rwinfo;
import frontend.gm_symtab;
import frontend.gm_symtab_entry;
import inc.gm_compile_step;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import tangible.RefObject;

import ast.ast_node_type;
import ast.ast_foreach;
import ast.ast_if;
import ast.ast_node;
import ast.ast_procdef;
import ast.ast_sent;
import ast.ast_sentblock;
import ast.gm_rwinfo_list;
import ast.gm_rwinfo_map;

import common.gm_flat_nested_sentblock;
import common.gm_new_sents_after_tc;
import common.gm_resolve_nc;
import common.gm_transform_helper;

public class gm_gps_opt_split_loops_for_flipping extends gm_compile_step {
	
	private static String USED_BY_WHO = "gm_split_used_by_who";
	private static final int USED_BY_OLDER = 1;
	private static final int USED_BY_YOUNGER = 2;

	private gm_gps_opt_split_loops_for_flipping() {
		set_description("(Pre-Flip) Splitting loops");
	}

	public void process(ast_procdef p) {
		// -------------------------------------
		// Find nested loops
		// -------------------------------------
		HashMap<ast_foreach, ast_foreach> MAP = new HashMap<ast_foreach, ast_foreach>();
		HashSet<ast_foreach> SET = new HashSet<ast_foreach>();
		BackendGpsGlobal.gm_gps_find_double_nested_loops(p, MAP);

		// -------------------------------------
		// Find target inner loops
		// -------------------------------------
		filter_target_loops(MAP, SET);

		// -------------------------------------
		// - Now split the loops
		// -------------------------------------
		for (ast_foreach fe : SET) {
			split_the_loop(fe);
		}

		gm_flat_nested_sentblock.gm_flat_nested_sentblock(p);

		// reconstruct_scope implied in flattening
		// gm_reconstruct_scope(p);

		// -------------------------------------
		// Re-do RW analysis
		// -------------------------------------
		gm_rw_analysis.gm_redo_rw_analysis(p.get_body());

	}

	@Override
	public gm_compile_step get_instance() {
		return new gm_gps_opt_split_loops_for_flipping();
	}

	public static gm_compile_step get_factory() {
		return new gm_gps_opt_split_loops_for_flipping();
	}
	
	private static void split_the_loop(ast_foreach in) {
		ast_foreach out = null;

		// ----------------------------------------------------------------
		// find the current frame structure
		// example>
		// Foreach() <--- out
		// {
		// A1; A2;
		// {
		// B1; B2;
		// If (...) {
		// C1; C2;
		// Froeach() { <--- in
		// }
		// C3; C4;
		// }
		// B3; B4;
		// }
		// A3; A4;
		// }
		//
		// frame: C -> If -> B -> A
		// older[A] = {A1; A2;}
		// older[B] = {B1; B2;}
		// older[C] = {C1; C2;}
		// younger[A] = {A3; A4;}
		// younger[B] = {B3; B4;}
		// younger[C] = {C3; C4;}
		// ----------------------------------------------------------------

		LinkedList<ast_node> frame = new LinkedList<ast_node>();
		HashMap<ast_sentblock, LinkedList<ast_sent>> older_siblings = new HashMap<ast_sentblock, LinkedList<ast_sent>>();
		HashMap<ast_sentblock, LinkedList<ast_sent>> younger_siblings = new HashMap<ast_sentblock, LinkedList<ast_sent>>();

		ast_node current = in;
		boolean need_young = false;
		boolean need_old = false;
		while (true) {
			ast_node node = current.get_parent();
			assert node != null;
			if (node.get_nodetype() == ast_node_type.AST_IF) {
				frame.addLast(node);
				current = node;
				continue;
			} else if (node.get_nodetype() == ast_node_type.AST_FOREACH) {
				out = (ast_foreach) node;
				break;
			} else if (node.get_nodetype() == ast_node_type.AST_SENTBLOCK) {
				ast_sentblock sb = (ast_sentblock) node;
				frame.addLast(sb);

				// add elder and younger brothers
				LinkedList<ast_sent> All = sb.get_sents();
				LinkedList<ast_sent> OLD = new LinkedList<ast_sent>();
				LinkedList<ast_sent> YOUNG = new LinkedList<ast_sent>();
				boolean older = true;
				for (ast_sent s : All) {
					if (s == current) {
						older = false;
						continue;
					}
					if (older) {
						OLD.addLast(s);
					} else {
						YOUNG.addLast(s);
					}
				}

				All.clear();
				All.addLast((ast_sent) current);

				assert older == false;
				older_siblings.put(sb, OLD);
				younger_siblings.put(sb, YOUNG);
				need_young = need_young || (YOUNG.size() > 0);
				need_old = need_old || (OLD.size() > 0);
				current = node;

				continue; // go up one level
			} else {
				assert false;
			}
		}
		assert out != null;

		assert out.get_iter_type().is_iteration_on_all_graph();
		gm_transform_helper.gm_make_it_belong_to_sentblock(out);
		assert out.get_parent().get_nodetype() == ast_node_type.AST_SENTBLOCK;

		// check if there are dependencies via scalar variable
		// also mark each scalar symbols whether they are used by elder siblings
		// or younger siblings
		ensure_no_dependency_via_scala(frame, older_siblings, younger_siblings);

		// ----------------------------------------------------------------
		// reconstruct program
		// ----------------------------------------------------------------
		if (need_old) {
			ast_node old = reconstruct_old_new(frame, older_siblings, true);
			assert old != null;

			ast_foreach old_loop = gm_new_sents_after_tc.gm_new_foreach_after_tc(out.get_iterator().copy(false), out.get_source().copy(true),
					(ast_sent) old, out.get_iter_type());
			// replace iterator id
			gm_resolve_nc.gm_replace_symbol_entry(out.get_iterator().getSymInfo(), old_loop.get_iterator().getSymInfo(), old);

			gm_transform_helper.gm_add_sent_before(out, old_loop);

		}

		if (need_young) {
			ast_node old = reconstruct_old_new(frame, younger_siblings, false);
			assert old != null;
			ast_foreach new_loop = gm_new_sents_after_tc.gm_new_foreach_after_tc(out.get_iterator().copy(false), out.get_source().copy(true),
					(ast_sent) old, out.get_iter_type());
			// replace iterator id
			gm_resolve_nc.gm_replace_symbol_entry(out.get_iterator().getSymInfo(), new_loop.get_iterator().getSymInfo(), old);

			gm_transform_helper.gm_add_sent_after(out, new_loop);

		}
	}
	
	private static void ensure_no_dependency_via_scala(LinkedList<ast_node> frame, HashMap<ast_sentblock, LinkedList<ast_sent>> elder,
			HashMap<ast_sentblock, LinkedList<ast_sent>> younger) {
		// create prev/next
		// create set of scalar symbols used in prev
		HashSet<gm_symtab_entry> PREVS = new HashSet<gm_symtab_entry>(); 
		// symbols used in next
		HashSet<gm_symtab_entry> NEXTS = new HashSet<gm_symtab_entry>();
		// every scalar symbols
		HashSet<gm_symtab_entry> ALL = new HashSet<gm_symtab_entry>();
		
		for (ast_node node : frame) {
			if (node.get_nodetype() == ast_node_type.AST_IF) {
				// continue
			} else if (node.get_nodetype() == ast_node_type.AST_SENTBLOCK) {
				ast_sentblock sb = (ast_sentblock) node;
				LinkedList<ast_sent> OLD = elder.get(sb);
				LinkedList<ast_sent> NEW = younger.get(sb);
				for (ast_sent sent : OLD) {
					add_scalar_rw(sent, PREVS);
				}

				for (ast_sent sent : NEW) {
					add_scalar_rw(sent, NEXTS);
				}

				HashSet<gm_symtab_entry> E = sb.get_symtab_var().get_entries();
				for (gm_symtab_entry e : E) {
					ALL.add(e);
				}
			} else {
				assert false;
			}
		}

		// check if any entry is both used PREV and NEXT
		for (gm_symtab_entry e : ALL) {
			if ((PREVS.contains(e)) && (NEXTS.contains(e))) {
				assert false;
				// [todo] replace these symbols with temporary node_prop
				// re-do rw-analysis afterward.
			} else if (PREVS.contains(e))
				e.add_info_int(USED_BY_WHO, USED_BY_OLDER);
			else if (NEXTS.contains(e))
				e.add_info_int(USED_BY_WHO, USED_BY_YOUNGER);
		}
	}

	

	private static void add_scalar_rw(ast_sent s, HashSet<gm_symtab_entry> TARGET) {
		gm_rwinfo_map W = gm_rw_analysis_check2.gm_get_write_set(s);
		gm_rwinfo_map R = gm_rw_analysis_check2.gm_get_write_set(s);
		for (gm_symtab_entry e : W.keySet()) {
			if (!e.getType().is_property()) {
				TARGET.add(e);
			}
		}
		for (gm_symtab_entry e : R.keySet()) {
			if (!e.getType().is_property()) {
				TARGET.add(e);
			}
		}
	}
	
	private static void reconstruct_old_new_main(ast_node n, HashMap<ast_sentblock, LinkedList<ast_sent>> siblings, boolean is_old, RefObject<ast_node> last) {

		if (n.get_nodetype() == ast_node_type.AST_IF) {
			if (last.argvalue == null) // can ignore this if loop
				return;

			ast_if iff = (ast_if) (n);
			ast_if new_if = ast_if.new_if(iff.get_cond().copy(true), (ast_sent) last.argvalue, null);
			new_if.set_line(iff.get_line());
			new_if.set_col(iff.get_col());
			last.argvalue = new_if;

		} else if ((n).get_nodetype() == ast_node_type.AST_SENTBLOCK) {
			ast_sentblock sb_org = (ast_sentblock) (n);
			ast_sentblock sb = ast_sentblock.new_sentblock();
			LinkedList<ast_sent> SIB = siblings.get(sb_org);
			sb.set_line(sb_org.get_line());
			sb.set_col(sb_org.get_col());

			if (is_old) {
				for (ast_sent sent : SIB) {
					sb.add_sent(sent);
				}
				if (last.argvalue != null)
					sb.add_sent((ast_sent) last.argvalue);
			} else {
				if (last.argvalue != null)
					sb.add_sent((ast_sent) last.argvalue);

				for (ast_sent sent : SIB) {
					sb.add_sent(sent);
				}
			}

			last.argvalue = sb;

			// move scalar symbols
			gm_symtab old_tab = sb_org.get_symtab_var();
			gm_symtab new_tab = sb.get_symtab_var();
			HashSet<gm_symtab_entry> entries = old_tab.get_entries();
			HashSet<gm_symtab_entry> T = new HashSet<gm_symtab_entry>();
			for (gm_symtab_entry e : entries) {
				int used_by = e.find_info_int(USED_BY_WHO);
				if ((used_by == USED_BY_OLDER) && (is_old)) {
					T.add(e);
				} else if ((used_by == USED_BY_YOUNGER) && (!is_old)) {
					T.add(e);
				}
			}
			for (gm_symtab_entry e : T) {
				old_tab.remove_entry_in_the_tab(e);
				new_tab.add_symbol(e);
			}
		}
	}
	
	private static gm_rwinfo_map gm_get_reduce_set(ast_sent S) {
		assert S != null;
		return gm_rw_analysis.get_rwinfo_sets(S).reduce_set;
	}
	
	// --------------------------------------------
	// example>
	// FE(n:)
	// {
	// A;
	// {
	// B;
	// If()
	// {
	// C;
	// Foreach(t:) {
	// }
	// D;
	// }
	// E;
	// }
	// F;
	// }
	// =====>
	// FE(n:)
	// {
	// A;
	// {
	// B;
	// If()
	// {
	// C;
	// } } }
	//
	// FE(n1:)
	// { { If() {
	// Foreach(t:) {
	// }
	// }
	// } }
	//
	// FE(n2:)
	// { { If()
	// {
	// D;
	// }
	// E;
	// }
	// F;
	// }
	// ------------------------------------------
	private static ast_node reconstruct_old_new(LinkedList<ast_node> frame, HashMap<ast_sentblock, LinkedList<ast_sent>> siblings, boolean is_old) {
		ast_node last = null;

		// reconstruct hierarchy
		// inmost --> outmost
		for (ast_node node : frame) {
			RefObject<ast_node> last_wrapper = new RefObject<ast_node>(last);
			reconstruct_old_new_main(node, siblings, is_old, last_wrapper);
			last = last_wrapper.argvalue;
		}

		return last;
	}
	
	// ----------------------------------------------------
	// N_P<Int>(G) _tmp_S;
	// Foreach (n: G.Nodes) {
	// n._tmp_S = 0;
	// Foreach( t: n.InNbrs) {
	// n._tmp_S += t.A;
	// }
	// n.B = n._tmp_S * alpha + beta;
	// }
	// ===>
	// Foreach (n: G.Nodes) {
	// n._tmp_S = 0;
	// }
	// Foreach (n: G.Nodes) {
	// Foreach( t: n.InNbrs) {
	// n._tmp_S += t.A;
	// }
	// }
	// Foreach (n: G.Nodes) {
	// n.B = n._tmp_S * alpha + beta;
	// }
	// ----------------------------------------------------
	//

	// ---------------------------------------
	//
	// Foreach(n.) {
	// .... (A)
	//
	// <if> { // no variables defined in between
	// Foreach(t) {
	// n.X = ...
	// }
	// }
	//
	// .... (B)
	// }
	// ---------------------------------------
	private static void filter_target_loops(HashMap<ast_foreach, ast_foreach> SRC, HashSet<ast_foreach> SET) {
		for (ast_foreach in : SRC.keySet()) {
			ast_foreach out = SRC.get(in);
			if (out == null)
				continue;

			gm_symtab_entry out_iter = out.get_iterator().getSymInfo();

			boolean is_target = false;
			// check if inner loop requires flipping
			gm_rwinfo_map WMAP = gm_rw_analysis_check2.gm_get_write_set(in.get_body());
			for (gm_symtab_entry e : WMAP.keySet()) {
				gm_rwinfo_list LIST = WMAP.get(e);
				boolean is_field = e.getType().is_property();
				for (gm_rwinfo info : LIST) {
					if (is_field && (info.driver == null) && (info.access_range == gm_range_type_t.GM_RANGE_RANDOM)) {
						is_target = true;
						continue;
					} else if (info.driver == null) {
						continue;
					}

					if (info.driver != out_iter) {
						is_target = false;
						break;
					}
					if (info.driver == out_iter) {
						is_target = true;
						continue;
					}
				}
			}

			gm_rwinfo_map DMAP = gm_get_reduce_set(in.get_body());
			for (gm_symtab_entry e : DMAP.keySet()) {
				gm_rwinfo_list LIST = DMAP.get(e);
				for (gm_rwinfo info : LIST) {
					if ((info.driver == null) && (info.access_range == gm_range_type_t.GM_RANGE_RANDOM)) {
						continue;
					} else if (info.driver == null)
						continue;

					if (info.driver != out_iter) {
						is_target = false;
						break;
					}
					if (info.driver == out_iter) {
						is_target = true;
						continue;
					}
				}
			}

			// printf(" loop for %s -> %c \n",
			// in->get_iterator()->get_genname(), is_target?'Y':'N');
			if (!is_target)
				continue;

			boolean meet_if = false;
			ast_node current = in;
			// printf("current = %p %s\n", current,
			// gm_get_nodetype_string(in->get_nodetype()));
			// move up until meet the outer loop
			while (true) {
				ast_node parent = current.get_parent();
				assert parent != null;
				// printf(" parent = %p %s\n", parent,
				// gm_get_nodetype_string(parent->get_nodetype()));
				if (parent.get_nodetype() == ast_node_type.AST_IF) {
					if (meet_if) {
						is_target = false;
						break;
					} else if (((ast_if) parent).get_else() == null) {
						meet_if = true;
					} else {
						is_target = false;
						break;
					}
				} else if (parent.get_nodetype() == ast_node_type.AST_SENTBLOCK) {
					// todo: this is a little bit to restrictive.
					// we can relax this by changing more scalars into node
					// properties.
					ast_sentblock sb = (ast_sentblock) parent;

					if (sb.get_symtab_field().get_num_symbols() > 0) {
						is_target = false;
						break;
					}
					/*
					 * if ((sb->get_symtab_field()->get_num_symbols() > 0) ||
					 * (sb->get_symtab_var()->get_num_symbols() > 0)) {
					 */
				} else if (parent.get_nodetype() == ast_node_type.AST_FOREACH) {
					ast_foreach fe = (ast_foreach) parent;
					if (fe == out)
						break;
					else {
						is_target = false;
						break;
					}
				} else {
					// printf("parent  = %s!!!\n",
					// gm_get_nodetype_string(parent->get_nodetype()));
					is_target = false;
					break;
				}
				current = parent;
			}

			// printf(" loop for %s -> %c \n",
			// in->get_iterator()->get_genname(), is_target?'Y':'N');
			if (!is_target)
				continue;
			SET.add(in);
		}
	}


}