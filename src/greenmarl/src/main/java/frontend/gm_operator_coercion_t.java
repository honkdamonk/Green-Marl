public enum gm_operator_coercion_t
{
	COERCION_ALL, // coercion
	COERCION_RIGHT, // coercion
	COERCION_NO; // no-coercion

	public int getValue()
	{
		return this.ordinal();
	}

	public static gm_operator_coercion_t forValue(int value)
	{
		return values()[value];
	}
}