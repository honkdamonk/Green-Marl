package ast;

import inc.GMEXPR_CLASS;
import inc.GMTYPE_T;

import java.util.Iterator;
import java.util.LinkedList;

import common.gm_apply;

public class ast_expr_foreign extends ast_expr {

	private String orig_text;

	// parsed foreign syntax
	private LinkedList<ast_node> parsed_gm = new LinkedList<ast_node>();
	private LinkedList<String> parsed_foreign = new LinkedList<String>();

	public void dispose() {
		// Iterator<ast_node> I;
		// for (I = parsed_gm.iterator(); I.hasNext();)
		// {
		// I.next() = null;
		// }
		// orig_text = null;
	}

	public static ast_expr_foreign new_expr_foreign(tangible.RefObject<String> text) {
		ast_expr_foreign aef = new ast_expr_foreign();
		aef.expr_class = GMEXPR_CLASS.GMEXPR_FOREIGN;
		assert text.argvalue != null;
		aef.orig_text = text.argvalue;
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

		LinkedList<ast_node> N = this.get_parsed_nodes();
		LinkedList<String> T = this.get_parsed_text();
		Iterator<ast_node> I = N.iterator();
		Iterator<String> J = T.iterator();
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

	public final LinkedList<ast_node> get_parsed_nodes() {
		return parsed_gm;
	}

	public final LinkedList<String> get_parsed_text() {
		return parsed_foreign;
	}

	// void parse_foreign_syntax();
	private ast_expr_foreign() {
		this.orig_text = null;
		set_nodetype(AST_NODE_TYPE.AST_EXPR_FOREIGN);
	}

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
		(new ForeignSyntaxParser(this)).parse();
	}

	private class ForeignSyntaxParser {

		private final int NULL = -1;

		private int ID_begin = NULL;
		private int ID_end = NULL; // inclusive
		private int FIELD_begin = NULL;
		private int FIELD_end = NULL; // inclusive
		private int TEXT_begin = NULL;
		private int TEXT_end = NULL; // inclusive
		private int curr_ptr = NULL;
		private char[] text = orig_text.toCharArray();

		private int ID_begin_line = 0;
		private int ID_begin_col = 0;
		private int FIELD_begin_line = 0;
		private int FIELD_begin_col = 0;
		private int TEXT_begin_line = 0;
		private int TEXT_begin_col = 0;

		private int state = 0;
		private int line = get_line();
		private int col = get_col();

		private final int S_TEXT = 0;
		private final int S_ID = 1; // $ SEEN
		private final int BEFORE_DOT = 2; // . WILL BE SEEN has white space
		private final int AFTER_DOT = 3; // . SEEN has while space
		private final int S_FIELD = 4;

		private final ast_expr_foreign parent;

		private ForeignSyntaxParser(ast_expr_foreign parent) {
			this.parent = parent;
		}

		void parse() {
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
					handleS_TEXT(c);
					break;
				case S_ID:
					handleS_ID(c);
					break;
				case BEFORE_DOT:
					handleBEFORE_DOT(c);
					break;
				case AFTER_DOT:
					handleAFTER_DOT(c);
					break;
				case S_FIELD:
					handleS_FIELD(c);
					break;
				}

				curr_ptr++;
				col++;
			}
			// finialize
			doFinalize();
		}

		private void handleS_TEXT(char c) {
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
						doAssertOnText();
					}
					ID_begin = curr_ptr + 1;
					ID_begin_line = line;
					ID_begin_col = col + 1;
					state = S_ID;
				}
			}
		}

		private void handleS_ID(char c) {
			if (Character.isLetterOrDigit(c) || (c == '_')) {
				// do nothing
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
						finishElement();
						TEXT_begin = curr_ptr;
						TEXT_begin_line = line;
						TEXT_begin_col = col;
						state = S_TEXT;
					}
				} else {
					finishElement();
					TEXT_begin = curr_ptr;
					TEXT_begin_line = line;
					TEXT_begin_col = col;
					state = S_TEXT;
				}
			}
		}

		private void handleBEFORE_DOT(char c) {
			if (Character.isWhitespace(c)) {// do nothing

			} else if (c == '.') {
				state = AFTER_DOT;
			} else {
				assert false;
			}
		}

		private void handleAFTER_DOT(char c) {
			if (Character.isLetter(c) || (c == '_')) {
				FIELD_begin = curr_ptr;
				FIELD_begin_line = line;
				FIELD_begin_col = col;
				state = S_FIELD;
			}
		}

		private void handleS_FIELD(char c) {
			if (!Character.isLetterOrDigit(c) && (c != '_')) {
				FIELD_end = curr_ptr - 1;
				doAssertOnField();
				finishElement();
				TEXT_begin = curr_ptr;
				TEXT_begin_line = line;
				TEXT_begin_col = col;
				state = S_TEXT;
			}
		}

		private void doFinalize() {
			switch (state) {
			case S_TEXT:
				if (TEXT_begin == curr_ptr) { // do nothing
				} else {
					TEXT_end = curr_ptr - 1;
					doAssertOnText();
					finishElement();
				}
				break;
			case S_ID:
				FIELD_end = curr_ptr - 1;
				doAssertOnField();
				finishElement();
				break;
			case S_FIELD:
				FIELD_end = curr_ptr - 1;
				doAssertOnField();
				finishElement();
				break;
			}
		}

		private void doAssertOnText() {
			assert TEXT_begin != NULL;
			assert TEXT_end >= TEXT_begin;
		}

		private void doAssertOnField() {
			assert FIELD_begin != NULL;
			assert FIELD_end >= FIELD_begin;
		}

		private void finishElement() {
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
					id1.set_parent(parent);
					parsed_gm.addLast(id1);
				} else {
					assert (FIELD_end >= FIELD_begin);
					S = new String(text, FIELD_begin, (FIELD_end - FIELD_begin + 1));
					ast_id id2 = ast_id.new_id(S, FIELD_begin_line, FIELD_begin_col);
					ast_field field = ast_field.new_field(id1, id2);
					field.set_parent(parent);
					parsed_gm.addLast(field);
				}
			}
			ID_begin = NULL;
			FIELD_begin = NULL;
			TEXT_begin = NULL;
		}

	}
}