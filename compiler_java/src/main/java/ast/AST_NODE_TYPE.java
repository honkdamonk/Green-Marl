package ast;

public enum AST_NODE_TYPE {
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
		switch (this) {
		case AST_ID:
			return "AST_ID";
		case AST_FIELD:
			return "AST_FIELD";
		case AST_MAPACCESS:
			return "AST_MAPACCESS";
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
}