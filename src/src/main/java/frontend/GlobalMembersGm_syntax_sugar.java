package frontend;

import java.util.LinkedList;

import ast.ast_argdecl;
import ast.ast_id;
import ast.ast_idlist;
import ast.ast_typedecl;

public class GlobalMembersGm_syntax_sugar {

	public static void gm_expand_argument_list(LinkedList<ast_argdecl> A) {
		
		LinkedList<ast_argdecl> s = new LinkedList<ast_argdecl>(); // temp;

		// expand x,y : INT -> x:INT, y:INT
		for(ast_argdecl a : A) {
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