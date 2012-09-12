package frontend;

import static backend_cpp.DefineConstants.GM_BLTIN_MUTATE_GROW;
import static backend_cpp.DefineConstants.GM_BLTIN_MUTATE_SHRINK;
import static frontend.gm_range_type_t.GM_RANGE_LEVEL;
import static frontend.gm_range_type_t.GM_RANGE_LEVEL_DOWN;
import static frontend.gm_range_type_t.GM_RANGE_LEVEL_UP;
import static frontend.gm_range_type_t.GM_RANGE_LINEAR;
import static frontend.gm_range_type_t.GM_RANGE_RANDOM;
import static frontend.gm_range_type_t.GM_RANGE_SINGLE;
import static inc.gm_assignment_location_t.GMASSIGN_LHS_MAP;
import inc.GMTYPE_T;
import inc.GM_REDUCE_T;
import inc.gm_assignment_location_t;

import java.util.HashMap;
import java.util.LinkedList;

import ast.AST_NODE_TYPE;
import ast.ast_assign;
import ast.ast_bfs;
import ast.ast_call;
import ast.ast_expr;
import ast.ast_expr_builtin;
import ast.ast_expr_builtin_field;
import ast.ast_expr_foreign;
import ast.ast_expr_mapaccess;
import ast.ast_expr_reduce;
import ast.ast_field;
import ast.ast_foreach;
import ast.ast_foreign;
import ast.ast_id;
import ast.ast_if;
import ast.ast_mapaccess;
import ast.ast_node;
import ast.ast_nop;
import ast.ast_return;
import ast.ast_sent;
import ast.ast_sentblock;
import ast.ast_while;
import ast.gm_rwinfo_list;
import ast.gm_rwinfo_map;

import common.GM_ERRORS_AND_WARNINGS;
import common.gm_apply;
import common.gm_builtin_def;
import common.gm_error;
import common.gm_traverse;
//----------------------------------------------------
// Traverse for Analysis
// (Should be 'post-applying')
//----------------------------------------------------
public class gm_rw_analysis extends gm_apply {
	
	private boolean _succ = true;
	
	@Override
	public boolean apply(ast_sent s) {
		boolean b = true;
		switch (s.get_nodetype()) {
		case AST_VARDECL: // no read/write done at declaration
			return true;
		case AST_NOP:
			b = apply_nop((ast_nop) s);
			_succ = _succ && b;
			return b;
		case AST_ASSIGN:
			b = apply_assign((ast_assign) s);
			_succ = _succ && b;
			return b;
		case AST_CALL:
			b = apply_call((ast_call) s);
			_succ = _succ && b;
			return b;
		case AST_SENTBLOCK:
			b = apply_sentblock((ast_sentblock) s);
			_succ = _succ && b;
			return b;
		case AST_IF:
			b = apply_if((ast_if) s);
			_succ = _succ && b;
			return b;
		case AST_WHILE:
			b = apply_while((ast_while) s);
			_succ = _succ && b;
			return b;
		case AST_FOREACH:
			b = apply_foreach((ast_foreach) s);
			_succ = _succ && b;
			return b;
		case AST_BFS:
			b = apply_bfs((ast_bfs) s);
			_succ = _succ && b;
			return b;
		case AST_RETURN:
			b = apply_return((ast_return) s);
			_succ = _succ && b;
			return b;
		case AST_FOREIGN:
			b = apply_foreign((ast_foreign) s);
			_succ = _succ && b;
			return b;
		default:
			assert false;
			return b;
		}
	}

	public final boolean is_success() {
		return _succ;
	}

