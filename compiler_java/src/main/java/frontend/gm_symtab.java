package frontend;

import java.util.HashSet;

import tangible.RefObject;
import ast.ast_id;
import ast.ast_node;
import ast.ast_typedecl;

// symbol table
public class gm_symtab {

	private final HashSet<gm_symtab_entry> entries = new HashSet<gm_symtab_entry>();
	private gm_symtab parent = null;
	private symtab_types symtab_type;
	private ast_node ast; // where this belongs to
	private boolean default_graph_used = false;

	public gm_symtab(symtab_types _symtab_type, ast_node _ast) {
		symtab_type = _symtab_type;
		ast = _ast;
	}

	public final symtab_types get_symtab_type() {
		return symtab_type;
	}

	public final void set_parent(gm_symtab p) {
		parent = p;
		assert this != parent;
	}

	public final gm_symtab get_parent() {
		return parent;
	}

	public final ast_node get_ast() {
		return ast;
	}

	// if old entry is not found, copy of id and copy of typedecl is added into
	// the table
	public final boolean check_duplicate_and_add_symbol(ast_id id, ast_typedecl type, RefObject<gm_symtab_entry> old_def_ref, boolean isRA) {
		return check_duplicate_and_add_symbol(id, type, old_def_ref, isRA, true);
	}

	public final boolean check_duplicate_and_add_symbol(ast_id id, ast_typedecl type, RefObject<gm_symtab_entry> old_def_ref) {
		return check_duplicate_and_add_symbol(id, type, old_def_ref, true, true);
	}

	public final boolean check_duplicate_and_add_symbol(ast_id id, ast_typedecl type, RefObject<gm_symtab_entry> old_def_ref, boolean isRA, boolean isWA) {
		assert id.getSymInfo() == null;
		// printf("check duplicate for %s\n", id->get_orgname());
		old_def_ref.argvalue = find_symbol(id);
		if (old_def_ref.argvalue != null)
			return false;
		add_entry(id, type, isRA, isWA); // copy is created inside
		return true;
	}

	public final boolean check_and_add_symbol(ast_id id, ast_typedecl type, RefObject<gm_symtab_entry> old_def_ref, boolean isRA) {
		return check_and_add_symbol(id, type, old_def_ref, isRA, true);
	}

	public final boolean check_and_add_symbol(ast_id id, ast_typedecl type, RefObject<gm_symtab_entry> old_def_ref) {
		return check_and_add_symbol(id, type, old_def_ref, true, true);
	}

	public final boolean check_and_add_symbol(ast_id id, ast_typedecl type, RefObject<gm_symtab_entry> old_def_ref, boolean isRA, boolean isWA) {
		return check_duplicate_and_add_symbol(id, type, old_def_ref, isRA, isWA);
	}

	public final gm_symtab_entry find_symbol(ast_id id) {
		for (gm_symtab_entry e : entries) {
			String c = e.getId().get_orgname();
			String c2 = id.get_orgname();
			if (c.equals(c2))
				return e;
		}
		if (parent == null)
			return null;
		return parent.find_symbol(id);
	}

	public final int get_num_symbols() {
		return entries.size();
	}

	public final HashSet<gm_symtab_entry> get_entries() {
		return entries;
	}

	/** return true if entry is in the table */
	public final boolean is_entry_in_the_tab(gm_symtab_entry e) {
		return entries.contains(e);
	}

	public final void remove_entry_in_the_tab(gm_symtab_entry e) {
		entries.remove(e);
	}

	/**
	 * merge table A into this. table A is emptied. (assertion: name conflict
	 * has been resolved before calling this function)
	 */
	public final void merge(gm_symtab A) {
		assert A != null;
		for (gm_symtab_entry entry : A.entries) {
			// entries.push_back(*i);
			entries.add(entry);
		}
		A.entries.clear();
	}

	/**
	 * add symbol entry (assertion: name conflict has been resolved)
	 */
	public final void add_symbol(gm_symtab_entry e) {
		entries.add(e);
	}

	public final void set_default_graph_used() {
		default_graph_used = true;
	}

	public final boolean is_default_graph_used() {
		if (default_graph_used)
			return true;
		else if (parent == null)
			return false;
		else
			return parent.is_default_graph_used();
	}

	public final int get_graph_declaration_count() {
		int count = 0;
		for (gm_symtab_entry entry : entries) {
			ast_typedecl entryType = entry.getType();
			if (entryType.is_graph()) {
				count++;
			}
		}
		if (parent == null)
			return count;
		else
			return count + parent.get_graph_declaration_count();
	}

	// // copy of (id) and copy of (type) is added into a new symbol entry
	// private void add_entry(ast_id id, ast_typedecl type, boolean isRA) {
	// add_entry(id, type, isRA, true);
	// }
	// unused

	private void add_entry(ast_id id, ast_typedecl type, boolean isRA, boolean isWA) {
		ast_id id_copy = id.copy();
		ast_typedecl type_copy = type.copy();
		gm_symtab_entry e = new gm_symtab_entry(id_copy, type_copy, isRA, isWA);

		id.setSymInfo(e);
		// entries.push_back(e);
		entries.add(e);
	}

}