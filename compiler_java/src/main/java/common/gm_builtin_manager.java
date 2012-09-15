package common;

import static common.gm_method_id.GM_BLTIN_EDGE_FROM;
import static common.gm_method_id.GM_BLTIN_EDGE_TO;
import static common.gm_method_id.GM_BLTIN_GRAPH_NUM_EDGES;
import static common.gm_method_id.GM_BLTIN_GRAPH_NUM_NODES;
import static common.gm_method_id.GM_BLTIN_GRAPH_RAND_NODE;
import static common.gm_method_id.GM_BLTIN_MAP_CLEAR;
import static common.gm_method_id.GM_BLTIN_MAP_GET_MAX_KEY;
import static common.gm_method_id.GM_BLTIN_MAP_GET_MAX_VALUE;
import static common.gm_method_id.GM_BLTIN_MAP_GET_MIN_KEY;
import static common.gm_method_id.GM_BLTIN_MAP_GET_MIN_VALUE;
import static common.gm_method_id.GM_BLTIN_MAP_HAS_KEY;
import static common.gm_method_id.GM_BLTIN_MAP_HAS_MAX_VALUE;
import static common.gm_method_id.GM_BLTIN_MAP_HAS_MIN_VALUE;
import static common.gm_method_id.GM_BLTIN_MAP_SIZE;
import static common.gm_method_id.GM_BLTIN_NODE_DEGREE;
import static common.gm_method_id.GM_BLTIN_NODE_HAS_EDGE_TO;
import static common.gm_method_id.GM_BLTIN_NODE_IN_DEGREE;
import static common.gm_method_id.GM_BLTIN_NODE_IS_NBR;
import static common.gm_method_id.GM_BLTIN_NODE_RAND_NBR;
import static common.gm_method_id.GM_BLTIN_NODE_TO_EDGE;
import static common.gm_method_id.GM_BLTIN_SET_ADD;
import static common.gm_method_id.GM_BLTIN_SET_ADD_BACK;
import static common.gm_method_id.GM_BLTIN_SET_COMPLEMENT;
import static common.gm_method_id.GM_BLTIN_SET_HAS;
import static common.gm_method_id.GM_BLTIN_SET_INTERSECT;
import static common.gm_method_id.GM_BLTIN_SET_REMOVE;
import static common.gm_method_id.GM_BLTIN_SET_REMOVE_BACK;
import static common.gm_method_id.GM_BLTIN_SET_SIZE;
import static common.gm_method_id.GM_BLTIN_SET_SUBSET;
import static common.gm_method_id.GM_BLTIN_SET_UNION;
import static common.gm_method_id.GM_BLTIN_TOP_DRAND;
import static common.gm_method_id.GM_BLTIN_TOP_EXP;
import static common.gm_method_id.GM_BLTIN_TOP_IRAND;
import static common.gm_method_id.GM_BLTIN_TOP_LOG;
import static common.gm_method_id.GM_BLTIN_TOP_POW;
import inc.gm_type;

import java.util.LinkedList;

// should-be a singleton 
public class gm_builtin_manager {
	
	private final static String GM_BLTIN_MUTATE_GROW = "1";
	private final static String GM_BLTIN_MUTATE_SHRINK = "2";
	private final static String GM_BLTIN_FLAG_TRUE = "true";
	
	public final static String GM_BLTIN_INFO_USE_REVERSE = "GM_BLTIN_INFO_USE_REVERSE";
	public final static String GM_BLTIN_INFO_CHECK_NBR = "GM_BLTIN_INFO_CHECK_NBR";
	public final static String GM_BLTIN_INFO_NEED_FROM = "GM_BLTIN_INFO_NEED_FROM";
	public final static String GM_BLTIN_INFO_MUTATING = "GM_BLTIN_INFO_MUTATING";
	
	private final LinkedList<gm_builtin_def> defs = new LinkedList<gm_builtin_def>();
	private gm_builtin_def last_def;

