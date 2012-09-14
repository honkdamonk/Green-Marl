package common;

import java.util.HashMap;

public class gm_userargs {

	private final HashMap<String, String> str_args = new HashMap<String, String>();
	private final HashMap<String, Boolean> bool_args = new HashMap<String, Boolean>();
	private final HashMap<String, Integer> int_args = new HashMap<String, Integer>();

	// key (and value) will be copied before it is stored.
	public final void set_arg_string(String key, String val) {
		assert key != null;
		assert val != null;
		str_args.put(key, val);
	}

	public final void set_arg_bool(String key, boolean b) {
		assert key != null;
		bool_args.put(key, b);
	}

	public final void set_arg_int(String key, int i) {
		assert key != null;
		int_args.put(key, i);
	}

	// returns NULL if not defined
	public final String get_arg_string(String key) {
		return str_args.get(key);
	}

	// returns 0 if not defined
	public final int get_arg_int(String key) {
		if (!int_args.containsKey(key))
			return 0;
		else
			return int_args.get(key);
	}

	// returns false if not defined
	public final boolean get_arg_bool(String key) {
		if (!bool_args.containsKey(key))
			return false;
		else
			return bool_args.get(key);
	}

	public final boolean is_arg_bool(String key) {
		return get_arg_bool(key);
	}

}