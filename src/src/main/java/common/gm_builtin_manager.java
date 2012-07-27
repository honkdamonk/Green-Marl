package common;

import inc.GMTYPE_T;

// should-be a singleton 
public class gm_builtin_manager {

	private java.util.LinkedList<gm_builtin_def> defs = new java.util.LinkedList<gm_builtin_def>();
	private gm_builtin_def last_def;

	public gm_builtin_manager() {
		// -----------------------------------------------------
		// construct built-in library by
		// parsing built-in strings in (gm_builtin.h)
		// -----------------------------------------------------
		// C++ TO JAVA CONVERTER WARNING: This 'sizeof' ratio was replaced with
		// a direct reference to the array length:
		// ORIGINAL LINE: int cnt = sizeof(GM_builtins) /
		// sizeof(gm_builtin_desc_t);
		int cnt = GlobalMembersGm_builtin.GM_builtins.length;
		for (int i = 0; i < cnt; i++) {
			// C++ TO JAVA CONVERTER WARNING: The following line was determined
			// to be a copy constructor call - this should be verified and a
			// copy constructor should be created if it does not yet exist:
			// ORIGINAL LINE: gm_builtin_def* d = new
			// gm_builtin_def(&GM_builtins[i]);
			gm_builtin_def d = new gm_builtin_def(new gm_builtin_desc_t(GlobalMembersGm_builtin.GM_builtins[i]));
			defs.addLast(d);
			if (!d.is_synonym_def())
				last_def = d;
		}
	}

	public void dispose() {
		// java.util.Iterator<gm_builtin_def> i;
		// for (i = defs.iterator(); i.hasNext();)
		// i.next() = null;
	}

	public final gm_builtin_def find_builtin_def(GMTYPE_T source_type, String orgname) {
		for (gm_builtin_def d : defs) {
			GMTYPE_T def_src = d.get_source_type_summary();
			if (orgname.equals(d.get_orgname())) {
				if (def_src == source_type) {
					if (d.is_synonym_def())
						return d.get_org_def();
					else
						return d;
				}
				boolean is_strict = d.need_strict_source_type();
				if (is_strict)
					continue;
				if (def_src == GMTYPE_T.GMTYPE_VOID)
					continue;
				assert (!def_src.is_prim_type());

				if (GMTYPE_T.is_same_node_or_edge_compatible_type(def_src, source_type)
						|| GMTYPE_T.collection_of_collection_compatible_type(def_src, source_type)) {
					if (d.is_synonym_def())
						return d.get_org_def();
					else
						return d;
				}
			}
		}
		return null;
	}

	public final gm_builtin_def find_builtin_def(GMTYPE_T source_type, gm_method_id_t id) {

		for (gm_builtin_def d : defs) {
			if (d.get_method_id() != id)
				continue;

			GMTYPE_T def_src = d.get_source_type_summary();
			if (def_src != source_type) {

				boolean is_strict = d.need_strict_source_type();

				if (is_strict)
					continue;
				if (source_type == GMTYPE_T.GMTYPE_VOID)
					continue;
				if (def_src.is_prim_type())
					continue;
				if (!GMTYPE_T.is_same_node_or_edge_compatible_type(def_src, source_type))
					continue;
			}
			if (d.is_synonym_def())
				return d.get_org_def();
			else
				return d;
		}
		return null;
	}

	public final gm_builtin_def get_last_def() {
		return last_def;
	}
}