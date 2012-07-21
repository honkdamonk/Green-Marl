package backend_gps;

// where the symbols is used 
public enum gm_gps_symbol_usage_location_t
{
	GPS_CONTEXT_MASTER,
	GPS_CONTEXT_VERTEX,
	GPS_CONTEXT_RECEIVER;

	public int getValue()
	{
		return this.ordinal();
	}

	public static gm_gps_symbol_usage_location_t forValue(int value)
	{
		return values()[value];
	}
}