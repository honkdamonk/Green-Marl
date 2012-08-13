package frontend;

public enum gm_operator_result_t
{
	RESULT_COERCION, // coercion
	RESULT_LEFT,
	RESULT_BOOL;

	public int getValue()
	{
		return this.ordinal();
	}

	public static gm_operator_result_t forValue(int value)
	{
		return values()[value];
	}
}