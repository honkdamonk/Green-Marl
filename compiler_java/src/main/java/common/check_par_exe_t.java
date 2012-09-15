package common;

import java.util.LinkedList;

import ast.ast_node_type;
import ast.ast_bfs;
import ast.ast_foreach;
import ast.ast_node;
import ast.ast_sent;

public class check_par_exe_t extends gm_apply {

	private final LinkedList<Boolean> _context = new LinkedList<Boolean>();
	private boolean _current_is_par;
	private ast_node _current_context = null;

	public check_par_exe_t(boolean entry_is_seq) {
		_current_is_par = !entry_is_seq;
	}

	// ----------------------------------------
	// called sequence for node n
	// begin_context(n) : AST_PROCDEF, AST_FOREACH, AST_SENTBLOCK, AST_BFS,
	// AST_REDUCE_EXPR
	// apply(n)
	// (traverse)
	// end_context(n)
	// ----------------------------------------

	@Override
	public void begin_context(ast_node n) {
		if (!n.is_sentence())
			return;

		// process my self
		((ast_sent) n).set_under_parallel_execution(_current_is_par);

		// save context
		_context.addLast(_current_is_par);

		// begin new context
		if (n.get_nodetype() == ast_node_type.AST_BFS) {
			_current_is_par = _current_is_par || ((ast_bfs) n).is_parallel();
		} else if (n.get_nodetype() == ast_node_type.AST_FOREACH) {
			_current_is_par = _current_is_par || ((ast_foreach) n).is_parallel();
		}
		_current_context = n;
	}

	@Override
	public void end_context(ast_node n) {
		if (!n.is_sentence())
			return;
		_current_is_par = _context.getLast();
		_context.removeLast();
	}

	@Override
	public boolean apply(ast_sent s) {
		if (_current_context == s)
			return true;
		s.set_under_parallel_execution(_current_is_par);
		return true;
	}

}