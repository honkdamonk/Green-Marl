package backend_gps;

import inc.gps_apply_bb;
import inc.gps_apply_bb_ast;

import java.util.HashSet;

public class gm_gps_misc {

	// return or of has_changed
	public static void gps_bb_apply_only_once(gm_gps_basic_block entry, gps_apply_bb apply) {
		HashSet<gm_gps_basic_block> set = new HashSet<gm_gps_basic_block>();
		set.clear();
		gm_gps_misc.bb_apply_recurse(set, entry, apply);
	}

	public static void gps_bb_traverse_ast(gm_gps_basic_block entry, gps_apply_bb_ast apply, boolean is_post, boolean is_pre) {
		apply.set_is_post(is_post);
		apply.set_is_pre(is_pre);

		// apply it once
		gm_gps_misc.gps_bb_apply_only_once(entry, apply);

	}
	
	// depth-first recurse
	public static void bb_apply_recurse(HashSet<gm_gps_basic_block> set, gm_gps_basic_block B, gps_apply_bb apply) {
		apply.apply(B);
		set.add(B);
		for (int i = 0; i < B.get_num_exits(); i++) {
			gm_gps_basic_block b = B.get_nth_exit(i);

			if (!set.contains(b)) {
				bb_apply_recurse(set, b, apply);
			}
		}
	}


}