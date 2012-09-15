package frontend;

import ast.ast_node_type;
import ast.ast_assign;
import ast.ast_sent;

import common.gm_errors_and_warnings;
import common.gm_apply;
import common.gm_error;

public class check_argmax_num_args_t extends gm_apply {
	
	private boolean _is_okay = true;
	
	public check_argmax_num_args_t() {
		set_for_sent(true);
	}

	// post apply
	@Override
	public final boolean apply(ast_sent e) {
		if (e.get_nodetype() == ast_node_type.AST_ASSIGN) {
			ast_assign a = (ast_assign) e;
			if (a.is_argminmax_assign()) {
				int l_count = a.get_lhs_list().size();
				int r_count = a.get_rhs_list().size();
				if (l_count != r_count) {
					String temp = String.format("lhs_count:%d, rhs_count:%d", l_count, r_count);
					gm_error.gm_type_error(gm_errors_and_warnings.GM_ERROR_INVALID_ARGMAX_COUNT, a.get_line(), a.get_col(), temp);
					_is_okay = false;
				}
			}
		}

		return true;
	}

	public final boolean is_okay() {
		return _is_okay;
	}

}