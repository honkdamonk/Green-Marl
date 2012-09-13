package backend_cpp;

import ast.AST_NODE_TYPE;
import ast.ast_foreach;
import ast.ast_sent;

import common.gm_apply;

// choice of parallel region
class make_all_seq_t extends gm_apply {
	
	@Override
	public boolean apply(ast_sent s) {
		if (s.get_nodetype() == AST_NODE_TYPE.AST_FOREACH) {
			ast_foreach fe = (ast_foreach) s;
			fe.set_sequential(true);
		}
		return true;
	}
}