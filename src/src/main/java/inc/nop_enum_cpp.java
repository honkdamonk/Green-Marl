package inc;

import java.util.HashMap;

//---------------------------------------------------
// (NOPS) for CPP/CPP_LIB
//---------------------------------------------------
public enum nop_enum_cpp {
	
	NOP_REDUCE_SCALAR(1000);

	private int intValue;
	private static HashMap<Integer, nop_enum_cpp> mappings;

	private static HashMap<Integer, nop_enum_cpp> getMappings() {
		if (mappings == null) {
			synchronized (nop_enum_cpp.class) {
				if (mappings == null) {
					mappings = new HashMap<Integer, nop_enum_cpp>();
				}
			}
		}
		return mappings;
	}

	private nop_enum_cpp(int value) {
		intValue = value;
		nop_enum_cpp.getMappings().put(value, this);
	}

	public int getValue() {
		return intValue;
	}

	public static nop_enum_cpp forValue(int value) {
		return getMappings().get(value);
	}
	
}