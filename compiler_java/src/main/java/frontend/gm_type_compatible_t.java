package frontend;

//-----------------------------------------------------------
// t1:lhs type summary, t2:rhs type summary (GMTYPE_ ...), t1!=t2
// return true, if assignment is possible
// assumption: target graph check is separately done.
// assumption: write-protection check is separately done. (i.e. preventing write to node iterator)
//-----------------------------------------------------------
public enum gm_type_compatible_t {
	FOR_ASSIGN, //
	FOR_EQ, //
	FOR_LESS, //
	FOR_BOP;
}
