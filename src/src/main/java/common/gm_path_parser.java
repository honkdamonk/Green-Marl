package common;

import java.io.File;

//------------------------------------------
// parsing path string
//------------------------------------------
public class gm_path_parser {

	private String name;
	private String extention;
	private String path;

	// defined in gm_misc.cc
	public final void parsePath(String fullPath) {
		
		File file = new File(fullPath);
		
		path = file.getParent();
		String fileName = file.getName();
		
		String[] fileTokens = fileName.split("[.]");
		assert fileTokens.length < 2;
		
		if (fileTokens.length == 1) {
			extention = "";
			name = fileTokens[0];
		} else {
			extention = "." + fileTokens[fileTokens.length - 1];
			name = fileName.substring(0, fileName.length() - extention.length());
		}
		
		name = (name == null) ? "" : name;
		path = "./" + ((path == null) ? "" : path + "/");
	}

	public final String getPath() {
		return path;
	}

	/**
	 * @return Returns the filename without path and extension.
	 */
	public final String getFilename() {
		return name;
	}

	
	public final String getExt() {
		return extention;
	}
	
	public static void main(String[] args) {
		gm_path_parser parser = new gm_path_parser();
		test(parser, "data/bla.bin");
		test(parser, "bla.bin");
		test(parser, "data/bla");
		test(parser, "bla");
		test(parser, "data/");
	}

	private static void test(gm_path_parser parser, String x) {
		parser.parsePath(x);
		System.out.println(x + ":\t" + parser.path + "|" + parser.name + "|" + parser.extention);
	}

}