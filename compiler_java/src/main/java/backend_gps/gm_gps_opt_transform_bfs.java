package backend_gps;

import static backend_gps.GPSConstants.GPS_FLAG_HAS_DOWN_NBRS;
import frontend.gm_rw_analysis;
import frontend.gm_symtab_entry;
import inc.gm_type;
import inc.gm_ops;
import inc.gm_reduce;
import inc.gm_assignment;
import inc.gm_compile_step;
import tangible.RefObject;
import ast.ast_assign;
import ast.ast_bfs;
import ast.ast_expr;
import ast.ast_field;
import ast.ast_foreach;
import ast.ast_id;
import ast.ast_if;
import ast.ast_procdef;
import ast.ast_sent;
import ast.ast_sentblock;
import ast.ast_while;

import common.gm_add_symbol;
import common.gm_flat_nested_sentblock;
import common.gm_main;
import common.gm_new_sents_after_tc;
import common.gm_resolve_nc;
import common.gm_transform_helper;

//-------------------------------------------
// [Step 1]
// Add delaration here
// declaration of optimization steps
//-------------------------------------------

//--------------------------------------------------------
// Transform BFS ==> Pregel-Canonical Statments. (i.e. while + foreach)
//
//  By doing this transformation, we may lose an opportunity to use 
//  a special fast implmentation of BFS.
//  However, we can re-use GPS tranlsation mechanism for while/foreach statments.
//  without creating new translation rules.
//----------------------------------------------------

public class gm_gps_opt_transform_bfs extends gm_compile_step {

	private gm_gps_opt_transform_bfs() {
		set_description("Transform BFS into while and foreach");
	}

	public void process(ast_procdef p) {

		gps_opt_find_bfs_t T = new gps_opt_find_bfs_t();
		p.traverse_both(T);
		for (ast_bfs b : T.get_targets()) {
			gm_gps_rewrite_bfs(b);
		}

		gm_flat_nested_sentblock.gm_flat_nested_sentblock(p);

		gm_rw_analysis.gm_redo_rw_analysis(p.get_body());
	}

	@Override
	public gm_compile_step get_instance() {
		return new gm_gps_opt_transform_bfs();
	}

	public static gm_compile_step get_factory() {
		return new gm_gps_opt_transform_bfs();
	}

	private static void create_initializer(ast_sentblock sb, ast_bfs bfs, gm_symtab_entry lev_sym, gm_symtab_entry curr_sym, gm_symtab_entry fin_sym) {

		// ---------------------------------------------
		// curr_level = -1;
		// bfs_finished = False;
		// Foreach(i:G.Nodes)
		// i.level = ( i == root) ? 0 : +INF;
		// ---------------------------------------------
		ast_id lhs_curr = curr_sym.getId().copy(true);
		ast_expr rhs_curr = ast_expr.new_ival_expr(-1);
		ast_assign a_curr = ast_assign.new_assign_scala(lhs_curr, rhs_curr);

		ast_id lhs_fin = fin_sym.getId().copy(true);
		ast_expr rhs_fin = ast_expr.new_bval_expr(false);
		ast_assign a_fin = ast_assign.new_assign_scala(lhs_fin, rhs_fin);

		ast_sentblock inner_sb = ast_sentblock.new_sentblock();
		String i_name = gm_main.FE.voca_temp_name_and_add("i", null, true);
		ast_id it = ast_id.new_id(i_name, bfs.get_iterator().get_line(), bfs.get_iterator().get_col());
		ast_foreach fe = gm_new_sents_after_tc.gm_new_foreach_after_tc(it, bfs.get_source().copy(true), inner_sb, gm_type.GMTYPE_NODEITER_ALL);

		sb.add_sent(fe);
		sb.add_sent(a_curr);
		sb.add_sent(a_fin);

		ast_expr rhs_inf = ast_expr.new_inf_expr(true);
		rhs_inf.set_type_summary(gm_type.GMTYPE_INT);
		ast_expr rhs_zero = ast_expr.new_ival_expr(0);
		ast_expr rhs_i = ast_expr.new_id_expr(fe.get_iterator().copy(true));
		ast_expr rhs_root = ast_expr.new_id_expr(bfs.get_root().copy(true));
		ast_expr rhs_eq = ast_expr.new_comp_expr(gm_ops.GMOP_EQ, rhs_i, rhs_root);
		ast_expr rhs_tri = ast_expr.new_ternary_expr(rhs_eq, rhs_zero, rhs_inf);
		ast_field lhs_lev = ast_field.new_field(fe.get_iterator().copy(true), lev_sym.getId().copy(true));
		ast_assign init_a = ast_assign.new_assign_field(lhs_lev, rhs_tri);
		inner_sb.add_sent(init_a);
	}

