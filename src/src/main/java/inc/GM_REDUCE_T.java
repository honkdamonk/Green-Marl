package inc;


public enum GM_REDUCE_T
{
	GMREDUCE_INVALID(0),
	GMREDUCE_PLUS(1),
	GMREDUCE_MULT(2),
	GMREDUCE_MIN(3),
	GMREDUCE_MAX(4),
	GMREDUCE_AND(5), // logical AND
	GMREDUCE_OR(6), // logical OR
	GMREDUCE_AVG(7), // average (syntactic sugar)
	GMREDUCE_DEFER(8), // deferred assignment is not a reduce op. but shares a lot of properies
	GMREDUCE_NULL(9);
// dummy value to mark end

	private int intValue;
	private static java.util.HashMap<Integer, GM_REDUCE_T> mappings;
	private static java.util.HashMap<Integer, GM_REDUCE_T> getMappings()
	{
		if (mappings == null)
		{
			synchronized (GM_REDUCE_T.class)
			{
				if (mappings == null)
				{
					mappings = new java.util.HashMap<Integer, GM_REDUCE_T>();
				}
			}
		}
		return mappings;
	}

	private GM_REDUCE_T(int value)
	{
		intValue = value;
		GM_REDUCE_T.getMappings().put(value, this);
	}

	public int getValue()
	{
		return intValue;
	}

	public static GM_REDUCE_T forValue(int value)
	{
		return getMappings().get(value);
	}
}