package backend_gps;

// where the symbol is defined
public enum gm_gps_scope_t
{
	GPS_SCOPE_GLOBAL,
	GPS_SCOPE_OUTER,
	GPS_SCOPE_INNER;

	public int getValue()
	{
		return this.ordinal();
	}

	public static gm_gps_scope_t forValue(int value)
	{
		return values()[value];
	}
}