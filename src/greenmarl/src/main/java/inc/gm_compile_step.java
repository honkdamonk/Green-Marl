package inc;

import ast.ast_procdef;

public abstract class gm_compile_step
{

	protected gm_compile_step()
	{
		this._okay = true;
		this._affected = false;
		this._desc = "compiler step";
	}
	public void dispose()
	{
	}

	public abstract void process(ast_procdef p); // ast_node would be ast_procdef
	public String get_description()
	{
		return _desc;
	}
	public boolean has_affected()
	{
		return _affected;
	}
	public boolean is_okay()
	{
		return _okay;
	}

	// factory methods
	public static gm_compile_step get_factory()
	{
		assert false;
		return null;
	}
	public abstract gm_compile_step get_instance();

	protected void set_okay(boolean b)
	{
		_okay = b;
	}
	protected void set_affected(boolean b)
	{
		_affected = b;
	} // [to be used later]
	protected void set_description(String c)
	{
		_desc = c;
	}
	private boolean _okay;
	private boolean _affected;
	private String _desc;

}
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP(CLASS, DESC) class CLASS : public gm_compile_step { private: CLASS() {set_description(DESC);}public: virtual void process(ast_procdef*p); virtual gm_compile_step* get_instance(){return new CLASS();} static gm_compile_step* get_factory(){return new CLASS();} };

//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP_FACTORY(CLASS) CLASS::get_factory()

