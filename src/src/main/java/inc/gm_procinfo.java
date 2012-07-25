package inc;

import ast.ast_extra_info;
import ast.ast_procdef;
import common.GlobalMembersGm_misc;
import common.gm_vocabulary;

// empty class

public class gm_procinfo {

	public gm_procinfo(ast_procdef d) {
		body = d;
		temp_no = 0;
		be_info = null;
	}

	public void dispose() {
	}

	// --------------------------------------------
	// vocaburary and temp name generator
	// --------------------------------------------
	public final gm_vocabulary get_voca() {
		return flat;
	}

	@Deprecated
	public final void add_voca(tangible.RefObject<String> n) {
		flat.add_word(n.argvalue);
	}

	public final void add_voca(String value) {
		flat.add_word(value);
	}

	public final void clear_voca() {
		flat.clear();
	}

	@Deprecated
	public final boolean isin_voca(tangible.RefObject<String> n) {
		return flat.has_word(n);
	}

	public final boolean isin_voca(String value) {
		return flat.has_word(value);
	}

	// ----------------------------------------------------------
	// create a temporary name, that does not conflict with any
	// word in the vocaburary
	// return a new string (caller should delete it later)
	// ----------------------------------------------------------
	public final String generate_temp_name(String base, gm_vocabulary extra) {
		return generate_temp_name(base, extra, false);
	}

	public final String generate_temp_name(String base) {
		return generate_temp_name(base, null, false);
	}

	// C++ TO JAVA CONVERTER NOTE: Java does not allow default values for
	// parameters. Overloaded methods are inserted above.
	// ORIGINAL LINE: sbyte* generate_temp_name(String base, gm_vocabulary*
	// extra = null, boolean try_org_name_first = false)
	public final String generate_temp_name(String base, gm_vocabulary extra, boolean try_org_name_first) {
		String temp = new String(new char[2048]); // should be enough
		if (try_org_name_first)
			String.format(temp, "%s", base); // try base name first
		else
			String.format(temp, "%s%d", base, temp_no++); // try base name first
		do {
			boolean tempVar = flat.has_word(temp);
			if (tempVar) {
				String.format(temp, "%s%d", base, temp_no++);
			} else {
				boolean tempVar2 = (extra != null) && (extra.has_word(temp));
				if (tempVar2) {
					String.format(temp, "%s%d", base, temp_no++);
				} else {
					break;
				}
			}

		} while (true);
		return GlobalMembersGm_misc.gm_strdup(temp);
	}

	// -------------------------------------------------------------------
	// adding extra information to procdef
	// -------------------------------------------------------------------
	public final boolean has_info(String id) {
		return body.has_info(id);
	}

	public final ast_extra_info find_info(String id) {
		return body.find_info(id);
	}

	public final boolean find_info_bool(String id) {
		return body.find_info_bool(id);
	}

	public final String find_info_string(String id) {
		return body.find_info_string(id);
	}

	public final float find_info_float(String id) {
		return body.find_info_float(id);
	}

	public final int find_info_int(String id) {
		return body.find_info_int(id);
	}

	public final Object find_info_ptr(String id) {
		return body.find_info_ptr(id);
	}

	public final Object find_info_ptr2(String id) {
		return body.find_info_ptr2(id);
	}

	public final void add_info(String id, ast_extra_info e) {
		body.add_info(id, e);
	}

	public final void add_info_int(String id, int i) {
		body.add_info_int(id, i);
	}

	public final void add_info_bool(String id, boolean b) {
		body.add_info_bool(id, b);
	}

	public final void add_info_ptr(String id, Object ptr1) {
		add_info_ptr(id, ptr1, null);
	}

	// C++ TO JAVA CONVERTER NOTE: Java does not allow default values for
	// parameters. Overloaded methods are inserted above.
	// ORIGINAL LINE: void add_info_ptr(String id, Object* ptr1, Object* ptr2 =
	// null)
	public final void add_info_ptr(String id, Object ptr1, Object ptr2) {
		body.add_info_ptr(id, ptr1, ptr2);
	}

	public final void add_info_float(String id, float f) {
		body.add_info_float(id, f);
	}

	public final void add_info_string(String id, String str) {
		body.add_info_string(id, str);
	}

	public final void remove_info(String id) {
		body.remove_info(id);
	}

	public final void remove_all_info() {
		body.remove_all_info();
	}

	public final ast_procdef get_body() {
		return body;
	}

	// --------------------------------------------------------------
	// Any backend strucut
	// --------------------------------------------------------------
	public final gm_backend_info get_be_info() {
		return be_info;
	}

	public final void set_be_info(gm_backend_info i) {
		be_info = i;
	}

	private gm_procinfo() {
		this.body = null;
		this.temp_no = 0;
		this.be_info = null;
	} // cannot create without body

	private ast_procdef body;
	private int temp_no;
	private gm_backend_info be_info;

	private gm_vocabulary flat = new gm_vocabulary(); // flat

}