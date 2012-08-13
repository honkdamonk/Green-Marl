package common;

import inc.GMTYPE_T;
import ast.AST_NODE_TYPE;
import ast.ast_id;
import ast.ast_node;
import ast.ast_sent;
import ast.ast_sentblock;
import ast.ast_typedecl;
import frontend.gm_symtab;
import frontend.gm_symtab_entry;
import frontend.gm_typechecker_stage_1;

public class GlobalMembersGm_add_symbol {

	public static ast_sentblock gm_find_upscope(ast_sent s) {
		if (s == null)
			return null;

		ast_node up = s.get_parent();
		if (up == null)
			return null;
		if (up.get_nodetype() == AST_NODE_TYPE.AST_SENTBLOCK)
			return (ast_sentblock) up;
		if (up.is_sentence())
			return GlobalMembersGm_add_symbol.gm_find_upscope((ast_sent) up);
		return null;
	}

	/**
	 * add a new symbol of primitive type into given sentence block<br>
	 * assumption: newname does not have any name-conflicts
	 */
	public static gm_symtab_entry gm_add_new_symbol_primtype(ast_sentblock sb, GMTYPE_T primtype, tangible.RefObject<String> newname) {
		assert sb != null;

		gm_symtab target_syms;
		target_syms = sb.get_symtab_var();
		assert target_syms != null;

		// create type object and check
		ast_typedecl type = ast_typedecl.new_primtype(primtype);
		boolean success = gm_typechecker_stage_1.gm_check_type_is_well_defined(type, target_syms);
		assert success;

		// create id object and declare
		ast_id new_id = ast_id.new_id(newname.argvalue, 0, 0);
		success = gm_typechecker_stage_1.gm_declare_symbol(target_syms, new_id, type, true, true);
		assert success;

		// return symbol
		gm_symtab_entry e = null;
		e = new_id.getSymInfo();
		assert e != null;

		// these are temporary
		if (type != null)
			type.dispose();
		if (new_id != null)
			new_id.dispose();

		return e;
	}

	public static gm_symtab_entry gm_add_new_symbol_nodeedge_type(ast_sentblock sb, GMTYPE_T nodeedge_type, gm_symtab_entry graph_sym,
			tangible.RefObject<String> newname) {
		assert sb != null;

		gm_symtab target_syms;
		target_syms = sb.get_symtab_var();
		assert target_syms != null;

		// create type object and check
		ast_typedecl type;
		if (nodeedge_type == GMTYPE_T.GMTYPE_NODE)
			type = ast_typedecl.new_nodetype(graph_sym.getId().copy(true));
		else if (nodeedge_type == GMTYPE_T.GMTYPE_EDGE)
			type = ast_typedecl.new_edgetype(graph_sym.getId().copy(true));
		else {
			assert false;
			throw new AssertionError();
		}
		boolean success = gm_typechecker_stage_1.gm_check_type_is_well_defined(type, target_syms);
		assert success;

		// create id object and declare
		ast_id new_id = ast_id.new_id(newname.argvalue, 0, 0);
		success = gm_typechecker_stage_1.gm_declare_symbol(target_syms, new_id, type, true, true);
		assert success;

		// return symbol
		gm_symtab_entry e = null;
		e = new_id.getSymInfo();
		assert e != null;

		// these are temporary
		if (type != null)
			type.dispose();
		if (new_id != null)
			new_id.dispose();

		return e;
	}

	/**
	 * add a new symbol of node(edge) property type into given sentence block<br>
	 * assumption: newname does not have any name-conflicts
	 */
	public static gm_symtab_entry gm_add_new_symbol_property(ast_sentblock sb, GMTYPE_T primtype, boolean is_nodeprop, gm_symtab_entry target_graph,
			tangible.RefObject<String> newname) // assumtpion: no name-conflict.
	{
		ast_id target_graph_id = target_graph.getId().copy();
		ast_typedecl target_type = ast_typedecl.new_primtype(primtype);

		// create type object and check
		ast_typedecl type;
		if (is_nodeprop)
			type = ast_typedecl.new_nodeprop(target_type, target_graph_id);
		else
			type = ast_typedecl.new_edgeprop(target_type, target_graph_id);

		boolean success = gm_typechecker_stage_1.gm_check_type_is_well_defined(type, sb.get_symtab_var());
		assert success;

		// create property id object and declare
		ast_id new_id = ast_id.new_id(newname.argvalue, 0, 0);
		gm_symtab target_syms;
		target_syms = sb.get_symtab_field();
		assert target_syms != null;
		success = gm_typechecker_stage_1.gm_declare_symbol(target_syms, new_id, type, true, true);
		assert success;

		// return symbol
		gm_symtab_entry e = null;
		e = new_id.getSymInfo();
		assert e != null;

		// these are temporary
		if (type != null)
			type.dispose();
		if (new_id != null)
			new_id.dispose();

		return e;
	}

