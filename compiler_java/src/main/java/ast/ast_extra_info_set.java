package ast;

import java.util.HashSet;

public class ast_extra_info_set extends ast_extra_info {
	
	public HashSet<Object> set = new HashSet<Object>();

	public ast_extra_info_set() {
	}

	@Override
	public ast_extra_info copy() {
		assert false;
		return null;
	}

	public final HashSet<Object> get_set() {
		return set;
	}
}