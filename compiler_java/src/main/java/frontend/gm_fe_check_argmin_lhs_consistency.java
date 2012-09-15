package frontend;

import java.util.LinkedList;

import ast.ast_assign;
import ast.ast_field;
import ast.ast_node;
import ast.ast_node_type;
import ast.ast_sent;

import common.gm_apply;
import common.gm_error;
import common.gm_errors_and_warnings;

public class gm_fe_check_argmin_lhs_consistency extends gm_apply {

	private boolean _is_okay = true;

	public gm_fe_check_argmin_lhs_consistency() {
		set_for_sent(true);
	}

	public final boolean is_okay() {
		return _is_okay;
	}

	@Override
	public boolean apply(ast_sent s) {
		if (s.get_nodetype() != ast_node_type.AST_ASSIGN)
			return false;
		ast_assign a = (ast_assign) s;
		if (!a.is_argminmax_assign())
			return false;

		LinkedList<ast_node> L = a.get_lhs_list();
		if (a.is_target_scalar()) {
			for (ast_node n : L) {
				if (n.get_nodetype() != ast_node_type.AST_ID) {
					gm_error.gm_type_error(gm_errors_and_warnings.GM_ERROR_INCONSISTENT_ARGMAX, n.get_line(), n.get_col());
					_is_okay = false;
				}
			}
		} else {
			gm_symtab_entry sym = a.get_lhs_field().get_first().getSymInfo();
			for (ast_node n : L) {
				if (n.get_nodetype() != ast_node_type.AST_FIELD) {
					gm_error.gm_type_error(gm_errors_and_warnings.GM_ERROR_INCONSISTENT_ARGMAX, n.get_line(), n.get_col());
					_is_okay = false;
				} else {
					ast_field f = (ast_field) n;
					gm_symtab_entry d = f.get_first().getSymInfo();
					if (d != sym) {
						gm_error.gm_type_error(gm_errors_and_warnings.GM_ERROR_INCONSISTENT_ARGMAX, n.get_line(), n.get_col());
						_is_okay = false;
					}
				}
			}
		}
		return true;
	}

}
