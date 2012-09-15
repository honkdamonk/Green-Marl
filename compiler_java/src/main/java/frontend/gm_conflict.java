package frontend;

// Type of conflicts
public enum gm_conflict {
	RW_CONFLICT, // Read-Write conflict (warning)
	WW_CONFLICT, // Write-Write conflict (warning)
	RD_CONFLICT, // Read-Reduce
	WD_CONFLICT, // Write-Reduce
	RM_CONFLICT, // Read-Mutate (warning)
	WM_CONFLICT, // Write-Mutate
	MM_CONFLICT; // Mutate-Mutate (warning)

	public int getValue() {
		return this.ordinal();
	}

	public static gm_conflict forValue(int value) {
		return values()[value];
	}
}