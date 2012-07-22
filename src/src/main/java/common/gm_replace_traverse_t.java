package common;

import ast.ast_assign;
import ast.ast_bfs;
import ast.ast_call;
import ast.ast_expr;
import ast.ast_expr_builtin;
import ast.ast_expr_reduce;
import ast.ast_foreach;
import ast.ast_if;
import ast.ast_return;
import ast.ast_sent;
import ast.ast_while;

public class gm_replace_traverse_t extends gm_apply
{
	public gm_replace_traverse_t(gm_expr_replacement_t E)
	{
		this._CHECK = E;
		set_for_sent(true);
		set_for_expr(true);
		_changed = false;
	}

	public final boolean is_changed()
	{
		return _changed;
	}

//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define CHECK_AND_REPLACE(SRC, NAME) { ast_expr* f = replace((SRC)->get_##NAME()); if (f!=NULL) {(SRC)->set_##NAME(f);}}

	public final boolean apply(ast_expr e)
	{
		if (e.is_foreign())
		{
			// [XXX]
		}
		else if (e.is_reduction())
		{
			ast_expr_reduce r = (ast_expr_reduce) e;
			{
				ast_expr f = replace((r).get_filter());
				if (f != null)
				{
					(r).set_filter(f);
				}
			};
			{
				ast_expr f = replace((r).get_body());
				if (f != null)
				{
					(r).set_body(f);
				}
			};
		}
		else if (e.is_builtin())
		{
			ast_expr_builtin b = (ast_expr_builtin) e;
			check_and_replace_list(b.get_args());
		}
		else
		{
		{
				ast_expr f = replace((e).get_left_op());
				if (f != null)
				{
					(e).set_left_op(f);
				}
			};
			{
				ast_expr f = replace((e).get_right_op());
				if (f != null)
				{
					(e).set_right_op(f);
				}
			};
			{
				ast_expr f = replace((e).get_cond_op());
				if (f != null)
				{
					(e).set_cond_op(f);
				}
			};
		}
		return true;
	}

	public final boolean apply(ast_sent s)
	{
		switch (s.get_nodetype())
		{
			case AST_IF:
			{
					ast_expr f = replace(((ast_if) s).get_cond());
					if (f != null)
					{
						((ast_if) s).set_cond(f);
					}
			};
				break;
			case AST_FOREACH:
			{
					ast_expr f = replace(((ast_foreach) s).get_filter());
					if (f != null)
					{
						((ast_foreach) s).set_filter(f);
					}
			};
				break;
			case AST_ASSIGN:
			{
				ast_assign a = (ast_assign) s;
				{
					ast_expr f = replace((a).get_rhs());
					if (f != null)
					{
						(a).set_rhs(f);
					}
				};
				check_and_replace_list(a.get_rhs_list());
				break;
			}
			case AST_WHILE:
			{
					ast_expr f = replace(((ast_while)s).get_cond());
					if (f != null)
					{
						((ast_while)s).set_cond(f);
					}
			};
				break;
			case AST_RETURN:
			{
					ast_expr f = replace(((ast_return)s).get_expr());
					if (f != null)
					{
						((ast_return)s).set_expr(f);
					}
			};
				break;

			case AST_BFS:
			{
					ast_expr f = replace(((ast_bfs)s).get_f_filter());
					if (f != null)
					{
						((ast_bfs)s).set_f_filter(f);
					}
			};
			{
					ast_expr f = replace(((ast_bfs)s).get_b_filter());
					if (f != null)
					{
						((ast_bfs)s).set_b_filter(f);
					}
				};
				{
					ast_expr f = replace(((ast_bfs)s).get_navigator());
					if (f != null)
					{
						((ast_bfs)s).set_navigator(f);
					}
				};
				break;
			case AST_CALL:
			{
				ast_call c = (ast_call) s;
				assert c.is_builtin_call();
				ast_expr f = replace(c.get_builtin());
				if (f != null)
				{
					assert f.is_builtin();
					c.set_builtin((ast_expr_builtin) f);
				}
				break;
			}
			case AST_FOREIGN:
				break;
			case AST_NOP:
				break;
			case AST_SENTBLOCK:
				break;
			default:
				assert false;
				break;
		}
		return true;
	}

	private boolean _changed;
	private gm_expr_replacement_t _CHECK;

	private ast_expr replace(ast_expr old) // check-replace-desotry
	{
		if (old == null)
			return null;

		if (_CHECK.is_target(old))
		{
			boolean destroy;
			tangible.RefObject<Boolean> tempRef_destroy = new tangible.RefObject<Boolean>(destroy);
			ast_expr newone = _CHECK.create_new_expr(old, tempRef_destroy);
			destroy = tempRef_destroy.argvalue;
			_changed = true;
			if (destroy)
			{
				if (old != null)
					old.dispose();
			}
			return newone;
		}
		return null;
	}

	private void check_and_replace_list(java.util.LinkedList<ast_expr> ARGS)
	{
		java.util.LinkedList<java.util.Iterator<ast_expr>> OLDS = new java.util.LinkedList<java.util.Iterator<ast_expr>>();
		java.util.Iterator<ast_expr> I;
		// check and insert
		for (I = ARGS.iterator(); I.hasNext();)
		{
			ast_expr f = replace(I.next());
			if (f != null)
			{
				OLDS.addLast(I);
//C++ TO JAVA CONVERTER TODO TASK: There is no direct equivalent to the STL list 'insert' method in Java:
				ARGS.insert(I, f);
			}
		}

		// remove positions
		java.util.Iterator<java.util.Iterator<ast_expr>> J;
		for (J = OLDS.iterator(); J.hasNext();)
		{
//C++ TO JAVA CONVERTER TODO TASK: There is no direct equivalent to the STL list 'erase' method in Java:
			ARGS.erase(J.next());
		}
	}
}