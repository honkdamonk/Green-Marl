package backend_gps;

import java.util.LinkedList;

import ast.AST_NODE_TYPE;
import ast.ast_foreach;
import ast.ast_if;
import ast.ast_node;
import ast.ast_sent;
import ast.ast_sentblock;
import frontend.GlobalMembersGm_rw_analysis_check2;
import frontend.gm_range_type_t;
import frontend.gm_rwinfo;
import frontend.gm_symtab;
import frontend.gm_symtab_entry;
import inc.GlobalMembersGm_defs;

import common.GlobalMembersGm_new_sents_after_tc;
import common.GlobalMembersGm_resolve_nc;
import common.GlobalMembersGm_transform_helper;

public class GlobalMembersGm_gps_opt_split_loops_for_flipping
{
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define TO_STR(X) #X
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define DEF_STRING(X) static const char *X = "X"
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define GM_COMPILE_STEP(CLASS, DESC) class CLASS : public gm_compile_step { private: CLASS() {set_description(DESC);}public: virtual void process(ast_procdef*p); virtual gm_compile_step* get_instance(){return new CLASS();} static gm_compile_step* get_factory(){return new CLASS();} };
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define GM_COMPILE_STEP_FACTORY(CLASS) CLASS::get_factory()
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define AUX_INFO(X,Y) "X"":""Y"
	///#define GM_BLTIN_MUTATE_GROW 1
	///#define GM_BLTIN_MUTATE_SHRINK 2
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define GM_BLTIN_FLAG_TRUE true

	// defined in opt_find_nested_foreach_loops.cc
	//extern void gm_gps_find_double_nested_loops(ast_node p, java.util.HashMap<ast_foreach, ast_foreach> MAP);

	//----------------------------------------------------
	// N_P<Int>(G) _tmp_S;
	// Foreach (n: G.Nodes) {
	//   n._tmp_S = 0;
	//   Foreach( t: n.InNbrs) {
	//       n._tmp_S += t.A;
	//   }
	//   n.B = n._tmp_S * alpha + beta;
	// }
	// ===>  
	// Foreach (n: G.Nodes) {
	//   n._tmp_S = 0;
	// }
	// Foreach (n: G.Nodes) {
	//   Foreach( t: n.InNbrs) {
	//       n._tmp_S += t.A;
	//   }
	// }
	// Foreach (n: G.Nodes) {
	//   n.B = n._tmp_S * alpha + beta;
	// }
	//----------------------------------------------------
	//