	private static final gm_builtin_desc_t[] GM_builtins = { //
			new gm_builtin_desc_t("Graph:NumNodes:Int:0", GM_BLTIN_GRAPH_NUM_NODES, ""),
			new gm_builtin_desc_t("Graph:NumEdges:Int:0", GM_BLTIN_GRAPH_NUM_EDGES, ""),
			new gm_builtin_desc_t("Graph:PickRandom:Node:0", GM_BLTIN_GRAPH_RAND_NODE, ""),
			
			new gm_builtin_desc_t("Node:NumNbrs:Int:0", GM_BLTIN_NODE_DEGREE, ""),
			new gm_builtin_desc_t("*NumOutNbrs", GM_BLTIN_NODE_DEGREE, ""),
			new gm_builtin_desc_t("*Degree", GM_BLTIN_NODE_DEGREE, ""),
			new gm_builtin_desc_t("*OutDegree", GM_BLTIN_NODE_DEGREE, ""),
			new gm_builtin_desc_t("Node:NumInNbrs:Int:0", GM_BLTIN_NODE_IN_DEGREE, AUX_INFO(GM_BLTIN_INFO_USE_REVERSE, GM_BLTIN_FLAG_TRUE)),
			new gm_builtin_desc_t("*InDegree", GM_BLTIN_NODE_IN_DEGREE, ""),
			new gm_builtin_desc_t("Node:IsNbrFrom:Bool:1:Node", GM_BLTIN_NODE_IS_NBR, AUX_INFO(GM_BLTIN_INFO_CHECK_NBR, GM_BLTIN_FLAG_TRUE)),
			new gm_builtin_desc_t("Node:HasEdgeTo:Bool:1:Node", GM_BLTIN_NODE_HAS_EDGE_TO, GM_BLTIN_INFO_CHECK_NBR + ":" + GM_BLTIN_FLAG_TRUE),
			new gm_builtin_desc_t("Node:PickRandomNbr:Node", GM_BLTIN_NODE_RAND_NBR, AUX_INFO(GM_BLTIN_INFO_CHECK_NBR, GM_BLTIN_FLAG_TRUE)),
			
			new gm_builtin_desc_t("!NI_In:ToEdge:Edge:0", GM_BLTIN_NODE_TO_EDGE, ""),
			new gm_builtin_desc_t("!NI_Out:ToEdge:Edge:0", GM_BLTIN_NODE_TO_EDGE, ""),
			new gm_builtin_desc_t("!NI_Down:ToEdge:Edge:0", GM_BLTIN_NODE_TO_EDGE, ""),
			new gm_builtin_desc_t("!NI_Up:ToEdge:Edge:0", GM_BLTIN_NODE_TO_EDGE, ""),
			
			new gm_builtin_desc_t("Edge:FromNode:Node:0", GM_BLTIN_EDGE_FROM, AUX_INFO(GM_BLTIN_INFO_NEED_FROM, GM_BLTIN_FLAG_TRUE)),
			new gm_builtin_desc_t("Edge:ToNode:Node:0", GM_BLTIN_EDGE_TO, ""),
			
	        // Set:
			new gm_builtin_desc_t("N_S:Add:Void:1:Node", GM_BLTIN_SET_ADD, AUX_INFO(GM_BLTIN_INFO_MUTATING, GM_BLTIN_MUTATE_GROW)),
			new gm_builtin_desc_t("N_S:Remove:Void:1:Node", GM_BLTIN_SET_REMOVE, AUX_INFO(GM_BLTIN_INFO_MUTATING, GM_BLTIN_MUTATE_SHRINK)),
			new gm_builtin_desc_t("N_S:Has:Bool:1:Node", GM_BLTIN_SET_HAS, ""),
			new gm_builtin_desc_t("N_S:Union:Void:1:N_S", GM_BLTIN_SET_UNION, AUX_INFO(GM_BLTIN_INFO_MUTATING, GM_BLTIN_MUTATE_GROW)),
			new gm_builtin_desc_t("N_S:Intersect:Void:1:N_S", GM_BLTIN_SET_INTERSECT, AUX_INFO(GM_BLTIN_INFO_MUTATING, GM_BLTIN_MUTATE_SHRINK)),
			new gm_builtin_desc_t("N_S:Complement:Void:1:N_S", GM_BLTIN_SET_COMPLEMENT, AUX_INFO(GM_BLTIN_INFO_MUTATING, GM_BLTIN_MUTATE_SHRINK)),
			new gm_builtin_desc_t("N_S:IsSubsetOf:Bool:1:N_S", GM_BLTIN_SET_SUBSET, ""),
			new gm_builtin_desc_t("N_S:Size:Int", GM_BLTIN_SET_SIZE, ""),
			
			// Order:
			new gm_builtin_desc_t("N_O:PushBack:Void:1:Node", GM_BLTIN_SET_ADD_BACK, AUX_INFO(GM_BLTIN_INFO_MUTATING, GM_BLTIN_MUTATE_GROW)),
			new gm_builtin_desc_t("*Push", GM_BLTIN_SET_ADD_BACK, AUX_INFO(GM_BLTIN_INFO_MUTATING, GM_BLTIN_MUTATE_GROW)),
			new gm_builtin_desc_t("N_O:PushFront:Void:1:Node", GM_BLTIN_SET_ADD, AUX_INFO(GM_BLTIN_INFO_MUTATING, GM_BLTIN_MUTATE_GROW)),
			new gm_builtin_desc_t("N_O:PopBack:Node:0", GM_BLTIN_SET_REMOVE_BACK, AUX_INFO(GM_BLTIN_INFO_MUTATING, GM_BLTIN_MUTATE_SHRINK)),
			new gm_builtin_desc_t("N_O:PopFront:Node:1:Node", GM_BLTIN_SET_REMOVE, AUX_INFO(GM_BLTIN_INFO_MUTATING, GM_BLTIN_MUTATE_SHRINK)),
			new gm_builtin_desc_t("*Pop", GM_BLTIN_SET_REMOVE, AUX_INFO(GM_BLTIN_INFO_MUTATING, GM_BLTIN_MUTATE_SHRINK)),
			new gm_builtin_desc_t("N_O:Has:Bool:1:Node", GM_BLTIN_SET_HAS, ""),
			new gm_builtin_desc_t("N_O:Size:Int", GM_BLTIN_SET_SIZE, ""),
			
			// Seq:
			new gm_builtin_desc_t("N_Q:PushBack:Void:1:Node", GM_BLTIN_SET_ADD_BACK, AUX_INFO(GM_BLTIN_INFO_MUTATING, GM_BLTIN_MUTATE_GROW)),
			new gm_builtin_desc_t("*Push", GM_BLTIN_SET_ADD_BACK, AUX_INFO(GM_BLTIN_INFO_MUTATING, GM_BLTIN_MUTATE_GROW)),
			new gm_builtin_desc_t("N_Q:PushFront:Void:1:Node", GM_BLTIN_SET_ADD, AUX_INFO(GM_BLTIN_INFO_MUTATING, GM_BLTIN_MUTATE_GROW)),
			new gm_builtin_desc_t("N_Q:PopBack:Node:0", GM_BLTIN_SET_REMOVE_BACK, AUX_INFO(GM_BLTIN_INFO_MUTATING, GM_BLTIN_MUTATE_SHRINK)),
			new gm_builtin_desc_t("N_Q:PopFront:Node:1:Node", GM_BLTIN_SET_REMOVE, AUX_INFO(GM_BLTIN_INFO_MUTATING, GM_BLTIN_MUTATE_SHRINK)),
			new gm_builtin_desc_t("*Pop", GM_BLTIN_SET_REMOVE, AUX_INFO(GM_BLTIN_INFO_MUTATING, GM_BLTIN_MUTATE_SHRINK)),
			new gm_builtin_desc_t("N_Q:Size:Int", GM_BLTIN_SET_SIZE, ""),
			
			new gm_builtin_desc_t("_:Uniform:Double:0", GM_BLTIN_TOP_DRAND, ""),
			new gm_builtin_desc_t("_:Rand:Long:1:Long", GM_BLTIN_TOP_IRAND, ""),
			new gm_builtin_desc_t("_:Log:Double:1:Double", GM_BLTIN_TOP_LOG, ""),
			new gm_builtin_desc_t("_:Exp:Double:1:Double", GM_BLTIN_TOP_EXP, ""),
			new gm_builtin_desc_t("_:Pow:Double:2:Double:Double", GM_BLTIN_TOP_POW, ""),
			
			// Map
			new gm_builtin_desc_t("Map:Size:Int:0", GM_BLTIN_MAP_SIZE, ""),
			new gm_builtin_desc_t("Map:Clear:Void:0", GM_BLTIN_MAP_CLEAR, ""),
			new gm_builtin_desc_t("Map:HasKey:Bool:1:Generic", GM_BLTIN_MAP_HAS_KEY, ""),
			new gm_builtin_desc_t("Map:HasMaxValue:Bool:1:Generic", GM_BLTIN_MAP_HAS_MAX_VALUE, ""),
			new gm_builtin_desc_t("Map:HasMinValue:Bool:1:Generic", GM_BLTIN_MAP_HAS_MIN_VALUE, ""),
			new gm_builtin_desc_t("Map:GetMaxKey:Generic:0:", GM_BLTIN_MAP_GET_MAX_KEY, ""),
			new gm_builtin_desc_t("Map:GetMinKey:Generic:0:", GM_BLTIN_MAP_GET_MIN_KEY, ""),
			new gm_builtin_desc_t("Map:GetMaxValue:Generic:0:", GM_BLTIN_MAP_GET_MAX_VALUE, ""),
			new gm_builtin_desc_t("Map:GetMinValue:Generic:0:", GM_BLTIN_MAP_GET_MIN_VALUE, "")
	};
	
