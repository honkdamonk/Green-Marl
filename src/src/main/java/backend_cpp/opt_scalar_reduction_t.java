package backend_cpp;

import inc.GMTYPE_T;
import inc.GM_REDUCE_T;
import inc.GlobalMembersGm_backend_cpp;
import inc.gm_assignment_t;
import inc.nop_reduce_scalar;
import tangible.RefObject;
import ast.AST_NODE_TYPE;
import ast.ast_assign;
import ast.ast_expr;
import ast.ast_extra_info;
import ast.ast_foreach;
import ast.ast_sent;
import ast.ast_sentblock;

import common.GlobalMembersGm_add_symbol;
import common.GlobalMembersGm_main;
import common.GlobalMembersGm_new_sents_after_tc;
import common.GlobalMembersGm_transform_helper;
import common.GlobalMembersGm_traverse;
import common.gm_apply;

import frontend.GlobalMembersGm_rw_analysis;
import frontend.gm_rwinfo;
import frontend.gm_symtab_entry;

//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define TO_STR(X) #X
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define DEF_STRING(X) static const char *X = "X"
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP(CLASS, DESC) class CLASS : public gm_compile_step { private: CLASS() {set_description(DESC);}public: virtual void process(ast_procdef*p); virtual gm_compile_step* get_instance(){return new CLASS();} static gm_compile_step* get_factory(){return new CLASS();} };
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP_FACTORY(CLASS) CLASS::get_factory()

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
//    Int S_prv; 
//    Foreach(t: xxx) { // omp for
//    
// 
//---------------------------------------------

