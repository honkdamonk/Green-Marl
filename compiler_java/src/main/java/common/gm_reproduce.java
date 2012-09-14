package common;

import inc.gm_code_writer;

import java.io.PrintStream;

public class gm_reproduce {

	public static final gm_code_writer Out = new gm_code_writer();

	public static void gm_flush_reproduce() {
		Out.flush();
	}

	public static void gm_newline_reproduce() {
		Out.NL();
	}

	public static void gm_redirect_reproduce(PrintStream ps) {
		Out.setOutputFile(ps);
	}

	public static void gm_baseindent_reproduce(int i) {
		Out.setBaseIndent(i);
	}

	public static void gm_push_reproduce(String s) {
		Out.push(s);
	}
}
