package common;

import java.io.File;

//------------------------------------------
// parsing path string
//------------------------------------------
public class gm_path_parser {

	private String name;
	private String extention;
	private String path;

	public final void parsePath(String fullPath) {
		
		File file = new File(fullPath);
		
		path = file.getParent();
		String fileName = file.getName();
		
		String[] fileTokens = fileName.split("[.]");
		assert fileTokens.length <= 2;
		
		if (fileTokens.length == 1) {
			extention = "";
			name = fileTokens[0];
		} else {
			extention = fileTokens[fileTokens.length - 1];
			name = fileName.substring(0, fileName.length() - extention.length() - 1);
		}
		
		name = (name == null) ? "" : name;
		path = ((path == null) ? "." : path + "/");
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

}