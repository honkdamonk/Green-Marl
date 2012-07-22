package backend_gps;

import ast.AST_NODE_TYPE;
import ast.ast_foreach;
import ast.ast_sent;
import inc.GMTYPE_T;
import inc.GlobalMembersGm_defs;

import common.gm_apply;

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

//--------------------------------------------------------
// Find all instances of foreach loops in the procedue 
// and make a map of following
//     - (outer loop -> NULL) or
//     - (inner loop -> outer loop)
//
//     outer-loop: node-wise iteration
//     inner-loop: neighbor hood iteration
//--------------------------------------------------------
//  Example>
//     foreach(n1: G.Nodes) {
//     }
//     foreach (n2: G.Nodes) {
//        foreach(t: n2.Nbrs) {..}
//        foreach(r: n2.Nbrs) {...}
//     }
// ==>
//     (n1, NULL)
//     (n2, NULL)
//     (n2, t)
//     (n2, r)
//
//-------------------------------------------------
// [todo] what to do with loops nested more than 3 depths
//----------------------------------------------------

public class gps_opt_find_nested_loops_t extends gm_apply
{

	public gps_opt_find_nested_loops_t(java.util.HashMap<ast_foreach, ast_foreach> M)
	{
		this.MAP = new java.util.HashMap<ast_foreach, ast_foreach>(M);
		set_for_sent(true);
		set_separate_post_apply(true);
		depth = 0;
		outer_loop = null;
	}

	@Override
	public boolean apply(ast_sent s)
	{
		if (s.get_nodetype() == AST_NODE_TYPE.AST_FOREACH)
		{
			depth++;
			ast_foreach fe = (ast_foreach) s;
			if (depth == 1)
			{
				if (GlobalMembersGm_defs.gm_is_all_graph_node_iter_type(fe.get_iter_type()))
				{
					outer_loop = fe;
					MAP.put(fe, null);
				}
			}
			else if ((depth == 2) && (outer_loop != null))
			{
				GMTYPE_T iter = fe.get_iter_type();
				if (GlobalMembersGm_defs.gm_is_inout_nbr_node_iter_type(fe.get_iter_type()))
				{
					MAP.put(fe, outer_loop);
				}
			}
		}
		return true;
	}

	@Override
	public boolean apply2(ast_sent s)
	{
		if (s.get_nodetype() == AST_NODE_TYPE.AST_FOREACH)
		{
			depth--;
			if (depth == 0)
				outer_loop = null;
		}
		return true;
	}

	private int depth;
	private ast_foreach outer_loop;
	private java.util.HashMap<ast_foreach, ast_foreach> MAP;
}
/*
 void gm_gps_opt_find_nested_loops_test::process(ast_procdef* p)
 {
 std::map<ast_foreach*, ast_foreach*> MAP;
 gm_gps_find_nested_loops(p, MAP);

 std::map<ast_foreach*, ast_foreach*>::iterator I;
 for(I= MAP.begin(); I!=MAP.end(); I++)
 {
 ast_foreach* fe1 = I->first;
 ast_foreach* fe2 = I->second;
 if (fe2 == NULL) {
 printf("outer loop = %s\n", fe1->get_iterator()->get_genname());
 } else {
 printf("inner loop = %s (outer loop =%s)\n",
 fe1->get_iterator()->get_genname(),
 fe2->get_iterator()->get_genname());
 }
 }
 }
 */

