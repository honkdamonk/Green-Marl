package inc;


public enum GM_OPS_T
{ // list of operators
	GMOP_ABS,
	GMOP_NEG,
	GMOP_MULT,
	GMOP_DIV,
	GMOP_MOD,
	GMOP_MAX,
	GMOP_MIN,
	GMOP_ADD,
	GMOP_SUB,
	GMOP_OR,
	GMOP_AND,
	GMOP_NOT,
	GMOP_EQ,
	GMOP_NEQ,
	GMOP_LE,
	GMOP_GE,
	GMOP_LT,
	GMOP_GT,
	GMOP_TYPEC, // TYPE Conversion
	GMOP_TER, // Ternary op
	GMOP_ASSIGN, // used in typechecking only.
	GMOP_END;
// a marker indicating end of enum

	public int getValue()
	{
		return this.ordinal();
	}

	public static GM_OPS_T forValue(int value)
	{
		return values()[value];
	}
}