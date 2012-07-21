public class GlobalMembersGm_new_typecheck_step4
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

	//----------------------------------------------------------------
	// Type-check Step 4: 
	//    Resolve the size of INF types from LHS
	//    Example)
	//       Int A = +INF; // +INF must be Integer infinity.
	//
	//  Also NIL type is resolved as well.
	//      NIL UNKNOWN => NIL NODE or
	//                     NIL EDGE
	//----------------------------------------------------------------

	// defined in common/gm_resolve_inf_size.cc
	//extern boolean gm_resolve_size_of_inf_expr(ast_expr e, int dest_type);
}