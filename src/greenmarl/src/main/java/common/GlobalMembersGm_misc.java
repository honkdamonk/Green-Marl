import inc.GMTYPE_T;
import inc.GM_OPS_T;
import inc.GM_REDUCE_T;
import inc.GlobalMembersGm_defs;

public class GlobalMembersGm_misc
{
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define TO_STR(X) #X
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define DEF_STRING(X) static const char *X = "X"

	//------------------------------------------------------
	// Misc Utility Routines and Classes
	//------------------------------------------------------

	public static String gm_strdup(String c)
	{
		assert c != null;
		String z = new byte[c.length() + 1];
		z = c;
		return z;
	}
	public static String gm_get_nodetype_string(int t)
	{
		switch (t)
		{
			case AST_ID:
				return "AST_ID";
			case AST_FIELD:
				return "AST_FIELD";
			case AST_PROCDEF:
				return "AST_PROCDEF";
			case AST_EXPR:
				return "AST_EXPR";
			case AST_EXPR_RDC:
				return "AST_EXPR_RDC";
			case AST_EXPR_BUILTIN:
				return "AST_EXPR_BUILTIN";
			case AST_EXPR_FOREIGN:
				return "AST_EXPR_FOREIGN";
			case AST_SENT:
				return "AST_SENT";
			case AST_SENTBLOCK:
				return "AST_SENTBLOCK";
			case AST_ASSIGN:
				return "AST_ASSIGN";
			case AST_VARDECL:
				return "AST_VARDECL";
			case AST_FOREACH:
				return "AST_FOREACH";
			case AST_IF:
				return "AST_IF";
			case AST_WHILE:
				return "AST_WHILE";
			case AST_RETURN:
				return "AST_RETURN";
			case AST_BFS:
				return "AST_BFS";
			case AST_CALL:
				return "AST_CALL";
			case AST_FOREIGN:
				return "AST_FOREIGN";
			case AST_NOP:
				return "AST_NOP";
			default:
				return "?";
		}
	}

// for debug-print only
	public static String gm_get_type_string(int t)
	{
		switch (t)
		{
			case GMTYPE_GRAPH:
				return "Graph";
			case GMTYPE_BYTE:
				return "Byte";
			case GMTYPE_SHORT:
				return "Short";
			case GMTYPE_INT:
				return "Int";
			case GMTYPE_LONG:
				return "Long";
			case GMTYPE_FLOAT:
				return "Float";
			case GMTYPE_DOUBLE:
				return "Double";
			case GMTYPE_NODEPROP:
				return "NP";
			case GMTYPE_EDGEPROP:
				return "EP";
			case GMTYPE_NODE:
				return "Node";
			case GMTYPE_EDGE:
				return "Edge";
			case GMTYPE_NODEITER_ALL:
				return "Node::I(ALL)";
			case GMTYPE_NODEITER_NBRS:
				return "Node::I(NBR)";
			case GMTYPE_NODEITER_IN_NBRS:
				return "Node::I(IN_NBR)";
			case GMTYPE_NODEITER_BFS:
				return "Node::I(BFS)";
			case GMTYPE_NODEITER_UP_NBRS:
				return "Node::I(+1)";
			case GMTYPE_NODEITER_DOWN_NBRS:
				return "Node::I(-1)";
			case GMTYPE_EDGEITER_ALL:
				return "EdgeI";
			case GMTYPE_EDGEITER_NBRS:
				return "EdgeI";
			case GMTYPE_BOOL:
				return "Bool";
			case GMTYPE_NSET:
				return "Node_Set";
			case GMTYPE_NORDER:
				return "Node_Order";
			case GMTYPE_NSEQ:
				return "Node_Sequence";
			case GMTYPE_ESET:
				return "Edge_Set";
			case GMTYPE_EORDER:
				return "Edge_Order";
			case GMTYPE_ESEQ:
				return "Edge_Sequence";
			case GMTYPE_INF:
			case GMTYPE_INF_INT:
			case GMTYPE_INF_LONG:
			case GMTYPE_INF_DOUBLE:
			case GMTYPE_INF_FLOAT:
				return "INF";
			case GMTYPE_NODEITER_SET:
				return "N_S::I";
			case GMTYPE_NODEITER_SEQ:
				return "N_Q::I";
			case GMTYPE_NODEITER_ORDER:
				return "N_O::I";
			case GMTYPE_EDGEITER_SET:
				return "E_S::I";
			case GMTYPE_EDGEITER_SEQ:
				return "E_Q::I";
			case GMTYPE_EDGEITER_ORDER:
				return "E_O::I";
			case GMTYPE_ITER_ANY:
				return "Collection::I";
			case GMTYPE_VOID:
				return "Void";

			default: //printf("%d\n",t); assert(false);
				return "Unknown";
		}
	}
	public static String gm_get_iter_type_string(int t)
	{
		switch (t)
		{
			case GMTYPE_NODEITER_ALL:
				return "Nodes";
			case GMTYPE_NODEITER_NBRS:
				return "Nbrs";
			case GMTYPE_EDGEITER_ALL:
				return "Edges";
			case GMTYPE_EDGEITER_NBRS:
				return "Nbr_Edges";
			case GMTYPE_NODEITER_UP_NBRS:
				return "UpNbrs";
			case GMTYPE_NODEITER_DOWN_NBRS:
				return "DownNbrs";
			case GMTYPE_NODEITER_IN_NBRS:
				return "InNbrs";
			case GMTYPE_NODEITER_COMMON_NBRS:
				return "CommonNbrs";

			case GMTYPE_NODEITER_SET:
				return "Items";
			case GMTYPE_NODEITER_SEQ:
				return "Items";
			case GMTYPE_NODEITER_ORDER:
				return "Items";
			case GMTYPE_ITER_ANY:
				return "Items";

			default:
				assert false;
				return "Unknown";
		}
	}
	public static String gm_get_op_string(int op_type)
	{
		String opstr = (op_type == GM_OPS_T.GMOP_MULT.getValue()) ? "*" : (op_type == GM_OPS_T.GMOP_DIV.getValue()) ? "/" : (op_type == GM_OPS_T.GMOP_SUB.getValue()) ? "-" : (op_type == GM_OPS_T.GMOP_MOD.getValue()) ? "%" : (op_type == GM_OPS_T.GMOP_ADD.getValue()) ? "+" : (op_type == GM_OPS_T.GMOP_NEG.getValue()) ? "-" : (op_type == GM_OPS_T.GMOP_AND.getValue()) ? "&&" : (op_type == GM_OPS_T.GMOP_OR.getValue()) ? "||" : (op_type == GM_OPS_T.GMOP_NOT.getValue()) ? "!" : (op_type == GM_OPS_T.GMOP_EQ.getValue()) ? "==" : (op_type == GM_OPS_T.GMOP_NEQ.getValue()) ? "!=" : (op_type == GM_OPS_T.GMOP_GT.getValue()) ? ">" : (op_type == GM_OPS_T.GMOP_LT.getValue()) ? "<" : (op_type == GM_OPS_T.GMOP_GE.getValue()) ? ">=" : (op_type == GM_OPS_T.GMOP_LE.getValue()) ? "<=" : (op_type == GM_OPS_T.GMOP_ABS.getValue()) ? "|" : (op_type == GM_OPS_T.GMOP_TYPEC.getValue()) ? "(type_conversion)" : "??";
		return opstr;
	}
	public static String gm_get_reduce_string(int rop_type)
	{
		String opstr = (rop_type == GM_REDUCE_T.GMREDUCE_PLUS.getValue()) ? "+=" : (rop_type == GM_REDUCE_T.GMREDUCE_MULT.getValue()) ? "*=" : (rop_type == GM_REDUCE_T.GMREDUCE_MIN.getValue()) ? "min=" : (rop_type == GM_REDUCE_T.GMREDUCE_MAX.getValue()) ? "max=" : (rop_type == GM_REDUCE_T.GMREDUCE_AND.getValue()) ? "&=" : (rop_type == GM_REDUCE_T.GMREDUCE_OR.getValue()) ? "|=" : (rop_type == GM_REDUCE_T.GMREDUCE_DEFER.getValue()) ? "<=" : "??";
		return opstr;
	}
	public static String gm_get_reduce_expr_string(int rop_type)
	{
		String opstr = (rop_type == GM_REDUCE_T.GMREDUCE_PLUS.getValue()) ? "Sum" : (rop_type == GM_REDUCE_T.GMREDUCE_MULT.getValue()) ? "Product" : (rop_type == GM_REDUCE_T.GMREDUCE_MIN.getValue()) ? "Min" : (rop_type == GM_REDUCE_T.GMREDUCE_MAX.getValue()) ? "Max" : (rop_type == GM_REDUCE_T.GMREDUCE_AND.getValue()) ? "All" : (rop_type == GM_REDUCE_T.GMREDUCE_OR.getValue()) ? "Exist" : "??";
		return opstr;
	}
	public static int gm_get_op_pred(int op_type)
	{
		return GlobalMembersGm_defs.GM_OPPRED_LEVEL[op_type];
	}
	public static boolean gm_need_paranthesis(int this_op, int up_op, boolean is_right)
	{
		if (up_op == GM_OPS_T.GMOP_TER.getValue())
		{
			// for clarity I prefer adding ()s, except chained ternary-ops.
			// example (A+B>C) ? (D+1) : (A+C>D) ? (E+1) : (F+1)
			if ((this_op == GM_OPS_T.GMOP_TER.getValue()) && is_right)
				return false;
			else
				return true;
		}
		else
		{
			if (GlobalMembersGm_misc.gm_get_op_pred(this_op) > GlobalMembersGm_misc.gm_get_op_pred(up_op))
				return true;

			else if (GlobalMembersGm_misc.gm_get_op_pred(this_op) == GlobalMembersGm_misc.gm_get_op_pred(up_op) && is_right)
				return true;

			else
				return false;
		}
	}
	public static boolean gm_is_same_string(String s1, String s2)
	{
		return (strcmp(s1, s2) == 0);
	}