	//---------------------------------------
	//
	// Foreach(n.) {
	//    ....  (A)
	//
	//    <if> {  // no variables defined in between
	//       Foreach(t) {
	//         n.X = ...
	//       }
	//    }
	//
	//    .... (B)
	// }
	//---------------------------------------
	public static void filter_target_loops(java.util.HashMap<ast_foreach, ast_foreach> SRC, java.util.HashSet<ast_foreach> SET)
	{
		for (ast_foreach in : SRC.keySet())
		{
			ast_foreach out = SRC.get(in);
			if (out == null)
				continue;

			gm_symtab_entry out_iter = out.get_iterator().getSymInfo();

			boolean is_target = false;
			// check if inner loop requires flipping
			java.util.HashMap<gm_symtab_entry, java.util.LinkedList<gm_rwinfo>> WMAP = GlobalMembersGm_rw_analysis_check2.gm_get_write_set(in.get_body());
			for (gm_symtab_entry e : WMAP.keySet())
			{
				java.util.LinkedList<gm_rwinfo> LIST = WMAP.get(e);
				boolean is_field = e.getType().is_property();
				for (gm_rwinfo info : LIST)
				{
					if (is_field && (info.driver == null) && (info.access_range == gm_range_type_t.GM_RANGE_RANDOM))
					{
						is_target = true;
						continue;
					}
					else if (info.driver == null)
					{
						continue;
					}

					if (info.driver != out_iter)
					{
						is_target = false;
						break;
					}
					if (info.driver == out_iter)
					{
						is_target = true;
						continue;
					}
				}
			}

			java.util.HashMap<gm_symtab_entry, LinkedList<gm_rwinfo>> DMAP = GlobalMembersGm_rw_analysis_check2.gm_get_reduce_set(in.get_body());
			for (gm_symtab_entry e : DMAP.keySet()) {
				LinkedList<gm_rwinfo> LIST = DMAP.get(e);
				for (gm_rwinfo info : LIST)
				{
					if ((info.driver == null) && (info.access_range == gm_range_type_t.GM_RANGE_RANDOM))
					{
						continue;
					}
					else if (info.driver == null)
						continue;

					if (info.driver != out_iter)
					{
						is_target = false;
						break;
					}
					if (info.driver == out_iter)
					{
						is_target = true;
						continue;
					}
				}
			}

			//printf(" loop for %s -> %c \n", in->get_iterator()->get_genname(), is_target?'Y':'N');
			if (!is_target)
				continue;

			boolean meet_if = false;
			ast_node current = in;
			//printf("current = %p %s\n", current, gm_get_nodetype_string(in->get_nodetype()));
			// move up until meet the outer loop
			while (true)
			{
				ast_node parent = current.get_parent();
				assert parent != null;
				//printf(" parent = %p %s\n", parent, gm_get_nodetype_string(parent->get_nodetype()));
				if (parent.get_nodetype() == AST_NODE_TYPE.AST_IF)
				{
					if (meet_if)
					{
						is_target = false;
						break;
					}
					else if (((ast_if) parent).get_else() == null)
					{
						meet_if = true;
					}
					else
					{
						is_target = false;
						break;
					}
				}
				else if (parent.get_nodetype() == AST_NODE_TYPE.AST_SENTBLOCK)
				{
					// todo: this is a little bit to restrictive.
					// we can relax this by changing more scalars into node properties.
					ast_sentblock sb = (ast_sentblock) parent;

					if (sb.get_symtab_field().get_num_symbols() > 0)
					{
						is_target = false;
						break;
					}
					/*
					 if ((sb->get_symtab_field()->get_num_symbols() > 0) ||
					 (sb->get_symtab_var()->get_num_symbols() > 0)) {
					 */
				}
				else if (parent.get_nodetype() == AST_NODE_TYPE.AST_FOREACH)
				{
					ast_foreach fe = (ast_foreach) parent;
					if (fe == out)
						break;
					else
					{
						is_target = false;
						break;
					}
				}
				else
				{
					//printf("parent  = %s!!!\n", gm_get_nodetype_string(parent->get_nodetype()));
					is_target = false;
					break;
				}
				current = parent;
			}

			//printf(" loop for %s -> %c \n", in->get_iterator()->get_genname(), is_target?'Y':'N');
			if (!is_target)
				continue;
			SET.add(in);
		}
	}

