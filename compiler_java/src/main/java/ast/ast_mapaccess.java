package ast;

import static ast.ast_node_type.AST_MAPACCESS;

import common.gm_dumptree;

import frontend.gm_symtab_entry;

public class ast_mapaccess extends ast_node {

	private ast_id mapId;
	private ast_expr keyExpr;
	private gm_symtab_entry keyGraph;
	private gm_symtab_entry valueGraph;

	private ast_mapaccess() {
		super(AST_MAPACCESS);
	}

	private ast_mapaccess(ast_id map, ast_expr key) {
		this();
		mapId = map;
		keyExpr = key;
	}

	public ast_mapaccess copy() {
		return copy(false);
	}

	public ast_mapaccess copy(boolean cp_sym) {
		ast_mapaccess clone = new ast_mapaccess();
		clone.mapId = mapId.copy(cp_sym);
		clone.keyExpr = keyExpr.copy(cp_sym);
		return clone;
	}

	public ast_id get_map_id() {
		assert (mapId != null);
		return mapId;
	}

	public ast_expr get_key_expr() {
		assert (keyExpr != null);
		return keyExpr;
	}

	public gm_symtab_entry get_bound_graph_for_key() {
		return keyGraph;
	}

	public void set_bound_graph_for_key(gm_symtab_entry graphEntry) {
		keyGraph = graphEntry;
	}

	public gm_symtab_entry get_bound_graph_for_value() {
		return valueGraph;
	}

	public void set_bound_graph_for_value(gm_symtab_entry graphEntry) {
		valueGraph = graphEntry;
	}

	@Override
	public void reproduce(int id_level) {
	    mapId.reproduce(0);
	    Out.push('[');
	    keyExpr.reproduce(0);
	    Out.push(']');
	}

	@Override
	public void dump_tree(int id_level) {
	    assert(parent != null);
	    gm_dumptree.IND(id_level);
	    System.out.print("[");
	    mapId.dump_tree(0);
	    System.out.print("[");
	    keyExpr.dump_tree(id_level + 1);
	    System.out.print("]");
	    System.out.print("]");
	}

	public static ast_mapaccess new_mapaccess(ast_id map, ast_expr key) {
		ast_mapaccess newMapAccess = new ast_mapaccess(map, key);
		assert (newMapAccess.keyExpr != null);
		return newMapAccess;
	}

}
