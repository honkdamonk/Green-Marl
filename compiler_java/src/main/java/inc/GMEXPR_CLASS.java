package inc;

//-------------------------------------------------------
// Class of Expressions
//-------------------------------------------------------
public enum GMEXPR_CLASS {
	GMEXPR_IVAL, // integer literal
	GMEXPR_FVAL, // floating literal
	GMEXPR_BVAL, // boolean literal
	GMEXPR_INF, // infinite literal
	GMEXPR_NIL, // NIL literal
	GMEXPR_ID, // identifier
	GMEXPR_FIELD, // field access
	GMEXPR_UOP, // unary op (neg)
	GMEXPR_LUOP, // logical not
	GMEXPR_BIOP, // numeric binary op
	GMEXPR_LBIOP, // logical binary op
	GMEXPR_COMP, // comparision ops (==, !=, <, >, <=, >=)
	GMEXPR_REDUCE, // reduction ops (Sum, Product, Min, Max)
	GMEXPR_BUILTIN, // builtin ops (NumNodes, NumNbrs, ...)
	GMEXPR_BUILTIN_FIELD, // builtin ops on property entries
	GMEXPR_TER, // ternary operation
	GMEXPR_FOREIGN, // foreign expression
	GMEXPR_MAPACCESS;

}