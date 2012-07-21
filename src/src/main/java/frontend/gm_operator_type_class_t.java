package frontend;

public enum gm_operator_type_class_t
{
	T_INT,
	T_BOOL,
	T_NUMERIC,
	T_NUMERIC_INF, // NUMERIC + INF
	T_COMPATIBLE;

	public int getValue()
	{
		return this.ordinal();
	}

	public static gm_operator_type_class_t forValue(int value)
	{
		return values()[value];
	}
}