package frontend;

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

//------------------------------------------------------------------------
// CLASS NUMERIC OP

//------------------------------------------------------------------------
public enum gm_operator_t
{
	INT_OP(0), // %
	NUMERIC_OP(1), // +,-,*,/, Max, Min
	BOOL_OP(2), // And, Or
	COMP_OP(3), // <,<=,>,>=
	EQ_OP(4), // == !=
	TER_OP(5), // ? t1 : t2
	ASSIGN_OP(6); // =

	private int intValue;
	private static java.util.HashMap<Integer, gm_operator_t> mappings;
	private static java.util.HashMap<Integer, gm_operator_t> getMappings()
	{
		if (mappings == null)
		{
			synchronized (gm_operator_t.class)
			{
				if (mappings == null)
				{
					mappings = new java.util.HashMap<Integer, gm_operator_t>();
				}
			}
		}
		return mappings;
	}

	private gm_operator_t(int value)
	{
		intValue = value;
		gm_operator_t.getMappings().put(value, this);
	}

	public int getValue()
	{
		return intValue;
	}

	public static gm_operator_t forValue(int value)
	{
		return getMappings().get(value);
	}
}