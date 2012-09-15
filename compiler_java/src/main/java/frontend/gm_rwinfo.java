package frontend;

import inc.gm_reduce;
import ast.ast_id;

public class gm_rwinfo {
	
	// RANGE_LINEAR, RANGE_RANDOM, RANGE_SINGLE
	public gm_range_type access_range;
	public gm_symtab_entry driver; // N.F --> N is the driver

	// if this destination always accessed
	// true: yes
	// false: no (there are non-accessing paths)
	public boolean always;

	// for reduce/defer access
	// GMREDUCE_* (defined in gm_frontend_api.h)
	public gm_reduce reduce_op;
	public gm_symtab_entry bound_symbol;

	public boolean is_supplement; // is-supplement LHS
	public gm_symtab_entry org_lhs;

	// one exxample of access instance
	// i.e. location in the code (for error message generation)
	public ast_id location;

	public int mutate_direction;

	public gm_rwinfo() {
		driver = null;
		bound_symbol = null;
		location = null;
		always = true;
		reduce_op = gm_reduce.GMREDUCE_NULL;
		// default is single access
		access_range = gm_range_type.GM_RANGE_SINGLE; 
		mutate_direction = -1;
		org_lhs = null;
		is_supplement = false;
	}

	public static gm_rwinfo new_scala_inst(ast_id loc, gm_reduce reduce_op, gm_symtab_entry bound_symbol, boolean supple) {
		return new_scala_inst(loc, reduce_op, bound_symbol, supple, null);
	}

	public static gm_rwinfo new_scala_inst(ast_id loc, gm_reduce reduce_op, gm_symtab_entry bound_symbol) {
		return new_scala_inst(loc, reduce_op, bound_symbol, false, null);
	}

	public static gm_rwinfo new_scala_inst(ast_id loc, gm_reduce reduce_op) {
		return new_scala_inst(loc, reduce_op, null, false, null);
	}

	public static gm_rwinfo new_scala_inst(ast_id loc) {
		return new_scala_inst(loc, gm_reduce.GMREDUCE_NULL, null, false, null);
	}

	public static gm_rwinfo new_scala_inst(ast_id loc, gm_reduce reduce_op, gm_symtab_entry bound_symbol, boolean supple, gm_symtab_entry org) {
		gm_rwinfo g = new gm_rwinfo();
		g.location = loc;
		g.reduce_op = reduce_op;
		g.bound_symbol = bound_symbol;
		g.is_supplement = supple;
		g.org_lhs = org;
		return g;
	}

	public static gm_rwinfo new_builtin_inst(ast_id loc, int mutate_dir) {
		gm_rwinfo g = new gm_rwinfo();
		g.location = loc;
		g.mutate_direction = mutate_dir;
		return g;
	}

	public static gm_rwinfo new_field_inst(gm_symtab_entry driver, ast_id loc, gm_reduce reduce_op, gm_symtab_entry bound_symbol, boolean supple) {
		return new_field_inst(driver, loc, reduce_op, bound_symbol, supple, null);
	}

	public static gm_rwinfo new_field_inst(gm_symtab_entry driver, ast_id loc, gm_reduce reduce_op, gm_symtab_entry bound_symbol) {
		return new_field_inst(driver, loc, reduce_op, bound_symbol, false, null);
	}

	public static gm_rwinfo new_field_inst(gm_symtab_entry driver, ast_id loc, gm_reduce reduce_op) {
		return new_field_inst(driver, loc, reduce_op, null, false, null);
	}

	public static gm_rwinfo new_field_inst(gm_symtab_entry driver, ast_id loc) {
		return new_field_inst(driver, loc, gm_reduce.GMREDUCE_NULL, null, false, null);
	}

	public static gm_rwinfo new_field_inst(gm_symtab_entry driver, ast_id loc, gm_reduce reduce_op, gm_symtab_entry bound_symbol, boolean supple,
			gm_symtab_entry org) {
		gm_rwinfo g = new gm_rwinfo();
		g.location = loc;
		g.driver = driver;
		g.reduce_op = reduce_op;
		g.bound_symbol = bound_symbol;
		g.is_supplement = supple;
		g.org_lhs = org;
		return g;
	}

	public static gm_rwinfo new_range_inst(gm_range_type range, boolean always, ast_id loc) {
		gm_rwinfo g = new gm_rwinfo();
		g.always = always;
		g.location = loc;
		g.access_range = range;
		return g;
	}

	// make a copy of this instance
	public final gm_rwinfo copy() {
		gm_rwinfo rwie = new gm_rwinfo();
		rwie.copyFrom(this); // copy by assignment
		return rwie;
	}

	// for debugging

	void copyFrom(gm_rwinfo source) {
		access_range = source.access_range;
		driver = source.driver;
		always = source.always;
		reduce_op = source.reduce_op;
		bound_symbol = source.bound_symbol;
		is_supplement = source.is_supplement;
		org_lhs = source.org_lhs;
		location = source.location;
		mutate_direction = source.mutate_direction;
	}

	// print each debug info
	public final void print() {
		if (access_range == gm_range_type.GM_RANGE_SINGLE) {
			if (driver == null)
				System.out.print("(SCALAR, ");
			else
				System.out.printf("(%s, ", driver.getId().get_orgname());
		} else {
			System.out.printf("(%s, ", gm_rw_analysis.gm_get_range_string(access_range));
		}

		if (always)
			System.out.print("ALWAYS");
		else
			System.out.print("COND");

		if (bound_symbol != null) {
			System.out.printf(" ,%s, %s ", reduce_op.get_reduce_string(), bound_symbol.getId().get_orgname());
		}

		System.out.print(")");
	}

}