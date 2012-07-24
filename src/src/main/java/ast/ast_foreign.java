package ast;

import inc.lhs_list;

import java.util.Iterator;
import java.util.LinkedList;

import common.gm_apply;

public class ast_foreign extends ast_sent {
	public void dispose() {
		if (expr != null)
			expr.dispose();
	}

	public static ast_foreign new_foreign(ast_expr_foreign f) {
		ast_foreign S = new ast_foreign();
		S.set_expr(f);

		f.set_parent(S);
		return (S);
	}

	public static ast_foreign new_foreign_mutate(ast_expr_foreign f, lhs_list l) {
		ast_foreign S = new ast_foreign();
		S.set_expr(f);
		f.set_parent(S);
		// C++ TO JAVA CONVERTER WARNING: The following line was determined to
		// be a copy constructor call - this should be verified and a copy
		// constructor should be created if it does not yet exist:
		// ORIGINAL LINE: LinkedList<ast_node*>&L = l->LIST;
		LinkedList<ast_node> L = new LinkedList<ast_node>(l.LIST);
		Iterator<ast_node> I;
		for (I = L.iterator(); I.hasNext();) {
			ast_node n = I.next();
			n.set_parent(S);
			S.modified.addLast(n);
		}
		if (l != null)
			l.dispose();
		return (S);
	}

	public void reproduce(int ind_level) {
		expr.reproduce(ind_level);
		if (modified.size() > 0) {
			Out.push("::[");
			int cnt = 0;
			Iterator<ast_node> I;
			for (I = modified.begin(); I.hasNext(); I++, cnt++) {
				ast_node n = I.next();
				n.reproduce(ind_level);
				if (cnt != (int) (modified.size() - 1))
					Out.push(", ");
			}
			Out.push("]");
		}
		Out.pushln(";");
	}

	public void traverse_sent(gm_apply a, boolean is_post, boolean is_pre) {
		boolean for_id = a.is_for_id();
		boolean for_expr = a.is_for_expr();
		boolean for_lhs = a.is_for_lhs();
		boolean b = a.has_separate_post_apply();
		if (is_pre) {
			if (for_id) {
				Iterator<ast_node> I;
				for (I = modified.begin(); I.hasNext();) {
					if ((I.next()).get_nodetype() == AST_NODE_TYPE.AST_ID) {
						ast_id id = (ast_id) (I.next());
						a.apply(id);
					} else if ((I.next()).get_nodetype() == AST_NODE_TYPE.AST_FIELD) {
						ast_id id1 = ((ast_field) (I.next())).get_first();
						ast_id id2 = ((ast_field) (I.next())).get_second();
						a.apply(id1);
						a.apply(id2);
					}
				}
			}
			if (for_lhs) {
				Iterator<ast_node> I;
				for (I = modified.begin(); I.hasNext();) {
					if ((I.next()).get_nodetype() == AST_NODE_TYPE.AST_ID) {
						ast_id id = (ast_id) (I.next());
						a.apply_lhs(id);
					} else if ((I.next()).get_nodetype() == AST_NODE_TYPE.AST_FIELD) {
						ast_field f = ((ast_field) (I.next()));
						a.apply_lhs(f);
					}
				}
			}
			if (for_expr)
				a.apply(expr);
		}

		if (for_expr || for_id)
			expr.traverse(a, is_post, is_pre);

		if (is_post) {
			if (for_id) {
				Iterator<ast_node> I;
				for (I = modified.begin(); I.hasNext();) {
					if ((I.next()).get_nodetype() == AST_NODE_TYPE.AST_ID) {
						ast_id id = (ast_id) (I.next());
						if (b)
							a.apply2(id);
						else
							a.apply(id);
					} else if ((I.next()).get_nodetype() == AST_NODE_TYPE.AST_FIELD) {
						ast_id id1 = ((ast_field) (I.next())).get_first();
						ast_id id2 = ((ast_field) (I.next())).get_second();
						if (b) {
							a.apply2(id1);
							a.apply2(id2);
						} else {
							a.apply(id1);
							a.apply(id2);
						}
					}
				}
			}
			if (for_lhs) {
				Iterator<ast_node> I;
				for (I = modified.begin(); I.hasNext();) {
					if ((I.next()).get_nodetype() == AST_NODE_TYPE.AST_ID) {
						ast_id id = (ast_id) (I.next());
						if (b)
							a.apply_lhs2(id);
						else
							a.apply_lhs(id);
					} else if ((I.next()).get_nodetype() == AST_NODE_TYPE.AST_FIELD) {
						ast_field f = ((ast_field) (I.next()));
						if (b)
							a.apply_lhs2(f);
						else
							a.apply_lhs(f);
					}
				}
			}
			if (for_expr) {
				if (b)
					a.apply2(expr);
				else
					a.apply(expr);
			}
		}

	}

	@Override
	public void dump_tree(int id_level) {
	}

	public final LinkedList<ast_node> get_modified() {
		return modified;
	}

	public final ast_expr_foreign get_expr() {
		return expr;
	}

	public final void set_expr(ast_expr_foreign f) {
		expr = f;
	}

	private ast_foreign() {
		super(AST_NODE_TYPE.AST_FOREIGN);
		this.expr = null;
	}

	private ast_expr_foreign expr;
	private LinkedList<ast_node> modified = new LinkedList<ast_node>();
}