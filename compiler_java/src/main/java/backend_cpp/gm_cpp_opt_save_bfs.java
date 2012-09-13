package backend_cpp;

import ast.ast_procdef;
import inc.gm_compile_step;

class gm_cpp_opt_save_bfs extends gm_compile_step {
	
	private gm_cpp_opt_save_bfs() {
		set_description("Finding BFS Children");
	}

	@Override
	public void process(ast_procdef p) {
		cpp_check_save_bfs_t T = new cpp_check_save_bfs_t();
		p.traverse_both(T);

		return;
	}

	@Override
	public gm_compile_step get_instance() {
		return new gm_cpp_opt_save_bfs();
	}

	public static gm_compile_step get_factory() {
		return new gm_cpp_opt_save_bfs();
	}
}