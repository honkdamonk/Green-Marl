package ast;

import static ast.ast_node_type.AST_IDLIST;

import java.util.ArrayList;

import common.gm_apply;
import common.gm_dumptree;

public class ast_idlist extends ast_node {

	private ArrayList<ast_id> lst = new ArrayList<ast_id>();

	public ast_idlist() {
		super(AST_IDLIST);
	}

	public final ast_idlist copy() {
		return copy(false);
	}

	public final ast_idlist copy(boolean cp_sym) {
		ast_idlist cpy = new ast_idlist();
		for (int i = 0; i < (int) lst.size(); i++)
			cpy.add_id(lst.get(i).copy(cp_sym));
		return cpy;
	}

	public final void add_id(ast_id id) {
		lst.add(id);
	}

	public final int get_length() {
		return lst.size();
	}

	public final ast_id get_item(int i) {
		return lst.get(i);
	}

	public void apply_id(gm_apply a, boolean is_post_apply) {
		for (int i = 0; i < get_length(); i++) {
			ast_id id = get_item(i);
			if (is_post_apply && a.has_separate_post_apply())
				a.apply2(id);
			else
				a.apply(id);
		}
	}

	@Override
	public void reproduce(int ind_level) {
		for (int i = 0; i < (int) lst.size(); i++) {
			ast_id id = lst.get(i);
			id.reproduce(0);
			if (i != (int) (lst.size() - 1))
				Out.push(", ");
		}
	}

	@Override
	public void dump_tree(int ind_lv) {
		assert parent != null;
		gm_dumptree.IND(ind_lv);
		System.out.print("[");
		for (int i = 0; i < (int) lst.size(); i++) {
			ast_id id = lst.get(i);
			id.dump_tree(0);
			System.out.print(" ");
		}
		System.out.print("]");
	}

	public final boolean contains(String id) {
		for (int i = 0; i < (int) lst.size(); i++)
			if (id.equals(lst.get(i).get_orgname()))
				return true;
		return false;
	}

	@Override
	public final int get_line() {
		assert get_length() > 0;
		return lst.get(0).get_line();
	}

	@Override
	public final int get_col() {
		assert get_length() > 0;
		return lst.get(0).get_col();
	}

}