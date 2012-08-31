package frontend;

import static frontend.gm_range_type_t.GM_RANGE_LEVEL;
import static frontend.gm_range_type_t.GM_RANGE_LEVEL_DOWN;
import static frontend.gm_range_type_t.GM_RANGE_LEVEL_UP;
import static frontend.gm_range_type_t.GM_RANGE_LINEAR;
import static frontend.gm_range_type_t.GM_RANGE_RANDOM;
import static frontend.gm_range_type_t.GM_RANGE_SINGLE;
import inc.GMTYPE_T;
import inc.GM_REDUCE_T;

import java.util.HashMap;
import java.util.LinkedList;

import ast.AST_NODE_TYPE;
import ast.ast_bfs;
import ast.ast_expr;
import ast.ast_expr_builtin;
import ast.ast_expr_foreign;
import ast.ast_expr_reduce;
import ast.ast_field;
import ast.ast_foreach;
import ast.ast_id;
import ast.ast_node;
import ast.ast_sent;
import ast.ast_sentblock;
import ast.gm_rwinfo_list;
import ast.gm_rwinfo_map;

import common.GM_ERRORS_AND_WARNINGS;
import common.gm_error;
import common.gm_traverse;

public class GlobalMembersGm_rw_analysis {

	public static gm_range_type_t gm_get_range_from_itertype(GMTYPE_T itype) {
		switch (itype) {
		case GMTYPE_NODEITER_ALL:
		case GMTYPE_EDGEITER_ALL:
			return GM_RANGE_LINEAR;
		case GMTYPE_NODEITER_NBRS:
		case GMTYPE_NODEITER_IN_NBRS:
		case GMTYPE_EDGEITER_NBRS:
		case GMTYPE_NODEITER_COMMON_NBRS:
		case GMTYPE_EDGEITER_IN_NBRS:
			return GM_RANGE_RANDOM;
		case GMTYPE_NODEITER_BFS:
		case GMTYPE_EDGEITER_BFS:
			return GM_RANGE_LEVEL;
		case GMTYPE_NODEITER_UP_NBRS:
		case GMTYPE_EDGEITER_UP_NBRS:
			return GM_RANGE_LEVEL_UP;
		case GMTYPE_NODEITER_DOWN_NBRS:
		case GMTYPE_EDGEITER_DOWN_NBRS:
			return GM_RANGE_LEVEL_DOWN;
		case GMTYPE_NODEITER_SET:
		case GMTYPE_EDGEITER_SET:
			return GM_RANGE_LINEAR;
		case GMTYPE_NODEITER_ORDER:
		case GMTYPE_EDGEITER_ORDER:
			return GM_RANGE_LINEAR;
		case GMTYPE_NODEITER_SEQ:
		case GMTYPE_EDGEITER_SEQ:
			return GM_RANGE_RANDOM;
		case GMTYPE_NODE:
		case GMTYPE_EDGE:
			return GM_RANGE_RANDOM;
		case GMTYPE_PROPERTYITER_SET:
		case GMTYPE_PROPERTYITER_SEQ:
		case GMTYPE_PROPERTYITER_ORDER:
			return GM_RANGE_LINEAR;
		case GMTYPE_COLLECTIONITER_SET:
		case GMTYPE_COLLECTIONITER_SEQ:
		case GMTYPE_COLLECTIONITER_ORDER:
			return GM_RANGE_RANDOM; // TODO is there somthing more suitable?
		default:
			System.out.printf("type = %d\n", itype);
			assert false;
			throw new AssertionError();
		}
	}

	// -----------------------------------------------------------
	// Add rwinfo to set.
	// new_entry deleted if not added
	// [return]
	// true; okay
	// false; error
	// -----------------------------------------------------------
	public static boolean gm_add_rwinfo_to_set(gm_rwinfo_map info_set, gm_symtab_entry sym, gm_rwinfo new_entry) {
		return gm_add_rwinfo_to_set(info_set, sym, new_entry, false);
	}

	// list of rw-info
	// map from target(symtab entry) to list of rw-info
	// (one field may have multiple access patterns)

