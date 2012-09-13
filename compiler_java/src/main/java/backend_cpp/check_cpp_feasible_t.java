package backend_cpp;

import ast.AST_NODE_TYPE;
import ast.ast_sent;

import common.GM_ERRORS_AND_WARNINGS;
import common.gm_apply;
import common.gm_error;

class check_cpp_feasible_t extends gm_apply {
	
	private boolean _okay = true;
	private int bfs_depth = 0;
	
	check_cpp_feasible_t() {
		set_for_symtab(true);
		set_for_sent(true);
		set_separate_post_apply(true);
	}

	@Override
	public final boolean apply(ast_sent s) {
		if (s.get_nodetype() == AST_NODE_TYPE.AST_BFS) {
			if (bfs_depth > 0) {
				gm_error.gm_backend_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_CPP_UNSUPPORTED_SYNTAX, s.get_line(), s.get_col(), "nested DFS/BFS.");
				set_okay(false);
			}
			bfs_depth++;
		}
		return true;
	}

	@Override
	public final boolean apply2(ast_sent s) {
		if (s.get_nodetype() == AST_NODE_TYPE.AST_BFS) {
			bfs_depth--;
		}
		return true;
	}

	final boolean is_okay() {
		return _okay;
	}

	final void set_okay(boolean b) {
		_okay = b;
	}

}