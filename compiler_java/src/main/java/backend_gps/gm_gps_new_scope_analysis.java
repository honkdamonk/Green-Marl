package backend_gps;

public enum gm_gps_new_scope_analysis {
	GPS_NEW_SCOPE_GLOBAL, // <-- least restricted
	GPS_NEW_SCOPE_OUT, //
	GPS_NEW_SCOPE_EDGE, //
	GPS_NEW_SCOPE_IN, //
	GPS_NEW_SCOPE_RANDOM; // <-- most restricted

	public static gm_gps_new_scope_analysis get_more_restricted_scope(gm_gps_new_scope_analysis t, gm_gps_new_scope_analysis j) {
		return t.ordinal() > j.ordinal() ? t : j;
	}
}