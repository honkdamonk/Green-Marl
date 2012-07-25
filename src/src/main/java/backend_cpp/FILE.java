package backend_cpp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

public class FILE extends OutputStream {

	public FILE(String fileName) throws FileNotFoundException {
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

}
