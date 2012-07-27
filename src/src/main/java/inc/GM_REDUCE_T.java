package inc;

import java.util.HashMap;

public enum GM_REDUCE_T {
	
	GMREDUCE_INVALID(0), //
	GMREDUCE_PLUS(1), //
	GMREDUCE_MULT(2), //
	GMREDUCE_MIN(3), //
	GMREDUCE_MAX(4), //
	GMREDUCE_AND(5), // logical AND
	GMREDUCE_OR(6), // logical OR
	GMREDUCE_AVG(7), // average (syntactic sugar)
	GMREDUCE_DEFER(8), // deferred assignment is not a reduce op. but shares a
						// lot of properies
	GMREDUCE_NULL(9);
	// dummy value to mark end

	private int intValue;
	private static HashMap<Integer, GM_REDUCE_T> mappings;

	private static HashMap<Integer, GM_REDUCE_T> getMappings() {
		if (mappings == null) {
			synchronized (GM_REDUCE_T.class) {
				if (mappings == null) {
					mappings = new HashMap<Integer, GM_REDUCE_T>();
				}
			}
		}
		return mappings;
	}

	private GM_REDUCE_T(int value) {
		intValue = value;
		getMappings().put(value, this);
	}

	public int getValue() {
		return intValue;
	}

	public static GM_REDUCE_T forValue(int value) {
		return getMappings().get(value);
	}

	public boolean is_strict_reduce_op() {
		return (this == GMREDUCE_PLUS) || (this == GMREDUCE_MULT) || (this == GMREDUCE_MIN) || (this == GMREDUCE_MAX) || (this == GMREDUCE_AND)
				|| (this == GMREDUCE_OR) || (this == GMREDUCE_AVG);
	}

	public boolean is_numeric_reduce_op() {
		return (this == GMREDUCE_PLUS) || (this == GMREDUCE_MULT) || (this == GMREDUCE_MIN) || (this == GMREDUCE_MAX) || (this == GMREDUCE_AVG);
	}

	public boolean is_boolean_reduce_op() {
		return (this == GMREDUCE_AND) || (this == GMREDUCE_OR);
	}

	public String get_reduce_expr_string() {
		return (this == GMREDUCE_PLUS) ? "Sum" : (this == GMREDUCE_MULT) ? "Product" : (this == GMREDUCE_MIN) ? "Min" : (this == GMREDUCE_MAX) ? "Max"
				: (this == GMREDUCE_AND) ? "All" : (this == GMREDUCE_OR) ? "Exist" : "??";
	}

	public String get_reduce_string() {
		return (this == GMREDUCE_PLUS) ? "+=" : (this == GMREDUCE_MULT) ? "*=" : (this == GMREDUCE_MIN) ? "min=" : (this == GMREDUCE_MAX) ? "max="
				: (this == GMREDUCE_AND) ? "&=" : (this == GMREDUCE_OR) ? "|=" : (this == GMREDUCE_DEFER) ? "<=" : "??";
	}
}