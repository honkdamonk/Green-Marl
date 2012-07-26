package ast;

import java.util.HashMap;

public class ast_extra_info_map extends ast_extra_info {
	
	public HashMap<Object, Object> map = new HashMap<Object, Object>();

	public ast_extra_info_map() {
	}

	public void dispose() {
	}

	@Override
	public ast_extra_info copy() {
		assert false;
		return null;
	}

	public final HashMap<Object, Object> get_map() {
		return map;
	}
}