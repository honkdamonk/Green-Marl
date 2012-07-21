package ast;


public class ast_extra_info_set extends ast_extra_info
{
	public java.util.HashSet<Object > set = new java.util.HashSet<Object >();
	public ast_extra_info_set()
	{
	}
	public void dispose()
	{
	}
	@Override
	public ast_extra_info copy()
	{
		assert false;
		return null;
	}
	public final java.util.HashSet<Object > get_set()
	{
		return set;
	}
}