	private boolean apply_if(ast_if i) {
		
		boolean is_okay = true;
		gm_rwinfo_sets sets = get_rwinfo_sets(i);

		gm_rwinfo_map R = sets.read_set;
		gm_rwinfo_map W = sets.write_set;
		gm_rwinfo_map D = sets.reduce_set;
		gm_rwinfo_map M = sets.mutate_set;

		// (1) Add expr into read set
		ast_expr e = i.get_cond();
		traverse_expr_for_readset_adding(e, R);

		// (2) Merge sets from then part and else part
		if (i.get_else() != null) {
			gm_rwinfo_sets S1 = get_rwinfo_sets(i.get_then());
			gm_rwinfo_sets S2 = get_rwinfo_sets(i.get_else());
			is_okay = is_okay && merge_for_if_else(R, S1.read_set, S2.read_set, false);
			is_okay = is_okay && merge_for_if_else(W, S1.write_set, S2.write_set, false);
			is_okay = is_okay && merge_for_if_else(D, S1.reduce_set, S2.reduce_set, true);
			is_okay = is_okay && merge_for_if_else(M, S1.mutate_set, S2.mutate_set, false);
		} else {
			gm_rwinfo_sets S1 = get_rwinfo_sets(i.get_then());
			is_okay = is_okay && merge_for_if(R, S1.read_set, false);
			is_okay = is_okay && merge_for_if(W, S1.write_set, false);
			is_okay = is_okay && merge_for_if(D, S1.reduce_set, true);
			is_okay = is_okay && merge_for_if(M, S1.mutate_set, false);
		}

		return is_okay;
	}

	// -----------------------------------------------------------------------------
	// AST_ASSIGN
	// LHS = RHS
	// 1) RHS (expr) goes to readset
	// 2) LHS (scala/field) goes to writeset or defer,set
	// (driver of LHS goes to readset)
	// -----------------------------------------------------------------------------
	private boolean apply_assign(ast_assign a) {
		boolean is_okay = true;
		gm_rwinfo_sets sets = get_rwinfo_sets(a);

		gm_rwinfo_map R = sets.read_set;
		gm_rwinfo_map W = sets.write_set;
		gm_rwinfo_map D = sets.reduce_set;

		// (1) LHS
		boolean is_reduce = (a.is_reduce_assign() || a.is_defer_assign()) && !a.is_map_entry_assign();
		gm_symtab_entry bound_sym = null;
		GM_REDUCE_T bound_op = GM_REDUCE_T.GMREDUCE_NULL;
		if (is_reduce) {
			assert a.get_bound() != null;
			bound_sym = a.get_bound().getSymInfo();
			bound_op = a.get_reduce_type();
		}

		gm_symtab_entry target_sym;
		@SuppressWarnings("unused")
		gm_symtab_entry graph_sym;
		gm_rwinfo new_entry;
		boolean is_group_assign = false;
		if (a.get_lhs_type() == gm_assignment_location_t.GMASSIGN_LHS_SCALA) {
			target_sym = a.get_lhs_scala().getSymInfo();
			new_entry = gm_rwinfo.new_scala_inst(a.get_lhs_scala(), bound_op, bound_sym);
	    } else if (a.get_lhs_type() == GMASSIGN_LHS_MAP) {
	        ast_mapaccess mapAccess = a.to_assign_mapentry().get_lhs_mapaccess();
	        target_sym = mapAccess.get_map_id().getSymInfo();
	        new_entry = gm_rwinfo.new_scala_inst(mapAccess.get_map_id(), bound_op, bound_sym);//TODO
	    } else {
			target_sym = a.get_lhs_field().get_second().getSymInfo();
			gm_symtab_entry iter_sym = a.get_lhs_field().get_first().getSymInfo();

			if (iter_sym.getType().is_graph()) {
				// [depricated; group assignment is expanded before RW analysis]
				assert false;
				assert !is_reduce;
				is_group_assign = true;
				graph_sym = a.get_lhs_field().get_first().getSymInfo();
				new_entry = gm_rwinfo.new_range_inst(gm_range_type_t.GM_RANGE_LINEAR, true, a.get_lhs_field().get_first());
			} else {
				new_entry = gm_rwinfo.new_field_inst(iter_sym, a.get_lhs_field().get_first(), bound_op, bound_sym);

				gm_symtab_entry driver_sym = a.get_lhs_field().get_first().getSymInfo();
				gm_rwinfo driver_entry = gm_rwinfo.new_scala_inst(a.get_lhs_field().get_first());

				// add driver to read-set
				is_okay = is_okay && gm_add_rwinfo_to_set(R, driver_sym, driver_entry, false);

				// no need to add drivers in lhs_list (if any) since all drivers
				// in the lhs are same
			}
		}

		if (is_reduce) {
			is_okay = is_okay && gm_add_rwinfo_to_set(D, target_sym, new_entry, true);

			if (a.is_argminmax_assign()) // lhs list
			{
				gm_symtab_entry org_sym = target_sym;
				LinkedList<ast_node> L = a.get_lhs_list();
				for (ast_node n : L) {
					if (n.get_nodetype() == AST_NODE_TYPE.AST_ID) {
						ast_id id = (ast_id) n;
						target_sym = id.getSymInfo();
						new_entry = gm_rwinfo.new_scala_inst(id, bound_op, bound_sym, true, org_sym);
					} else {
						assert n.get_nodetype() == AST_NODE_TYPE.AST_FIELD;
						ast_field f = (ast_field) n;
						target_sym = f.get_second().getSymInfo();
						gm_symtab_entry driver_sym = f.get_first().getSymInfo();
						new_entry = gm_rwinfo.new_field_inst(driver_sym, f.get_first(), bound_op, bound_sym, true, org_sym);
					}
					is_okay = is_okay && gm_add_rwinfo_to_set(D, target_sym, new_entry, true);
				}
			}

		} else {
			is_okay = is_okay && gm_add_rwinfo_to_set(W, target_sym, new_entry, false);
		}

		// (2) RHS
		ast_expr rhs = a.get_rhs();
		if (is_group_assign) // [deprecated]
		{
			assert false;
			throw new AssertionError();
			// dead code range_cond_t RR = new
			// range_cond_t(gm_range_type_t.GM_RANGE_LINEAR, true);
			// dead code
			// Default_DriverMap.put(graph_sym, RR);
		}
		traverse_expr_for_readset_adding(rhs, R);
		if (is_group_assign) // [deprecated]
		{
			assert false;
			throw new AssertionError();
			// dead code
			// Default_DriverMap.remove(graph_sym);
		}

		return is_okay;
	}

