package ast;

import inc.GM_REDUCE_T;
import inc.gm_assignment_location_t;
import inc.gm_assignment_t;

import java.util.Iterator;
import java.util.LinkedList;

import common.gm_apply;
import common.gm_dumptree;

public class ast_assign extends ast_sent {
	
	private gm_assignment_t assign_type; // normal, deferred, reduce
	private gm_assignment_location_t lhs_type; // scalar, field
	private GM_REDUCE_T reduce_type; // add, mult, min, max
	private ast_id lhs_scala;
	private ast_field lhs_field;
	private ast_expr rhs;
	private ast_id bound; // bounding iterator

	private boolean arg_minmax;
	private boolean isReference = false;

	private LinkedList<ast_node> l_list = new LinkedList<ast_node>();
	private LinkedList<ast_expr> r_list = new LinkedList<ast_expr>();
	
	public void dispose() {
		if (lhs_scala != null)
			lhs_scala.dispose();
		if (lhs_field != null)
			lhs_field.dispose();
		if (rhs != null)
			rhs.dispose();
		if (bound != null)
			bound.dispose();
	}

	public static ast_assign new_assign_scala(ast_id id, ast_expr r, gm_assignment_t assign_type, ast_id itor) {
		return new_assign_scala(id, r, assign_type, itor, GM_REDUCE_T.GMREDUCE_NULL);
	}

	public static ast_assign new_assign_scala(ast_id id, ast_expr r, gm_assignment_t assign_type) {
		return new_assign_scala(id, r, assign_type, null, GM_REDUCE_T.GMREDUCE_NULL);
	}

	public static ast_assign new_assign_scala(ast_id id, ast_expr r) {
		return new_assign_scala(id, r, gm_assignment_t.GMASSIGN_NORMAL, null, GM_REDUCE_T.GMREDUCE_NULL);
	}

	public static ast_assign new_assign_scala(ast_id id, ast_expr r, gm_assignment_t assign_type, ast_id itor, GM_REDUCE_T reduce_type) {
		// assign to scala
		ast_assign A = new ast_assign();
		A.lhs_scala = id;
		A.rhs = r;
		id.set_parent(A);
		r.set_parent(A);
		A.lhs_type = gm_assignment_location_t.GMASSIGN_LHS_SCALA;
		if (itor != null) {
			itor.set_parent(A);
		}
		A.bound = itor;
		A.assign_type = assign_type; // normal, reduced, or deferred
		A.reduce_type = reduce_type; // reduce or defer type
		return A;
	}

	public static ast_assign new_assign_field(ast_field id, ast_expr r, gm_assignment_t assign_type, ast_id itor) {
		return new_assign_field(id, r, assign_type, itor, GM_REDUCE_T.GMREDUCE_NULL);
	}

	public static ast_assign new_assign_field(ast_field id, ast_expr r, gm_assignment_t assign_type) {
		return new_assign_field(id, r, assign_type, null, GM_REDUCE_T.GMREDUCE_NULL);
	}

	public static ast_assign new_assign_field(ast_field id, ast_expr r) {
		return new_assign_field(id, r, gm_assignment_t.GMASSIGN_NORMAL, null, GM_REDUCE_T.GMREDUCE_NULL);
	}

	// C++ TO JAVA CONVERTER NOTE: Java does not allow default values for
	// parameters. Overloaded methods are inserted above.
	// ORIGINAL LINE: static ast_assign* new_assign_field(ast_field* id,
	// ast_expr* r, int assign_type = GMASSIGN_NORMAL, ast_id* itor = null, int
	// reduce_type = GMREDUCE_NULL)
	public static ast_assign new_assign_field(ast_field id, ast_expr r, gm_assignment_t assign_type, ast_id itor, GM_REDUCE_T reduce_type) {
		// assign to property
		ast_assign A = new ast_assign();
		A.lhs_field = id;
		A.rhs = r;
		id.set_parent(A);
		r.set_parent(A);
		A.lhs_type = gm_assignment_location_t.GMASSIGN_LHS_FIELD;
		if (itor != null) {
			itor.set_parent(A);
		}
		A.bound = itor;
		A.assign_type = assign_type;
		A.reduce_type = reduce_type;
		return A;
	}

