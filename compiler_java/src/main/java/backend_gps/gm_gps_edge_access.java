package backend_gps;

public enum gm_gps_edge_access {
	GPS_ENUM_EDGE_VALUE_WRITE, //
	GPS_ENUM_EDGE_VALUE_SENT, //
	GPS_ENUM_EDGE_VALUE_SENT_WRITE, //
	GPS_ENUM_EDGE_VALUE_WRITE_SENT, //
	GPS_ENUM_EDGE_VALUE_ERROR;

	public int getValue() {
		return ordinal();
	}

}