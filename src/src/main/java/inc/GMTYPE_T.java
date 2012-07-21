package inc;


// 'C' Functions called by gm_grammer.y

public enum GMTYPE_T
{
	GMTYPE_GRAPH(0),
	GMTYPE_UGRAPH(1),
	GMTYPE_NODEPROP(2),
	GMTYPE_EDGEPROP(3),
	GMTYPE_NODE(4),
	GMTYPE_EDGE(5),
	GMTYPE_NSET(6),
	GMTYPE_NSEQ(7),
	GMTYPE_NORDER(8),
	GMTYPE_ESET(9),
	GMTYPE_ESEQ(10),
	GMTYPE_EORDER(11),
	GMTYPE_COLLECTION(12),

	// iterators
	GMTYPE_NODEITER_ALL(100),
	GMTYPE_NODEITER_NBRS(101), // out nbr
	GMTYPE_NODEITER_IN_NBRS(102), // in nbr
	GMTYPE_NODEITER_BFS(103), // bfs
	GMTYPE_NODEITER_UP_NBRS(104), // up nbr
	GMTYPE_NODEITER_DOWN_NBRS(105), // doen nbr
	GMTYPE_NODEITER_SET(106), // set
	GMTYPE_NODEITER_SEQ(107), // sequence
	GMTYPE_NODEITER_ORDER(108), // order

	GMTYPE_NODEITER_COMMON_NBRS(109), // common neighbors

	GMTYPE_COLLECTIONITER_SET(110), // iterator over collection of collection
	GMTYPE_COLLECTIONITER_ORDER(111),
	GMTYPE_COLLECTIONITER_SEQ(112),

	GMTYPE_EDGEITER_ALL(200),
	GMTYPE_EDGEITER_NBRS(201),
	GMTYPE_EDGEITER_IN_NBRS(202),
	GMTYPE_EDGEITER_BFS(203),
	GMTYPE_EDGEITER_UP_NBRS(204),
	GMTYPE_EDGEITER_DOWN_NBRS(205),
	GMTYPE_EDGEITER_SET(206), // set
	GMTYPE_EDGEITER_SEQ(207), // sequence
	GMTYPE_EDGEITER_ORDER(208), // order

	GMTYPE_PROPERTYITER_SET(209),
	GMTYPE_PROPERTYITER_SEQ(210),
	GMTYPE_PROPERTYITER_ORDER(211),

	// 
	GMTYPE_BIT(1000), // 1b (for future extension)
	GMTYPE_BYTE(1001), // 1B (for future extension)
	GMTYPE_SHORT(1002), // 2B (for future extension)
	GMTYPE_INT(1003), // 4B
	GMTYPE_LONG(1004), // 8B
	GMTYPE_FLOAT(1005), // 4B
	GMTYPE_DOUBLE(1006), // 8B
	GMTYPE_BOOL(1007),
	GMTYPE_INF(1008), // PLUS INF or MINUS INF
	GMTYPE_INF_INT(1009),
	GMTYPE_INF_LONG(1010),
	GMTYPE_INF_FLOAT(1011),
	GMTYPE_INF_DOUBLE(1012),
	GMTYPE_NIL_UNKNOWN(1013),
	GMTYPE_NIL_NODE(1014),
	GMTYPE_NIL_EDGE(1015),
	GMTYPE_FOREIGN_EXPR(1016), // foreign type. Can be matched with any
	GMTYPE_UNKNOWN(9999), // expression whose type is not identified yet (variable before typechecking)
	GMTYPE_UNKNOWN_NUMERIC(10000), // expression whose type should be numeric, size not determined yet
	GMTYPE_ITER_ANY(10001), // iterator to some collection. resolved after type checking
	GMTYPE_ITER_UNDERSPECIFIED(10002),
	GMTYPE_VOID(10003),
	GMTYPE_INVALID(99999);

	private int intValue;
	private static java.util.HashMap<Integer, GMTYPE_T> mappings;
	private static java.util.HashMap<Integer, GMTYPE_T> getMappings()
	{
		if (mappings == null)
		{
			synchronized (GMTYPE_T.class)
			{
				if (mappings == null)
				{
					mappings = new java.util.HashMap<Integer, GMTYPE_T>();
				}
			}
		}
		return mappings;
	}

	private GMTYPE_T(int value)
	{
		intValue = value;
		GMTYPE_T.getMappings().put(value, this);
	}

	public int getValue()
	{
		return intValue;
	}

	public static GMTYPE_T forValue(int value)
	{
		return getMappings().get(value);
	}
}