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
	GMOP_TYPECONVERSION, //
	GMOP_TERNARY, //
	GMOP_ASSIGN, // used in typechecking only.
	GMOP_MAPACCESS, //
	GMOP_END; // a marker indicating end of enum

	public int getValue() {
		return this.ordinal();
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
		return is_eq_op() || is_less_op();
	}

	public boolean is_ternary_op() {
		return (this == GMOP_TERNARY);
	}

	// see http://cppreference.com/wiki/language/operator_precedence
	// ABS (not in cpp)
	private static int[] GM_OPPRED_LEVEL = { 2, 3, 5, 5, 5, 2, 2, 6, 6, 13, 13, 3, 9, 9, 8, 8, 8, 8, 2, 15, 99 };

	public int get_op_pred() {
		return GM_OPPRED_LEVEL[getValue()];
	}

	public boolean gm_need_paranthesis(GM_OPS_T up_op, boolean is_right) {
		if (up_op == GMOP_TERNARY) {
			// for clarity I prefer adding ()s, except chained ternary-ops.
			// example (A+B>C) ? (D+1) : (A+C>D) ? (E+1) : (F+1)
			if ((this == GMOP_TERNARY) && is_right)
				return false;
			else
				return true;
		} else {
			if (get_op_pred() > up_op.get_op_pred())
				return true;

			else if (get_op_pred() == up_op.get_op_pred() && is_right)
				return true;

			else
				return false;
		}
	}

	public String get_op_string() {
		return (this == GMOP_MULT) ? "*" : (this == GMOP_DIV) ? "/" : (this == GMOP_SUB) ? "-" : (this == GMOP_MOD) ? "%" : (this == GMOP_ADD) ? "+"
				: (this == GMOP_NEG) ? "-" : (this == GMOP_AND) ? "&&" : (this == GMOP_OR) ? "||" : (this == GMOP_NOT) ? "!" : (this == GMOP_EQ) ? "=="
						: (this == GMOP_NEQ) ? "!=" : (this == GMOP_GT) ? ">" : (this == GMOP_LT) ? "<" : (this == GMOP_GE) ? ">=" : (this == GMOP_LE) ? "<="
								: (this == GMOP_ABS) ? "|" : (this == GMOP_TYPECONVERSION) ? "(type_conversion)" : "??";
	}
}