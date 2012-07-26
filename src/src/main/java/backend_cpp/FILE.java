package backend_cpp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class FILE extends OutputStream {

	public FILE(String fileName) throws FileNotFoundException {
	}
	
	public FILE(PrintStream out) {
		// TODO Auto-generated constructor stub
	}

	public static FILE fopen(String temp, String string) {
		try {
			return new FILE(temp);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void fclose(FILE f_header) {
		try {
			f_header.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	@Override
	public void write(int b) throws IOException {
	}

	public static void fprintf(FILE _out, String string) {
		// TODO Auto-generated method stub
		
	}

	public static void fwrite(String _temp_buf, int ptr, int i, FILE _out) {
		// TODO Auto-generated method stub
		
	}

}
