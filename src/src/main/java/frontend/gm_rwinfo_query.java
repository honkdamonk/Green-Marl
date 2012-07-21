package frontend;

import inc.GM_REDUCE_T;

// returns true if the symbol is modified in ths sentence subtree S.
public class gm_rwinfo_query
{
	public gm_rwinfo_query()
	{
		this._check_range = false;
		this._check_driver = false;
		this._check_always = false;
		this._check_reduceop = false;
		this._check_bound = false;
		this.range = gm_range_type_t.GM_RANGE_INVALID;
		this.reduce_op = GM_REDUCE_T.GMREDUCE_NULL;
		this.driver = null;
		this.bound = null;
		this.always = true;
	}
	public final void check_range(gm_range_type_t r)
	{
		_check_range = true;
		range = r;
	}
	public final void check_driver(gm_symtab_entry d)
	{
		_check_driver = true;
		driver = d;
	}
	public final void check_always(boolean a)
	{
		_check_always = true;
		always = a;
	}
	public final void check_reduce_op(GM_REDUCE_T o)
	{
		_check_reduceop = true;
		reduce_op = o;
	}
	public final void check_bound(gm_symtab_entry b)
	{
		_check_bound = true;
		bound = b;
	}
	//bool is_any_set() {return _check_range || _check_driver || _check_always || _check_reduceop || _check_bound;}

	public boolean _check_range;
	public boolean _check_driver;
	public boolean _check_always;
	public boolean _check_reduceop;
	public boolean _check_bound;
	public gm_range_type_t range;
	public GM_REDUCE_T reduce_op;
	public gm_symtab_entry driver;
	public gm_symtab_entry bound;
	public boolean always;
}