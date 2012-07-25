package backend_gps;


public class GlobalMembersGm_gps_bb_split
{

//    [prev -> BB -> next] ==>
//    [prev -> BB(S) -> new_seq -> BB(R) -> next]

	public static gm_gps_basic_block split_vertex_BB(gm_gps_basic_block BB, gm_gps_beinfo gen)
	{
		//printf("splitting BB id = %d\n", BB->get_id());

		assert BB.is_vertex();
		//assert(BB->has_sender());
		assert BB.has_receiver();
		assert BB.get_num_entries() == 1;
		assert BB.get_num_exits() == 1;

		gm_gps_basic_block prev = BB.get_nth_entry(0);
		gm_gps_basic_block next = BB.get_nth_exit(0);

		assert!prev.is_vertex();
		assert!next.is_vertex();
		assert next.get_num_entries() == 1;

		gm_gps_basic_block new_seq = new gm_gps_basic_block(gen.issue_basicblock_id());
		new_seq.set_after_vertex(true);

		gm_gps_basic_block new_BB = new gm_gps_basic_block(gen.issue_basicblock_id(), gm_gps_bbtype_t.GM_GPS_BBTYPE_BEGIN_VERTEX);

		//--------------------------------------
		// migrate receiver list to new_BB
		//--------------------------------------
		java.util.LinkedList<gm_gps_comm_unit> L = BB.get_receivers();

		for (gm_gps_comm_unit unit : L)
		{
			new_BB.add_receiver(unit);
		}
		BB.clear_receivers();

		// insert basic blocks
		BB.remove_all_exits();
		next.remove_all_entries();

		BB.add_exit(new_seq);
		new_seq.add_exit(new_BB);
		new_BB.add_exit(next);

		java.util.LinkedList<gm_gps_basic_block> BBLIST = gen.get_basic_blocks();
		BBLIST.addLast(new_seq);
		BBLIST.addLast(new_BB);

		return new_BB;

	}
}