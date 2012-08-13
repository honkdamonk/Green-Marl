package common;

import inc.GMTYPE_T;

import java.util.HashMap;

import ast.ast_extra_info;

public class gm_builtin_def {

	private GMTYPE_T src_type = GMTYPE_T.GMTYPE_INVALID;
	private GMTYPE_T res_type = GMTYPE_T.GMTYPE_INVALID;
	private int num_args = 0;
	private GMTYPE_T[] arg_types = null;
	private String orgname = null;
	private gm_method_id_t method_id;
	private boolean need_strict = false;
	private HashMap<String, ast_extra_info> extra_info = new HashMap<String, ast_extra_info>();

	private boolean synonym = false;
	private gm_builtin_def org_def = null;

	public gm_builtin_def(gm_builtin_desc_t def) {
		this.method_id = def.method_id;

		// parse string
		int temp = 0;
		assert def.def_string != null;
		char[] text = def.def_string.toCharArray();

		if (text[0] == '*') // synonym
		{

			gm_builtin_def org_def = GlobalMembersGm_main.BUILT_IN.get_last_def();
			assert org_def != null;

			this.synonym = true;
			this.need_strict = false;
			this.org_def = org_def;
			this.src_type = org_def.src_type; // need source type.
			this.orgname = "" + text[1];

			// no need.
			// this->res_type = org_def->res_type;

		} else {
			this.synonym = false;

			if (text[0] == '!') {
				this.need_strict = true;
				temp = temp + 1;
			} else {
				this.need_strict = false;
			}

			// parse and fill
			String p = tangible.StringFunctions.strTok(new String(text, temp, text.length - temp), ":");
			if (p.charAt(0) == '_')
				src_type = GMTYPE_T.GMTYPE_VOID; // top-level
			else
				src_type = GMTYPE_T.gm_get_type_from_string(p);

			p = tangible.StringFunctions.strTok(null, ":");
			assert p != null;
			orgname = p;
			p = tangible.StringFunctions.strTok(null, ":");
			res_type = GMTYPE_T.gm_get_type_from_string(p);
			p = tangible.StringFunctions.strTok(null, ":");
			if (p == null)
				num_args = 0;
			else
				num_args = Integer.parseInt(p);
			if (num_args > 0) {
				arg_types = new GMTYPE_T[num_args];
				for (int i = 0; i < num_args; i++) {
					p = tangible.StringFunctions.strTok(null, ":");
					arg_types[i] = GMTYPE_T.gm_get_type_from_string(p);
				}
			}

			// -----------------------------------------------------------
			// now parse the extra info [todo]
			// -----------------------------------------------------------
			String extra_info = def.extra_info;

			p = tangible.StringFunctions.strTok(extra_info, ":");
			String p2 = tangible.StringFunctions.strTok(null, ":");
			while ((p != null) && (p2 != null)) {
				String key = p;
				if (p2.equals("true")) {
					add_info_bool(key, true);
					assert find_info_bool(key) == true;
				} else if (p2.equals("false")) {
					add_info_bool(key, false);
				} else {
					add_info_int(key, Integer.parseInt(p2));
				}
				p = tangible.StringFunctions.strTok(null, ":");
				p2 = tangible.StringFunctions.strTok(null, ":");
			}
		}
	}

	public final int get_num_args() {
		return num_args;
	}

	public final GMTYPE_T get_source_type_summary() {
		return src_type;
	}

	public final GMTYPE_T get_result_type_summary() {
		return res_type;
	}

	public final GMTYPE_T get_arg_type(int i) {
		assert i < num_args;
		return arg_types[i];
	}

	public final gm_method_id_t get_method_id() {
		return method_id;
	}

	public final String get_orgname() {
		return orgname;
	}

	public final boolean is_synonym_def() {
		return synonym;
	}

	public final gm_builtin_def get_org_def() {
		return org_def;
	}

	public final void add_info_int(String key, int v) {
		String s = key;
		if (!extra_info.containsKey(key)) {
			ast_extra_info I = new ast_extra_info();
			I.ival = v;
			extra_info.put(s, I);
		} else {
			extra_info.get(s).ival = v;
		}
	}

	public final void add_info_bool(String key, boolean v) {
		String s = key;
		if (!extra_info.containsKey(key)) {
			ast_extra_info I = new ast_extra_info();
			I.bval = v;
			extra_info.put(s, I);
		} else {
			extra_info.get(s).bval = v;
		}
	}

	public final boolean has_info(String key) {
		String s = key;
		if (!extra_info.containsKey(s))
			return false;
		return true;
	}

	public final boolean find_info_bool(String key) {
		String s = key;
		if (!extra_info.containsKey(s))
			return false;
		else {
			return extra_info.get(s).bval;
		}

	}

	public final int find_info_int(String key) {
		String s = key;
		if (!extra_info.containsKey(s))
			return 0;
		else
			return extra_info.get(s).ival;
	}

	public final boolean need_strict_source_type() {
		return need_strict;
	}
}