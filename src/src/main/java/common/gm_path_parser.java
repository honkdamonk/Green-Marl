package common;

//------------------------------------------
// parsing path string
//------------------------------------------
public class gm_path_parser
{
	public gm_path_parser()
	{
		create_arrays(128);
	}
	public void dispose()
	{
		delete_arrays();
	}

	//defined in gm_misc.cc
	public final void parsePath(String fullpath)
	{
		int sz = fullpath.length();
		if (_sz < (sz + 1))
		{
			delete_arrays();
			create_arrays(sz + 1);
		}
		String _temp = new byte[sz + 1];
		_temp = fullpath;

		//----------------------------------------
		// parse path name
		//----------------------------------------
		String p_last = null;
//C++ TO JAVA CONVERTER TODO TASK: Java does not have an equivalent for pointers to value types:
//ORIGINAL LINE: sbyte* p = strtok(_temp, "/");
		byte p = tangible.StringFunctions.strTok(_temp, "/"); // ignore windows for now
		while (p != null)
		{
			p_last = p;
			p = tangible.StringFunctions.strTok(null, "/"); // not thread safe you know.
		}
		int index0;
		if (p_last == null)
		{
			_path = "";
			p_last = _temp;
			index0 = 0;
		}
		else
		{

			index0 = ((int)(p_last - _temp));
			_path = fullpath.substring(0, index0);
			_path = tangible.StringFunctions.changeCharacter(_path, index0, '\0');
		}
		String h_begin = fullpath.charAt(index0);

		//----------------------------------------
		// parse ext name
		//----------------------------------------
		String p_begin = p_last;
		p = tangible.StringFunctions.strTok(p_begin, ".");
		p_last = null;
		while (p != null)
		{
			p_last = p;
			p = tangible.StringFunctions.strTok(null, "."); // strtok is not thread safe, you know.
		}
		if (p_last.equals(p_begin))
		{
			_ext = "";
			_fname = h_begin;
		}
		else
		{
			_ext = p_last;

			int index1 = ((int)(p_last - p_begin));
			if (index1 > 1)
				_fname = h_begin.substring(0, index1 - 1);
			_fname = tangible.StringFunctions.changeCharacter(_fname, index1 - 1, '\0');
		}

		if (GlobalMembersGm_misc.gm_is_same_string(_path, ""))
			String.format(_path, ".");

	}

	public final String getPath()
	{
		return (String) _path;
	}
	public final String getFilename()
	{
		return (String) _fname;
	} // without extension
	public final String getExt()
	{
		return (String) _ext;
	}
	private String _fname;
	private String _ext;
	private String _path;
	private int _sz;
	private void delete_arrays()
	{
		_fname = null;
		_ext = null;
		_path = null;
	}
	private void create_arrays(int s)
	{
		_sz = s;
		_fname = new byte[_sz];
		_ext = new byte[_sz];
		_path = new byte[_sz];
	}
}