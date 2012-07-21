package ast;

import inc.GMEXPR_CLASS;
import inc.GMTYPE_T;
import inc.GM_OPS_T;
import common.GlobalMembersGm_dumptree;
import common.GlobalMembersGm_misc;
import common.gm_apply;

import frontend.gm_symtab_entry;

// Numeric or boolean expression
//C++ TO JAVA CONVERTER NOTE: Java has no need of forward class declarations:
//class gm_builtin_def;
// defined in gm_builtin.h

public class ast_expr extends ast_node
{
	public void dispose()
	{
		if (id1 != null)
			id1.dispose();
		if (field != null)
			field.dispose();
		if (left != null)
			left.dispose(); // object is new-ed
		if (right != null)
			right.dispose();
		if (cond != null)
			cond.dispose();
	}

	public void reproduce(int ind_level)
	{
    
		String buf = new String(new char[1024]);
		switch (expr_class)
		{
			case GMEXPR_NIL:
				Out.push("NIL");
				return;
			case GMEXPR_INF:
				Out.push(plus_inf ? '+' : '-');
				Out.push("INF");
				return;
			case GMEXPR_IVAL:
				String.format(buf, "%ld", ival);
				Out.push(buf);
				return;
			case GMEXPR_FVAL:
				String.format(buf, "%lf", fval);
				Out.push(buf);
				return;
			case GMEXPR_BVAL:
				if (bval)
					Out.push("True");
				else
					Out.push("False");
				return;
			case GMEXPR_ID:
				id1.reproduce(0);
				return;
			case GMEXPR_FIELD:
				field.reproduce(0);
				return;
			case GMEXPR_UOP:
				if (op_type == GM_OPS_T.GMOP_NEG)
				{
					Out.push(" - ");
					left.reproduce(0);
				}
				else if (op_type == GM_OPS_T.GMOP_ABS)
				{
					Out.push(" | ");
					left.reproduce(0);
					Out.push(" | ");
				}
				else if (op_type == GM_OPS_T.GMOP_TYPEC)
				{
					Out.push(" (");
					Out.push(GlobalMembersGm_misc.gm_get_type_string(type_of_expression));
					Out.push(" ) ");
					left.reproduce(0);
				}
				else
				{
					assert false;
				}
				return;
			case GMEXPR_LUOP:
				assert op_type == GM_OPS_T.GMOP_NOT;
				Out.push("!");
				left.reproduce(0);
				return;
			case GMEXPR_BUILTIN:
				assert false;
				// should be called with another virtual function
				//id1->reproduce(0);
				//Out.push('.');
				//Out.push(get_builtin_call());
				//Out.push("()");
				return;
			case GMEXPR_REDUCE:
				assert false;
				// should be called with anohter virtual function
				return;
			case GMEXPR_TER:
			{
				boolean need_para;
				if (up == null)
					need_para = false;
				else
					need_para = GlobalMembersGm_misc.gm_need_paranthesis(get_optype(), up.get_optype(), is_right_op());
				if (need_para)
					Out.push('(');
				cond.reproduce(0);
				Out.push(" ? ");
				left.reproduce(0);
				Out.push(" : ");
				right.reproduce(0);
				if (need_para)
					Out.push(')');
				return;
			}
			case GMEXPR_BIOP:
			case GMEXPR_LBIOP:
			case GMEXPR_COMP:
			case GMEXPR_BUILTIN_FIELD:
			case GMEXPR_FOREIGN:
				//TODO add some print statements for these?
				return;
		}
    
		// binop
		String opstr = GlobalMembersGm_misc.gm_get_op_string(op_type);
		assert is_biop() || is_comp();
		// numeric or logical
    
		// check need ()
		boolean need_para = true;
		if (up == null)
			need_para = false;
		else if (is_comp())
		{
			need_para = true;
		}
		else if (up.is_biop() || up.is_comp())
		{
			need_para = GlobalMembersGm_misc.gm_need_paranthesis(get_optype(), up.get_optype(), is_right_op());
		}
		else
			need_para = true;
    
		if (need_para)
		{
			Out.push("(");
		}
		left.reproduce(ind_level);
		Out.SPC();
		Out.push(opstr);
		Out.SPC();
		right.reproduce(ind_level);
		if (need_para)
		{
			Out.push(") ");
		}
	}
	public void dump_tree(int ind_level)
	{
    
		GlobalMembersGm_dumptree.IND(ind_level);
		assert parent != null;
		switch (expr_class)
		{
			case GMEXPR_INF:
				System.out.printf("%cINF", plus_inf ? '+' : '-');
				return;
			case GMEXPR_IVAL:
				System.out.printf("%ld", ival);
				return;
			case GMEXPR_FVAL:
				System.out.printf("%fl", fval);
				return;
			case GMEXPR_BVAL:
				System.out.printf("%s", bval ? "true" : "false");
				return;
			case GMEXPR_ID:
				id1.dump_tree(0);
				return;
			case GMEXPR_FIELD:
				field.dump_tree(0);
				return;
			case GMEXPR_UOP:
				System.out.print("[ ");
				if (op_type == GM_OPS_T.GMOP_NEG)
				{
					System.out.print("- \n");
				}
				else if (op_type == GM_OPS_T.GMOP_ABS)
				{
					System.out.print("abs \n");
				}
				else if (op_type == GM_OPS_T.GMOP_TYPEC)
				{
					System.out.printf("( %s )\n", GlobalMembersGm_misc.gm_get_type_string(type_of_expression));
				}
				left.dump_tree(ind_level + 1);
				System.out.print("\n");
				GlobalMembersGm_dumptree.IND(ind_level);
				System.out.print("]");
				return;
			case GMEXPR_LUOP:
				System.out.print("[ !\n");
				left.dump_tree(ind_level + 1);
				System.out.print("\n");
				GlobalMembersGm_dumptree.IND(ind_level);
				System.out.print("]");
				return;
			case GMEXPR_TER:
				System.out.print("[(cond)\n");
				cond.dump_tree(ind_level + 1);
				System.out.print("\n");
				GlobalMembersGm_dumptree.IND(ind_level);
				System.out.print("(left)\n");
				left.dump_tree(ind_level + 1);
				System.out.print("\n");
				GlobalMembersGm_dumptree.IND(ind_level);
				System.out.print("(right)\n");
				right.dump_tree(ind_level + 1);
				System.out.print("\n");
				GlobalMembersGm_dumptree.IND(ind_level);
				System.out.print("]");
				return;
			case GMEXPR_BUILTIN:
				assert false;
				//printf("[");
				//id1->dump_tree(0);
				//printf(".%s()]",get_builtin_call());
				return;
			case GMEXPR_NIL:
			case GMEXPR_BIOP:
			case GMEXPR_LBIOP:
			case GMEXPR_COMP:
			case GMEXPR_REDUCE:
			case GMEXPR_BUILTIN_FIELD:
			case GMEXPR_FOREIGN:
				//TODO add some print statements for these?
				return;
		}
		String opstr = GlobalMembersGm_misc.gm_get_op_string(op_type);
    
		assert (expr_class == GMEXPR_CLASS.GMEXPR_BIOP) || (expr_class == GMEXPR_CLASS.GMEXPR_LBIOP) || (expr_class == GMEXPR_CLASS.GMEXPR_COMP);
    
		System.out.printf("[%s\n", opstr);
		left.dump_tree(ind_level + 1);
		System.out.print("\n");
		right.dump_tree(ind_level + 1);
		System.out.print("\n");
		GlobalMembersGm_dumptree.IND(ind_level);
		System.out.print("]");
	}
	public void traverse(gm_apply a, boolean is_post, boolean is_pre)
	{
		boolean for_sent = a.is_for_sent();
		boolean for_id = a.is_for_id();
		boolean for_expr = a.is_for_expr();
		boolean for_symtab = a.is_for_symtab();
		boolean for_rhs = a.is_for_rhs();
    
		if (!(for_id || for_expr || for_symtab || for_rhs)) // no more sentence behind this
			return;
    
		if (for_expr && is_pre)
			a.apply(this);
    
		boolean b = a.has_separate_post_apply();
		switch (get_opclass())
		{
			case GMEXPR_ID:
				if (for_id)
				{
					if (is_pre)
						a.apply(get_id());
					if (is_post)
					{
						if (b)
							a.apply2(get_id());
						else
							a.apply(get_id());
					}
				}
				if (for_rhs)
				{
					if (is_pre)
						a.apply_rhs(get_id());
					if (is_post)
					{
						if (b)
							a.apply_rhs2(get_id());
						else
							a.apply_rhs(get_id());
					}
				}
				break;
			case GMEXPR_FIELD:
				if (for_id)
				{
					if (is_pre)
					{
						a.apply(get_field().get_first());
						a.apply(get_field().get_second());
					}
					if (is_post)
					{
						if (b)
						{
							a.apply2(get_field().get_first());
							a.apply2(get_field().get_second());
						}
						else
						{
							a.apply(get_field().get_first());
							a.apply(get_field().get_second());
						}
					}
				}
				if (for_rhs)
				{
					if (is_pre)
						a.apply_rhs(get_field());
					if (is_post)
					{
						if (b)
							a.apply_rhs2(get_field());
						else
							a.apply_rhs(get_field());
					}
				}
				break;
			case GMEXPR_UOP:
			case GMEXPR_LUOP:
				get_left_op().traverse(a, is_post, is_pre);
				break;
			case GMEXPR_BIOP:
			case GMEXPR_LBIOP:
			case GMEXPR_COMP:
				get_left_op().traverse(a, is_post, is_pre);
				get_right_op().traverse(a, is_post, is_pre);
				break;
			case GMEXPR_TER:
				get_cond_op().traverse(a, is_post, is_pre);
				get_left_op().traverse(a, is_post, is_pre);
				get_right_op().traverse(a, is_post, is_pre);
				break;
    
			case GMEXPR_IVAL:
			case GMEXPR_FVAL:
			case GMEXPR_BVAL:
			case GMEXPR_INF:
			case GMEXPR_NIL:
				break;
    
			case GMEXPR_BUILTIN:
			case GMEXPR_FOREIGN:
			case GMEXPR_REDUCE:
				assert false;
				// should not be in here
				break;
    
			default:
				assert false;
				break;
		}
    
		if (for_expr && is_post)
		{
			boolean b = a.has_separate_post_apply();
			if (b)
				a.apply2(this);
			else
				a.apply(this);
		}
    
		return;
	}
	public ast_expr copy(boolean b)
	{
		ast_expr e;
		switch (expr_class)
		{
			case GMEXPR_ID:
				e = ast_expr.new_id_expr(id1.copy(b));
				break;
			case GMEXPR_FIELD:
				e = ast_expr.new_field_expr(field.copy(b));
				break;
			case GMEXPR_IVAL:
				e = ast_expr.new_ival_expr(ival);
				break;
			case GMEXPR_FVAL:
				e = ast_expr.new_fval_expr(fval);
				break;
			case GMEXPR_BVAL:
				e = ast_expr.new_bval_expr(bval);
				break;
			case GMEXPR_INF:
				e = ast_expr.new_inf_expr(plus_inf);
				break;
			case GMEXPR_UOP:
				e = ast_expr.new_uop_expr(op_type, left.copy(b));
				break;
			case GMEXPR_BIOP:
				e = ast_expr.new_biop_expr(op_type, left.copy(b), right.copy(b));
				break;
			case GMEXPR_LUOP:
				e = ast_expr.new_luop_expr(op_type, left.copy(b));
				break;
			case GMEXPR_LBIOP:
				e = ast_expr.new_lbiop_expr(op_type, left.copy(b), right.copy(b));
				break;
			case GMEXPR_COMP:
				e = ast_expr.new_comp_expr(op_type, left.copy(b), right.copy(b));
				break;
			case GMEXPR_TER:
				e = ast_expr.new_ternary_expr(cond.copy(b), left.copy(b), right.copy(b));
				break;
			case GMEXPR_BUILTIN:
				//e= ast_expr::new_builtin_expr(id1->copy(b), builtin_orgname); break;
				return ((ast_expr_builtin) this).copy(b);
				break;
			case GMEXPR_REDUCE:
				return ((ast_expr_reduce) this).copy(b);
				break;
			default:
				assert false;
				break;
		}
    
		e.set_type_summary(this.get_type_summary());
		return e;
	}

