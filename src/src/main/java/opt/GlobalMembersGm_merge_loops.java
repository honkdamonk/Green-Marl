package opt;

import ast.ast_foreach;
import ast.ast_id;

import common.GlobalMembersGm_resolve_nc;

import frontend.GlobalMembersGm_rw_analysis;
import frontend.gm_range_type_t;
import frontend.gm_rwinfo;
import frontend.gm_rwinfo_sets;
import frontend.gm_symtab_entry;

public class GlobalMembersGm_merge_loops
{
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define TO_STR(X) #X
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define DEF_STRING(X) static const char *X = "X"
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define GM_COMPILE_STEP(CLASS, DESC) class CLASS : public gm_compile_step { private: CLASS() {set_description(DESC);}public: virtual void process(ast_procdef*p); virtual gm_compile_step* get_instance(){return new CLASS();} static gm_compile_step* get_factory(){return new CLASS();} };
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define GM_COMPILE_STEP_FACTORY(CLASS) CLASS::get_factory()

	//---------------------------------------------------------
	// gm_merge_foreach_if_possible(P, Q)
	//   1. check if mergeable
	//   2. replace driver sym
	//   3. merge two body(see gm_merge_sentlock.cc)
	//---------------------------------------------------------
	public static boolean is_linear_access_only(java.util.LinkedList<gm_rwinfo> L)
	{
		java.util.Iterator<gm_rwinfo> i;
		assert L != null;
		for (i = L.iterator(); i.hasNext();)
		{
			gm_rwinfo rw = i.next();
			assert rw != null;
			if (rw.access_range != gm_range_type_t.GM_RANGE_LINEAR.getValue())
				return false;
		}
		return true;
	}

	public static boolean intersect_check_for_merge(java.util.HashMap<gm_symtab_entry, java.util.LinkedList<gm_rwinfo>> S1, java.util.HashMap<gm_symtab_entry, java.util.LinkedList<gm_rwinfo>> S2, java.util.HashMap<gm_symtab_entry, java.util.LinkedList<gm_rwinfo>> S1_reduce, boolean check_reduce)
	{
		java.util.Iterator<gm_symtab_entry, java.util.LinkedList<gm_rwinfo>> i;
		java.util.Iterator<gm_symtab_entry, java.util.LinkedList<gm_rwinfo>> j;
		for (i = S1.iterator(); i.hasNext();)
		{
			gm_symtab_entry e = i.next().getKey();
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created if it does not yet exist:
//ORIGINAL LINE: j = S2.find(e);
			j.copyFrom(S2.find(e));
			if (j.hasNext())
			{
				// found entry
				if (!e.getType().is_property()) // scala
				{
					if (e.getType().is_collection())
					{
						boolean isSeq1 = e.getType().is_sequential_collection();
						boolean isSeq2 = j.next().getKey().getType().is_sequential_collection();

						if (!(isSeq1 || isSeq2))
							return false;
					}
					return true;
				}
				else
				{
					// check S1, S2 is linearly accessed only.
					if (!GlobalMembersGm_merge_loops.is_linear_access_only(i.next().getValue()))
						return true;
					if (!GlobalMembersGm_merge_loops.is_linear_access_only(j.next().getValue()))
						return true;
					if (check_reduce) // if e is in the reduce-set,
					{
						if (S1_reduce.containsKey(e))
							return true;
					}
				}
			}
		}

		return false;
	}

