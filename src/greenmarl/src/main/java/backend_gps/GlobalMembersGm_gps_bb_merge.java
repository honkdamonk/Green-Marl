package backend_gps;


public class GlobalMembersGm_gps_bb_merge
{

	public static void gm_gps_merge_basic_blocks(gm_gps_basic_block entry)
	{
		gps_merge_simple_t T = new gps_merge_simple_t();
		GlobalMembersGm_gps_misc.gps_bb_apply_until_no_change(entry, T);
	}
}