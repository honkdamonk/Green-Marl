package common;

import ast.ast_expr;
import ast.ast_node;
import ast.ast_procdef;
import ast.ast_sent;
import frontend.SYMTAB_TYPES;
import frontend.gm_symtab;
import frontend.gm_symtab_entry;

public class gm_traverse
{

	// [todo make stop traverse]

	public static final boolean GM_POST_APPLY = true;
	public static final boolean GM_PRE_APPLY = false;
public static void gm_traverse_ids(ast_procdef top, gm_apply a)
{
	gm_traverse_ids(top, a, false);
}

	//--------------------------------------------------------------------
	// traverse all the ids in the procedure ast and apply operation 
	// (except ids in the symtab)
	// This should be called after type-check is finished.
	//--------------------------------------------------------------------
//C++ TO JAVA CONVERTER NOTE: Java does not allow default values for parameters. Overloaded methods are inserted above.
//ORIGINAL LINE: static void gm_traverse_ids(ast_procdef* top, gm_apply* a, boolean is_post_apply = false)
	public static void gm_traverse_ids(ast_procdef top, gm_apply a, boolean is_post_apply)
	{
		a.set_all(false);
		a.set_for_id(true);
		top.traverse(a, is_post_apply, !is_post_apply);
	}
public static void gm_traverse_sents(ast_procdef top, gm_apply a)
{
	gm_traverse_sents(top, a, false);
}

	//--------------------------------------------------------------------
	// traverse all the sentences in the procedure and apply operation
	//--------------------------------------------------------------------
//C++ TO JAVA CONVERTER NOTE: Java does not allow default values for parameters. Overloaded methods are inserted above.
//ORIGINAL LINE: static void gm_traverse_sents(ast_procdef* top, gm_apply *a, boolean is_post_apply = false)
	public static void gm_traverse_sents(ast_procdef top, gm_apply a, boolean is_post_apply)
	{
		a.set_all(false);
		a.set_for_sent(true);
		top.traverse(a, is_post_apply, !is_post_apply);
	}
public static void gm_traverse_sents(ast_sent top, gm_apply a)
{
	gm_traverse_sents(top, a, false);
}
//C++ TO JAVA CONVERTER NOTE: Java does not allow default values for parameters. Overloaded methods are inserted above.
//ORIGINAL LINE: static void gm_traverse_sents(ast_sent* top, gm_apply *a, boolean is_post_apply = false)
	public static void gm_traverse_sents(ast_sent top, gm_apply a, boolean is_post_apply)
	{
		a.set_all(false);
		a.set_for_sent(true);
		top.traverse(a, is_post_apply, !is_post_apply);
	}

	public static void gm_traverse_sents_pre_post(ast_procdef top, gm_apply a)
	{
		a.set_all(false);
		a.set_for_sent(true);
		a.set_separate_post_apply(true);
		top.traverse(a, true, true);
	}

	// traverse and apply two times (pre/post)
	public static void gm_traverse_sents_pre_post(ast_sent top, gm_apply a)
	{
		a.set_all(false);
		a.set_for_sent(true);
		a.set_separate_post_apply(true);
		top.traverse(a, true, true);
	}
public static void gm_traverse_symtabs(ast_procdef top, gm_apply a)
{
	gm_traverse_symtabs(top, a, false);
}

	//--------------------------------------------------------------------
	// traverse all the symtabs in the procedure ast
	//--------------------------------------------------------------------
//C++ TO JAVA CONVERTER NOTE: Java does not allow default values for parameters. Overloaded methods are inserted above.
//ORIGINAL LINE: static void gm_traverse_symtabs(ast_procdef* top, gm_apply* a, boolean is_post_apply = false)
	public static void gm_traverse_symtabs(ast_procdef top, gm_apply a, boolean is_post_apply)
	{
		a.set_all(false);
		a.set_for_symtab(true);
		top.traverse(a, is_post_apply, !is_post_apply);
	}
public static void gm_traverse_symtabs(ast_sent top, gm_apply a)
{
	gm_traverse_symtabs(top, a, false);
}
//C++ TO JAVA CONVERTER NOTE: Java does not allow default values for parameters. Overloaded methods are inserted above.
//ORIGINAL LINE: static void gm_traverse_symtabs(ast_sent* top, gm_apply* a, boolean is_post_apply = false)
	public static void gm_traverse_symtabs(ast_sent top, gm_apply a, boolean is_post_apply)
	{
		a.set_all(false);
		a.set_for_symtab(true);
		top.traverse(a, is_post_apply, !is_post_apply);
	}
public static void gm_traverse_exprs(ast_procdef top, gm_apply a)
{
	gm_traverse_exprs(top, a, false);
}

	//--------------------------------------------------------------------
	// traverse all the exprs in the procedure ast
	//--------------------------------------------------------------------
//C++ TO JAVA CONVERTER NOTE: Java does not allow default values for parameters. Overloaded methods are inserted above.
//ORIGINAL LINE: static void gm_traverse_exprs(ast_procdef* top, gm_apply* a, boolean is_post_apply = false)
	public static void gm_traverse_exprs(ast_procdef top, gm_apply a, boolean is_post_apply)
	{
		a.set_all(false);
		a.set_for_expr(true);
		top.traverse(a, is_post_apply, !is_post_apply);
	}
public static void gm_traverse_exprs(ast_expr top, gm_apply a)
{
	gm_traverse_exprs(top, a, false);
}
//C++ TO JAVA CONVERTER NOTE: Java does not allow default values for parameters. Overloaded methods are inserted above.
//ORIGINAL LINE: static void gm_traverse_exprs(ast_expr* top, gm_apply* a, boolean is_post_apply = false)
	public static void gm_traverse_exprs(ast_expr top, gm_apply a, boolean is_post_apply)
	{
		a.set_all(false);
		a.set_for_expr(true);
		top.traverse(a, is_post_apply, !is_post_apply);
	}

// traverse all the sentences, upward.

	//-------------------------------------------------------------
	// travese up the nodes and apply a to every sent in place
	// return true, if it reached to the top 'null'
	// return false, if traverse stopped in the middle by apply
	//-------------------------------------------------------------
	public static boolean gm_traverse_up_sent(ast_node n, gm_apply a)
	{
		if (n == null)
			return true;

		else if (n.is_sentence())
		{
			boolean b = a.apply((ast_sent) n);
			if (!b)
				return false;
		}

		return gm_traverse.gm_traverse_up_sent(n.get_parent(), a);
	}

	public static final boolean POST_APPLY = true;
	public static final boolean PRE_APPLY = false;
	public static void apply_symtab_each(gm_apply a, gm_symtab s, SYMTAB_TYPES symtab_type, boolean is_post)
	{
		java.util.HashSet<gm_symtab_entry> v = s.get_entries();
		for (gm_symtab_entry entry : v)
		{
			if (is_post && a.has_separate_post_apply())
			{
				a.apply2(entry, symtab_type);
			}
			else
			{
				a.apply(entry, symtab_type);
			}
		}
	}
}