	private boolean apply_sentblock(ast_sentblock s) {
		boolean is_okay = true;
		gm_rwinfo_sets sets = get_rwinfo_sets(s);

		gm_rwinfo_map R = sets.read_set;
		gm_rwinfo_map W = sets.write_set;
		gm_rwinfo_map D = sets.reduce_set;
		gm_rwinfo_map M = sets.mutate_set;

		LinkedList<ast_sent> sents = s.get_sents();
		for (ast_sent sent : sents) {
			gm_rwinfo_sets sets2 = get_rwinfo_sets(sent);

			gm_rwinfo_map R2 = sets2.read_set;
			gm_rwinfo_map W2 = sets2.write_set;
			gm_rwinfo_map D2 = sets2.reduce_set;
			gm_rwinfo_map M2 = sets2.mutate_set;

			is_okay = merge_for_sentblock(s, R, R2, false) && is_okay;
			is_okay = merge_for_sentblock(s, W, W2, false) && is_okay;
			is_okay = merge_for_sentblock(s, D, D2, true) && is_okay;
			is_okay = merge_for_sentblock(s, M, M2, false) && is_okay;
		}
		return is_okay;
	}

	private boolean apply_while(ast_while a) {
		boolean is_okay = true;
		gm_rwinfo_sets sets = get_rwinfo_sets(a);

		gm_rwinfo_map R = sets.read_set;
		gm_rwinfo_map W = sets.write_set;
		gm_rwinfo_map D = sets.reduce_set;
		gm_rwinfo_map M = sets.mutate_set;

		ast_sent s = a.get_body();
		ast_expr e = a.get_cond();
		boolean is_do_while = a.is_do_while(); // do-while or while?

		// Add expr into read set
		assert e != null;
		traverse_expr_for_readset_adding(e, R);

		// copy (conditionally) body
		is_okay = merge_body(R, W, D, M, s, !is_do_while);

		return is_okay;
	}

