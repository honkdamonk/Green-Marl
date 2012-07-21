package opt;

import backend_cpp.*;
import backend_giraph.*;
import common.*;
import frontend.*;
import inc.*;
import tangible.*;

public class triple_comp_t
{
//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: boolean operator ()(const triple_t& lhs, const triple_t& rhs) const
//C++ TO JAVA CONVERTER TODO TASK: The following operator cannot be converted to Java:
	boolean operator ()(triple_t lhs, triple_t rhs)
	{
		if (lhs.bound < rhs.bound)
			return true;
		else if (lhs.bound > rhs.bound)
			return false;
		else if (lhs.target < rhs.target)
			return true;
		else if (lhs.target > rhs.target)
			return false;
		else if (lhs.is_rev_bfs < rhs.is_rev_bfs)
			return true;
		else if (lhs.is_rev_bfs > rhs.is_rev_bfs)
			return false;
		else
			return false;
	}
}