	private static ast_sentblock create_fw_body_prepare(ast_sentblock while_sb, ast_bfs bfs, gm_symtab_entry lev_sym, gm_symtab_entry curr_sym, RefObject<ast_foreach> out) {
		// outer loop
		ast_sentblock foreach_sb = ast_sentblock.new_sentblock();
		ast_foreach foreach_out = gm_new_sents_after_tc.gm_new_foreach_after_tc(bfs.get_iterator().copy(false), bfs.get_source().copy(true),
				foreach_sb, gm_type.GMTYPE_NODEITER_ALL);
		while_sb.add_sent(foreach_out);

		// outer if
		ast_expr lev_check_out_c = ast_expr.new_comp_expr(gm_ops.GMOP_EQ,
				ast_expr.new_field_expr(ast_field.new_field(foreach_out.get_iterator().copy(true), lev_sym.getId().copy(true))),
				ast_expr.new_id_expr(curr_sym.getId().copy(true)));
		ast_sentblock lev_check_out_sb = ast_sentblock.new_sentblock();
		ast_if lev_check_out_if = ast_if.new_if(lev_check_out_c, lev_check_out_sb, null);
		foreach_sb.add_sent(lev_check_out_if);

		out.argvalue = foreach_out;
		return lev_check_out_sb;

	}

	private static void create_user_body_main(ast_sentblock sb_to_add, ast_bfs bfs, ast_foreach out_loop, gm_symtab_entry lev_sym, gm_symtab_entry curr_sym,
			boolean is_fw) {

		// rip-off body from bfs
		ast_sent body = (is_fw) ? bfs.get_fbody() : bfs.get_bbody();
		gm_transform_helper.gm_ripoff_sent(body);

		// replace iterator
		gm_resolve_nc.gm_replace_symbol_entry(bfs.get_iterator().getSymInfo(), out_loop.get_iterator().getSymInfo(), body);
		// what was iterator 2 again?
		if (bfs.get_iterator2() != null)
			gm_resolve_nc.gm_replace_symbol_entry(bfs.get_iterator2().getSymInfo(), out_loop.get_iterator().getSymInfo(), body);

		// replace up/down nbr
		gps_opt_find_updown_foreach_t T = new gps_opt_find_updown_foreach_t(curr_sym, lev_sym);
		body.traverse_pre(T);
		T.post_process();

		sb_to_add.add_sent(body);
	}

