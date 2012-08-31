package backend_gps;

import inc.gm_compile_step;
import ast.ast_procdef;

import common.gm_transform_helper;

public class gm_gps_new_rewrite_rhs extends gm_compile_step {

	private gm_gps_new_rewrite_rhs() {
		set_description("Rewriting rhs for messages");
	}

	public void process(ast_procdef proc) {

		// TODO--> get rid of 'parent' pointer of symtabs. (It is hard to move
		// sentences around with this)
		gm_transform_helper.gm_reconstruct_scope(proc);

		gps_rewrite_rhs_preprocessing_t T1 = new gps_rewrite_rhs_preprocessing_t();
		proc.traverse_pre(T1);
		T1.process();

		gps_rewrite_rhs_t T2 = new gps_rewrite_rhs_t();
		proc.traverse_pre(T2);
		T2.process();

		// now re-do analysis
		gm_gps_new_analysis_scope_sent_var_t.gm_gps_do_new_analysis_scope_sent_var(proc);
		gm_gps_new_analyze_scope_rhs_lhs.gm_gps_do_new_analysis_rhs_lhs(proc);
	}

	@Override
	public gm_compile_step get_instance() {
		return new gm_gps_new_rewrite_rhs();
	}

	public static gm_compile_step get_factory() {
		return new gm_gps_new_rewrite_rhs();
	}
	
}