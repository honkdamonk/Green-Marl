package inc;

import backend_gps.gm_gps_basic_block;


public abstract class gps_apply_bb
{
	public void dispose()
	{
	}
	public abstract void apply(gm_gps_basic_block b);
	public boolean has_changed()
	{
		return changed;
	}
	public void set_changed(boolean b)
	{
		changed = b;
	}
	protected boolean changed;
}