	private boolean apply_foreach(ast_foreach a) {
		boolean is_okay = true;
		gm_rwinfo_sets sets = get_rwinfo_sets(a);

		gm_rwinfo_map R = sets.read_set;
		gm_rwinfo_map W = sets.write_set;
		gm_rwinfo_map D = sets.reduce_set;
		gm_rwinfo_map M = sets.mutate_set;
		
		assert R.size() == 0;
		assert W.size() == 0;
		assert D.size() == 0;
		assert M.size() == 0;

		// make temporary copy
		gm_rwinfo_map R_temp = new gm_rwinfo_map();
		gm_rwinfo_map W_temp = new gm_rwinfo_map();
		gm_rwinfo_map D_temp = new gm_rwinfo_map();
		gm_rwinfo_map M_temp = new gm_rwinfo_map();

		if (a.get_filter() != null)
			traverse_expr_for_readset_adding(a.get_filter(), R_temp);

		// add source to the readset
		{
			ast_id source = a.get_source();
			gm_rwinfo new_entry = gm_rwinfo.new_scala_inst(source);
			gm_symtab_entry sym = source.getSymInfo();
			gm_add_rwinfo_to_set(R_temp, sym, new_entry, false);
		}

		boolean is_conditional = (a.get_filter() != null) || (a.get_iter_type().is_collection_iter_type());
		is_okay = merge_body(R_temp, W_temp, D_temp, M_temp, a.get_body(), is_conditional);

		// 3) Eliminate access driven by the current iterator
		// 4) And construct bound set
		// printf("foreach: %s, iter_type = %s\n",
		// a->get_iterator()->get_genname(),
		// gm_get_type_string(a->get_iter_type()));
		gm_rwinfo_map B = gm_get_bound_set_info(a).bound_set;
		is_okay = cleanup_iterator_access(a.get_iterator(), R_temp, R, a.get_iter_type(), a.is_parallel()) && is_okay;
		is_okay = cleanup_iterator_access(a.get_iterator(), W_temp, W, a.get_iter_type(), a.is_parallel()) && is_okay;
		is_okay = cleanup_iterator_access_reduce(a.get_iterator(), D_temp, D, W, B, a.get_iter_type(), a.is_parallel()) && is_okay;
		is_okay = cleanup_iterator_access(a.get_iterator(), M_temp, M, a.get_iter_type(), a.is_parallel()) && is_okay;

		// printf("R:");gm_print_rwinfo_set(R);
		// printf("done\n");

		return is_okay;
	}

	private boolean apply_bfs(ast_bfs a) {
		boolean is_okay = true;
		gm_rwinfo_sets sets = get_rwinfo_sets(a);

		gm_rwinfo_map R = sets.read_set;
		gm_rwinfo_map W = sets.write_set;
		gm_rwinfo_map D = sets.reduce_set;
		gm_rwinfo_map M = sets.mutate_set;
		
		assert R.size() == 0;
		assert W.size() == 0;
		assert D.size() == 0;
		assert M.size() == 0;

		// make temporary copy
		gm_rwinfo_map R_temp = new gm_rwinfo_map();
		gm_rwinfo_map W_temp = new gm_rwinfo_map();
		gm_rwinfo_map D_temp1 = new gm_rwinfo_map();
		gm_rwinfo_map D_temp2 = new gm_rwinfo_map();
		gm_rwinfo_map M_temp = new gm_rwinfo_map();

		GMTYPE_T iter_type = a.get_iter_type(); // should be GMTYPE_NODEITER_BFS
												// ||
		// GMTYPE_NODEIER_DFS
		gm_symtab_entry it = a.get_iterator().getSymInfo();
		assert it != null;

		if (a.get_navigator() != null) {
			range_cond_t R_alt = new range_cond_t(gm_range_type_t.GM_RANGE_LEVEL_DOWN, true);
			Default_DriverMap.put(it, R_alt);
			traverse_expr_for_readset_adding(a.get_navigator(), R_temp);
			Default_DriverMap.remove(it);
		}

		if (a.get_f_filter() != null) {
			range_cond_t R_alt = new range_cond_t(gm_get_range_from_itertype(iter_type), true);
			Default_DriverMap.put(it, R_alt);
			traverse_expr_for_readset_adding(a.get_f_filter(), R_temp);
			Default_DriverMap.remove(it);
		}
		if (a.get_b_filter() != null) {
			range_cond_t R_alt = new range_cond_t(gm_get_range_from_itertype(iter_type), true);
			Default_DriverMap.put(it, R_alt);
			traverse_expr_for_readset_adding(a.get_b_filter(), R_temp);
			Default_DriverMap.remove(it);
		}

		boolean is_conditional = true;
		// !((a->get_filter() == NULL) && (a->get_node_cond() == NULL) &&
		// (a->get_edge_cond() == NULL)) ;
		if (a.get_fbody() != null) {
			is_okay = merge_body(R_temp, W_temp, D_temp1, M_temp, a.get_fbody(), is_conditional) && is_okay;
		}
		if (a.get_bbody() != null) {
			is_okay = merge_body(R_temp, W_temp, D_temp2, M_temp, a.get_bbody(), is_conditional) && is_okay;
		}

		// [TODO: reduce operation bound to BFS]
		// [one bound for f-body, the other bound for b-body]
		gm_rwinfo_map B = gm_get_bound_set_info(a).bound_set;
		gm_rwinfo_map B2 = gm_get_bound_set_info(a).bound_set_back;

		is_okay = cleanup_iterator_access(a.get_iterator(), R_temp, R, iter_type, a.is_parallel()) && is_okay;
		is_okay = cleanup_iterator_access(a.get_iterator(), W_temp, W, iter_type, a.is_parallel()) && is_okay;
		is_okay = cleanup_iterator_access_reduce(a.get_iterator(), D_temp1, D, W, B, iter_type, a.is_parallel()) && is_okay;
		is_okay = cleanup_iterator_access_reduce(a.get_iterator(), D_temp2, D, W, B2, iter_type, a.is_parallel()) && is_okay;
		is_okay = cleanup_iterator_access(a.get_iterator(), M_temp, M, iter_type, a.is_parallel()) && is_okay;

		cleanup_iterator_access_bfs(R);
		cleanup_iterator_access_bfs(W);

		return is_okay;
	}

