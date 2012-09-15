package inc;

public enum gm_reduce {

	GMREDUCE_INVALID, //
	GMREDUCE_PLUS, //
	GMREDUCE_MULT, //
	GMREDUCE_MIN, //
	GMREDUCE_MAX, //
	GMREDUCE_AND, // logical AND
	GMREDUCE_OR, // logical OR
	GMREDUCE_AVG, // average (syntactic sugar)
	GMREDUCE_DEFER, // deferred assignment is not a reduce op. but shares a lot
					// of properies
	GMREDUCE_NULL; // dummy value to mark end

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
				: (this == GMREDUCE_AND) ? "All" : (this == GMREDUCE_OR) ? "Exist" : (this == GMREDUCE_AVG) ? "Avg" : "??";
	}

	public String get_reduce_string() {
		return (this == GMREDUCE_PLUS) ? "+=" : (this == GMREDUCE_MULT) ? "*=" : (this == GMREDUCE_MIN) ? "min=" : (this == GMREDUCE_MAX) ? "max="
				: (this == GMREDUCE_AND) ? "&=" : (this == GMREDUCE_OR) ? "|=" : (this == GMREDUCE_DEFER) ? "<=" : "??";
	}

	public String get_reduce_base_value(gm_type gm_type) {
		switch (this) {
		case GMREDUCE_PLUS:
			return "0";
		case GMREDUCE_MULT:
			return "1";
		case GMREDUCE_AND:
			return "true";
		case GMREDUCE_OR:
			return "false";
		case GMREDUCE_MIN:
			switch (gm_type) {
			case GMTYPE_INT:
				return "Integer.MAX_VALUE";
			case GMTYPE_LONG:
				return "Long.MAX_VALUE";
			case GMTYPE_FLOAT:
				return "Float.MAX_VALUE";
			case GMTYPE_DOUBLE:
				return "Double.MAX_VALUE";
			default:
				assert false;
				return "0";
			}
		case GMREDUCE_MAX:
			switch (gm_type) {
			case GMTYPE_INT:
				return "Integer.MIN_VALUE";
			case GMTYPE_LONG:
				return "Long.MIN_VALUE";
			case GMTYPE_FLOAT:
				return "Float.MIN_VALUE";
			case GMTYPE_DOUBLE:
				return "Double.MIN_VALUE";
			default:
				assert false;
				return "0";
			}
		default:
			assert false;
			break;
		}
		return "0";
	}
}