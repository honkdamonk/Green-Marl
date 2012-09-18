package common;

import inc.gm_type;

import java.util.Map;
import java.util.TreeMap;

import ast.ast_extra_info;

public class gm_builtin_def {

	private gm_type src_type = gm_type.GMTYPE_INVALID;
	private gm_type res_type = gm_type.GMTYPE_INVALID;
	private int num_args = 0;
	private gm_type[] arg_types = null;
	private String orgname = null;
	private gm_method_id method_id;
	private boolean need_strict = false;
	private final Map<String, ast_extra_info> extra_info = new TreeMap<String, ast_extra_info>();

	private boolean synonym = false;
	private gm_builtin_def org_def = null;

	public gm_builtin_def(gm_builtin_desc_t def, gm_builtin_manager manager) {
		method_id = def.method_id;

		// parse string
		assert def.def_string != null;
		String text = def.def_string;

		if (text.charAt(0) == '*') // synonym
		{

			gm_builtin_def org_def = manager.get_last_def();
			assert org_def != null;

			this.synonym = true;
			this.need_strict = false;
			this.org_def = org_def;
			this.src_type = org_def.src_type; // need source type.
			this.orgname = text.substring(1);

			// no need.
			// this->res_type = org_def->res_type;

		} else {
			this.synonym = false;

			if (text.charAt(0) == '!') {
				this.need_strict = true;
				text = text.substring(1);
			} else {
				this.need_strict = false;
			}

			// parse and fill
			String[] p = text.split(":");
			if (p[0].charAt(0) == '_')
				src_type = gm_type.GMTYPE_VOID; // top-level
			else
				src_type = gm_type.gm_get_type_from_string(p[0]);

			assert p != null;
			orgname = p[1];
			res_type = gm_type.gm_get_type_from_string(p[2]);
			if (p.length < 4)
				num_args = 0;
			else
				num_args = Integer.parseInt(p[3]);
			if (num_args > 0) {
				arg_types = new gm_type[num_args];
				for (int i = 0; i < num_args; i++) {
					arg_types[i] = gm_type.gm_get_type_from_string(p[i + 4]);
				}
			}

			// -----------------------------------------------------------
			// now parse the extra info [todo]
			// -----------------------------------------------------------
			String extra_info = def.extra_info;

			p = extra_info.split(":");
			if (p.length > 1) {
				for (int i = 0; i < p.length; i += 2) {
					String key = p[i];
					if (p[i + 1].equals("true")) {
						add_info_bool(key, true);
						assert find_info_bool(key) == true;
					} else if (p[i + 1].equals("false")) {
						add_info_bool(key, false);
					} else {
						add_info_int(key, Integer.parseInt(p[i + 1]));
					}
				}
			}
		}
	}

	public final int get_num_args() {
		return num_args;
	}

	public final gm_type get_source_type_summary() {
		return src_type;
	}

	public final gm_type get_result_type_summary() {
		return res_type;
	}

	public final gm_type get_arg_type(int i) {
		assert i < num_args;
		return arg_types[i];
	}

	public boolean genericArgumentTypeIsKeyType(int position) {
		assert (position == 0); // we only support maps yet
		switch (method_id) {
		case GM_BLTIN_MAP_HAS_MAX_VALUE:
		case GM_BLTIN_MAP_HAS_MIN_VALUE:
		case GM_BLTIN_MAP_HAS_KEY:
			return true;
		default:
			assert (false); // if you called it with the wrong method_id, then
							// your code must be wrong...
			return false;
		}
	}

	public final gm_method_id get_method_id() {
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

	public boolean genericTypeIsKeyType() {
		switch (method_id) {
		case GM_BLTIN_MAP_GET_MAX_KEY:
		case GM_BLTIN_MAP_GET_MIN_KEY:
			return true;
		case GM_BLTIN_MAP_GET_MAX_VALUE:
		case GM_BLTIN_MAP_GET_MIN_VALUE:
			return false;
		default:
			assert (false); // if you called it with the wrong method_id, then
							// your code must be wrong...
			return false;
		}
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