	//--------------------------------------------
	// example>
	//    FE(n:) 
	//    {
	//        A; 
	//        {
	//              B;
	//              If() 
	//              {
	//                   C;
	//                   Foreach(t:) {
	//                   }
	//                   D;
	//              }
	//              E;
	//         }
	//         F;
	//    }
	// =====>
	//    FE(n:) 
	//    {
	//        A; 
	//        {
	//              B;
	//              If() 
	//              {
	//                   C;
	//    }   }     }
	//
	//    FE(n1:) 
	//    {  {      If() {
	//                   Foreach(t:) {
	//                   }
	//              }
	//    }   }
	//
	//    FE(n2:) 
	//    {  {      If() 
	//              {
	//                  D;
	//              }
	//              E;
	//        }
	//        F;
	//    }   
	//------------------------------------------        
	public static ast_node reconstruct_old_new(java.util.LinkedList<ast_node> frame, java.util.HashMap<ast_sentblock, java.util.LinkedList<ast_sent>> siblings, boolean is_old)
	{
		ast_node last = null;

		// reconstruct hierarchy
		// inmost --> outmost
		java.util.Iterator<ast_node> I;
		for (I = frame.iterator(); I.hasNext();)
		{
			 GlobalMembersGm_gps_opt_split_loops_for_flipping.reconstruct_old_new_main(I.next(), siblings, is_old, last);
		}

		return last;
	}
	public static void ensure_no_dependency_via_scala(java.util.LinkedList<ast_node> frame, java.util.HashMap<ast_sentblock, java.util.LinkedList<ast_sent>> elder, java.util.HashMap<ast_sentblock, java.util.LinkedList<ast_sent>> younger)
	{
		// create prev/next
		// create set of scalar
		java.util.HashSet<gm_symtab_entry> PREVS = new java.util.HashSet<gm_symtab_entry>(); // symbols used in prev
		java.util.HashSet<gm_symtab_entry> NEXTS = new java.util.HashSet<gm_symtab_entry>(); // symbols used in next
		java.util.HashSet<gm_symtab_entry> ALL = new java.util.HashSet<gm_symtab_entry>(); // every scalar symbols
		java.util.Iterator<ast_node> I;
		for (I = frame.iterator(); I.hasNext();)
		{
			if ((I.next()).get_nodetype() == AST_NODE_TYPE.AST_IF)
			{
				// continue
			}
			else if ((I.next()).get_nodetype() == AST_NODE_TYPE.AST_SENTBLOCK)
			{
				ast_sentblock sb = (ast_sentblock)(I.next());
				java.util.LinkedList<ast_sent> OLD = elder.get(sb);
				java.util.LinkedList<ast_sent> NEW = younger.get(sb);
				java.util.Iterator<ast_sent> J;
				for (J = OLD.iterator(); J.hasNext();)
				{
					GlobalMembersGm_gps_opt_split_loops_for_flipping.add_scalar_rw(J.next(), PREVS);
				}

				for (J = NEW.iterator(); J.hasNext();)
				{
					GlobalMembersGm_gps_opt_split_loops_for_flipping.add_scalar_rw(J.next(), NEXTS);
				}

				java.util.HashSet<gm_symtab_entry> E = sb.get_symtab_var().get_entries();
				java.util.Iterator<gm_symtab_entry> K;
				for (K = E.iterator(); K.hasNext();)
				{
					ALL.add(K.next());
				}
			}
			else
			{
				assert false;
			}
		}

		// check if any entry is both used PREV and NEXT
		java.util.Iterator<gm_symtab_entry> K;
		for (K = ALL.iterator(); K.hasNext();)
		{
			gm_symtab_entry e = K.next();
			if ((PREVS.contains(e)) && (NEXTS.contains(e)))
			{
				assert false;
				// [todo] replace these symbols with temporary node_prop
				// re-do rw-analysis afterward.
			}
			else if (PREVS.contains(e))
				e.add_info_int(USED_BY_WHO, USED_BY_OLDER);
			else if (NEXTS.contains(e))
				e.add_info_int(USED_BY_WHO, USED_BY_YOUNGER);
		}
	}

