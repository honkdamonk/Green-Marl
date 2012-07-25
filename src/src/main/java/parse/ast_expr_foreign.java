public class ast_expr_foreign
{
//C++ TO JAVA CONVERTER WARNING: The original C++ declaration of the following method implementation was not found:
	public void parse_foreign_syntax()
	{
		// scan through the original text and find '$' symbol
		//int size = strlen(orig_text);
    
		String ID_begin = null;
		String ID_end = null; // inclusive
		String FIELD_begin = null;
		String FIELD_end = null; // inclusive
		String TEXT_begin = null;
		String TEXT_end = null; // inclusive
		String curr_ptr = orig_text;
    
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
    
	//C++ TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
	///#if false
	//    printf("ORG:[%s]\n", orig_text);
	///#endif
    
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
		while (!curr_ptr.equals('\0'))
		{
			byte c = curr_ptr;
			if (c == '\n')
				line++;
    
			switch (state)
			{
			case S_TEXT:
				if (c == '$') // begin GM
				{
					// check if next character is alpha numeric
					byte d = *(curr_ptr + 1);
					if (Character.isLetter(d) || (d == '_'))
					{
						if (TEXT_begin.equals(curr_ptr))
						{
							TEXT_begin = null;
							TEXT_end = null;
						}
						else
						{
						{
								TEXT_end = (false) ? curr_ptr : curr_ptr - 1;
								assert TEXT_begin != null;
								assert TEXT_end.compareTo(TEXT_begin) >= 0;
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
						assert ID_begin != null;
						assert ID_end.compareTo(ID_begin) >= 0;
					};
					if (c == '.')
					{
						// check if ID comes
						boolean dot_follow = false;
	//C++ TO JAVA CONVERTER TODO TASK: Pointer arithmetic is detected on this variable, so pointers on this variable are left unchanged.
						byte * p = curr_ptr + 1;
						while (Character.isWhitespace(*p))
							p++;
						if (Character.isLetterOrDigit(*p) || (*p == '_'))
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
							assert ID_begin != null;
							assert ID_end.compareTo(ID_begin) >= 0;
						};
    
						// look forward and check if '.' follows after space
	//C++ TO JAVA CONVERTER TODO TASK: Pointer arithmetic is detected on this variable, so pointers on this variable are left unchanged.
						byte * p = curr_ptr;
						boolean dot_follow = false;
						while (Character.isWhitespace(*p))
							p++;
						if (*p == '.')
						{
							// check if alpha numeric comes after space
							p++;
							while (Character.isWhitespace(*p))
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
							() {if (TEXT_begin == null) {String S; parsed_foreign.push_back(S);} else {assert (TEXT_end.compareTo(TEXT_begin) >= 0); String S(TEXT_begin, (TEXT_end - TEXT_begin + 1)); parsed_foreign.push_back(S);} ast_id * id1 = null; if (ID_begin == null) {parsed_gm.push_back(null);} else {assert (ID_end.compareTo(ID_begin) >= 0); String S(ID_begin, (ID_end - ID_begin + 1)); id1 = ast_id.new_id(S.c_str(), ID_begin_line, ID_begin_col); if (FIELD_begin == null) {id1.set_parent(this); parsed_gm.push_back(id1);} else {assert (FIELD_end.compareTo(FIELD_begin) >= 0); String S(FIELD_begin, (FIELD_end - FIELD_begin + 1)); ast_id * id2 = ast_id.new_id(S.c_str(), FIELD_begin_line, FIELD_begin_col); ast_field * field = ast_field.new_field(id1, id2); field.set_parent(this); parsed_gm.push_back(field);}} ID_begin = null; FIELD_begin = null; TEXT_begin = null;}();
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
						() {if (TEXT_begin == null) {String S; parsed_foreign.push_back(S);} else {assert (TEXT_end.compareTo(TEXT_begin) >= 0); String S(TEXT_begin, (TEXT_end - TEXT_begin + 1)); parsed_foreign.push_back(S);} ast_id * id1 = null; if (ID_begin == null) {parsed_gm.push_back(null);} else {assert (ID_end.compareTo(ID_begin) >= 0); String S(ID_begin, (ID_end - ID_begin + 1)); id1 = ast_id.new_id(S.c_str(), ID_begin_line, ID_begin_col); if (FIELD_begin == null) {id1.set_parent(this); parsed_gm.push_back(id1);} else {assert (FIELD_end.compareTo(FIELD_begin) >= 0); String S(FIELD_begin, (FIELD_end - FIELD_begin + 1)); ast_id * id2 = ast_id.new_id(S.c_str(), FIELD_begin_line, FIELD_begin_col); ast_field * field = ast_field.new_field(id1, id2); field.set_parent(this); parsed_gm.push_back(field);}} ID_begin = null; FIELD_begin = null; TEXT_begin = null;}();
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
						assert FIELD_begin != null;
						assert FIELD_end.compareTo(FIELD_begin) >= 0;
					};
					() {if (TEXT_begin == null) {String S; parsed_foreign.push_back(S);} else {assert (TEXT_end.compareTo(TEXT_begin) >= 0); String S(TEXT_begin, (TEXT_end - TEXT_begin + 1)); parsed_foreign.push_back(S);} ast_id * id1 = null; if (ID_begin == null) {parsed_gm.push_back(null);} else {assert (ID_end.compareTo(ID_begin) >= 0); String S(ID_begin, (ID_end - ID_begin + 1)); id1 = ast_id.new_id(S.c_str(), ID_begin_line, ID_begin_col); if (FIELD_begin == null) {id1.set_parent(this); parsed_gm.push_back(id1);} else {assert (FIELD_end.compareTo(FIELD_begin) >= 0); String S(FIELD_begin, (FIELD_end - FIELD_begin + 1)); ast_id * id2 = ast_id.new_id(S.c_str(), FIELD_begin_line, FIELD_begin_col); ast_field * field = ast_field.new_field(id1, id2); field.set_parent(this); parsed_gm.push_back(field);}} ID_begin = null; FIELD_begin = null; TEXT_begin = null;}();
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
				if (TEXT_begin.equals(curr_ptr)) //do nothing
				{
				}
				else
				{
				{
						TEXT_end = (false) ? curr_ptr : curr_ptr - 1;
						assert TEXT_begin != null;
						assert TEXT_end.compareTo(TEXT_begin) >= 0;
					};
					() {if (TEXT_begin == null) {String S; parsed_foreign.push_back(S);} else {assert (TEXT_end.compareTo(TEXT_begin) >= 0); String S(TEXT_begin, (TEXT_end - TEXT_begin + 1)); parsed_foreign.push_back(S);} ast_id * id1 = null; if (ID_begin == null) {parsed_gm.push_back(null);} else {assert (ID_end.compareTo(ID_begin) >= 0); String S(ID_begin, (ID_end - ID_begin + 1)); id1 = ast_id.new_id(S.c_str(), ID_begin_line, ID_begin_col); if (FIELD_begin == null) {id1.set_parent(this); parsed_gm.push_back(id1);} else {assert (FIELD_end.compareTo(FIELD_begin) >= 0); String S(FIELD_begin, (FIELD_end - FIELD_begin + 1)); ast_id * id2 = ast_id.new_id(S.c_str(), FIELD_begin_line, FIELD_begin_col); ast_field * field = ast_field.new_field(id1, id2); field.set_parent(this); parsed_gm.push_back(field);}} ID_begin = null; FIELD_begin = null; TEXT_begin = null;}();
				}
				break;
			case S_ID:
			{
					FIELD_end = (false) ? curr_ptr : curr_ptr - 1;
					assert FIELD_begin != null;
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