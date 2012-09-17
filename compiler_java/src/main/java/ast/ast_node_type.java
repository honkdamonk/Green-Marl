package ast;

public enum ast_node_type {
	AST_ID, //
	AST_FIELD, // A.B
	AST_MAPACCESS, // A[B]
	AST_IDLIST, // A, B, C,
	AST_TYPEDECL, // INT
	AST_ARGDECL, // a,b : B
	AST_PROCDEF, // proc A() {}
	AST_EXPR, // c + 3
	AST_EXPR_RDC, // c + 3
	AST_EXPR_BUILTIN, // c + 3
	AST_EXPR_FOREIGN, // Foreign Expression
	AST_EXPR_MAPACCESS, //
	AST_SENT, //
	AST_SENTBLOCK, // { ... }
	AST_ASSIGN, // C =D
	AST_VARDECL, // INT x;
					// NODE_PROPERTY<INT>(G) x;
	AST_FOREACH, // Foreach (t: G.Nodes) {...}
	AST_IF, // IF (x) THEN s; ELSE z ;
	AST_WHILE, // While (x) {...} or Do {...} While (x)
	AST_RETURN, // Return y;
	AST_BFS, // InBFS(t: G.Nodes) {....} InReverse {....}
	AST_CALL, // Call to (built-in) function
	AST_FOREIGN, // Foreign syntax
	AST_NOP, // NOP (for backend-only)

	AST_END;

	public String get_nodetype_string() {
		return this.name();
	}
}