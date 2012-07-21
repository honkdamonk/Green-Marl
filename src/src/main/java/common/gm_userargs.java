public class gm_userargs
{

	// key (and value) will be copied before
	// it is stored.
	public final void set_arg_string(String key, String val)
	{
		assert key != null;
		String _k = new byte[key.length() + 1];
		_k = key;
		assert val != null;
		String _v = new byte[val.length() + 1];
		_v = val;

		java.util.Iterator<String, String > it = str_args.find(key);
		if (it.hasNext()) // delete old, if found
		{
			str_args.remove(it);
		}
		// re-insert
		str_args.put(_k, _v);
	}

	public final void set_arg_bool(String key, boolean b)
	{
		assert key != null;
		String _k = new byte[key.length() + 1];
		_k = key;
		java.util.Iterator<String, Boolean> it = bool_args.find(key);
		if (it.hasNext()) // delete old, if found
		{
			bool_args.remove(it);
		}
		// re-insert
		bool_args.put(_k, b);
	}

	public final void set_arg_int(String key, int i)
	{
		assert key != null;
		String _k = new byte[key.length() + 1];
		_k = key;
		java.util.Iterator<String, Integer> it = int_args.find(key);
		if (it.hasNext()) // delete old, if found
		{
			int_args.remove(it);
		}
		// re-insert
		int_args.put(_k, i);
	}

	// returns NULL if not defined
	public final String get_arg_string(String key)
	{
		java.util.Iterator<String, String > it = str_args.find(key);
		if (it == str_args.end())
		{
			return null;
		}
		else
		{
			return it.next().getValue();
		}
	}

	// returns 0 if not defined
	public final int get_arg_int(String key)
	{
		java.util.Iterator<String, Integer> it = int_args.find(key);
		if (it == int_args.end())
		{
			return 0;
		}
		else
		{
			return it.next().getValue();
		}
	}

	// returns false if not defined
	public final boolean get_arg_bool(String key)
	{
		java.util.Iterator<String, Boolean> it = bool_args.find(key);
		if (it == bool_args.end())
		{
			return false;
		}
		else
		{
			return it.next().getValue();
		}
	}
	public final boolean is_arg_bool(String key)
	{
		return get_arg_bool(key);
	}

	private java.util.HashMap<String, String, gm_lesscstr> str_args = new java.util.HashMap<String, String, gm_lesscstr>();
	private java.util.HashMap<String, boolean, gm_lesscstr> bool_args = new java.util.HashMap<String, boolean, gm_lesscstr>();
	private java.util.HashMap<String, int, gm_lesscstr> int_args = new java.util.HashMap<String, int, gm_lesscstr>();
}