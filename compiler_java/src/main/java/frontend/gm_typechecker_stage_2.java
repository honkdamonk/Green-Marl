package frontend;

import inc.GMTYPE_T;
import ast.AST_NODE_TYPE;
import ast.ast_assign;
import ast.ast_expr;
import ast.ast_expr_builtin;
import ast.ast_expr_builtin_field;
import ast.ast_field;
import ast.ast_id;
import ast.ast_sent;
import ast.ast_typedecl;

import common.GM_ERRORS_AND_WARNINGS;
import common.gm_error;
import common.gm_main;
import common.gm_apply;
import common.gm_builtin_def;

//----------------------------------------------------------------
// Type-Check Step 2:
//     (1) Check group assignment.
//          e.g. G.X = G.Y  where X:N_P, Y:E_P
//     (2) Find (built-in) function definitions
//          * check argument counts
//     (3) Check if BFS is nested (temporary)
//----------------------------------------------------------------
public class gm_typechecker_stage_2 extends gm_apply {
	
	public gm_typechecker_stage_2() {
		_is_okay = true;
		set_for_expr(true);
		set_for_sent(true);
		set_separate_post_apply(true);
		bfs_level = 0;
		_is_group_assignment = false;
		_is_group_assignment_node_prop = false; // node or edge property
		_group_sym = null;
	}

	// pre
	@Override
	public boolean apply(ast_sent s) {
		if (s.get_nodetype() == AST_NODE_TYPE.AST_BFS) {
			if (bfs_level > 0) {
				gm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_NESTED_BFS, s.get_line(), s.get_col());
				set_okay(false);
			}
			bfs_level++;
		}

		if (s.get_nodetype() == AST_NODE_TYPE.AST_ASSIGN) {
			ast_assign a = (ast_assign) s;
			if (!a.is_target_scalar()) {
				ast_field f = a.get_lhs_field();
				if (f.get_first().getTypeInfo().is_graph() || f.get_first().getTypeInfo().is_collection()) {

					// printf("begin group assignment\n");
					_is_group_assignment = true;

					if (f.get_second().getTypeInfo().is_node_property())
						_is_group_assignment_node_prop = true;
					else
						_is_group_assignment_node_prop = false;

					_group_sym = f.get_first().getSymInfo();

					if (a.is_reduce_assign() || a.is_defer_assign()) {
						gm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_GROUP_REDUCTION, a.get_line(), a.get_col());
						set_okay(false);
						return false;
					}
				}
			}
		}

		return is_okay();
	}

	// post
	@Override
	public boolean apply2(ast_sent s) {
		if (s.get_nodetype() == AST_NODE_TYPE.AST_BFS) {
			bfs_level--;
		}
		if (s.get_nodetype() == AST_NODE_TYPE.AST_ASSIGN) {
			if (_is_group_assignment) {
				_is_group_assignment = false;
				// printf("end group assignment\n");
			}
		}

		return is_okay();
	}

	@Override
	public boolean apply(ast_expr e) {
		boolean is_okay = true;
		switch (e.get_opclass()) {
		case GMEXPR_ID: {
			if (_is_group_assignment) {
				ast_id id = e.get_id();
				if (id.getSymInfo() == _group_sym) {
					if (_is_group_assignment_node_prop)
						e.set_alternative_type(GMTYPE_T.GMTYPE_NODE);
					else
						e.set_alternative_type(GMTYPE_T.GMTYPE_EDGE);
				}
			}
			break;
		}
		case GMEXPR_FIELD: {
			ast_field f = e.get_field();
			is_okay = apply_on_field(f);
			break;
		}
		case GMEXPR_BUILTIN: {
			// find function definition:w
			ast_expr_builtin builtin = (ast_expr_builtin) e;
			is_okay = apply_on_builtin(builtin);
			break;
		}
		case GMEXPR_BUILTIN_FIELD: {
			ast_expr_builtin_field builtinField = (ast_expr_builtin_field) e;
			is_okay = apply_on_builtin(builtinField);
			is_okay &= apply_on_field(builtinField.get_field_driver());
			break;
		}
		default:
			break;
		}
		set_okay(is_okay);
		return is_okay;
	}

	public final void set_okay(boolean b) {
		_is_okay = b && _is_okay;
	}

	public final boolean is_okay() {
		return _is_okay;
	}

	private boolean _is_group_assignment;
	private boolean _is_group_assignment_node_prop; // node or edge
	private boolean _is_okay;
	private int bfs_level;
	private gm_symtab_entry _group_sym;

	private boolean apply_on_builtin(ast_expr_builtin builtinExpr) {
		GMTYPE_T sourceType = builtinExpr.get_source_type();
		switch (sourceType) {
		case GMTYPE_PROPERTYITER_SET:
		case GMTYPE_COLLECTIONITER_SET:
			sourceType = GMTYPE_T.GMTYPE_NSET;
			break;
		case GMTYPE_PROPERTYITER_SEQ:
		case GMTYPE_COLLECTIONITER_SEQ:
			sourceType = GMTYPE_T.GMTYPE_NSEQ;
			break;
		case GMTYPE_PROPERTYITER_ORDER:
		case GMTYPE_COLLECTIONITER_ORDER:
			sourceType = GMTYPE_T.GMTYPE_NORDER;
			break;
		default:
			break;
		}
		return set_and_check_builtin_definition(builtinExpr, sourceType);
	}

	private boolean set_and_check_builtin_definition(ast_expr_builtin builtinExpr, GMTYPE_T sourceType) {

		gm_builtin_def builtinDef = gm_main.BUILT_IN.find_builtin_def(sourceType, builtinExpr.get_callname());

		if (builtinDef == null) {
			if (_is_group_assignment && (sourceType.is_graph_type() || sourceType.is_collection_type())) {
				if (_is_group_assignment_node_prop)
					sourceType = GMTYPE_T.GMTYPE_NODE;
				else
					sourceType = GMTYPE_T.GMTYPE_EDGE;

				builtinDef = gm_main.BUILT_IN.find_builtin_def(sourceType, builtinExpr.get_callname());
			}
		}

		boolean isOkay = true;
		if (builtinDef == null) {
			gm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_INVALID_BUILTIN, builtinExpr.get_line(), builtinExpr.get_col(),
					builtinExpr.get_callname());
			isOkay = false;
		}
		builtinExpr.set_builtin_def(builtinDef);

		if (isOkay) {
			java.util.LinkedList<ast_expr> arguments = builtinExpr.get_args();

			int argCount = arguments.size();
			if (argCount != builtinDef.get_num_args()) {
				gm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_INVALID_BUILTIN_ARG_COUNT, builtinExpr.get_line(), builtinExpr.get_col(),
						builtinExpr.get_callname());
				isOkay = false;
			}
		}
		return isOkay;
	}

	private boolean apply_on_field(ast_field f) {

		ast_id driver = f.get_first();
		ast_typedecl t = driver.getTypeInfo();

		if (t.is_graph() || t.is_collection()) // group assignment
		{
			if ((!_is_group_assignment) || (_group_sym != driver.getSymInfo())) {
				gm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_INVALID_GROUP_DRIVER, driver);
				return false;
			}
			// check node property
			ast_typedecl prop_type = f.get_second().getTypeInfo();
			if (_is_group_assignment_node_prop) {
				if (!prop_type.is_node_property()) {
					gm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_WRONG_PROPERTY, f.get_second(), "Node_Property");
					return false;
				}
			} else {
				if (!prop_type.is_edge_property()) {
					gm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_WRONG_PROPERTY, f.get_second(), "Edge_Property");
					return false;
				}
			}
		}
		return true;
	}
}