package inc;


//-------------------------------------------------------
// Class of Expressions
//-------------------------------------------------------
public enum GMEXPR_CLASS
{
	GMEXPR_IVAL(0), // integer literal
	GMEXPR_FVAL(1), // floating literal
	GMEXPR_BVAL(2), // boolean literal
	GMEXPR_INF(3), // infinite literal
	GMEXPR_NIL(4), // NIL literal
	GMEXPR_ID(5), // identifier
	GMEXPR_FIELD(6), // field access
	GMEXPR_UOP(7), // unary op (neg)
	GMEXPR_LUOP(8), // logical not
	GMEXPR_BIOP(9), // numeric binary op
	GMEXPR_LBIOP(10), // logical binary op
	GMEXPR_COMP(11), // comparision ops (==, !=, <, >, <=, >=)
	GMEXPR_REDUCE(12), // reduction ops (Sum, Product, Min, Max)
	GMEXPR_BUILTIN(13), // builtin ops (NumNodes, NumNbrs, ...)
	GMEXPR_BUILTIN_FIELD(14), //builtin ops on property entries
	GMEXPR_TER(15), // ternary operation
	GMEXPR_FOREIGN(16);
// foreign expression

	private int intValue;
	private static java.util.HashMap<Integer, GMEXPR_CLASS> mappings;
	private static java.util.HashMap<Integer, GMEXPR_CLASS> getMappings()
	{
		if (mappings == null)
		{
			synchronized (GMEXPR_CLASS.class)
			{
				if (mappings == null)
				{
					mappings = new java.util.HashMap<Integer, GMEXPR_CLASS>();
				}
			}
		}
		return mappings;
	}

	private GMEXPR_CLASS(int value)
	{
		intValue = value;
		GMEXPR_CLASS.getMappings().put(value, this);
	}

	public int getValue()
	{
		return intValue;
	}

	public static GMEXPR_CLASS forValue(int value)
	{
		return getMappings().get(value);
	}
}