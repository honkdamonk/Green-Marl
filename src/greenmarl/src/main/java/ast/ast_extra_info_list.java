package ast;


public class ast_extra_info_list extends ast_extra_info
{
	public java.util.LinkedList<Object > list = new java.util.LinkedList<Object >();
	public ast_extra_info_list()
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
	public final java.util.LinkedList<Object > get_list()
	{
		return list;
	}
}