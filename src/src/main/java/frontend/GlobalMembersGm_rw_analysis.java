package frontend;

import inc.GMTYPE_T;
import inc.GM_REDUCE_T;

import java.util.HashMap;
import java.util.Iterator;
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

import common.GM_ERRORS_AND_WARNINGS;
import common.GlobalMembersGm_error;
import common.GlobalMembersGm_misc;
import common.GlobalMembersGm_traverse;

public class GlobalMembersGm_rw_analysis {

	public static gm_range_type_t gm_get_range_from_itertype(GMTYPE_T itype) {
		switch (itype) {
		case GMTYPE_NODEITER_ALL:
		case GMTYPE_EDGEITER_ALL:
			return gm_range_type_t.GM_RANGE_LINEAR;
		case GMTYPE_NODEITER_NBRS:
		case GMTYPE_NODEITER_IN_NBRS:
		case GMTYPE_EDGEITER_NBRS:
		case GMTYPE_NODEITER_COMMON_NBRS:
		case GMTYPE_EDGEITER_IN_NBRS:
			return gm_range_type_t.GM_RANGE_RANDOM;
		case GMTYPE_NODEITER_BFS:
		case GMTYPE_EDGEITER_BFS:
			return gm_range_type_t.GM_RANGE_LEVEL;
		case GMTYPE_NODEITER_UP_NBRS:
		case GMTYPE_EDGEITER_UP_NBRS:
			return gm_range_type_t.GM_RANGE_LEVEL_UP;
		case GMTYPE_NODEITER_DOWN_NBRS:
		case GMTYPE_EDGEITER_DOWN_NBRS:
			return gm_range_type_t.GM_RANGE_LEVEL_DOWN;
		case GMTYPE_NODEITER_SET:
		case GMTYPE_EDGEITER_SET:
			return gm_range_type_t.GM_RANGE_LINEAR;
		case GMTYPE_NODEITER_ORDER:
		case GMTYPE_EDGEITER_ORDER:
			return gm_range_type_t.GM_RANGE_LINEAR;
		case GMTYPE_NODEITER_SEQ:
		case GMTYPE_EDGEITER_SEQ:
			return gm_range_type_t.GM_RANGE_RANDOM;
		case GMTYPE_NODE:
		case GMTYPE_EDGE:
			return gm_range_type_t.GM_RANGE_RANDOM;
		case GMTYPE_PROPERTYITER_SET:
		case GMTYPE_PROPERTYITER_SEQ:
		case GMTYPE_PROPERTYITER_ORDER:
			return gm_range_type_t.GM_RANGE_LINEAR;
		case GMTYPE_COLLECTIONITER_SET:
		case GMTYPE_COLLECTIONITER_SEQ:
		case GMTYPE_COLLECTIONITER_ORDER:
			return gm_range_type_t.GM_RANGE_RANDOM; // TODO is there somthing
													// more suitable?
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
	public static boolean gm_add_rwinfo_to_set(HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> info_set, gm_symtab_entry sym, gm_rwinfo new_entry) {
		return gm_add_rwinfo_to_set(info_set, sym, new_entry, false);
	}

	// list of rw-info
	// map from target(symtab entry) to list of rw-info
	// (one field may have multiple access patterns)

	// C++ TO JAVA CONVERTER NOTE: Java does not allow default values for
	// parameters. Overloaded methods are inserted above.
	// ORIGINAL LINE: boolean gm_add_rwinfo_to_set(HashMap<gm_symtab_entry*,
	// LinkedList<gm_rwinfo*>*>& info_set, gm_symtab_entry* sym, gm_rwinfo*
	// new_entry, boolean is_reduce_ops = false)
	public static boolean gm_add_rwinfo_to_set(HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> info_set, gm_symtab_entry sym, gm_rwinfo new_entry,
			boolean is_reduce_ops) {
		boolean is_error = false;

		// find entry in the map
		if (!info_set.containsKey(sym)) // not found --> add new;
		{
			LinkedList<gm_rwinfo> l = new LinkedList<gm_rwinfo>();
			l.addLast(new_entry);
			info_set.put(sym, l);
		} // check entries already exists
		else {
			LinkedList<gm_rwinfo> l = info_set.get(sym);
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
					// C++ TO JAVA CONVERTER WARNING: The following line was
					// determined to be a copy assignment (rather than a
					// reference assignment) - this should be verified and a
					// 'copyFrom' method should be created if it does not yet
					// exist:
					// ORIGINAL LINE: *e2 = *new_entry;
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

	// Actual information kept for sentence
	// Three maps. (readset, writeset, reduce-set)
	public static void gm_delete_rwinfo_map(HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> m) {
		for (LinkedList<gm_rwinfo> l : m.values()) {
			for (gm_rwinfo j : l) {
				if (j != null)
					j.dispose();
			}
			l.clear();
//			if (l != null)
//				l.dispose();
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

	// -------------------------------------------------------
	// additional information for foreach statement
	// -------------------------------------------------------
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

	// for debug
	// extern void gm_print_rwinfo_set(HashMap<gm_symtab_entry,
	// LinkedList<gm_rwinfo>> m);

	// ----------------------------------------------
	// re-do rw analysis for IR tree s.
	// (result does not propagate upward from s though.)
	// ----------------------------------------------
	public static boolean gm_redo_rw_analysis(ast_sent s) {
		// nullify previous analysis. (IR tree has been modified)
		gm_delete_rw_analysis D = new gm_delete_rw_analysis();
		GlobalMembersGm_traverse.gm_traverse_sents(s, D, GlobalMembersGm_traverse.GM_POST_APPLY);

		// do-it again RW analysis
		gm_rw_analysis RWA = new gm_rw_analysis();
		GlobalMembersGm_traverse.gm_traverse_sents(s, RWA, GlobalMembersGm_traverse.GM_POST_APPLY); // post
																									// apply
		return RWA.is_success();
	}

	// --------------------------------------------------------
	// use of rw analysis result
	// { P; Q; }
	// (i.e. P, Q should belong to the same static scope level)
	//
	// Is Q dependent on P?
	// P.writeset && Q.readset ==> true dependency
	// P.writeset && Q.writeset ==> output dependency
	// P.readset && Q.writeset ==> anti dependency
	// --------------------------------------------------------
	// extern boolean gm_has_dependency(ast_sent P, ast_sent Q);

	// extern boolean gm_has_dependency(gm_rwinfo_sets P1, gm_rwinfo_sets Q1);

	// extern boolean gm_does_intersect(HashMap<gm_symtab_entry,
	// LinkedList<gm_rwinfo>> S1, HashMap<gm_symtab_entry,
	// LinkedList<gm_rwinfo>> S2, boolean regard_mutate_direction); // return
	// true, if any of they have same symbool table

	// extern HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>>
	// gm_get_write_set(ast_sent S);
	// extern HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>>
	// gm_get_reduce_set(ast_sent S);
	// extern boolean gm_is_modified(ast_sent S, gm_symtab_entry e);
	// extern boolean gm_is_modified_with_condition(ast_sent S, gm_symtab_entry
	// e, gm_rwinfo_query q);
	public static boolean gm_is_modified_always_linearly(ast_sent S, gm_symtab_entry e) {
		gm_rwinfo_query Q = new gm_rwinfo_query();
		Q.check_range(gm_range_type_t.GM_RANGE_LINEAR);
		Q.check_always(true);
		return GlobalMembersGm_rw_analysis_check2.gm_is_modified_with_condition(S, e, Q);
	}

	// -----------------------------------------------------------------------------
	// AST_IF
	// If (expr) [Then sent] [Else sent2]
	// 1) add expr into read set
	// 2) merge then-part sets and else-part sets
	// make all the accesses conditional,
	// unless both-path contain same access
	// return: is_okay
	// -----------------------------------------------------------------------------

	// C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced
	// in-line:
	// /#define TO_STR(X) #X
	// C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced
	// in-line:
	// /#define DEF_STRING(X) static const char *X = "X"
	// C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced
	// in-line:
	// /#define GM_COMPILE_STEP(CLASS, DESC) class CLASS : public
	// gm_compile_step { private: CLASS() {set_description(DESC);}public:
	// virtual void process(ast_procdef*p); virtual gm_compile_step*
	// get_instance(){return new CLASS();} static gm_compile_step*
	// get_factory(){return new CLASS();} };
	// C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced
	// in-line:
	// /#define GM_COMPILE_STEP_FACTORY(CLASS) CLASS::get_factory()
	// C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced
	// in-line:
	// /#define AUX_INFO(X,Y) "X"":""Y"
	// /#define GM_BLTIN_MUTATE_GROW 1
	// /#define GM_BLTIN_MUTATE_SHRINK 2
	// C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced
	// in-line:
	// /#define GM_BLTIN_FLAG_TRUE true

	public static boolean merge_for_if_else(HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> Target, HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> S1,
			HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> S2, boolean is_reduce) {
		boolean is_okay = true;

		// search for all elements in then-part
		for (gm_symtab_entry sym : S1.keySet()) {
			LinkedList<gm_rwinfo> l1 = S1.get(sym);
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
						LinkedList<gm_rwinfo> l2 = S2.get(sym);
						
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
			LinkedList<gm_rwinfo> l2 = S2.get(sym);
			for (gm_rwinfo e : l2) {
				gm_rwinfo copy = e.copy();
				copy.always = false; // chage it into conditional access
				is_okay = is_okay && GlobalMembersGm_rw_analysis.gm_add_rwinfo_to_set(Target, sym, copy, is_reduce);
			}
		}

		return is_okay; // returns is_okay
	}

	public static String gm_get_range_string(gm_range_type_t access_range) {
		return (access_range == gm_range_type_t.GM_RANGE_LINEAR) ? "LINEAR" : (access_range == gm_range_type_t.GM_RANGE_RANDOM) ? "RANDOM"
				: (access_range == gm_range_type_t.GM_RANGE_LEVEL) ? "LEVEL" : (access_range == gm_range_type_t.GM_RANGE_LEVEL_UP) ? "LEVEL_UP"
						: (access_range == gm_range_type_t.GM_RANGE_LEVEL_DOWN) ? "LEVEL_DOWN" : "???";
	}

	// ------------------------------------------------------------
	// Add info to set,
	// unless the same information already exists in the set
	//
	// If current info is 'wider', remove previous information
	// (i.e. conditional < always)
	// (note: cannot compare different rages: e.g. linear vs. random)
	// -----------------------------------------------------------
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

	// true if neo is wider
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

	// ---------------------------------------------------
	// true if error
	// check if two symbols bound to the same operator
	// check if two symbols bound to the same boud
	// ---------------------------------------------------
	public static boolean is_reduce_error(gm_rwinfo old, gm_rwinfo neo) {
		assert neo.bound_symbol != null;
		assert old.bound_symbol != null;

		// check if they bound to the same operator
		if (old.reduce_op != neo.reduce_op) {
			// generate error message
			assert neo.location != null;
			// assert(neo->driver->getId() != NULL);

			GlobalMembersGm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_DOUBLE_BOUND_OP, neo.location.get_line(), neo.location.get_col(),
					GlobalMembersGm_misc.gm_get_reduce_string(old.reduce_op));
			return true;
		}

		// check if they are bound to the same symbol
		if (old.bound_symbol != neo.bound_symbol) {
			// generate error message
			GlobalMembersGm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_DOUBLE_BOUND_ITOR, neo.location.get_line(), neo.location.get_col(),
					old.bound_symbol.getId().get_orgname());
			return true;
		}

		return false;
	}

	public static HashMap<gm_symtab_entry, range_cond_t> Default_DriverMap = new HashMap<gm_symtab_entry, range_cond_t>();

	public static void traverse_expr_for_readset_adding(ast_expr e, HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> rset) {
		traverse_expr_for_readset_adding(e, rset, Default_DriverMap);
	}

	// C++ TO JAVA CONVERTER NOTE: Java does not allow default values for
	// parameters. Overloaded methods are inserted above.
	// ORIGINAL LINE: void traverse_expr_for_readset_adding(ast_expr* e,
	// HashMap<gm_symtab_entry*, LinkedList<gm_rwinfo*>*>& rset,
	// HashMap<gm_symtab_entry*, range_cond_t>& DrvMap = Default_DriverMap)
	public static void traverse_expr_for_readset_adding(ast_expr e, HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> rset,
			HashMap<gm_symtab_entry, range_cond_t> DrvMap) {

		switch (e.get_opclass()) {
		case GMEXPR_ID: {
			gm_rwinfo new_entry = gm_rwinfo.new_scala_inst(e.get_id());
			gm_symtab_entry sym = e.get_id().getSymInfo();
			assert sym != null;

			boolean b = GlobalMembersGm_rw_analysis.gm_add_rwinfo_to_set(rset, sym, new_entry, false);
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
			HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> R1 = new HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>>();
			GlobalMembersGm_rw_analysis.traverse_expr_for_readset_adding(e.get_left_op(), R1, DrvMap);
			HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> R2 = new HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>>();
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

	public static void traverse_expr_for_readset_adding_field(ast_expr e, HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> rset,
			HashMap<gm_symtab_entry, range_cond_t> DrvMap) {
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

	public static void traverse_expr_for_readset_adding_builtin(ast_expr_builtin builtin, HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> rset,
			HashMap<gm_symtab_entry, range_cond_t> DrvMap) {
		// add every arguments in the readset
		LinkedList<ast_expr> args = builtin.get_args();
		for (ast_expr a : args) {
			GlobalMembersGm_rw_analysis.traverse_expr_for_readset_adding(a, rset, DrvMap);
		}
	}

	public static void traverse_expr_for_readset_adding_foreign(ast_expr_foreign f, HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> rset,
			HashMap<gm_symtab_entry, range_cond_t> DrvMap) {

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

	public static void traverse_expr_for_readset_adding_reduce(ast_expr_reduce e2, HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> rset,
			HashMap<gm_symtab_entry, range_cond_t> DrvMap) {
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

	// C++ TO JAVA CONVERTER NOTE: Java has no need of forward class
	// declarations:
	// class gm_builtin_def;

	public static boolean merge_for_if(HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> Target, HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> S1,
			boolean is_reduce) {
		boolean is_okay = true;
		// search for all elements in then-part
		// and add a copy into the target set.
		for (gm_symtab_entry sym : S1.keySet()) {
			LinkedList<gm_rwinfo> l1 = S1.get(sym);
			assert l1 != null;

			for (gm_rwinfo e : l1) {
				gm_rwinfo copy = e.copy();
				copy.always = false; // chage it into conditional access
				is_okay = is_okay && GlobalMembersGm_rw_analysis.gm_add_rwinfo_to_set(Target, sym, copy, is_reduce);
			}
		}
		return is_okay;
	}

	// -----------------------------------------------------------------------------
	// AST_SENTBLOCK
	// { s1; s2; s3;}
	// 1) merge sentence sets
	// 2) remove all the acesses to the varibles defined in the local-scope
	// return: is_okay
	// -----------------------------------------------------------------------------
	public static boolean merge_for_sentblock(ast_sentblock s, HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> target,
			HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> old, boolean is_reduce) {
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
			LinkedList<gm_rwinfo> l = old.get(e);
			for (gm_rwinfo info : l) {
				gm_rwinfo copy = info.copy();
				boolean b = GlobalMembersGm_rw_analysis.gm_add_rwinfo_to_set(target, e, copy, is_reduce);
				is_okay = is_okay && b;
			}
		}
		return is_okay;
	}

	// -----------------------------------------------------------------------------
	// AST_WHILE
	// while(expr) SB or do SB while (expr);
	// 1) copy sentence-block (conditionally for while, always for do-while)
	// 2) add expr
	// return: is_okay
	// -----------------------------------------------------------------------------
	public static boolean merge_all(HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> target, HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> old,
			boolean is_reduce) {
		boolean is_okay = true;

		for (gm_symtab_entry e : old.keySet()) {
			// add copy of access info to the target set
			LinkedList<gm_rwinfo> l = old.get(e);
			for (gm_rwinfo info : l) {
				gm_rwinfo copy = info.copy();
				is_okay = GlobalMembersGm_rw_analysis.gm_add_rwinfo_to_set(target, e, copy, is_reduce) && is_okay;
			}
		}
		return is_okay;
	}

	public static boolean merge_body(HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> R, HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> W,
			HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> D, HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> M, ast_sent s, boolean is_conditional) // body
																																						// sentence
																																						// -
																																						// top
	{
		gm_rwinfo_sets sets2 = GlobalMembersGm_rw_analysis.get_rwinfo_sets(s);
		// C++ TO JAVA CONVERTER WARNING: The following line was determined to
		// be a copy constructor call - this should be verified and a copy
		// constructor should be created if it does not yet exist:
		// ORIGINAL LINE: HashMap<gm_symtab_entry*, LinkedList<gm_rwinfo*>*>& R2
		// = sets2->read_set;
		HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> R2 = new HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>>(sets2.read_set);
		// C++ TO JAVA CONVERTER WARNING: The following line was determined to
		// be a copy constructor call - this should be verified and a copy
		// constructor should be created if it does not yet exist:
		// ORIGINAL LINE: HashMap<gm_symtab_entry*, LinkedList<gm_rwinfo*>*>& W2
		// = sets2->write_set;
		HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> W2 = new HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>>(sets2.write_set);
		// C++ TO JAVA CONVERTER WARNING: The following line was determined to
		// be a copy constructor call - this should be verified and a copy
		// constructor should be created if it does not yet exist:
		// ORIGINAL LINE: HashMap<gm_symtab_entry*, LinkedList<gm_rwinfo*>*>& D2
		// = sets2->reduce_set;
		HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> D2 = new HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>>(sets2.reduce_set);
		// C++ TO JAVA CONVERTER WARNING: The following line was determined to
		// be a copy constructor call - this should be verified and a copy
		// constructor should be created if it does not yet exist:
		// ORIGINAL LINE: HashMap<gm_symtab_entry*, LinkedList<gm_rwinfo*>*>& M2
		// = sets2->mutate_set;
		HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> M2 = new HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>>(sets2.mutate_set);
		boolean is_okay = true;

		if (!is_conditional) {
			is_okay = GlobalMembersGm_rw_analysis.merge_all(R, R2, false) && is_okay; // copy
																						// as
																						// is
			is_okay = GlobalMembersGm_rw_analysis.merge_all(W, W2, false) && is_okay;
			is_okay = GlobalMembersGm_rw_analysis.merge_all(D, D2, true) && is_okay;
			is_okay = GlobalMembersGm_rw_analysis.merge_all(M, M2, false) && is_okay;
		} else {
			is_okay = GlobalMembersGm_rw_analysis.merge_for_if(R, R2, false) && is_okay; // copy
																							// and
																							// change
																							// it
																							// as
																							// conditional
			is_okay = GlobalMembersGm_rw_analysis.merge_for_if(W, W2, false) && is_okay;
			is_okay = GlobalMembersGm_rw_analysis.merge_for_if(D, D2, true) && is_okay;
			is_okay = GlobalMembersGm_rw_analysis.merge_for_if(M, M2, false) && is_okay;
		}

		return is_okay;
	}

	// -----------------------------------------------------------------------------
	// AST_FOREACH
	// foreach(X)<filter> SB
	// 1) add filter to readset
	// 2) copy contents of sentence-block (add conditional flag if filter
	// exists)
	// 3) Resolve all the references driven via current iterator
	// 3b) Resolve all the references driven via outside iterator --> all become
	// random (if parallel)
	// 4) Create bound-set
	//
	// e.g.) Foreach (n:G.Nodes) <<- at here
	// A += n.val @ n;
	// [A Reduce n ---> write to A ]
	// [val Read via n ---> linear read]
	//
	// e.g.2) Foreach (n:...) {
	// Foreach (t:G.Nodes/n.Nbrs ) { <<- at here
	// t.A = ==> linear/random write
	// = t.A ==> linear/random read
	// n.A = ==> random write
	// = n.A ==> random read
	// x ==> write
	// = x ==> read
	// } }
	//
	// e.g.3) Foreach (n:...) {
	// For (t:G.Nodes/n.Nbrs ) { <<- at here
	// t.A = ==> linear/random write
	// = t.A ==> linear/random read
	// n.A = ==> write via n
	// = n.A ==> read via n
	// x ==> write
	// = x ==> read
	// } }
	// -----------------------------------------------------------------------------
	//
	public static boolean cleanup_iterator_access(ast_id iter, HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> T_temp,
			HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> T, GMTYPE_T iter_type, boolean is_parallel) {
		boolean is_okay = true;

		gm_symtab_entry iter_sym = iter.getSymInfo();
		gm_range_type_t range = GlobalMembersGm_rw_analysis.gm_get_range_from_itertype(iter_type);
		// printf("iter_type = %s, range = %s\n", gm_get_type_string(iter_type),
		// gm_get_range_string(range));
		for (gm_symtab_entry sym : T_temp.keySet()) {
			LinkedList<gm_rwinfo> l = T_temp.get(sym);
			if (sym == iter_sym) // direct reading of iterator
				continue;
			for (gm_rwinfo e : l) {
				gm_rwinfo cp = e.copy();
				if (cp.driver != null)
					/*
					 * printf("cp->driver = %s %p, iter_sym = %s %p\n",
					 * cp->driver->getId()->get_genname(), cp->driver,
					 * iter_sym->getId()->get_genname(), iter_sym);
					 */
					if (cp.driver == iter_sym) // replace access from this
												// iterator
					{
						cp.driver = null;
						cp.access_range = range;
					} else if (cp.driver == null) {
						if (cp.access_range == gm_range_type_t.GM_RANGE_SINGLE) {
							// scalar, do nothing
						} else if (is_parallel) {
							// printf("sym = %s!!! %p line:%d, col:%d\n",
							// sym->getId()->get_genname(), e->driver,
							// e->location->get_line(), e->location->get_col());
							cp.access_range = gm_range_type_t.GM_RANGE_RANDOM;
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

	// (called after cleanup_iterator_access if called)
	// replace LEVEL(_UP/_DOWN) -> (LINEAR + conditional)
	public static void cleanup_iterator_access_bfs(HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> T) {
		// bfs iter ==> conditional, linear iteration
		boolean new_always = false;
		gm_range_type_t new_range = gm_range_type_t.GM_RANGE_LINEAR; // G.Nodes
																		// or
																		// G.Edges

		for (gm_symtab_entry key : T.keySet()) {
			boolean is_target = false;
			boolean is_already = false;
			ast_id location = null;
			LinkedList<gm_rwinfo> l = T.get(key);
			// remove all items that are LEVEL_UP/DOWN
			while (!l.isEmpty()) {
				gm_rwinfo e = l.removeFirst();
				if ((e.access_range == gm_range_type_t.GM_RANGE_LEVEL) || (e.access_range == gm_range_type_t.GM_RANGE_LEVEL_UP)
						|| (e.access_range == gm_range_type_t.GM_RANGE_LEVEL_DOWN)) {
					is_target = true;
					location = e.location;
					continue;
				} else if ((e.access_range == gm_range_type_t.GM_RANGE_LINEAR) && (e.always == false)) {
					is_already = true;
				}
			}
			if (is_target && !is_already) {
				gm_rwinfo new_entry = gm_rwinfo.new_range_inst(new_range, new_always, location);
				l.addLast(new_entry);
			}
		}
	}

	public static boolean cleanup_iterator_access_reduce(ast_id iter, HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> D_temp,
			HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> D, HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> W,
			HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> B, GMTYPE_T iter_type, boolean is_parallel) // Nodes
																										// or
																										// NBRS
																										// -
																										// bound-set
																										// for
																										// Foreach
																										// -
																										// write
																										// -
																										// reduce
																										// map
																										// of
																										// the
																										// Foreach-statement
																										// -
																										// reduce
																										// map
																										// of
																										// the
																										// body

	{
		boolean is_okay = true;
		gm_symtab_entry iter_sym = iter.getSymInfo();
		gm_range_type_t range = GlobalMembersGm_rw_analysis.gm_get_range_from_itertype(iter_type);

		for (gm_symtab_entry sym : D_temp.keySet()) {
			LinkedList<gm_rwinfo> l = D_temp.get(sym);
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
						if (cp.access_range != gm_range_type_t.GM_RANGE_SINGLE) {
							cp.access_range = gm_range_type_t.GM_RANGE_RANDOM; // scalar
																				// access
																				// becomes
																				// random
																				// access
						}
					} else if (!cp.driver.getType().is_node_edge_iterator()) {
						cp.access_range = gm_range_type_t.GM_RANGE_RANDOM;
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
}