package backend_cpp;

import ast.ast_node_type;
import ast.ast_foreach;
import ast.ast_sent;

import common.gm_apply;

class set_to_seq_t extends gm_apply {
	
	@Override
	public boolean apply(ast_sent s) {
		if (s.get_nodetype() == ast_node_type.AST_FOREACH) {
			ast_foreach fe = (ast_foreach) s;
			fe.set_sequential(true);
		}
		return true;
	}
}