package opt;

import inc.gm_compile_step;
import inc.gm_ind_opt_flip_edge_bfs;
import inc.gm_ind_opt_flip_edges;
import inc.gm_ind_opt_hoist_assign;
import inc.gm_ind_opt_hoist_foreach;
import inc.gm_ind_opt_loop_merge;
import inc.gm_ind_opt_move_propdecl;
import inc.gm_ind_opt_nonconf_reduce;
import inc.gm_ind_opt_syntax_sugar2;

import java.util.LinkedList;

import common.GlobalMembersGm_apply_compiler_stage;

public class gm_independent_optimize {
	public gm_independent_optimize() {
		init_steps();
	}

	public void dispose() {
	}

	// return true if successful
	public boolean do_local_optimize() {
		return GlobalMembersGm_apply_compiler_stage.gm_apply_compiler_stage(opt_steps);
	}

	// --------------------------------------------
	// backend-independnt transformation
	// --------------------------------------------

	public final void init_steps() {
		LinkedList<gm_compile_step> LIST = opt_steps;

		LIST.addLast(gm_ind_opt_flip_edge_bfs.get_factory());
		LIST.addLast(gm_ind_opt_syntax_sugar2.get_factory());
		LIST.addLast(gm_ind_opt_move_propdecl.get_factory());
		LIST.addLast(gm_ind_opt_hoist_assign.get_factory());
		LIST.addLast(gm_ind_opt_hoist_foreach.get_factory());
		LIST.addLast(gm_ind_opt_flip_edges.get_factory());
		LIST.addLast(gm_ind_opt_loop_merge.get_factory());
		LIST.addLast(gm_ind_opt_nonconf_reduce.get_factory());
	}

	// ----------------------------------------------------
	// Any later stage can call below optimizations
	// returns false if error
	// ----------------------------------------------------
	// group assign => foreach
	// reduction op => foreach
	/*
	 * virtual bool do_regularize_syntax(ast_procdef *p); virtual bool
	 * do_hoist_assign(ast_procdef* proc); virtual bool
	 * do_hoist_foreach(ast_procdef* proc); virtual bool
	 * do_merge_foreach(ast_procdef* proc); virtual bool
	 * do_moveup_propdecl(ast_procdef* p); virtual bool
	 * do_flip_edges(ast_procdef* p);
	 */

	protected LinkedList<gm_compile_step> opt_steps = new LinkedList<gm_compile_step>();
}
/*
 */
