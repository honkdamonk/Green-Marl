package common;

import ast.AST_NODE_TYPE;
import ast.ast_node;
import ast.ast_procdef;

public class gm_flat_nested_sentblock
{

	public static void gm_flat_nested_sentblock(ast_node n)
	{

		gm_flat_nested_sentblock_t T = new gm_flat_nested_sentblock_t();
		n.traverse_post(T);
		T.post_process();

		// need to re-build scope
		if (n.get_nodetype() == AST_NODE_TYPE.AST_PROCDEF)
			gm_transform_helper.gm_reconstruct_scope(((ast_procdef)n).get_body());
		else
		{
			while (!n.has_scope())
			{
				n = n.get_parent();
				assert n != null;
			}
			gm_transform_helper.gm_reconstruct_scope(n);
		}
	}
}