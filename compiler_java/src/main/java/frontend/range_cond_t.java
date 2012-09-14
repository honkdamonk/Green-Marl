package frontend;

import static frontend.gm_range_type_t.GM_RANGE_INVALID;
//--------------------------------------------------------------------------
//--------------------------------------------------------------------------
// temporary iterator symbol
// (e.g. G.A = G.B)   ==> [G.B]
// (e.g. X = sum(t:G.Nodes)(t.x == 0){t.A} ==> [t.A]

public class range_cond_t {

	public gm_range_type_t range_type;
	public boolean is_always;

	public range_cond_t(gm_range_type_t range_type, boolean b) {
		this.range_type = range_type;
		is_always = b;
	}

	public range_cond_t() {
		range_type = GM_RANGE_INVALID;
		is_always = false;
	}

}