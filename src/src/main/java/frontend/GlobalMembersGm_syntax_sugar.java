package frontend;

import ast.ast_argdecl;
import ast.ast_id;
import ast.ast_idlist;
import ast.ast_typedecl;

public class GlobalMembersGm_syntax_sugar
{

	public static void gm_expand_argument_list(java.util.LinkedList<ast_argdecl> A)
	{
		java.util.LinkedList<ast_argdecl> s = new java.util.LinkedList<ast_argdecl>(); // temp;
		java.util.Iterator<ast_argdecl> I;

		// expand  x,y : INT -> x:INT, y:INT
		for (I = A.iterator(); I.hasNext();)
		{
			ast_argdecl a = I.next();
			ast_idlist idl = a.get_idlist();
			ast_typedecl t = a.get_type();
			if (idl.get_length() == 1)
			{
				s.addLast(a);
			}
			else
			{
				for (int i = 0; i < idl.get_length(); i++)
				{
					ast_id I = idl.get_item(i).copy();
					ast_idlist IDL = new ast_idlist();
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy constructor call - this should be verified and a copy constructor should be created if it does not yet exist:
//ORIGINAL LINE: IDL->add_id(I);
					IDL.add_id(new java.util.Iterator(I));
					ast_typedecl T = t.copy();

					ast_argdecl aa = ast_argdecl.new_argdecl(IDL, T);
					s.addLast(aa);
				}

				if (a != null)
				a.dispose();
			}
		}

		// new clear A, and put contents of S into A
		A.clear();
		for (I = s.iterator(); I.hasNext();)
		{
			A.addLast(I.next());
		}
	}
}