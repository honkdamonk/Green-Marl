package backend_gps;

import frontend.gm_rwinfo_sets;

public class GlobalMembersGm_gps_bb_rw_analysis {

	public static gm_rwinfo_sets gm_gps_get_rwinfo_from_bb(gm_gps_basic_block BB, gm_rwinfo_sets S) {
		return gm_gps_get_rwinfo_from_bb(BB, S, false);
	}

	public static gm_rwinfo_sets gm_gps_get_rwinfo_from_bb(gm_gps_basic_block BB, gm_rwinfo_sets S, boolean check_receivers) {
		if (S == null)
			S = new gm_rwinfo_sets();
		assert check_receivers == false;

		// -------------------------------------------
		// traverse AST inside BB
		// merge read/write sets
		// caution for communicating symbols
		// -------------------------------------------
		gm_gps_find_rwinfo_simple T = new gm_gps_find_rwinfo_simple(S);
		T.set_check_receiver(check_receivers);
		// post && pre
		GlobalMembersGm_gps_misc.gps_bb_traverse_ast_single(BB, T, true, true);

		return S;
	}

	public static gm_rwinfo_sets gm_gps_get_rwinfo_from_all_reachable_bb(gm_gps_basic_block BB, gm_rwinfo_sets S) {
		return gm_gps_get_rwinfo_from_all_reachable_bb(BB, S, false);
	}

	public static gm_rwinfo_sets gm_gps_get_rwinfo_from_all_reachable_bb(gm_gps_basic_block BB, gm_rwinfo_sets S, boolean check_receivers) {
		if (S == null)
			S = new gm_rwinfo_sets();
		assert check_receivers == false;

		// -------------------------------------------
		// traverse AST inside BB
		// merge read/write sets
		// caution for communicating symbols
		// -------------------------------------------
		gm_gps_find_rwinfo_simple T = new gm_gps_find_rwinfo_simple(S);
		T.set_check_receiver(check_receivers);

		// post && pre
		GlobalMembersGm_gps_misc.gps_bb_traverse_ast(BB, T, true, true); 

		return S;
	}
}