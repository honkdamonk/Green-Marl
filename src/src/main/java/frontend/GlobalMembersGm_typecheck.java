package frontend;

import inc.GMTYPE_T;

public class GlobalMembersGm_typecheck
{
	public static final boolean GM_READ_AVAILABLE = true;
	public static final boolean GM_READ_NOT_AVAILABLE = false;
	public static final boolean GM_WRITE_AVAILABLE = true;
	public static final boolean GM_WRITE_NOT_AVAILABLE = false;

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

	//----------------------------------------------------------------------------------------------------------------
	// Utility functions (Type summary)
	//----------------------------------------------------------------------------------------------------------------
	// check the byte size of two numeric type
	public static int gm_compare_numeric_type_size(GMTYPE_T t1, GMTYPE_T t2)
	{
		// GMTYPE_... is defined as small to larger
		return t1.subtract(t2); // +:t1 > t2 , 0:t2==t2, -:t1 < t2
	}
	// check the size (in Bytes) of two numeric types 
	public static GMTYPE_T gm_get_larger_type(GMTYPE_T t1, GMTYPE_T t2)
	{
		if (GlobalMembersGm_typecheck.gm_compare_numeric_type_size(t1, t2) > 0)
			return t1;
		else
			return t2;
	}

	// determine resulting type of numeric operation A (+,-,*,/) B 
	public static GMTYPE_T gm_determine_result_type(GMTYPE_T t1, GMTYPE_T t2)
	{
		// assumption. t1/t2 is compatible
		if (t1 == t2)
			return t1;
		else if (t1.is_inf_type())
			return t1;
		else if (t2.is_inf_type())
			return t2;
		else if (t1.is_numeric_type())
		{
			if (t1.is_float_type() == t2.is_float_type())
				return GlobalMembersGm_typecheck.gm_get_larger_type(t1, t2);
			else if (t1.is_float_type())
				return t1;
			else
				return t2;
		}
		else if (t1.is_iter_type())
			return t2;
		else if (t1.is_iter_type())
			return t1;
		else
		{
			assert false;
			return GMTYPE_T.GMTYPE_INVALID;
		}
	}

	public static boolean gm_check_compatible_types(GMTYPE_T t1, GMTYPE_T t2, gm_type_compatible_t for_what)
	{
		if (t1 == t2)
			return true;

		//----------------------------------------------------------
		// ASSUMTION
		// let t1 be the 'smaller' type (ordering by GM_XXX_TYPE enumeration)
		// GRAPH -> PROP -> NODE-EDGE/ITER -> NUMERIC -> BOOL -> INF  (see gm_frontend_api.h)
		//----------------------------------------------------------
		if (t2.isSmallerThan(t1))
		{
			GMTYPE_T t3;
			t3 = t1;
			t1 = t2;
			t2 = t3;
		}

		if (t1.is_node_compatible_type())
		{
			if (for_what == gm_type_compatible_t.FOR_BOP)
				return false;
			else
				return t2.is_node_compatible_type();
		}

		if (t1.is_edge_compatible_type())
		{
			if (for_what == gm_type_compatible_t.FOR_BOP)
				return false;
			else
				return t2.is_edge_compatible_type();
		}

		if (t1.is_numeric_type())
		{
			if (for_what == gm_type_compatible_t.FOR_BOP)
				return t2.is_numeric_type();
			else
				return t2.is_numeric_type() || t2.is_inf_type(); // it is possible to assign INF to numeric
		}

		if (t1.is_boolean_type())
			return (t2.is_boolean_type());
		if (t1.is_inf_type())
			return t2.is_inf_type();

		//printf("unexpected type = %s\n", gm_get_type_string(t1));
		//assert(false);
		return false;
	}

	public static boolean gm_is_compatible_type_for_assign(GMTYPE_T lhs, GMTYPE_T rhs)
	{
		return GlobalMembersGm_typecheck.gm_check_compatible_types(lhs, rhs, gm_type_compatible_t.FOR_ASSIGN);
	}
	public static boolean gm_is_compatible_type_for_eq(GMTYPE_T t1, GMTYPE_T t2)
	{
		return GlobalMembersGm_typecheck.gm_check_compatible_types(t1, t2, gm_type_compatible_t.FOR_EQ);
	}
	public static boolean gm_is_compatible_type_for_less(GMTYPE_T t1, GMTYPE_T t2)
	{
		return GlobalMembersGm_typecheck.gm_check_compatible_types(t1, t2, gm_type_compatible_t.FOR_LESS);
	}
	public static boolean gm_is_compatible_type_for_biop(GMTYPE_T t1, GMTYPE_T t2)
	{
		return GlobalMembersGm_typecheck.gm_check_compatible_types(t1, t2, gm_type_compatible_t.FOR_BOP);
	}
}