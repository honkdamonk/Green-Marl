package backend_gps;

import frontend.gm_symtab_entry;
import inc.GlobalMembersGm_backend_gps;
import inc.gm_compile_step;

import java.util.HashMap;

import ast.ast_id;
import ast.ast_procdef;
import ast.ast_sent;
import ast.ast_typedecl;

import common.GlobalMembersGm_main;
import common.GlobalMembersGm_traverse;

public class gm_gps_opt_create_ebb extends gm_compile_step {
	
	private gm_gps_opt_create_ebb() {
		set_description("Create ExtendedBasicBlocks");
	}

	public void process(ast_procdef proc) {
		// --------------------------------
		// STEP 1:
		// trasverse AST and mark each sentence
		// --------------------------------
		HashMap<ast_sent, gps_gps_sentence_t> s_mark = new HashMap<ast_sent, gps_gps_sentence_t>();
		gm_stage_create_pre_process_t T1 = new gm_stage_create_pre_process_t(s_mark);
		GlobalMembersGm_traverse.gm_traverse_sents_pre_post(proc, T1);

		// --------------------------------
		// STEP 2:
		// create Basic Blocks
		// --------------------------------
		gm_gps_beinfo beinfo = (gm_gps_beinfo) GlobalMembersGm_main.FE.get_backend_info(proc);
		gm_gps_create_basic_block1_t T2 = new gm_gps_create_basic_block1_t(s_mark, beinfo);
		GlobalMembersGm_traverse.gm_traverse_sents_pre_post(proc, T2);

		// Debug Print
		/*
		 * gps_bb_print_all(T2.get_entry()); // return or of has_changed
		 * exit(0);
		 */

		// --------------------------------
		// STEP 3:
		// merge BASIC BLOCKS
		// --------------------------------
		GlobalMembersGm_gps_bb_merge.gm_gps_merge_basic_blocks(T2.get_entry());

		// ---------------------------
		// STEP 4:
		// ---------------------------
		gm_gps_basic_block top = T2.get_entry();
		if (GlobalMembersGm_main.FE.get_proc_info(proc).find_info_bool(GlobalMembersGm_backend_gps.GPS_FLAG_USE_REVERSE_EDGE)) {
			// create prepareation state
			gm_gps_basic_block t1 = new gm_gps_basic_block(GlobalMembersGm_backend_gps.GPS_PREPARE_STEP1, gm_gps_bbtype_t.GM_GPS_BBTYPE_PREPARE1);
			gm_gps_basic_block t2 = new gm_gps_basic_block(GlobalMembersGm_backend_gps.GPS_PREPARE_STEP2, gm_gps_bbtype_t.GM_GPS_BBTYPE_PREPARE2);
			t1.add_exit(t2);

			t2.add_exit(top);
			top = t1;

			// create dummy communication info
			ast_id dummy = ast_id.new_id(GlobalMembersGm_backend_gps.GPS_DUMMY_ID, 0, 0);
			ast_id dummy2 = ast_id.new_id("dummy_graph", 0, 0);
			ast_typedecl t = ast_typedecl.new_nodetype(dummy2);
			gm_symtab_entry e = new gm_symtab_entry(dummy, t);

			beinfo.add_communication_unit_initializer(); // use NULL as a
															// special symbol
			beinfo.add_communication_symbol_initializer(e);
		}

		beinfo.set_entry_basic_block(top);

	}

	@Override
	public gm_compile_step get_instance() {
		return new gm_gps_opt_create_ebb();
	}

	public static gm_compile_step get_factory() {
		return new gm_gps_opt_create_ebb();
	}
}