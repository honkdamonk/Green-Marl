package frontend;

import static frontend.gm_range_type.GM_RANGE_INVALID;
import static inc.gm_reduce.GMREDUCE_NULL;
import inc.gm_reduce;

// returns true if the symbol is modified in ths sentence subtree S.
public class gm_rwinfo_query {
	
	public boolean _check_range = false;
	public boolean _check_driver = false;
	public boolean _check_always = false;
	public boolean _check_reduceop = false;
	public boolean _check_bound = false;
	public gm_range_type range = GM_RANGE_INVALID;
	public gm_reduce reduce_op = GMREDUCE_NULL;
	public gm_symtab_entry driver = null;
	public gm_symtab_entry bound = null;
	public boolean always = true;
	
	public final void check_range(gm_range_type r) {
		_check_range = true;
		range = r;
	}

	public final void check_driver(gm_symtab_entry d) {
		_check_driver = true;
		driver = d;
	}

	public final void check_always(boolean a) {
		_check_always = true;
		always = a;
	}

	public final void check_reduce_op(gm_reduce o) {
		_check_reduceop = true;
		reduce_op = o;
	}

	public final void check_bound(gm_symtab_entry b) {
		_check_bound = true;
		bound = b;
	}

}