	public void reproduce(int ind_level) {
		boolean argmin = is_argminmax_assign();

		if (argmin) {
			Out.push('<');
		}
		if (lhs_type == gm_assignment_location_t.GMASSIGN_LHS_SCALA) {
			lhs_scala.reproduce(0);
		} else if (lhs_type == gm_assignment_location_t.GMASSIGN_LHS_FIELD) {
			lhs_field.reproduce(0);
		}

		if (argmin) {
			Out.push(" ; ");
			LinkedList<ast_node> L = get_lhs_list();
			int cnt = 0;
			int last = L.size();
			for (ast_node n : L) {
				if (n.get_nodetype() == AST_NODE_TYPE.AST_FIELD) {
					ast_field f = (ast_field) n;
					f.reproduce(0);
				} else {
					assert n.get_nodetype() == AST_NODE_TYPE.AST_ID;
					ast_id i = (ast_id) n;
					i.reproduce(0);
				}
				if (cnt != (last - 1)) {
					Out.push(", ");
				}
				cnt++;
			}

			Out.push('>');
		}

		if (assign_type == gm_assignment_t.GMASSIGN_NORMAL) {
			Out.push(" = ");
		} else if (assign_type == gm_assignment_t.GMASSIGN_REDUCE) {
			Out.SPC();
			Out.push(reduce_type.get_reduce_string());
			Out.SPC();
		} else if (assign_type == gm_assignment_t.GMASSIGN_DEFER) {
			Out.push(" <= ");
		} else {
			assert false;
		}

		if (argmin) {
			Out.push('<');
		}
		rhs.reproduce(0);

		if (argmin) {
			Out.push(" ; ");
			LinkedList<ast_expr> L = get_rhs_list();
			int cnt = 0;
			int last = L.size();
			for (ast_expr n : L) {
				n.reproduce(0);
				if (cnt != (last - 1))
					Out.push(", ");
				cnt++;
			}
			Out.push('>');
		}

		if ((assign_type == gm_assignment_t.GMASSIGN_REDUCE) || (assign_type == gm_assignment_t.GMASSIGN_DEFER)) {
			if (bound != null) {
				Out.push(" @ ");
				bound.reproduce(0);
				Out.SPC();
			}
		}
		Out.pushln(";");
	}

