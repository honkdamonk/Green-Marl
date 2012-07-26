package inc;

import java.util.LinkedList;

import ast.ast_assign;
import ast.ast_expr;
import ast.ast_id;
import ast.ast_nop;
import ast.gm_rwinfo_map;
import backend_cpp.gm_cpp_gen;
import frontend.GlobalMembersGm_rw_analysis;
import frontend.gm_rwinfo;
import frontend.gm_rwinfo_sets;
import frontend.gm_symtab_entry;

public class nop_reduce_scalar extends ast_nop {

	public nop_reduce_scalar() {
		super(nop_enum_cpp.NOP_REDUCE_SCALAR);
	}

	public void set_symbols(LinkedList<gm_symtab_entry> O, LinkedList<gm_symtab_entry> N, LinkedList<GM_REDUCE_T> R,
			LinkedList<LinkedList<gm_symtab_entry>> O_S, LinkedList<LinkedList<gm_symtab_entry>> N_S) {
		// shallow copy the whole list
		old_s = O;
		new_s = N;
		reduce_op = R;
		old_supple = O_S;
		new_supple = N_S;
	}

	public boolean do_rw_analysis() {

		gm_rwinfo_sets sets = GlobalMembersGm_rw_analysis.get_rwinfo_sets(this);
		gm_rwinfo_map R = new gm_rwinfo_map(sets.read_set);
		gm_rwinfo_map W = new gm_rwinfo_map(sets.write_set);

		// read all old symbols
		for (gm_symtab_entry entry : old_s) {
			gm_rwinfo r = gm_rwinfo.new_scala_inst(entry.getId());
			GlobalMembersGm_rw_analysis.gm_add_rwinfo_to_set(R, entry, r);
		}

		// write all new symbols
		for (gm_symtab_entry entry : old_s) {
			gm_rwinfo w = gm_rwinfo.new_scala_inst(entry.getId());
			GlobalMembersGm_rw_analysis.gm_add_rwinfo_to_set(W, entry, w);
		}

		// read all old supple lhs symbols
		for (LinkedList<gm_symtab_entry> L : old_supple) {
			for (gm_symtab_entry entry : L) {
				gm_rwinfo r = gm_rwinfo.new_scala_inst(entry.getId());
				GlobalMembersGm_rw_analysis.gm_add_rwinfo_to_set(R, entry, r);
			}
		}
		for (LinkedList<gm_symtab_entry> L : new_supple) {
			for (gm_symtab_entry entry : L) {
				gm_rwinfo w = gm_rwinfo.new_scala_inst(entry.getId());
				GlobalMembersGm_rw_analysis.gm_add_rwinfo_to_set(W, entry, w);
			}
		}

		return true;
	}

	public void generate(gm_cpp_gen gen) {
		for (int i = 0; i < old_s.size(); i++) {
			gm_symtab_entry old_sym = old_s.get(i);
			gm_symtab_entry new_sym = new_s.get(i);
			GM_REDUCE_T r_type = reduce_op.get(i);
			// supple old
			LinkedList<gm_symtab_entry> OLD_LIST = old_supple.get(0);
			// supple new
			LinkedList<gm_symtab_entry> NEW_LIST = new_supple.get(0);

			ast_id lhs = old_sym.getId().copy(true);
			ast_id rhs_s = new_sym.getId().copy(true);
			ast_expr rhs = ast_expr.new_id_expr(rhs_s);

			ast_assign new_assign = ast_assign.new_assign_scala(lhs, rhs, gm_assignment_t.GMASSIGN_REDUCE, null, r_type);

			if (OLD_LIST.size() > 0) {
				assert OLD_LIST.size() == NEW_LIST.size();
				new_assign.set_argminmax_assign(true);
				for (int j = 0; i < OLD_LIST.size(); j++) {
					gm_symtab_entry lhs_sym = OLD_LIST.get(j);
					gm_symtab_entry rhs_sym = NEW_LIST.get(j);
					assert lhs_sym != null;
					assert rhs_sym != null;
					ast_id lhs_inner = lhs_sym.getId().copy(true);
					assert (lhs_inner != null);
					assert lhs_inner.getSymInfo() != null;
					ast_id rhs_s_inner = rhs_sym.getId().copy(true);
					ast_expr rhs_inner = ast_expr.new_id_expr(rhs_s_inner);
					new_assign.get_lhs_list().addLast(lhs_inner);
					new_assign.get_rhs_list().addLast(rhs_inner);
				}
			}

			gen.generate_sent_reduce_assign(new_assign);

			if (new_assign != null)
				new_assign.dispose();
		}
	}

	public LinkedList<gm_symtab_entry> old_s = new LinkedList<gm_symtab_entry>();
	public LinkedList<gm_symtab_entry> new_s = new LinkedList<gm_symtab_entry>();
	public LinkedList<GM_REDUCE_T> reduce_op = new LinkedList<GM_REDUCE_T>();
	public LinkedList<LinkedList<gm_symtab_entry>> old_supple = new LinkedList<LinkedList<gm_symtab_entry>>(); // supplimental
																												// lhs
																												// for
																												// argmin/argmax
	public LinkedList<LinkedList<gm_symtab_entry>> new_supple = new LinkedList<LinkedList<gm_symtab_entry>>();
}