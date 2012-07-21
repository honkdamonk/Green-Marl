package frontend;

//-----------------------------------------------------------
// t1:lhs type summary, t2:rhs type summary (GMTYPE_ ...), t1!=t2
// return true, if assignment is possible
// assumption: target graph check is separately done.
// assumption: write-protection check is separately done. (i.e. preventing write to node iterator)
//-----------------------------------------------------------

public enum gm_type_compatible_t
{
	FOR_ASSIGN,
	FOR_EQ,
	FOR_LESS,
	FOR_BOP;

	public int getValue()
	{
		return this.ordinal();
	}

	public static gm_type_compatible_t forValue(int value)
	{
		return values()[value];
	}
}
/*
 bool gm_frontend::do_type_resolve(ast_procdef* p)
 {
 return true;
 }
 */

//=========================================================================
// defines for gm_ast.h
//=========================================================================




