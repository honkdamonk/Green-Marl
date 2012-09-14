package inc;

//---------------------------------------------------
// (NOPS) for CPP/CPP_LIB
//---------------------------------------------------
public enum nop_enum_cpp {

	NOP_REDUCE_SCALAR;

	public static nop_enum_cpp forValue(int value) {
		return values()[value];
	}

}