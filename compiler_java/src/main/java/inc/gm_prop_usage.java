package inc;

public enum gm_prop_usage {
	GMUSAGE_UNUSED, //
	GMUSAGE_IN, // Read only
	GMUSAGE_OUT, // Write all, then optionally read
	GMUSAGE_INOUT, // Read and Write
	GMUSAGE_INVALID;

	public int getValue() {
		return ordinal();
	}

	public static gm_prop_usage forValue(int value) {
		return values()[value];
	}
}
