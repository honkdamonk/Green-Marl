package backend_cpp;

import frontend.gm_symtab_entry;
import inc.gm_type;

import java.util.Iterator;
import java.util.LinkedList;

import tangible.RefObject;
import ast.ast_foreach;
import ast.ast_if;
import ast.ast_node_type;
import ast.ast_sent;

import common.gm_apply;
import common.gm_transform_helper;

class cpp_opt_common_nbr_t extends gm_apply {
	
	private final LinkedList<gm_cpp_common_nbr_item_t> _targets = new LinkedList<gm_cpp_common_nbr_item_t>();
	
	// find targets
	@Override
	public boolean apply(ast_sent sent) {
		// check only for foreach
		// todo: do similar thing for BFS
		if (sent.get_nodetype() != ast_node_type.AST_FOREACH)
			return true;

		ast_foreach fe = (ast_foreach) sent;

		if (fe.get_iter_type() != gm_type.GMTYPE_NODEITER_NBRS)
			return true;

		ast_sent fe_body = gm_transform_helper.gm_get_sentence_if_trivial_sentblock(fe.get_body());

		if (fe_body.get_nodetype() != ast_node_type.AST_IF)
			return true;

		ast_if iff = (ast_if) fe_body;
		if (iff.get_else() != null)
			return true;

		gm_symtab_entry t_sym = null;
		RefObject<gm_symtab_entry> t_sym_wrapper = new RefObject<gm_symtab_entry>(t_sym);
		if (gm_cpp_opt_common_nbr.is_common_nbr_expression(iff.get_cond(), t_sym_wrapper)) {
			gm_cpp_common_nbr_item_t T = new gm_cpp_common_nbr_item_t();
			T.fe = fe;
			T.iff = iff;
			T.nested_iff = false;
			T.common_sym = t_sym_wrapper.argvalue;
			_targets.addLast(T);
		} else {
			ast_sent iff_body = gm_transform_helper.gm_get_sentence_if_trivial_sentblock(iff.get_then());
			if (iff_body.get_nodetype() != ast_node_type.AST_IF)
				return true;

			ast_if iff2 = (ast_if) iff_body;
			if (gm_cpp_opt_common_nbr.is_common_nbr_expression(iff2.get_cond(), t_sym_wrapper)) {
				gm_cpp_common_nbr_item_t T = new gm_cpp_common_nbr_item_t();
				T.fe = fe;
				T.iff = iff2;
				T.nested_iff = true;
				T.out_iff = iff;
				T.common_sym = t_sym_wrapper.argvalue;
				_targets.addLast(T);
			}

			return true;
		}
		return true;
	}

	// iterate over targets
	final void transform_targets() {
		Iterator<gm_cpp_common_nbr_item_t> I;
		for (I = _targets.iterator(); I.hasNext();) {
			apply_transform(I.next());
		}
	}

	final boolean has_targets() {
		return _targets.size() > 0;
	}

	// ---------------------------------------------
	// apply to each BFS
	// ---------------------------------------------
	private void apply_transform(gm_cpp_common_nbr_item_t T) {
		ast_foreach fe = T.fe;
		ast_if iff = T.iff;
		ast_if out_iff = T.out_iff;
		boolean nested_iff = T.nested_iff;
		gm_symtab_entry common_sym = T.common_sym;

		ast_sent if_body = iff.get_then();
		gm_transform_helper.gm_ripoff_sent(if_body);
		gm_transform_helper.gm_ripoff_sent(iff);

		if (!nested_iff) {
			// --------------------------
			// foreach(n: x.Nbrs)
			// {
			// If (n.isNbrFrom(z))
			// // body
			// }
			// ==>
			// foreach(n: x.CommonNbrs(z))
			// {
			// // body
			// }
			// -----------------------------
			fe.set_body(if_body);
		} else {
			// --------------------------
			// foreach(n: x.Nbrs)
			// {
			// If( ...) {
			// If (n.isNbrFrom(z))
			// // body
			// }
			// }
			// ==>
			// foreach(n: x.CommonNbrs(z))
			// {
			// If (...) {
			// // body
			// }
			// }
			// -----------------------------
			// set if_body to for_body
			out_iff.set_then(if_body);
		}

		// set new iterator
		fe.set_iter_type(gm_type.GMTYPE_NODEITER_COMMON_NBRS);
		fe.get_iterator().getSymInfo().getType().set_typeid(gm_type.GMTYPE_NODEITER_COMMON_NBRS);
		fe.set_source2(common_sym.getId().copy());

		// adjust scope information of fe and below
		gm_transform_helper.gm_reconstruct_scope(fe);

		// iff not used anymore
		if (iff != null)
			iff.dispose();
	}

}