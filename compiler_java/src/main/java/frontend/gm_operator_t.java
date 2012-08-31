package frontend;

//------------------------------------------------------------------------
// CLASS NUMERIC OP

//------------------------------------------------------------------------
public enum gm_operator_t
{
	INT_OP(0), // %
	NUMERIC_OP(1), // +,-,*,/, Max, Min
	BOOL_OP(2), // And, Or
	COMP_OP(3), // <,<=,>,>=
	EQ_OP(4), // == !=
	TER_OP(5), // ? t1 : t2
	ASSIGN_OP(6); // =

	private int intValue;
	private static java.util.HashMap<Integer, gm_operator_t> mappings;
	private static java.util.HashMap<Integer, gm_operator_t> getMappings()
	{
		if (mappings == null)
		{
			synchronized (gm_operator_t.class)
			{
				if (mappings == null)
				{
					mappings = new java.util.HashMap<Integer, gm_operator_t>();
				}
			}
		}
		return mappings;
	}

	private gm_operator_t(int value)
	{
		intValue = value;
		gm_operator_t.getMappings().put(value, this);
	}

	public int getValue()
	{
		return intValue;
	}

	public static gm_operator_t forValue(int value)
	{
		return getMappings().get(value);
	}
}