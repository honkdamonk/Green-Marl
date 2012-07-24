package inc;

public enum GM_ACCESS_T { // 16 bit bitmap
	GMACCESS_NONE(0x0000), //
	GMACCESS_EMPTY(0x0001), //
	GMACCESS_SHRINK(0x0002), //
	GMACCESS_GROW(0x0004), //
	GMACCESS_FULL(0x0008), //
	GMACCESS_LOOKUP(0x0010), //
	GMACCESS_COPY(0x0020);//

	private int intValue;
	private static java.util.HashMap<Integer, GM_ACCESS_T> mappings;

	private static java.util.HashMap<Integer, GM_ACCESS_T> getMappings() {
		if (mappings == null) {
			synchronized (GM_ACCESS_T.class) {
				if (mappings == null) {
					mappings = new java.util.HashMap<Integer, GM_ACCESS_T>();
				}
			}
		}
		return mappings;
	}

	private GM_ACCESS_T(int value) {
		intValue = value;
		GM_ACCESS_T.getMappings().put(value, this);
	}

	public int getValue() {
		return intValue;
	}

	public static GM_ACCESS_T forValue(int value) {
		return getMappings().get(value);
	}

	public boolean is_collection_access_none() {
		return (this == GM_ACCESS_T.GMACCESS_NONE);
	}
}