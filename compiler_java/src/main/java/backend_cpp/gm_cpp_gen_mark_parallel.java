package backend_cpp;

import inc.gm_compile_step;
import ast.ast_procdef;

import common.gm_parallel_helper;

class gm_cpp_gen_mark_parallel extends gm_compile_step {
	
	private gm_cpp_gen_mark_parallel() {
		set_description("Mark every parallel sentence");
	}

	@Override
	public void process(ast_procdef p) {
		final boolean entry_is_seq = true;
		gm_parallel_helper.gm_mark_sents_under_parallel_execution(p.get_body(), entry_is_seq);
	}

	@Override
	public gm_compile_step get_instance() {
		return new gm_cpp_gen_mark_parallel();
	}

	public static gm_compile_step get_factory() {
		return new gm_cpp_gen_mark_parallel();
	}
	
}