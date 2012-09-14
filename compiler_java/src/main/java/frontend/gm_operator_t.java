package frontend;

//------------------------------------------------------------------------
// CLASS NUMERIC OP
//------------------------------------------------------------------------
public enum gm_operator_t {
	INT_OP, // %
	NUMERIC_OP, // +,-,*,/, Max, Min
	BOOL_OP, // And, Or
	COMP_OP, // <,<=,>,>=
	EQ_OP, // == !=
	TER_OP, // ? t1 : t2
	ASSIGN_OP; // =
}