	@Override
	public boolean is_expr()
	{
		return true;
	}

	// factory methods

	public static ast_expr new_id_expr(ast_id id)
	{
		ast_expr E = new ast_expr();
		E.expr_class = GMEXPR_CLASS.GMEXPR_ID;
		E.id1 = id;
		id.set_parent(E);
		return E;
	}

	public static ast_expr new_field_expr(ast_field f)
	{
		ast_expr E = new ast_expr();
		E.expr_class = GMEXPR_CLASS.GMEXPR_FIELD;
		E.field = f;
		f.set_parent(E);
		return E;
	}

	public static ast_expr new_ival_expr(int ival)
	{
		ast_expr E = new ast_expr();
		E.expr_class = GMEXPR_CLASS.GMEXPR_IVAL;
		E.type_of_expression = GMTYPE_T.GMTYPE_INT.getValue(); // LONG?
		E.ival = ival;
		return E;
	}
	public static ast_expr new_fval_expr(double fval)
	{
		ast_expr E = new ast_expr();
		E.expr_class = GMEXPR_CLASS.GMEXPR_FVAL;
		E.type_of_expression = GMTYPE_T.GMTYPE_FLOAT.getValue(); // DOUBLE?
		E.fval = fval;
		return E;
	}
	public static ast_expr new_bval_expr(boolean bval)
	{
		ast_expr E = new ast_expr();
		E.expr_class = GMEXPR_CLASS.GMEXPR_BVAL;
		E.type_of_expression = GMTYPE_T.GMTYPE_BOOL.getValue();
		E.bval = bval;
		return E;
	}
	public static ast_expr new_nil_expr()
	{
		ast_expr E = new ast_expr();
		E.expr_class = GMEXPR_CLASS.GMEXPR_NIL;
		E.type_of_expression = GMTYPE_T.GMTYPE_NIL_UNKNOWN.getValue();
		return E;
	}
	public static ast_expr new_inf_expr(boolean is_p)
	{
		ast_expr E = new ast_expr();
		E.expr_class = GMEXPR_CLASS.GMEXPR_INF;
		E.type_of_expression = GMTYPE_T.GMTYPE_INF.getValue();
		E.plus_inf = is_p;
		return E;
	}
	public static ast_expr new_typeconv_expr(int target_type, ast_expr l)
	{
		ast_expr E = new ast_expr();
		E.expr_class = GMEXPR_CLASS.GMEXPR_UOP;
		E.op_type = GM_OPS_T.GMOP_TYPEC.getValue();
		E.type_of_expression = target_type; // GMTYPE_xxx
		E.left = l;
		l.up = E;
		l.parent = E;
		return E;
	}
	public static ast_expr new_uop_expr(int op_type, ast_expr l)
	{
		ast_expr E = new ast_expr();
		E.expr_class = GMEXPR_CLASS.GMEXPR_UOP;
		E.op_type = op_type;
		E.type_of_expression = GMTYPE_T.GMTYPE_UNKNOWN_NUMERIC.getValue();
		E.left = l;
		l.up = E;
		l.parent = E;
		return E;
	}
	public static ast_expr new_biop_expr(int op_type, ast_expr l, ast_expr r)
	{
		ast_expr E = new ast_expr();
		E.expr_class = GMEXPR_CLASS.GMEXPR_BIOP;
		E.op_type = op_type;
		E.left = l;
		E.right = r;
		l.up = E;
		r.up = E;
		r.is_right = true;
		E.type_of_expression = GMTYPE_T.GMTYPE_UNKNOWN_NUMERIC.getValue();
		l.parent = r.parent = E;
		return E;
	}
	public static ast_expr new_luop_expr(int op_type, ast_expr l)
	{
		ast_expr E = new ast_expr();
		E.expr_class = GMEXPR_CLASS.GMEXPR_LUOP;
		E.op_type = op_type;
		E.type_of_expression = GMTYPE_T.GMTYPE_BOOL.getValue();
		E.left = l;
		l.up = E;
		l.parent = E;
		return E;
	}
	public static ast_expr new_lbiop_expr(int op_type, ast_expr l, ast_expr r)
	{
		ast_expr E = new ast_expr();
		E.expr_class = GMEXPR_CLASS.GMEXPR_LBIOP;
		E.op_type = op_type;
		E.left = l;
		E.right = r;
		l.up = E;
		r.up = E;
		r.is_right = true;
		E.type_of_expression = GMTYPE_T.GMTYPE_BOOL.getValue();
		l.parent = r.parent = E;
		return E;
	}
	public static ast_expr new_comp_expr(int op_type, ast_expr l, ast_expr r)
	{
		ast_expr E = new ast_expr();
		E.expr_class = GMEXPR_CLASS.GMEXPR_COMP;
		E.op_type = op_type;
		E.left = l;
		E.right = r;
		l.up = E;
		r.up = E;
		r.is_right = true;
		E.type_of_expression = GMTYPE_T.GMTYPE_BOOL.getValue();
		l.parent = r.parent = E;
		return E;
	}

