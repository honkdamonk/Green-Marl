package inc;

public enum GM_OPS_T { // list of operators
	GMOP_ABS, //
	GMOP_NEG, //
	GMOP_MULT, //
	GMOP_DIV, //
	GMOP_MOD, //
	GMOP_MAX, //
	GMOP_MIN, //
	GMOP_ADD, //
	GMOP_SUB, //
	GMOP_OR, //
	GMOP_AND, //
	GMOP_NOT, //
	GMOP_EQ, //
	GMOP_NEQ, //
	GMOP_LE, //
	GMOP_GE, //
	GMOP_LT, //
	GMOP_GT, //
	GMOP_TYPEC, // TYPE Conversion
	GMOP_TER, // Ternary op
	GMOP_ASSIGN, // used in typechecking only.
	GMOP_END;
	// a marker indicating end of enum

	public int getValue() {
		return this.ordinal();
	}

	public static GM_OPS_T forValue(int value) {
		return values()[value];
	}

	public boolean is_numeric_op() {
		return (this == GMOP_MULT) || (this == GMOP_DIV) || (this == GMOP_MOD) || (this == GMOP_ADD) || (this == GMOP_SUB) || (this == GMOP_NEG)
				|| (this == GMOP_ABS) || (this == GMOP_MAX) || (this == GMOP_MIN);
	}

	public boolean is_boolean_op() {
		return (this == GMOP_NOT) || (this == GMOP_AND) || (this == GMOP_OR);
	}

	public boolean is_eq_op() {
		return (this == GMOP_EQ) || (this == GMOP_NEQ);
	}

	public boolean is_less_op() {
		return (this == GMOP_GT) || (this == GMOP_LT) || (this == GMOP_GE) || (this == GMOP_LE);
	}

	public boolean is_eq_or_less_op() {
		return this.is_eq_op() || this.is_less_op();
	}

	public boolean is_ternary_op() {
		return (this == GMOP_TER);
	}

	// see http://cppreference.com/wiki/language/operator_precedence
	// ABS (not in cpp)
	private static int[] GM_OPPRED_LEVEL = { 2, 3, 5, 5, 5, 2, 2, 6, 6, 13, 13, 3, 9, 9, 8, 8, 8, 8, 2, 15, 99 };

	public int get_op_pred() {
		return GM_OPPRED_LEVEL[this.getValue()];
	}
}