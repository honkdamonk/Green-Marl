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

	public void parse_foreign_syntax() {
		// scan through the original text and find '$' symbol
		// int size = strlen(orig_text);

		final int NULL = -1;

		int ID_begin = NULL;
		int ID_end = NULL; // inclusive
		int FIELD_begin = NULL;
		int FIELD_end = NULL; // inclusive
		int TEXT_begin = NULL;
		int TEXT_end = NULL; // inclusive
		int curr_ptr = NULL;
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

		TEXT_begin = curr_ptr;
		TEXT_begin_line = line;
		TEXT_begin_col = col;
		state = S_TEXT;

		// [TODO] consideration for built-in functions
		// we will make
		// [TEXT->NODE] -> [TEXT->NODE], -> [TEXT,NODE]
		// Text can be ""
		// ID can be NULL
		while (curr_ptr < text.length) {
			char c = text[curr_ptr];
			if (c == '\n')
				line++;

			switch (state) {
			case S_TEXT:
				if (c == '$') // begin GM
				{
					// check if next character is alpha numeric
					char d = text[curr_ptr + 1];
					if (Character.isLetter(d) || (d == '_')) {
						if (TEXT_begin == curr_ptr) {
							TEXT_begin = NULL;
							TEXT_end = NULL;
						} else {
							TEXT_end = curr_ptr - 1;
							assert TEXT_begin != NULL;
							assert TEXT_end >= TEXT_begin;
						}
						ID_begin = curr_ptr + 1;
						ID_begin_line = line;
						ID_begin_col = col + 1;
						state = S_ID;
					}
				}
				break;

			case S_ID:
				if (Character.isLetterOrDigit(c) || (c == '_')) // do nothing
				{
				} else {
					ID_end = curr_ptr - 1;
					assert ID_begin != NULL;
					assert ID_end >= ID_begin;
					if (c == '.') {
						// check if ID comes
						boolean dot_follow = false;
						int p = curr_ptr + 1;
						while (Character.isWhitespace(text[p]))
							p++;
						if (Character.isLetterOrDigit(text[p]) || (text[p] == '_')) {
							dot_follow = true;
						}

						if (dot_follow) {
							state = AFTER_DOT;
						} else {
							TEXT_begin = curr_ptr;
							TEXT_begin_line = line;
							TEXT_begin_col = col;
							state = S_TEXT;
						}
					} else if (Character.isWhitespace(c)) {
						ID_end = curr_ptr - 1;
						assert ID_begin != NULL;
						assert ID_end >= ID_begin;

						// look forward and check if '.' follows after space
						int p = curr_ptr;
						boolean dot_follow = false;
						while (Character.isWhitespace(text[p]))
							p++;
						if (text[p] == '.') {
							// check if alpha numeric comes after space
							p++;
							while (Character.isWhitespace(text[p]))
								p++;
							if (Character.isLetter(c) || (c == '_'))
								dot_follow = true;
						}
						if (dot_follow) {
							state = BEFORE_DOT;
						} else {
							{
								if (TEXT_begin == 0) {
									String S = "";
									parsed_foreign.addLast(S);
								} else {
									assert (TEXT_end >= TEXT_begin);
									String S = new String(text, TEXT_begin, (TEXT_end - TEXT_begin + 1));
									parsed_foreign.addLast(S);
								}
								ast_id id1 = null;
								if (ID_begin == NULL) {
									parsed_gm.addLast(null);
								} else {
									assert (ID_end >= ID_begin);
									String S = new String(text, ID_begin, (ID_end - ID_begin + 1));
									id1 = ast_id.new_id(S, ID_begin_line, ID_begin_col);
									if (FIELD_begin == NULL) {
										id1.set_parent(this);
										parsed_gm.addLast(id1);
									} else {
										assert (FIELD_end >= FIELD_begin);
										S = new String(text, FIELD_begin, (FIELD_end - FIELD_begin + 1));
										ast_id id2 = ast_id.new_id(S, FIELD_begin_line, FIELD_begin_col);
										ast_field field = ast_field.new_field(id1, id2);
										field.set_parent(this);
										parsed_gm.addLast(field);
									}
								}
								ID_begin = NULL;
								FIELD_begin = NULL;
								TEXT_begin = NULL;
							}
							;
							TEXT_begin = curr_ptr;
							TEXT_begin_line = line;
							TEXT_begin_col = col;
							state = S_TEXT;
						}
					} else {
						{
							if (TEXT_begin == NULL) {
								String S = "";
								parsed_foreign.addLast(S);
							} else {
								assert (TEXT_end >= TEXT_begin);
								String S = new String(text, TEXT_begin, (TEXT_end - TEXT_begin + 1));
								parsed_foreign.addLast(S);
							}
							ast_id id1 = null;
							if (ID_begin == NULL) {
								parsed_gm.addLast(null);
							} else {
								assert (ID_end >= ID_begin);
								String S = new String(text, ID_begin, (ID_end - ID_begin + 1));
								id1 = ast_id.new_id(S, ID_begin_line, ID_begin_col);
								if (FIELD_begin == NULL) {
									id1.set_parent(this);
									parsed_gm.addLast(id1);
								} else {
									assert (FIELD_end >= FIELD_begin);
									S = new String(text, FIELD_begin, (FIELD_end - FIELD_begin + 1));
									ast_id id2 = ast_id.new_id(S, FIELD_begin_line, FIELD_begin_col);
									ast_field field = ast_field.new_field(id1, id2);
									field.set_parent(this);
									parsed_gm.addLast(field);
								}
							}
							ID_begin = NULL;
							FIELD_begin = NULL;
							TEXT_begin = NULL;
						}
						TEXT_begin = curr_ptr;
						TEXT_begin_line = line;
						TEXT_begin_col = col;
						state = S_TEXT;
					}
				}
				break;

			case BEFORE_DOT:
				if (Character.isWhitespace(c)) // do nothing
				{
				} else if (c == '.') {
					state = AFTER_DOT;
				} else {
					assert false;
				}
				break;

			case AFTER_DOT:
				if (Character.isWhitespace(c)) // consume
				{
				} else if (Character.isLetter(c) || (c == '_')) {
					FIELD_begin = curr_ptr;
					FIELD_begin_line = line;
					FIELD_begin_col = col;
					state = S_FIELD;
				}
				break;

			case S_FIELD:
				if (Character.isLetterOrDigit(c) || (c == '_')) // do nothing
				{
				} else {
					{
						FIELD_end = curr_ptr - 1;
						assert FIELD_begin != NULL;
						assert FIELD_end >= FIELD_begin;
					}
					;
					{
						if (TEXT_begin == NULL) {
							String S = "";
							parsed_foreign.addLast(S);
						} else {
							assert (TEXT_end >= TEXT_begin);
							String S = new String(text, TEXT_begin, (TEXT_end - TEXT_begin + 1));
							parsed_foreign.addLast(S);
						}
						ast_id id1 = null;
						if (ID_begin == NULL) {
							parsed_gm.addLast(null);
						} else {
							assert (ID_end >= ID_begin);
							String S = new String(text, ID_begin, (ID_end - ID_begin + 1));
							id1 = ast_id.new_id(S, ID_begin_line, ID_begin_col);
							if (FIELD_begin == NULL) {
								id1.set_parent(this);
								parsed_gm.addLast(id1);
							} else {
								assert (FIELD_end >= FIELD_begin);
								S = new String(text, FIELD_begin, (FIELD_end - FIELD_begin + 1));
								ast_id id2 = ast_id.new_id(S, FIELD_begin_line, FIELD_begin_col);
								ast_field field = ast_field.new_field(id1, id2);
								field.set_parent(this);
								parsed_gm.addLast(field);
							}
						}
						ID_begin = NULL;
						FIELD_begin = NULL;
						TEXT_begin = NULL;
					}
					TEXT_begin = curr_ptr;
					TEXT_begin_line = line;
					TEXT_begin_col = col;
					state = S_TEXT;
				}
				break;
			}

			curr_ptr++;
			col++;
		}

		// finialize
		switch (state) {
		case S_TEXT:
			if (TEXT_begin == curr_ptr) // do nothing
			{
			} else {
				TEXT_end = curr_ptr - 1;
				assert TEXT_begin != NULL;
				assert TEXT_end >= TEXT_begin;

				{
					if (TEXT_begin == NULL) {
						String S = "";
						parsed_foreign.addLast(S);
					} else {
						assert (TEXT_end >= TEXT_begin);
						String S = new String(text, TEXT_begin, (TEXT_end - TEXT_begin + 1));
						parsed_foreign.addLast(S);
					}
					ast_id id1 = null;
					if (ID_begin == NULL) {
						parsed_gm.addLast(null);
					} else {
						assert (ID_end >= ID_begin);
						String S = new String(text, ID_begin, (ID_end - ID_begin + 1));
						id1 = ast_id.new_id(S, ID_begin_line, ID_begin_col);
						if (FIELD_begin == NULL) {
							id1.set_parent(this);
							parsed_gm.addLast(id1);
						} else {
							assert (FIELD_end >= FIELD_begin);
							S = new String(text, FIELD_begin, (FIELD_end - FIELD_begin + 1));
							ast_id id2 = ast_id.new_id(S, FIELD_begin_line, FIELD_begin_col);
							ast_field field = ast_field.new_field(id1, id2);
							field.set_parent(this);
							parsed_gm.addLast(field);
						}
					}
					ID_begin = NULL;
					FIELD_begin = NULL;
					TEXT_begin = NULL;
				}
			}
			break;
		case S_ID:
			FIELD_end = curr_ptr - 1;
			assert FIELD_begin != NULL;
			assert FIELD_end >= FIELD_begin;

			{
				if (TEXT_begin == NULL) {
					String S = "";
					parsed_foreign.addLast(S);
				} else {
					assert (TEXT_end >= TEXT_begin);
					String S = new String(text, TEXT_begin, (TEXT_end - TEXT_begin + 1));
					parsed_foreign.addLast(S);
				}
				ast_id id1 = null;
				if (ID_begin == NULL) {
					parsed_gm.addLast(null);
				} else {
					assert (ID_end >= ID_begin);
					String S = new String(text, ID_begin, (ID_end - ID_begin + 1));
					id1 = ast_id.new_id(S, ID_begin_line, ID_begin_col);
					if (FIELD_begin == NULL) {
						id1.set_parent(this);
						parsed_gm.addLast(id1);
					} else {
						assert (FIELD_end >= FIELD_begin);
						S = new String(text, FIELD_begin, (FIELD_end - FIELD_begin + 1));
						ast_id id2 = ast_id.new_id(S, FIELD_begin_line, FIELD_begin_col);
						ast_field field = ast_field.new_field(id1, id2);
						field.set_parent(this);
						parsed_gm.addLast(field);
					}
				}
				ID_begin = NULL;
				FIELD_begin = NULL;
				TEXT_begin = NULL;
			}
			break;
		case S_FIELD:
			FIELD_end = curr_ptr - 1;
			assert FIELD_begin != NULL;
			assert FIELD_end >= FIELD_begin;
			{
				if (TEXT_begin == NULL) {
					String S = "";
					parsed_foreign.addLast(S);
				} else {
					assert (TEXT_end >= TEXT_begin);
					String S = new String(text, TEXT_begin, (TEXT_end - TEXT_begin + 1));
					parsed_foreign.addLast(S);
				}
				ast_id id1 = null;
				if (ID_begin == NULL) {
					parsed_gm.addLast(null);
				} else {
					assert (ID_end >= ID_begin);
					String S = new String(text, ID_begin, (ID_end - ID_begin + 1));
					id1 = ast_id.new_id(S, ID_begin_line, ID_begin_col);
					if (FIELD_begin == NULL) {
						id1.set_parent(this);
						parsed_gm.addLast(id1);
					} else {
						assert (FIELD_end >= FIELD_begin);
						S = new String(text, FIELD_begin, (FIELD_end - FIELD_begin + 1));
						ast_id id2 = ast_id.new_id(S, FIELD_begin_line, FIELD_begin_col);
						ast_field field = ast_field.new_field(id1, id2);
						field.set_parent(this);
						parsed_gm.addLast(field);
					}
				}
				ID_begin = NULL;
				FIELD_begin = NULL;
				TEXT_begin = NULL;
			}
			break;
		}
	}
}