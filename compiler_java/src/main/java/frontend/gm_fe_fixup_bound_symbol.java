package frontend;

import inc.gm_compile_step;
import ast.ast_procdef;

import common.gm_traverse;

public class gm_fe_fixup_bound_symbol extends gm_compile_step {
	
	private gm_fe_fixup_bound_symbol() {
		set_description("Select bound symbols and optimize");
	}

	@Override
	public void process(ast_procdef p) {
		find_hpb_t T = new find_hpb_t();
		T.set_opt_seq_bound(true);

		gm_traverse.gm_traverse_sents(p, T);
		T.post_process();
	}

	@Override
	public gm_compile_step get_instance() {
		return new gm_fe_fixup_bound_symbol();
	}

	public static gm_compile_step get_factory() {
		return new gm_fe_fixup_bound_symbol();
	}
}