public class opt_scalar_reduction_t extends gm_apply {
	// choose targets
	@Override
	public boolean apply(ast_sent sent) {

		// check only for foreach
		// todo: do similar thing for BFS
		if (sent.get_nodetype() != AST_NODE_TYPE.AST_FOREACH)
			return true;

		ast_foreach fe = (ast_foreach) sent;
		java.util.HashMap<gm_symtab_entry, java.util.LinkedList<gm_rwinfo>> B = GlobalMembersGm_rw_analysis.gm_get_bound_set_info(fe).bound_set;
		if (B.size() == 0)
			return true;

		assert fe.is_parallel();

		boolean has_scalar_reduction = false;
		java.util.Iterator<gm_symtab_entry, java.util.LinkedList<gm_rwinfo>> I;
		for (I = B.iterator(); I.hasNext();) {
			gm_symtab_entry e = I.next().getKey();
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
		java.util.Iterator<ast_sent> I;
		for (I = _targets.iterator(); I.hasNext();) {
			ast_sent s = I.next();
			assert s.get_nodetype() == AST_NODE_TYPE.AST_FOREACH;
			ast_foreach fe = (ast_foreach) s;
			apply_transform(fe);
		}
	}

	public final boolean has_targets() {
		return _targets.size() > 0;
	}

	private java.util.LinkedList<ast_sent> _targets = new java.util.LinkedList<ast_sent>();

	// ---------------------------------------------
	// apply to each BFS
	// ---------------------------------------------
	private void apply_transform(ast_foreach fe) {
		java.util.HashMap<gm_symtab_entry, gm_symtab_entry> symbol_map = new java.util.HashMap<gm_symtab_entry, gm_symtab_entry>();
		java.util.LinkedList<gm_symtab_entry> old_s = new java.util.LinkedList<gm_symtab_entry>();
		java.util.LinkedList<gm_symtab_entry> new_s = new java.util.LinkedList<gm_symtab_entry>();
		java.util.LinkedList<GM_REDUCE_T> reduce_op = new java.util.LinkedList<GM_REDUCE_T>();
		java.util.HashMap<gm_symtab_entry, java.util.LinkedList<gm_symtab_entry>> old_supple_map = new java.util.HashMap<gm_symtab_entry, java.util.LinkedList<gm_symtab_entry>>();
		java.util.HashMap<gm_symtab_entry, java.util.LinkedList<gm_symtab_entry>> new_supple_map = new java.util.HashMap<gm_symtab_entry, java.util.LinkedList<gm_symtab_entry>>();
		java.util.LinkedList<java.util.LinkedList<gm_symtab_entry>> old_supple = new java.util.LinkedList<java.util.LinkedList<gm_symtab_entry>>();
		java.util.LinkedList<java.util.LinkedList<gm_symtab_entry>> new_supple = new java.util.LinkedList<java.util.LinkedList<gm_symtab_entry>>();

		// make scope
		GlobalMembersGm_transform_helper.gm_make_it_belong_to_sentblock_nested(fe);
		assert fe.get_parent().get_nodetype() == AST_NODE_TYPE.AST_SENTBLOCK;
		ast_sentblock se = (ast_sentblock) fe.get_parent();

		// set scope parallel
		se.add_info(GlobalMembersGm_backend_cpp.LABEL_PAR_SCOPE, new ast_extra_info(true));

		// foreach scalar boundsymbol
		java.util.HashMap<gm_symtab_entry, java.util.LinkedList<gm_rwinfo>> B = GlobalMembersGm_rw_analysis.gm_get_bound_set_info(fe).bound_set;
		for (gm_symtab_entry e : B.keySet()) {
			if (e.getType().is_property())
				continue;

			GM_REDUCE_T reduce_type = B.get(e).getFirst().reduce_op;
			GMTYPE_T e_type = e.getType().getTypeSummary();
			boolean is_supple = B.get(e).getFirst().is_supplement;
			gm_symtab_entry org_target = B.get(e).getFirst().org_lhs;

			if (!e_type.is_prim_type()) {
				assert e.getType().is_node_compatible() || e.getType().is_edge_compatible();
				assert (reduce_type == GM_REDUCE_T.GMREDUCE_MAX) || (reduce_type == GM_REDUCE_T.GMREDUCE_MIN);
			}
			String new_name = GlobalMembersGm_main.FE.voca_temp_name_and_add(e.getId().get_genname(), "_prv");

			// add local variable at scope
			gm_symtab_entry _thread_local;
			if (e_type.is_prim_type()) {
				_thread_local = GlobalMembersGm_add_symbol.gm_add_new_symbol_primtype(se, e_type, new RefObject<String>(new_name));
			} else if (e_type.is_node_compatible_type()) {
				_thread_local = GlobalMembersGm_add_symbol.gm_add_new_symbol_nodeedge_type(se, GMTYPE_T.GMTYPE_NODE, e.getType().get_target_graph_sym(),
						new RefObject<String>(new_name));
			} else if (e_type.is_edge_compatible_type()) {
				_thread_local = GlobalMembersGm_add_symbol.gm_add_new_symbol_nodeedge_type(se, GMTYPE_T.GMTYPE_EDGE, e.getType().get_target_graph_sym(),
						new RefObject<String>(new_name));
			} else {
				assert false;
			}

			assert !symbol_map.containsKey(e);

			// save to symbol_map (for change_reduction_t)
			symbol_map.put(e, _thread_local);

			if (is_supple) {
				java.util.LinkedList<gm_symtab_entry> L1 = old_supple_map.get(org_target);
				java.util.LinkedList<gm_symtab_entry> L2 = new_supple_map.get(org_target);
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
				GMTYPE_T expr_type = e.getType().getTypeSummary();
				ast_expr init_val;
				if ((reduce_type == GM_REDUCE_T.GMREDUCE_MIN) || (reduce_type == GM_REDUCE_T.GMREDUCE_MAX)) {
					init_val = ast_expr.new_id_expr(e.getId().copy(true));
				} else {
					init_val = GlobalMembersGm_new_sents_after_tc.gm_new_bottom_symbol(reduce_type, expr_type);
				}
				ast_assign init_a = ast_assign.new_assign_scala(_thread_local.getId().copy(true), init_val, gm_assignment_t.GMASSIGN_NORMAL);

				GlobalMembersGm_transform_helper.gm_add_sent_before(fe, init_a);
			}

			new_name = null;
		}

		for (gm_symtab_entry e : old_s) {
			if (!old_supple_map.containsKey(e)) {
				java.util.LinkedList<gm_symtab_entry> L = new java.util.LinkedList<gm_symtab_entry>(); // empty
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
		GlobalMembersGm_traverse.gm_traverse_sents(fe.get_body(), T);
		T.post_process();

		// -------------------------------------------------
		// add reduction nop
		// -------------------------------------------------
		nop_reduce_scalar N = new nop_reduce_scalar();
		N.set_symbols(old_s, new_s, reduce_op, old_supple, new_supple);
		GlobalMembersGm_transform_helper.gm_insert_sent_end_of_sb(se, N, false);

	}

}