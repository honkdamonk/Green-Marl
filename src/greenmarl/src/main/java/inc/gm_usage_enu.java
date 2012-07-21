package inc;


public enum gm_usage_enu
{
	GMUSE_INARG,
	GMUSE_OUTARG,
	GMUSE_RETURN,
	GMUSE_LOCALDEF;

	public int getValue()
	{
		return this.ordinal();
	}

	public static gm_usage_enu forValue(int value)
	{
		return values()[value];
	}
}