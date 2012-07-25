package inc;

import java.util.Iterator;
import java.util.LinkedList;

import ast.AST_NODE_TYPE;
import ast.ast_assign;
import ast.ast_bfs;
import ast.ast_call;
import ast.ast_expr;
import ast.ast_expr_foreign;
import ast.ast_field;
import ast.ast_foreach;
import ast.ast_foreign;
import ast.ast_id;
import ast.ast_if;
import ast.ast_node;
import ast.ast_nop;
import ast.ast_return;
import ast.ast_sent;
import ast.ast_sentblock;
import ast.ast_vardecl;
import ast.ast_while;

import common.GlobalMembersGm_misc;
import common.GlobalMembersGm_reproduce;

// default code generator
public abstract class gm_code_generator {

	public gm_code_generator() {
		this._Body = new gm_code_writer();
	}

	@Deprecated
	public gm_code_generator(gm_code_writer W) {
		this._Body = new gm_code_writer();
	}

	public void dispose() {
	}

	// should be overrided
	public abstract void generate_rhs_id(ast_id i);

	public abstract void generate_rhs_field(ast_field i);

	public void generate_expr_foreign(ast_expr e) {
		ast_expr_foreign f = (ast_expr_foreign) e;

		LinkedList<ast_node> N = f.get_parsed_nodes();
		LinkedList<String> T = f.get_parsed_text();
		Iterator<ast_node> I = N.iterator();
		Iterator<String> J = T.iterator();
		while (I.hasNext()) {
			_Body.push(J.next().toString());
			ast_node n = I.next();
			if (n == null)
				continue;
			if (n.get_nodetype() == AST_NODE_TYPE.AST_ID) {
				generate_rhs_id((ast_id) n);
			} else if (n.get_nodetype() == AST_NODE_TYPE.AST_FIELD) {
				generate_rhs_field((ast_field) n);
			}
		}

	}

	public abstract void generate_expr_builtin(ast_expr e);

	public abstract void generate_expr_minmax(ast_expr e);

	public abstract void generate_expr_abs(ast_expr e);

	public abstract void generate_expr_nil(ast_expr e);

	final static String LP = "(";
	final static String RP = ")";

	public void generate_expr_type_conversion(ast_expr e) {
		boolean no_lp1 = (e.get_up_op() == null);
		boolean need_lp2 = e.get_left_op().is_builtin();
		if (!no_lp1)
			_Body.push(LP);
		_Body.push(LP);
		_Body.push(get_type_string(e.get_type_summary()));
		_Body.push(RP);
		if (need_lp2)
			_Body.push(LP);
		generate_expr(e.get_left_op());
		if (need_lp2)
			_Body.push(RP);
		if (!no_lp1)
			_Body.push(RP);
	}

	public abstract String get_type_string(GMTYPE_T gmtype_T); // returned
																// string
	// should be copied
	// before usage.

	public void generate_expr_list(LinkedList<ast_expr> L) {
		int i = 0;
		int size = L.size();
		for (ast_expr expr : L) {
			generate_expr(expr);
			if (i != (size - 1))
				_Body.push(", ");
			i++;
		}
	}

	public void generate_expr(ast_expr e) {
		if (e.is_inf())
			generate_expr_inf(e);
		else if (e.is_literal())
			generate_expr_val(e);
		else if (e.is_terop())
			generate_expr_ter(e);
		else if (e.is_id())
			generate_rhs_id(e.get_id());
		else if (e.is_field())
			generate_rhs_field(e.get_field());
		else if (e.is_uop())
			generate_expr_uop(e);
		else if (e.is_biop()) {
			if ((e.get_optype() == GM_OPS_T.GMOP_MIN) || (e.get_optype() == GM_OPS_T.GMOP_MAX))
				generate_expr_minmax(e);
			else
				generate_expr_bin(e);
		} else if (e.is_comp())
			generate_expr_comp(e);
		else if (e.is_terop())
			generate_expr_ter(e);
		else if (e.is_builtin())
			generate_expr_builtin(e);
		else if (e.is_foreign())
			generate_expr_foreign(e);
		else if (e.is_nil())
			generate_expr_nil(e);
		else {
			e.reproduce(0);
			GlobalMembersGm_reproduce.gm_flush_reproduce();
			assert false;
		}
	}

