package frontend;

import inc.GM_PROP_USAGE_T;
import ast.ast_extra_info;
import ast.ast_extra_info_string;
import ast.ast_id;
import ast.ast_typedecl;

// symbol table entry
public class gm_symtab_entry {

	private gm_symtab_entry() {
		this.id = null;
		this.type = null;
		this.isRA = false;
		this.isWA = false;
		this.isArg = false;
	}

	// always call with a copy of ID
	public gm_symtab_entry(ast_id _id, ast_typedecl _type, boolean _isRA) {
		this(_id, _type, _isRA, true);
	}

	public gm_symtab_entry(ast_id _id, ast_typedecl _type) {
		this(_id, _type, true, true);
	}

	public gm_symtab_entry(ast_id _id, ast_typedecl _type, boolean _isRA, boolean _isWA) {
		this.id = _id;
		this.type = _type;
		this.isRA = _isRA;
		this.isWA = _isWA;
		this.isArg = false;
		id.setSymInfo(this, true);
		assert type != null;
		assert id.name != null;
	}

	public void dispose() {
		if (id != null)
			id.dispose();
		if (type != null)
			type.dispose();
		// TODO not tested!
		for (String s : extra.keySet()) {
			if (extra.get(s) != null)
				extra.get(s).dispose();
		}
	}

	public final ast_typedecl getType() {
		return type;
	}

	public final ast_id getId() {
		return id;
	}

	public final boolean isReadable() {
		return (isRA == GlobalMembersGm_typecheck.GM_READ_AVAILABLE);
	}

	public final boolean isWriteable() {
		return (isWA == GlobalMembersGm_typecheck.GM_WRITE_AVAILABLE);
	}

	public final boolean isArgument() {
		return isArg;
	}

	// defined in gm_node_info.cc
	public boolean has_info(String id) {
		return extra.containsKey(id);
	}

	public ast_extra_info find_info(String id) {
		if (has_info(id)) {
			return extra.get(id);
		} else {
			return null;
		}
	}

	public boolean find_info_bool(String id) {
		ast_extra_info info = find_info(id);
		if (info == null)
			return false;
		else
			return info.bval;
	}

	public String find_info_string(String id) {
		ast_extra_info_string info = (ast_extra_info_string) find_info(id);
		if (info == null)
			return ""; // or NULL string?
		else
			return info.get_string();
	}

	public float find_info_float(String id) {
		ast_extra_info info = find_info(id);
		if (info == null)
			return 0;
		else
			return info.fval;
	}

	public int find_info_int(String id) {
		ast_extra_info info = find_info(id);
		if (info == null)
			return 0;
		else
			return info.ival;
	}

	public Object find_info_ptr(String id) {
		ast_extra_info info = find_info(id);
		if (info == null)
			return null;
		else
			return info.ptr1;
	}

	public Object find_info_ptr2(String id) {
		ast_extra_info info = find_info(id);
		if (info == null)
			return null;
		else
			return info.ptr2;
	}

	public void add_info(String id, ast_extra_info e) {
		// should I delete repeated entry?
		assert e != null;
		extra.put(id, e);
	}

	public void add_info_int(String id, GM_PROP_USAGE_T gmusageUnused) {
		add_info(id, new ast_extra_info(gmusageUnused));
	}

	public void add_info_bool(String id, boolean b) {
		add_info(id, new ast_extra_info(b));
	}

	public void add_info_ptr(String id, Object ptr1, Object ptr2) {
		add_info(id, new ast_extra_info(ptr1, ptr2));
	}

	public void add_info_float(String id, float f) {
		add_info(id, new ast_extra_info(f));
	}

	public void add_info_string(String id, String str) {
		add_info(id, new ast_extra_info_string(str));
	}

	public void remove_info(String id) {
		extra.remove(id); // [XXX] need delete extra_info object
	}

	// void remove_all_info();

	public final void setArgument(boolean b) {
		isArg = b;
	}

	private ast_id id;
	private ast_typedecl type;
	private boolean isRA;
	private boolean isWA;
	private boolean isArg;
	private java.util.HashMap<String, ast_extra_info> extra = new java.util.HashMap<String, ast_extra_info>();
}