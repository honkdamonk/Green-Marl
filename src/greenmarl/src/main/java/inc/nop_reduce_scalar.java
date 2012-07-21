package inc;

import ast.ast_assign;
import ast.ast_expr;
import ast.ast_id;
import ast.ast_nop;
import backend_cpp.*;
import backend_giraph.*;
import common.*;
import frontend.*;
import opt.*;
import tangible.*;

public class nop_reduce_scalar extends ast_nop
{
	public nop_reduce_scalar()
	{
		super(nop_enum_cpp.NOP_REDUCE_SCALAR);
	}
	public void set_symbols(java.util.LinkedList<gm_symtab_entry> O, java.util.LinkedList<gm_symtab_entry> N, java.util.LinkedList<Integer> R, java.util.LinkedList<java.util.LinkedList<gm_symtab_entry>> O_S, java.util.LinkedList<java.util.LinkedList<gm_symtab_entry>> N_S)
	{
		// shallow copy the whole list 
		old_s = O;
		new_s = N;
		reduce_op = R;
		old_supple = O_S;
		new_supple = N_S;
	}

	public boolean do_rw_analysis()
	{
    
		gm_rwinfo_sets sets = GlobalMembersGm_rw_analysis.get_rwinfo_sets(this);
	//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy constructor call - this should be verified and a copy constructor should be created if it does not yet exist:
	//ORIGINAL LINE: java.util.HashMap<gm_symtab_entry*, java.util.LinkedList<gm_rwinfo*>*>& R = sets->read_set;
		java.util.HashMap<gm_symtab_entry, java.util.LinkedList<gm_rwinfo>> R = new java.util.HashMap(sets.read_set);
	//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy constructor call - this should be verified and a copy constructor should be created if it does not yet exist:
	//ORIGINAL LINE: java.util.HashMap<gm_symtab_entry*, java.util.LinkedList<gm_rwinfo*>*>& W = sets->write_set;
		java.util.HashMap<gm_symtab_entry, java.util.LinkedList<gm_rwinfo>> W = new java.util.HashMap(sets.write_set);
    
		// read all old symbols
		java.util.Iterator<gm_symtab_entry> I;
		for (I = old_s.begin(); I.hasNext();)
		{
			gm_rwinfo r = gm_rwinfo.new_scala_inst((I.next()).getId());
			GlobalMembersGm_rw_analysis.gm_add_rwinfo_to_set(R, I.next(), r);
		}
    
		// write all new symbols
		for (I = new_s.begin(); I.hasNext();)
		{
			gm_rwinfo w = gm_rwinfo.new_scala_inst((I.next()).getId());
			GlobalMembersGm_rw_analysis.gm_add_rwinfo_to_set(W, I.next(), w);
		}
    
		// read all old supple lhs symbols
		java.util.Iterator<java.util.LinkedList<gm_symtab_entry>> II;
		for (II = old_supple.begin(); II.hasNext();)
		{
			java.util.LinkedList<gm_symtab_entry> L = II.next();
			for (I = L.iterator(); I.hasNext();)
			{
				gm_rwinfo r = gm_rwinfo.new_scala_inst((I.next()).getId());
				GlobalMembersGm_rw_analysis.gm_add_rwinfo_to_set(R, I.next(), r);
			}
		}
		for (II = new_supple.begin(); II.hasNext();)
		{
			java.util.LinkedList<gm_symtab_entry> L = II.next();
			for (I = L.iterator(); I.hasNext();)
			{
				gm_rwinfo w = gm_rwinfo.new_scala_inst((I.next()).getId());
				GlobalMembersGm_rw_analysis.gm_add_rwinfo_to_set(W, I.next(), w);
			}
		}
    
		return true;
	}
	public void generate(gm_cpp_gen gen)
	{
		java.util.Iterator<gm_symtab_entry> I1;
		java.util.Iterator<gm_symtab_entry> I2;
		java.util.Iterator<Integer> I3;
		java.util.Iterator<java.util.LinkedList<gm_symtab_entry>> I4; // supple old
		java.util.Iterator<java.util.LinkedList<gm_symtab_entry>> I5; // supple new
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
			java.util.LinkedList<gm_symtab_entry> OLD_LIST = I4.next();
			java.util.LinkedList<gm_symtab_entry> NEW_LIST = I5.next();
    
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

	public java.util.LinkedList<gm_symtab_entry> old_s = new java.util.LinkedList<gm_symtab_entry>();
	public java.util.LinkedList<gm_symtab_entry> new_s = new java.util.LinkedList<gm_symtab_entry>();
	public java.util.LinkedList<Integer> reduce_op = new java.util.LinkedList<Integer>();
	public java.util.LinkedList<java.util.LinkedList<gm_symtab_entry>> old_supple = new java.util.LinkedList<java.util.LinkedList<gm_symtab_entry>>(); // supplimental lhs for argmin/argmax
	public java.util.LinkedList<java.util.LinkedList<gm_symtab_entry>> new_supple = new java.util.LinkedList<java.util.LinkedList<gm_symtab_entry>>();
}