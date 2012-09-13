package backend_cpp;

import ast.AST_NODE_TYPE;
import ast.ast_assign;
import ast.ast_field;
import ast.ast_id;
import ast.ast_sent;
import frontend.gm_symtab_entry;
import inc.GM_REDUCE_T;
import inc.gm_assignment_t;

import common.gm_apply;

//---------------------------------------------------
// replace  a.X <= <expr> 
// with     a.X_new = <expr> 
//---------------------------------------------------
class gm_replace_da_t extends gm_apply {
	
	private gm_symtab_entry e_old;
	private gm_symtab_entry e_new;
	private ast_sent s;
	
	@Override
	public final boolean apply(ast_sent s) {
		if (s.get_nodetype() != AST_NODE_TYPE.AST_ASSIGN)
			return true;
		ast_assign a = (ast_assign) s;
		if (a.is_defer_assign()) {
			if (!a.is_target_scalar()) {
				ast_field lhs = a.get_lhs_field();
				ast_id prop = lhs.get_second();
				if (prop.getSymInfo() == e_old) {
					// replace it into normal write to new property
					prop.setSymInfo(e_new);

					ast_id bound = a.get_bound();
					a.set_bound(null);
					a.set_assign_type(gm_assignment_t.GMASSIGN_NORMAL);
					a.set_reduce_type(GM_REDUCE_T.GMREDUCE_NULL);
					if (bound != null)
						bound.dispose();
				}
			}
		}
		return true;
	}

	public final void replace_da(gm_symtab_entry o, gm_symtab_entry n, ast_sent _s) {
		e_old = o;
		e_new = n;
		s = _s;
		set_all(false);
		set_for_sent(true);
		s.traverse_post(this);
	}

}