	private static void create_fw_iteration(ast_sentblock sb, ast_bfs bfs, gm_symtab_entry lev_sym, gm_symtab_entry curr_sym, gm_symtab_entry fin_sym) {
		// While(bfs_finished != false) {
		// bfs_finished = True;
		// curr_level ++;
		//
		// Foreach(v:G.Nodes) {
		// if (v.level == curr_level) {
		// Foreach(k:v.Nbrs) {
		// If (k.level == +INF && [navigator]) {
		// k.level = curr_level + 1;
		// bfs_finished &= False;
		// }
		// }
		//
		// // if (downnbr is not used)
		// << body 1 >>
		// }
		// }
		//
		// // (only if downnbr is used)
		// Foreach(v:G.Nodes) {
		// if (v.level == curr_level) {
		// << body 2 >>
		// }
		// }
		//
		// }

		// while
		ast_sentblock while_sb = ast_sentblock.new_sentblock();
		ast_expr check_l = ast_expr.new_id_expr(fin_sym.getId().copy(true));
		ast_expr check_r = ast_expr.new_bval_expr(true);
		ast_expr check_op = ast_expr.new_comp_expr(gm_ops.GMOP_NEQ, check_l, check_r);
		ast_sent fw_while = ast_while.new_while(check_op, while_sb);
		sb.add_sent(fw_while);

		// assign not finished
		ast_expr true_rhs = ast_expr.new_bval_expr(true);
		ast_assign true_a = ast_assign.new_assign_scala(fin_sym.getId().copy(true), true_rhs);
		while_sb.add_sent(true_a);

		// increase level
		ast_expr inc_rhs = ast_expr.new_biop_expr(gm_ops.GMOP_ADD, ast_expr.new_id_expr(curr_sym.getId().copy(true)), ast_expr.new_ival_expr(1));
		ast_assign inc_a = ast_assign.new_assign_scala(curr_sym.getId().copy(true), inc_rhs);
		while_sb.add_sent(inc_a);

		// outer loop
		ast_sentblock foreach_sb = ast_sentblock.new_sentblock();
		ast_foreach foreach_out = gm_new_sents_after_tc.gm_new_foreach_after_tc(bfs.get_iterator().copy(false), bfs.get_source().copy(true),
				foreach_sb, gm_type.GMTYPE_NODEITER_ALL);
		while_sb.add_sent(foreach_out);

		ast_expr lev_check_out_c = ast_expr.new_comp_expr(gm_ops.GMOP_EQ,
				ast_expr.new_field_expr(ast_field.new_field(foreach_out.get_iterator().copy(true), lev_sym.getId().copy(true))),
				ast_expr.new_id_expr(curr_sym.getId().copy(true)));
		ast_sentblock lev_check_out_sb = ast_sentblock.new_sentblock();
		ast_if lev_check_out_if = ast_if.new_if(lev_check_out_c, lev_check_out_sb, null);
		foreach_sb.add_sent(lev_check_out_if);

		// inner loop
		ast_sentblock inner_sb = ast_sentblock.new_sentblock();
		String inner_name = gm_main.FE.voca_temp_name_and_add("_t", null, true);
		ast_id inner_id = ast_id.new_id(inner_name, 0, 0);
		ast_foreach foreach_in = gm_new_sents_after_tc.gm_new_foreach_after_tc(inner_id, foreach_out.get_iterator().copy(true), inner_sb,
				gm_type.GMTYPE_NODEITER_NBRS);
		lev_check_out_sb.add_sent(foreach_in);

		// inner level_check
		ast_expr inf = ast_expr.new_inf_expr(true);
		inf.set_type_summary(gm_type.GMTYPE_INT);
		ast_expr lev_check_in_c = ast_expr.new_comp_expr(gm_ops.GMOP_EQ,
				ast_expr.new_field_expr(ast_field.new_field(foreach_in.get_iterator().copy(true), lev_sym.getId().copy(true))), inf);
		ast_sentblock lev_check_in_sb = ast_sentblock.new_sentblock();
		ast_if lev_check_in_if;
		if (bfs.get_navigator() != null) {
			ast_expr navi = bfs.get_navigator();
			bfs.set_navigator(null);

			// replace bfs symbol ==> an inner foreach symbol
			gm_resolve_nc.gm_replace_symbol_entry(bfs.get_iterator().getSymInfo(), foreach_in.get_iterator().getSymInfo(), navi);

			// check with navigator
			ast_expr new_top = ast_expr.new_lbiop_expr(gm_ops.GMOP_AND, lev_check_in_c, navi);
			lev_check_in_if = ast_if.new_if(new_top, lev_check_in_sb, null);
		} else {
			lev_check_in_if = ast_if.new_if(lev_check_in_c, lev_check_in_sb, null);
		}
		inner_sb.add_sent(lev_check_in_if);

		// increase level
		ast_expr inc_lev_rhs = ast_expr.new_biop_expr(gm_ops.GMOP_ADD, ast_expr.new_id_expr(curr_sym.getId().copy(true)), ast_expr.new_ival_expr(1));
		ast_assign inc_level = ast_assign.new_assign_field(ast_field.new_field(foreach_in.get_iterator().copy(true), lev_sym.getId().copy(true)), inc_lev_rhs);
		lev_check_in_sb.add_sent(inc_level);

		// set not finished
		ast_expr update_fin_rhs = ast_expr.new_bval_expr(false);
		ast_assign update_fin = ast_assign.new_assign_scala(fin_sym.getId().copy(true), update_fin_rhs, gm_assignment.GMASSIGN_REDUCE, foreach_out
				.get_iterator().copy(true), gm_reduce.GMREDUCE_AND);
		lev_check_in_sb.add_sent(update_fin);

		// create user body
		if (((ast_sentblock) bfs.get_fbody()).get_sents().size() > 0) {
			ast_sentblock fw_body_to_add;
			if (bfs.find_info_bool(GPS_FLAG_HAS_DOWN_NBRS)) {
				RefObject<ast_foreach> foreach_out_wrapper = new RefObject<ast_foreach>(foreach_out);
				fw_body_to_add = create_fw_body_prepare(while_sb, bfs, lev_sym, curr_sym, foreach_out_wrapper);
				foreach_out = foreach_out_wrapper.argvalue;
			} else {
				fw_body_to_add = lev_check_out_sb;
			}

			create_user_body_main(fw_body_to_add, bfs, foreach_out, lev_sym, curr_sym, true);
		}

	}

