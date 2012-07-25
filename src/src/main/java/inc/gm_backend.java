package inc;

import common.gm_vocabulary;

//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define TO_STR(X) #X
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define DEF_STRING(X) static const char *X = "X"

public interface gm_backend {

	public void setTargetDir(String dname);

	public void setFileName(String fname);

	// --------------------------------------------------
	// apply local optimize (transform), specific for target
	// returns is_okay
	// --------------------------------------------------
	public boolean do_local_optimize();

	// --------------------------------------------------
	// apply local optimize (transform), specific for library
	// returns is_okay
	// --------------------------------------------------
	public boolean do_local_optimize_lib();

	// --------------------------------------------------
	// apply local optimize (transform) before code gen
	// returns is_okay
	// --------------------------------------------------
	public boolean do_generate();

	public gm_vocabulary get_language_voca();

}