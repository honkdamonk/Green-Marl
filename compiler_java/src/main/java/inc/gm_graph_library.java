package inc;


public abstract class gm_graph_library
{
	public void dispose()
	{
	}

	//virtual const char* get_header_info()=0;
	//virtual const char* get_type_string(ast_typedecl*, int usage) =0;

	//virtual const char* get_header_info()=0;
	//virtual const char* get_type_string(ast_typedecl*, int usage) =0;

	//virtual bool generate(ast_nop* n) {return true;}
	//virtual bool generate_builtin(ast_expr_builtin* e) = 0;

	public abstract boolean do_local_optimize();

	public final void set_code_writer(gm_code_writer w)
	{
		Body = w;
	}
	public final gm_code_writer get_code_writer()
	{
		return Body;
	}

	protected gm_code_writer Body;
}