	private static void create_bw_iteration(ast_sentblock sb, ast_bfs bfs, gm_symtab_entry lev_sym, gm_symtab_entry curr_sym, gm_symtab_entry fin_sym) {
		//
		// While(curr_level >=0) {
		//
		// Foreach(v:G.Nodes) {
		// if (v.level == curr_level) {
		// << body 1 >>
		// }
		// }
		//
		// curr_level --
		// }

		// while loop
		ast_sentblock while_sb = ast_sentblock.new_sentblock();
		ast_expr check_l = ast_expr.new_id_expr(curr_sym.getId().copy(true));
		ast_expr check_r = ast_expr.new_ival_expr(0);
		ast_expr check_op = ast_expr.new_comp_expr(gm_ops.GMOP_GE, check_l, check_r);
		ast_sent fw_while = ast_while.new_while(check_op, while_sb);
		sb.add_sent(fw_while);

		// outer loop
		ast_sentblock foreach_sb = ast_sentblock.new_sentblock();
		ast_foreach foreach_out = gm_new_sents_after_tc.gm_new_foreach_after_tc(bfs.get_iterator().copy(false), bfs.get_source().copy(true),
				foreach_sb, gm_type.GMTYPE_NODEITER_ALL);
		while_sb.add_sent(foreach_out);

		// level check
		ast_expr lev_check_out_c = ast_expr.new_comp_expr(gm_ops.GMOP_EQ,
				ast_expr.new_field_expr(ast_field.new_field(foreach_out.get_iterator().copy(true), lev_sym.getId().copy(true))),
				ast_expr.new_id_expr(curr_sym.getId().copy(true)));
		ast_sentblock lev_check_out_sb = ast_sentblock.new_sentblock();
		ast_if lev_check_out_if = ast_if.new_if(lev_check_out_c, lev_check_out_sb, null);
		foreach_sb.add_sent(lev_check_out_if);

		create_user_body_main(lev_check_out_sb, bfs, foreach_out, lev_sym, curr_sym, false);

		// decrease curr level
		ast_expr dec_lev_rhs = ast_expr.new_biop_expr(gm_ops.GMOP_SUB, ast_expr.new_id_expr(curr_sym.getId().copy(true)), ast_expr.new_ival_expr(1));
		ast_assign dec_level = ast_assign.new_assign_scala(curr_sym.getId().copy(true), dec_lev_rhs);
		while_sb.add_sent(dec_level);
	}

	private static void gm_gps_rewrite_bfs(ast_bfs b) {
		gm_transform_helper.gm_make_it_belong_to_sentblock(b);

		ast_sentblock sb = ast_sentblock.new_sentblock();
		gm_transform_helper.gm_add_sent_after(b, sb);

		// replace BFS with a sentence block
		// InBFS(v: G.Nodes From root) {
		//
		// }
		// ==>
		// {
		// N_P<Int> level;
		// Int curr_level;
		// Bool bfs_finished;
		//
		// <initializer>
		//
		// <forward iteration>
		//
		// <backward iteration>
		//
		// }
		//
		String lev_name = gm_main.FE.voca_temp_name_and_add("level", null, true);
		String curr_name = gm_main.FE.voca_temp_name_and_add("curr_level", null, true);
		String fin_name = gm_main.FE.voca_temp_name_and_add("bfs_finished", null, true);
		gm_symtab_entry lev_sym = gm_add_symbol.gm_add_new_symbol_property(sb, gm_type.GMTYPE_INT, true, b.get_source().getSymInfo(),
				lev_name);
		gm_symtab_entry curr_sym = gm_add_symbol.gm_add_new_symbol_primtype(sb, gm_type.GMTYPE_INT, curr_name);
		gm_symtab_entry fin_sym = gm_add_symbol.gm_add_new_symbol_primtype(sb, gm_type.GMTYPE_BOOL, fin_name);

		create_initializer(sb, b, lev_sym, curr_sym, fin_sym);

		create_fw_iteration(sb, b, lev_sym, curr_sym, fin_sym);

		if (b.get_bbody() != null)
			create_bw_iteration(sb, b, lev_sym, curr_sym, fin_sym);

		// replace bfs with sb
		gm_transform_helper.gm_ripoff_sent(b);

	}
}