	/**
	 * - move a symbol entry up into another symbol table [assumption] new_tab
	 * belongs to a sentence block<br>
	 * - name conflict does not happen
	 * 
	 * @return the sentence block which is the new scope
	 */
	public static void gm_move_symbol_into(gm_symtab_entry e, gm_symtab old_tab, gm_symtab new_tab, boolean is_scalar) {
		assert new_tab.get_ast().get_nodetype() == AST_NODE_TYPE.AST_SENTBLOCK;
		assert old_tab.is_entry_in_the_tab(e);

		ast_sentblock sb = (ast_sentblock) new_tab.get_ast();

		// delete from the old-table
		old_tab.remove_entry_in_the_tab(e);

		// resolve name conflict in the up-scope
		// gm_resolve_name_conflict(sb, e, is_scalar);

		// add in the new-table
		new_tab.add_symbol(e);
	}

	/**
	 * - move a symbol entry up into a sentence block<br>
	 * - name conflict is resolved
	 * 
	 * @return the sentence block which is the new scope
	 */
	public static ast_sentblock gm_move_symbol_up(gm_symtab_entry e, gm_symtab old_tab, boolean is_scalar) {
		assert old_tab.is_entry_in_the_tab(e);

		// find up_scope table
		gm_symtab up;
		boolean found = false;
		while (true) {
			up = old_tab.get_parent();
			if (up == null)
				break;
			if (up.get_ast().get_nodetype() == AST_NODE_TYPE.AST_SENTBLOCK) {
				found = true;
				break;
			}
		}
		if (!found)
			return null;
		ast_sentblock sb = (ast_sentblock) up.get_ast();
		gm_symtab new_tab = is_scalar ? sb.get_symtab_var() : sb.get_symtab_field();

		GlobalMembersGm_add_symbol.gm_move_symbol_into(e, old_tab, new_tab, is_scalar);

		return sb;
	}

	public static void gm_remove_symbols(ast_node top, java.util.HashSet<gm_symtab_entry> S) {
		gm_remove_symbols_t T = new gm_remove_symbols_t(S);
		top.traverse_pre(T);
	}

	public static void gm_remove_symbol(ast_node top, gm_symtab_entry e) {
		java.util.HashSet<gm_symtab_entry> S = new java.util.HashSet<gm_symtab_entry>();
		S.add(e);

		gm_remove_symbols_t T = new gm_remove_symbols_t(S);
		top.traverse_pre(T);
	}

	public static ast_sentblock gm_find_defining_sentblock_up(ast_node node, gm_symtab_entry e) {
		return gm_find_defining_sentblock_up(node, e, false);
	}

	public static ast_sentblock gm_find_defining_sentblock_up(ast_node node, gm_symtab_entry e, boolean is_property) {
		while (node != null) {
			if (node.has_symtab()) {
				if (is_property) {
					if (node.get_symtab_field().is_entry_in_the_tab(e)) {
						assert node.get_nodetype() == AST_NODE_TYPE.AST_SENTBLOCK;
						return (ast_sentblock) node;
					}
				} else {
					if (node.get_symtab_var().is_entry_in_the_tab(e)) {
						if (node.get_nodetype() != AST_NODE_TYPE.AST_SENTBLOCK) {
							System.out.printf("%s not defined in a sentblock\n", e.getId().get_genname());
						}
						assert node.get_nodetype() == AST_NODE_TYPE.AST_SENTBLOCK;
						return (ast_sentblock) node;
					}
				}

			}
			node = node.get_parent();
		}

		return null;
	}
}