	//extern const char* gm_get_builtin_string(int t);
	//extern int gm_get_output_type_summary_builtin(int btype); 
	public static int gm_get_iter_type_from_set_type(int set_type)
	{
		switch (set_type)
		{
			case GMTYPE_NSET:
				return GMTYPE_T.GMTYPE_NODEITER_SET;
			case GMTYPE_NSEQ:
				return GMTYPE_T.GMTYPE_NODEITER_SEQ;
			case GMTYPE_NORDER:
				return GMTYPE_T.GMTYPE_NODEITER_ORDER;
			case GMTYPE_ESET:
				return GMTYPE_T.GMTYPE_NODEITER_SET;
			case GMTYPE_ESEQ:
				return GMTYPE_T.GMTYPE_NODEITER_SEQ;
			case GMTYPE_EORDER:
				return GMTYPE_T.GMTYPE_NODEITER_ORDER;
			default:
				assert false;
				return 0;
		}
	}

	//-----------------------------------------------------
	// For compiler debug,
	// mark begining/end of compiler stage (major or minor).
	// All numbering should start from 1. (not from 0)
	public static final int GMSTAGE_PARSE = 1;
	public static final int GMSTAGE_FRONTEND = 2;
	public static final int GMSTAGE_INDEPENDENT_OPT = 3;
	public static final int GMSTAGE_BACKEND_OPT = 4;
	public static final int GMSTAGE_LIBRARY_OPT = 5;
	public static final int GMSTAGE_CODEGEN = 6;

	//extern void gm_begin_major_compiler_stage(int major_no, String desc);
	//extern void gm_end_major_compiler_stage();
	//extern void gm_begin_minor_compiler_stage(int major_no, String desc);
	//extern void gm_end_minor_compiler_stage();
}