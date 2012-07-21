package backend_gps;


public class gm_gps_congruent_msg_class
{
	public int id;
	public gm_gps_communication_size_info sz_info;
	public java.util.HashSet<gm_gps_basic_block> recv_bb = new java.util.HashSet<gm_gps_basic_block>();

	public final void add_receiving_basic_block(gm_gps_basic_block b)
	{
		recv_bb.add(b);
	}
	public final boolean find_basic_block_in_receiving_list(gm_gps_basic_block b)
	{
		return (recv_bb.find(b).hasNext());
	}
}