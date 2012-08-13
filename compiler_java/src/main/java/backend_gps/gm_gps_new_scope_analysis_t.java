package backend_gps;

public enum gm_gps_new_scope_analysis_t {
	GPS_NEW_SCOPE_GLOBAL(0), //
	GPS_NEW_SCOPE_OUT(1), //
	GPS_NEW_SCOPE_EDGE(2), //
	GPS_NEW_SCOPE_IN(3), //
	GPS_NEW_SCOPE_RANDOM(4);

	private int intValue;
	private static java.util.HashMap<Integer, gm_gps_new_scope_analysis_t> mappings;

	private static java.util.HashMap<Integer, gm_gps_new_scope_analysis_t> getMappings() {
		if (mappings == null) {
			synchronized (gm_gps_new_scope_analysis_t.class) {
				if (mappings == null) {
					mappings = new java.util.HashMap<Integer, gm_gps_new_scope_analysis_t>();
				}
			}
		}
		return mappings;
	}

	private gm_gps_new_scope_analysis_t(int value) {
		intValue = value;
		gm_gps_new_scope_analysis_t.getMappings().put(value, this);
	}

	public int getValue() {
		return intValue;
	}

	public static gm_gps_new_scope_analysis_t forValue(int value) {
		return getMappings().get(value);
	}

	private static gm_gps_new_scope_analysis_t max(gm_gps_new_scope_analysis_t a, gm_gps_new_scope_analysis_t b) {
		return a.getValue() > b.getValue() ? a : b;
	}

	public static gm_gps_new_scope_analysis_t get_more_restricted_scope(gm_gps_new_scope_analysis_t t, gm_gps_new_scope_analysis_t j) {
		// GPS_NEW_SCOPE_GLOBAL = 0,
		// GPS_NEW_SCOPE_OUT ,
		// GPS_NEW_SCOPE_EDGE,
		// GPS_NEW_SCOPE_IN ,
		// GPS_NEW_SCOPE_RANDOM, <-- most restricted
		return max(t, j);
	}
}