	private static String AUX_INFO(String X, String Y) {
		return X + ":" + Y;
	}

	gm_builtin_manager() {
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

	public final gm_builtin_def find_builtin_def(gm_type source_type, String orgname) {
		for (gm_builtin_def d : defs) {
			gm_type def_src = d.get_source_type_summary();
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
				if (def_src == gm_type.GMTYPE_VOID)
					continue;
				assert (!def_src.is_prim_type());

				if (gm_type.is_same_node_or_edge_compatible_type(def_src, source_type)
						|| gm_type.collection_of_collection_compatible_type(def_src, source_type)) {
					if (d.is_synonym_def())
						return d.get_org_def();
					else
						return d;
				}
			}
		}
		return null;
	}

	public final gm_builtin_def find_builtin_def(gm_type source_type, gm_method_id id) {

		for (gm_builtin_def d : defs) {
			if (d.get_method_id() != id)
				continue;

			gm_type def_src = d.get_source_type_summary();
			if (def_src != source_type) {

				boolean is_strict = d.need_strict_source_type();

				if (is_strict)
					continue;
				if (source_type == gm_type.GMTYPE_VOID)
					continue;
				if (def_src.is_prim_type())
					continue;
				if (!gm_type.is_same_node_or_edge_compatible_type(def_src, source_type))
					continue;
			}
			if (d.is_synonym_def())
				return d.get_org_def();
			else
				return d;
		}
		return null;
	}

	final gm_builtin_def get_last_def() {
		return last_def;
	}
}