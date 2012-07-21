import ast.ast_typedecl;

//------------------------------------------
// static scope
//------------------------------------------
public class gm_scope
{
	public gm_scope()
	{
		this.for_group_expr = false;
		this.G = null;
		this.node_prop = false;
		this.RT = null;
		this.tg = null;
	}
	public void dispose()
	{
	}

	public final void push_symtabs(gm_symtab v, gm_symtab f, gm_symtab p)
	{
		var_syms.addLast(v);
		field_syms.addLast(f);
		proc_syms.addLast(p);
	}
	public final void pop_symtabs()
	{
		var_syms.removeLast();
		field_syms.removeLast();
		proc_syms.removeLast();
	}
	public final gm_symtab get_varsyms()
	{
		return var_syms.getLast();
	}
	public final gm_symtab get_fieldsyms()
	{
		return field_syms.getLast();
	}
	public final gm_symtab get_procsyms()
	{
		return proc_syms.getLast();
	}

	public java.util.LinkedList<gm_symtab> var_syms = new java.util.LinkedList<gm_symtab>();
	public java.util.LinkedList<gm_symtab> field_syms = new java.util.LinkedList<gm_symtab>();
	public java.util.LinkedList<gm_symtab> proc_syms = new java.util.LinkedList<gm_symtab>();

	//----------------------------------------------------------------
	// temporary information, during typechecking
	//----------------------------------------------------------------
	// for group assignment
	private boolean for_group_expr;
	private gm_symtab_entry G; // target symbol for group-assignment
	private boolean node_prop; // true if n_p, false if e_p.

	// for retuern type-check
	private ast_typedecl RT; // return type

	// for target graph matching check
	private gm_symtab_entry tg;

	public final boolean is_for_group_expr()
	{
		return for_group_expr;
	}
	public final boolean is_for_node_prop()
	{
		return node_prop;
	}
	public final gm_symtab_entry get_target_sym()
	{
		return G;
	}
	public final void set_group_expr(boolean for_group, gm_symtab_entry g)
	{
		set_group_expr(for_group, g, false);
	}
	public final void set_group_expr(boolean for_group)
	{
		set_group_expr(for_group, null, false);
	}
//C++ TO JAVA CONVERTER NOTE: Java does not allow default values for parameters. Overloaded methods are inserted above.
//ORIGINAL LINE: void set_group_expr(boolean for_group, gm_symtab_entry *g = null, boolean np = false)
	public final void set_group_expr(boolean for_group, gm_symtab_entry g, boolean np)
	{
		for_group_expr = for_group;
		G = g;
		node_prop = np;
	}

	public final void set_return_type(ast_typedecl R)
	{
		RT = R;
	}
	public final ast_typedecl get_return_type()
	{
		return RT;
	}

	public final void set_target_graph(gm_symtab_entry e)
	{
		tg = e;
	}
	public final gm_symtab_entry get_target_graph()
	{
		return tg;
	}
}