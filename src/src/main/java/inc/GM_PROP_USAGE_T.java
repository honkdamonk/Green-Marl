package inc;

public enum GM_PROP_USAGE_T {
	GMUSAGE_UNUSED, GMUSAGE_IN, // Read only
	GMUSAGE_OUT, // Write all, then optionally read
	GMUSAGE_INOUT, // Read and Write
	GMUSAGE_INVALID;

	public int getValue() {
		return this.ordinal();
	}

	public static GM_PROP_USAGE_T forValue(int value) {
		return values()[value];
	}
}
