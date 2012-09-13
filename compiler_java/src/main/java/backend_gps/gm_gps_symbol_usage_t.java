package backend_gps;

// how the symbols is used (LHS/RHS/REDUCE) 
public enum gm_gps_symbol_usage_t
{
	GPS_SYM_USED_AS_RHS,
	GPS_SYM_USED_AS_LHS,
	GPS_SYM_USED_AS_REDUCE;

	public int getValue()
	{
		return this.ordinal();
	}

	public static gm_gps_symbol_usage_t forValue(int value)
	{
		return values()[value];
	}
}