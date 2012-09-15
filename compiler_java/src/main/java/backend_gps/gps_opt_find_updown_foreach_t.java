package backend_gps;

import inc.gm_type;
import inc.gm_ops;

import java.util.LinkedList;

import ast.ast_node_type;
import ast.ast_expr;
import ast.ast_field;
import ast.ast_foreach;
import ast.ast_if;
import ast.ast_sent;

import common.gm_apply;
import common.gm_transform_helper;

import frontend.gm_symtab_entry;

//----------------------------------------------------------------------
// Create body
//  ->  find and replace up/down nbr
//----------------------------------------------------------------------

public class gps_opt_find_updown_foreach_t extends gm_apply
{

	public gps_opt_find_updown_foreach_t(gm_symtab_entry c, gm_symtab_entry l)
	{
		this.curr_sym = c;
		this.lev_sym = l;
		set_for_sent(true);
	}

	@Override
	public boolean apply(ast_sent s)
	{
		if (s.get_nodetype() == ast_node_type.AST_FOREACH)
		{
			ast_foreach fe = (ast_foreach) s;
			if ((fe.get_iter_type() == gm_type.GMTYPE_NODEITER_UP_NBRS) || (fe.get_iter_type() == gm_type.GMTYPE_NODEITER_DOWN_NBRS))
			{
				targets.addLast(fe);
			}
		}
		return true;
	}

	public final void post_process()
	{
		//-------------------------
		//  Foreach(i: n.UpNbrs) { ... }
		//  ==>
		//  Foreach(i: n.InNbrs) If (i.lev == curr_level -1) { ... }
		//-------------------------
		for (ast_foreach fe : targets)
		{
			ast_sent body = fe.get_body();
			gm_transform_helper.gm_ripoff_sent(body);

			gm_type new_iter_type = (fe.get_iter_type() == gm_type.GMTYPE_NODEITER_UP_NBRS) ? gm_type.GMTYPE_NODEITER_IN_NBRS : gm_type.GMTYPE_NODEITER_NBRS;
			gm_ops op_for_check = (fe.get_iter_type() == gm_type.GMTYPE_NODEITER_UP_NBRS) ? gm_ops.GMOP_SUB : gm_ops.GMOP_ADD;

			// chechge iter type
			fe.set_iter_type(new_iter_type);
			fe.get_iterator().getTypeInfo().set_typeid(new_iter_type);

			// if (i.lev == (curr_level -1))
			ast_expr check_level = ast_expr.new_comp_expr(gm_ops.GMOP_EQ, ast_expr.new_field_expr(ast_field.new_field(fe.get_iterator().copy(true), lev_sym.getId().copy(true))), ast_expr.new_biop_expr(op_for_check, ast_expr.new_id_expr(curr_sym.getId().copy(true)), ast_expr.new_ival_expr(1)));
			ast_if check_level_if = ast_if.new_if(check_level, body, null);
			fe.set_body(check_level_if);
		}
	}

	private LinkedList<ast_foreach> targets = new LinkedList<ast_foreach>();
	private gm_symtab_entry curr_sym;
	private gm_symtab_entry lev_sym;

}