	public void traverse_sent(gm_apply a, boolean is_post, boolean is_pre) {
		boolean for_id = a.is_for_id();

		if (is_pre) {
			if (for_id) {
				if (get_lhs_type() == gm_assignment_location_t.GMASSIGN_LHS_SCALA) {
					a.apply(get_lhs_scala());
				} // LHS_FIELD
				else {
					a.apply(get_lhs_field().get_first());
					a.apply(get_lhs_field().get_second());
				}
				if (get_bound() != null) // REDUCE or DEFER
					a.apply(get_bound());

				if (is_argminmax_assign()) {
					for (ast_node n : l_list) {
						if (n.get_nodetype() == AST_NODE_TYPE.AST_ID) {
							ast_id i = (ast_id) n;
							a.apply(i);
						} else {
							ast_field f = (ast_field) n;
							a.apply(f.get_first());
							a.apply(f.get_second());
						}
					}
				}
			}
		}

		boolean for_rhs = a.is_for_rhs();
		boolean for_lhs = a.is_for_lhs();

		if (for_lhs || for_rhs) {
			a.set_matching_lhs((get_lhs_type() == gm_assignment_location_t.GMASSIGN_LHS_SCALA) ? (ast_node) get_lhs_scala() : (ast_node) get_lhs_field());

			a.set_matching_rhs_top(get_rhs());
		}

		if (is_pre && for_lhs) {
			if (get_lhs_type() == gm_assignment_location_t.GMASSIGN_LHS_SCALA) {
				a.apply_lhs(get_lhs_scala());
			} // LHS_FIELD
			else {
				a.apply_lhs(get_lhs_field());
			}
		}
		get_rhs().traverse(a, is_post, is_pre);
		if (is_post && for_lhs) {
			if (get_lhs_type() == gm_assignment_location_t.GMASSIGN_LHS_SCALA) {
				if (a.has_separate_post_apply())
					a.apply_lhs2(get_lhs_scala());
				else
					a.apply_lhs(get_lhs_scala());
			} // LHS_FIELD
			else {
				if (a.has_separate_post_apply())
					a.apply_lhs2(get_lhs_field());
				else
					a.apply_lhs(get_lhs_field());
			}
		}

		if (is_argminmax_assign()) {
			Iterator<ast_node> J = l_list.iterator();
			Iterator<ast_expr> I = r_list.iterator();
			while (I.hasNext()) {
				ast_node n = J.next();
				ast_expr e = I.next();
				if (for_lhs || for_rhs) {
					a.set_matching_lhs(n);
					a.set_matching_rhs_top(e);
				}

				if (is_pre && for_lhs) {
					if (n.get_nodetype() == AST_NODE_TYPE.AST_ID) {
						a.apply_lhs((ast_id) n);
					} else {
						a.apply_lhs((ast_field) n);
					}
				}
				e.traverse(a, is_post, is_pre);
				if (is_post && for_lhs) {
					if (a.has_separate_post_apply()) {
						if (n.get_nodetype() == AST_NODE_TYPE.AST_ID) {
							a.apply_lhs2((ast_id) n);
						} else {
							a.apply_lhs2((ast_field) n);
						}
					} else {
						if (n.get_nodetype() == AST_NODE_TYPE.AST_ID) {
							a.apply_lhs((ast_id) n);
						} else {
							a.apply_lhs((ast_field) n);
						}

					}
				}
			}
		}

		if (is_post) {
			boolean b = a.has_separate_post_apply();
			if (for_id) {
				if (get_lhs_type() == gm_assignment_location_t.GMASSIGN_LHS_SCALA) {
					if (b)
						a.apply2(get_lhs_scala());
					else
						a.apply(get_lhs_scala());
				} // LHS_FIELD
				else {
					if (b) {
						a.apply2(get_lhs_field().get_first());
						a.apply2(get_lhs_field().get_second());
					} else {
						a.apply(get_lhs_field().get_first());
						a.apply(get_lhs_field().get_second());
					}
				}
				if (get_bound() != null) // REDUCE or DEFER
				{
					if (b)
						a.apply2(get_bound());
					else
						a.apply(get_bound());
				}
				if (is_argminmax_assign()) {
					for (ast_node n : l_list) {
						if (n.get_nodetype() == AST_NODE_TYPE.AST_ID) {
							ast_id i = (ast_id) n;
							if (b)
								a.apply2(i);
							else
								a.apply(i);
						} else {
							ast_field f = (ast_field) n;
							if (b) {
								a.apply2(f.get_first());
								a.apply2(f.get_second());
							} else {
								a.apply(f.get_first());
								a.apply(f.get_second());
							}
						}
					}
				}
			}
		}

		if (for_lhs || for_rhs) {
			a.set_matching_lhs(null);
			a.set_matching_rhs_top(null);
		}
	}

