package ast;


public class ast_extra_info_map extends ast_extra_info
{
	public java.util.HashMap<Object, Object > map = new java.util.HashMap<Object, Object >();
	public ast_extra_info_map()
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
	public final java.util.HashMap<Object, Object > get_map()
	{
		return map;
	}
}