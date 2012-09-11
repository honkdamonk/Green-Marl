package common;

import inc.GMTYPE_T;

import java.util.LinkedList;

// should-be a singleton 
public class gm_builtin_manager {
	
	public final static String GM_BLTIN_MUTATE_GROW = "1";
	public final static String GM_BLTIN_MUTATE_SHRINK = "2";
	public final static String GM_BLTIN_FLAG_TRUE = "true";
	
	public final static String GM_BLTIN_INFO_USE_REVERSE = "GM_BLTIN_INFO_USE_REVERSE";
	public final static String GM_BLTIN_INFO_CHECK_NBR = "GM_BLTIN_INFO_CHECK_NBR";
	public final static String GM_BLTIN_INFO_NEED_FROM = "GM_BLTIN_INFO_NEED_FROM";
	public final static String GM_BLTIN_INFO_MUTATING = "GM_BLTIN_INFO_MUTATING";

	public static String AUX_INFO(String X, String Y) {
		return X + ":" + Y;
	}
	
	public static final gm_builtin_desc_t[] GM_builtins = { //
			new gm_builtin_desc_t("Graph:NumNodes:Int:0", gm_method_id_t.GM_BLTIN_GRAPH_NUM_NODES, ""),
			new gm_builtin_desc_t("Graph:NumEdges:Int:0", gm_method_id_t.GM_BLTIN_GRAPH_NUM_EDGES, ""),
			new gm_builtin_desc_t("Graph:PickRandom:Node:0", gm_method_id_t.GM_BLTIN_GRAPH_RAND_NODE, ""),
			new gm_builtin_desc_t("Node:NumNbrs:Int:0", gm_method_id_t.GM_BLTIN_NODE_DEGREE, ""),
			new gm_builtin_desc_t("Node:NumOutNbrs:Int:0", gm_method_id_t.GM_BLTIN_NODE_DEGREE, ""),
			new gm_builtin_desc_t("Node:Degree:Int:0", gm_method_id_t.GM_BLTIN_NODE_DEGREE, ""),
			new gm_builtin_desc_t("Node:OutDegree:Int:0", gm_method_id_t.GM_BLTIN_NODE_DEGREE, ""),
			new gm_builtin_desc_t("Node:NumInNbrs:Int:0", gm_method_id_t.GM_BLTIN_NODE_IN_DEGREE, AUX_INFO(GM_BLTIN_INFO_USE_REVERSE, GM_BLTIN_FLAG_TRUE)),
			new gm_builtin_desc_t("Node:InDegree:Int:0", gm_method_id_t.GM_BLTIN_NODE_IN_DEGREE, ""),
			new gm_builtin_desc_t("Node:IsNbrFrom:Bool:1:Node", gm_method_id_t.GM_BLTIN_NODE_IS_NBR, AUX_INFO(GM_BLTIN_INFO_CHECK_NBR, GM_BLTIN_FLAG_TRUE)),
			new gm_builtin_desc_t("Node:HasEdgeTo:Bool:1:Node", gm_method_id_t.GM_BLTIN_NODE_HAS_EDGE_TO, GM_BLTIN_INFO_CHECK_NBR + ":" + GM_BLTIN_FLAG_TRUE),
			new gm_builtin_desc_t("Node:PickRandomNbr:Node", gm_method_id_t.GM_BLTIN_NODE_RAND_NBR, AUX_INFO(GM_BLTIN_INFO_CHECK_NBR, GM_BLTIN_FLAG_TRUE)),
			new gm_builtin_desc_t("!NI_In:ToEdge:Edge:0", gm_method_id_t.GM_BLTIN_NODE_TO_EDGE, ""),
			new gm_builtin_desc_t("!NI_Out:ToEdge:Edge:0", gm_method_id_t.GM_BLTIN_NODE_TO_EDGE, ""),
			new gm_builtin_desc_t("!NI_Down:ToEdge:Edge:0", gm_method_id_t.GM_BLTIN_NODE_TO_EDGE, ""),
			new gm_builtin_desc_t("!NI_Up:ToEdge:Edge:0", gm_method_id_t.GM_BLTIN_NODE_TO_EDGE, ""),
			new gm_builtin_desc_t("Edge:FromNode:Node:0", gm_method_id_t.GM_BLTIN_EDGE_FROM, AUX_INFO(GM_BLTIN_INFO_NEED_FROM, GM_BLTIN_FLAG_TRUE)),
			new gm_builtin_desc_t("Edge:ToNode:Node:0", gm_method_id_t.GM_BLTIN_EDGE_TO, ""),
			new gm_builtin_desc_t("N_S:Add:Void:1:Node", gm_method_id_t.GM_BLTIN_SET_ADD, AUX_INFO(GM_BLTIN_INFO_MUTATING, GM_BLTIN_MUTATE_GROW)),
			new gm_builtin_desc_t("N_S:Remove:Void:1:Node", gm_method_id_t.GM_BLTIN_SET_REMOVE, AUX_INFO(GM_BLTIN_INFO_MUTATING, GM_BLTIN_MUTATE_SHRINK)),
			new gm_builtin_desc_t("N_S:Has:Bool:1:Node", gm_method_id_t.GM_BLTIN_SET_HAS, ""),
			new gm_builtin_desc_t("N_S:Union:Void:1:N_S", gm_method_id_t.GM_BLTIN_SET_UNION, AUX_INFO(GM_BLTIN_INFO_MUTATING, GM_BLTIN_MUTATE_GROW)),
			new gm_builtin_desc_t("N_S:Intersect:Void:1:N_S", gm_method_id_t.GM_BLTIN_SET_INTERSECT, AUX_INFO(GM_BLTIN_INFO_MUTATING, GM_BLTIN_MUTATE_SHRINK)),
			new gm_builtin_desc_t("N_S:Complement:Void:1:N_S", gm_method_id_t.GM_BLTIN_SET_COMPLEMENT, AUX_INFO(GM_BLTIN_INFO_MUTATING, GM_BLTIN_MUTATE_SHRINK)),
			new gm_builtin_desc_t("N_S:IsSubsetOf:Bool:1:N_S", gm_method_id_t.GM_BLTIN_SET_SUBSET, ""),
			new gm_builtin_desc_t("N_S:Size:Int", gm_method_id_t.GM_BLTIN_SET_SIZE, ""),
			new gm_builtin_desc_t("N_O:PushBack:Void:1:Node", gm_method_id_t.GM_BLTIN_SET_ADD_BACK, AUX_INFO(GM_BLTIN_INFO_MUTATING, GM_BLTIN_MUTATE_GROW)),
			new gm_builtin_desc_t("N_O:Push:Void:1:Node", gm_method_id_t.GM_BLTIN_SET_ADD_BACK, AUX_INFO(GM_BLTIN_INFO_MUTATING, GM_BLTIN_MUTATE_GROW)),
			new gm_builtin_desc_t("N_O:PushFront:Void:1:Node", gm_method_id_t.GM_BLTIN_SET_ADD, AUX_INFO(GM_BLTIN_INFO_MUTATING, GM_BLTIN_MUTATE_GROW)),
			new gm_builtin_desc_t("N_O:PopBack:Node:0", gm_method_id_t.GM_BLTIN_SET_REMOVE_BACK, AUX_INFO(GM_BLTIN_INFO_MUTATING, GM_BLTIN_MUTATE_SHRINK)),
			new gm_builtin_desc_t("N_O:PopFront:Node:1:Node", gm_method_id_t.GM_BLTIN_SET_REMOVE, AUX_INFO(GM_BLTIN_INFO_MUTATING, GM_BLTIN_MUTATE_SHRINK)),
			new gm_builtin_desc_t("N_O:Pop:Node:1:Node", gm_method_id_t.GM_BLTIN_SET_REMOVE, AUX_INFO(GM_BLTIN_INFO_MUTATING, GM_BLTIN_MUTATE_SHRINK)),
			new gm_builtin_desc_t("N_O:Has:Bool:1:Node", gm_method_id_t.GM_BLTIN_SET_HAS, ""),
			new gm_builtin_desc_t("N_O:Size:Int", gm_method_id_t.GM_BLTIN_SET_SIZE, ""),
			new gm_builtin_desc_t("N_Q:PushBack:Void:1:Node", gm_method_id_t.GM_BLTIN_SET_ADD_BACK, AUX_INFO(GM_BLTIN_INFO_MUTATING, GM_BLTIN_MUTATE_GROW)),
			new gm_builtin_desc_t("N_Q:Push:Void:1:Node", gm_method_id_t.GM_BLTIN_SET_ADD_BACK, AUX_INFO(GM_BLTIN_INFO_MUTATING, GM_BLTIN_MUTATE_GROW)),
			new gm_builtin_desc_t("N_Q:PushFront:Void:1:Node", gm_method_id_t.GM_BLTIN_SET_ADD, AUX_INFO(GM_BLTIN_INFO_MUTATING, GM_BLTIN_MUTATE_GROW)),
			new gm_builtin_desc_t("N_Q:PopBack:Node:0", gm_method_id_t.GM_BLTIN_SET_REMOVE_BACK, AUX_INFO(GM_BLTIN_INFO_MUTATING, GM_BLTIN_MUTATE_SHRINK)),
			new gm_builtin_desc_t("N_Q:PopFront:Node:1:Node", gm_method_id_t.GM_BLTIN_SET_REMOVE, AUX_INFO(GM_BLTIN_INFO_MUTATING, GM_BLTIN_MUTATE_SHRINK)),
			new gm_builtin_desc_t("N_Q:Pop:Node:1:Node", gm_method_id_t.GM_BLTIN_SET_REMOVE, AUX_INFO(GM_BLTIN_INFO_MUTATING, GM_BLTIN_MUTATE_SHRINK)),
			new gm_builtin_desc_t("N_Q:Size:Int", gm_method_id_t.GM_BLTIN_SET_SIZE, ""),
			new gm_builtin_desc_t("_:Uniform:Double:0", gm_method_id_t.GM_BLTIN_TOP_DRAND, ""),
			new gm_builtin_desc_t("_:Rand:Long:1:Long", gm_method_id_t.GM_BLTIN_TOP_IRAND, ""),
			new gm_builtin_desc_t("_:Log:Double:1:Double", gm_method_id_t.GM_BLTIN_TOP_LOG, ""),
			new gm_builtin_desc_t("_:Exp:Double:1:Double", gm_method_id_t.GM_BLTIN_TOP_EXP, ""),
			new gm_builtin_desc_t("_:Pow:Double:2:Double:Double", gm_method_id_t.GM_BLTIN_TOP_POW, "") //
	};

