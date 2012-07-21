package inc;

import common.gm_vocabulary;

//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define TO_STR(X) #X
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define DEF_STRING(X) static const char *X = "X"

public abstract class gm_backend
{

	public gm_backend()
	{
		this._voca_created = false;
	}
	public void dispose()
	{
	}

	public abstract void setTargetDir(String dname);
	public abstract void setFileName(String fname);

	//--------------------------------------------------
	// apply local optimize (transform), specific for target
	// returns is_okay
	//--------------------------------------------------
	public abstract boolean do_local_optimize();

	//--------------------------------------------------
	// apply local optimize (transform), specific for library
	// returns is_okay
	//--------------------------------------------------
	public abstract boolean do_local_optimize_lib();

	//--------------------------------------------------
	// apply local optimize (transform) before code gen
	// returns is_okay
	//--------------------------------------------------
	public abstract boolean do_generate();

	public gm_vocabulary get_language_voca()
	{
		if (!_voca_created)
		{
			_voca_created = true;
			build_up_language_voca();
		}
		return _lang_voca;
	}

	protected void build_up_language_voca()
	{
	} // default is to do nothing

	protected gm_vocabulary _lang_voca = new gm_vocabulary();
	protected boolean _voca_created;

}