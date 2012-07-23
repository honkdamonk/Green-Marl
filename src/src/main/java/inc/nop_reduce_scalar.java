package inc;

import java.util.HashMap;
import java.util.LinkedList;

import ast.ast_assign;
import ast.ast_expr;
import ast.ast_id;
import ast.ast_nop;
import backend_cpp.gm_cpp_gen;
import frontend.GlobalMembersGm_rw_analysis;
import frontend.gm_rwinfo;
import frontend.gm_rwinfo_sets;
import frontend.gm_symtab_entry;

public class nop_reduce_scalar extends ast_nop {
	public nop_reduce_scalar() {
		super(nop_enum_cpp.NOP_REDUCE_SCALAR.getValue());
	}

	public void set_symbols(LinkedList<gm_symtab_entry> O, LinkedList<gm_symtab_entry> N, LinkedList<Integer> R, LinkedList<LinkedList<gm_symtab_entry>> O_S,
			LinkedList<LinkedList<gm_symtab_entry>> N_S) {
		// shallow copy the whole list
		old_s = O;
		new_s = N;
		reduce_op = R;
		old_supple = O_S;
		new_supple = N_S;
	}

	public boolean do_rw_analysis() {

		gm_rwinfo_sets sets = GlobalMembersGm_rw_analysis.get_rwinfo_sets(this);
		// C++ TO JAVA CONVERTER WARNING: The following line was determined to
		// be a copy constructor call - this should be verified and a copy
		// constructor should be created if it does not yet exist:
		// ORIGINAL LINE: HashMap<gm_symtab_entry*, LinkedList<gm_rwinfo*>*>& R
		// = sets->read_set;
		HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> R = new HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>>(sets.read_set);
		// C++ TO JAVA CONVERTER WARNING: The following line was determined to
		// be a copy constructor call - this should be verified and a copy
		// constructor should be created if it does not yet exist:
		// ORIGINAL LINE: HashMap<gm_symtab_entry*, LinkedList<gm_rwinfo*>*>& W
		// = sets->write_set;
		HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> W = new HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>>(sets.write_set);

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

	public void generate(gm_cpp_gen gen)
	{
		java.util.Iterator<gm_symtab_entry> I1;
		java.util.Iterator<gm_symtab_entry> I2;
		java.util.Iterator<Integer> I3;
		java.util.Iterator<LinkedList<gm_symtab_entry>> I4; // supple old
		java.util.Iterator<LinkedList<gm_symtab_entry>> I5; // supple new
		I1 = old_s.begin();
		I2 = new_s.begin();
		I3 = reduce_op.begin();
		I4 = old_supple.begin();
		I5 = new_supple.begin();
		for (; I1.hasNext(); I1++, I2++, I3++)
		{
			gm_symtab_entry old_sym = I1.next();
			gm_symtab_entry new_sym = I2.next();
			int r_type = I3.next();
			LinkedList<gm_symtab_entry> OLD_LIST = I4.next();
			LinkedList<gm_symtab_entry> NEW_LIST = I5.next();
    
			ast_id lhs = old_sym.getId().copy(true);
			ast_id rhs_s = new_sym.getId().copy(true);
			ast_expr rhs = ast_expr.new_id_expr(rhs_s);
    
			ast_assign new_assign = ast_assign.new_assign_scala(lhs, rhs, gm_assignment_t.GMASSIGN_REDUCE, null, r_type);
    
			if (OLD_LIST.size() > 0)
			{
				assert OLD_LIST.size() == NEW_LIST.size();
				new_assign.set_argminmax_assign(true);
				java.util.Iterator<gm_symtab_entry> J1 = OLD_LIST.iterator();
				java.util.Iterator<gm_symtab_entry> J2 = NEW_LIST.iterator();
				for (; J1.hasNext(); J1++, J2++)
				{
					gm_symtab_entry lhs_sym = J1.next();
					gm_symtab_entry rhs_sym = J2.next();
					assert lhs_sym != null;
					assert rhs_sym != null;
					ast_id lhs = lhs_sym.getId().copy(true);
					(assert(lhs != null));
					assert lhs.getSymInfo() != null;
					ast_id rhs_s = rhs_sym.getId().copy(true);
					ast_expr rhs = ast_expr.new_id_expr(rhs_s);
					//printf("Hello:%s\n", lhs->get_genname());
					new_assign.get_lhs_list().addLast(lhs);
					new_assign.get_rhs_list().addLast(rhs);
				}
			}
    
			gen.generate_sent_reduce_assign(new_assign);
    
			if (new_assign != null)
				new_assign.dispose();
		}
	}

	public LinkedList<gm_symtab_entry> old_s = new LinkedList<gm_symtab_entry>();
	public LinkedList<gm_symtab_entry> new_s = new LinkedList<gm_symtab_entry>();
	public LinkedList<Integer> reduce_op = new LinkedList<Integer>();
	public LinkedList<LinkedList<gm_symtab_entry>> old_supple = new LinkedList<LinkedList<gm_symtab_entry>>(); // supplimental
																												// lhs
																												// for
																												// argmin/argmax
	public LinkedList<LinkedList<gm_symtab_entry>> new_supple = new LinkedList<LinkedList<gm_symtab_entry>>();
}