	private LinkedList<gm_builtin_def> defs = new LinkedList<gm_builtin_def>();
	private gm_builtin_def last_def;

	public gm_builtin_manager() {
		// -----------------------------------------------------
		// construct built-in library by
		// parsing built-in strings in (gm_builtin.h)
		// -----------------------------------------------------
		for (int i = 0; i < GM_builtins.length; i++) {
			gm_builtin_def d = new gm_builtin_def(new gm_builtin_desc_t(GM_builtins[i]), this);
			defs.addLast(d);
			if (!d.is_synonym_def())
				last_def = d;
		}
	}

	public final gm_builtin_def find_builtin_def(GMTYPE_T source_type, String orgname) {
		for (gm_builtin_def d : defs) {
			GMTYPE_T def_src = d.get_source_type_summary();
			if (orgname.equals(d.get_orgname())) {
				if (def_src == source_type) {
					if (d.is_synonym_def())
						return d.get_org_def();
					else
						return d;
				}
				boolean is_strict = d.need_strict_source_type();
				if (is_strict)
					continue;
				if (def_src == GMTYPE_T.GMTYPE_VOID)
					continue;
				assert (!def_src.is_prim_type());

				if (GMTYPE_T.is_same_node_or_edge_compatible_type(def_src, source_type)
						|| GMTYPE_T.collection_of_collection_compatible_type(def_src, source_type)) {
					if (d.is_synonym_def())
						return d.get_org_def();
					else
						return d;
				}
			}
		}
		return null;
	}

	public final gm_builtin_def find_builtin_def(GMTYPE_T source_type, gm_method_id_t id) {

		for (gm_builtin_def d : defs) {
			if (d.get_method_id() != id)
				continue;

			GMTYPE_T def_src = d.get_source_type_summary();
			if (def_src != source_type) {

				boolean is_strict = d.need_strict_source_type();

				if (is_strict)
					continue;
				if (source_type == GMTYPE_T.GMTYPE_VOID)
					continue;
				if (def_src.is_prim_type())
					continue;
				if (!GMTYPE_T.is_same_node_or_edge_compatible_type(def_src, source_type))
					continue;
			}
			if (d.is_synonym_def())
				return d.get_org_def();
			else
				return d;
		}
		return null;
	}

	public final gm_builtin_def get_last_def() {
		return last_def;
	}
}