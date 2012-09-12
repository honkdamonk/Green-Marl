package ast;

import static ast.AST_NODE_TYPE.AST_EXPR_MAPACCESS;
import inc.GMEXPR_CLASS;
import inc.GM_OPS_T;
import static inc.GMEXPR_CLASS.GMEXPR_MAPACCESS;
import static inc.GM_OPS_T.GMOP_MAPACCESS;

public class ast_expr_mapaccess extends ast_expr {

	private ast_mapaccess mapAccess;

	private ast_expr_mapaccess() {
		// ast_expr(), mapAccess(null) {
		set_nodetype(AST_EXPR_MAPACCESS);
	}

	private ast_expr_mapaccess(ast_mapaccess mapAccess, int line, int column) {
		// ast_expr(), mapAccess(mapAccess) {
		set_nodetype(AST_EXPR_MAPACCESS);
		set_line(line);
		set_col(column);
	}

	public ast_expr_mapaccess copy() {
		return copy(false);
	}

	@Override
	public ast_expr_mapaccess copy(boolean cp_sym) {
		ast_expr_mapaccess clone = new ast_expr_mapaccess();
		clone.mapAccess = mapAccess.copy(cp_sym);
		return clone;
	}

	@Override
	public boolean is_mapaccess() {
		return true;
	}

	@Override
	public GM_OPS_T get_optype() {
		return GMOP_MAPACCESS;
	}

	@Override
	public GMEXPR_CLASS get_opclass() {
		return GMEXPR_MAPACCESS;
	}

	@Override
	public ast_id get_id() {
		return mapAccess.get_map_id();
	}

	public ast_mapaccess get_mapaccess() {
		assert (mapAccess != null);
		return mapAccess;
	}

	public static ast_expr_mapaccess new_expr_mapaccess(ast_mapaccess mapAccess, int line, int column) {
		ast_expr_mapaccess newMapAccess = new ast_expr_mapaccess(mapAccess, line, column);
		return newMapAccess;
	}
}
