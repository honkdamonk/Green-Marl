package common;

//----------------------------------------------
// Compiler Options
//----------------------------------------------
public class gm_comp_args {
	
	public String name; // e.g. -h
	public int arg_type; // 0:NULL, 1:string, 2:int, 3:boolean
	public String help_string;
	public String def_value;
	
	public gm_comp_args(String name, int argType, String helpString, String defValue) {
		this.name = name;
		arg_type = argType;
		help_string = helpString;
		def_value = defValue;
	}

}