package backend_gps;

import inc.gps_apply_bb;
import ast.ast_sent;

//------------------------------------------------------------------
// Merge simple basic blocks
//    B -> C
//      C does not have any other predecessor
//      B does not have any other successor
//      B and C are SEQUENTIAL_TYPE
//------------------------------------------------------------------
public class gps_merge_simple_t extends gps_apply_bb
{
	@Override
	public void apply(gm_gps_basic_block B)
	{
		// B -> C
		if (B.get_num_exits() == 1)
		{
			gm_gps_basic_block C = B.get_nth_exit(0);
			if (C.get_num_entries() == 1)
			{

				if (B.get_type() != C.get_type())
					return;
				if (B.get_type() != gm_gps_bbtype.GM_GPS_BBTYPE_SEQ)
					return;

				// merge two BB
				set_changed(true);

				// merge sents
				C.prepare_iter();
				ast_sent s = C.get_next();
				while (s != null)
				{
					B.add_sent(s);
					s = C.get_next();
				}

				assert C.is_after_vertex() == false;

				// update exits
				B.remove_all_exits();

				if (C.get_num_exits() != 0)
				{
					assert C.get_num_exits() == 1;
					gm_gps_basic_block D = C.get_nth_exit(0);
					boolean auto_insert_remote_entry = false; // do not auto-add entries at D
					B.add_exit(D, auto_insert_remote_entry);
					D.update_entry_from(C, B);

				}

				// migrate extra info from B to C
				//printf("merging %d %d \n", B->get_id(), C->get_id());
				B.copy_info_from(C);

				// delete C
				if (C != null)
					C.dispose();
			}
		}
	}

}