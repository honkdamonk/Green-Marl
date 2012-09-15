package common;

import ast.ast_bfs;
import ast.ast_expr;
import ast.ast_expr_builtin;
import ast.ast_field;
import ast.ast_id;
import ast.ast_node;
import ast.ast_procdef;
import ast.ast_sent;
import frontend.symtab_types;
import frontend.gm_symtab;
import frontend.gm_symtab_entry;

// codes for traverse

public abstract class gm_apply {

	protected boolean for_id = false;
	protected boolean for_symtab = false;
	protected boolean for_sent = false;
	protected boolean for_expr = false;
	protected boolean for_proc = false;
	protected boolean for_lhs = false;
	protected boolean for_rhs = false;
	protected boolean for_builtin = false;
	protected boolean separate_post_apply = false;
	protected boolean traverse_local_expr_only = false;
	protected ast_sent curr_sent_being_traversed = null; // [xxx] who sets up
															// this?
	protected ast_node matching_lhs = null;
	protected ast_expr matching_rhs = null;

	// SYMTAB_ARG, SYMTAB_FIELD, SYMTAB_VAR, SYMTAB_PROC
	public boolean apply(gm_symtab_entry gm_symtab_entry, symtab_types symtab_type) {
		return true;
	}

	public boolean apply(gm_symtab e, symtab_types symtabType) {
		return true;
	}

	public boolean apply(ast_id e) {
		return true;
	}

	public boolean apply(ast_sent s) {
		return true;
	}

	public boolean apply(ast_expr e) {
		return true;
	}

	public boolean apply(ast_procdef s) {
		return true;
	}

	public void begin_context(ast_node n) {
		return;
	}

	public void end_context(ast_node n) {
		return;
	}

	public boolean apply_lhs(ast_id e) {
		return true;
	}

	public boolean apply_lhs(ast_field e) {
		return true;
	}

	public boolean apply_rhs(ast_id e) {
		return true;
	}

	public boolean apply_rhs(ast_field e) {
		return true;
	}

	public boolean apply_builtin(ast_expr_builtin e) {
		return true;
	}

	// SYMTAB_ARG, SYMTAB_FIELD, SYMTAB_VAR, SYMTAB_PROC
	public boolean apply2(gm_symtab_entry gm_symtab_entry, symtab_types symtab_type) {
		return true;
	}

	public boolean apply2(gm_symtab e, symtab_types symtabType) {
		return true;
	}

	public boolean apply2(ast_id e) {
		return true;
	}

	public boolean apply2(ast_sent s) {
		return true;
	}

	public boolean apply2(ast_expr e) {
		return true;
	}

	public boolean apply2(ast_procdef s) {
		return true;
	}

	public boolean apply_lhs2(ast_id e) {
		return true;
	}

	public boolean apply_lhs2(ast_field e) {
		return true;
	}

	public boolean apply_rhs2(ast_id e) {
		return true;
	}

	public boolean apply_rhs2(ast_field e) {
		return true;
	}

	public boolean apply_builtin2(ast_expr_builtin e) {
		return true;
	}

	// (for bfs iteration)
	public boolean begin_traverse_reverse(ast_bfs bfs) {
		return true;
	}

	public boolean end_traverse_reverse(ast_bfs bfs) {
		return true;
	}

	// (should be called inside apply_lhs or apply_rhs of assignment
	public ast_node get_matching_lhs() {
		return matching_lhs;
	}

	public ast_node get_matching_rhs_top() {
		return matching_rhs;
	}

	// used by traversal engine
	public final void set_matching_lhs(ast_node n) {
		matching_lhs = n;
	}

	public final void set_matching_rhs_top(ast_expr n) {
		matching_rhs = n;
	}

	public final boolean is_for_id() {
		return for_id;
	}

	public final boolean is_for_symtab() {
		return for_symtab;
	}

	public final boolean is_for_sent() {
		return for_sent;
	}

	public final boolean is_for_expr() {
		return for_expr;
	}

	public final boolean is_for_proc() {
		return for_proc;
	}

	public final boolean is_for_lhs() {
		return for_lhs;
	}

	public final boolean is_for_rhs() {
		return for_rhs;
	}

	public final boolean is_for_builtin() {
		return for_builtin;
	}

	public final void set_for_id(boolean b) {
		for_id = b;
	}

	public final void set_for_symtab(boolean b) {
		for_symtab = b;
	}

	public final void set_for_sent(boolean b) {
		for_sent = b;
	}

	public final void set_for_expr(boolean b) {
		for_expr = b;
	}

	public final void set_for_proc(boolean b) {
		for_proc = b;
	}

	public final void set_for_lhs(boolean b) {
		for_lhs = b;
	}

	public final void set_for_rhs(boolean b) {
		for_rhs = b;
	}

	public final void set_for_builtin(boolean b) {
		for_builtin = b;
	}

	public final boolean has_separate_post_apply() {
		return separate_post_apply;
	}

	public final void set_separate_post_apply(boolean b) {
		separate_post_apply = b;
	}

	public final boolean is_traverse_local_expr_only() {
		return traverse_local_expr_only;
	}

	public final void set_traverse_local_expr_only(boolean b) {
		traverse_local_expr_only = b;
	} // what is this for?

	public final ast_sent get_current_sent() {
		return curr_sent_being_traversed;
	}

	public final void set_current_sent(ast_sent s) {
		curr_sent_being_traversed = s;
	}

	public final void set_all(boolean b) {
		for_id = for_sent = for_expr = for_symtab = b;
	}

}
// ----------------------------------------------------------------------------------
// [begin_context]
// apply(sent)
// apply(symtab_entry)
// ... per sentence-subtype pre: (expr, id, ...)
// ...... per sentence-subtype recursive traverse
// ... per sentence-subtype post: (expr, id, ...)
// apply2(symtab_entry)
// apply2(sent)
// [end_context]
// ----------------------------------------------------------------------------------