	public void generate_expr_val(ast_expr e) {
		String temp = temp_str;
		switch (e.get_opclass()) {
		case GMEXPR_IVAL:
			String.format(temp, "%ld", e.get_ival()); // to be changed
			_Body.push(temp);
			return;
		case GMEXPR_FVAL:
			if (e.get_type_summary() == GMTYPE_T.GMTYPE_FLOAT) {
				String.format(temp, "(float)(%lf)", e.get_fval()); // to be
																	// changed
				_Body.push(temp);
			} else {
				String.format(temp, "%lf", e.get_fval()); // to be changed
				_Body.push(temp);
			}
			return;
		case GMEXPR_BVAL:
			String.format(temp, "%s", e.get_bval() ? "true" : "false");
			_Body.push(temp);
			return;

		default:
			assert false;
			return;
		}
	}

	public void generate_expr_inf(ast_expr e) {
		String temp = temp_str;
		assert e.get_opclass() == GMEXPR_CLASS.GMEXPR_INF;
		GMTYPE_T t = e.get_type_summary();
		switch (t) {
		case GMTYPE_INF:
		case GMTYPE_INF_INT:
			String.format(temp, "%s", e.is_plus_inf() ? "INT_MAX" : "INT_MIN"); // temporary
			break;
		case GMTYPE_INF_LONG:
			String.format(temp, "%s", e.is_plus_inf() ? "LLONG_MAX" : "LLONG_MIN"); // temporary
			break;
		case GMTYPE_INF_FLOAT:
			String.format(temp, "%s", e.is_plus_inf() ? "FLT_MAX" : "FLT_MIN"); // temporary
			break;
		case GMTYPE_INF_DOUBLE:
			String.format(temp, "%s", e.is_plus_inf() ? "DBL_MAX" : "DBL_MIN"); // temporary
			break;
		default:
			String.format(temp, "%s", e.is_plus_inf() ? "INT_MAX" : "INT_MIN"); // temporary
			break;
		}
		_Body.push(temp);
		return;
	}

	public void generate_expr_uop(ast_expr e) {
		// char* temp = temp_str;
		switch (e.get_opclass()) {
		case GMEXPR_UOP:
			if (e.get_optype() == GM_OPS_T.GMOP_NEG) {
				_Body.push(" -");
				generate_expr(e.get_left_op());
				return;
			} else if (e.get_optype() == GM_OPS_T.GMOP_ABS) {
				generate_expr_abs(e);
				return;
			} else if (e.get_optype() == GM_OPS_T.GMOP_TYPEC) {
				generate_expr_type_conversion(e);
				return;
			} else {
				assert false;
				break;
			}
		case GMEXPR_LUOP:
			_Body.push(" !");
			generate_expr(e.get_left_op());
			break;
		default:
			assert false;
			break;
		}
	}

	public void generate_expr_ter(ast_expr e) {

		boolean need_para = (e.get_up_op() == null) ? false : check_need_para(e.get_optype(), e.get_up_op().get_optype(), e.is_right_op());

		if (need_para)
			_Body.push("(");
		generate_expr(e.get_cond_op());
		_Body.push("?");
		generate_expr(e.get_left_op());
		_Body.push(":");
		generate_expr(e.get_right_op());
		if (need_para)
			_Body.push(")");
		return;
	}

	public void generate_expr_bin(ast_expr e) {
		// char* temp = temp_str;
		ast_expr up = e.get_up_op();
		boolean need_para = false;
		if (up == null)
			need_para = false;
		else if (up.is_biop() || up.is_comp()) {
			need_para = check_need_para(e.get_optype(), up.get_optype(), e.is_right_op());
		} else {
			need_para = true;
		}

		if (need_para)
			_Body.push("(");

		generate_expr(e.get_left_op());
		_Body.SPC();
		String opstr = GlobalMembersGm_misc.gm_get_op_string(e.get_optype());
		_Body.push_spc(opstr);
		generate_expr(e.get_right_op());

		if (need_para)
			_Body.push(")");
	}

	public void generate_expr_comp(ast_expr e) {
		// char* temp = temp_str;
		ast_expr up = e.get_up_op();
		boolean need_para = (up == null) ? false : true;

		if (need_para)
			_Body.push("(");

		generate_expr(e.get_left_op());
		_Body.SPC();
		String opstr = GlobalMembersGm_misc.gm_get_op_string(e.get_optype());
		_Body.push_spc(opstr);
		generate_expr(e.get_right_op());

		if (need_para)
			_Body.push(")");
	}

	public boolean check_need_para(GM_OPS_T optype, GM_OPS_T up_optype, boolean is_right) {
		return GlobalMembersGm_misc.gm_need_paranthesis(optype, up_optype, is_right);
	}

	public abstract void generate_lhs_id(ast_id i);

	public abstract void generate_lhs_field(ast_field i);

