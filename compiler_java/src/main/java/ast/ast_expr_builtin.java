package ast;

import static ast.AST_NODE_TYPE.AST_EXPR_BUILTIN;
import inc.GMEXPR_CLASS;
import inc.GMTYPE_T;
import inc.expr_list;

import java.util.LinkedList;

import common.gm_apply;
import common.gm_builtin_def;
import common.gm_dumptree;

public class ast_expr_builtin extends ast_expr {

	protected ast_id driver; // canbe null
	protected String orgname;
	protected LinkedList<ast_expr> args = new LinkedList<ast_expr>();
	protected gm_builtin_def def;

	protected ast_expr_builtin() {
		super();
		this.driver = null;
		this.orgname = null;
		this.def = null;
		set_nodetype(AST_EXPR_BUILTIN);
	}

	@Override
	public void reproduce(int ind_level) {
		if (this.get_opclass() == GMEXPR_CLASS.GMEXPR_ID) {
			id1.reproduce(0);
			return;
		}

		if (driver != null) {
			driver.reproduce(0);
			Out.push('.');
		}
		assert orgname != null;
		Out.push(orgname);
		int cnt = 0;
		int size = args.size();
		Out.push('(');
		for (ast_expr e : args) {
			e.reproduce(0);
			if (cnt != (size - 1))
				Out.push(',');
			cnt++;
		}
		Out.push(')');
	}

	@Override
	public void dump_tree(int ind_level) {
		gm_dumptree.IND(ind_level);
		System.out.print("[ ");
		if (driver == null)
			System.out.print("_");
		else
			driver.dump_tree(0);
		System.out.printf(".%s (", orgname);
		if (args.size() > 0) {
			System.out.print("\n");
			for (ast_expr e : args) {
				e.dump_tree(ind_level + 1);
				System.out.print("\n");
			}
			gm_dumptree.IND(ind_level);
			System.out.print(")]");
		} else
			System.out.print(")]");
	}

	@Override
	public void traverse(gm_apply a, boolean is_post, boolean is_pre) {
		boolean for_id = a.is_for_id();
		boolean for_expr = a.is_for_expr();
		boolean for_rhs = a.is_for_rhs();
		boolean for_builtin = a.is_for_builtin();

		if (is_pre) {
			if (for_id && (driver != null))
				a.apply(driver);
			if (for_rhs && (driver != null))
				a.apply_rhs(driver);
			if (for_builtin)
				a.apply(this);
			if (for_expr)
				a.apply(this);
		}

		// built-in arguments are always rhs
		for (ast_expr e : args) {
			e.traverse(a, is_post, is_pre);
		}

		if (is_post) {
			boolean b = a.has_separate_post_apply();
			if (for_id && (driver != null)) {
				if (b)
					a.apply2(driver);
				else
					a.apply(driver);
			}
			if (for_rhs && (driver != null)) {
				if (b)
					a.apply_rhs2(driver);
				else
					a.apply_rhs(driver);
			}
			if (for_builtin) {
				if (b)
					a.apply_builtin2(this);
				else
					a.apply_builtin(this);
			}
			if (for_expr) {
				if (b)
					a.apply2(this);
				else
					a.apply(this);
			}
		}
	}

	@Override
	public ast_expr copy(boolean b) {
		expr_list T = new expr_list();
		for (ast_expr e : args) {
			ast_expr e2 = e.copy(b);
			T.LIST.addLast(e2);
		}

		ast_expr_builtin e = ast_expr_builtin.new_builtin_expr(driver.copy(b), orgname, T);
		e.set_type_summary(this.get_type_summary());
		return e;
	}

	public boolean driver_is_field() {
		return false;
	}

	public GMTYPE_T get_source_type() {
		return (driver == null) ? GMTYPE_T.GMTYPE_VOID : driver.getTypeSummary();
	}

	public static ast_expr_builtin new_builtin_expr(ast_id id, String orgname, expr_list t) {
		ast_expr_builtin E = new ast_expr_builtin();
		E.expr_class = GMEXPR_CLASS.GMEXPR_BUILTIN;
		E.driver = id;
		if (id != null) // type unknown yet.
			id.set_parent(E);
		assert orgname != null;
		E.orgname = orgname;
		if (t != null) {
			E.args = new LinkedList<ast_expr>(t.LIST); // shallow copy
														// LIST
			// but not set 'up' pointer.
			for (ast_expr e : E.args)
				e.set_parent(E);
		}
		return E;
	}

	public static ast_expr_builtin new_builtin_expr(ast_id id, gm_builtin_def d, expr_list t) {
		ast_expr_builtin E = new ast_expr_builtin();
		E.expr_class = GMEXPR_CLASS.GMEXPR_BUILTIN;
		E.driver = id;
		if (id != null) // type unknown yet.
			id.set_parent(E);
		E.def = d;
		assert d.get_orgname() != null;
		E.orgname = d.get_orgname();
		if (t != null) {
			E.args = t.LIST; // shallow copy LIST
			// but not set 'up' pointer.
			for (ast_expr e : E.args)
				e.set_parent(E);
		}
		return E;
	}

	public final String get_orgname() {
		return orgname;
	}

	public final String get_callname() {
		return orgname;
	}

	public ast_id get_driver() {
		return driver;
	}

	public final void set_driver(ast_id i) {
		driver = i;
		i.set_parent(this);
	}

	public final LinkedList<ast_expr> get_args() {
		return args;
	}

	public final gm_builtin_def get_builtin_def() {
		return def;
	}

	public final void set_builtin_def(gm_builtin_def d) {
		def = d;
	}

}