	private boolean apply_return(ast_return r) {
		gm_rwinfo_sets sets = get_rwinfo_sets(r);
		gm_rwinfo_map R = sets.read_set;

		ast_expr rhs = r.get_expr();
		if (rhs != null)
			traverse_expr_for_readset_adding(rhs, R);
		return true;
	}

	private boolean apply_nop(ast_nop n) {
		n.do_rw_analysis();
		return true;
	}

	private boolean apply_call(ast_call c) {
		assert c.is_builtin_call();
		gm_rwinfo_sets sets = get_rwinfo_sets(c);

		gm_rwinfo_map R = sets.read_set;
		gm_rwinfo_map M = sets.mutate_set;

		ast_expr e = c.get_builtin();
		traverse_expr_for_readset_adding(e, R);
		ast_expr_builtin builtin_expr = (ast_expr_builtin) e;
		gm_builtin_def def = builtin_expr.get_builtin_def();

		boolean is_okay = true;
		int mutate_direction = def.find_info_int("GM_BLTIN_INFO_MUTATING");

		if (mutate_direction == GM_BLTIN_MUTATE_GROW || mutate_direction == GM_BLTIN_MUTATE_SHRINK) {

			ast_id driver;
			if (builtin_expr.driver_is_field())
				driver = ((ast_expr_builtin_field) builtin_expr).get_field_driver().get_second();
			else
				driver = builtin_expr.get_driver();
			gm_rwinfo new_entry = gm_rwinfo.new_builtin_inst(driver, mutate_direction);
			gm_symtab_entry sym = driver.getSymInfo();
			is_okay = gm_add_rwinfo_to_set(M, sym, new_entry, false);
		}

		return is_okay;
	}

	// -----------------------------------------------------------------------------
	// AST_ASSIGN
	// [EXPR]::[LHS list]
	// -----------------------------------------------------------------------------
	private boolean apply_foreign(ast_foreign f) {
		
		gm_rwinfo_sets sets = get_rwinfo_sets(f);

		gm_rwinfo_map R = sets.read_set;
		gm_rwinfo_map W = sets.write_set;

		gm_symtab_entry bound_sym = null;
		gm_symtab_entry target_sym = null;
		GM_REDUCE_T bound_op = GM_REDUCE_T.GMREDUCE_NULL;
		boolean is_okay = true;

		// -----------------------------------------
		// Mutation LIST
		// -----------------------------------------
		LinkedList<ast_node> L = f.get_modified();
		gm_rwinfo new_entry = null;
		for (ast_node node : L) {
			if (node.get_nodetype() == AST_NODE_TYPE.AST_ID) {
				ast_id id = (ast_id) node;
				target_sym = id.getSymInfo();
				assert target_sym != null;
				new_entry = gm_rwinfo.new_scala_inst(id, GM_REDUCE_T.GMREDUCE_NULL, null);
			} else if (node.get_nodetype() == AST_NODE_TYPE.AST_FIELD) {
				ast_field fld = (ast_field) node;
				target_sym = fld.get_second().getSymInfo();
				gm_symtab_entry iter_sym = fld.get_first().getSymInfo();
				assert target_sym != null;
				assert iter_sym != null;
				assert !iter_sym.getType().is_graph();

				new_entry = gm_rwinfo.new_field_inst(iter_sym, fld.get_first(), bound_op, bound_sym);
			}
			is_okay = gm_add_rwinfo_to_set(W, target_sym, new_entry, false) && is_okay;
		}

		// -----------------------------------------
		// Expression
		// -----------------------------------------
		traverse_expr_for_readset_adding(f.get_expr(), R);

		return is_okay;

	}

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

