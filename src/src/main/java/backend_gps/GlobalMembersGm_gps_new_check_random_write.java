package backend_gps;

import ast.AST_NODE_TYPE;
import ast.ast_node;
import frontend.gm_symtab_entry;

public class GlobalMembersGm_gps_new_check_random_write
{
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define TO_STR(X) #X
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define DEF_STRING(X) static const char *X = "X"
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define GM_COMPILE_STEP(CLASS, DESC) class CLASS : public gm_compile_step { private: CLASS() {set_description(DESC);}public: virtual void process(ast_procdef*p); virtual gm_compile_step* get_instance(){return new CLASS();} static gm_compile_step* get_factory(){return new CLASS();} };
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define GM_COMPILE_STEP_FACTORY(CLASS) CLASS::get_factory()
	//-----------------------------------------------------------------
	// Check random access
	//-----------------------------------------------------------------
	// Rules:
	//    - Reading of node properties via random node varaible is not allowed.
	//    - Writing of node properties via random node variable is allowed if
	//         - The node variable is out-scoped (temporary)
	//         - The node variable is assigned only once
	//         - The random write is out-scoped (temporary)
	//         - The random write is not conditioned 
	//
	// Example>
	//
	// Node(G) root;
	// root.X = 3;      // not okay 
	//
	// Foreach(n: G.Nodes) {
	//   Node(G) y = root;
	//   root.X = n.A;  // not okay
	//   y.X = n.A;     // okay
	// }
	//
	// Foreach(n:G.Nodes) {
	//   Foreach(t:n.Nbrs) {
	//      Node(G) z = root;
	//      root.X = t.A;      // not okay 
	//      z.X = t.A;         // not okay 
	//   }
	// }
	//
	// Foreach(n: G.Nodes) {
	//   Node(G) y = root;
	//   y.X = t.A;
	//   y = root2;            // not okay
	// }
	//
	// Foreach(n:G.Nodes) {
	//   Node(G) y = root;
	//   y.B = 0;
	//   if (n.A > 0) {
	//       Node(G) z = y;
	//       y.C = 1;       // not okay 
	//       z.C = 1;       // okay
	//   }
	// }
	//
	// [Todo: Multiple definitions? ]
	// {
	//   Node(G) y1= root;
	//   Node(G) y2= root;
	//   y1.X = 0;
	//   y2.X = 1;  // what would be the value of root.X after word?
	// }
	//
	// Constructed Information
	//    FLAG_SENT_BLOCK_FOR_RANDOM_WRITE_ASSIGN: 
	//       (to => assign_sttement, what: the sentblock that contains random write for this assign statement)
	//    FLAG_RANDWM_WRITE_SYMBOL_FOR_SB
	//       (to => sent_block, what: set of symbols that are used as ramdom-write driver in the sent-block) 
	//-----------------------------------------------------------------
	public static boolean check_if_met_conditional_before(ast_node s, gm_symtab_entry symbol)
	{
		while (true)
		{
			assert s != null;
			if ((s.get_nodetype() == AST_NODE_TYPE.AST_WHILE) || (s.get_nodetype() == AST_NODE_TYPE.AST_IF))
			{
				return true;
			}
			if (s.has_symtab())
			{
				if (s.get_symtab_var().is_entry_in_the_tab(symbol))
				{
					return false;
				}
			}

			s = (ast_node) s.get_parent();
		}
		return false;
	}
}