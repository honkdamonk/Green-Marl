package inc;

import backend_cpp.FILE;

//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define TO_STR(X) #X
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define DEF_STRING(X) static const char *X = "X"

public class gm_code_writer {
	public static final int MAX_COL = 1024;

	public FILE _out = new FILE(System.out);
	public int indent = 0;
	public int tabsz = 4;
	public int col = 0;
	public int max_col = MAX_COL;
	public String _buf;
	public int base_indent = 0;

	// buffered file write.
	// (to implement 'inserting codes'
	public int file_ptr = 0;
	// char file_buf[8*1024*1024]; // source code should be
	public String file_buf;

	public gm_code_writer() {
		// _buf = new byte[MAX_COL * 2]; // one line buffer
		// file_buf = new byte[32 * 1024 * 1024]; // 32MB. should be enough for
		// a file
	}

	public void dispose() {
		_buf = null;
		file_buf = null;
	}

	public final void push_indent() {
		indent++;
	}

	public final void pop_indent() {
		indent--;
	}

	public final void set_output_file(FILE f) {
		_out = f;
		file_ptr = 0;
	}

	public final void set_base_indent(int i) {
		base_indent = i;
	}

	public final void flush() {

		String[] lines = file_buf.split("\n");
		for (String line : lines) {
			if (line.matches("\\p{Space}*")) {// consists of whitespaces
				FILE.fprintf(_out, "\n");
			} else {
				FILE.fprintf(_out, line + "\n");
			}
		}
		col = 0;
		file_ptr = 0;
	}

	public final void push(String s) {
		int l = s.length();
		for (int i = 0; i < l; i++) {
			push(s.charAt(i));
		}
	}

	public final void push_to_upper(String s) {
		int l = s.length();
		for (int i = 0; i < l; i++) {
			if (Character.isLowerCase(s.charAt(i)))
				push(Character.toUpperCase(s.charAt(i)));
			else
				push(s.charAt(i));
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
		tabsz = i;
	}

	public final void push(char s) {

		_buf += s;
		col++;

		assert col > MAX_COL * 2;

		if (s == '\n') {
			// First count if this sentence starts or closes an indentention
			if (_buf.charAt(0) == '}')
				indent--;

			// print this line with previous indent
			_buf += '\0';
			col++;

			if ((col != 1) || (_buf.charAt(0) != '\n')) {
				for (int i = 0; i < tabsz * (indent + base_indent); i++) {
					file_buf += ' ';
					file_ptr++;
				}
			}

			// fprintf(_out, "%s", _buf);
			// file_ptr += sprintf(&file_buf[file_ptr], "[indent:%d]\n",
			// indent);
			file_ptr += _buf.length();
			file_buf += _buf;

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

	public final int get_write_ptr(tangible.RefObject<Integer> curr_indent) {
		if (col != 0)
			push('\n');
		curr_indent.argvalue = indent;
		return file_ptr;
	}

	// assumption1: str is one line that ends with '\n'
	// assumption2: str does not begins a new code-block
	public final void insert_at(int ptr, int then_indent, String str) {
		assert str != null;
		assert ptr < file_ptr;
		if (ptr == file_ptr)
			return;
		if (col != 0)
			push('\n');

		int len = str.length();
		int to_move = len + tabsz * then_indent;
		// memmove( &file_buf[ptr + to_move], &file_buf[ptr], to_move);
		for (int i = file_ptr + to_move - 1; i >= (ptr + to_move); i--) {
			// printf("%c", file_buf[i-to_move]);
			file_buf = tangible.StringFunctions.changeCharacter(file_buf, i, file_buf.charAt(i - to_move));
		}

		for (int i = 0; i < tabsz * then_indent; i++) {
			file_buf = tangible.StringFunctions.changeCharacter(file_buf, ptr++, ' ');
		}
		for (int i = 0; i < len; i++) {
			file_buf = tangible.StringFunctions.changeCharacter(file_buf, ptr++, str.charAt(i));
		}
		// printf("ptr= %d\n", ptr);
		// printf("file_ptr= %d\n", file_ptr);
		// printf("to_move = %d\n", to_move);

		file_ptr += to_move;
	}

}