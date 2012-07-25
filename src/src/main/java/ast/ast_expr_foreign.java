package ast;

import inc.GMEXPR_CLASS;
import inc.GMTYPE_T;

import common.GlobalMembersGm_misc;
import common.gm_apply;

public class ast_expr_foreign extends ast_expr {
	public void dispose() {
		// java.util.Iterator<ast_node> I;
		// for (I = parsed_gm.iterator(); I.hasNext();)
		// {
		// I.next() = null;
		// }
		// orig_text = null;
	}

	public static ast_expr_foreign new_expr_foreign(tangible.RefObject<String> text) {
		ast_expr_foreign aef = new ast_expr_foreign();
		aef.expr_class = GMEXPR_CLASS.GMEXPR_FOREIGN;
		aef.orig_text = GlobalMembersGm_misc.gm_strdup(text.argvalue);
		aef.type_of_expression = GMTYPE_T.GMTYPE_FOREIGN_EXPR;
		return aef;
	}

	public void traverse(gm_apply a, boolean is_post, boolean is_pre) {
		boolean for_id = a.is_for_id();
		boolean for_expr = a.is_for_expr();
		boolean for_rhs = a.is_for_rhs();
		if (is_pre) {
			if (for_id) {
				apply_id(a, false);
			}
			if (for_rhs) {
				apply_rhs(a, false);
			}
		}

		if (for_expr)
			a.apply(this);

		if (is_post) {
			if (for_id) {
				apply_id(a, a.has_separate_post_apply());
			}
			if (for_rhs) {
				apply_rhs(a, a.has_separate_post_apply());
			}
		}

	}

	public void reproduce(int ind_lvel) {

		java.util.LinkedList<ast_node> N = this.get_parsed_nodes();
		java.util.LinkedList<String> T = this.get_parsed_text();
		java.util.Iterator<ast_node> I = N.iterator();
		java.util.Iterator<String> J = T.iterator();
		Out.push('[');
		while (I.hasNext()) {
			ast_node n = I.next();
			String J_s = J.next();
			Out.push(J_s);
			if (n == null)
				continue;
			if (n.get_nodetype() == AST_NODE_TYPE.AST_ID) {
				((ast_id) n).reproduce(0);
			} else if (n.get_nodetype() == AST_NODE_TYPE.AST_FIELD) {
				((ast_field) n).reproduce(0);
			}
		}
		Out.push(']');
	}

	public final java.util.LinkedList<ast_node> get_parsed_nodes() {
		return parsed_gm;
	}

	public final java.util.LinkedList<String> get_parsed_text() {
		return parsed_foreign;
	}

	// void parse_foreign_syntax();
	private ast_expr_foreign() {
		this.orig_text = null;
		set_nodetype(AST_NODE_TYPE.AST_EXPR_FOREIGN);
	}

	private String orig_text;

	// parsed foreign syntax
	private java.util.LinkedList<ast_node> parsed_gm = new java.util.LinkedList<ast_node>();
	private java.util.LinkedList<String> parsed_foreign = new java.util.LinkedList<String>();

	public void apply_id(gm_apply a, boolean apply2) {
		for (ast_node n : parsed_gm) {
			if (n == null)
				continue;
			if (n.get_nodetype() == AST_NODE_TYPE.AST_ID) {
				ast_id id = (ast_id) n;
				if (apply2)
					a.apply2(id);
				else
					a.apply(id);
			} else if (n.get_nodetype() == AST_NODE_TYPE.AST_FIELD) {
				ast_field f = (ast_field) n;
				if (apply2) {
					a.apply2(f.get_first());
					a.apply2(f.get_second());
				} else {
					a.apply(f.get_first());
					a.apply(f.get_second());
				}
			}
		}
	}

