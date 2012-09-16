package backend_gps;

import static backend_gps.GPSConstants.GPS_FLAG_COMM_DEF_ASSIGN;
import static backend_gps.GPSConstants.GPS_FLAG_COMM_SYMBOL;
import static backend_gps.GPSConstants.GPS_FLAG_EDGE_DEFINED_INNER;
import static backend_gps.GPSConstants.GPS_FLAG_IS_INNER_LOOP;
import static backend_gps.GPSConstants.GPS_FLAG_SENT_BLOCK_FOR_RANDOM_WRITE_ASSIGN;
import static backend_gps.GPSConstants.GPS_INT_EXPR_SCOPE;
import frontend.gm_symtab_entry;
import inc.gm_reduce;
import inc.gps_apply_bb_ast;
import ast.ast_node_type;
import ast.ast_assign;
import ast.ast_expr;
import ast.ast_extra_info;
import ast.ast_foreach;
import ast.ast_id;
import ast.ast_sent;
import ast.ast_sentblock;

//---------------------------------------------------------------------
// Traverse AST per each BB
//  - figure out how each symbol is used.
//     (as LHS, as RHS, as REDUCE targe)
//     (in MASTER, in RECEIVER, in VERTEX)
//---------------------------------------------------------------------
public class gps_merge_symbol_usage_t extends gps_apply_bb_ast {
	
	private static final boolean IS_SCALAR = true;
	private static final boolean IS_FIELD = false;
	
	protected int foreach_depth = 0;

	protected ast_foreach in_loop = null;
	protected gm_gps_beinfo beinfo;

	protected boolean is_random_write_target = false;
	protected boolean is_edge_prop_write_target = false;
	protected boolean is_message_write_target  = false;
	protected gm_symtab_entry random_write_target  = null;
	protected ast_sentblock random_write_target_sb  = null;

	public gps_merge_symbol_usage_t(gm_gps_beinfo i) {
		set_for_sent(true);
		set_for_expr(true);
		set_separate_post_apply(true);
		beinfo = i;
	}

	@Override
	public boolean apply(ast_sent s) {
		is_random_write_target = false;
		is_message_write_target = false;

		if (s.get_nodetype() == ast_node_type.AST_ASSIGN) {
			// GPS_CONTEXT_ (MASTER, VERTEX, RECEIVER)
			gm_gps_symbol_usage_location context = get_current_context();

			ast_assign a = (ast_assign) s;

			random_write_target_sb = (ast_sentblock) s.find_info_ptr(GPS_FLAG_SENT_BLOCK_FOR_RANDOM_WRITE_ASSIGN);

			// check if random write
			is_random_write_target = (random_write_target_sb != null);
			if (is_random_write_target) {
				assert !a.is_target_scalar();
				random_write_target = a.get_lhs_field().get_first().getSymInfo();
			}

			is_message_write_target = s.find_info_bool(GPS_FLAG_COMM_DEF_ASSIGN);

			/*
			 * what? if (foreach_depth > 1) { if (context == GPS_CONTEXT_MASTER)
			 * // inner loop return true; }
			 */
			ast_id target = (a.is_target_scalar()) ? a.get_lhs_scala() : a.get_lhs_field().get_second();
			boolean is_scalar = (a.is_target_scalar()) ? IS_SCALAR : IS_FIELD;
			gm_gps_symbol_usage lhs_reduce = a.is_reduce_assign() ? gm_gps_symbol_usage.GPS_SYM_USED_AS_REDUCE : gm_gps_symbol_usage.GPS_SYM_USED_AS_LHS;
			gm_reduce r_type = a.is_reduce_assign() ? a.get_reduce_type() : gm_reduce.GMREDUCE_NULL;

			if (is_scalar == false && a.get_lhs_field().get_first().getSymInfo().find_info_bool(GPS_FLAG_EDGE_DEFINED_INNER))
				is_edge_prop_write_target = true;

			if (context == gm_gps_symbol_usage_location.GPS_CONTEXT_RECEIVER) {
				if (is_message_write_target || is_edge_prop_write_target || is_random_write_target)
					return true;
			}

			// save lhs usage
			if (is_message_write_target) {
				// lhs is communication symbol
				beinfo.add_communication_symbol_nested(in_loop, target.getSymInfo());
				target.getSymInfo().add_info_bool(GPS_FLAG_COMM_SYMBOL, true);
			}

			update_access_information(target, is_scalar, lhs_reduce, context, r_type);
		}

		else if (s.get_nodetype() == ast_node_type.AST_FOREACH) {
			ast_foreach fe = (ast_foreach) s;
			if (fe.find_info_bool(GPS_FLAG_IS_INNER_LOOP))
				in_loop = fe;
		}
		return true;
	}

	@Override
	public boolean apply2(ast_sent s) {
		is_edge_prop_write_target = false;
		is_random_write_target = false;
		is_message_write_target = false;
		return true;
	}

