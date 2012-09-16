package ast;

import static ast.ast_node_type.AST_EXPR_MAPACCESS;
import static inc.gm_expr_class.GMEXPR_MAPACCESS;
import static inc.gm_ops.GMOP_MAPACCESS;
import inc.gm_expr_class;
import inc.gm_ops;

public class ast_expr_mapaccess extends ast_expr {

	private ast_mapaccess mapAccess;

	private ast_expr_mapaccess() {
		set_nodetype(AST_EXPR_MAPACCESS);
	}

	private ast_expr_mapaccess(ast_mapaccess mapAccess) {
		// ast_expr(), mapAccess(mapAccess) {
		set_nodetype(AST_EXPR_MAPACCESS);
		set_line(mapAccess.line);
		set_col(mapAccess.col);
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
	public gm_ops get_optype() {
		return GMOP_MAPACCESS;
	}

	@Override
	public gm_expr_class get_opclass() {
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

	public static ast_expr_mapaccess new_expr_mapaccess(ast_mapaccess mapAccess) {
		ast_expr_mapaccess newMapAccess = new ast_expr_mapaccess(mapAccess);
		return newMapAccess;
	}
}
