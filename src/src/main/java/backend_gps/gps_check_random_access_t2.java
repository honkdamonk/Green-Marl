package backend_gps;

import ast.AST_NODE_TYPE;
import ast.ast_field;
import ast.ast_id;
import ast.ast_sent;
import ast.ast_sentblock;
import frontend.gm_symtab_entry;
import inc.GlobalMembersGm_backend_gps;

import common.GM_ERRORS_AND_WARNINGS;
import common.GlobalMembersGm_add_symbol;
import common.GlobalMembersGm_error;
import common.gm_apply;

public class gps_check_random_access_t2 extends gm_apply {
	public gps_check_random_access_t2() {
		set_for_lhs(true);
		_error = false;
	}

	public final boolean is_error() {
		return _error;
	}

	public final boolean apply_lhs(ast_id i) {
		gm_symtab_entry sym = i.getSymInfo();

		if (sym.getType().is_node()
				&& (sym.find_info_int(GlobalMembersGm_backend_gps.GPS_INT_SYMBOL_SCOPE) == gm_gps_new_scope_analysis_t.GPS_NEW_SCOPE_OUT.getValue())) {
			// redefined;
			if (is_defined(sym)) {
				GlobalMembersGm_error.gm_backend_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_GPS_RANDOM_NODE_WRITE_REDEF, i.get_line(), i.get_col());
				_error = true;
			} else {
				add_define(sym);
			}
		}

		return true;
	}

	public final boolean apply_lhs(ast_field f) {
		gm_symtab_entry sym = f.get_first().getSymInfo();
		ast_sent s = get_current_sent();
		if (sym.getType().is_node_compatible()) {
			boolean is_random_write = false;
			if (sym.find_info_bool(GlobalMembersGm_backend_gps.GPS_FLAG_IS_INNER_LOOP)
					|| sym.find_info_bool(GlobalMembersGm_backend_gps.GPS_FLAG_IS_OUTER_LOOP)) {
				// non random write
			} else if (sym.find_info_int(GlobalMembersGm_backend_gps.GPS_INT_SYMBOL_SCOPE) == gm_gps_new_scope_analysis_t.GPS_NEW_SCOPE_OUT.getValue()) {
				if (s.find_info_int(GlobalMembersGm_backend_gps.GPS_INT_SYNTAX_CONTEXT) != gm_gps_new_scope_analysis_t.GPS_NEW_SCOPE_OUT.getValue()) {
					_error = true;
					GlobalMembersGm_error.gm_backend_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_GPS_RANDOM_NODE_WRITE_USE_SCOPE, f.get_line(), f.get_col());
				} else if (GlobalMembersGm_gps_new_check_random_write.check_if_met_conditional_before(s, sym)) {
					_error = true;
					GlobalMembersGm_error.gm_backend_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_GPS_RANDOM_NODE_WRITE_CONDITIONAL, f.get_line(), f.get_col());
				}

				// okay
				is_random_write = true;
			} else {
				_error = true;
				GlobalMembersGm_error.gm_backend_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_GPS_RANDOM_NODE_WRITE_DEF_SCOPE, f.get_line(), f.get_col());
			}

			if (is_random_write) {
				ast_sentblock sb = GlobalMembersGm_add_symbol.gm_find_defining_sentblock_up(s, sym);
				assert sb != null;
				assert s.get_nodetype() == AST_NODE_TYPE.AST_ASSIGN;
				s.add_info_ptr(GlobalMembersGm_backend_gps.GPS_FLAG_SENT_BLOCK_FOR_RANDOM_WRITE_ASSIGN, sb);
				sb.add_info_set_element(GlobalMembersGm_backend_gps.GPS_FLAG_RANDOM_WRITE_SYMBOLS_FOR_SB, sym);
			}
		}

		return true;
	}

	private boolean is_defined(gm_symtab_entry e) {
		return defined.contains(e);
	}

	private void add_define(gm_symtab_entry e) {
		defined.add(e);
	}

	private boolean _error;
	private java.util.HashSet<gm_symtab_entry> defined = new java.util.HashSet<gm_symtab_entry>();
}