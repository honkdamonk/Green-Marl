package inc;


public class GlobalMembersGm_gps_basicblock
{

	//boolean gps_bb_apply_until_no_change(gm_gps_basic_block entry, gps_apply_bb apply);
	//void gps_bb_apply_only_once(gm_gps_basic_block entry, gps_apply_bb apply); // in DFS order

	//void gps_bb_print_all(gm_gps_basic_block entry);

	// traverse BB (only once with DFS order) and apply to each AST
	//void gps_bb_traverse_ast(gm_gps_basic_block entry, gps_apply_bb_ast apply, boolean is_post, boolean is_pre);

	// traverse single BB only
	//void gps_bb_traverse_ast_single(gm_gps_basic_block entry, gps_apply_bb_ast apply, boolean is_post, boolean is_pre);

	//gm_rwinfo_sets gm_gps_get_rwinfo_from_bb(gm_gps_basic_block BB, gm_rwinfo_sets S, boolean check_receiver);
	//gm_rwinfo_sets gm_gps_get_rwinfo_from_all_reachable_bb(gm_gps_basic_block BB, gm_rwinfo_sets S, boolean check_receiver);
}