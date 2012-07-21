package backend_gps;

import ast.ast_foreach;
import backend_cpp.*;
import backend_giraph.*;
import common.*;
import frontend.*;
import inc.*;
import opt.*;
import tangible.*;

public class GlobalMembersGm_gps_new_check_edge_value
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

	//------------------------------------------------------------------------------------
	// Check things related to the edge property
	//
	// [1] Conditions 
	//   - Access to edge property is available only through edge variable that is defined
	//     inside the 2nd-level FE.
	//   - Edge variable, defined inside a 2nd-level FE, should be initialized as (2nd-level iterator).toEdge.
	//   - 2nd level iteration should use out-going edges only.
	//          
	//     Foreach(n: G.Nodes)  {
	//        Foreach(s: n.Nbrs) {
	//            Edge(G) e = s.ToEdge();
	//     }  }
	//
	// [2] Writing to Edge property 
	//   - Write to edge-property should be 'simple' (i.e. not conditional)
	//   - RHSs of edge-prop writing are not mapped into communication
	//   - RHSs of edge prop writing cannot condtain inner-loop scoped symbol
	//       Foreach(n: G.Nodes)  {
	//          Foreach(s: n.Nbrs)  {
	//            Edge(G) e = s.ToEdge();
	//            if (s.Y > 10) e.C = 10; // error
	//            e.A = e.B + n.Y;  // okay. n.Y or e.B is not transported, but locally used.
	//            e.B = s.Y; // error
	//       }  }
	//
	// [3] Reading from Edge property
	//    - Possible Edge-Prop Access Sequence
	//        - Sent : okay
	//        - Write : okay
	//        - Write -> sent : okay 
	//        - Send -> Write : okay
	//        - Write -> Send -> Write: okay
	//        - Send -> Write -> Send: Error
	//           ==> because message cannot hold two versions of edge property 
	//
	//      Foreach(n: G.Nodes) {
	//         Foreach(s: n.Nbrs) {
	//            Edge(g) e = s.toEdge();
	//            e.A = n.Y;                // okay.  A: write
	//            s.Z += e.A + e.B;         // okay   A: write-sent, B :sent
	//            e.B = n.Y+1;              // okay.  B: sent-write
	//            e.A = 0;                  // okay.  A: (write-)sent-write
	//            s.Z += e.B;               // Error, B: (Send-write-Send)
	//         }
	//      }
	//------------------------------------------------------------------------------------
	// Implementation
	//    - Inner loop maintains a map of edge-prop symbols
	//         <symbol, state>
	//    - Inner loop maintains a list of edge-prop writes
	//
	//
	// Additional Information creted
	//     GPS_MAP_EDGE_PROP_ACCESS :   <where:>foreach, <what:> map(symbol,int(state)), one of GPS_ENUM_EDGE_VALUE_xxx
	//     GPS_FLAG_EDGE_DEFINED_INNER: <where:>var symbol(edge type),<what:>if the varaible is defined inside inner loop
	//     GPS_FLAG_EDGE_DEFINING_INNTER: <where:>foreach, <what:>if this inner loop defines an edge variable
	//     GPS_LIST_EDGE_PROP_WRITE: <where:>foreach, <what:> (list of) assigns whose target is edge variables
	//     GPS_FLAG_EDGE_DEFINING_WRITING:<where:>assign, <what:>if this assignment is defining en edge (as inner.ToEdge())
	//------------------------------------------------------------------------------------

	public static final int SENDING = 1;
	public static final int WRITING = 2;

	// return: is_error
	public static boolean manage_edge_prop_access_state(ast_foreach fe, gm_symtab_entry e, int op)
	{
		assert (op == SENDING) || (op == WRITING);
//C++ TO JAVA CONVERTER TODO TASK: Java does not have an equivalent for pointers to value types:
//ORIGINAL LINE: int* curr_state = (int*) fe->find_info_map_value(GPS_MAP_EDGE_PROP_ACCESS, e);
		int curr_state = (Integer) fe.find_info_map_value(GlobalMembersGm_backend_gps.GPS_MAP_EDGE_PROP_ACCESS, e);

		// first access
		if (curr_state == null)
		{
//C++ TO JAVA CONVERTER TODO TASK: Java does not have an equivalent for pointers to value types:
//ORIGINAL LINE: int* new_state = new int();
			int new_state = new int();
			new_state = (op == SENDING) ? gm_gps_edge_access_t.GPS_ENUM_EDGE_VALUE_SENT : gm_gps_edge_access_t.GPS_ENUM_EDGE_VALUE_WRITE;

			fe.add_info_map_key_value(GlobalMembersGm_backend_gps.GPS_MAP_EDGE_PROP_ACCESS, e, new_state);
		}
		else
		{
			int curr_state_val = curr_state;
			switch (curr_state_val)
			{
				case GPS_ENUM_EDGE_VALUE_ERROR: //already error
					return false;

				case GPS_ENUM_EDGE_VALUE_WRITE:
					if (op == SENDING)
						curr_state = gm_gps_edge_access_t.GPS_ENUM_EDGE_VALUE_WRITE_SENT.getValue();
					return false; // no error

				case GPS_ENUM_EDGE_VALUE_SENT:
					if (op == WRITING)
						curr_state = gm_gps_edge_access_t.GPS_ENUM_EDGE_VALUE_SENT_WRITE.getValue();
					return false; // no error

				case GPS_ENUM_EDGE_VALUE_WRITE_SENT:
					if (op == WRITING)
						curr_state = gm_gps_edge_access_t.GPS_ENUM_EDGE_VALUE_SENT_WRITE.getValue();
					return false;

				case GPS_ENUM_EDGE_VALUE_SENT_WRITE:
					if (op == SENDING) // sending two versions!
					{
						curr_state = gm_gps_edge_access_t.GPS_ENUM_EDGE_VALUE_ERROR.getValue();
						return true; // ERROR
					}
					else
						return false;
				default:
					assert false;
					break;
			}
		}
		return false;
	}
}