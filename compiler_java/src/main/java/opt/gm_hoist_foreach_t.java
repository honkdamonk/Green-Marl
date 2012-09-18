package opt;

import ast.ast_assign;
import ast.ast_node_type;
import ast.ast_sent;

//--------------------------------------------------------------------
// hoist up initialization assignment as far as possible
//--------------------------------------------------------------------

// code almost similar to hoist_assign. <i.e. need to restructure rather than copy-paste>
public class gm_hoist_foreach_t extends gm_hoist_normal_sent_t {
	
	protected gm_check_if_constant_t is_const_check = new gm_check_if_constant_t();
	
	// need post apply.
	@Override
	protected boolean check_target(ast_sent target) {
		if (target.get_nodetype() != ast_node_type.AST_FOREACH)
			return false;
		else
			return true;
	}

	@Override
	protected boolean check_trivial_pred(ast_sent S) {
		if (S.get_nodetype() == ast_node_type.AST_VARDECL)
			return true;
		else if (S.get_nodetype() == ast_node_type.AST_FOREACH)
			return true;
		else if (S.get_nodetype() == ast_node_type.AST_ASSIGN) {
			ast_assign a = (ast_assign) S;
			is_const_check.prepare();
			a.get_rhs().traverse_pre(is_const_check);

			if (is_const_check.is_const())
				return true; // do not pass over const assignment
			else
				return false;
		} else
			return false;
	}

}