	public static ast_expr new_ternary_expr(ast_expr cond, ast_expr left, ast_expr right)
	{
		ast_expr E = new ast_expr();
		E.expr_class = GMEXPR_CLASS.GMEXPR_TER;
		E.op_type = GM_OPS_T.GMOP_TER.getValue();
		E.left = left;
		E.right = right;
		E.cond = cond;
		cond.up = left.up = right.up = E;
		cond.parent = left.parent = right.parent = E;
		right.is_right = cond.is_cond = true;
		return E;
	}

	protected ast_expr()
	{
		super(AST_NODE_TYPE.AST_EXPR);
		this.expr_class = GMEXPR_CLASS.GMEXPR_ID;
		this.left = null;
		this.right = null;
		this.cond = null;
		this.up = null;
		this.id1 = null;
		this.field = null;
		this.ival = 0;
		this.fval = 0;
		this.bval = false;
		this.plus_inf = false;
		this.op_type = GM_OPS_T.GMOP_END.getValue();
		this.is_right = false;
		this.is_cond = false;
		this.type_of_expression = GMTYPE_T.GMTYPE_UNKNOWN.getValue();
		this.alternative_type_of_expression = GMTYPE_T.GMTYPE_UNKNOWN.getValue();
		this.bound_graph_sym = null;
	}

