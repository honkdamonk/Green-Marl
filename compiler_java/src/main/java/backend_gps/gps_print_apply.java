package backend_gps;

import inc.gps_apply_bb;

public class gps_print_apply extends gps_apply_bb {
	
	@Override
	public void apply(gm_gps_basic_block b) {
		b.print();
	}
}
// -----------------------------------------------------
// traverse AST in each BB
// -----------------------------------------------------

