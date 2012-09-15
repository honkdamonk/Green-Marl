package ast;

import inc.GMTYPE_T;

import common.gm_dumptree;

/**
 * access of node/edge property
 */
public class ast_field extends ast_node {

	private ast_id first = null;
	private ast_id second = null;
	private boolean rarrow = false;

	private ast_field() {
		super(ast_node_type.AST_FIELD);
	}

	private ast_field(ast_id l, ast_id f) {
		super(ast_node_type.AST_FIELD);
		first = l;
		second = f;
		first.set_parent(this);
		second.set_parent(this);
		line = first.get_line();
		col = first.get_col();
	}

	@Override
	public void reproduce(int ind_level) {
		first.reproduce(0);
		Out.push('.');
		second.reproduce(0);
	}

	@Override
	public void dump_tree(int ind_level) {
		assert parent != null;
		gm_dumptree.IND(ind_level);
		System.out.print("[");
		first.dump_tree(0);
		System.out.print(".");
		second.dump_tree(0);
		System.out.print("]");
	}

	public static ast_field new_field(ast_id l, ast_id f) {
		return new_field(l, f, false);
	}

	public static ast_field new_field(ast_id l, ast_id f, boolean is_r_arrow) {
		ast_field af = new ast_field(l, f);
		af.set_rarrow(is_r_arrow);
		return af;
	}

	// FIRST.SECOND
	public final ast_id get_first() // Identifier
	{
		return first;
	}

	public final ast_id get_second() // Field
	{
		return second;
	}

	public final boolean is_rarrow() // Is it Edge(x).y?
	{
		return rarrow;
	}

	public final void set_rarrow(boolean b) {
		rarrow = b;
	}

	// type information about source (node/edge/graph)
	public final GMTYPE_T getSourceTypeSummary() {
		return first.getTypeSummary();
	}

	public final ast_typedecl getSourceTypeInfo() {
		return first.getTypeInfo();
	}

	// type information about field (nodeprop/edgeprop)
	public final GMTYPE_T getTypeSummary() {
		return second.getTypeSummary();
	}

	public final ast_typedecl getTypeInfo() {
		return second.getTypeInfo();
	}

	// type information about target (primitive)
	public final GMTYPE_T getTargetTypeSummary() {
		return second.getTargetTypeSummary();
	}

	public final ast_typedecl getTargetTypeInfo() {
		return second.getTargetTypeInfo();
	}

	public final void set_first(ast_id f) {
		first = f;
		f.set_parent(this);
	}

	public final void set_second(ast_id s) {
		second = s;
		s.set_parent(this);
	}

	public final ast_field copy() {
		return copy(false);
	}

	public final ast_field copy(boolean cp_sym) {
		ast_id f = first.copy(cp_sym);
		ast_id s = second.copy(cp_sym);
		ast_field fld = new ast_field(f, s);
		return fld;
	}

}