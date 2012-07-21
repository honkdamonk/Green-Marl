package backend_cpp;

import ast.AST_NODE_TYPE;
import ast.ast_bfs;
import ast.ast_expr;
import ast.ast_expr_builtin;
import ast.ast_extra_info_set;
import ast.ast_id;
import ast.ast_procdef;
import ast.ast_sent;
import backend_giraph.*;
import common.*;
import frontend.*;
import inc.*;
import opt.*;
import tangible.*;

//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define TO_STR(X) #X
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define DEF_STRING(X) static const char *X = "X"
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP(CLASS, DESC) class CLASS : public gm_compile_step { private: CLASS() {set_description(DESC);}public: virtual void process(ast_procdef*p); virtual gm_compile_step* get_instance(){return new CLASS();} static gm_compile_step* get_factory(){return new CLASS();} };
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP_FACTORY(CLASS) CLASS::get_factory()

//---------------------------------------------------------------
// Checking for BFS or DFS
//---------------------------------------------------------------
// (1) Check BFS-MAIN
//   Find every bfs in the procedure
//   - save sym entries of variables that are used inside each bfs
//   - give a name to each bfs call-site
// (2) Check BFS Built-In
//   For every bfs
//   - save sym entries of collections that are used inside each bfs
//---------------------------------------------------------------
public class check_bfs_main_t extends gm_apply
{
	public check_bfs_main_t(ast_procdef p)
	{
		this.current_bfs = null;
		this.proc = p;
		this.has_bfs = false;
		this.in_bfs = false;
		set_for_sent(true);
		set_for_expr(true);
		set_separate_post_apply(true);
	}

	public final void process_rwinfo(java.util.HashMap<gm_symtab_entry, java.util.LinkedList<gm_rwinfo>> MAP, java.util.HashSet<Object > SET)
	{
		java.util.Iterator<gm_symtab_entry, java.util.LinkedList<gm_rwinfo>> I;
		for (I = MAP.iterator(); I.hasNext();)
		{
			gm_symtab_entry e = I.next().getKey();
			SET.add(e);

			java.util.LinkedList<gm_rwinfo> use = I.next().getValue();
			(assert(use != null));
			java.util.Iterator<gm_rwinfo> K;
			for (K = use.iterator(); K.hasNext();)
			{
				gm_symtab_entry driver = (K.next()).driver;
				if (driver != null)
				{
					SET.add(driver);
					ast_id g = driver.getType().get_target_graph_id();
					ast_id c = driver.getType().get_target_collection_id();
					if (g != null)
						SET.add(g.getSymInfo());
					if (c != null)
						SET.add(c.getSymInfo());
				}
			}
		}
	}

	public final boolean apply(ast_sent s)
	{
		if (s.get_nodetype() == AST_NODE_TYPE.AST_BFS)
		{
			ast_extra_info_set syms = new ast_extra_info_set();
			java.util.HashSet<Object > S = syms.get_set();

			// insert graph symbol at the first
			gm_symtab_entry graph = ((ast_bfs) s).get_root().getTypeInfo().get_target_graph_sym();

			S.add(graph);

			// are symbols that are read/writen inside bfs
			gm_rwinfo_sets RWINFO = GlobalMembersGm_rw_analysis.gm_get_rwinfo_sets(s);
			assert RWINFO != null;

//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy constructor call - this should be verified and a copy constructor should be created if it does not yet exist:
//ORIGINAL LINE: java.util.HashMap<gm_symtab_entry*, java.util.LinkedList<gm_rwinfo*>*>& RS = RWINFO->read_set;
			java.util.HashMap<gm_symtab_entry, java.util.LinkedList<gm_rwinfo>> RS = new java.util.HashMap(RWINFO.read_set);
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy constructor call - this should be verified and a copy constructor should be created if it does not yet exist:
//ORIGINAL LINE: java.util.HashMap<gm_symtab_entry*, java.util.LinkedList<gm_rwinfo*>*>& WS = RWINFO->write_set;
			java.util.HashMap<gm_symtab_entry, java.util.LinkedList<gm_rwinfo>> WS = new java.util.HashMap(RWINFO.write_set);
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy constructor call - this should be verified and a copy constructor should be created if it does not yet exist:
//ORIGINAL LINE: java.util.HashMap<gm_symtab_entry*, java.util.LinkedList<gm_rwinfo*>*>& DS = RWINFO->reduce_set;
			java.util.HashMap<gm_symtab_entry, java.util.LinkedList<gm_rwinfo>> DS = new java.util.HashMap(RWINFO.reduce_set);

			process_rwinfo(RS, S);
			process_rwinfo(WS, S);
			process_rwinfo(DS, S);

			s.add_info(GlobalMembersGm_backend_cpp.CPPBE_INFO_BFS_SYMBOLS, syms);
			has_bfs = true;
			ast_bfs bfs = (ast_bfs) s;

			String temp = new String(new char[1024]);
			String.format(temp, "%s", proc.get_procname().get_genname());
			String suffix = bfs.is_bfs() ? "_bfs" : "_dfs";
//C++ TO JAVA CONVERTER TODO TASK: Java does not have an equivalent for pointers to value types:
//ORIGINAL LINE: sbyte* c = FE.voca_temp_name_and_add(temp, suffix);
			byte c = GlobalMembersGm_main.FE.voca_temp_name_and_add(temp, suffix);

			s.add_info_string(GlobalMembersGm_backend_cpp.CPPBE_INFO_BFS_NAME, c);

			bfs_lists.addLast(s);

			assert in_bfs == false;
			in_bfs = true;
			current_bfs = (ast_bfs) s;
		}

		return true;
	}

	// [XXX]: should be merged with 'improved RW' analysis (to be done)
	public final boolean apply(ast_expr e)
	{
		if (!in_bfs)
			return true;
		if (e.is_builtin())
		{
			ast_expr_builtin bin = (ast_expr_builtin) e;
			ast_id driver = bin.get_driver();
			if (driver != null)
			{

				java.util.HashSet<Object > SET = ((ast_extra_info_set) current_bfs.find_info(GlobalMembersGm_backend_cpp.CPPBE_INFO_BFS_SYMBOLS)).get_set();
				SET.add(driver.getSymInfo());
			}
		}
		return true;
	}

	public final boolean apply2(ast_sent s)
	{
		if (s.get_nodetype() == AST_NODE_TYPE.AST_BFS)
		{
			in_bfs = false;
		}
		return true;
	}

	public ast_bfs current_bfs;
	public ast_procdef proc;
	public java.util.LinkedList<ast_sent> bfs_lists = new java.util.LinkedList<ast_sent>();
	public boolean has_bfs;
	public boolean in_bfs;
}