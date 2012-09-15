package backend_gps;

import static backend_gps.GPSConstants.GPS_FLAG_COMM_DEF_ASSIGN;
import static backend_gps.GPSConstants.GPS_FLAG_EDGE_DEFINING_WRITE;
import static backend_gps.GPSConstants.GPS_FLAG_IS_INNER_LOOP;
import static backend_gps.GPSConstants.GPS_INT_EXPR_SCOPE;
import static backend_gps.GPSConstants.GPS_INT_SYNTAX_CONTEXT;
import inc.gm_type;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import ast.ast_node_type;
import ast.ast_assign;
import ast.ast_expr;
import ast.ast_field;
import ast.ast_foreach;
import ast.ast_id;
import ast.ast_sent;
import ast.ast_sentblock;

import common.gm_add_symbol;
import common.gm_apply;
import common.gm_main;
import common.gm_transform_helper;

import frontend.gm_symtab_entry;

public class gps_rewrite_rhs_t extends gm_apply {
	
	public gps_rewrite_rhs_t() {
		set_for_expr(true);
		set_for_sent(true);
		current_fe = null;
	}

	public final boolean apply(ast_sent s) {
		if (s.get_nodetype() == ast_node_type.AST_FOREACH) {
			if (s.find_info_bool(GPS_FLAG_IS_INNER_LOOP)) {
				current_fe = (ast_foreach) s;
				HashSet<ast_expr> empty = new HashSet<ast_expr>();
				sub_exprs.put(current_fe, empty); // initialization by copying
			}
		}

		return true;
	}

	public final boolean apply(ast_expr e) {
		ast_sent s = get_current_sent();
		if (s.find_info_int(GPS_INT_SYNTAX_CONTEXT) != gm_gps_new_scope_analysis.GPS_NEW_SCOPE_IN.getValue())
			return true;

		if ((e.find_info_int(GPS_INT_EXPR_SCOPE) == gm_gps_new_scope_analysis.GPS_NEW_SCOPE_OUT.getValue())
				|| (e.find_info_int(GPS_INT_EXPR_SCOPE) == gm_gps_new_scope_analysis.GPS_NEW_SCOPE_EDGE.getValue())) {
			// (current traversal engine does not support pruning, so should
			// look at parents
			//
			if (!parent_already_added(e)) {
				sub_exprs.get(current_fe).add(e);
			}
		}

		return true;
	}

	public final void process() {
		for (ast_foreach key : sub_exprs.keySet()) {
			if (sub_exprs.get(key).size() > 0)
				process_foreach(key, sub_exprs.get(key));
		}
	}

