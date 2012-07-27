package tangible;

//----------------------------------------------------------------------------------------
//	Copyright � 2006 - 2012 Tangible Software Solutions Inc.
//	This class can be used by anyone provided that the copyright notice remains intact.
//
//	This class provides the ability to simulate various classic C string functions
//	which don't have exact equivalents in the Java framework.
//----------------------------------------------------------------------------------------
public final class StringFunctions {
	// ------------------------------------------------------------------------------------
	// This method allows replacing a single character in a string, to help
	// convert
	// C++ code where a single character in a character array is replaced.
	// ------------------------------------------------------------------------------------
	public static String changeCharacter(String sourcestring, int charindex, char changechar) {
		return (charindex > 0 ? sourcestring.substring(0, charindex) : "") + Character.toString(changechar)
				+ (charindex < sourcestring.length() - 1 ? sourcestring.substring(charindex + 1) : "");
	}

	// ------------------------------------------------------------------------------------
	// This method simulates the classic C string function 'isxdigit' (and
	// 'iswxdigit').
	// ------------------------------------------------------------------------------------
	public static boolean isXDigit(char character) {
		if (Character.isDigit(character))
			return true;
		else if ("ABCDEFabcdef".indexOf(character) > -1)
			return true;
		else
			return false;
	}

	// ------------------------------------------------------------------------------------
	// This method simulates the classic C string function 'strchr' (and
	// 'wcschr').
	// ------------------------------------------------------------------------------------
	public static String strChr(String stringtosearch, char chartofind) {
		int index = stringtosearch.indexOf(chartofind);
		if (index > -1)
			return stringtosearch.substring(index);
		else
			return null;
	}

	// ------------------------------------------------------------------------------------
	// This method simulates the classic C string function 'strrchr' (and
	// 'wcsrchr').
	// ------------------------------------------------------------------------------------
	public static String strRChr(String stringtosearch, char chartofind) {
		int index = stringtosearch.lastIndexOf(chartofind);
		if (index > -1)
			return stringtosearch.substring(index);
		else
			return null;
	}

	// ------------------------------------------------------------------------------------
	// This method simulates the classic C string function 'strstr' (and
	// 'wcsstr').
	// ------------------------------------------------------------------------------------
	public static String strStr(String stringtosearch, String stringtofind) {
		int index = stringtosearch.indexOf(stringtofind);
		if (index > -1)
			return stringtosearch.substring(index);
		else
			return null;
	}

	// ------------------------------------------------------------------------------------
	// This method simulates the classic C string function 'strtok' (and
	// 'wcstok').
	// ------------------------------------------------------------------------------------
	private static String activestring;
	private static int activeposition;

	public static String strTok(String stringtotokenize, String delimiters) {
		
		if (stringtotokenize != null) {
			activestring = stringtotokenize;
			activeposition = -1;
		}

		// the stringtotokenize was never set:
		if (activestring == null)
			return null;

		// all tokens have already been extracted:
		if (activeposition == activestring.length())
			return null;

		// bypass delimiters:
		activeposition++;
		while (activeposition < activestring.length() && delimiters.indexOf(activestring.charAt(activeposition)) > -1) {
			activeposition++;
		}

		// only delimiters were left, so return null:
		if (activeposition == activestring.length())
			return null;

		// get starting position of string to return:
		int startingposition = activeposition;

		// read until next delimiter:
		do {
			activeposition++;
		} while (activeposition < activestring.length() && delimiters.indexOf(activestring.charAt(activeposition)) == -1);

		return activestring.substring(startingposition, activeposition);
	}
}