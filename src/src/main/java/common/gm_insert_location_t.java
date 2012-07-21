public enum gm_insert_location_t
{
	GM_INSERT_BEGIN,
	GM_INSERT_END,
	GM_INSERT_BEFORE,
	GM_INSERT_AFTER;

	public int getValue()
	{
		return this.ordinal();
	}

	public static gm_insert_location_t forValue(int value)
	{
		return values()[value];
	}
}