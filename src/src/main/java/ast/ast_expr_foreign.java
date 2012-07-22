package ast;

import inc.GMEXPR_CLASS;
import inc.GMTYPE_T;
import backend_cpp.*;
import backend_giraph.*;
import common.*;
import frontend.*;
import opt.*;
import tangible.*;

public class ast_expr_foreign extends ast_expr
{
	public void dispose()
	{
		java.util.Iterator<ast_node> I;
		for (I = parsed_gm.iterator(); I.hasNext();)
		{
			I.next() = null;
		}
		orig_text = null;
	}

	public static ast_expr_foreign new_expr_foreign(tangible.RefObject<String> text)
	{
		ast_expr_foreign aef = new ast_expr_foreign();
		aef.expr_class = GMEXPR_CLASS.GMEXPR_FOREIGN;
		aef.orig_text = GlobalMembersGm_misc.gm_strdup(text.argvalue);
		aef.type_of_expression = GMTYPE_T.GMTYPE_FOREIGN_EXPR;
		return aef;
	}
	public void traverse(gm_apply a, boolean is_post, boolean is_pre)
	{
		boolean for_id = a.is_for_id();
		boolean for_expr = a.is_for_expr();
		boolean for_rhs = a.is_for_rhs();
		if (is_pre)
		{
			if (for_id)
			{
				apply_id(a, false);
			}
			if (for_rhs)
			{
				apply_rhs(a, false);
			}
		}
    
		if (for_expr)
			a.apply(this);
    
		if (is_post)
		{
			if (for_id)
			{
				apply_id(a, a.has_separate_post_apply());
			}
			if (for_rhs)
			{
				apply_rhs(a, a.has_separate_post_apply());
			}
		}
    
	}
	public void reproduce(int ind_lvel)
	{
    
		java.util.LinkedList<ast_node> N = this.get_parsed_nodes();
		java.util.LinkedList<String> T = this.get_parsed_text();
		java.util.Iterator<ast_node> I = N.iterator();
		java.util.Iterator<String> J = T.iterator();
		Out.push('[');
		for (; I.hasNext(); I++, J++)
		{
			Out.push(J.c_str());
			ast_node n = I.next();
			if (n == null)
				continue;
			if (n.get_nodetype() == AST_NODE_TYPE.AST_ID)
			{
				((ast_id) n).reproduce(0);
			}
			else if (n.get_nodetype() == AST_NODE_TYPE.AST_FIELD)
			{
				((ast_field) n).reproduce(0);
			}
		}
		Out.push(']');
    
	}

	public final java.util.LinkedList<ast_node> get_parsed_nodes()
	{
		return parsed_gm;
	}
	public final java.util.LinkedList<String> get_parsed_text()
	{
		return parsed_foreign;
	}

//	void parse_foreign_syntax();
	private ast_expr_foreign()
	{
		this.orig_text = null;
		set_nodetype(AST_NODE_TYPE.AST_EXPR_FOREIGN);
	}
	private String orig_text;

	// parsed foreign syntax
	private java.util.LinkedList<ast_node> parsed_gm = new java.util.LinkedList<ast_node>();
	private java.util.LinkedList<String> parsed_foreign = new java.util.LinkedList<String>();
	public void apply_id(gm_apply a, boolean apply2)
	{
		java.util.Iterator<ast_node> I;
		for (I = parsed_gm.begin(); I.hasNext();)
		{
			ast_node n = I.next();
			if (n == null)
				continue;
			if (n.get_nodetype() == AST_NODE_TYPE.AST_ID)
			{
				ast_id id = (ast_id) n;
				if (apply2)
					a.apply2(id);
				else
					a.apply(id);
			}
			else if (n.get_nodetype() == AST_NODE_TYPE.AST_FIELD)
			{
				ast_field f = (ast_field) n;
				if (apply2)
				{
					a.apply2(f.get_first());
					a.apply2(f.get_second());
				}
				else
				{
					a.apply(f.get_first());
					a.apply(f.get_second());
				}
			}
		}
	}
	public void apply_rhs(gm_apply a, boolean apply2)
	{
		java.util.Iterator<ast_node> I;
		for (I = parsed_gm.begin(); I.hasNext();)
		{
			ast_node n = I.next();
			if (n == null)
				continue;
			if (n.get_nodetype() == AST_NODE_TYPE.AST_ID)
			{
				ast_id id = (ast_id) n;
				if (apply2)
					a.apply_rhs2(id);
				else
					a.apply_rhs(id);
			}
			else if (n.get_nodetype() == AST_NODE_TYPE.AST_FIELD)
			{
				ast_field f = (ast_field) n;
				if (apply2)
				{
					a.apply_rhs2(f);
				}
				else
				{
					a.apply_rhs(f);
				}
			}
		}
	}
}