	public static boolean gm_add_rwinfo_to_set(gm_rwinfo_map info_set, gm_symtab_entry sym, gm_rwinfo new_entry, boolean is_reduce_ops) {
		boolean is_error = false;

		// find entry in the map
		if (!info_set.containsKey(sym)) // not found --> add new;
		{
			gm_rwinfo_list l = new gm_rwinfo_list();
			l.addLast(new_entry);
			info_set.put(sym, l);
		} // check entries already exists
		else {
			gm_rwinfo_list l = info_set.get(sym);
			assert l != null;
			for (gm_rwinfo e2 : l) {
				// check reduce error
				if (is_reduce_ops) {

					is_error = GlobalMembersGm_rw_analysis.is_reduce_error(e2, new_entry);
					if (is_error) {
						if (new_entry != null)
							new_entry.dispose(); // not required
						return false;
					}

				}

				if (GlobalMembersGm_rw_analysis.is_same_entry(e2, new_entry)) {
					if (new_entry != null)
						new_entry.dispose(); // not required
					return true;
				} // old entry is wider
				else if (GlobalMembersGm_rw_analysis.is_wider_entry(new_entry, e2)) {
					if (new_entry != null)
						new_entry.dispose(); // drop new entry
					return true;
				} // new_entry is wider
				else if (GlobalMembersGm_rw_analysis.is_wider_entry(e2, new_entry)) {
					// hack. copy new entry into old one
					e2.copyFrom(new_entry);
					if (new_entry != null)
						new_entry.dispose(); // not required
					return true;
				}

			}
			// add new
			l.addLast(new_entry);
		}

		return true;
	}

	/** Actual information kept for sentence
	* Three maps. (readset, writeset, reduce-set)
	*/
	public static void gm_delete_rwinfo_map(gm_rwinfo_map m) {
		for (gm_rwinfo_list l : m.values()) {
			for (gm_rwinfo j : l) {
				if (j != null)
					j.dispose();
			}
			l.clear();
		}
		m.clear();
	}

	public static String GM_INFOKEY_RW = "GM_INFOKEY_RW";

	public static gm_rwinfo_sets get_rwinfo_sets(ast_node n) {
		// get rwinfo from a node. (create one if not there)
		gm_rwinfo_sets rwi = (gm_rwinfo_sets) n.find_info(GM_INFOKEY_RW);
		if (rwi == null) {
			rwi = new gm_rwinfo_sets();
			n.add_info(GM_INFOKEY_RW, rwi);
		}
		return rwi;
	}

	public static gm_rwinfo_sets gm_get_rwinfo_sets(ast_node n) {
		return GlobalMembersGm_rw_analysis.get_rwinfo_sets(n);
	}

	/**
	* additional information for foreach statement
	*/
	public static String GM_INFOKEY_BOUND = "GM_INFOKEY_BOUND";

	public static gm_bound_set_info gm_get_bound_set_info(ast_foreach n) {
		gm_bound_set_info bi = (gm_bound_set_info) n.find_info(GM_INFOKEY_BOUND);
		if (bi == null) {
			bi = new gm_bound_set_info();
			n.add_info(GM_INFOKEY_BOUND, bi);
		}
		return bi;
	}

	public static gm_bound_set_info gm_get_bound_set_info(ast_bfs n) {
		gm_bound_set_info bi = (gm_bound_set_info) n.find_info(GM_INFOKEY_BOUND);
		if (bi == null) {
			bi = new gm_bound_set_info();
			n.add_info(GM_INFOKEY_BOUND, bi);
		}
		return bi;
	}

	/**
	* re-do rw analysis for IR tree s.
	* (result does not propagate upward from s though.)
	*/
	public static boolean gm_redo_rw_analysis(ast_sent s) {
		// nullify previous analysis. (IR tree has been modified)
		gm_delete_rw_analysis D = new gm_delete_rw_analysis();
		gm_traverse.gm_traverse_sents(s, D, gm_traverse.GM_POST_APPLY);

		// do-it again RW analysis
		gm_rw_analysis RWA = new gm_rw_analysis();
		gm_traverse.gm_traverse_sents(s, RWA, gm_traverse.GM_POST_APPLY); // post
																									// apply
		return RWA.is_success();
	}

