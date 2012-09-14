package inc;

import java.io.PrintStream;

import tangible.RefObject;

public class gm_code_writer {

	private static final int MAX_COL = 1024;

	private PrintStream _out = System.out;
	private int indent = 0;
	private int tabSize = 4;
	private int col = 0;
	private StringBuffer line_buf = new StringBuffer();
	private int base_indent = 0;

	// buffered file write.
	// (to implement 'inserting codes'
	private StringBuffer file_buf = new StringBuffer();

	public final void setBaseIndent(int i) {
		base_indent = i;
	}

	public final void pushIndent() {
		indent++;
	}

	public final void popIndent() {
		indent--;
	}

	public final void setOutputFile(PrintStream out) {
		_out = out;
	}

	public final void flush() {
		String[] lines = file_buf.toString().split("\n");
		for (String line : lines) {
			if (line.matches("[\\s]*")) {// consists of whitespace
				_out.printf("\n");
			} else {
				_out.printf(line + "\n");
			}
		}
		col = 0;
		file_buf = new StringBuffer();
	}

	public final void push(String s) {
		for (char c : s.toCharArray()) {
			push(c);
		}
	}

	public final void pushToUpper(String s) {
		for (char c : s.toCharArray()) {
			push(Character.toUpperCase(c));
		}
	}

	public final void pushln(String s) {
		push(s);
		NL();
	}

	public final void pushSpace(String s) {
		push(s);
		SPC();
	}

	public final void pushSpace(char s) {
		push(s);
		SPC();
	}

	public final void NL() {
		push('\n');
	}

	public final void SPC() {
		push(' ');
	}

	public final void setTabSize(int i) {
		tabSize = i;
	}

	public final void push(char s) {
		line_buf.append(s);
		col++;

		assert col < MAX_COL * 2;

		if (s == '\n') {
			// First count if this sentence starts or closes an indentation
			if (line_buf.charAt(0) == '}')
				indent--;

			// print this line with previous indent
			if ((col != 1) || (line_buf.charAt(0) != '\n')) {
				for (int i = 0; i < tabSize * (indent + base_indent); i++) {
					file_buf.append(' ');
				}
			}
			file_buf.append(line_buf);
			
			// compute next indent
			if (line_buf.charAt(0) == '}')
				indent++;
			for (int i = 0; i < col; i++) {
				switch (line_buf.charAt(i)) {
				case '{':
				case '(':
					indent++;
					break;
				case '}':
				case ')':
					indent--;
					break;
				}
			}
			col = 0;
			line_buf = new StringBuffer();
		}
	}

	public final int get_write_ptr(RefObject<Integer> curr_indent) {
		if (col != 0)
			push('\n');
		curr_indent.argvalue = indent;
		return file_buf.length();
	}

	/**
	 * assumption1: str is one line that ends with '\n' assumption2: str does
	 * not begins a new code-block
	 **/
	public final void insert_at(int ptr, int then_indent, String str) {
		assert str != null;
		assert ptr < file_buf.length();
		if (ptr == file_buf.length())
			return;
		if (col != 0)
			push('\n');

		int len = str.length();
		int to_move = len + tabSize * then_indent;

		int size = file_buf.length();
		for (int i = size + to_move - 1; i >= (ptr + to_move); i--) {
			file_buf.setCharAt(i, file_buf.charAt(i - to_move));
		}

		for (int i = 0; i < tabSize * then_indent; i++) {
			file_buf.setCharAt(ptr++, ' ');
		}
		for (int i = 0; i < len; i++) {
			file_buf.setCharAt(ptr++, str.charAt(i));
		}
	}

}