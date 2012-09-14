package ast;

import java.util.LinkedList;

public class ast_extra_info_list extends ast_extra_info {
	
	public LinkedList<Object> list = new LinkedList<Object>();

	public ast_extra_info_list() {
	}

	@Override
	public ast_extra_info copy() {
		assert false;
		return null;
	}

	public final LinkedList<Object> get_list() {
		return list;
	}
}