					is_error = is_reduce_error(e2, new_entry);
					if (is_error) {
						if (new_entry != null)
							new_entry.dispose(); // not required
						return false;
					}

				}

				if (is_same_entry(e2, new_entry)) {
					if (new_entry != null)
						new_entry.dispose(); // not required
					return true;
				} // old entry is wider
				else if (is_wider_entry(new_entry, e2)) {
					if (new_entry != null)
						new_entry.dispose(); // drop new entry
					return true;
				} // new_entry is wider
				else if (is_wider_entry(e2, new_entry)) {
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
		return get_rwinfo_sets(n);
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
					is_okay = is_okay && gm_add_rwinfo_to_set(Target, sym, copy, is_reduce);
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
							if (is_same_entry(copy, else_info)) {
								found = true;
								break;
							}
						}
						if (!found) // add it as 'non-conditional' access
							copy.always = false;

						is_okay = is_okay && gm_add_rwinfo_to_set(Target, sym, copy, is_reduce);
					} // alrady conditional. simply add in the new set
					else {
						is_okay = is_okay && gm_add_rwinfo_to_set(Target, sym, copy, is_reduce);
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
				is_okay = is_okay && gm_add_rwinfo_to_set(Target, sym, copy, is_reduce);
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
			gm_add_rwinfo_to_set(rset, sym, new_entry, false);
			break;
		}
		case GMEXPR_FIELD:
			traverse_expr_for_readset_adding_field(e, rset, DrvMap);
			break;
		case GMEXPR_MAPACCESS:
			ast_mapaccess mapAccess = ((ast_expr_mapaccess) e).get_mapaccess();
			traverse_expr_for_readset_adding(mapAccess.get_key_expr(), rset, DrvMap);
		case GMEXPR_UOP:
		case GMEXPR_LUOP:
			traverse_expr_for_readset_adding(e.get_left_op(), rset, DrvMap);
			break;
		case GMEXPR_BIOP:
		case GMEXPR_LBIOP:
		case GMEXPR_COMP:
			traverse_expr_for_readset_adding(e.get_left_op(), rset, DrvMap);
			traverse_expr_for_readset_adding(e.get_right_op(), rset, DrvMap);
			break;
		case GMEXPR_TER: {
			traverse_expr_for_readset_adding(e.get_cond_op(), rset, DrvMap);
			gm_rwinfo_map R1 = new gm_rwinfo_map();
			traverse_expr_for_readset_adding(e.get_left_op(), R1, DrvMap);
			gm_rwinfo_map R2 = new gm_rwinfo_map();
			traverse_expr_for_readset_adding(e.get_right_op(), R2, DrvMap);
			merge_for_if_else(rset, R1, R2, false);
			break;
		}
		case GMEXPR_REDUCE:
			traverse_expr_for_readset_adding_reduce((ast_expr_reduce) e, rset, DrvMap);
			break;
		case GMEXPR_BUILTIN: // built-ins does not read or modify anything other
								// than arguments
			traverse_expr_for_readset_adding_builtin((ast_expr_builtin) e, rset, DrvMap);
			break;
		case GMEXPR_BUILTIN_FIELD:
			traverse_expr_for_readset_adding_builtin((ast_expr_builtin) e, rset, DrvMap);
			traverse_expr_for_readset_adding_field(e, rset, DrvMap);
			break;
		case GMEXPR_FOREIGN: // read symbols in the foreign
			traverse_expr_for_readset_adding_foreign((ast_expr_foreign) e, rset, DrvMap);
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
			gm_add_rwinfo_to_set(rset, driver_sym, driver_entry);

		} // temporary driver or vector driver
		else {
			gm_range_type_t range_type = DrvMap.get(iter_sym).range_type;
			boolean always = DrvMap.get(iter_sym).is_always;
			new_entry = gm_rwinfo.new_range_inst(range_type, always, e.get_field().get_first());
		}

		gm_add_rwinfo_to_set(rset, field_sym, new_entry);
	}

	public static void traverse_expr_for_readset_adding_builtin(ast_expr_builtin builtin, gm_rwinfo_map rset, HashMap<gm_symtab_entry, range_cond_t> DrvMap) {
		// add every arguments in the readset
		LinkedList<ast_expr> args = builtin.get_args();
		for (ast_expr a : args) {
			traverse_expr_for_readset_adding(a, rset, DrvMap);
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
				gm_add_rwinfo_to_set(rset, sym, new_entry, false);
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
				gm_add_rwinfo_to_set(rset, field_sym, new_entry);
			}
		}
	}

	public static void traverse_expr_for_readset_adding_reduce(ast_expr_reduce e2, gm_rwinfo_map rset, HashMap<gm_symtab_entry, range_cond_t> DrvMap) {
		gm_symtab_entry it = e2.get_iterator().getSymInfo();
		GMTYPE_T iter_type = e2.get_iter_type();
		ast_expr f = e2.get_filter();
		ast_expr b = e2.get_body();
		boolean is_conditional = (f != null) || iter_type.is_collection_iter_type();
		range_cond_t R = new range_cond_t(gm_get_range_from_itertype(iter_type), !is_conditional);
		DrvMap.put(it, R);
		traverse_expr_for_readset_adding(b, rset, DrvMap);
		DrvMap.remove(it);

		if (f != null) // filter itself is always accessed
		{
			DrvMap.put(it, R);
			R.is_always = true;
			traverse_expr_for_readset_adding(f, rset, DrvMap);
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
				is_okay = is_okay && gm_add_rwinfo_to_set(Target, sym, copy, is_reduce);
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
				boolean b = gm_add_rwinfo_to_set(target, e, copy, is_reduce);
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
				is_okay = gm_add_rwinfo_to_set(target, e, copy, is_reduce) && is_okay;
			}
		}
		return is_okay;
	}

	// body sentence - top
	public static boolean merge_body(gm_rwinfo_map R, gm_rwinfo_map W, gm_rwinfo_map D, gm_rwinfo_map M, ast_sent s, boolean is_conditional) {
		
		gm_rwinfo_sets sets2 = get_rwinfo_sets(s);
		gm_rwinfo_map R2 = sets2.read_set;
		gm_rwinfo_map W2 = sets2.write_set;
		gm_rwinfo_map D2 = sets2.reduce_set;
		gm_rwinfo_map M2 = sets2.mutate_set;
		boolean is_okay = true;

		if (!is_conditional) {
			// copy as is
			is_okay = merge_all(R, R2, false) && is_okay; 
			is_okay = merge_all(W, W2, false) && is_okay;
			is_okay = merge_all(D, D2, true) && is_okay;
			is_okay = merge_all(M, M2, false) && is_okay;
		} else {
			// copy and change it as conditional
			is_okay = merge_for_if(R, R2, false) && is_okay; 
			is_okay = merge_for_if(W, W2, false) && is_okay;
			is_okay = merge_for_if(D, D2, true) && is_okay;
			is_okay = merge_for_if(M, M2, false) && is_okay;
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
		gm_range_type_t range = gm_get_range_from_itertype(iter_type);

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
				is_okay = gm_add_rwinfo_to_set(T, sym, cp, false) && is_okay;
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
		gm_range_type_t range = gm_get_range_from_itertype(iter_type);

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
					boolean b = gm_add_rwinfo_to_set(B, sym, cp2, true);
					assert b == true;
				}

				if (cp.bound_symbol == null)
					is_okay = gm_add_rwinfo_to_set(W, sym, cp, false) && is_okay;
				else
					is_okay = gm_add_rwinfo_to_set(D, sym, cp, true) && is_okay;
			}
		}
		return is_okay;
	}
	
	private static boolean is_modified_with_condition(ast_sent S, gm_symtab_entry e, gm_rwinfo_query Q) {
		assert Q != null;
		gm_rwinfo_map W = gm_rw_analysis_check2.gm_get_write_set(S);
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