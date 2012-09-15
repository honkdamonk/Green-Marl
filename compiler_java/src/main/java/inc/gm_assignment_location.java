package inc;

public enum gm_assignment_location {
	
	GMASSIGN_LHS_SCALA, //
	GMASSIGN_LHS_FIELD, //
	GMASSIGN_LHS_MAP, //
	GMASSIGN_LHS_END;

	public int getValue() {
		return this.ordinal();
	}

	public static gm_assignment_location forValue(int value) {
		return values()[value];
	}
}