package ast;

import inc.GMEXPR_CLASS;
import inc.GMTYPE_T;
import inc.expr_list;
import common.GlobalMembersGm_misc;

public class ast_expr_builtin_field extends ast_expr_builtin {

	public void dispose() {
		if (field_driver != null)
			field_driver.dispose();
	}

	public final ast_id get_driver() {
		assert false;
		return null;
	}

	public final boolean driver_is_field() {
		return true;
	}

	public final ast_field get_field_driver() {
		return field_driver;
	}

	public final ast_field get_field() {
		return field_driver;
	}

	public final GMTYPE_T get_source_type() {
		assert field_driver != null;
		return field_driver.get_second().getTargetTypeInfo().getTypeSummary();
	}

	public static ast_expr_builtin_field new_builtin_field_expr(ast_field field, String orgname, expr_list exList) {

		ast_expr_builtin_field newExpression = new ast_expr_builtin_field();
		newExpression.expr_class = GMEXPR_CLASS.GMEXPR_BUILTIN_FIELD;
		newExpression.field_driver = field;
		newExpression.orgname = GlobalMembersGm_misc.gm_strdup(orgname);

		if (field != null) {
			field.set_parent(newExpression); // type unknown yet.
		}

		if (exList != null) {
			newExpression.args = new java.util.LinkedList<ast_expr>(exList.LIST); // shallow
																					// copy
																					// LIST
			// but not set 'up' pointer.
			for (ast_expr e : newExpression.args)
				e.set_parent(newExpression);
		}
		return newExpression;
	}

	private ast_expr_builtin_field() {
		super();
		this.field_driver = null;
	}

	private ast_field field_driver;
}