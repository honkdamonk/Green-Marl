package backend_gps;

import static backend_gps.GPSConstants.GPS_FLAG_USE_IN_DEGREE;
import static backend_gps.GPSConstants.GPS_FLAG_USE_REVERSE_EDGE;
import static backend_gps.GPSConstants.GPS_NAME_IN_DEGREE_PROP;
import frontend.GlobalMembersGm_rw_analysis;
import frontend.gm_symtab_entry;
import inc.GMTYPE_T;
import inc.GM_REDUCE_T;
import inc.gm_assignment_t;
import inc.gm_compile_step;
import ast.ast_assign;
import ast.ast_expr;
import ast.ast_field;
import ast.ast_foreach;
import ast.ast_id;
import ast.ast_procdef;
import ast.ast_sentblock;

import common.GlobalMembersGm_add_symbol;
import common.GlobalMembersGm_main;
import common.GlobalMembersGm_new_sents_after_tc;
import common.GlobalMembersGm_transform_helper;

//-------------------------------------------
// [Step 1]
// Add delaration here
// declaration of optimization steps
//-------------------------------------------
public class gm_gps_opt_check_reverse_edges extends gm_compile_step {
	
	private gm_gps_opt_check_reverse_edges() {
		set_description("Check use of reverse edges");
	}

	public void process(ast_procdef p) {
		gps_check_reverse_edge_t T = new gps_check_reverse_edge_t();
		p.traverse_pre(T);
		if (T.use_rev_edge()) {
			GlobalMembersGm_main.FE.get_proc_info(p).add_info_bool(GPS_FLAG_USE_REVERSE_EDGE, true);
			// a special basic block will be added in create ebb state.
		} else if (T.use_in_degree()) {
			GlobalMembersGm_main.FE.get_proc_info(p).add_info_bool(GPS_FLAG_USE_IN_DEGREE, true);

			// define a new node_property for in_degree counting
			String tmp_name = GlobalMembersGm_main.FE.voca_temp_name_and_add("_in_degree", null, true);
			GlobalMembersGm_main.FE.get_proc_info(p).add_info_string(GPS_NAME_IN_DEGREE_PROP, tmp_name);

			// create a temporary node property
			ast_sentblock sb = p.get_body();
			tangible.RefObject<String> tempRef_tmp_name = new tangible.RefObject<String>(tmp_name);
			gm_symtab_entry new_prop = GlobalMembersGm_add_symbol.gm_add_new_symbol_property(sb, GMTYPE_T.GMTYPE_INT, true, T.get_target_graph(),
					tempRef_tmp_name);
			tmp_name = tempRef_tmp_name.argvalue;

			// create a routine that initializes reverse degree
			String tmp_iter = GlobalMembersGm_main.FE.voca_temp_name_and_add("t");
			ast_sentblock sb2 = ast_sentblock.new_sentblock();
			ast_id it2 = ast_id.new_id(tmp_iter, 0, 0);
			ast_id src = T.get_target_graph().getId().copy(true);
			ast_foreach fe = GlobalMembersGm_new_sents_after_tc.gm_new_foreach_after_tc(it2, src, sb2, GMTYPE_T.GMTYPE_NODEITER_ALL);
			ast_expr rhs = ast_expr.new_ival_expr(0);
			ast_field f = ast_field.new_field(fe.get_iterator().copy(true), new_prop.getId().copy(true));
			ast_assign a = ast_assign.new_assign_field(f, rhs);
			GlobalMembersGm_transform_helper.gm_insert_sent_begin_of_sb(sb2, a);

			it2 = it2.copy(false);
			src = src.copy(true);
			sb2 = ast_sentblock.new_sentblock();
			ast_foreach fe2 = GlobalMembersGm_new_sents_after_tc.gm_new_foreach_after_tc(it2, src, sb2, GMTYPE_T.GMTYPE_NODEITER_ALL);

			String tmp_iter2 = GlobalMembersGm_main.FE.voca_temp_name_and_add("u");
			ast_sentblock sb3 = ast_sentblock.new_sentblock();
			ast_id it3 = ast_id.new_id(tmp_iter2, 0, 0);
			src = fe2.get_iterator().copy(true);
			ast_foreach fe3 = GlobalMembersGm_new_sents_after_tc.gm_new_foreach_after_tc(it3, src, sb3, GMTYPE_T.GMTYPE_NODEITER_NBRS);
			GlobalMembersGm_transform_helper.gm_insert_sent_begin_of_sb(sb2, fe3);

			rhs = ast_expr.new_ival_expr(1);
			f = ast_field.new_field(fe3.get_iterator().copy(true), new_prop.getId().copy(true));
			a = ast_assign.new_assign_field(f, rhs, gm_assignment_t.GMASSIGN_REDUCE, fe2.get_iterator().copy(true), GM_REDUCE_T.GMREDUCE_PLUS);
			GlobalMembersGm_transform_helper.gm_insert_sent_begin_of_sb(sb3, a);

			GlobalMembersGm_transform_helper.gm_insert_sent_begin_of_sb(p.get_body(), fe2);
			GlobalMembersGm_transform_helper.gm_insert_sent_begin_of_sb(p.get_body(), fe);

			// Rename every InDegree() access with
			// access to the new property
			replace_in_degree_t T_2 = new replace_in_degree_t();
			T_2.set_new_prop(new_prop);
			GlobalMembersGm_transform_helper.gm_replace_expr_general(p.get_body(), T_2);

			// re-do RW analysis
			GlobalMembersGm_rw_analysis.gm_redo_rw_analysis(p.get_body());
		}
	}

	@Override
	public gm_compile_step get_instance() {
		return new gm_gps_opt_check_reverse_edges();
	}

	public static gm_compile_step get_factory() {
		return new gm_gps_opt_check_reverse_edges();
	}
}