	public static void split_the_loop(ast_foreach in)
	{
		ast_foreach out = null;

		//----------------------------------------------------------------
		// find the current frame structure
		// example>
		//    Foreach() <--- out
		//    {                        
		//       A1; A2;
		//       {  
		//          B1; B2;
		//          If (...) {
		//              C1; C2; 
		//              Froeach() { <--- in
		//              }
		//              C3; C4;
		//          }
		//          B3; B4;
		//       }
		//       A3; A4;
		//   }
		//
		//   frame: C -> If -> B -> A
		//   older[A] = {A1; A2;}
		//   older[B] = {B1; B2;}
		//   older[C] = {C1; C2;}
		//   younger[A] = {A3; A4;}
		//   younger[B] = {B3; B4;}
		//   younger[C] = {C3; C4;}
		//----------------------------------------------------------------

		java.util.LinkedList<ast_node> frame = new java.util.LinkedList<ast_node>();
		java.util.HashMap<ast_sentblock, java.util.LinkedList<ast_sent>> older_siblings = new java.util.HashMap<ast_sentblock, java.util.LinkedList<ast_sent>>();
		java.util.HashMap<ast_sentblock, java.util.LinkedList<ast_sent>> younger_siblings = new java.util.HashMap<ast_sentblock, java.util.LinkedList<ast_sent>>();

		ast_node current = in;
		boolean need_young = false;
		boolean need_old = false;
		while (true)
		{
			ast_node node = current.get_parent();
			assert node != null;
			if (node.get_nodetype() == AST_NODE_TYPE.AST_IF)
			{
				frame.addLast(node);
				current = node;
				continue;
			}
			else if (node.get_nodetype() == AST_NODE_TYPE.AST_FOREACH)
			{
				out = (ast_foreach) node;
				break;
			}
			else if (node.get_nodetype() == AST_NODE_TYPE.AST_SENTBLOCK)
			{
				ast_sentblock sb = (ast_sentblock) node;
				frame.addLast(sb);

				// add elder and younger brothers
				java.util.LinkedList<ast_sent> All = sb.get_sents();
				java.util.LinkedList<ast_sent> OLD = new java.util.LinkedList<ast_sent>();
				java.util.LinkedList<ast_sent> YOUNG = new java.util.LinkedList<ast_sent>();
				java.util.Iterator<ast_sent> I;
				boolean older = true;
				for (I = All.iterator(); I.hasNext();)
				{
					ast_sent s = I.next();
					if (s == current)
					{
						older = false;
						continue;
					}
					if (older)
					{
						OLD.addLast(s);
					}
					else
					{
						YOUNG.addLast(s);
					}
				}

				All.clear();
				All.addLast((ast_sent) current);

				assert older == false;
				older_siblings.put(sb, OLD);
				younger_siblings.put(sb, YOUNG);
				need_young = need_young || (YOUNG.size() > 0);
				need_old = need_old || (OLD.size() > 0);
				current = node;

				continue; // go up one level
			}
			else
			{
				assert false;
			}
		}
		assert out != null;

		assert GlobalMembersGm_defs.gm_is_iteration_on_all_graph(out.get_iter_type());
		GlobalMembersGm_transform_helper.gm_make_it_belong_to_sentblock(out);
		assert out.get_parent().get_nodetype() == AST_NODE_TYPE.AST_SENTBLOCK;

		// check if there are dependencies via scalar variable 
		// also mark each scalar symbols whether they are used by elder siblings or younger siblings
		GlobalMembersGm_gps_opt_split_loops_for_flipping.ensure_no_dependency_via_scala(frame, older_siblings, younger_siblings);

		//----------------------------------------------------------------
		// reconstruct program
		//----------------------------------------------------------------
		java.util.Iterator<ast_node> I;
		if (need_old)
		{
			ast_node old = GlobalMembersGm_gps_opt_split_loops_for_flipping.reconstruct_old_new(frame, older_siblings, true);
			assert old != null;

			ast_foreach old_loop = GlobalMembersGm_new_sents_after_tc.gm_new_foreach_after_tc(out.get_iterator().copy(false), out.get_source().copy(true), (ast_sent) old, out.get_iter_type());
			// replace iterator id
			GlobalMembersGm_resolve_nc.gm_replace_symbol_entry(out.get_iterator().getSymInfo(), old_loop.get_iterator().getSymInfo(), old);

			GlobalMembersGm_transform_helper.gm_add_sent_before(out, old_loop);

		}

		if (need_young)
		{
			ast_node old = GlobalMembersGm_gps_opt_split_loops_for_flipping.reconstruct_old_new(frame, younger_siblings, false);
			assert old != null;
			ast_foreach new_loop = GlobalMembersGm_new_sents_after_tc.gm_new_foreach_after_tc(out.get_iterator().copy(false), out.get_source().copy(true), (ast_sent) old, out.get_iter_type());
			// replace iterator id
			GlobalMembersGm_resolve_nc.gm_replace_symbol_entry(out.get_iterator().getSymInfo(), new_loop.get_iterator().getSymInfo(), old);

			GlobalMembersGm_transform_helper.gm_add_sent_after(out, new_loop);

		}
	}

