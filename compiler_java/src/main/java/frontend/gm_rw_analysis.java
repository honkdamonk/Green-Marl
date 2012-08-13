package frontend;

import static backend_cpp.DefineConstants.GM_BLTIN_MUTATE_GROW;
import static backend_cpp.DefineConstants.GM_BLTIN_MUTATE_SHRINK;
import inc.GMTYPE_T;
import inc.GM_REDUCE_T;
import inc.gm_assignment_location_t;

import java.util.LinkedList;

import ast.AST_NODE_TYPE;
import ast.ast_assign;
import ast.ast_bfs;
import ast.ast_call;
import ast.ast_expr;
import ast.ast_expr_builtin;
import ast.ast_expr_builtin_field;
import ast.ast_field;
import ast.ast_foreach;
import ast.ast_foreign;
import ast.ast_id;
import ast.ast_if;
import ast.ast_node;
import ast.ast_nop;
import ast.ast_return;
import ast.ast_sent;
import ast.ast_sentblock;
import ast.ast_while;
import ast.gm_rwinfo_map;

import common.gm_apply;
import common.gm_builtin_def;
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
		gm_rwinfo_sets sets = GlobalMembersGm_rw_analysis.get_rwinfo_sets(i);

		gm_rwinfo_map R = sets.read_set;
		gm_rwinfo_map W = sets.write_set;
		gm_rwinfo_map D = sets.reduce_set;
		gm_rwinfo_map M = sets.mutate_set;

		// (1) Add expr into read set
		ast_expr e = i.get_cond();
		GlobalMembersGm_rw_analysis.traverse_expr_for_readset_adding(e, R);

		// (2) Merge sets from then part and else part
		if (i.get_else() != null) {
			gm_rwinfo_sets S1 = GlobalMembersGm_rw_analysis.get_rwinfo_sets(i.get_then());
			gm_rwinfo_sets S2 = GlobalMembersGm_rw_analysis.get_rwinfo_sets(i.get_else());
			is_okay = is_okay && GlobalMembersGm_rw_analysis.merge_for_if_else(R, S1.read_set, S2.read_set, false);
			is_okay = is_okay && GlobalMembersGm_rw_analysis.merge_for_if_else(W, S1.write_set, S2.write_set, false);
			is_okay = is_okay && GlobalMembersGm_rw_analysis.merge_for_if_else(D, S1.reduce_set, S2.reduce_set, true);
			is_okay = is_okay && GlobalMembersGm_rw_analysis.merge_for_if_else(M, S1.mutate_set, S2.mutate_set, false);
		} else {
			gm_rwinfo_sets S1 = GlobalMembersGm_rw_analysis.get_rwinfo_sets(i.get_then());
			is_okay = is_okay && GlobalMembersGm_rw_analysis.merge_for_if(R, S1.read_set, false);
			is_okay = is_okay && GlobalMembersGm_rw_analysis.merge_for_if(W, S1.write_set, false);
			is_okay = is_okay && GlobalMembersGm_rw_analysis.merge_for_if(D, S1.reduce_set, true);
			is_okay = is_okay && GlobalMembersGm_rw_analysis.merge_for_if(M, S1.mutate_set, false);
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
		gm_rwinfo_sets sets = GlobalMembersGm_rw_analysis.get_rwinfo_sets(a);

		gm_rwinfo_map R = sets.read_set;
		gm_rwinfo_map W = sets.write_set;
		gm_rwinfo_map D = sets.reduce_set;

		// (1) LHS
		boolean is_reduce = (a.is_reduce_assign() || a.is_defer_assign());
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
				is_okay = is_okay && GlobalMembersGm_rw_analysis.gm_add_rwinfo_to_set(R, driver_sym, driver_entry, false);

				// no need to add drivers in lhs_list (if any) since all drivers
				// in the lhs are same
			}
		}

		if (is_reduce) {
			is_okay = is_okay && GlobalMembersGm_rw_analysis.gm_add_rwinfo_to_set(D, target_sym, new_entry, true);

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
					is_okay = is_okay && GlobalMembersGm_rw_analysis.gm_add_rwinfo_to_set(D, target_sym, new_entry, true);
				}
			}

		} else {
			is_okay = is_okay && GlobalMembersGm_rw_analysis.gm_add_rwinfo_to_set(W, target_sym, new_entry, false);
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
			// GlobalMembersGm_rw_analysis.Default_DriverMap.put(graph_sym, RR);
		}
		GlobalMembersGm_rw_analysis.traverse_expr_for_readset_adding(rhs, R);
		if (is_group_assign) // [deprecated]
		{
			assert false;
			throw new AssertionError();
			// dead code
			// GlobalMembersGm_rw_analysis.Default_DriverMap.remove(graph_sym);
		}

		return is_okay;
	}

	private boolean apply_sentblock(ast_sentblock s) {
		boolean is_okay = true;
		gm_rwinfo_sets sets = GlobalMembersGm_rw_analysis.get_rwinfo_sets(s);

		gm_rwinfo_map R = sets.read_set;
		gm_rwinfo_map W = sets.write_set;
		gm_rwinfo_map D = sets.reduce_set;
		gm_rwinfo_map M = sets.mutate_set;

		LinkedList<ast_sent> sents = s.get_sents();
		for (ast_sent sent : sents) {
			gm_rwinfo_sets sets2 = GlobalMembersGm_rw_analysis.get_rwinfo_sets(sent);

			gm_rwinfo_map R2 = sets2.read_set;
			gm_rwinfo_map W2 = sets2.write_set;
			gm_rwinfo_map D2 = sets2.reduce_set;
			gm_rwinfo_map M2 = sets2.mutate_set;

			is_okay = GlobalMembersGm_rw_analysis.merge_for_sentblock(s, R, R2, false) && is_okay;
			is_okay = GlobalMembersGm_rw_analysis.merge_for_sentblock(s, W, W2, false) && is_okay;
			is_okay = GlobalMembersGm_rw_analysis.merge_for_sentblock(s, D, D2, true) && is_okay;
			is_okay = GlobalMembersGm_rw_analysis.merge_for_sentblock(s, M, M2, false) && is_okay;
		}
		return is_okay;
	}

	private boolean apply_while(ast_while a) {
		boolean is_okay = true;
		gm_rwinfo_sets sets = GlobalMembersGm_rw_analysis.get_rwinfo_sets(a);

		gm_rwinfo_map R = sets.read_set;
		gm_rwinfo_map W = sets.write_set;
		gm_rwinfo_map D = sets.reduce_set;
		gm_rwinfo_map M = sets.mutate_set;

		ast_sent s = a.get_body();
		ast_expr e = a.get_cond();
		boolean is_do_while = a.is_do_while(); // do-while or while?

		// Add expr into read set
		assert e != null;
		GlobalMembersGm_rw_analysis.traverse_expr_for_readset_adding(e, R);

		// copy (conditionally) body
		is_okay = GlobalMembersGm_rw_analysis.merge_body(R, W, D, M, s, !is_do_while);

		return is_okay;
	}

	private boolean apply_foreach(ast_foreach a) {
		boolean is_okay = true;
		gm_rwinfo_sets sets = GlobalMembersGm_rw_analysis.get_rwinfo_sets(a);

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
			GlobalMembersGm_rw_analysis.traverse_expr_for_readset_adding(a.get_filter(), R_temp);

		// add source to the readset
		{
			ast_id source = a.get_source();
			gm_rwinfo new_entry = gm_rwinfo.new_scala_inst(source);
			gm_symtab_entry sym = source.getSymInfo();
			GlobalMembersGm_rw_analysis.gm_add_rwinfo_to_set(R_temp, sym, new_entry, false);
		}

		boolean is_conditional = (a.get_filter() != null) || (a.get_iter_type().is_collection_iter_type());
		is_okay = GlobalMembersGm_rw_analysis.merge_body(R_temp, W_temp, D_temp, M_temp, a.get_body(), is_conditional);

		// 3) Eliminate access driven by the current iterator
		// 4) And construct bound set
		// printf("foreach: %s, iter_type = %s\n",
		// a->get_iterator()->get_genname(),
		// gm_get_type_string(a->get_iter_type()));
		gm_rwinfo_map B = GlobalMembersGm_rw_analysis.gm_get_bound_set_info(a).bound_set;
		is_okay = GlobalMembersGm_rw_analysis.cleanup_iterator_access(a.get_iterator(), R_temp, R, a.get_iter_type(), a.is_parallel()) && is_okay;
		is_okay = GlobalMembersGm_rw_analysis.cleanup_iterator_access(a.get_iterator(), W_temp, W, a.get_iter_type(), a.is_parallel()) && is_okay;
		is_okay = GlobalMembersGm_rw_analysis.cleanup_iterator_access_reduce(a.get_iterator(), D_temp, D, W, B, a.get_iter_type(), a.is_parallel()) && is_okay;
		is_okay = GlobalMembersGm_rw_analysis.cleanup_iterator_access(a.get_iterator(), M_temp, M, a.get_iter_type(), a.is_parallel()) && is_okay;

		// printf("R:");gm_print_rwinfo_set(R);
		// printf("done\n");

		return is_okay;
	}

	private boolean apply_bfs(ast_bfs a) {
		boolean is_okay = true;
		gm_rwinfo_sets sets = GlobalMembersGm_rw_analysis.get_rwinfo_sets(a);

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
			GlobalMembersGm_rw_analysis.Default_DriverMap.put(it, R_alt);
			GlobalMembersGm_rw_analysis.traverse_expr_for_readset_adding(a.get_navigator(), R_temp);
			GlobalMembersGm_rw_analysis.Default_DriverMap.remove(it);
		}

		if (a.get_f_filter() != null) {
			range_cond_t R_alt = new range_cond_t(GlobalMembersGm_rw_analysis.gm_get_range_from_itertype(iter_type), true);
			GlobalMembersGm_rw_analysis.Default_DriverMap.put(it, R_alt);
			GlobalMembersGm_rw_analysis.traverse_expr_for_readset_adding(a.get_f_filter(), R_temp);
			GlobalMembersGm_rw_analysis.Default_DriverMap.remove(it);
		}
		if (a.get_b_filter() != null) {
			range_cond_t R_alt = new range_cond_t(GlobalMembersGm_rw_analysis.gm_get_range_from_itertype(iter_type), true);
			GlobalMembersGm_rw_analysis.Default_DriverMap.put(it, R_alt);
			GlobalMembersGm_rw_analysis.traverse_expr_for_readset_adding(a.get_b_filter(), R_temp);
			GlobalMembersGm_rw_analysis.Default_DriverMap.remove(it);
		}

		boolean is_conditional = true;
		// !((a->get_filter() == NULL) && (a->get_node_cond() == NULL) &&
		// (a->get_edge_cond() == NULL)) ;
		if (a.get_fbody() != null) {
			is_okay = GlobalMembersGm_rw_analysis.merge_body(R_temp, W_temp, D_temp1, M_temp, a.get_fbody(), is_conditional) && is_okay;
		}
		if (a.get_bbody() != null) {
			is_okay = GlobalMembersGm_rw_analysis.merge_body(R_temp, W_temp, D_temp2, M_temp, a.get_bbody(), is_conditional) && is_okay;
		}

		// [TODO: reduce operation bound to BFS]
		// [one bound for f-body, the other bound for b-body]
		gm_rwinfo_map B = GlobalMembersGm_rw_analysis.gm_get_bound_set_info(a).bound_set;
		gm_rwinfo_map B2 = GlobalMembersGm_rw_analysis.gm_get_bound_set_info(a).bound_set_back;

		is_okay = GlobalMembersGm_rw_analysis.cleanup_iterator_access(a.get_iterator(), R_temp, R, iter_type, a.is_parallel()) && is_okay;
		is_okay = GlobalMembersGm_rw_analysis.cleanup_iterator_access(a.get_iterator(), W_temp, W, iter_type, a.is_parallel()) && is_okay;
		is_okay = GlobalMembersGm_rw_analysis.cleanup_iterator_access_reduce(a.get_iterator(), D_temp1, D, W, B, iter_type, a.is_parallel()) && is_okay;
		is_okay = GlobalMembersGm_rw_analysis.cleanup_iterator_access_reduce(a.get_iterator(), D_temp2, D, W, B2, iter_type, a.is_parallel()) && is_okay;
		is_okay = GlobalMembersGm_rw_analysis.cleanup_iterator_access(a.get_iterator(), M_temp, M, iter_type, a.is_parallel()) && is_okay;

		GlobalMembersGm_rw_analysis.cleanup_iterator_access_bfs(R);
		GlobalMembersGm_rw_analysis.cleanup_iterator_access_bfs(W);

		return is_okay;
	}

	private boolean apply_return(ast_return r) {
		gm_rwinfo_sets sets = GlobalMembersGm_rw_analysis.get_rwinfo_sets(r);
		gm_rwinfo_map R = sets.read_set;

		ast_expr rhs = r.get_expr();
		if (rhs != null)
			GlobalMembersGm_rw_analysis.traverse_expr_for_readset_adding(rhs, R);
		return true;
	}

	private boolean apply_nop(ast_nop n) {
		n.do_rw_analysis();
		return true;
	}

	private boolean apply_call(ast_call c) {
		assert c.is_builtin_call();
		gm_rwinfo_sets sets = GlobalMembersGm_rw_analysis.get_rwinfo_sets(c);

		gm_rwinfo_map R = sets.read_set;
		gm_rwinfo_map M = sets.mutate_set;

		ast_expr e = c.get_builtin();
		GlobalMembersGm_rw_analysis.traverse_expr_for_readset_adding(e, R);
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
			is_okay = GlobalMembersGm_rw_analysis.gm_add_rwinfo_to_set(M, sym, new_entry, false);
		}

		return is_okay;
	}

	// -----------------------------------------------------------------------------
	// AST_ASSIGN
	// [EXPR]::[LHS list]
	// -----------------------------------------------------------------------------
	private boolean apply_foreign(ast_foreign f) {
		
		gm_rwinfo_sets sets = GlobalMembersGm_rw_analysis.get_rwinfo_sets(f);

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
			is_okay = GlobalMembersGm_rw_analysis.gm_add_rwinfo_to_set(W, target_sym, new_entry, false) && is_okay;
		}

		// -----------------------------------------
		// Expression
		// -----------------------------------------
		GlobalMembersGm_rw_analysis.traverse_expr_for_readset_adding(f.get_expr(), R);

		return is_okay;

	}

}