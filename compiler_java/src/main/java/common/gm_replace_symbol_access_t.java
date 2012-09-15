package common;

import java.util.Iterator;
import java.util.LinkedList;

import ast.ast_node_type;
import ast.ast_assign;
import ast.ast_expr;
import ast.ast_field;
import ast.ast_id;
import ast.ast_node;
import ast.ast_sent;

public class gm_replace_symbol_access_t extends gm_apply {

	private gm_sym_change_info INFO;
	private boolean changed;

	public gm_replace_symbol_access_t(gm_sym_change_info I) {
		INFO = new gm_sym_change_info(I);
		set_for_sent(I.change_lhs);
		set_for_expr(I.change_rhs);
		changed = false;
	}

	private ast_id make_replace_id(ast_id src) {
		ast_id new_id = INFO.tgt.getId().copy(true);
		new_id.set_line(src.get_line());
		new_id.set_col(src.get_col());
		return new_id;
	}

	private ast_id make_replace_id(ast_field src) {
		ast_id new_id = INFO.tgt.getId().copy(true);
		new_id.set_line(src.get_line());
		new_id.set_col(src.get_col());
		return new_id;
	}

	private ast_field make_replace_field(ast_id src) {
		ast_id new_id1 = INFO.t_drv.getId().copy(true);
		ast_id new_id2 = INFO.tgt.getId().copy(true);
		ast_field f = ast_field.new_field(new_id1, new_id2);
		new_id1.set_line(src.get_line());
		new_id1.set_col(src.get_col());
		new_id2.set_line(src.get_line());
		new_id2.set_col(src.get_col());
		f.set_line(new_id1.get_line());
		f.set_col(new_id1.get_col());

		return f;
	}

	private ast_field make_replace_field(ast_field src) {
		ast_id new_id1 = INFO.t_drv.getId().copy(true);
		ast_id new_id2 = INFO.tgt.getId().copy(true);
		ast_field f = ast_field.new_field(new_id1, new_id2);
		new_id1.set_line(src.get_first().get_line());
		new_id1.set_col(src.get_first().get_col());
		new_id2.set_line(src.get_second().get_line());
		new_id2.set_col(src.get_second().get_col());
		f.set_line(src.get_line());
		f.set_col(src.get_col());

		return f;
	}

	private ast_node make_replace(ast_id src) {
		if (INFO.tgt_scalar)
			return make_replace_field(src);
		else
			return make_replace_id(src);
	}

	private ast_node make_replace(ast_field src) {
		if (INFO.tgt_scalar)
			return make_replace_field(src);
		else
			return make_replace_id(src);
	}

	@Override
	public final boolean apply(ast_expr e) {
		if (e.is_id() && (INFO.src_scalar)) {
			ast_id i = e.get_id();
			if (i.getSymInfo() == INFO.src) {
				if (INFO.tgt_scalar) {
					e.set_id(make_replace_id(i));
				} else {
					e.set_id(null);
					e.set_field(make_replace_field(i));
				}
				changed = true;
				if (i != null)
					i.dispose();
			}
		} else if (e.is_field() && (!INFO.src_scalar)) {
			ast_field f = e.get_field();
			if ((f.get_first().getSymInfo() == INFO.s_drv) && (f.get_second().getSymInfo() == INFO.src)) {
				if (INFO.tgt_scalar) {
					e.set_field(null);
					e.set_id(make_replace_id(f));
				} else {
					e.set_field(make_replace_field(f));
				}
				changed = true;
				if (f != null)
					f.dispose();
			}
		}
		return true;
	}

	// LHS changing
	@Override
	public final boolean apply(ast_sent s) {
		if (s.get_nodetype() == ast_node_type.AST_ASSIGN) {
			ast_assign a = (ast_assign) s;
			if (a.is_target_scalar() && INFO.src_scalar) {
				if (a.get_lhs_scala().getSymInfo() == INFO.src) {
					ast_id old_id = a.get_lhs_scala();
					if (INFO.tgt_scalar) {
						a.set_lhs_scala(make_replace_id(old_id));
					} else {
						a.set_lhs_scala(null);
						a.set_lhs_field(make_replace_field(old_id));
					}

					changed = true;
					if (old_id != null)
						old_id.dispose();
				}
			} else if (!a.is_target_scalar() && !INFO.src_scalar) {
				ast_field old_f = a.get_lhs_field();
				if ((old_f.get_first().getSymInfo() == INFO.s_drv) && (old_f.get_second().getSymInfo() == INFO.src)) {
					if (INFO.tgt_scalar) {
						a.set_lhs_field(null);
						a.set_lhs_scala(make_replace_id(old_f));
					} else {
						a.set_lhs_field(make_replace_field(old_f));
					}
					changed = true;
					if (old_f != null)
						old_f.dispose();
				}
			}

			// lhs list
			if (a.has_lhs_list()) {
				LinkedList<ast_node> lhs_list = a.get_lhs_list();
				Iterator<ast_node> I;
				LinkedList<ast_node> to_be_removed = new LinkedList<ast_node>();
				int i = 0;
				for (I = lhs_list.iterator(); I.hasNext(); i++) {
					ast_node n = I.next();
					if (n.get_nodetype() == ast_node_type.AST_ID) {
						ast_id id = (ast_id) n;
						if (id.getSymInfo() == INFO.src) {

							// insert new lhs in front of this one
							ast_node new_target = make_replace(id);
							lhs_list.add(i, new_target);
							to_be_removed.addLast(n);
						}
					} else if (n.get_nodetype() == ast_node_type.AST_FIELD) {
						ast_field f = (ast_field) n;
						if ((f.get_first().getSymInfo() == INFO.s_drv) && (f.get_second().getSymInfo() == INFO.src)) {
							// insert new lhs in front of this one
							ast_node new_target = make_replace(f);
							lhs_list.add(i, new_target);
							to_be_removed.addLast(n);
						}
					} else {
						assert false;
					}
				}
				to_be_removed.clear();
			}

		}
		return true;

		// TODO: procedure call
	}

	public final boolean is_changed() {
		return changed;
	}

}