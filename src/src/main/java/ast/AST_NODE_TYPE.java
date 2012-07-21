package ast;


public enum AST_NODE_TYPE
{
	AST_ID,
	AST_FIELD, // A.B
	AST_IDLIST, // A, B, C,
	AST_TYPEDECL, // INT
	AST_ARGDECL, // a,b : B
	AST_PROCDEF, // proc A() {}
	AST_EXPR, // c + 3
	AST_EXPR_RDC, // c + 3
	AST_EXPR_BUILTIN, // c + 3
	AST_EXPR_FOREIGN, // Foreign Expression
	AST_SENT,
	AST_SENTBLOCK(0), // { ... }
	AST_ASSIGN(1), // C =D
	AST_VARDECL(2), // INT x;
				  // NODE_PROPERTY<INT>(G) x;
	AST_FOREACH(3), // Foreach (t: G.Nodes) {...}
	AST_IF(4), // IF (x) THEN s; ELSE z ;
	AST_WHILE(5), // While (x) {...} or Do {...} While (x)
	AST_RETURN(6), // Return y;
	AST_BFS(7), // InBFS(t: G.Nodes) {....} InReverse {....}
	AST_CALL(8), // Call to (built-in) function
	AST_FOREIGN(9), // Foreign syntax
	AST_NOP(10), // NOP (for backend-only)

	AST_END(11);

	private int intValue;
	private static java.util.HashMap<Integer, AST_NODE_TYPE> mappings;
	private static java.util.HashMap<Integer, AST_NODE_TYPE> getMappings()
	{
		if (mappings == null)
		{
			synchronized (AST_NODE_TYPE.class)
			{
				if (mappings == null)
				{
					mappings = new java.util.HashMap<Integer, AST_NODE_TYPE>();
				}
			}
		}
		return mappings;
	}

	private AST_NODE_TYPE(int value)
	{
		intValue = value;
		AST_NODE_TYPE.getMappings().put(value, this);
	}

	public int getValue()
	{
		return intValue;
	}

	public static AST_NODE_TYPE forValue(int value)
	{
		return getMappings().get(value);
	}
}