	protected GMEXPR_CLASS expr_class; // GMEXPR_...
	protected ast_expr left;
	protected ast_expr right;
	protected ast_expr cond;
	protected ast_expr up;
	protected ast_id id1;
	protected ast_field field;
	protected int ival;
	protected double fval;
	protected boolean bval;
	protected boolean plus_inf;
	protected int op_type;
	protected boolean is_right; // am I a right-operand?
	protected boolean is_cond; // am I a conditional-operand?

	protected int type_of_expression; // set after local typecheck
	protected int alternative_type_of_expression; // used for group-assignment only. (during type checking)

	public final boolean is_biop()
	{
		return (expr_class == GMEXPR_CLASS.GMEXPR_BIOP) || (expr_class == GMEXPR_CLASS.GMEXPR_LBIOP);
	}
	public final boolean is_uop()
	{
		return (expr_class == GMEXPR_CLASS.GMEXPR_UOP) || (expr_class == GMEXPR_CLASS.GMEXPR_LUOP);
	}
	public final boolean is_comp()
	{
		return (expr_class == GMEXPR_CLASS.GMEXPR_COMP);
	}
	public final boolean is_id()
	{
		return expr_class == GMEXPR_CLASS.GMEXPR_ID;
	}
	public final boolean is_nil()
	{
		return expr_class == GMEXPR_CLASS.GMEXPR_NIL;
	}
	public final boolean is_field()
	{
		return expr_class == GMEXPR_CLASS.GMEXPR_FIELD;
	}
	public final boolean is_int_literal()
	{
		return expr_class == GMEXPR_CLASS.GMEXPR_IVAL;
	}
	public final boolean is_float_literal()
	{
		return expr_class == GMEXPR_CLASS.GMEXPR_FVAL;
	}
	public final boolean is_boolean_literal()
	{
		return expr_class == GMEXPR_CLASS.GMEXPR_BVAL;
	}
	public final boolean is_inf()
	{
		return expr_class == GMEXPR_CLASS.GMEXPR_INF;
	}
	public final boolean is_literal()
	{
		return is_int_literal() || is_float_literal() || is_boolean_literal() || is_inf();
	}
	public final boolean is_reduction()
	{
		return expr_class == GMEXPR_CLASS.GMEXPR_REDUCE;
	}
	public final boolean is_builtin()
	{
		return expr_class == GMEXPR_CLASS.GMEXPR_BUILTIN || expr_class == GMEXPR_CLASS.GMEXPR_BUILTIN_FIELD;
	}
	public final boolean is_terop()
	{
		return expr_class == GMEXPR_CLASS.GMEXPR_TER;
	}
	public final boolean is_foreign()
	{
		return expr_class == GMEXPR_CLASS.GMEXPR_FOREIGN;
	}

