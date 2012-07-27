package common;

import inc.GMTYPE_T;

public class GlobalMembersGm_builtin {
	// C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced
	// in-line:
	// /#define TO_STR(X) #X
	// C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced
	// in-line:
	// /#define DEF_STRING(X) static const char *X = "X"
	// C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced
	// in-line:
	// /#define GM_COMPILE_STEP(CLASS, DESC) class CLASS : public
	// gm_compile_step { private: CLASS() {set_description(DESC);}public:
	// virtual void process(ast_procdef*p); virtual gm_compile_step*
	// get_instance(){return new CLASS();} static gm_compile_step*
	// get_factory(){return new CLASS();} };
	// C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced
	// in-line:
	// /#define GM_COMPILE_STEP_FACTORY(CLASS) CLASS::get_factory()

	// C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced
	// in-line:
	// /#define AUX_INFO(X,Y) "X:Y"

	// /#define GM_BLTIN_MUTATE_GROW 1
	// /#define GM_BLTIN_MUTATE_SHRINK 2
	// C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced
	// in-line:
	// /#define GM_BLTIN_FLAG_TRUE true

	public static String GM_BLTIN_INFO_USE_REVERSE = "GM_BLTIN_INFO_USE_REVERSE";
	public static String GM_BLTIN_INFO_CHECK_NBR = "GM_BLTIN_INFO_CHECK_NBR";
	public static String GM_BLTIN_INFO_NEED_FROM = "GM_BLTIN_INFO_NEED_FROM";
	public static String GM_BLTIN_INFO_MUTATING = "GM_BLTIN_INFO_MUTATING";

	public static final gm_builtin_desc_t[] GM_builtins = { //
			new gm_builtin_desc_t("Graph:NumNodes:Int:0", gm_method_id_t.GM_BLTIN_GRAPH_NUM_NODES, ""),
			new gm_builtin_desc_t("Graph:NumEdges:Int:0", gm_method_id_t.GM_BLTIN_GRAPH_NUM_EDGES, ""),
			new gm_builtin_desc_t("Graph:PickRandom:Node:0", gm_method_id_t.GM_BLTIN_GRAPH_RAND_NODE, ""),
			new gm_builtin_desc_t("Node:NumNbrs:Int:0", gm_method_id_t.GM_BLTIN_NODE_DEGREE, ""),
			new gm_builtin_desc_t("*NumOutNbrs", gm_method_id_t.GM_BLTIN_NODE_DEGREE, ""),
			new gm_builtin_desc_t("*Degree", gm_method_id_t.GM_BLTIN_NODE_DEGREE, ""),
			new gm_builtin_desc_t("*OutDegree", gm_method_id_t.GM_BLTIN_NODE_DEGREE, ""),
			new gm_builtin_desc_t("Node:NumInNbrs:Int:0", gm_method_id_t.GM_BLTIN_NODE_IN_DEGREE, "X:Y"),
			new gm_builtin_desc_t("*InDegree", gm_method_id_t.GM_BLTIN_NODE_IN_DEGREE, ""),
			new gm_builtin_desc_t("Node:IsNbrFrom:Bool:1:Node", gm_method_id_t.GM_BLTIN_NODE_IS_NBR, "X:Y"),
			new gm_builtin_desc_t("Node:HasEdgeTo:Bool:1:Node", gm_method_id_t.GM_BLTIN_NODE_HAS_EDGE_TO, "X:Y"),
			new gm_builtin_desc_t("Node:PickRandomNbr:Node", gm_method_id_t.GM_BLTIN_NODE_RAND_NBR, "X:Y"),
			new gm_builtin_desc_t("!NI_In:ToEdge:Edge:0", gm_method_id_t.GM_BLTIN_NODE_TO_EDGE, ""),
			new gm_builtin_desc_t("!NI_Out:ToEdge:Edge:0", gm_method_id_t.GM_BLTIN_NODE_TO_EDGE, ""),
			new gm_builtin_desc_t("!NI_Down:ToEdge:Edge:0", gm_method_id_t.GM_BLTIN_NODE_TO_EDGE, ""),
			new gm_builtin_desc_t("!NI_Up:ToEdge:Edge:0", gm_method_id_t.GM_BLTIN_NODE_TO_EDGE, ""),
			new gm_builtin_desc_t("Edge:FromNode:Node:0", gm_method_id_t.GM_BLTIN_EDGE_FROM, "X:Y"),
			new gm_builtin_desc_t("Edge:ToNode:Node:0", gm_method_id_t.GM_BLTIN_EDGE_TO, ""),
			new gm_builtin_desc_t("N_S:Add:Void:1:Node", gm_method_id_t.GM_BLTIN_SET_ADD, "X:Y"),
			new gm_builtin_desc_t("N_S:Remove:Void:1:Node", gm_method_id_t.GM_BLTIN_SET_REMOVE, "X:Y"),
			new gm_builtin_desc_t("N_S:Has:Bool:1:Node", gm_method_id_t.GM_BLTIN_SET_HAS, ""),
			new gm_builtin_desc_t("N_S:Union:Void:1:N_S", gm_method_id_t.GM_BLTIN_SET_UNION, "X:Y"),
			new gm_builtin_desc_t("N_S:Intersect:Void:1:N_S", gm_method_id_t.GM_BLTIN_SET_INTERSECT, "X:Y"),
			new gm_builtin_desc_t("N_S:Complement:Void:1:N_S", gm_method_id_t.GM_BLTIN_SET_COMPLEMENT, "X:Y"),
			new gm_builtin_desc_t("N_S:IsSubsetOf:Bool:1:N_S", gm_method_id_t.GM_BLTIN_SET_SUBSET, ""),
			new gm_builtin_desc_t("N_S:Size:Int", gm_method_id_t.GM_BLTIN_SET_SIZE, ""),
			new gm_builtin_desc_t("N_O:PushBack:Void:1:Node", gm_method_id_t.GM_BLTIN_SET_ADD_BACK, "X:Y"),
			new gm_builtin_desc_t("*Push", gm_method_id_t.GM_BLTIN_SET_ADD_BACK, "X:Y"),
			new gm_builtin_desc_t("N_O:PushFront:Void:1:Node", gm_method_id_t.GM_BLTIN_SET_ADD, "X:Y"),
			new gm_builtin_desc_t("N_O:PopBack:Node:0", gm_method_id_t.GM_BLTIN_SET_REMOVE_BACK, "X:Y"),
			new gm_builtin_desc_t("N_O:PopFront:Node:1:Node", gm_method_id_t.GM_BLTIN_SET_REMOVE, "X:Y"),
			new gm_builtin_desc_t("*Pop", gm_method_id_t.GM_BLTIN_SET_REMOVE, "X:Y"),
			new gm_builtin_desc_t("N_O:Has:Bool:1:Node", gm_method_id_t.GM_BLTIN_SET_HAS, ""),
			new gm_builtin_desc_t("N_O:Size:Int", gm_method_id_t.GM_BLTIN_SET_SIZE, ""),
			new gm_builtin_desc_t("N_Q:PushBack:Void:1:Node", gm_method_id_t.GM_BLTIN_SET_ADD_BACK, "X:Y"),
			new gm_builtin_desc_t("*Push", gm_method_id_t.GM_BLTIN_SET_ADD_BACK, "X:Y"),
			new gm_builtin_desc_t("N_Q:PushFront:Void:1:Node", gm_method_id_t.GM_BLTIN_SET_ADD, "X:Y"),
			new gm_builtin_desc_t("N_Q:PopBack:Node:0", gm_method_id_t.GM_BLTIN_SET_REMOVE_BACK, "X:Y"),
			new gm_builtin_desc_t("N_Q:PopFront:Node:1:Node", gm_method_id_t.GM_BLTIN_SET_REMOVE, "X:Y"),
			new gm_builtin_desc_t("*Pop", gm_method_id_t.GM_BLTIN_SET_REMOVE, "X:Y"),
			new gm_builtin_desc_t("N_Q:Size:Int", gm_method_id_t.GM_BLTIN_SET_SIZE, ""),
			new gm_builtin_desc_t("_:Uniform:Double:0", gm_method_id_t.GM_BLTIN_TOP_DRAND, ""),
			new gm_builtin_desc_t("_:Rand:Long:1:Long", gm_method_id_t.GM_BLTIN_TOP_IRAND, ""),
			new gm_builtin_desc_t("_:Log:Double:1:Double", gm_method_id_t.GM_BLTIN_TOP_LOG, ""),
			new gm_builtin_desc_t("_:Exp:Double:1:Double", gm_method_id_t.GM_BLTIN_TOP_EXP, ""),
			new gm_builtin_desc_t("_:Pow:Double:2:Double:Double", gm_method_id_t.GM_BLTIN_TOP_POW, "") //
	};