	public static boolean gm_is_modified_always_linearly(ast_sent S, gm_symtab_entry e) {
		gm_rwinfo_query Q = new gm_rwinfo_query();
		Q.check_range(GM_RANGE_LINEAR);
		Q.check_always(true);
		return is_modified_with_condition(S, e, Q);
	}

	/**
	* AST_IF<br>
	* If (expr) [Then sent] [Else sent2]<br>
	* 1) add expr into read set<br>
	* 2) merge then-part sets and else-part sets<br>
	* make all the accesses conditional,
	* unless both-path contain same access
	* @return is_okay
	*/
	public static boolean merge_for_if_else(gm_rwinfo_map Target, gm_rwinfo_map S1, gm_rwinfo_map S2, boolean is_reduce) {
		boolean is_okay = true;

		// search for all elements in then-part
		for (gm_symtab_entry sym : S1.keySet()) {
			gm_rwinfo_list l1 = S1.get(sym);
			assert l1 != null;

			// check this symbol is accessed in S2 as well
			if (S2.containsKey(sym)) {
				// not in the else path
				// --> copy_and_add all the accesses to this symbol.
				for (gm_rwinfo e : l1) {
					gm_rwinfo copy = e.copy();
					copy.always = false; // chage it into conditional access
					is_okay = is_okay && GlobalMembersGm_rw_analysis.gm_add_rwinfo_to_set(Target, sym, copy, is_reduce);
				}
			} else {
				// symbol also in the else path
				for (gm_rwinfo e : l1) {
					gm_rwinfo copy = e.copy();
					if (copy.always == true) {
						boolean found = false;
						// check if the same access happens in else part
						gm_rwinfo_list l2 = S2.get(sym);

						assert l2 != null;
						for (gm_rwinfo else_info : l2) {
							if (GlobalMembersGm_rw_analysis.is_same_entry(copy, else_info)) {
								found = true;
								break;
							}
						}
						if (!found) // add it as 'non-conditional' access
							copy.always = false;

						is_okay = is_okay && GlobalMembersGm_rw_analysis.gm_add_rwinfo_to_set(Target, sym, copy, is_reduce);
					} // alrady conditional. simply add in the new set
					else {
						is_okay = is_okay && GlobalMembersGm_rw_analysis.gm_add_rwinfo_to_set(Target, sym, copy, is_reduce);
					}
				}
			}
		}

		// elements in the else-part
		for (gm_symtab_entry sym : S2.keySet()) {
			// we can blindly add here.
			// (If merged from the then-part, wider entry will be already in the
			// target set.)
			gm_rwinfo_list l2 = S2.get(sym);
			for (gm_rwinfo e : l2) {
				gm_rwinfo copy = e.copy();
				copy.always = false; // chage it into conditional access
				is_okay = is_okay && GlobalMembersGm_rw_analysis.gm_add_rwinfo_to_set(Target, sym, copy, is_reduce);
			}
		}

		return is_okay; // returns is_okay
	}

	public static String gm_get_range_string(gm_range_type_t access_range) {
		return (access_range == GM_RANGE_LINEAR) ? "LINEAR" : (access_range == GM_RANGE_RANDOM) ? "RANDOM" : (access_range == GM_RANGE_LEVEL) ? "LEVEL"
				: (access_range == GM_RANGE_LEVEL_UP) ? "LEVEL_UP" : (access_range == GM_RANGE_LEVEL_DOWN) ? "LEVEL_DOWN" : "???";
	}

	/**
	* Add info to set,
	* unless the same information already exists in the set<br>
	*
	* If current info is 'wider', remove previous information
	* (i.e. conditional < always)<br>
	* (note: cannot compare different rages: e.g. linear vs. random)<br>
	*/
	public static boolean is_same_entry(gm_rwinfo old, gm_rwinfo neo) {
		// compare except location
		if (old.access_range != neo.access_range)
			return false;
		if (old.driver != neo.driver)
			return false;
		if (old.always != neo.always)
			return false;
		if (old.reduce_op != neo.reduce_op)
			return false;
		if (old.bound_symbol != neo.bound_symbol)
			return false;
		if (old.is_supplement != neo.is_supplement)
			return false;
		if (old.org_lhs != neo.org_lhs)
			return false;
		if (old.mutate_direction != neo.mutate_direction)
			return false;
		return true;
	}

