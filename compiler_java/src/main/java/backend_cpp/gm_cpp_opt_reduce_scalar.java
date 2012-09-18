package backend_cpp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import frontend.FrontendGlobal;
import frontend.gm_rw_analysis;
import frontend.gm_symtab_entry;
import inc.gm_assignment;
import inc.gm_compile_step;
import inc.gm_reduce;
import inc.gm_type;
import inc.nop_reduce_scalar;
import ast.ast_assign;
import ast.ast_expr;
import ast.ast_extra_info;
import ast.ast_foreach;
import ast.ast_id;
import ast.ast_node;
import ast.ast_node_type;
import ast.ast_procdef;
import ast.ast_sent;
import ast.ast_sentblock;
import ast.gm_rwinfo_map;

import common.gm_add_symbol;
import common.gm_apply;
import common.gm_main;
import common.gm_new_sents_after_tc;
import common.gm_transform_helper;
import common.gm_traverse;

class gm_cpp_opt_reduce_scalar extends gm_compile_step {
	
	private gm_cpp_opt_reduce_scalar() {
		set_description("Privitize reduction to scalar");
	}

	@Override
	public void process(ast_procdef p) {
		opt_scalar_reduction_t T = new opt_scalar_reduction_t();
		gm_rw_analysis.gm_redo_rw_analysis(p.get_body());
		gm_traverse.gm_traverse_sents(p.get_body(), T);
		if (T.has_targets()) {
			T.transform_targets();

			// need redo rw analysis
			gm_rw_analysis.gm_redo_rw_analysis(p.get_body());

			set_affected(true);
		}
	}

	@Override
	public gm_compile_step get_instance() {
		return new gm_cpp_opt_reduce_scalar();
	}

	public static gm_compile_step get_factory() {
		return new gm_cpp_opt_reduce_scalar();
	}
	
	//---------------------------------------------
	// Optimize Scalar Reductions
	//---------------------------------------------
	// Int S;
	// Foreach(t: xxx) {
	//   S += ....
	// }
	// ===>
	// Int S;
	// { // omp_parallel
	//	    Int S_prv; 
	//	    Foreach(t: xxx) { // omp for
	//    
	// 
	//---------------------------------------------

	class opt_scalar_reduction_t extends gm_apply {
		
		private final LinkedList<ast_sent> _targets = new LinkedList<ast_sent>();
		
		// choose targets
		@Override
		public boolean apply(ast_sent sent) {

			// check only for foreach
			// todo: do similar thing for BFS
			if (sent.get_nodetype() != ast_node_type.AST_FOREACH)
				return true;

			ast_foreach fe = (ast_foreach) sent;
			gm_rwinfo_map B = gm_rw_analysis.gm_get_bound_set_info(fe).bound_set;
			if (B.size() == 0)
				return true;

			assert fe.is_parallel();

			boolean has_scalar_reduction = false;
			for (gm_symtab_entry e : B.keySet()) {
				if (e.getType().is_property())
					continue;

				has_scalar_reduction = true;
				break;
			}

			if (has_scalar_reduction)
				_targets.addLast(fe);
			return true;
		}

		// iterate over targets
		public final void transform_targets() {
			Iterator<ast_sent> I;
			for (I = _targets.iterator(); I.hasNext();) {
				ast_sent s = I.next();
				assert s.get_nodetype() == ast_node_type.AST_FOREACH;
				ast_foreach fe = (ast_foreach) s;
				apply_transform(fe);
			}
		}

		public final boolean has_targets() {
			return _targets.size() > 0;
		}

