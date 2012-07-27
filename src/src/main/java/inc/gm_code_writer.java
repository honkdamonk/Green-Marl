package inc;

import tangible.RefObject;
import backend_cpp.FILE;

public class gm_code_writer {

	public static final int MAX_COL = 1024;

	private FILE _out = new FILE(System.out);
	public int indent = 0;
	public int tabSize = 4;
	public int col = 0;
	public int max_col = MAX_COL;
	public StringBuffer _buf = new StringBuffer();
	public int base_indent = 0;

	// buffered file write.
	// (to implement 'inserting codes'
	public StringBuffer file_buf = new StringBuffer();

	public final void push_indent() {
		indent++;
	}

	public final void pop_indent() {
		indent--;
	}

	public final void set_output_file(FILE f) {
		_out = f;
	}

	public final void set_base_indent(int i) {
		base_indent = i;
	}

	public final void flush() {

		String[] lines = file_buf.toString().split("\n");
		for (String line : lines) {
			if (line.matches("\\p{Space}*")) {// consists of whitespaces
				FILE.fprintf(_out, "\n");
			} else {
				FILE.fprintf(_out, line + "\n");
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

	public final void push_to_upper(String s) {
		for (char c : s.toCharArray()) {
			push(Character.toUpperCase(c));
		}
	}

	public final void pushln(String s) {
		push(s);
		NL();
	}

	public final void push_spc(String s) {
		push(s);
		SPC();
	}

	public final void push_spc(char s) {
		push(s);
		SPC();
	}

	public final void NL() {
		push('\n');
	}

	public final void SPC() {
		push(' ');
	}

	public final void set_tab_size(int i) {
		tabSize = i;
	}

	public final void push(char s) {
		_buf.append(s);
		col++;

		assert col < MAX_COL * 2;

		if (s == '\n') {
			// First count if this sentence starts or closes an indentention
			if (_buf.charAt(0) == '}')
				indent--;

			// print this line with previous indent
			_buf.append('\0');
			col++;

			if ((col != 1) || (_buf.charAt(0) != '\n')) {
				for (int i = 0; i < tabSize * (indent + base_indent); i++) {
					file_buf.append(' ');
				}
			}

			file_buf.append(_buf);
			// compute next indent
			if (_buf.charAt(0) == '}')
				indent++;
			for (int i = 0; i < col; i++) {
				if (_buf.charAt(i) == '{')
					indent++;
				else if (_buf.charAt(i) == '(')
					indent++;
				else if (_buf.charAt(i) == '}')
					indent--;
				else if (_buf.charAt(i) == ')')
					indent--;
			}

			col = 0;
		}
	}

	public final int get_write_ptr(RefObject<Integer> curr_indent) {
		if (col != 0)
			push('\n');
		curr_indent.argvalue = indent;
		return file_buf.length();
	}

	/**
	 * assumption1: str is one line that ends with '\n' 
	 * assumption2: str does not begins a new code-block
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