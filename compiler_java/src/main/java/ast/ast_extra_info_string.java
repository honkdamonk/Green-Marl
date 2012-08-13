package ast;

public class ast_extra_info_string extends ast_extra_info {
	public String str;

	public ast_extra_info_string() {
		this.str = null;
	}

	public void dispose() {
		str = null;
	}

	public ast_extra_info_string(String org) {
		assert org != null;
		str = org;
	}

	public String get_string() {
		return (String) str;
	}

	public ast_extra_info copy() {
		ast_extra_info_string s = new ast_extra_info_string(str);
		s.base_copy(this);
		return s;
	}
}