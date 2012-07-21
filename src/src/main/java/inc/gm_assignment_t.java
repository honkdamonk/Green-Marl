package inc;


//-------------------------------------------------------
// Assignments
//-------------------------------------------------------
public enum gm_assignment_t
{
	GMASSIGN_NORMAL,
	GMASSIGN_REDUCE,
	GMASSIGN_DEFER,
	GMASSIGN_INVALID;

	public int getValue()
	{
		return this.ordinal();
	}

	public static gm_assignment_t forValue(int value)
	{
		return values()[value];
	}
}