	@Override
	public boolean apply(ast_expr e) {
		if (!e.is_id() && !e.is_field())
			return true;

		// int syntax_scope =
		// get_current_sent()->find_info_int(GPS_INT_SYNTAX_CONTEXT);
		int expr_scope = e.find_info_int(GPS_INT_EXPR_SCOPE);
		gm_gps_symbol_usage_location context = get_current_context();
		gm_gps_symbol_usage used_type = gm_gps_symbol_usage.GPS_SYM_USED_AS_RHS;
		boolean is_id = e.is_id();
		boolean sc_type = is_id ? IS_SCALAR : IS_FIELD;
		ast_id tg = is_id ? e.get_id() : e.get_field().get_second();

		boolean comm_symbol = false; // is this symbol used in communication?

		if (context == gm_gps_symbol_usage_location.GPS_CONTEXT_RECEIVER) {
			// sender context only
			if (is_message_write_target || is_edge_prop_write_target || is_random_write_target)
				return true;
		}

		if (is_random_write_target) {
			if ((expr_scope != gm_gps_new_scope_analysis.GPS_NEW_SCOPE_RANDOM.getValue())
					&& (expr_scope != gm_gps_new_scope_analysis.GPS_NEW_SCOPE_GLOBAL.getValue()))
				comm_symbol = true;
			/*
			 * if (is_id) { gps_syminfo* syminfo = gps_get_global_syminfo(tg);
			 * if ((syminfo!=NULL) && (syminfo->is_scoped_global())) comm_symbol
			 * = false; else if (tg->getSymInfo() == random_write_target) //
			 * need this? comm_symbol = false; else comm_symbol = true; } else {
			 * if (drv != random_write_target) { comm_symbol = true; } }
			 */
		}

		update_access_information(tg, sc_type, used_type, context);

		if (comm_symbol) {
			if (is_random_write_target) {
				beinfo.add_communication_symbol_random_write(random_write_target_sb, random_write_target, tg.getSymInfo());
			} else {
				beinfo.add_communication_symbol_nested(in_loop, tg.getSymInfo());
			}
			tg.getSymInfo().add_info_bool(GPS_FLAG_COMM_SYMBOL, true);
		}
		return true;
	}

	protected final gm_gps_symbol_usage_location get_current_context() {
		gm_gps_symbol_usage_location context;
		if (!get_curr_BB().is_vertex()) {
			// master context
			context = gm_gps_symbol_usage_location.GPS_CONTEXT_MASTER;
		} else {
			// sender/recevier
			if (is_under_receiver_traverse())
				context = gm_gps_symbol_usage_location.GPS_CONTEXT_RECEIVER;
			else
				context = gm_gps_symbol_usage_location.GPS_CONTEXT_VERTEX;
		}

		return context;
	}

	protected final void update_access_information(ast_id i, boolean is_scalar, gm_gps_symbol_usage usage, gm_gps_symbol_usage_location context) {
		update_access_information(i, is_scalar, usage, context, gm_reduce.GMREDUCE_NULL);
	}

	protected final void update_access_information(ast_id i, boolean is_scalar, gm_gps_symbol_usage usage, gm_gps_symbol_usage_location context,
			gm_reduce r_type) {
		// update global information
		gps_syminfo syminfo = get_or_create_global_syminfo(i, is_scalar);

		// update global information
		syminfo.add_usage_in_BB(get_curr_BB().get_id(), usage, context, r_type);

		// update local information
		syminfo = get_or_create_local_syminfo(i, is_scalar);

		syminfo.add_usage_in_BB(get_curr_BB().get_id(), usage, context, r_type);

		/*
		 * printf("Add usage : %s for BB : %d, context: %s\n", i->get_genname(),
		 * get_curr_BB()->get_id(), (context == GPS_CONTEXT_MASTER) ? "master" :
		 * (context == GPS_CONTEXT_RECEIVER) ? "receiver" : "vertex" );
		 */
	}

	protected final gps_syminfo get_or_create_global_syminfo(ast_id i, boolean is_scalar) {
		gm_symtab_entry sym = i.getSymInfo();

		ast_extra_info info = sym.find_info(GPS_TAG_BB_USAGE);
		gps_syminfo syminfo;
		if (info == null) {
			syminfo = new gps_syminfo(is_scalar);
			sym.add_info(GPS_TAG_BB_USAGE, syminfo);
		} else {
			syminfo = (gps_syminfo) info;
		}
		return syminfo;
	}

	protected final gps_syminfo get_or_create_local_syminfo(ast_id i, boolean is_scalar) {
		gm_symtab_entry sym = i.getSymInfo();

		// find info from BB-local map
		gps_syminfo syminfo = get_curr_BB().find_symbol_info(sym);
		if (syminfo == null) {
			syminfo = new gps_syminfo(is_scalar);
			get_curr_BB().add_symbol_info(sym, syminfo);
		}
		return syminfo;
	}

}