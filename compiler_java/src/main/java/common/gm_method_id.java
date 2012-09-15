package common;

public enum gm_method_id {
	GM_BLTIN_GRAPH_NUM_NODES, // number of nodes in graph
	GM_BLTIN_GRAPH_NUM_EDGES, // number of edges in graph
	GM_BLTIN_GRAPH_RAND_NODE, // pick random node in the graph.

	GM_BLTIN_NODE_DEGREE, // (out-)degree of a node
	GM_BLTIN_NODE_IN_DEGREE, // in-degree of a node
	GM_BLTIN_NODE_TO_EDGE, // edge to that node-iterator
	GM_BLTIN_NODE_IS_NBR, // check if the node should be
	GM_BLTIN_NODE_HAS_EDGE_TO, // check if a node has an outgoing edge to other
								// node
	GM_BLTIN_NODE_RAND_NBR, // returns a random neighbor of the node

	GM_BLTIN_EDGE_FROM, // source node of an edge
	GM_BLTIN_EDGE_TO, // destination

	GM_BLTIN_TOP_DRAND, // rand function
	GM_BLTIN_TOP_IRAND, // rand function
	GM_BLTIN_TOP_LOG, // log function
	GM_BLTIN_TOP_EXP, // exp function
	GM_BLTIN_TOP_POW, // pow function

	GM_BLTIN_SET_ADD, //
	GM_BLTIN_SET_REMOVE, //
	GM_BLTIN_SET_HAS, //
	GM_BLTIN_SET_ADD_BACK, //
	GM_BLTIN_SET_REMOVE_BACK, //
	GM_BLTIN_SET_PEEK, //
	GM_BLTIN_SET_PEEK_BACK, //
	GM_BLTIN_SET_UNION, //
	GM_BLTIN_SET_INTERSECT, //
	GM_BLTIN_SET_COMPLEMENT, //
	GM_BLTIN_SET_SUBSET, //
	GM_BLTIN_SET_SIZE,

	GM_BLTIN_MAP_SIZE, // returns the number of mappings in the map
	GM_BLTIN_MAP_HAS_MAX_VALUE, // checks if the key is mapped to the biggest
								// value
	GM_BLTIN_MAP_HAS_MIN_VALUE, // checks if the key is mapped to the smallest
								// value
	GM_BLTIN_MAP_HAS_KEY, // checks if there is a mapping for the key in the map
	GM_BLTIN_MAP_GET_MAX_KEY, // returns the key that is mapped to the biggest
								// value
	GM_BLTIN_MAP_GET_MIN_KEY, // returns the key that is mapped to the smallest
								// value
	GM_BLTIN_MAP_GET_MAX_VALUE, // returns the biggest value in the map
	GM_BLTIN_MAP_GET_MIN_VALUE, // returns the smallest value in the map
	GM_BLTIN_MAP_CLEAR, // clears the whole map

	GM_BLTIN_END;
}