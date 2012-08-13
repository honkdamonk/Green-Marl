package common;

import ast.ast_sent;

public class GlobalMembersGm_parallel_helper
{

	public static void gm_mark_sents_under_parallel_execution(ast_sent S, boolean entry_is_seq)
	{
		check_par_exe_t T = new check_par_exe_t(entry_is_seq);
		GlobalMembersGm_traverse.gm_traverse_sents(S, T);
	}
}