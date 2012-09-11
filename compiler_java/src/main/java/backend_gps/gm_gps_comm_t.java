package backend_gps;


public enum gm_gps_comm_t
{
	GPS_COMM_NESTED,       // communication for nested loop
	GPS_COMM_RANDOM_WRITE, // communication due to random write
	GPS_COMM_INIT;         // reverse edge genertor at zero step

	public int getValue()
	{
		return this.ordinal();
	}

	public static gm_gps_comm_t forValue(int value)
	{
		return values()[value];
	}
}