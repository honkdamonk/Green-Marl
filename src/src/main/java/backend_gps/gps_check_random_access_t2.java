package backend_gps;

import static common.GM_ERRORS_AND_WARNINGS.GM_ERROR_GPS_RANDOM_NODE_WRITE_CONDITIONAL;
import static common.GM_ERRORS_AND_WARNINGS.GM_ERROR_GPS_RANDOM_NODE_WRITE_DEF_SCOPE;
import static common.GM_ERRORS_AND_WARNINGS.GM_ERROR_GPS_RANDOM_NODE_WRITE_REDEF;
import static common.GM_ERRORS_AND_WARNINGS.GM_ERROR_GPS_RANDOM_NODE_WRITE_USE_SCOPE;
import inc.GlobalMembersGm_backend_gps;

import java.util.HashSet;

import ast.AST_NODE_TYPE;
import ast.ast_field;
import ast.ast_id;
import ast.ast_node;
import ast.ast_sent;
import ast.ast_sentblock;

import common.GlobalMembersGm_add_symbol;
import common.GlobalMembersGm_error;
import common.gm_apply;

import frontend.gm_symtab_entry;

public class gps_check_random_access_t2 extends gm_apply {

	private boolean _error = false;
	private HashSet<gm_symtab_entry> defined = new HashSet<gm_symtab_entry>();

	public gps_check_random_access_t2() {
		set_for_lhs(true);
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
				GlobalMembersGm_error.gm_backend_error(GM_ERROR_GPS_RANDOM_NODE_WRITE_REDEF, i.get_line(), i.get_col());
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
					GlobalMembersGm_error.gm_backend_error(GM_ERROR_GPS_RANDOM_NODE_WRITE_USE_SCOPE, f.get_line(), f.get_col());
				} else if (check_if_met_conditional_before(s, sym)) {
					_error = true;
					GlobalMembersGm_error.gm_backend_error(GM_ERROR_GPS_RANDOM_NODE_WRITE_CONDITIONAL, f.get_line(), f.get_col());
				}

				// okay
				is_random_write = true;
			} else {
				_error = true;
				GlobalMembersGm_error.gm_backend_error(GM_ERROR_GPS_RANDOM_NODE_WRITE_DEF_SCOPE, f.get_line(), f.get_col());
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

	/**
	 * ----------------------------------------------------------------- Check
	 * random access
	 * ----------------------------------------------------------------- Rules:
	 * - Reading of node properties via random node varaible is not allowed. -
	 * Writing of node properties via random node variable is allowed if - The
	 * node variable is out-scoped (temporary) - The node variable is assigned
	 * only once - The random write is out-scoped (temporary) - The random write
	 * is not conditioned
	 * 
	 * Example>
	 * 
	 * Node(G) root; root.X = 3; * not okay
	 * 
	 * Foreach(n: G.Nodes) { Node(G) y = root; root.X = n.A; * not okay y.X =
	 * n.A; * okay }
	 * 
	 * Foreach(n:G.Nodes) { Foreach(t:n.Nbrs) { Node(G) z = root; root.X = t.A;
	 * * not okay z.X = t.A; * not okay } }
	 * 
	 * Foreach(n: G.Nodes) { Node(G) y = root; y.X = t.A; y = root2; * not okay
	 * }
	 * 
	 * Foreach(n:G.Nodes) { Node(G) y = root; y.B = 0; if (n.A > 0) { Node(G) z
	 * = y; y.C = 1; * not okay z.C = 1; * okay } }
	 * 
	 * [Todo: Multiple definitions? ] { Node(G) y1= root; Node(G) y2= root; y1.X
	 * = 0; y2.X = 1; * what would be the value of root.X after word? }
	 * 
	 * Constructed Information FLAG_SENT_BLOCK_FOR_RANDOM_WRITE_ASSIGN: (to =>
	 * assign_sttement, what: the sentblock that contains random write for this
	 * assign statement) FLAG_RANDWM_WRITE_SYMBOL_FOR_SB (to => sent_block,
	 * what: set of symbols that are used as ramdom-write driver in the
	 * sent-block)
	 * -----------------------------------------------------------------
	 */
	private static boolean check_if_met_conditional_before(ast_node s, gm_symtab_entry symbol) {
		while (true) {
			assert s != null;
			if ((s.get_nodetype() == AST_NODE_TYPE.AST_WHILE) || (s.get_nodetype() == AST_NODE_TYPE.AST_IF)) {
				return true;
			}
			if (s.has_symtab()) {
				if (s.get_symtab_var().is_entry_in_the_tab(symbol)) {
					return false;
				}
			}

			s = (ast_node) s.get_parent();
		}
	}
}