	/** true if neo is wider */
	public static boolean is_wider_entry(gm_rwinfo old, gm_rwinfo neo) {
		// should agree on the range & iterator
		if (old.access_range != neo.access_range)
			return false;
		if (old.driver != neo.driver)
			return false;
		if ((old.always == false) && (neo.always == true))
			return true;
		return false;
	}

	/**
	* check if two symbols bound to the same operator
	* check if two symbols bound to the same boud
	* @return true if error
	*/
	public static boolean is_reduce_error(gm_rwinfo old, gm_rwinfo neo) {
		assert neo.bound_symbol != null;
		assert old.bound_symbol != null;

		// check if they bound to the same operator
		if (old.reduce_op != neo.reduce_op) {
			// generate error message
			assert neo.location != null;
			// assert(neo->driver->getId() != NULL);

			gm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_DOUBLE_BOUND_OP, neo.location.get_line(), neo.location.get_col(),
					old.reduce_op.get_reduce_string());
			return true;
		}

		// check if they are bound to the same symbol
		if (old.bound_symbol != neo.bound_symbol) {
			// generate error message
			gm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_DOUBLE_BOUND_ITOR, neo.location.get_line(), neo.location.get_col(),
					old.bound_symbol.getId().get_orgname());
			return true;
		}

		return false;
	}

	public static HashMap<gm_symtab_entry, range_cond_t> Default_DriverMap = new HashMap<gm_symtab_entry, range_cond_t>();

	public static void traverse_expr_for_readset_adding(ast_expr e, gm_rwinfo_map rset) {
		traverse_expr_for_readset_adding(e, rset, Default_DriverMap);
	}

	public static void traverse_expr_for_readset_adding(ast_expr e, gm_rwinfo_map rset, HashMap<gm_symtab_entry, range_cond_t> DrvMap) {

		switch (e.get_opclass()) {
		case GMEXPR_ID: {
			gm_rwinfo new_entry = gm_rwinfo.new_scala_inst(e.get_id());
			gm_symtab_entry sym = e.get_id().getSymInfo();
			assert sym != null;
			GlobalMembersGm_rw_analysis.gm_add_rwinfo_to_set(rset, sym, new_entry, false);
			break;
		}
		case GMEXPR_FIELD:
			GlobalMembersGm_rw_analysis.traverse_expr_for_readset_adding_field(e, rset, DrvMap);
			break;
		case GMEXPR_UOP:
		case GMEXPR_LUOP:
			GlobalMembersGm_rw_analysis.traverse_expr_for_readset_adding(e.get_left_op(), rset, DrvMap);
			break;
		case GMEXPR_BIOP:
		case GMEXPR_LBIOP:
		case GMEXPR_COMP:
			GlobalMembersGm_rw_analysis.traverse_expr_for_readset_adding(e.get_left_op(), rset, DrvMap);
			GlobalMembersGm_rw_analysis.traverse_expr_for_readset_adding(e.get_right_op(), rset, DrvMap);
			break;
		case GMEXPR_TER: {
			GlobalMembersGm_rw_analysis.traverse_expr_for_readset_adding(e.get_cond_op(), rset, DrvMap);
			gm_rwinfo_map R1 = new gm_rwinfo_map();
			GlobalMembersGm_rw_analysis.traverse_expr_for_readset_adding(e.get_left_op(), R1, DrvMap);
			gm_rwinfo_map R2 = new gm_rwinfo_map();
			GlobalMembersGm_rw_analysis.traverse_expr_for_readset_adding(e.get_right_op(), R2, DrvMap);
			GlobalMembersGm_rw_analysis.merge_for_if_else(rset, R1, R2, false);
			break;
		}
		case GMEXPR_REDUCE:
			GlobalMembersGm_rw_analysis.traverse_expr_for_readset_adding_reduce((ast_expr_reduce) e, rset, DrvMap);
			break;
		case GMEXPR_BUILTIN: // built-ins does not read or modify anything other
								// than arguments
			GlobalMembersGm_rw_analysis.traverse_expr_for_readset_adding_builtin((ast_expr_builtin) e, rset, DrvMap);
			break;
		case GMEXPR_BUILTIN_FIELD:
			GlobalMembersGm_rw_analysis.traverse_expr_for_readset_adding_builtin((ast_expr_builtin) e, rset, DrvMap);
			GlobalMembersGm_rw_analysis.traverse_expr_for_readset_adding_field(e, rset, DrvMap);
			break;
		case GMEXPR_FOREIGN: // read symbols in the foreign
			GlobalMembersGm_rw_analysis.traverse_expr_for_readset_adding_foreign((ast_expr_foreign) e, rset, DrvMap);
			break;
		case GMEXPR_IVAL:
		case GMEXPR_FVAL:
		case GMEXPR_BVAL:
		case GMEXPR_INF:
		case GMEXPR_NIL:
			break;
		default:
			assert false;
			break;
		}
	}

	public static void traverse_expr_for_readset_adding_field(ast_expr e, gm_rwinfo_map rset, HashMap<gm_symtab_entry, range_cond_t> DrvMap) {
		gm_symtab_entry iter_sym = e.get_field().get_first().getSymInfo();
		gm_symtab_entry field_sym = e.get_field().get_second().getSymInfo();
		assert iter_sym != null;

		assert iter_sym.getType() != null;

		gm_rwinfo new_entry;
		if (!DrvMap.containsKey(iter_sym)) {
			new_entry = gm_rwinfo.new_field_inst(iter_sym, e.get_field().get_first()); // iterator
																						// syminfo

			gm_symtab_entry driver_sym = e.get_field().get_first().getSymInfo();
			gm_rwinfo driver_entry = gm_rwinfo.new_scala_inst(e.get_field().get_first());
			GlobalMembersGm_rw_analysis.gm_add_rwinfo_to_set(rset, driver_sym, driver_entry);

		} // temporary driver or vector driver
		else {
			gm_range_type_t range_type = DrvMap.get(iter_sym).range_type;
			boolean always = DrvMap.get(iter_sym).is_always;
			new_entry = gm_rwinfo.new_range_inst(range_type, always, e.get_field().get_first());
		}

		GlobalMembersGm_rw_analysis.gm_add_rwinfo_to_set(rset, field_sym, new_entry);
	}

	public static void traverse_expr_for_readset_adding_builtin(ast_expr_builtin builtin, gm_rwinfo_map rset, HashMap<gm_symtab_entry, range_cond_t> DrvMap) {
		// add every arguments in the readset
		LinkedList<ast_expr> args = builtin.get_args();
		for (ast_expr a : args) {
			GlobalMembersGm_rw_analysis.traverse_expr_for_readset_adding(a, rset, DrvMap);
		}
	}

	public static void traverse_expr_for_readset_adding_foreign(ast_expr_foreign f, gm_rwinfo_map rset, HashMap<gm_symtab_entry, range_cond_t> DrvMap) {

		gm_rwinfo new_entry;
		LinkedList<ast_node> N = f.get_parsed_nodes();
		for (ast_node n : N) {
			if (n == null)
				continue;
			if (n.get_nodetype() == AST_NODE_TYPE.AST_ID) {
				ast_id id = (ast_id) n;
				new_entry = gm_rwinfo.new_scala_inst(id);
				gm_symtab_entry sym = id.getSymInfo();
				assert sym != null;
				GlobalMembersGm_rw_analysis.gm_add_rwinfo_to_set(rset, sym, new_entry, false);
			} else if (n.get_nodetype() == AST_NODE_TYPE.AST_FIELD) {
				ast_field f1 = (ast_field) n;
				gm_symtab_entry iter_sym = f1.get_first().getSymInfo();
				gm_symtab_entry field_sym = f1.get_second().getSymInfo();

				if (!DrvMap.containsKey(iter_sym)) {
					new_entry = gm_rwinfo.new_field_inst(iter_sym, f1.get_first());
				} // temporary driver or vector driver
				else {
					gm_range_type_t range_type = DrvMap.get(iter_sym).range_type;
					boolean always = DrvMap.get(iter_sym).is_always;
					new_entry = gm_rwinfo.new_range_inst(range_type, always, f1.get_first());
				}
				GlobalMembersGm_rw_analysis.gm_add_rwinfo_to_set(rset, field_sym, new_entry);
			}
		}
	}

	public static void traverse_expr_for_readset_adding_reduce(ast_expr_reduce e2, gm_rwinfo_map rset, HashMap<gm_symtab_entry, range_cond_t> DrvMap) {
		gm_symtab_entry it = e2.get_iterator().getSymInfo();
		GMTYPE_T iter_type = e2.get_iter_type();
		ast_expr f = e2.get_filter();
		ast_expr b = e2.get_body();
		boolean is_conditional = (f != null) || iter_type.is_collection_iter_type();
		range_cond_t R = new range_cond_t(GlobalMembersGm_rw_analysis.gm_get_range_from_itertype(iter_type), !is_conditional);
		DrvMap.put(it, R);
		GlobalMembersGm_rw_analysis.traverse_expr_for_readset_adding(b, rset, DrvMap);
		DrvMap.remove(it);

		if (f != null) // filter itself is always accessed
		{
			DrvMap.put(it, R);
			R.is_always = true;
			GlobalMembersGm_rw_analysis.traverse_expr_for_readset_adding(f, rset, DrvMap);
			DrvMap.remove(it);
		}
	}

	public static boolean merge_for_if(gm_rwinfo_map Target, gm_rwinfo_map S1, boolean is_reduce) {
		boolean is_okay = true;
		// search for all elements in then-part
		// and add a copy into the target set.
		for (gm_symtab_entry sym : S1.keySet()) {
			gm_rwinfo_list l1 = S1.get(sym);
			assert l1 != null;

			for (gm_rwinfo e : l1) {
				gm_rwinfo copy = e.copy();
				copy.always = false; // chage it into conditional access
				is_okay = is_okay && GlobalMembersGm_rw_analysis.gm_add_rwinfo_to_set(Target, sym, copy, is_reduce);
			}
		}
		return is_okay;
	}

	/**
	* AST_SENTBLOCK
	* { s1; s2; s3;}
	* 1) merge sentence sets
	* 2) remove all the acesses to the varibles defined in the local-scope
	* @return is_okay
	*/
	public static boolean merge_for_sentblock(ast_sentblock s, gm_rwinfo_map target, gm_rwinfo_map old, boolean is_reduce) {
		boolean is_okay = true;
		for (gm_symtab_entry e : old.keySet()) {
			boolean is_current_level;
			// check e belongs to current scope
			if (e.getType().is_property()) {
				is_current_level = s.get_symtab_field().is_entry_in_the_tab(e);
			} else {
				is_current_level = s.get_symtab_var().is_entry_in_the_tab(e);
			}

			if (is_current_level) // no need to propagate to upper level
			{
				// printf("!!!%s\n",e->getId()->get_orgname());
				continue;
			}

			// add copy of access info to the target set
			gm_rwinfo_list l = old.get(e);
			for (gm_rwinfo info : l) {
				gm_rwinfo copy = info.copy();
				boolean b = GlobalMembersGm_rw_analysis.gm_add_rwinfo_to_set(target, e, copy, is_reduce);
				is_okay = is_okay && b;
			}
		}
		return is_okay;
	}

	/**
	* AST_WHILE
	* while(expr) SB or do SB while (expr);
	* 1) copy sentence-block (conditionally for while, always for do-while)
	* 2) add expr
	* @return is_okay
	*/
	public static boolean merge_all(gm_rwinfo_map target, gm_rwinfo_map old, boolean is_reduce) {
		boolean is_okay = true;

		for (gm_symtab_entry e : old.keySet()) {
			// add copy of access info to the target set
			gm_rwinfo_list l = old.get(e);
			for (gm_rwinfo info : l) {
				gm_rwinfo copy = info.copy();
				is_okay = GlobalMembersGm_rw_analysis.gm_add_rwinfo_to_set(target, e, copy, is_reduce) && is_okay;
			}
		}
		return is_okay;
	}

	// body sentence - top
	public static boolean merge_body(gm_rwinfo_map R, gm_rwinfo_map W, gm_rwinfo_map D, gm_rwinfo_map M, ast_sent s, boolean is_conditional) {
		
		gm_rwinfo_sets sets2 = GlobalMembersGm_rw_analysis.get_rwinfo_sets(s);
		gm_rwinfo_map R2 = sets2.read_set;
		gm_rwinfo_map W2 = sets2.write_set;
		gm_rwinfo_map D2 = sets2.reduce_set;
		gm_rwinfo_map M2 = sets2.mutate_set;
		boolean is_okay = true;

		if (!is_conditional) {
			// copy as is
			is_okay = GlobalMembersGm_rw_analysis.merge_all(R, R2, false) && is_okay; 
			is_okay = GlobalMembersGm_rw_analysis.merge_all(W, W2, false) && is_okay;
			is_okay = GlobalMembersGm_rw_analysis.merge_all(D, D2, true) && is_okay;
			is_okay = GlobalMembersGm_rw_analysis.merge_all(M, M2, false) && is_okay;
		} else {
			// copy and change it as conditional
			is_okay = GlobalMembersGm_rw_analysis.merge_for_if(R, R2, false) && is_okay; 
			is_okay = GlobalMembersGm_rw_analysis.merge_for_if(W, W2, false) && is_okay;
			is_okay = GlobalMembersGm_rw_analysis.merge_for_if(D, D2, true) && is_okay;
			is_okay = GlobalMembersGm_rw_analysis.merge_for_if(M, M2, false) && is_okay;
		}

		return is_okay;
	}

	/**
	* AST_FOREACH<br>
	* foreach(X)<filter> SB<br>
	* 1) add filter to readset<br>
	* 2) copy contents of sentence-block (add conditional flag if filter
	* exists)<br>
	* 3) Resolve all the references driven via current iterator<br>
	* 3b) Resolve all the references driven via outside iterator --> all become
	* random (if parallel)<br>
	* 4) Create bound-set<br>
	*
	* e.g.) Foreach (n:G.Nodes) <<- at here<br>
	* A += n.val @ n;<br>
	* [A Reduce n ---> write to A ]<br>
	* [val Read via n ---> linear read]<br>
	*
	* e.g.2) Foreach (n:...) {<br>
	* Foreach (t:G.Nodes/n.Nbrs ) { <<- at here<br>
	* t.A = ==> linear/random write<br>
	* = t.A ==> linear/random read<br>
	* n.A = ==> random write<br>
	* = n.A ==> random read<br>
	* x ==> write<br>
	* = x ==> read<br>
	* } }<br>
	*
	* e.g.3) Foreach (n:...) {<br>
	* For (t:G.Nodes/n.Nbrs ) { <<- at here<br>
	* t.A = ==> linear/random write<br>
	* = t.A ==> linear/random read<br>
	* n.A = ==> write via n<br>
	* = n.A ==> read via n<br>
	* x ==> write<br>
	* = x ==> read<br>
	* } }
	*/
	public static boolean cleanup_iterator_access(ast_id iter, gm_rwinfo_map T_temp, gm_rwinfo_map T, GMTYPE_T iter_type, boolean is_parallel) {
		
		boolean is_okay = true;

		gm_symtab_entry iter_sym = iter.getSymInfo();
		gm_range_type_t range = GlobalMembersGm_rw_analysis.gm_get_range_from_itertype(iter_type);

		for (gm_symtab_entry sym : T_temp.keySet()) {
			gm_rwinfo_list l = T_temp.get(sym);
			if (sym == iter_sym) // direct reading of iterator
				continue;
			for (gm_rwinfo e : l) {
				gm_rwinfo cp = e.copy();
				if (cp.driver != null)
					// replace access from this iterator
					if (cp.driver == iter_sym) 
					{
						cp.driver = null;
						cp.access_range = range;
					} else if (cp.driver == null) {
						if (cp.access_range == GM_RANGE_SINGLE) {
							// scalar, do nothing
						} else if (is_parallel) {
							// printf("sym = %s!!! %p line:%d, col:%d\n",
							// sym->getId()->get_genname(), e->driver,
							// e->location->get_line(), e->location->get_col());
							cp.access_range = GM_RANGE_RANDOM;
							cp.driver = null;
						}
					} else if (is_parallel) {
						// cp->access_range = GM_RANGE_RANDOM;
						// cp->driver = NULL;
					}
				is_okay = GlobalMembersGm_rw_analysis.gm_add_rwinfo_to_set(T, sym, cp, false) && is_okay;
			}
		}
		return is_okay;
	}

	/** (called after cleanup_iterator_access if called)
	* replace LEVEL(_UP/_DOWN) -> (LINEAR + conditional)
	*/
	public static void cleanup_iterator_access_bfs(gm_rwinfo_map T) {
		// bfs iter ==> conditional, linear iteration
		boolean new_always = false;
		gm_range_type_t new_range = GM_RANGE_LINEAR; // G.Nodes
														// or
														// G.Edges

		for (gm_symtab_entry key : T.keySet()) {
			boolean is_target = false;
			boolean is_already = false;
			ast_id location = null;
			gm_rwinfo_list l = T.get(key);
			// remove all items that are LEVEL_UP/DOWN
			while (!l.isEmpty()) {
				gm_rwinfo e = l.removeFirst();
				if ((e.access_range == GM_RANGE_LEVEL) || (e.access_range == GM_RANGE_LEVEL_UP) || (e.access_range == GM_RANGE_LEVEL_DOWN)) {
					is_target = true;
					location = e.location;
					continue;
				} else if ((e.access_range == GM_RANGE_LINEAR) && (e.always == false)) {
					is_already = true;
				}
			}
			if (is_target && !is_already) {
				gm_rwinfo new_entry = gm_rwinfo.new_range_inst(new_range, new_always, location);
				l.addLast(new_entry);
			}
		}
	}

	/** Nodes or NBRS - bound-set for Foreach - write - reduce map of the
	* Foreach-statement - reduce map of the body
	*/
	public static boolean cleanup_iterator_access_reduce(ast_id iter, gm_rwinfo_map D_temp, gm_rwinfo_map D, gm_rwinfo_map W, gm_rwinfo_map B,
			GMTYPE_T iter_type, boolean is_parallel) {
		boolean is_okay = true;
		gm_symtab_entry iter_sym = iter.getSymInfo();
		gm_range_type_t range = GlobalMembersGm_rw_analysis.gm_get_range_from_itertype(iter_type);

		for (gm_symtab_entry sym : D_temp.keySet()) {
			gm_rwinfo_list l = D_temp.get(sym);
			for (gm_rwinfo e : l) {
				gm_rwinfo cp = e.copy();

				// X.val <= .... @ Y
				// X.val <= .... @ X
				if (cp.driver == iter_sym) // replace access from this iterator
				{
					cp.driver = null;
					cp.access_range = range;
				} else if (is_parallel) {
					if (cp.driver == null) {
						if (cp.access_range != GM_RANGE_SINGLE) {
							cp.access_range = GM_RANGE_RANDOM; // scalar
																// access
																// becomes
																// random
																// access
						}
					} else if (!cp.driver.getType().is_node_edge_iterator()) {
						cp.access_range = GM_RANGE_RANDOM;
						cp.driver = null;
					}
				}

				// X.val <= .... @ X
				// Y.val <= .... @ X
				// (random/linear) <= .... @X
				if (cp.bound_symbol == iter_sym) {
					// change to write
					cp.bound_symbol = null;
					cp.reduce_op = GM_REDUCE_T.GMREDUCE_NULL;

					// -------------------------------
					// add to my 'bound' set
					// -------------------------------
					gm_rwinfo cp2 = e.copy();
					boolean b = GlobalMembersGm_rw_analysis.gm_add_rwinfo_to_set(B, sym, cp2, true);
					assert b == true;
				}

				if (cp.bound_symbol == null)
					is_okay = GlobalMembersGm_rw_analysis.gm_add_rwinfo_to_set(W, sym, cp, false) && is_okay;
				else
					is_okay = GlobalMembersGm_rw_analysis.gm_add_rwinfo_to_set(D, sym, cp, true) && is_okay;
			}
		}
		return is_okay;
	}
	
	private static boolean is_modified_with_condition(ast_sent S, gm_symtab_entry e, gm_rwinfo_query Q) {
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
}