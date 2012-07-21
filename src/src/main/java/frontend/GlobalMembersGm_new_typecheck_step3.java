import ast.ast_expr;
import ast.ast_id;
import inc.GlobalMembersGm_defs;

public class GlobalMembersGm_new_typecheck_step3
{

	public static boolean check_special_case_inside_group_assign(ast_id l_id, int alt_type_l, ast_expr r)
	{

		int r_type = r.get_type_summary();

		if (GlobalMembersGm_defs.gm_is_node_compatible_type(alt_type_l) && !GlobalMembersGm_defs.gm_is_node_compatible_type(r_type))
			return false;

		if (GlobalMembersGm_defs.gm_is_edge_compatible_type(alt_type_l) && !GlobalMembersGm_defs.gm_is_edge_compatible_type(r_type))
			return false;

		assert l_id.getTypeInfo().is_graph() || l_id.getTypeInfo().is_collection();

		if (l_id.getTypeInfo().is_graph() && (l_id.getSymInfo() != r.get_bound_graph()))
			return false;

		if (l_id.getTypeInfo().is_collection() && (l_id.getTypeInfo().get_target_graph_sym() != r.get_bound_graph()))
			return false;

		return true;
	}

	public static boolean gm_is_compatible_type_collection_of_collection(int shouldbeType, int currentType, int methodId)
	{
		//TODO find better way to do this
		switch (methodId)
		{
			case GM_BLTIN_SET_ADD:
			case GM_BLTIN_SET_ADD_BACK:
				return shouldbeType == currentType;
			case GM_BLTIN_SET_REMOVE:
			case GM_BLTIN_SET_REMOVE_BACK:
			case GM_BLTIN_SET_SIZE:
				return true;
			default:
				assert false;
				return false;
		}

		return true;
	}
//C++ TO JAVA CONVERTER NOTE: Access declarations are not available in Java:
	//;

	// defined in gm_coercion.cc
	//extern void gm_insert_explicit_type_conversion_for_op(java.util.HashMap<ast_expr, int> targets);
}