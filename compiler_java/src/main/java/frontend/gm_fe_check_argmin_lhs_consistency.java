package frontend;

import ast.AST_NODE_TYPE;
import ast.ast_assign;
import ast.ast_field;
import ast.ast_node;
import ast.ast_sent;

import common.GM_ERRORS_AND_WARNINGS;
import common.GlobalMembersGm_error;
import common.gm_apply;

public class gm_fe_check_argmin_lhs_consistency extends gm_apply {

	public gm_fe_check_argmin_lhs_consistency() {
		set_for_sent(true);
		_is_okay = true;
	}

	public final boolean is_okay() {
		return _is_okay;
	}

	@Override
	public boolean apply(ast_sent s) {
		if (s.get_nodetype() != AST_NODE_TYPE.AST_ASSIGN)
			return false;
		ast_assign a = (ast_assign) s;
		if (!a.is_argminmax_assign())
			return false;

		java.util.LinkedList<ast_node> L = a.get_lhs_list();
		if (a.is_target_scalar()) {
			for (ast_node n : L) {
				if (n.get_nodetype() != AST_NODE_TYPE.AST_ID) {
					GlobalMembersGm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_INCONSISTENT_ARGMAX, n.get_line(), n.get_col());
					_is_okay = false;
				}
			}
		} else {
			gm_symtab_entry sym = a.get_lhs_field().get_first().getSymInfo();
			for (ast_node n : L) {
				if (n.get_nodetype() != AST_NODE_TYPE.AST_FIELD) {
					GlobalMembersGm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_INCONSISTENT_ARGMAX, n.get_line(), n.get_col());
					_is_okay = false;
				} else {
					ast_field f = (ast_field) n;
					gm_symtab_entry d = f.get_first().getSymInfo();
					if (d != sym) {
						GlobalMembersGm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_INCONSISTENT_ARGMAX, n.get_line(), n.get_col());
						_is_okay = false;
					}
				}
			}
		}
		return true;
	}

	private boolean _is_okay;
}
// bool gm_frontend::do_typecheck_step5_check_assign(ast_procdef* p)