	public abstract void generate_sent_nop(ast_nop n);

	public abstract void generate_sent_reduce_assign(ast_assign a);

	public abstract void generate_sent_defer_assign(ast_assign a);

	public abstract void generate_sent_vardecl(ast_vardecl a);

	public abstract void generate_sent_foreach(ast_foreach a);

	public abstract void generate_sent_bfs(ast_bfs b);

	public void generate_sent(ast_sent s) {
		switch (s.get_nodetype()) {
		case AST_SENTBLOCK:
			generate_sent_block((ast_sentblock) s);
			break;
		case AST_VARDECL:
			generate_sent_vardecl((ast_vardecl) s);
			break;
		case AST_IF:
			generate_sent_if((ast_if) s);
			break;
		case AST_WHILE:
			generate_sent_while((ast_while) s);
			break;
		case AST_RETURN:
			generate_sent_return((ast_return) s);
			break;
		case AST_FOREACH:
			generate_sent_foreach((ast_foreach) s);
			break;
		case AST_BFS:
			generate_sent_bfs((ast_bfs) s);
			break;
		case AST_ASSIGN: {
			ast_assign a = (ast_assign) s;
			if (a.is_reduce_assign()) {
				generate_sent_reduce_assign(a);
			} else if (a.is_defer_assign()) {
				generate_sent_defer_assign(a);
			} else {
				generate_sent_assign(a);
			}
			break;
		}
		case AST_NOP:
			generate_sent_nop((ast_nop) s);
			break;
		case AST_CALL:
			generate_sent_call((ast_call) s);
			break;
		case AST_FOREIGN:
			generate_sent_foreign((ast_foreign) s);
			break;
		default:
			assert false;
			break;
		}
	}

	public void generate_sent_assign(ast_assign a) {

		if (a.is_target_scalar()) {
			generate_lhs_id(a.get_lhs_scala());
		} else {
			generate_lhs_field(a.get_lhs_field());
		}

		_Body.push(" = ");

		generate_expr(a.get_rhs());

		_Body.pushln(" ;");
	}

	public void generate_sent_if(ast_if i) {
		_Body.push("if (");
		generate_expr(i.get_cond());

		_Body.pushln(")");
		ast_sent s = i.get_then();
		if (s.get_nodetype() != AST_NODE_TYPE.AST_SENTBLOCK) {
			_Body.push_indent();
		}

		generate_sent(s);

		if (s.get_nodetype() != AST_NODE_TYPE.AST_SENTBLOCK) {
			_Body.pop_indent();
		}

		s = i.get_else();
		if (s == null)
			return;

		_Body.pushln("else");
		if (s.get_nodetype() != AST_NODE_TYPE.AST_SENTBLOCK) {
			_Body.push_indent();
		}

		generate_sent(s);

		if (s.get_nodetype() != AST_NODE_TYPE.AST_SENTBLOCK) {
			_Body.pop_indent();
		}

	}

	public void generate_sent_while(ast_while w) {
		ast_sent b = w.get_body();
		assert b.get_nodetype() == AST_NODE_TYPE.AST_SENTBLOCK;

		if (w.is_do_while()) {
			_Body.pushln("do");

			generate_sent(b);

			_Body.push("while (");
			generate_expr(w.get_cond());
			_Body.pushln(");");
		} else {
			_Body.push("while (");
			generate_expr(w.get_cond());
			_Body.pushln(")");

			generate_sent(b);
		}

	}

	public void generate_sent_block(ast_sentblock sb) {
		generate_sent_block(sb, true);
	}

	public void generate_sent_block(ast_sentblock sb, boolean need_brace) {
		LinkedList<ast_sent> sents = sb.get_sents();

		java.util.Iterator<ast_sent> i;
		if (need_brace)
			_Body.pushln("{");
		for (i = sents.iterator(); i.hasNext();) {
			ast_sent s = i.next();
			generate_sent(s);
		}
		if (need_brace)
			_Body.pushln("}");
	}

	public void generate_sent_return(ast_return r) {
		_Body.push("return");
		if (r.get_expr() != null) {
			_Body.SPC();
			generate_expr(r.get_expr());
		}
		_Body.pushln(";");
	}

	public void generate_sent_call(ast_call c) {
		assert false;
	}

	public void generate_sent_foreign(ast_foreign f) {
		ast_expr_foreign ff = f.get_expr();
		generate_expr(ff);
		_Body.pushln(";");
	}

	protected String temp_str = new String(new char[1024 * 8]);
	protected gm_code_writer _Body;

}