	// C++ TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
	// /#undef AUX_INFO

	// C++ TO JAVA CONVERTER NOTE: 'extern' variable declarations are not
	// required in Java:
	// extern gm_builtin_manager BUILT_IN;

	public static GMTYPE_T gm_get_type_from_string(String s) {
		assert s != null;
		if (s.equals("Graph"))
			return GMTYPE_T.GMTYPE_GRAPH;
		else if (s.equals("Node"))
			return GMTYPE_T.GMTYPE_NODE;
		else if (s.equals("Edge"))
			return GMTYPE_T.GMTYPE_EDGE;
		else if (s.equals("NI_All"))
			return GMTYPE_T.GMTYPE_NODEITER_ALL;
		else if (s.equals("EI_All"))
			return GMTYPE_T.GMTYPE_EDGEITER_ALL;
		else if (s.equals("NI_Out"))
			return GMTYPE_T.GMTYPE_NODEITER_NBRS;
		else if (s.equals("NI_In"))
			return GMTYPE_T.GMTYPE_NODEITER_IN_NBRS;
		else if (s.equals("NI_Up"))
			return GMTYPE_T.GMTYPE_NODEITER_UP_NBRS;
		else if (s.equals("NI_Down"))
			return GMTYPE_T.GMTYPE_NODEITER_DOWN_NBRS;
		else if (s.equals("Int"))
			return GMTYPE_T.GMTYPE_INT;
		else if (s.equals("Long"))
			return GMTYPE_T.GMTYPE_LONG;
		else if (s.equals("Float"))
			return GMTYPE_T.GMTYPE_FLOAT;
		else if (s.equals("Double"))
			return GMTYPE_T.GMTYPE_DOUBLE;
		else if (s.equals("N_S"))
			return GMTYPE_T.GMTYPE_NSET;
		else if (s.equals("E_S"))
			return GMTYPE_T.GMTYPE_ESET;
		else if (s.equals("N_O"))
			return GMTYPE_T.GMTYPE_NORDER;
		else if (s.equals("E_O"))
			return GMTYPE_T.GMTYPE_EORDER;
		else if (s.equals("N_Q"))
			return GMTYPE_T.GMTYPE_NSEQ;
		else if (s.equals("E_Q"))
			return GMTYPE_T.GMTYPE_ESEQ;
		else if (s.equals("Void"))
			return GMTYPE_T.GMTYPE_VOID;
		else if (s.equals("Bool"))
			return GMTYPE_T.GMTYPE_BOOL;
		else {
			assert false;
			throw new AssertionError();
		}
	}
}