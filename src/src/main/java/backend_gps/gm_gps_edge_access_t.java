package backend_gps;

public enum gm_gps_edge_access_t
{
	GPS_ENUM_EDGE_VALUE_WRITE,
	GPS_ENUM_EDGE_VALUE_SENT,
	GPS_ENUM_EDGE_VALUE_SENT_WRITE,
	GPS_ENUM_EDGE_VALUE_WRITE_SENT,
	GPS_ENUM_EDGE_VALUE_ERROR;

	public int getValue()
	{
		return this.ordinal();
	}

	public static gm_gps_edge_access_t forValue(int value)
	{
		return values()[value];
	}
}