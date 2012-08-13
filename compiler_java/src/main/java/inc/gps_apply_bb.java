package inc;

import ast.ast_id;
import backend_gps.gm_gps_basic_block;
import backend_gps.gps_syminfo;

import common.gm_apply;

public abstract class gps_apply_bb extends gm_apply {
	
	public final static String GPS_TAG_BB_USAGE = "GPS_TAG_BB";
	
	protected boolean changed;
	
	public abstract void apply(gm_gps_basic_block b);

	public boolean has_changed() {
		return changed;
	}

	public void set_changed(boolean b) {
		changed = b;
	}
	
	public static gps_syminfo gps_get_global_syminfo(ast_id i) {
		return (gps_syminfo) i.getSymInfo().find_info(GPS_TAG_BB_USAGE);
	}

}