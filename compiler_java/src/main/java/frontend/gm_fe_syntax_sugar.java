package frontend;

import java.util.LinkedList;

import inc.gm_compile_step;
import ast.ast_argdecl;
import ast.ast_id;
import ast.ast_idlist;
import ast.ast_procdef;
import ast.ast_typedecl;

import common.gm_traverse;

public class gm_fe_syntax_sugar extends gm_compile_step {

	private gm_fe_syntax_sugar() {
		set_description("Regularize syntax (without typeinfo)");
	}

	@Override
	public void process(ast_procdef p) {
		gm_ss1_filter s1 = new gm_ss1_filter();
		gm_traverse.gm_traverse_sents(p, s1);

		gm_ss1_initial_expr s2 = new gm_ss1_initial_expr();
		gm_traverse.gm_traverse_sents(p, s2);

		gm_expand_argument_list(p.get_in_args());
		gm_expand_argument_list(p.get_out_args());

	}

	@Override
	public gm_compile_step get_instance() {
		return new gm_fe_syntax_sugar();
	}

	public static gm_compile_step get_factory() {
		return new gm_fe_syntax_sugar();
	}

	private static void gm_expand_argument_list(LinkedList<ast_argdecl> A) {

		LinkedList<ast_argdecl> s = new LinkedList<ast_argdecl>(); // temp;

		// expand x,y : INT -> x:INT, y:INT
		for (ast_argdecl a : A) {
			ast_idlist idl = a.get_idlist();
			ast_typedecl t = a.get_type();
			if (idl.get_length() == 1) {
				s.addLast(a);
			} else {
				for (int i = 0; i < idl.get_length(); i++) {
					ast_id I1 = idl.get_item(i).copy();
					ast_idlist IDL = new ast_idlist();
					IDL.add_id(I1.copy());
					ast_typedecl T = t.copy();

					ast_argdecl aa = ast_argdecl.new_argdecl(IDL, T);
					s.addLast(aa);
				}
			}
		}
		// new clear A, and put contents of S into A
		A.clear();
		A.addAll(s);
	}
}