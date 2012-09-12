package backend_cpp;

import tangible.RefObject;
import frontend.gm_symtab_entry;
import inc.GMTYPE_T;
import inc.GM_OPS_T;
import inc.gm_compile_step;
import ast.ast_expr;
import ast.ast_expr_builtin;
import ast.ast_id;
import ast.ast_procdef;

import common.gm_method_id_t;
import common.gm_traverse;

public class gm_cpp_opt_common_nbr extends gm_compile_step
{
	private gm_cpp_opt_common_nbr()
	{
		set_description("Common Neigbhor Iteration");
	}
	
	public void process(ast_procdef p)
	{
		cpp_opt_common_nbr_t T = new cpp_opt_common_nbr_t();
		gm_traverse.gm_traverse_sents(p.get_body(), T);
		if (T.has_targets())
		{
			T.transform_targets();
			set_affected(true);
		}
	}
	
	@Override
	public gm_compile_step get_instance()
	{
		return new gm_cpp_opt_common_nbr();
	}
	
	public static gm_compile_step get_factory()
	{
		return new gm_cpp_opt_common_nbr();
	}
	
	//---------------------------------------------
	// Optimize Common Nbr 
	//---------------------------------------------
	// Finding t such that x --> t and  y --> t
	//---------------------------------------------
	// Foreach(t: x.Nbrs){
	//   If (t.IsNbrFrom(y)) {  // t is an common nbr of x and y
	//      ...
	//   }
	// }
	// ===>
	// Foreach(t: x.CommonNbrs(y)){
	//    ...
	// }
	// 
	//---------------------------------------------

	public static boolean is_common_nbr_expression(ast_expr e, RefObject<gm_symtab_entry> s)
	{
		// all right. Just be practical and check only two cases:
		// t.IsNbrFrom(y)
		// t.IsNbrFrom(y) == True
		if (e.get_optype() == GM_OPS_T.GMOP_EQ)
		{
			ast_expr l = e.get_left_op();
			ast_expr r = e.get_right_op();
			if (r.is_literal() && (r.get_type_summary() == GMTYPE_T.GMTYPE_BOOL) && (r.get_bval() == true))
			{
				e = l;
			}
			else
				return false;
		}

		if (e.is_builtin())
		{
			ast_expr_builtin b = (ast_expr_builtin) e;

			// check if node.isNobrTo()

			ast_id driver = b.get_driver();
			if (driver == null)
				return false;
			if (!driver.getTypeInfo().is_node_compatible())
				return false;

			if (b.get_builtin_def().get_method_id() == gm_method_id_t.GM_BLTIN_NODE_IS_NBR)
			{
				java.util.LinkedList<ast_expr> L = b.get_args();
				ast_expr arg = L.getFirst();
				assert arg != null;
				if (!arg.is_id())
					return false;
				s.argvalue = (arg.get_id()).getSymInfo();
				return true;
			}
		}

		return false;
	}
}