		// ---------------------------------------------
		// apply to each BFS
		// ---------------------------------------------
		private void apply_transform(ast_foreach fe) {
			HashMap<gm_symtab_entry, gm_symtab_entry> symbol_map = new HashMap<gm_symtab_entry, gm_symtab_entry>();
			LinkedList<gm_symtab_entry> old_s = new LinkedList<gm_symtab_entry>();
			LinkedList<gm_symtab_entry> new_s = new LinkedList<gm_symtab_entry>();
			LinkedList<gm_reduce> reduce_op = new LinkedList<gm_reduce>();
			HashMap<gm_symtab_entry, LinkedList<gm_symtab_entry>> old_supple_map = new HashMap<gm_symtab_entry, LinkedList<gm_symtab_entry>>();
			HashMap<gm_symtab_entry, LinkedList<gm_symtab_entry>> new_supple_map = new HashMap<gm_symtab_entry, LinkedList<gm_symtab_entry>>();
			LinkedList<LinkedList<gm_symtab_entry>> old_supple = new LinkedList<LinkedList<gm_symtab_entry>>();
			LinkedList<LinkedList<gm_symtab_entry>> new_supple = new LinkedList<LinkedList<gm_symtab_entry>>();

			// make scope
			gm_transform_helper.gm_make_it_belong_to_sentblock_nested(fe);
			assert fe.get_parent().get_nodetype() == ast_node_type.AST_SENTBLOCK;
			ast_sentblock se = (ast_sentblock) fe.get_parent();

			// set scope parallel
			se.add_info(gm_cpp_gen.LABEL_PAR_SCOPE, new ast_extra_info(true));

			// foreach scalar boundsymbol
			gm_rwinfo_map B = gm_rw_analysis.gm_get_bound_set_info(fe).bound_set;
			for (gm_symtab_entry e : B.keySet()) {
				if (e.getType().is_property())
					continue;

				gm_reduce reduce_type = B.get(e).getFirst().reduce_op;
				gm_type e_type = e.getType().getTypeSummary();
				boolean is_supple = B.get(e).getFirst().is_supplement;
				gm_symtab_entry org_target = B.get(e).getFirst().org_lhs;

				if (!e_type.is_prim_type()) {
					assert e.getType().is_node_compatible() || e.getType().is_edge_compatible();
					assert (reduce_type == gm_reduce.GMREDUCE_MAX) || (reduce_type == gm_reduce.GMREDUCE_MIN);
				}
				String new_name = gm_main.FE.voca_temp_name_and_add(e.getId().get_genname(), "_prv");

				// add local variable at scope
				gm_symtab_entry _thread_local;
				if (e_type.is_prim_type()) {
					_thread_local = gm_add_symbol.gm_add_new_symbol_primtype(se, e_type, new_name);
				} else if (e_type.is_node_compatible_type()) {
					_thread_local = gm_add_symbol.gm_add_new_symbol_nodeedge_type(se, gm_type.GMTYPE_NODE, e.getType().get_target_graph_sym(),
							new_name);
				} else if (e_type.is_edge_compatible_type()) {
					_thread_local = gm_add_symbol.gm_add_new_symbol_nodeedge_type(se, gm_type.GMTYPE_EDGE, e.getType().get_target_graph_sym(),
							new_name);
				} else {
					_thread_local = null;
					assert false;
				}

				assert !symbol_map.containsKey(e);

				// save to symbol_map (for change_reduction_t)
				symbol_map.put(e, _thread_local);

				if (is_supple) {
					LinkedList<gm_symtab_entry> L1 = old_supple_map.get(org_target);
					LinkedList<gm_symtab_entry> L2 = new_supple_map.get(org_target);
					L1.addLast(e);
					L2.addLast(_thread_local);
					// printf("%s is supplement LHS (%s)\n",
					// e->getId()->get_genname(),
					// org_target->getId()->get_genname());
				} else {
					// save to lists (for code-generation nop)
					assert reduce_type.is_strict_reduce_op();
					old_s.addLast(e);
					new_s.addLast(_thread_local);
					reduce_op.addLast(reduce_type);
				}

				// add intializer
				if (!is_supple) {
					gm_type expr_type = e.getType().getTypeSummary();
					ast_expr init_val;
					if ((reduce_type == gm_reduce.GMREDUCE_MIN) || (reduce_type == gm_reduce.GMREDUCE_MAX)) {
						init_val = ast_expr.new_id_expr(e.getId().copy(true));
					} else {
						init_val = gm_new_sents_after_tc.gm_new_bottom_symbol(reduce_type, expr_type);
					}
					ast_assign init_a = ast_assign.new_assign_scala(_thread_local.getId().copy(true), init_val, gm_assignment.GMASSIGN_NORMAL);

					gm_transform_helper.gm_add_sent_before(fe, init_a);
				}

				new_name = null;
			}

			for (gm_symtab_entry e : old_s) {
				if (!old_supple_map.containsKey(e)) {
					LinkedList<gm_symtab_entry> L = new LinkedList<gm_symtab_entry>(); // empty
																						// list
					old_supple.addLast(L);
					new_supple.addLast(L);
				} else {
					// printf("num supple for %s : %d\n", e->getId()->get_genname(),
					// (int)old_supple_map[e].size());
					old_supple.addLast(old_supple_map.get(e));
					new_supple.addLast(new_supple_map.get(e));
				}
			}

			// create supplement list

			// -------------------------------------------------
			// find all reductions in the body.
			// - replace to normal assignment(s) to local lhs
			// -------------------------------------------------
			change_reduction_t T = new change_reduction_t();
			T.set_map(symbol_map);
			gm_traverse.gm_traverse_sents(fe.get_body(), T);
			T.post_process();

			// -------------------------------------------------
			// add reduction nop
			// -------------------------------------------------
			nop_reduce_scalar N = new nop_reduce_scalar();
			N.set_symbols(old_s, new_s, reduce_op, old_supple, new_supple);
			gm_transform_helper.gm_insert_sent_end_of_sb(se, N, false);

		}
	}

	class change_reduction_t extends gm_apply {
		
		private HashMap<gm_symtab_entry, gm_symtab_entry> symbol_map;
		private LinkedList<ast_assign> to_normals = new LinkedList<ast_assign>();
		
		public final void set_map(HashMap<gm_symtab_entry, gm_symtab_entry> m) {
			symbol_map = m;
		}
	
		@Override
		public boolean apply(ast_sent s) {
			if (s.get_nodetype() != ast_node_type.AST_ASSIGN)
				return true;
			ast_assign a = (ast_assign) s;
			if (!a.is_reduce_assign())
				return true;
			if (!a.is_target_scalar())
				return true;
	
			ast_id lhs = a.get_lhs_scala();
	
			if (!symbol_map.containsKey(lhs.getSymInfo())) // not target
				return true;
	
			gm_symtab_entry new_target = symbol_map.get(lhs.getSymInfo());
	
			// change lhs symbol
			lhs.setSymInfo(new_target);
			if (a.is_argminmax_assign()) {
				LinkedList<ast_node> L_old = a.get_lhs_list();
				for (ast_node n : L_old) {
					assert n.get_nodetype() == ast_node_type.AST_ID;
					ast_id id = (ast_id) n;
					gm_symtab_entry old_e = id.getSymInfo();
					gm_symtab_entry new_e = symbol_map.get(old_e);
					assert new_e != null;
					id.setSymInfo(new_e);
				}
			}
	
			// change to normal write
			to_normals.addLast(a);
	
			return true;
		}
	
		public final void post_process() {
			for (ast_assign a : to_normals) {
				gm_transform_helper.gm_make_it_belong_to_sentblock(a);
				FrontendGlobal.gm_make_normal_assign(a);
			}
		}
	
	}
}