	public final void process_foreach(ast_foreach fe, HashSet<ast_expr> exprs) {
		HashMap<gm_symtab_entry, gm_symtab_entry> props_vars = new HashMap<gm_symtab_entry, gm_symtab_entry>();
		HashMap<ast_expr, gm_symtab_entry> expr_vars = new HashMap<ast_expr, gm_symtab_entry>();

		assert fe.find_info_bool(GPS_FLAG_IS_INNER_LOOP);

		ast_sentblock sb = (ast_sentblock) (fe.get_body());
		// printf("(2)fe = %p, sb = %p\n", fe, sb);
		assert sb.get_nodetype() == ast_node_type.AST_SENTBLOCK;

		gm_symtab_entry out_iter = null;

		// process raw property access first
		for (ast_expr e : exprs) {
			if (e.is_field()) {
				gm_symtab_entry p = e.get_field().get_second().getSymInfo();
				if (!props_vars.containsKey(p)) {
					gm_symtab_entry target = define_temp(p.getType().getTargetTypeSummary(), sb, p.getType().get_target_graph_sym());

					props_vars.put(p, target);
					if (out_iter == null)
						out_iter = e.get_field().get_first().getSymInfo();
					else {
						assert out_iter == e.get_field().get_first().getSymInfo();
					}
				}
			}
		}

		// process complicated sub expressions
		for (ast_expr e : exprs) {
			if (e.is_field())
				continue;

			// for future optimization
			if (is_composed_of(e, props_vars)) {
				assert false;
			} else {
				gm_symtab_entry target;
				if (e.get_type_summary().is_node_edge_compatible_type()) {
					assert e.is_id();
					target = define_temp(e.get_type_summary(), sb, e.get_id().getTypeInfo().get_target_graph_sym());
				} else {
					target = define_temp(e.get_type_summary(), sb, null);
				}
				expr_vars.put(e, target);
			}
		}

		// replace accesses
		for (ast_expr e : exprs) {
			// if not is_composed_of ...
			if (e.is_field())
				replace_access_expr(e, props_vars.get(e.get_field().get_second().getSymInfo()), true);
			else
				replace_access_expr(e, expr_vars.get(e), false);
		}

		// create definitions
		for (ast_expr rhs : expr_vars.keySet()) {
			gm_symtab_entry target = expr_vars.get(rhs);
			ast_id lhs_id = target.getId().copy(true);
			ast_assign r_assign = ast_assign.new_assign_scala(lhs_id, rhs);

			r_assign.add_info_bool(GPS_FLAG_COMM_DEF_ASSIGN, true);

			gm_transform_helper.gm_insert_sent_begin_of_sb(sb, r_assign);
		}

		for (gm_symtab_entry prop : props_vars.keySet()) {
			gm_symtab_entry target = props_vars.get(prop);

			ast_id lhs_id = target.getId().copy(true);
			ast_id driver = out_iter.getId().copy(true);
			ast_id prop_id = prop.getId().copy(true);
			ast_field f = ast_field.new_field(driver, prop_id);
			ast_expr rhs = ast_expr.new_field_expr(f);
			ast_assign r_assign = ast_assign.new_assign_scala(lhs_id, rhs);

			r_assign.add_info_bool(GPS_FLAG_COMM_DEF_ASSIGN, true);

			gm_transform_helper.gm_insert_sent_begin_of_sb(sb, r_assign);
		}

		// move edge-defining writes at the top
		LinkedList<ast_sent> sents_to_move = new LinkedList<ast_sent>();
		for (ast_sent s : sb.get_sents()) {
			if (s.find_info_bool(GPS_FLAG_EDGE_DEFINING_WRITE)) {
				sents_to_move.addLast(s);
			}
		}
		for (ast_sent s : sb.get_sents()) {
			gm_transform_helper.gm_ripoff_sent(s);
			gm_transform_helper.gm_insert_sent_begin_of_sb(sb, s);
		}

	}

	private HashMap<ast_foreach, HashSet<ast_expr>> sub_exprs = new HashMap<ast_foreach, HashSet<ast_expr>>();
	private ast_foreach current_fe;

	private gm_symtab_entry define_temp(gm_type type, ast_sentblock sb, gm_symtab_entry graph) {
		String temp_name = gm_main.FE.voca_temp_name_and_add("_m");
		gm_symtab_entry target;
		if (type.is_prim_type()) {
			target = gm_add_symbol.gm_add_new_symbol_primtype(sb, type, temp_name);
		} else if (type.is_node_edge_compatible_type()) {
			if (type.is_node_compatible_type()) {
				type = gm_type.GMTYPE_NODE;
			} else if (type.is_edge_compatible_type()) {
				type = gm_type.GMTYPE_EDGE;
			}
			target = gm_add_symbol.gm_add_new_symbol_nodeedge_type(sb, type, graph, temp_name);
		} else {
			assert false;
			throw new AssertionError();
		}
		temp_name = null;

		return target;
	}

	private boolean parent_already_added(ast_expr e) {
		e = e.get_up_op();
		while (e != null) {
			if (sub_exprs.get(current_fe).contains(e)) {
				return true;
			}
			e = e.get_up_op();
		}
		return false;
	}

	public static boolean is_composed_of(ast_expr e, HashMap<gm_symtab_entry, gm_symtab_entry> SYMS) {
		return false;
	}

	public static void replace_access_expr(ast_expr org, gm_symtab_entry target, boolean destroy) {
		gm_replace_simple_props_t T = new gm_replace_simple_props_t(org, target, destroy);
		assert org.get_parent() != null;
		gm_transform_helper.gm_replace_expr_general(org.get_parent(), T);
	}
}