	//----------------------------------------------------
	// example
	// (okay)
	//   Foreach(t:G.Nodes) {t.A = x+1;} 
	//   Foreach(q:G.Nodes) {Q.B = t.A +1 ;}  // okay to merge. linear access only for A.
	//
	// (not-okay#1)
	//   Foreach(t:G.Nodes) {t.A = x+1;} 
	//   Foreach(q:G.Nodes) {
	//     Foreach(r:q.Nbrs) {
	//       q.B += r.A @ q;}  // cannot merge t-loop and q-loop because of A is randomly accessed
	//   } }
	//
	// (not-okay#2)
	//   Foreach(t:G.Nodes){
	//     Foreach(r:t.Nbrs) 
	//       t.A += r.C +1 @ t;   // t.A is being reduced
	//   }
	//   Foreach(q:G.Nodes) {Q.B = t.A +1;}  // cannot merge t-loop and q-loop because of A.
	//----------------------------------------------------
	public static boolean gm_is_mergeable_loops(ast_foreach P, ast_foreach Q)
	{
		// check same source
		if (P.get_source().getSymInfo() != Q.get_source().getSymInfo())
			return false;

		// check same iteration type
		if (P.get_iter_type() != Q.get_iter_type())
			return false;

		// check same parallel type
		if (P.is_parallel() != Q.is_parallel())
			return false;

		// dependency check for loops.
		// linear access does not make dependency, unless being reduced.
		gm_rwinfo_sets P_SET = GlobalMembersGm_rw_analysis.get_rwinfo_sets(P);
		gm_rwinfo_sets Q_SET = GlobalMembersGm_rw_analysis.get_rwinfo_sets(Q);

//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy constructor call - this should be verified and a copy constructor should be created if it does not yet exist:
//ORIGINAL LINE: java.util.HashMap<gm_symtab_entry*, java.util.LinkedList<gm_rwinfo*>*>& P_R = P_SET->read_set;
		java.util.HashMap<gm_symtab_entry, java.util.LinkedList<gm_rwinfo>> P_R = new java.util.HashMap(P_SET.read_set);
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy constructor call - this should be verified and a copy constructor should be created if it does not yet exist:
//ORIGINAL LINE: java.util.HashMap<gm_symtab_entry*, java.util.LinkedList<gm_rwinfo*>*>& P_W = P_SET->write_set;
		java.util.HashMap<gm_symtab_entry, java.util.LinkedList<gm_rwinfo>> P_W = new java.util.HashMap(P_SET.write_set);
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy constructor call - this should be verified and a copy constructor should be created if it does not yet exist:
//ORIGINAL LINE: java.util.HashMap<gm_symtab_entry*, java.util.LinkedList<gm_rwinfo*>*>& Q_R = Q_SET->read_set;
		java.util.HashMap<gm_symtab_entry, java.util.LinkedList<gm_rwinfo>> Q_R = new java.util.HashMap(Q_SET.read_set);
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy constructor call - this should be verified and a copy constructor should be created if it does not yet exist:
//ORIGINAL LINE: java.util.HashMap<gm_symtab_entry*, java.util.LinkedList<gm_rwinfo*>*>& Q_W = Q_SET->write_set;
		java.util.HashMap<gm_symtab_entry, java.util.LinkedList<gm_rwinfo>> Q_W = new java.util.HashMap(Q_SET.write_set);
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy constructor call - this should be verified and a copy constructor should be created if it does not yet exist:
//ORIGINAL LINE: java.util.HashMap<gm_symtab_entry*, java.util.LinkedList<gm_rwinfo*>*>& P_M = P_SET->mutate_set;
		java.util.HashMap<gm_symtab_entry, java.util.LinkedList<gm_rwinfo>> P_M = new java.util.HashMap(P_SET.mutate_set);
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy constructor call - this should be verified and a copy constructor should be created if it does not yet exist:
//ORIGINAL LINE: java.util.HashMap<gm_symtab_entry*, java.util.LinkedList<gm_rwinfo*>*>& Q_M = Q_SET->mutate_set;
		java.util.HashMap<gm_symtab_entry, java.util.LinkedList<gm_rwinfo>> Q_M = new java.util.HashMap(Q_SET.mutate_set);

		gm_rwinfo_sets P_BODY_SET = GlobalMembersGm_rw_analysis.get_rwinfo_sets(P.get_body());
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy constructor call - this should be verified and a copy constructor should be created if it does not yet exist:
//ORIGINAL LINE: java.util.HashMap<gm_symtab_entry*, java.util.LinkedList<gm_rwinfo*>*>& P_D = P_BODY_SET->reduce_set;
		java.util.HashMap<gm_symtab_entry, java.util.LinkedList<gm_rwinfo>> P_D = new java.util.HashMap(P_BODY_SET.reduce_set);

		boolean b;
		//---------------------------------------------------
		// true dependency check. (P.W -> Q.R)
		//---------------------------------------------------
		b = GlobalMembersGm_merge_loops.intersect_check_for_merge(P_W, Q_R, P_D, true);
		if (b)
			return false;

		//---------------------------------------------------
		// anti dependency check. (P.R -> Q.W)
		//---------------------------------------------------
		b = GlobalMembersGm_merge_loops.intersect_check_for_merge(P_R, Q_W, P_D, false);
		if (b)
			return false;

		//---------------------------------------------------
		// anti dependency check. (P.W -> Q.W)
		//---------------------------------------------------
		b = GlobalMembersGm_merge_loops.intersect_check_for_merge(P_W, Q_W, P_D, true);
		if (b)
			return false;

		//---------------------------------------------------
		// mutate dependency checks.
		//---------------------------------------------------
		// 1. write mutate check  (P.M -> Q.W) || (P.W -> Q.M)
		b = GlobalMembersGm_merge_loops.intersect_check_for_merge(P_M, Q_W, P_D, false);
		if (b)
			return false;
		b = GlobalMembersGm_merge_loops.intersect_check_for_merge(P_W, Q_M, P_D, false);
		if (b)
			return false;

		// 2. read mutate check   (P.M -> Q.R) || (P.R -> Q.M)
		b = GlobalMembersGm_merge_loops.intersect_check_for_merge(P_M, Q_R, P_D, false);
		if (b)
			return false;
		b = GlobalMembersGm_merge_loops.intersect_check_for_merge(P_R, Q_M, P_D, false);
		if (b)
			return false;

		// 3. mutate mutate check (P.M -> Q.M)
		b = GlobalMembersGm_merge_loops.intersect_check_for_merge(P_M, Q_M, P_D, false);
		if (b)
			return false;

		return true;
	}

	public static void replace_iterator_sym(ast_foreach P, ast_foreach Q)
	{
		// Q's iterator -> P's iterator
		ast_id iter_p = P.get_iterator();
		ast_id iter_q = Q.get_iterator();
		gm_symtab_entry e_p = iter_p.getSymInfo();
		gm_symtab_entry e_q = iter_q.getSymInfo();

		// filter should be NULL. but I'm a paranoid
		assert Q.get_filter() == null;
		assert P.get_filter() == null;

		GlobalMembersGm_resolve_nc.gm_replace_symbol_entry(e_q, e_p, Q.get_body());

	}
}