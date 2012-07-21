//----------------------------------------
// A class that stores command line arguments 
//----------------------------------------
public class gm_lesscstr
{
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: boolean operator ()(String s1, String s2) const
//C++ TO JAVA CONVERTER TODO TASK: The following operator cannot be converted to Java:
	boolean operator ()(String s1, String s2)
	{
		return strcmp(s1, s2) < 0;
	}
}