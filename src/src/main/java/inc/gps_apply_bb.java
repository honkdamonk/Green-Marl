package inc;

import backend_gps.gm_gps_basic_block;

import common.gm_apply;

public abstract class gps_apply_bb extends gm_apply {
	
	protected boolean changed;
	
	public abstract void apply(gm_gps_basic_block b);

	public boolean has_changed() {
		return changed;
	}

	public void set_changed(boolean b) {
		changed = b;
	}

}