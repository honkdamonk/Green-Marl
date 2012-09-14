package backend_gps;

import inc.gps_apply_bb;

import java.util.LinkedList;

//------------------------------------------------
// Traverse BB DAG
// Push visited BB nodes into the list
//------------------------------------------------
public class gps_get_reachable_bb_list_t extends gps_apply_bb
{
	public gps_get_reachable_bb_list_t(LinkedList<gm_gps_basic_block> bb_blocks)
	{
		this.blist = new LinkedList<gm_gps_basic_block>(bb_blocks);
		blist.clear();
	}

	@Override
	public void apply(gm_gps_basic_block b)
	{
		blist.addLast(b);
	}
	public LinkedList<gm_gps_basic_block> blist;
}