package common;

import static inc.GM_OPS_T.GMOP_ABS;
import static inc.GM_OPS_T.GMOP_ADD;
import static inc.GM_OPS_T.GMOP_AND;
import static inc.GM_OPS_T.GMOP_DIV;
import static inc.GM_OPS_T.GMOP_EQ;
import static inc.GM_OPS_T.GMOP_GE;
import static inc.GM_OPS_T.GMOP_GT;
import static inc.GM_OPS_T.GMOP_LE;
import static inc.GM_OPS_T.GMOP_LT;
import static inc.GM_OPS_T.GMOP_MOD;
import static inc.GM_OPS_T.GMOP_MULT;
import static inc.GM_OPS_T.GMOP_NEG;
import static inc.GM_OPS_T.GMOP_NEQ;
import static inc.GM_OPS_T.GMOP_NOT;
import static inc.GM_OPS_T.GMOP_OR;
import static inc.GM_OPS_T.GMOP_SUB;
import static inc.GM_OPS_T.GMOP_TYPEC;
import inc.GM_OPS_T;
import inc.GM_REDUCE_T;

public class GlobalMembersGm_misc {

	// -----------------------------------------------------
	// For compiler debug,
	// mark begining/end of compiler stage (major or minor).
	// All numbering should start from 1. (not from 0)
	public static final int GMSTAGE_PARSE = 1;
	public static final int GMSTAGE_FRONTEND = 2;
	public static final int GMSTAGE_INDEPENDENT_OPT = 3;
	public static final int GMSTAGE_BACKEND_OPT = 4;
	public static final int GMSTAGE_LIBRARY_OPT = 5;
	public static final int GMSTAGE_CODEGEN = 6;

	// ------------------------------------------------------
	// Misc Utility Routines and Classes
	// ------------------------------------------------------

	public static String gm_get_op_string(GM_OPS_T op_type) {
		String opstr = (op_type == GMOP_MULT) ? "*" : (op_type == GMOP_DIV) ? "/" : (op_type == GMOP_SUB) ? "-" : (op_type == GMOP_MOD) ? "%"
				: (op_type == GMOP_ADD) ? "+" : (op_type == GMOP_NEG) ? "-" : (op_type == GMOP_AND) ? "&&" : (op_type == GMOP_OR) ? "||"
						: (op_type == GMOP_NOT) ? "!" : (op_type == GMOP_EQ) ? "==" : (op_type == GMOP_NEQ) ? "!=" : (op_type == GMOP_GT) ? ">"
								: (op_type == GMOP_LT) ? "<" : (op_type == GMOP_GE) ? ">=" : (op_type == GMOP_LE) ? "<=" : (op_type == GMOP_ABS) ? "|"
										: (op_type == GMOP_TYPEC) ? "(type_conversion)" : "??";
		return opstr;
	}

	public static String gm_get_reduce_string(GM_REDUCE_T rop_type) {
		String opstr = (rop_type == GM_REDUCE_T.GMREDUCE_PLUS) ? "+=" : (rop_type == GM_REDUCE_T.GMREDUCE_MULT) ? "*="
				: (rop_type == GM_REDUCE_T.GMREDUCE_MIN) ? "min=" : (rop_type == GM_REDUCE_T.GMREDUCE_MAX) ? "max="
						: (rop_type == GM_REDUCE_T.GMREDUCE_AND) ? "&=" : (rop_type == GM_REDUCE_T.GMREDUCE_OR) ? "|="
								: (rop_type == GM_REDUCE_T.GMREDUCE_DEFER) ? "<=" : "??";
		return opstr;
	}

	public static String gm_get_reduce_expr_string(GM_REDUCE_T rop_type) {
		String opstr = (rop_type == GM_REDUCE_T.GMREDUCE_PLUS) ? "Sum" : (rop_type == GM_REDUCE_T.GMREDUCE_MULT) ? "Product"
				: (rop_type == GM_REDUCE_T.GMREDUCE_MIN) ? "Min" : (rop_type == GM_REDUCE_T.GMREDUCE_MAX) ? "Max"
						: (rop_type == GM_REDUCE_T.GMREDUCE_AND) ? "All" : (rop_type == GM_REDUCE_T.GMREDUCE_OR) ? "Exist" : "??";
		return opstr;
	}

	// extern void gm_begin_major_compiler_stage(int major_no, String desc);
	// extern void gm_end_major_compiler_stage();
	// extern void gm_begin_minor_compiler_stage(int major_no, String desc);
	// extern void gm_end_minor_compiler_stage();
}