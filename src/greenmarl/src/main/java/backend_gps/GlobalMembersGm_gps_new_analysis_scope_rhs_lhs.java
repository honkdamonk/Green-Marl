package backend_gps;

import ast.ast_procdef;

public class GlobalMembersGm_gps_new_analysis_scope_rhs_lhs
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

	//-----------------------------------------------------------------------------------------------------------------------
	// Analysis the 'scope' of each RHS, and LHS
	//-----------------------------------------------------------------------------------------------------------------------
	//     * Check the scope of each expression: GLOBAL, OUT, IN, RANDOM, EDGE
	//     * Check the destination of each assignment: GLOBAL, OUT, IN, RANDOM, EDGE
	//
	//
	//  Int x;                          // global   scope
	//  Int x2;
	//
	//  Foreach(s: G.Nodes) {           // outer    loop
	//      Int y;               
	//      Int y2;               
	//
	//      If (s.A + x > 0)            // (s.A + x > 0) ==> EXPR_OUT
	//          x2 += s.B;              // assignment: PREFIX_COND_OUT, lhs: GLOBAL, rhs: OUT,  
	//
	//      Foreach(t: s.Nbrs) {        // inner    loop
	//          Int z;
	//          Int z2;
	//      }
	//  }
	//-----------------------------------------------------------------------------------------------------------------------
	public static int get_more_restricted_scope(int i, int j)
	{
		//GPS_NEW_SCOPE_GLOBAL = 0,
		//GPS_NEW_SCOPE_OUT ,
		//GPS_NEW_SCOPE_EDGE,
		//GPS_NEW_SCOPE_IN ,
		//GPS_NEW_SCOPE_RANDOM,  <-- most restricted
		return Math.max(i, j);
	}

	public static void gm_gps_do_new_analysis_rhs_lhs(ast_procdef proc)
	{
		gm_gps_new_analysis_rhs_lhs_t T = new gm_gps_new_analysis_rhs_lhs_t();
		proc.traverse_post(T);
	}
}