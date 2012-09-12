package ast;

import static inc.gm_assignment_location_t.GMASSIGN_LHS_MAP;
import static inc.gm_assignment_t.GMASSIGN_REDUCE;
import inc.GM_REDUCE_T;
import inc.gm_assignment_location_t;

import common.gm_apply;

public class ast_assign_mapentry extends ast_assign {

	private ast_mapaccess lhs;

	private ast_assign_mapentry(ast_mapaccess lhs, ast_expr rhs) {
		super();
		this.lhs = lhs;
		this.rhs = rhs;
	}

	private ast_assign_mapentry(ast_mapaccess lhs, ast_expr rhs, GM_REDUCE_T reduceType) {
		super();
		this.rhs = rhs;
		this.lhs = lhs;
		set_reduce_type(reduceType);
		set_assign_type(GMASSIGN_REDUCE);
	}

	@Override
	public void reproduce(int indLevel) {
		lhs.reproduce(0);
		Out.SPC();
		if (is_reduce_assign())
			Out.push(get_reduce_type().get_reduce_string());
		else
			Out.push("=");
		Out.SPC();
		rhs.reproduce(0);
		Out.pushln(";");
	}

	@Override
	public void traverse_sent(gm_apply a, boolean is_post, boolean is_pre) {
		if (is_pre) {
			a.apply(to_assign_mapentry().get_lhs_mapaccess().get_key_expr());
		}

		get_rhs().traverse(a, is_post, is_pre);

		if (is_post) {
			a.apply(to_assign_mapentry().get_lhs_mapaccess().get_key_expr());
		}
	}

	@Override
	public boolean is_map_entry_assign() {
		return true;
	}

	@Override
	public boolean is_target_map_entry() {
		return true;
	}

	@Override
	public boolean is_target_scalar() {
		return false;
	}

	@Override
	public gm_assignment_location_t get_lhs_type() {
		return GMASSIGN_LHS_MAP;
	}

	@Override
	public ast_assign_mapentry to_assign_mapentry() {
		return this;
	}

	public ast_mapaccess get_lhs_mapaccess() {
		return lhs;
	}

	public static ast_assign_mapentry new_mapentry_assign(ast_mapaccess lhs, ast_expr rhs) {
		ast_assign_mapentry newAssign = new ast_assign_mapentry(lhs, rhs);
		return newAssign;
	}

	public static ast_assign_mapentry new_mapentry_reduce_assign(ast_mapaccess lhs, ast_expr rhs, GM_REDUCE_T reduceType) {
		ast_assign_mapentry newAssign = new ast_assign_mapentry(lhs, rhs, reduceType);
		return newAssign;
	}

}
