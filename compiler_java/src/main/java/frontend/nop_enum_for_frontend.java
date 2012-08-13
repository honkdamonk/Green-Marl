package frontend;

public enum nop_enum_for_frontend
{
	NOP_DUMMY_MARKER;

	public int getValue()
	{
		return this.ordinal();
	}

	public static nop_enum_for_frontend forValue(int value)
	{
		return values()[value];
	}
} // NOPs used in front-end