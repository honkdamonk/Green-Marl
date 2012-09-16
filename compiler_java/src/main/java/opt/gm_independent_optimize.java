package opt;

import inc.gm_compile_step;
import inc.gm_ind_opt_flip_edge_bfs;
import inc.gm_ind_opt_flip_edges;
import inc.gm_ind_opt_hoist_assign;
import inc.gm_ind_opt_hoist_foreach;
import inc.gm_ind_opt_loop_merge;
import inc.gm_ind_opt_move_propdecl;
import inc.gm_ind_opt_nonconf_reduce;
import inc.gm_ind_opt_propagate_trivial_writes;
import inc.gm_ind_opt_remove_unused_scalar;
import inc.gm_ind_opt_syntax_sugar2;

import java.util.LinkedList;

import common.gm_apply_compiler_stage;

public class gm_independent_optimize {

	protected final LinkedList<gm_compile_step> opt_steps = new LinkedList<gm_compile_step>();

	public gm_independent_optimize() {
		init_steps();
	}

	public void dispose() {
	}

	// return true if successful
	public boolean do_local_optimize() {
		return gm_apply_compiler_stage.apply(opt_steps);
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
		LIST.addLast(gm_ind_opt_propagate_trivial_writes.get_factory());
		LIST.addLast(gm_ind_opt_remove_unused_scalar.get_factory());
	}

}
