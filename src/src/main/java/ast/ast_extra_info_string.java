package ast;

import common.GlobalMembersGm_misc;

public class ast_extra_info_string extends ast_extra_info
{
	public String str;
	public ast_extra_info_string()
	{
		this.str = null;
	}
	public void dispose()
	{
		str = null;
	}

	public ast_extra_info_string(String org)
	{
		str = GlobalMembersGm_misc.gm_strdup(org);
	}
	public String get_string()
	{
		return (String) str;
	}
	public ast_extra_info copy()
	{
		ast_extra_info_string s = new ast_extra_info_string(str);
		s.base_copy(this);
		return s;
	}
}