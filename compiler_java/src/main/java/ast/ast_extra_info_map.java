package ast;

import java.util.Map;
import java.util.TreeMap;

public class ast_extra_info_map extends ast_extra_info {
	
	public Map<Object, Object> map = new TreeMap<Object, Object>();

	public ast_extra_info_map() {
	}

	@Override
	public ast_extra_info copy() {
		assert false;
		return null;
	}

	public final Map<Object, Object> get_map() {
		return map;
	}
}