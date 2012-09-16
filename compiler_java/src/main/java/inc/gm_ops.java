package inc;

public enum gm_ops { // list of operators
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
	private static int[] GM_OPPRED_LEVEL = {
			2,           // ABS (not in cpp)
	        3,           // NEG
	        5, 5, 5,     // MULT, DIV, MOD
	        2, 2,        // MAX, MIN
	        6, 6,        // ADD, SUB
	        13, 13,      // OR, AND,   // Actually AND has higher pred in parsing. But for clarity, we regard them to have same prec in code generation.
	        3,           // NOT,
	        9, 9,        // EQ, NEQ
	        8, 8, 8, 8,  // LE, GE, LT, GT
	        2,           // TYPE
	        15,          // TERNARY
	        99,          // ASSIGN (for type=checking only)
	        //TODO missing value for MAPACCESS
	        };

	public int get_op_pred() {
		return GM_OPPRED_LEVEL[ordinal()];
	}

	public boolean gm_need_paranthesis(gm_ops up_op, boolean is_right) {
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