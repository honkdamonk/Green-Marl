package common;

//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define TO_STR(X) #X
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define DEF_STRING(X) static const char *X = "X"
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP(CLASS, DESC) class CLASS : public gm_compile_step { private: CLASS() {set_description(DESC);}public: virtual void process(ast_procdef*p); virtual gm_compile_step* get_instance(){return new CLASS();} static gm_compile_step* get_factory(){return new CLASS();} };
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP_FACTORY(CLASS) CLASS::get_factory()


//----------------------------------------------
// Compiler Options
//----------------------------------------------
public class gm_comp_args
{
	public gm_comp_args(String name, int argType, String helpString, String defValue) {
		this.name = name;
		arg_type = argType;
		help_string = helpString;
		def_value = defValue;
	}
	
	public String name; // e.g. -h
	public int arg_type; // 0:NULL, 1:string, 2:int, 3:boolean
	public String help_string;
	public String def_value;

}