	public void apply_rhs(gm_apply a, boolean apply2) {
		for (ast_node n : parsed_gm) {
			if (n == null)
				continue;
			if (n.get_nodetype() == AST_NODE_TYPE.AST_ID) {
				ast_id id = (ast_id) n;
				if (apply2)
					a.apply_rhs2(id);
				else
					a.apply_rhs(id);
			} else if (n.get_nodetype() == AST_NODE_TYPE.AST_FIELD) {
				ast_field f = (ast_field) n;
				if (apply2) {
					a.apply_rhs2(f);
				} else {
					a.apply_rhs(f);
				}
			}
		}
	}

	public void parse_foreign_syntax()
	{
		// scan through the original text and find '$' symbol
		//int size = strlen(orig_text);
    
		int ID_begin = 0;
		int ID_end = 0; // inclusive
		int FIELD_begin = 0;
		int FIELD_end = 0; // inclusive
		int TEXT_begin = 0;
		int TEXT_end = 0; // inclusive
		int curr_ptr = 0;
		char[] text = orig_text.toCharArray();
    
		int ID_begin_line = 0;
		int ID_begin_col = 0;
		int FIELD_begin_line = 0;
		int FIELD_begin_col = 0;
		int TEXT_begin_line = 0;
		int TEXT_begin_col = 0;
    
		int state = 0;
		int line = get_line();
		int col = get_col();
    
		final int S_TEXT = 0;
		final int S_ID = 1; // $ SEEN
		final int BEFORE_DOT = 2; // . WILL BE SEEN has white space
		final int AFTER_DOT = 3; // . SEEN has while space
		final int S_FIELD = 4;
		
		final boolean FROM_NEXT = true;
		final boolean FROM_CURR = false;
		final boolean TILL_CURR = true;
		final boolean TILL_PREV = false;    
    
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define FROM_NEXT true
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define FROM_CURR false
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define TILL_CURR true
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define TILL_PREV false
		{
			TEXT_begin = (false) ? curr_ptr + 1 : curr_ptr;
			TEXT_begin_line = line;
			TEXT_begin_col = (false) ? col + 1 : col;
			state = S_TEXT;
		};
    
		// [TODO] consideration for built-in functions
		// we will make
		//  [TEXT->NODE] -> [TEXT->NODE], -> [TEXT,NODE] 
		//     Text can be ""
		//     ID can be NULL
		while (curr_ptr < text.length)
		{
			char c = text[curr_ptr];
			if (c == '\n')
				line++;
    
			switch (state)
			{
			case S_TEXT:
				if (c == '$') // begin GM
				{
					// check if next character is alpha numeric
					char d = text[curr_ptr + 1];
					if (Character.isLetter(d) || (d == '_'))
					{
						if (TEXT_begin == curr_ptr)
						{
							TEXT_begin = 0;
							TEXT_end = 0;
						}
						else
						{
						{
								TEXT_end = (false) ? curr_ptr : curr_ptr - 1;
								//assert TEXT_begin != null;
								assert TEXT_end >= TEXT_begin;
							};
						}
						{
							ID_begin = (true) ? curr_ptr + 1 : curr_ptr;
							ID_begin_line = line;
							ID_begin_col = (true) ? col + 1 : col;
							state = S_ID;
						};
					}
				}
				break;
    
			case S_ID:
				if (Character.isLetterOrDigit(c) || (c == '_')) // do nothing
				{
				}
				else
				{
				{
						ID_end = (false) ? curr_ptr : curr_ptr - 1;
						//assert ID_begin != null;
						assert ID_end >= ID_begin;
					};
					if (c == '.')
					{
						// check if ID comes
						boolean dot_follow = false;
	//C++ TO JAVA CONVERTER TODO TASK: Pointer arithmetic is detected on this variable, so pointers on this variable are left unchanged.
						int  p = curr_ptr + 1;
						while (Character.isWhitespace(text[p]))
							p++;
						if (Character.isLetterOrDigit(text[p]) || (text[p] == '_'))
						{
							dot_follow = true;
						}
    
						if (dot_follow)
						{
							state = AFTER_DOT;
						}
						else
						{
						{
								TEXT_begin = (false) ? curr_ptr + 1 : curr_ptr;
								TEXT_begin_line = line;
								TEXT_begin_col = (false) ? col + 1 : col;
								state = S_TEXT;
							};
						}
					}
					else if (Character.isWhitespace(c))
					{
					{
							ID_end = (false) ? curr_ptr : curr_ptr - 1;
							//assert ID_begin != null;
							assert ID_end >= ID_begin;
						};
    
						// look forward and check if '.' follows after space
	//C++ TO JAVA CONVERTER TODO TASK: Pointer arithmetic is detected on this variable, so pointers on this variable are left unchanged.
						int p = curr_ptr;
						boolean dot_follow = false;
						while (Character.isWhitespace(text[p]))
							p++;
						if (text[p] == '.')
						{
							// check if alpha numeric comes after space
							p++;
							while (Character.isWhitespace(text[p]))
								p++;
							if (Character.isLetter(c) || (c == '_'))
								dot_follow = true;
						}
						if (dot_follow)
						{
							state = BEFORE_DOT;
						}
						else
						{
						//TODO	{if (TEXT_begin == 0) {String S = ""; parsed_foreign.push_back(S);} else {assert (TEXT_end >= TEXT_begin); String S(TEXT_begin, (TEXT_end - TEXT_begin + 1)); parsed_foreign.push_back(S);} ast_id * id1 = null; if (ID_begin == null) {parsed_gm.push_back(null);} else {assert (ID_end.compareTo(ID_begin) >= 0); String S(ID_begin, (ID_end - ID_begin + 1)); id1 = ast_id.new_id(S.c_str(), ID_begin_line, ID_begin_col); if (FIELD_begin == null) {id1.set_parent(this); parsed_gm.push_back(id1);} else {assert (FIELD_end.compareTo(FIELD_begin) >= 0); String S(FIELD_begin, (FIELD_end - FIELD_begin + 1)); ast_id * id2 = ast_id.new_id(S.c_str(), FIELD_begin_line, FIELD_begin_col); ast_field * field = ast_field.new_field(id1, id2); field.set_parent(this); parsed_gm.push_back(field);}} ID_begin = null; FIELD_begin = null; TEXT_begin = null;};
							{
								TEXT_begin = (false) ? curr_ptr + 1 : curr_ptr;
								TEXT_begin_line = line;
								TEXT_begin_col = (false) ? col + 1 : col;
								state = S_TEXT;
							};
						}
					}
					else
					{
						//TODO {if (TEXT_begin == null) {String S; parsed_foreign.push_back(S);} else {assert (TEXT_end.compareTo(TEXT_begin) >= 0); String S(TEXT_begin, (TEXT_end - TEXT_begin + 1)); parsed_foreign.push_back(S);} ast_id * id1 = null; if (ID_begin == null) {parsed_gm.push_back(null);} else {assert (ID_end.compareTo(ID_begin) >= 0); String S(ID_begin, (ID_end - ID_begin + 1)); id1 = ast_id.new_id(S.c_str(), ID_begin_line, ID_begin_col); if (FIELD_begin == null) {id1.set_parent(this); parsed_gm.push_back(id1);} else {assert (FIELD_end.compareTo(FIELD_begin) >= 0); String S(FIELD_begin, (FIELD_end - FIELD_begin + 1)); ast_id * id2 = ast_id.new_id(S.c_str(), FIELD_begin_line, FIELD_begin_col); ast_field * field = ast_field.new_field(id1, id2); field.set_parent(this); parsed_gm.push_back(field);}} ID_begin = null; FIELD_begin = null; TEXT_begin = null;}();
						{
							TEXT_begin = (false) ? curr_ptr + 1 : curr_ptr;
							TEXT_begin_line = line;
							TEXT_begin_col = (false) ? col + 1 : col;
							state = S_TEXT;
						};
					}
				}
				break;
    
			case BEFORE_DOT:
				if (Character.isWhitespace(c)) // do nothing
				{
				}
				else if (c == '.')
				{
					state = AFTER_DOT;
				}
				else
				{
					assert false;
				}
				break;
    
			case AFTER_DOT:
				if (Character.isWhitespace(c)) // consume
				{
				}
				else if (Character.isLetter(c) || (c == '_'))
				{
				{
						FIELD_begin = (false) ? curr_ptr + 1 : curr_ptr;
						FIELD_begin_line = line;
						FIELD_begin_col = (false) ? col + 1 : col;
						state = S_FIELD;
					};
				}
				break;
    
			case S_FIELD:
				if (Character.isLetterOrDigit(c) || (c == '_')) // do nothing
				{
				}
				else
				{
				{
						FIELD_end = (false) ? curr_ptr : curr_ptr - 1;
						//assert FIELD_begin != null;
						assert FIELD_end >= FIELD_begin;
					};
					//TODO {if (TEXT_begin == null) {String S; parsed_foreign.push_back(S);} else {assert (TEXT_end.compareTo(TEXT_begin) >= 0); String S(TEXT_begin, (TEXT_end - TEXT_begin + 1)); parsed_foreign.push_back(S);} ast_id * id1 = null; if (ID_begin == null) {parsed_gm.push_back(null);} else {assert (ID_end.compareTo(ID_begin) >= 0); String S(ID_begin, (ID_end - ID_begin + 1)); id1 = ast_id.new_id(S.c_str(), ID_begin_line, ID_begin_col); if (FIELD_begin == null) {id1.set_parent(this); parsed_gm.push_back(id1);} else {assert (FIELD_end.compareTo(FIELD_begin) >= 0); String S(FIELD_begin, (FIELD_end - FIELD_begin + 1)); ast_id * id2 = ast_id.new_id(S.c_str(), FIELD_begin_line, FIELD_begin_col); ast_field * field = ast_field.new_field(id1, id2); field.set_parent(this); parsed_gm.push_back(field);}} ID_begin = null; FIELD_begin = null; TEXT_begin = null;}();
					{
						TEXT_begin = (false) ? curr_ptr + 1 : curr_ptr;
						TEXT_begin_line = line;
						TEXT_begin_col = (false) ? col + 1 : col;
						state = S_TEXT;
					};
				}
				break;
			}
    
			curr_ptr++;
			col++;
		}
    
		// finialize
		switch (state)
		{
			case S_TEXT:
				if (TEXT_begin == curr_ptr) //do nothing
				{
				}
				else
				{
				{
						TEXT_end = (false) ? curr_ptr : curr_ptr - 1;
						//assert TEXT_begin != null;
						assert TEXT_end >= TEXT_begin;
					};
					//TODO {if (TEXT_begin == null) {String S; parsed_foreign.push_back(S);} else {assert (TEXT_end.compareTo(TEXT_begin) >= 0); String S(TEXT_begin, (TEXT_end - TEXT_begin + 1)); parsed_foreign.push_back(S);} ast_id * id1 = null; if (ID_begin == null) {parsed_gm.push_back(null);} else {assert (ID_end.compareTo(ID_begin) >= 0); String S(ID_begin, (ID_end - ID_begin + 1)); id1 = ast_id.new_id(S.c_str(), ID_begin_line, ID_begin_col); if (FIELD_begin == null) {id1.set_parent(this); parsed_gm.push_back(id1);} else {assert (FIELD_end.compareTo(FIELD_begin) >= 0); String S(FIELD_begin, (FIELD_end - FIELD_begin + 1)); ast_id * id2 = ast_id.new_id(S.c_str(), FIELD_begin_line, FIELD_begin_col); ast_field * field = ast_field.new_field(id1, id2); field.set_parent(this); parsed_gm.push_back(field);}} ID_begin = null; FIELD_begin = null; TEXT_begin = null;}();
				}
				break;
			case S_ID:
			{
					FIELD_end = (false) ? curr_ptr : curr_ptr - 1;
					//assert FIELD_begin != null;
					assert FIELD_end.compareTo(FIELD_begin) >= 0;
			};
				() {if (TEXT_begin == null) {String S; parsed_foreign.push_back(S);} else {assert (TEXT_end.compareTo(TEXT_begin) >= 0); String S(TEXT_begin, (TEXT_end - TEXT_begin + 1)); parsed_foreign.push_back(S);} ast_id * id1 = null; if (ID_begin == null) {parsed_gm.push_back(null);} else {assert (ID_end.compareTo(ID_begin) >= 0); String S(ID_begin, (ID_end - ID_begin + 1)); id1 = ast_id.new_id(S.c_str(), ID_begin_line, ID_begin_col); if (FIELD_begin == null) {id1.set_parent(this); parsed_gm.push_back(id1);} else {assert (FIELD_end.compareTo(FIELD_begin) >= 0); String S(FIELD_begin, (FIELD_end - FIELD_begin + 1)); ast_id * id2 = ast_id.new_id(S.c_str(), FIELD_begin_line, FIELD_begin_col); ast_field * field = ast_field.new_field(id1, id2); field.set_parent(this); parsed_gm.push_back(field);}} ID_begin = null; FIELD_begin = null; TEXT_begin = null;}();
				break;
			case S_FIELD:
			{
					FIELD_end = (false) ? curr_ptr : curr_ptr - 1;
					assert FIELD_begin != null;
					assert FIELD_end.compareTo(FIELD_begin) >= 0;
			};
				() {if (TEXT_begin == null) {String S; parsed_foreign.push_back(S);} else {assert (TEXT_end.compareTo(TEXT_begin) >= 0); String S(TEXT_begin, (TEXT_end - TEXT_begin + 1)); parsed_foreign.push_back(S);} ast_id * id1 = null; if (ID_begin == null) {parsed_gm.push_back(null);} else {assert (ID_end.compareTo(ID_begin) >= 0); String S(ID_begin, (ID_end - ID_begin + 1)); id1 = ast_id.new_id(S.c_str(), ID_begin_line, ID_begin_col); if (FIELD_begin == null) {id1.set_parent(this); parsed_gm.push_back(id1);} else {assert (FIELD_end.compareTo(FIELD_begin) >= 0); String S(FIELD_begin, (FIELD_end - FIELD_begin + 1)); ast_id * id2 = ast_id.new_id(S.c_str(), FIELD_begin_line, FIELD_begin_col); ast_field * field = ast_field.new_field(id1, id2); field.set_parent(this); parsed_gm.push_back(field);}} ID_begin = null; FIELD_begin = null; TEXT_begin = null;}();
				break;
		}
    
		//-----------------------------------------
		//Let's see what happend
		//-----------------------------------------
	//C++ TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
	///#if false
	//
	//    std::list<ast_node*>::iterator I;   
	//    std::list<std::string>::iterator J; 
	//
	//    J = parsed_foreign.begin();
	//    I = parsed_gm.begin();
	//    for(; I!=parsed_gm.end(); I++, J++)
	//    {
	//        std::string& S = *J;
	//        ast_node* node = *I;
	//        printf("TEXT[%s]::", S.c_str());
	//        if (node == NULL) 
	//        {
	//            printf("(NIL)");
	//        }
	//        else if (node->get_nodetype() == AST_FIELD) 
	//        {
	//            ast_field* f = (ast_field*) node;
	//            printf("FIELD[%s.%s(%d.%d)]",
	//                    f->get_first()->get_orgname(), f->get_second()->get_orgname(),
	//                    f->get_first()->get_line(), f->get_first()->get_col());
	//        }
	//        else if (node->get_nodetype() == AST_ID)
	//        {
	//            ast_id* i = (ast_id*) node;
	//            printf("ID[%s(%d.%d)]",
	//                    i->get_orgname(), i->get_line(), i->get_col());
	//        }
	//        else {
	//            assert(false);
	//        }
	//        printf("-->");
	//    }
	//    printf("\n");
	//
	///#endif
	}
}