	public static void add_scalar_rw(ast_sent s, java.util.HashSet<gm_symtab_entry> TARGET)
	{
		java.util.HashMap<gm_symtab_entry, java.util.LinkedList<gm_rwinfo>> W = GlobalMembersGm_rw_analysis_check2.gm_get_write_set(s);
		java.util.HashMap<gm_symtab_entry, java.util.LinkedList<gm_rwinfo>> R = GlobalMembersGm_rw_analysis_check2.gm_get_write_set(s);
		for (gm_symtab_entry e : W.keySet())
		{
			if (!e.getType().is_property())
			{
				TARGET.add(e);
			}
		}
		for (gm_symtab_entry e : R.keySet())
		{
			if (!e.getType().is_property())
			{
				TARGET.add(e);
			}
		}
	}

	public static String USED_BY_WHO = "gm_split_used_by_who";
	public static final int USED_BY_OLDER = 1;
	public static final int USED_BY_YOUNGER = 2;

	public static void reconstruct_old_new_main(ast_node n, java.util.HashMap<ast_sentblock, java.util.LinkedList<ast_sent>> siblings, boolean is_old, ast_node last)
	{

		if (n.get_nodetype() == AST_NODE_TYPE.AST_IF)
		{
			if (last == null) // can ignore this if loop
				return;

			ast_if iff = (ast_if)(n);
			ast_if new_if = ast_if.new_if(iff.get_cond().copy(true), (ast_sent) last, null);
			new_if.set_line(iff.get_line());
			new_if.set_col(iff.get_col());
			last = new_if;

		}
		else if ((n).get_nodetype() == AST_NODE_TYPE.AST_SENTBLOCK)
		{
			ast_sentblock sb_org = (ast_sentblock)(n);
			ast_sentblock sb = ast_sentblock.new_sentblock();
			java.util.LinkedList<ast_sent> SIB = siblings.get(sb_org);
			java.util.Iterator<ast_sent> J;
			sb.set_line(sb_org.get_line());
			sb.set_col(sb_org.get_col());

			if (is_old)
			{
				for (J = SIB.iterator(); J.hasNext();)
				{
					sb.add_sent(J.next());
				}
				if (last != null)
					sb.add_sent((ast_sent) last);
			}
			else
			{
				if (last != null)
					sb.add_sent((ast_sent) last);

				for (J = SIB.iterator(); J.hasNext();)
				{
					sb.add_sent(J.next());
				}
			}

			last = sb;

			// move scalar symbols
			gm_symtab old_tab = sb_org.get_symtab_var();
			gm_symtab new_tab = sb.get_symtab_var();
			java.util.HashSet<gm_symtab_entry> entries = old_tab.get_entries();
			java.util.Iterator<gm_symtab_entry> E;
			java.util.HashSet<gm_symtab_entry> T = new java.util.HashSet<gm_symtab_entry>();
			for (E = entries.iterator(); E.hasNext();)
			{
				gm_symtab_entry e = E.next();
				int used_by = e.find_info_int(USED_BY_WHO);
				if ((used_by == USED_BY_OLDER) && (is_old))
				{
					T.add(e);
				}
				else if ((used_by == USED_BY_YOUNGER) && (!is_old))
				{
					T.add(e);
				}
			}
			for (E = T.iterator(); E.hasNext();)
			{
				gm_symtab_entry e = E.next();
				old_tab.remove_entry_in_the_tab(e);
				new_tab.add_symbol(e);
			}
		}
	}
}