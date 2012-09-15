package common;

import frontend.gm_scope;
import frontend.gm_symtab;
import frontend.gm_typechecker_stage_1;
import inc.gm_type;
import inc.gm_reduce;
import ast.ast_expr;
import ast.ast_expr_reduce;
import ast.ast_foreach;
import ast.ast_id;
import ast.ast_sent;
import ast.ast_sentblock;
import ast.ast_typedecl;

public class gm_new_sents_after_tc {
	// temporary: should be improved
	// extern boolean gm_declare_symbol(gm_symtab SYM, ast_id id, ast_typedecl
	// type, boolean is_readable, boolean is_writeable);

	// ------------------------------------------------------------
	// create new foreach (called after typecheck is finished)
	// [input]
	// ast_id* itor: new iterator (no symbol entry)
	// ast_id* source: source of iteration (must have a valid symbol entry)
	// int iter_type: iteration type
	// ast_sent* body: body of sentence (Must have null enclosing scope)
	// : if NULL, a new empty sentence block is created as body.
	// [output]
	// ast_foreach* : a new foreach node that points all the input nodes.
	//
	// [what being done]
	// create foreach node
	// create 'foreach scope'.
	// - add 'iterator def' to this 'foreach' scope
	// - setup 'foreach scope' as the enclosing scope of body
	//
	// [Note]
	// * Iterator ids in body still do not contain valid symtab entry, after
	// this function.
	// * They should be adjusted after this function.
	// ------------------------------------------------------------
	public static ast_foreach gm_new_foreach_after_tc(ast_id it, ast_id src, ast_sent body, gm_type iter_type) {
		assert it.getSymInfo() == null;
		assert src.getSymInfo() != null;
		assert iter_type.is_iteration_on_all_graph() || iter_type.is_iteration_on_neighbors_compatible();

		// -----------------------------------------------------
		// create foreach node
		// -----------------------------------------------------
		if (body == null)
			body = ast_sentblock.new_sentblock();
		ast_foreach fe = ast_foreach.new_foreach(it, src, body, iter_type);

		// --------------------------------------------------
		// create iterator type
		// --------------------------------------------------
		ast_typedecl type;
		if (iter_type.is_iteration_on_all_graph()) {
			assert src.getTypeSummary().is_graph_type();
			type = ast_typedecl.new_nodeedge_iterator(src.copy(true), iter_type);
		} else if (iter_type.is_iteration_on_neighbors_compatible()) {
			assert src.getTypeSummary().is_node_compatible_type();
			type = ast_typedecl.new_nbr_iterator(src.copy(true), iter_type);
		} else {
			assert false;
			throw new AssertionError();
		}
		type.enforce_well_defined();

		// ----------------------------------------------
		// Add iterator definition to the 'this' scope
		// ----------------------------------------------
		gm_symtab vars = fe.get_symtab_var();
		boolean success;
		// enforce type well defined ness (upscope of this foreach is not
		// available yet)

		success = gm_typechecker_stage_1.gm_declare_symbol(vars, it, type, true, false);

		assert success;
		assert it.getSymInfo() != null;
		assert it.getTypeInfo().get_target_graph_id() != null;

		// ----------------------------------------------
		// set enclosing scope of the body
		// ----------------------------------------------
		gm_scope S = new gm_scope();
		fe.get_this_scope(S);
		gm_transform_helper.gm_put_new_upper_scope_on_null(body, S);

		return fe;
	}

	// almost identical to new_foreach_after_tc
	public static ast_expr_reduce gm_new_expr_reduce_after_tc(ast_id it, ast_id src, ast_expr body, ast_expr filter, gm_type iter_type, gm_reduce op_type) {
		assert it.getSymInfo() == null;
		assert src.getSymInfo() != null;
		assert iter_type.is_iteration_on_all_graph() || iter_type.is_iteration_on_neighbors_compatible();

		// -----------------------------------------------------
		// create expression node
		// -----------------------------------------------------
		ast_expr_reduce R = ast_expr_reduce.new_reduce_expr(op_type, it, src, iter_type, body, filter);

		// --------------------------------------------------
		// create iterator type
		// --------------------------------------------------
		ast_typedecl type;
		if (iter_type.is_iteration_on_all_graph()) {
			assert src.getTypeSummary().is_graph_type();
			type = ast_typedecl.new_nodeedge_iterator(src.copy(true), iter_type);
		} else if (iter_type.is_iteration_on_neighbors_compatible()) {
			assert src.getTypeSummary().is_node_compatible_type();
			type = ast_typedecl.new_nbr_iterator(src.copy(true), iter_type);
		} else {
			assert false;
			throw new AssertionError();
		}
		type.enforce_well_defined();

		// ----------------------------------------------
		// Add iterator definition to the 'this' scope
		// ----------------------------------------------
		gm_symtab vars = R.get_symtab_var();
		boolean success;
		// enforce type well defined ness (upscope of this foreach is not
		// available yet)
		success = gm_typechecker_stage_1.gm_declare_symbol(vars, it, type, true, false);

		assert success;
		assert it.getSymInfo() != null;
		assert it.getTypeInfo().get_target_graph_id() != null;

		// ----------------------------------------------
		// set enclosing scope of the body
		// ----------------------------------------------
		gm_scope S = new gm_scope();
		R.get_this_scope(S);

		gm_transform_helper.gm_put_new_upper_scope_on_null(body, S);
		if (filter != null)
			gm_transform_helper.gm_put_new_upper_scope_on_null(filter, S);

		return R;
	}

	// --------------------------------------------------------------
	// Create bottom symbol for reduction
	// --------------------------------------------------------------
	public static ast_expr gm_new_bottom_symbol(gm_reduce reduce_type, gm_type lhs_type) {
		ast_expr init_val;
		switch (reduce_type) {
		case GMREDUCE_PLUS: // Sum
			if (lhs_type.is_integer_type())
				init_val = ast_expr.new_ival_expr(0);
			else
				init_val = ast_expr.new_fval_expr(0.0);
			break;
		case GMREDUCE_MULT: // Product
			if (lhs_type.is_integer_type())
				init_val = ast_expr.new_ival_expr(1);
			else
				init_val = ast_expr.new_fval_expr(1.0);
			break;
		case GMREDUCE_MIN:
			init_val = ast_expr.new_inf_expr(true);
			init_val.set_type_summary(lhs_type.get_sized_inf_type());
			break;
		case GMREDUCE_MAX:
			init_val = ast_expr.new_inf_expr(false);
			init_val.set_type_summary(lhs_type.get_sized_inf_type());
			break;
		case GMREDUCE_AND:
			init_val = ast_expr.new_bval_expr(true);
			break;
		case GMREDUCE_OR:
			init_val = ast_expr.new_bval_expr(false);
			break;
		default:
			System.out.printf("%d %s \n", reduce_type, reduce_type.get_reduce_string());
			assert false;
			throw new AssertionError();
		}

		return init_val;
	}
}