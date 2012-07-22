package backend_giraph;

import inc.GMTYPE_T;
import inc.GM_REDUCE_T;


public class GlobalMembersGm_giraph_gen_master
{

	//extern void gm_redirect_reproduce(FILE f);
	//extern void gm_baseindent_reproduce(int i);
	public static String get_reduce_base_value(GM_REDUCE_T reduce_type, GMTYPE_T gm_type)
	{
		switch (reduce_type)
		{
			case GMREDUCE_PLUS:
				return "0";
			case GMREDUCE_MULT:
				return "1";
			case GMREDUCE_AND:
				return "true";
			case GMREDUCE_OR:
				return "false";
			case GMREDUCE_MIN:
				switch (gm_type)
				{
					case GMTYPE_INT:
						return "Integer.MAX_VALUE";
					case GMTYPE_LONG:
						return "Long.MAX_VALUE";
					case GMTYPE_FLOAT:
						return "Float.MAX_VALUE";
					case GMTYPE_DOUBLE:
						return "Double.MAX_VALUE";
					default:
						assert false;
						return "0";
				}
			case GMREDUCE_MAX:
				switch (gm_type)
				{
					case GMTYPE_INT:
						return "Integer.MIN_VALUE";
					case GMTYPE_LONG:
						return "Long.MIN_VALUE";
					case GMTYPE_FLOAT:
						return "Float.MIN_VALUE";
					case GMTYPE_DOUBLE:
						return "Double.MIN_VALUE";
					default:
						assert false;
						return "0";
				}
			default:
				assert false;
				break;
		}
		return "0";
	}
}
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define TO_STR(X) #X
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define DEF_STRING(X) static const char *X = "X"
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP(CLASS, DESC) class CLASS : public gm_compile_step { private: CLASS() {set_description(DESC);}public: virtual void process(ast_procdef*p); virtual gm_compile_step* get_instance(){return new CLASS();} static gm_compile_step* get_factory(){return new CLASS();} };
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP_FACTORY(CLASS) CLASS::get_factory()
