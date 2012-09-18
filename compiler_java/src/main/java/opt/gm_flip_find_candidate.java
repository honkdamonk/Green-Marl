package opt;

import inc.gm_type;

import java.util.LinkedList;

import tangible.RefObject;
import ast.ast_assign;
import ast.ast_field;
import ast.ast_foreach;
import ast.ast_if;
import ast.ast_node_type;
import ast.ast_sent;

import common.gm_apply;

// find candiates
public class gm_flip_find_candidate extends gm_apply {

	private boolean avoid_reverse = false;
	private boolean avoid_pull = false;

	private LinkedList<ast_foreach> target = new LinkedList<ast_foreach>();

	public gm_flip_find_candidate() {
		set_for_sent(true);
	}

	@Override
	public boolean apply(ast_sent s) {
		if (s.get_nodetype() == ast_node_type.AST_FOREACH) {
			ast_foreach fe = (ast_foreach) s;
			RefObject<ast_foreach> in_ref = new RefObject<ast_foreach>(null);
			RefObject<ast_sent> dest_ref = new RefObject<ast_sent>(null);

			boolean test = gm_flip_edges.capture_pattern(fe, new RefObject<ast_if>(null), in_ref, new RefObject<ast_if>(null), dest_ref);
			ast_sent dest = dest_ref.argvalue;
			ast_foreach in = in_ref.argvalue;

			if (!test)
				return true;

			if (avoid_reverse) {
				if (in.get_iter_type() == gm_type.GMTYPE_NODEITER_IN_NBRS) {
					target.addLast(fe);
					return true; // do ont push it twice
				}
			}

			if (avoid_pull) {
				if (dest.get_nodetype() == ast_node_type.AST_ASSIGN) {
					ast_assign d = (ast_assign) dest;
					if (!d.is_target_scalar()) {
						ast_field f = d.get_lhs_field();

						// driver is inner loop
						if (f.get_first().getSymInfo() == fe.get_iterator().getSymInfo()) {
							target.addLast(fe);
							return true;
						}
					}
				}
			}
		}

		return true;
	}

	public final void set_to_avoid_reverse_edges(boolean b) {
		avoid_reverse = b;
	}

	public final void set_to_avoid_pull_computation(boolean b) {
		avoid_pull = b;
	}

	public final LinkedList<ast_foreach> get_target() {
		return target;
	}

}