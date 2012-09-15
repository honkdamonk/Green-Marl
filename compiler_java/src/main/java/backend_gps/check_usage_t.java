package backend_gps;

import static backend_gps.gm_gps_opt_merge_ebb_again.FOR_PAR1;
import static backend_gps.gm_gps_opt_merge_ebb_again.FOR_PAR2;
import static backend_gps.gm_gps_opt_merge_ebb_again.FOR_SEQ1;
import frontend.gm_symtab_entry;
import inc.gm_reduce;
import inc.gps_apply_bb_ast;

import java.util.HashMap;
import java.util.LinkedList;

import ast.ast_assign;
import ast.ast_expr;
import ast.ast_id;
import ast.ast_node;
import ast.ast_node_type;
import ast.ast_sent;

class check_usage_t extends gps_apply_bb_ast {

	private final HashMap<gm_symtab_entry, gm_reduce> usage = new HashMap<gm_symtab_entry, gm_reduce>();
	private final HashMap<gm_symtab_entry, gm_reduce> read_usage = new HashMap<gm_symtab_entry, gm_reduce>();
	private boolean _okay_to_merge = true;
	private int _state;

	public check_usage_t() {
		set_is_pre(true);
		set_state(FOR_PAR1);
	}

	public final boolean is_okay_to_merge() {
		return _okay_to_merge;
	}

	public final void set_state(int s) {
		_state = s;
		if (_state == FOR_PAR1) {
			set_for_sent(true);
			set_for_expr(false);
		} else {
			set_for_sent(true);
			set_for_expr(true);
		}
	}

	@Override
	public void apply(gm_gps_basic_block b) {
		super.apply(b);
	}

	@Override
	public boolean apply(ast_sent s) {
		switch (_state) {
		case FOR_PAR1:
			apply_par1(s);
			break;
		case FOR_SEQ1:
			apply_seq1(s);
			break;
		case FOR_PAR2:
			apply_par2(s);
			break;
		}
		return true;
	}

	@Override
	public boolean apply(ast_expr e) {
		switch (_state) {
		case FOR_SEQ1:
			apply_seq1(e);
			break;
		case FOR_PAR2:
			apply_par2(e);
			break;
		}
		return true;
	}

	// ----------------------------------------------------------

	public final boolean check_symbol_write_in_par2(gm_symtab_entry e, gm_reduce gmreduceInvalid) {
		// read in SEQ1? --> not okay
		if (read_usage.containsKey(e)) // read in SEQ1
		{
			_okay_to_merge = false;
			return false;
		}
		// reduced in SEQ1/PAR1? --> okay, only if same reduction
		if (usage.containsKey(e)) {
			gm_reduce reduce_type = usage.get(e);
			if ((reduce_type != gm_reduce.GMREDUCE_INVALID) && (reduce_type == gmreduceInvalid))
				return true;
			_okay_to_merge = false;
			return false;
		}

		return true;
	}

	public final boolean check_symbol_read_in_par2(gm_symtab_entry e) {
		// wrote in SEQ1, PAR1? --> not_okay
		if (usage.containsKey(e)) {
			_okay_to_merge = false;
			return false;
		}
		return true;
	}

	// ----------------------------------------------------------------------
	public final boolean apply_par2(ast_sent s) {
		if (s.get_nodetype() == ast_node_type.AST_ASSIGN) {
			ast_assign a = (ast_assign) s;

			// property access is fine
			if (!a.is_target_scalar())
				return true;

			gm_symtab_entry e = a.get_lhs_scala().getSymInfo();

			// check if this symbol is used in SEQ1
			// or modified in PAR1 or SEQ1
			if (!check_symbol_write_in_par2(e, a.get_reduce_type()))
				return true;

			if (a.is_argminmax_assign()) {
				LinkedList<ast_node> L = a.get_lhs_list();
				for (ast_node node : L) {
					assert node.get_nodetype() == ast_node_type.AST_ID;
					gm_symtab_entry e2 = ((ast_id) node).getSymInfo();
					if (!check_symbol_write_in_par2(e2, gm_reduce.GMREDUCE_INVALID))
						return true;
				}
			}
		}

		// [todo] other lhs:
		return true;
	}

	public final boolean apply_seq1(ast_sent s) {
		return apply_par1(s);
	}

	// check reductions to global scala symbols
	public final boolean apply_par1(ast_sent s) {
		if (s.get_nodetype() == ast_node_type.AST_ASSIGN) {
			ast_assign a = (ast_assign) s;

			// property access is fine
			if (!a.is_target_scalar())
				return true;

			gm_symtab_entry e = a.get_lhs_scala().getSymInfo();
			if (a.is_argminmax_assign()) {
				usage.put(e, gm_reduce.GMREDUCE_INVALID); // normal write
				LinkedList<ast_node> L = a.get_lhs_list();
				for (ast_node node : L) {
					assert node.get_nodetype() == ast_node_type.AST_ID;
					gm_symtab_entry e2 = ((ast_id) node).getSymInfo();
					usage.put(e2, gm_reduce.GMREDUCE_INVALID);
				}
			} else if (!a.is_reduce_assign()) {
				usage.put(e, gm_reduce.GMREDUCE_INVALID); // normal write
			} else {
				if (!usage.containsKey(e)) {
					usage.put(e, a.get_reduce_type());
				}
			}
		}
		// [todo] other lhs:
		return true;
	}

	// ----------------------------------------------------------------------
	public final boolean apply_par2(ast_expr e) {
		if (e.is_id()) {
			check_symbol_read_in_par2(e.get_id().getSymInfo());
		}
		return true;
	}

	public final boolean apply_seq1(ast_expr e) {
		if (e.is_id()) {
			// TODO: changed 1 to GMREDUCE_PLUS, is this ok?
			read_usage.put(e.get_id().getSymInfo(), gm_reduce.GMREDUCE_PLUS);
		}
		return true;
	}

}