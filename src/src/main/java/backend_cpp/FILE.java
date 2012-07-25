package backend_cpp;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FILE extends FileOutputStream {

	public FILE(String fileName) throws FileNotFoundException {
		super(fileName);
	}

	public static FILE fopen(String temp, String string) {
		try {
			return new FILE(temp);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public static void fclose(FILE f_header) {
		try {
			f_header.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

}