	//-----------------------------------------------
	// type is set after type-checker execution
	//-----------------------------------------------
	public final int get_type_summary()
	{
		return type_of_expression;
	}
	public final void set_type_summary(int t)
	{
		type_of_expression = t;
	} // set by type checker

	public final gm_symtab_entry get_bound_graph()
	{
		if (bound_graph_sym == null && is_id())
			return id1.getTypeInfo().get_target_graph_sym();
		else
			return bound_graph_sym;
	}
	public final void set_bound_graph(gm_symtab_entry e)
	{
		bound_graph_sym = e;
	}

	public final int get_ival()
	{
		return ival;
	}

	public final double get_fval()
	{
		return fval;
	}

	public final boolean get_bval()
	{
		return bval;
	}

	public final boolean is_plus_inf()
	{
		return is_inf() && plus_inf;
	} // true o
	public final ast_id get_id()
	{
		return id1;
	}

	public ast_field get_field()
	{
		return field;
	}

	public final GMEXPR_CLASS get_opclass()
	{
		return expr_class;
	}

	public final int get_optype()
	{
		return op_type;
	}

	public final boolean is_right_op()
	{
		return is_right;
	}

	public final boolean is_cond_op()
	{
		return is_cond;
	}

	public final void set_id(ast_id i)
	{
		id1 = i;
		if (i != null)
		{
			i.set_parent(this);
			expr_class = GMEXPR_CLASS.GMEXPR_ID;
		}
	}