	public void dump_tree(int ind_level) {
		gm_dumptree.IND(ind_level);
		assert parent != null;

		if (assign_type == gm_assignment_t.GMASSIGN_NORMAL) {
			System.out.print("[ASSIGN ");
		} else if (assign_type == gm_assignment_t.GMASSIGN_REDUCE) {
			System.out.print("[ASSIGN_REDUCE ");
			System.out.printf(" <%s @ ", reduce_type.get_reduce_string());
			if (bound == null)
				System.out.print("(NULL)>  ");
			else {
				bound.dump_tree(0);
				System.out.print("> ");
			}
		} else
			assert false;

		if (lhs_type == gm_assignment_location_t.GMASSIGN_LHS_SCALA) {
			lhs_scala.dump_tree(0);
		} else if (lhs_type == gm_assignment_location_t.GMASSIGN_LHS_FIELD) {
			lhs_field.dump_tree(0);
		}
		System.out.print("\n");
		rhs.dump_tree(ind_level + 1);
		System.out.print("\n");
		gm_dumptree.IND(ind_level);
		System.out.print("]");
	}

	public final gm_assignment_t get_assign_type() {
		return assign_type;
	}

	public final gm_assignment_location_t get_lhs_type() {
		return lhs_type;
	}

	public final GM_REDUCE_T get_reduce_type() {
		return reduce_type;
	}

	public final void set_assign_type(gm_assignment_t a) {
		assign_type = a;
	}

	public final void set_reduce_type(GM_REDUCE_T a) {
		reduce_type = a;
	}

	public final ast_id get_lhs_scala() {
		return lhs_scala;
	}

	public final ast_field get_lhs_field() {
		return lhs_field;
	}

	public final ast_expr get_rhs() {
		return rhs;
	}

	public final ast_id get_bound() {
		return bound;
	}

	public final void set_bound(ast_id i) {
		bound = i;
		if (bound != null)
			i.set_parent(this);
	}

	public final boolean is_reduce_assign() {
		return assign_type == gm_assignment_t.GMASSIGN_REDUCE;
	}

	public final boolean is_defer_assign() {
		return assign_type == gm_assignment_t.GMASSIGN_DEFER;
	}

	public final boolean is_target_scalar() {
		return get_lhs_type() == gm_assignment_location_t.GMASSIGN_LHS_SCALA;
	}

	public final void set_rhs(ast_expr r) {
		rhs = r;
		rhs.set_parent(this);
	}

	public final boolean is_argminmax_assign() {
		return arg_minmax;
	}

	public final void set_argminmax_assign(boolean b) {
		arg_minmax = b;
	}

	public final boolean has_lhs_list() {
		return l_list.size() > 0;
	}

	public final LinkedList<ast_node> get_lhs_list() {
		return l_list;
	}

	public final LinkedList<ast_expr> get_rhs_list() {
		return r_list;
	}

	public final void set_lhs_list(LinkedList<ast_node> L) {
		l_list = new LinkedList<ast_node>(L);
	}

	public final void set_rhs_list(LinkedList<ast_expr> R) {
		r_list = new LinkedList<ast_expr>(R);
	}

	public final void set_lhs_scala(ast_id new_id) {
		lhs_scala = new_id;
		if (new_id != null)
			lhs_type = gm_assignment_location_t.GMASSIGN_LHS_SCALA;
	}

	public final void set_lhs_field(ast_field new_id) {
		lhs_field = new_id;
		if (new_id != null)
			lhs_type = gm_assignment_location_t.GMASSIGN_LHS_FIELD;
	}
	
	public final boolean is_reference() {
		return isReference;
	}
	
	public final void set_is_reference(boolean isRef) {
		isReference = isRef;
	}

	private ast_assign() {
		super(AST_NODE_TYPE.AST_ASSIGN);
		this.lhs_scala = null;
		this.lhs_field = null;
		this.rhs = null;
		this.bound = null;
		this.arg_minmax = false;
		this.lhs_type = gm_assignment_location_t.GMASSIGN_LHS_SCALA;
		this.assign_type = gm_assignment_t.GMASSIGN_NORMAL;
		this.reduce_type = GM_REDUCE_T.GMREDUCE_INVALID;
	}

}