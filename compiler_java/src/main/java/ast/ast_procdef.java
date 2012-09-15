package ast;

import static ast.ast_node_type.AST_PROCDEF;

import java.util.LinkedList;

import common.gm_apply;
import common.gm_dumptree;
import common.gm_traverse;

//-------------------------------------------------------
// Procedure declaration
//-------------------------------------------------------
public class ast_procdef extends ast_node {

	private final LinkedList<ast_argdecl> in_args = new LinkedList<ast_argdecl>();
	private final LinkedList<ast_argdecl> out_args = new LinkedList<ast_argdecl>();

	private ast_id id = null;
	private ast_sentblock sents = null;
	private ast_typedecl ret_type = null;
	private boolean local = false;

	private ast_procdef() {
		super(AST_PROCDEF);
		create_symtabs();
	}

	@Override
	public void reproduce(int ind_level) {
		Out.push("Procedure ");
		id.reproduce(0);
		Out.push("(\n");

		int cnt = 0;
		int last = in_args.size();
		for (ast_argdecl d : in_args) {
			d.reproduce(ind_level);
			if (cnt != (last - 1))
				Out.push(",\n");
			cnt++;
		}

		last = out_args.size();
		if (last > 0) {
			Out.push(";\n");
			cnt = 0;
			for (ast_argdecl d : out_args) {
				d.reproduce(ind_level);
				if (cnt != (last - 1))
					Out.push(",\n");
				cnt++;
			}
		}
		Out.push(")");
		if ((ret_type != null) && (!ret_type.is_void())) {
			Out.push(" : ");
			ret_type.reproduce(0);
		}
		Out.NL();

		assert sents != null;
		sents.reproduce(ind_level);
		Out.NL();
		Out.flush();
	}

	@Override
	public void dump_tree(int ind_level) {
		gm_dumptree.IND(ind_level);
		System.out.print("[PROC ");
		id.dump_tree(0);
		System.out.print("\n");
		gm_dumptree.IND(ind_level);
		System.out.print(" IN:\n");
		for (ast_argdecl d : in_args) {
			d.dump_tree(ind_level + 1);
			System.out.print("\n");
		}
		System.out.print("\n");
		gm_dumptree.IND(ind_level);
		System.out.print(" OUT:\n");
		for (ast_argdecl d : in_args) {
			d.dump_tree(ind_level + 1);
			System.out.print("\n");
		}
		System.out.print("\n");
		gm_dumptree.IND(ind_level);
		System.out.print(" RET: ");
		if (ret_type == null)
			System.out.print("(void)");
		else {
			ret_type.dump_tree(0);
		}
		System.out.print("\n");
		sents.dump_tree(ind_level + 1);
		System.out.print("\n");
	}

	@Override
	public void traverse(gm_apply a, boolean is_post, boolean is_pre) {
		boolean for_symtab = a.is_for_symtab();
		boolean for_id = a.is_for_id();
		boolean for_proc = a.is_for_proc();

		a.begin_context(this);

		if (is_pre) {
			if (for_symtab)
				apply_symtabs(a, gm_traverse.PRE_APPLY);
			if (for_id)
				apply_id(a, gm_traverse.PRE_APPLY);
			if (for_proc)
				a.apply(this);
		}

		// traverse body
		((ast_sent) get_body()).traverse(a, is_post, is_pre);

		if (is_post) {
			if (for_symtab)
				apply_symtabs(a, gm_traverse.POST_APPLY);
			if (for_id)
				apply_id(a, gm_traverse.POST_APPLY);
			if (for_proc) {
				if (a.has_separate_post_apply())
					a.apply2(this);
				else
					a.apply(this);
			}
		}

		a.end_context(this);
	}

	public void apply_id(gm_apply a, boolean is_post) {
		// --------------------------
		// [todo] fix for name and return type
		// --------------------------
		// [todo] symbol-table for (name && signature, return type)
		// a->apply(get_procname());
		{
			LinkedList<ast_argdecl> args = get_in_args();
			for (ast_argdecl decl : args) {
				ast_idlist idl = decl.get_idlist();
				idl.apply_id(a, is_post);
			}
		}
		{
			LinkedList<ast_argdecl> args = get_out_args();
			for (ast_argdecl decl : args) {
				ast_idlist idl = decl.get_idlist();
				idl.apply_id(a, is_post);
			}
		}
	}

	@Override
	public boolean has_scope() {
		return true;
	}

	public static ast_procdef begin_new_procdef(ast_id id) {
		ast_procdef d = new ast_procdef();
		d.id = id;
		id.set_parent(d);
		return d;
	}

	public final void add_argdecl(ast_argdecl d) {
		in_args.addLast(d);
		d.set_parent(this);
	}

	public final void add_out_argdecl(ast_argdecl d) {
		out_args.addLast(d);
		d.set_parent(this);
	}

	public final void set_sentblock(ast_sentblock s) {
		sents = s;
		s.set_parent(this);
	}

	public final void set_return_type(ast_typedecl t) {
		ret_type = t;
		t.set_parent(this);
	}

	public final LinkedList<ast_argdecl> get_in_args() {
		return in_args;
	}

	public final LinkedList<ast_argdecl> get_out_args() {
		return out_args;
	}

	public final ast_sentblock get_body() {
		return sents;
	}

	public final ast_typedecl get_return_type() {
		return ret_type;
	}

	public final ast_id get_procname() {
		return id;
	}

	public final void set_local(boolean b) {
		local = b;
	}

	public final boolean is_local() {
		return local;
	}

}