	public final void set_field(ast_field f)
	{
		field = f;
		if (f != null)
		{
			f.set_parent(this);
			expr_class = GMEXPR_CLASS.GMEXPR_FIELD;
		}
	}

	public final boolean is_type_conv()
	{
		return op_type == GM_OPS_T.GMOP_TYPEC.getValue();
	}

	public final ast_expr get_left_op()
	{
		return left;
	}

	public final ast_expr get_right_op()
	{
		return right;
	}

	public final ast_expr get_up_op()
	{
		return up;
	} // same to parent. but expr
	public final ast_expr get_cond_op()
	{
		return cond;
	}

	public final void set_left_op(ast_expr l)
	{
		left = l;
		if (l != null)
		{
			l.set_parent(this);
			l.set_up_op(this);
		}
	}

	public final void set_right_op(ast_expr r)
	{
		right = r;
		if (r != null)
		{
			r.set_parent(this);
			r.set_up_op(this);
		}
	}

	public final void set_up_op(ast_expr e)
	{
		up = e;
	}

	public final void set_cond_op(ast_expr e)
	{
		cond = e;
		if (e != null)
		{
			e.set_parent(this);
			e.set_up_op(this);
		}
	}

	public final void set_alternative_type(int i)
	{
		alternative_type_of_expression = i;
	}

	public final int get_alternative_type()
	{
		return alternative_type_of_expression;
	}

	public final void set_expr_class(GMEXPR_CLASS ec)
	{
		expr_class = ec;
	}

	protected gm_symtab_entry bound_graph_sym; // used only during typecheck
}