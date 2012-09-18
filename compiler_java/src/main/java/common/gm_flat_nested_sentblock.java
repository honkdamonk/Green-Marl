package common;

import ast.ast_node;
import ast.ast_node_type;
import ast.ast_procdef;

public class gm_flat_nested_sentblock {

	public static void apply(ast_node n) {

		gm_flat_nested_sentblock_t T = new gm_flat_nested_sentblock_t();
		n.traverse_post(T);
		T.post_process();

		// need to re-build scope
		if (n.get_nodetype() == ast_node_type.AST_PROCDEF)
			gm_transform_helper.gm_reconstruct_scope(((ast_procdef) n).get_body());
		else {
			while (!n.has_scope()) {
				n = n.get_parent();
				assert n != null;
			}
			gm_transform_helper.gm_reconstruct_scope(n);
		}
	}
}