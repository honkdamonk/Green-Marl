package frontend;

import inc.GM_REDUCE_T;
import ast.ast_id;

import common.GlobalMembersGm_misc;

public class gm_rwinfo
{
	// RANGE_LINEAR, RANGE_RANDOM, RANGE_SINGLE
	public gm_range_type_t access_range;
	public gm_symtab_entry driver; // N.F --> N is the driver

	// if this destination always accessed
	// true: yes
	// false: no (there are non-accessing paths)
	public boolean always;

	// for reduce/defer access
	// GMREDUCE_* (defined in gm_frontend_api.h)
	public GM_REDUCE_T reduce_op;
	public gm_symtab_entry bound_symbol;

	public boolean is_supplement; // is-supplement LHS
	public gm_symtab_entry org_lhs;

	// one exxample of  access instance 
	// i.e. location in the code (for error message generation)
	public ast_id location;

	public int mutate_direction;

	public gm_rwinfo()
	{
		driver = null;
		bound_symbol = null;
		location = null;
		always = true;
		reduce_op = GM_REDUCE_T.GMREDUCE_NULL;
		access_range = gm_range_type_t.GM_RANGE_SINGLE; // default is single access
		mutate_direction = -1;
		org_lhs = null;
		is_supplement = false;
	}

	public static gm_rwinfo new_scala_inst(ast_id loc, GM_REDUCE_T reduce_op, gm_symtab_entry bound_symbol, boolean supple)
	{
		return new_scala_inst(loc, reduce_op, bound_symbol, supple, null);
	}
	public static gm_rwinfo new_scala_inst(ast_id loc, GM_REDUCE_T reduce_op, gm_symtab_entry bound_symbol)
	{
		return new_scala_inst(loc, reduce_op, bound_symbol, false, null);
	}
	public static gm_rwinfo new_scala_inst(ast_id loc, GM_REDUCE_T reduce_op)
	{
		return new_scala_inst(loc, reduce_op, null, false, null);
	}
	public static gm_rwinfo new_scala_inst(ast_id loc)
	{
		return new_scala_inst(loc, GM_REDUCE_T.GMREDUCE_NULL, null, false, null);
	}
//C++ TO JAVA CONVERTER NOTE: Java does not allow default values for parameters. Overloaded methods are inserted above.
//ORIGINAL LINE: static gm_rwinfo* new_scala_inst(ast_id* loc, int reduce_op = GMREDUCE_NULL, gm_symtab_entry* bound_symbol = null, boolean supple = false, gm_symtab_entry* org = null)
	public static gm_rwinfo new_scala_inst(ast_id loc, GM_REDUCE_T reduce_op, gm_symtab_entry bound_symbol, boolean supple, gm_symtab_entry org)
	{
		gm_rwinfo g = new gm_rwinfo();
		g.location = loc;
		g.reduce_op = reduce_op;
		g.bound_symbol = bound_symbol;
		g.is_supplement = supple;
		g.org_lhs = org;
		return g;
	}

	public static gm_rwinfo new_builtin_inst(ast_id loc, int mutate_dir)
	{
		gm_rwinfo g = new gm_rwinfo();
		g.location = loc;
		g.mutate_direction = mutate_dir;
		return g;
	}

	public static gm_rwinfo new_field_inst(gm_symtab_entry driver, ast_id loc, GM_REDUCE_T reduce_op, gm_symtab_entry bound_symbol, boolean supple)
	{
		return new_field_inst(driver, loc, reduce_op, bound_symbol, supple, null);
	}
	public static gm_rwinfo new_field_inst(gm_symtab_entry driver, ast_id loc, GM_REDUCE_T reduce_op, gm_symtab_entry bound_symbol)
	{
		return new_field_inst(driver, loc, reduce_op, bound_symbol, false, null);
	}
	public static gm_rwinfo new_field_inst(gm_symtab_entry driver, ast_id loc, GM_REDUCE_T reduce_op)
	{
		return new_field_inst(driver, loc, reduce_op, null, false, null);
	}
	public static gm_rwinfo new_field_inst(gm_symtab_entry driver, ast_id loc)
	{
		return new_field_inst(driver, loc, GM_REDUCE_T.GMREDUCE_NULL, null, false, null);
	}
//C++ TO JAVA CONVERTER NOTE: Java does not allow default values for parameters. Overloaded methods are inserted above.
//ORIGINAL LINE: static gm_rwinfo* new_field_inst(gm_symtab_entry* driver, ast_id* loc, int reduce_op = GMREDUCE_NULL, gm_symtab_entry* bound_symbol = null, boolean supple = false, gm_symtab_entry* org = null)
	public static gm_rwinfo new_field_inst(gm_symtab_entry driver, ast_id loc, GM_REDUCE_T reduce_op, gm_symtab_entry bound_symbol, boolean supple, gm_symtab_entry org)
	{
		gm_rwinfo g = new gm_rwinfo();
		g.location = loc;
		g.driver = driver;
		g.reduce_op = reduce_op;
		g.bound_symbol = bound_symbol;
		g.is_supplement = supple;
		g.org_lhs = org;
		return g;
	}
	public static gm_rwinfo new_range_inst(gm_range_type_t range, boolean always, ast_id loc)
	{
		gm_rwinfo g = new gm_rwinfo();
		g.always = always;
		g.location = loc;
		g.access_range = range;
		return g;
	}

	// make a copy of this instance
	public final gm_rwinfo copy()
	{
		gm_rwinfo rwie = new gm_rwinfo();
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy assignment (rather than a reference assignment) - this should be verified and a 'copyFrom' method should be created if it does not yet exist:
//ORIGINAL LINE: *rwie = *this;
		rwie.copyFrom(this); // copy by assignment
		return rwie;
	}

	// for debugging

	// print each debug info
	public final void print()
	{
		if (access_range == gm_range_type_t.GM_RANGE_SINGLE)
		{
			if (driver == null)
				System.out.print("(SCALAR, ");
			else
				System.out.printf("(%s, ", driver.getId().get_orgname());
		}
		else
		{
			System.out.printf("(%s, ", GlobalMembersGm_rw_analysis.gm_get_range_string(access_range));
		}

		if (always)
			System.out.print("ALWAYS");
		else
			System.out.print("COND");

		if (bound_symbol != null)
		{
			System.out.printf(" ,%s, %s ", GlobalMembersGm_misc.gm_get_reduce_string(reduce_op), bound_symbol.getId().get_orgname());
		}

		System.out.print(")");
	}
}