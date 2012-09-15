package backend_giraph;

import static backend_gps.GPSConstants.GPS_FLAG_IS_INTRA_MERGED_CONDITIONAL;
import static backend_gps.GPSConstants.GPS_FLAG_SENT_BLOCK_FOR_RANDOM_WRITE_ASSIGN;
import static backend_gps.GPSConstants.GPS_FLAG_USE_EDGE_PROP;
import static backend_gps.GPSConstants.GPS_FLAG_USE_REVERSE_EDGE;
import static backend_gps.GPSConstants.GPS_INTRA_MERGE_IS_FIRST;
import static backend_gps.GPSConstants.GPS_INT_INTRA_MERGED_CONDITIONAL_NO;
import static backend_gps.GPSConstants.GPS_KEY_FOR_STATE;
import static backend_gps.GPSConstants.GPS_LIST_INTRA_MERGED_CONDITIONAL;
import static backend_gps.GPSConstants.GPS_PREPARE_STEP1;
import static backend_gps.GPSConstants.GPS_RET_VALUE;
import static backend_gps.GPSConstants.GPS_REV_NODE_ID;
import static common.gm_errors_and_warnings.GM_ERROR_GPS_PROC_NAME;
import static inc.gps_apply_bb.GPS_TAG_BB_USAGE;
import frontend.gm_symtab;
import frontend.gm_symtab_entry;
import inc.GM_REDUCE_T;
import inc.gm_compile_step;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import ast.ast_node_type;
import ast.ast_foreach;
import ast.ast_if;
import ast.ast_procdef;
import ast.ast_sent;
import ast.ast_sentblock;
import ast.ast_typedecl;
import ast.ast_while;
import backend_gps.gm_gps_basic_block;
import backend_gps.gm_gps_bbtype_t;
import backend_gps.gm_gps_beinfo;
import backend_gps.gm_gps_comm_t;
import backend_gps.gm_gps_comm_unit;
import backend_gps.gm_gps_gen;
import backend_gps.gm_gps_new_check_depth_two;
import backend_gps.gm_gps_new_check_pull_data;
import backend_gps.gm_gps_new_check_random_read;
import backend_gps.gm_gps_new_check_random_write;
import backend_gps.gm_gps_new_rewrite_rhs;
import backend_gps.gm_gps_opt_analyze_symbol_summary;
import backend_gps.gm_gps_opt_analyze_symbol_usage;
import backend_gps.gm_gps_opt_check_edge_value;
import backend_gps.gm_gps_opt_check_reverse_edges;
import backend_gps.gm_gps_opt_create_ebb;
import backend_gps.gm_gps_opt_find_congruent_message;
import backend_gps.gm_gps_opt_find_reachable;
import backend_gps.gm_gps_opt_merge_ebb_again;
import backend_gps.gm_gps_opt_merge_ebb_intra_loop;
import backend_gps.gm_gps_opt_split_comm_ebb;
import backend_gps.gps_syminfo;

import common.gm_apply_compiler_stage;
import common.gm_argopts;
import common.gm_error;
import common.gm_main;
import common.gm_reproduce;

//-----------------------------------------------------------------
// interface for graph library Layer
//-----------------------------------------------------------------
// state number,
// begin sentence
// is parallel
public class gm_giraph_gen extends gm_gps_gen {

	private gm_giraphlib glib; // graph library

	public gm_giraph_gen() {
		super();
		glib = new gm_giraphlib(this);
	}

	@Override
	public gm_giraphlib get_lib() {
		return glib;
	}

	@Override
	public void init_gen_steps() {
		LinkedList<gm_compile_step> L = get_gen_steps();
		// no more change of AST at this point
		L.addLast(gm_gps_opt_check_reverse_edges.get_factory());
		L.addLast(gm_gps_new_check_depth_two.get_factory());
		L.addLast(gm_gps_new_check_pull_data.get_factory());
		L.addLast(gm_gps_new_check_random_read.get_factory());
		L.addLast(gm_gps_new_check_random_write.get_factory());
		L.addLast(gm_gps_opt_check_edge_value.get_factory());
		L.addLast(gm_gps_new_rewrite_rhs.get_factory());

		L.addLast(gm_gps_opt_create_ebb.get_factory());
		L.addLast(gm_gps_opt_split_comm_ebb.get_factory());
		L.addLast(gm_gps_opt_merge_ebb_again.get_factory());
		L.addLast(gm_gps_opt_merge_ebb_intra_loop.get_factory());
		L.addLast(gm_gps_opt_analyze_symbol_usage.get_factory());
		L.addLast(gm_gps_opt_analyze_symbol_summary.get_factory());
		L.addLast(gm_gps_opt_find_reachable.get_factory());
		L.addLast(gm_gps_opt_find_congruent_message.get_factory());

		L.addLast(gm_giraph_gen_class.get_factory());
	}

	// ----------------------------------
	// stages in backend gen
	// ----------------------------------
	@Override
	public boolean do_generate() {

		gm_main.FE.prepare_proc_iteration();
		ast_procdef proc = gm_main.FE.get_next_proc();

		// Check whether procedure name is the same as the filename
		String proc_name = proc.get_procname().get_genname();
		if (!proc_name.equals(fname)) {
			gm_error.gm_backend_error(GM_ERROR_GPS_PROC_NAME, proc.get_procname().get_genname(), fname);
			return false;
		}

		// Append 'Vertex' to filename if we only generate vertex logic
		if (gm_main.OPTIONS.get_arg_bool(gm_argopts.GMARGFLAG_GIRAPH_VERTEX_ONLY)) {
			String filename = String.format("%sVertex", proc.get_procname().get_genname());
			gm_main.PREGEL_BE.setFileName(filename);
		}

		if (!open_output_files())
			return false;

		boolean b = gm_apply_compiler_stage.gm_apply_compiler_stage(get_gen_steps());

		close_output_files();

		return b;
	}

	@Override
	public void write_headers() {
		get_lib().generate_headers(Body);
		Body.NL();
	}

	@Override
	public void begin_class() {
		ast_procdef proc = gm_main.FE.get_current_proc();
		Body.push("public class ");
		Body.push(proc.get_procname().get_genname());
		Body.push(" implements Tool {");
		Body.NL();
		Body.NL();
	}

	@Override
	public void end_class() {
		Body.pushln("}");
	}

	void do_generate_global_variables() {
		ast_procdef proc = gm_main.FE.get_current_proc();
		Body.pushln("// Class logger");
		String temp = String.format("private static final Logger LOG = Logger.getLogger(%s.class);", proc.get_procname().get_genname());
		Body.pushln(temp);
		Body.NL();
		Body.pushln("// Configuration");
		Body.pushln("private Configuration conf;");
		Body.NL();
	}

	@Override
	public void do_generate_master() {
		do_generate_shared_variables_keys();
		set_master_generate(true);
		do_generate_master_class();
		do_generate_master_scalar();
		do_generate_master_states();
		do_generate_master_serialization();
		Body.pushln("}"); // finish master class
		Body.NL();
	}

	@Override
	public void do_generate_master_states() {
		Body.pushln("//----------------------------------------------------------");
		Body.pushln("// Master's State-machine ");
		Body.pushln("//----------------------------------------------------------");
		Body.pushln("private void _master_state_machine() {");
		Body.pushln("_master_should_start_workers = false;");
		Body.pushln("_master_should_finish = false;");
		Body.pushln("do {");
		Body.pushln("_master_state = _master_state_nxt ;");
		Body.pushln("switch(_master_state) {");
		gm_gps_beinfo info = (gm_gps_beinfo) gm_main.FE.get_current_backend_info();

		LinkedList<gm_gps_basic_block> bb_blocks = info.get_basic_blocks();

		for (gm_gps_basic_block b : bb_blocks) {
			int id = b.get_id();
			String temp = String.format("case %d: _master_state_%d(); break;", id, id);
			Body.pushln(temp);
		}
		Body.pushln("}");
		Body.pushln("} while (!_master_should_start_workers && !_master_should_finish);");
		Body.pushln("}");
		Body.NL();

		Body.pushln("//@ Override");
		Body.pushln("public void compute() {");
		Body.pushln("// Graph size is not available in superstep 0");
		Body.pushln("if (getSuperstep() == 0) {");
		Body.pushln("return;");
		Body.pushln("}");
		Body.NL();

		Body.pushln("_master_state_machine();");
		Body.NL();

		Body.pushln("if (_master_should_finish) { ");
		Body.pushln("haltComputation();");
		Body.pushln("writeOutput();");
		Body.pushln("}");

		Body.pushln("}");
		Body.NL();

		gm_reproduce.gm_redirect_reproduce(ps_body); // for temporary
		gm_reproduce.gm_baseindent_reproduce(3);

		for (gm_gps_basic_block b : bb_blocks) {
			do_generate_master_state_body(b);
		}
		gm_reproduce.gm_redirect_reproduce(System.out);
		gm_reproduce.gm_baseindent_reproduce(0);
	}

	@Override
	public void do_generate_master_class() {
		ast_procdef proc = gm_main.FE.get_current_proc();
		gm_gps_beinfo info = (gm_gps_beinfo) gm_main.FE.get_current_backend_info();

		// --------------------------------------------------------------------
		// create master class
		// --------------------------------------------------------------------
		String temp = String.format(" static class %sMasterCompute extends MasterCompute {", proc.get_procname().get_genname());
		Body.pushln(temp);
		Body.pushln("// Control fields");
		boolean prep = gm_main.FE.get_current_proc_info().find_info_bool(GPS_FLAG_USE_REVERSE_EDGE);
		temp = String.format("private int     _master_state                = %d;", !prep ? 0 : GPS_PREPARE_STEP1);
		Body.pushln(temp);
		temp = String.format("private int     _master_state_nxt            = %d;", !prep ? 0 : GPS_PREPARE_STEP1);
		Body.pushln(temp);
		Body.pushln("private boolean _master_should_start_workers = false;");
		Body.pushln("private boolean _master_should_finish        = false;");
		Body.NL();

		// --------------------------------------------------------------------
		// initialization function
		// --------------------------------------------------------------------

		Body.pushln("public void initialize() throws InstantiationException, IllegalAccessException {");

		LinkedList<gm_gps_basic_block> bb_blocks = info.get_basic_blocks();
		HashSet<gm_symtab_entry> scalar = info.get_scalar_symbols();

		temp = String.format("registerPersistentAggregator(%s, IntOverwriteAggregator.class);", GPS_KEY_FOR_STATE);
		Body.pushln(temp);
		for (gm_gps_basic_block b : bb_blocks) {
			if ((!b.is_prepare()) && (!b.is_vertex()))
				continue;

			if (b.find_info_bool(GPS_FLAG_IS_INTRA_MERGED_CONDITIONAL)) {
				int cond_bb_no = b.find_info_int(GPS_INT_INTRA_MERGED_CONDITIONAL_NO);
				temp = String.format("registerPersistentAggregator(\"%s%d\", BooleanOverwriteAggregator.class);", GPS_INTRA_MERGE_IS_FIRST, cond_bb_no);
				Body.pushln(temp);
			}
		}

		for (gm_symtab_entry sym : scalar) {
			gps_syminfo syminfo = (gps_syminfo) sym.find_info(GPS_TAG_BB_USAGE);
			assert syminfo != null;

			if ((syminfo.is_used_in_vertex() || syminfo.is_used_in_receiver()) && syminfo.is_used_in_master()) {
				temp = String.format("registerPersistentAggregator(%s, ", get_lib().create_key_string(sym.getId()));
				Body.push(temp);
				get_lib().generate_broadcast_aggregator_type(sym.getId().getTypeSummary(), Body, syminfo.get_reduce_type());
				Body.pushln(".class);");
			}
		}

		// Iterate symbol table
		gm_symtab args = proc.get_symtab_var();
		assert args != null;
		HashSet<gm_symtab_entry> syms = args.get_entries();
		for (gm_symtab_entry s : syms) {
			// check if used in master
			gps_syminfo syminfo = (gps_syminfo) s.find_info(GPS_TAG_BB_USAGE);
			if (!syminfo.is_used_in_master())
				continue;

			// input argument
			if (!s.getType().is_primitive() && (!s.getType().is_node()))
				continue;

			if (s.isReadable()) {
				String argname = s.getId().get_genname();
				temp = String.format("%s = getContext().getConfiguration().", argname);
				Body.push(temp);
				switch (s.getType().getTypeSummary()) {
				case GMTYPE_BOOL:
					temp = String.format("getBoolean(\"%s\", false);", argname);
					break;
				case GMTYPE_INT:
					temp = String.format("getInt(\"%s\", -1);", argname);
					break;
				case GMTYPE_LONG:
					temp = String.format("getLong(\"%s\", -1L);", argname);
					break;
				case GMTYPE_FLOAT:
					temp = String.format("getFloat(\"%s\", -1.0f);", argname);
					break;
				// TODO Waiting for
				// https://issues.apache.org/jira/browse/HADOOP-8415 to be
				// accepted
				// case GMTYPE_DOUBLE:
				// sprintf(temp, "getDouble(\"%s\", -1.0);", argname);
				// break;
				case GMTYPE_DOUBLE:
					temp = String.format("getFloat(\"%s\", -1.0f);", argname);
					break;
				case GMTYPE_NODE:
					if (get_lib().is_node_type_int()) {
						temp = String.format("getInteger(\"%s\", -1);", argname);
					} else {
						temp = String.format("getLong(\"%s\", -1L);", argname);
					}
					break;
				default:
					assert false;
					break;
				}
				Body.pushln(temp);
			}
		}
		Body.pushln("}");
		Body.NL();

		// --------------------------------------------------------------------
		// A method that saves final output values
		// --------------------------------------------------------------------
		Body.pushln("//save output");
		Body.pushln("public void writeOutput() {");
		ast_typedecl t = proc.get_return_type();
		if ((t != null) && (!t.is_void())) {
			temp = String.format("System.out.println(\"%s:\\t\" + %s + \"\\n\");", GPS_RET_VALUE, GPS_RET_VALUE);
			Body.pushln(temp);
		}
		for (gm_symtab_entry s : syms) {
			// output arguments
			if (!s.getType().is_primitive())
				continue;
			if (s.isWriteable()) {
				temp = String.format("System.out.println(\"%s:\\t\" + %s + \"\\n\");", s.getId().get_genname(), s.getId().get_genname());
				Body.pushln(temp);
			}
		}
		Body.pushln("}");
		Body.NL();
	}

	@Override
	public void do_generate_master_scalar() {
		Body.pushln("//----------------------------------------------------------");
		Body.pushln("// Scalar Variables ");
		Body.pushln("//----------------------------------------------------------");
		gm_gps_beinfo info = (gm_gps_beinfo) gm_main.FE.get_current_backend_info();
		HashSet<gm_symtab_entry> scalar = info.get_scalar_symbols();

		for (gm_symtab_entry e : scalar) {
			gps_syminfo syminfo = (gps_syminfo) e.find_info(GPS_TAG_BB_USAGE);
			if (!syminfo.is_used_in_master())
				continue;

			String temp = String.format("private %s %s;", get_type_string(e.getType(), true), e.getId().get_genname());
			Body.pushln(temp);
		}

		ast_procdef proc = gm_main.FE.get_current_proc();
		ast_typedecl t = proc.get_return_type();
		if ((t != null) && (!t.is_void())) {
			String temp = String.format("private %s %s; // the final return value of the procedure", get_type_string(t, true), GPS_RET_VALUE);
			Body.pushln(temp);
		}

		// Intra-Loop Merging
		if (proc.has_info(GPS_LIST_INTRA_MERGED_CONDITIONAL)) {
			LinkedList<Object> L = proc.get_info_list(GPS_LIST_INTRA_MERGED_CONDITIONAL);
			for (Object obj : L) {
				gm_gps_basic_block bb = (gm_gps_basic_block) (obj);
				String temp = String.format("private boolean %s%d = true;", GPS_INTRA_MERGE_IS_FIRST, bb.get_id());
				Body.pushln(temp);
			}
		}

		Body.NL();
	}

	void do_generate_master_serialization() {
		gm_gps_beinfo info = (gm_gps_beinfo) gm_main.FE.get_current_backend_info();
		HashSet<gm_symtab_entry> scalar = info.get_scalar_symbols();
		get_lib().generate_master_class_details(scalar, Body);
	}

	@Override
	public void do_generate_master_state_body(gm_gps_basic_block b) {
		int id = b.get_id();
		gm_gps_bbtype_t type = b.get_type();

		String temp = String.format("private void _master_state_%d() {", id);
		Body.pushln(temp);
		Body.pushln("/*------");
		Body.flush();
		b.reproduce_sents();
		Body.pushln("-----*/");
		temp = String.format("LOG.info(\"Running _master_state %d\");", id);
		Body.pushln(temp);
		if (type == gm_gps_bbtype_t.GM_GPS_BBTYPE_BEGIN_VERTEX) {

			// generate Broadcast
			do_generate_scalar_broadcast_send(b);
			get_lib().generate_broadcast_state_master("_master_state", Body);
			if (b.find_info_bool(GPS_FLAG_IS_INTRA_MERGED_CONDITIONAL)) {
				int cond_bb_no = b.find_info_int(GPS_INT_INTRA_MERGED_CONDITIONAL_NO);
				temp = String.format("%s%d", GPS_INTRA_MERGE_IS_FIRST, cond_bb_no);
				get_lib().generate_broadcast_isFirst_master(temp, Body);
			}
			Body.NL();

			// generate next statement
			assert b.get_num_exits() == 1;
			int n = b.get_nth_exit(0).get_id();
			temp = String.format("_master_state_nxt = %d;", n);
			Body.pushln(temp);
			Body.pushln("_master_should_start_workers = true;");
		} else if (type == gm_gps_bbtype_t.GM_GPS_BBTYPE_SEQ) {
			if (b.is_after_vertex()) {
				assert b.get_num_entries() == 1;
				do_generate_scalar_broadcast_receive(b);
			}

			// define local variables
			HashMap<gm_symtab_entry, gps_syminfo> symbols = b.get_symbols();
			for (gm_symtab_entry sym : symbols.keySet()) {
				gps_syminfo local_info = symbols.get(sym);
				if (!local_info.is_scalar() || sym.isArgument()) // TODO: why is
																	// sym->isArgument()
																	// !=
																	// local_info->is_argument()
																	// ?
					continue;
				gps_syminfo global_info = (gps_syminfo) sym.find_info(GPS_TAG_BB_USAGE);

				if (!global_info.is_used_in_multiple_BB()) {
					generate_scalar_var_def(sym, true);
				}
			}
			Body.NL();

			int cond_bb_no = b.find_info_int(GPS_INT_INTRA_MERGED_CONDITIONAL_NO);

			// generate sequential sentences
			b.prepare_iter();
			ast_sent s = b.get_next();
			while (s != null) {
				if (s.find_info_bool(GPS_FLAG_IS_INTRA_MERGED_CONDITIONAL)) {
					temp = String.format("if (!%s%d) {", GPS_INTRA_MERGE_IS_FIRST, cond_bb_no);
					Body.pushln(temp);
				}

				generate_sent(s);

				if (s.find_info_bool(GPS_FLAG_IS_INTRA_MERGED_CONDITIONAL)) {
					Body.pushln("}");
				}

				s = b.get_next();
			}

			if (b.get_num_exits() == 0) {
				Body.pushln("_master_should_finish = true;");
			} else {
				int n = b.get_nth_exit(0).get_id();
				temp = String.format("_master_state_nxt = %d;", n);
				Body.pushln(temp);
			}
		} else if (type == gm_gps_bbtype_t.GM_GPS_BBTYPE_IF_COND) {

			Body.push("boolean _expression_result = ");

			// generate sentences
			ast_sent s = b.get_1st_sent();
			assert s != null;
			assert s.get_nodetype() == ast_node_type.AST_IF;
			ast_if i = (ast_if) s;
			generate_expr(i.get_cond());
			Body.pushln(";");

			temp = String.format("if (_expression_result) _master_state_nxt = %d;\nelse _master_state_nxt = %d;", b.get_nth_exit(0).get_id(), b.get_nth_exit(1)
					.get_id());
			Body.pushln(temp);
		} else if (type == gm_gps_bbtype_t.GM_GPS_BBTYPE_WHILE_COND) {
			ast_sent s = b.get_1st_sent();
			assert s != null;
			assert s.get_nodetype() == ast_node_type.AST_WHILE;
			ast_while i = (ast_while) s;
			if (i.is_do_while())
				Body.pushln("// Do-While(...)");
			else
				Body.pushln("// While (...)");

			Body.NL();
			Body.push("boolean _expression_result = ");
			generate_expr(i.get_cond());
			Body.pushln(";");

			temp = String.format("if (_expression_result) _master_state_nxt = %d;\nelse _master_state_nxt = %d;\n", b.get_nth_exit(0).get_id(),
					b.get_nth_exit(1).get_id()); // exit - continue while
			Body.pushln(temp);

			if (b.find_info_bool(GPS_FLAG_IS_INTRA_MERGED_CONDITIONAL)) {
				temp = String.format("if (!_expression_result) %s%d=true; // reset is_first\n\n", GPS_INTRA_MERGE_IS_FIRST, b.get_id());
				Body.pushln(temp);
			}

		} else if ((type == gm_gps_bbtype_t.GM_GPS_BBTYPE_PREPARE1) || (type == gm_gps_bbtype_t.GM_GPS_BBTYPE_PREPARE2)) {

			// generate Broadcast
			do_generate_scalar_broadcast_send(b);
			get_lib().generate_broadcast_state_master("_master_state", Body);

			Body.pushln("// Preparation Step;");
			assert b.get_num_exits() == 1;
			int n = b.get_nth_exit(0).get_id();
			temp = String.format("_master_state_nxt = %d;", n);
			Body.pushln(temp);
			Body.pushln("_master_should_start_workers = true;");
		} else if (type == gm_gps_bbtype_t.GM_GPS_BBTYPE_MERGED_TAIL) {
			Body.pushln("// Intra-Loop Merged");
			int source_id = b.find_info_int(GPS_INT_INTRA_MERGED_CONDITIONAL_NO);
			temp = String.format("if (%s%d) _master_state_nxt = %d;", GPS_INTRA_MERGE_IS_FIRST, source_id, b.get_nth_exit(0).get_id());
			Body.pushln(temp);
			temp = String.format("else _master_state_nxt = %d;", b.get_nth_exit(1).get_id());
			Body.pushln(temp);
			temp = String.format("%s%d = false;\n", GPS_INTRA_MERGE_IS_FIRST, source_id);
			Body.pushln(temp);
		} else {
			assert false;
		}

		Body.pushln("}"); // end of state function
	}

	@Override
	public void do_generate_scalar_broadcast_send(gm_gps_basic_block b) {
		// check if scalar variable is used inside the block
		HashMap<gm_symtab_entry, gps_syminfo> syms = b.get_symbols();
		for (gm_symtab_entry entry : syms.keySet()) {
			gps_syminfo local_info = syms.get(entry);
			gps_syminfo global_info = (gps_syminfo) entry.find_info(GPS_TAG_BB_USAGE);
			if (!global_info.is_scalar())
				continue;
			if (local_info.is_used_as_reduce()) {
				GM_REDUCE_T reduce_type = local_info.get_reduce_type();

				// printf("being used as reduce :%s\n",
				// I->first->getId()->get_genname());
				get_lib().generate_broadcast_reduce_initialize_master(entry.getId(), Body, reduce_type,
						reduce_type.get_reduce_base_value(entry.getType().getTypeSummary()));
				// [TODO] global argmax
				continue;
			}
			if (!global_info.is_used_in_master() && !global_info.is_argument())
				continue;
			if (local_info.is_used_as_rhs() && !global_info.is_argument()) {
				// create a broad cast variable
				get_lib().generate_broadcast_send_master(entry.getId(), Body);
			}
		}
	}

	@Override
	public void do_generate_scalar_broadcast_receive(gm_gps_basic_block b) {
		assert b.get_num_entries() == 1;
		gm_gps_basic_block pred = b.get_nth_entry(0);
		assert pred.is_vertex();

		// check if scalar variable is modified inside the block
		HashMap<gm_symtab_entry, gps_syminfo> syms = pred.get_symbols();
		for (gm_symtab_entry entry : syms.keySet()) {
			gps_syminfo local_info = syms.get(entry);
			gps_syminfo global_info = (gps_syminfo) entry.find_info(GPS_TAG_BB_USAGE);
			if (!global_info.is_scalar())
				continue;
			if (!global_info.is_used_in_master())
				continue;
			if (local_info.is_used_as_lhs() || local_info.is_used_as_reduce()) {
				// create a broad cast variable
				get_lib().generate_broadcast_receive_master(entry.getId(), Body, local_info.get_reduce_type());
			}
		}
	}

	@Override
	public void do_generate_shared_variables_keys() {
		Body.pushln("// Keys for shared_variables ");
		gm_gps_beinfo info = (gm_gps_beinfo) gm_main.FE.get_current_backend_info();
		HashSet<gm_symtab_entry> scalar = info.get_scalar_symbols();

		for (gm_symtab_entry sym : scalar) {
			gps_syminfo syminfo = (gps_syminfo) sym.find_info(GPS_TAG_BB_USAGE);
			assert syminfo != null;

			// if the symbol is used in vertex and master
			// we need shared variable
			if ((syminfo.is_used_in_vertex() || syminfo.is_used_in_receiver()) && (syminfo.is_scoped_global())) {
				Body.push("private static final String ");
				Body.push(get_lib().create_key_string(sym.getId()));
				Body.push(" = ");
				Body.push("\"");
				Body.push(sym.getId().get_genname());
				Body.pushln("\";");
			}
		}
		Body.NL();
	}

	void do_generate_vertex_body() {
		set_master_generate(false);
		do_generate_vertex_states();
		do_generate_worker_context_class();
		do_generate_vertex_property_class(false);

		if (gm_main.FE.get_current_proc().find_info_bool(GPS_FLAG_USE_EDGE_PROP))
			do_generate_vertex_property_class(true);

		do_generate_message_class();
	}

	void do_generate_vertex_begin() {
		String temp;
		String proc_name = gm_main.FE.get_current_proc().get_procname().get_genname();
		Body.pushln("//----------------------------------------------");
		Body.pushln("// Main Vertex Class");
		Body.pushln("//----------------------------------------------");
		if (gm_main.OPTIONS.get_arg_bool(gm_argopts.GMARGFLAG_GIRAPH_VERTEX_ONLY)) {
			temp = String.format("public class %sVertex", proc_name);
		} else {
			temp = String.format("public static class %sVertex", proc_name);
		}
		Body.pushln(temp);
		Body.pushIndent();
		if (gm_main.FE.get_current_proc().find_info_bool(GPS_FLAG_USE_EDGE_PROP)) {
			temp = String.format("extends EdgeListVertex< %s, %sVertex.VertexData, %sVertex.EdgeData, %sVertex.MessageData > {", gm_main.PREGEL_BE.get_lib()
					.is_node_type_int() ? "IntWritable" : "LongWritable", proc_name, proc_name, proc_name);
		} else {
			temp = String.format("extends EdgeListVertex< %s, %sVertex.VertexData, NullWritable, %sVertex.MessageData > {", gm_main.PREGEL_BE.get_lib()
					.is_node_type_int() ? "IntWritable" : "LongWritable", proc_name, proc_name);
		}
		Body.pushln(temp);
		Body.popIndent();
		Body.NL();
		Body.pushln("// Vertex logger");
		temp = String.format("private static final Logger LOG = Logger.getLogger(%sVertex.class);", proc_name);
		Body.pushln(temp);
		Body.NL();
	}

	void do_generate_vertex_end() {
		Body.pushln("} // end of vertex class");
		Body.NL();
	}

	void do_generate_worker_context_class() {
		String proc_name = gm_main.FE.get_current_proc().get_procname().get_genname();

		Body.pushln("//----------------------------------------------");
		Body.pushln("// Worker Context Class");
		Body.pushln("//----------------------------------------------");
		String temp = String.format("public static class %sWorkerContext extends WorkerContext {", proc_name);
		Body.pushln(temp);
		Body.NL();
		Body.pushln("@Override");
		Body.pushln("public void preApplication() throws InstantiationException, IllegalAccessException {");
		Body.pushln("}");
		Body.NL();
		Body.pushln("@Override");
		Body.pushln("public void postApplication() {");
		Body.pushln("}");
		Body.NL();
		Body.pushln("@Override");
		Body.pushln("public void preSuperstep() {");
		Body.pushln("}");
		Body.NL();
		Body.pushln("@Override");
		Body.pushln("public void postSuperstep() {");
		Body.pushln("}");
		Body.NL();
		Body.pushln("} // end of worker context");
		Body.NL();
	}

	@Override
	public void do_generate_vertex_property_class(boolean is_edge_prop) {
		Body.pushln("//----------------------------------------------");
		if (is_edge_prop)
			Body.pushln("// Edge Property Class");
		else
			Body.pushln("// Vertex Property Class");
		Body.pushln("//----------------------------------------------");
		ast_procdef proc = gm_main.FE.get_current_proc();
		assert proc != null;
		String temp = String.format(" static class %s implements Writable {", is_edge_prop ? "EdgeData" : "VertexData");
		Body.pushln(temp);

		// list out property
		Body.pushln("// properties");
		gm_gps_beinfo info = (gm_gps_beinfo) gm_main.FE.get_current_backend_info();
		HashSet<gm_symtab_entry> prop = is_edge_prop ? info.get_edge_prop_symbols() : info.get_node_prop_symbols();
		for (gm_symtab_entry sym : prop) {
			// gps_syminfo* syminfo = (gps_syminfo*)
			// sym->find_info(TAG_BB_USAGE);
			temp = String.format("%s %s;", get_type_string(sym.getType().get_target_type(), is_master_generate()), sym.getId().get_genname());

			Body.pushln(temp);
		}

		if (gm_main.FE.get_current_proc_info().find_info_bool(GPS_FLAG_USE_REVERSE_EDGE)) {
			temp = String.format("%s[] %s; //reverse edges (node IDs)", get_lib().is_node_type_int() ? "IntWritable" : "LongWritable", GPS_REV_NODE_ID);
			Body.pushln(temp);
		}

		Body.NL();
		get_lib().generate_vertex_prop_class_details(prop, Body, is_edge_prop);

		Body.pushln("} // end of vertex property class");
		Body.NL();

	}

	@Override
	public void do_generate_message_class() {
		Body.pushln("//----------------------------------------------");
		Body.pushln("// Message Data ");
		Body.pushln("//----------------------------------------------");

		ast_procdef proc = gm_main.FE.get_current_proc();
		assert proc != null;
		gm_gps_beinfo info = (gm_gps_beinfo) gm_main.FE.get_current_backend_info();
		Body.pushln("public static class MessageData implements Writable {");

		Body.pushln("public MessageData() {}");
		Body.NL();

		if (info.is_single_message()) {
			Body.pushln("//single message type; argument ignored");
			Body.pushln("public MessageData(byte type) {}");
		} else {
			Body.pushln("byte m_type;");
			Body.pushln("public MessageData(byte type) {m_type = type;}");
		}
		Body.NL();

		get_lib().generate_message_class_details(info, Body);

		Body.pushln("} // end of message-data class");
		Body.NL();
	}

	@Override
	public void do_generate_vertex_states() {
		Body.NL();
		Body.pushln("@Override");
		Body.pushln("public void compute(Iterable<MessageData> _msgs) {");
		get_lib().generate_receive_state_vertex("_state_vertex", Body);

		Body.pushln("switch(_state_vertex) { ");
		gm_gps_beinfo info = (gm_gps_beinfo) gm_main.FE.get_current_backend_info();
		LinkedList<gm_gps_basic_block> bb_blocks = info.get_basic_blocks();
		int cnt = 0;
		for (gm_gps_basic_block b : bb_blocks) {
			if ((!b.is_prepare()) && (!b.is_vertex()))
				continue;
			int id = b.get_id();
			String temp = String.format("case %d: _vertex_state_%d(_msgs); break;", id, id);
			Body.pushln(temp);
			cnt++;
		}
		if (cnt == 0) {
			Body.pushln("default: break;");
		}
		Body.pushln("}");

		Body.pushln("}");

		gm_reproduce.gm_redirect_reproduce(ps_body); // for temporary
		gm_reproduce.gm_baseindent_reproduce(3);
		for (gm_gps_basic_block b : bb_blocks) {
			if ((!b.is_prepare()) && (!b.is_vertex()))
				continue;
			do_generate_vertex_state_body(b);
		}
		gm_reproduce.gm_redirect_reproduce(System.out);
		gm_reproduce.gm_baseindent_reproduce(0);
	}

	@Override
	public void do_generate_vertex_state_body(gm_gps_basic_block b) {
		int id = b.get_id();
		gm_gps_bbtype_t type = b.get_type();

		String temp = String.format("private void _vertex_state_%d(Iterable<MessageData> _msgs) {", id);
		Body.pushln(temp);

		get_lib().generate_vertex_prop_access_prepare(Body);

		do_generate_vertex_state_receive_global(b);

		if (b.is_prepare()) {
			get_lib().generate_prepare_bb(Body, b);
			Body.pushln("}");
			return;
		}

		assert type == gm_gps_bbtype_t.GM_GPS_BBTYPE_BEGIN_VERTEX;
		boolean is_conditional = b.find_info_bool(GPS_FLAG_IS_INTRA_MERGED_CONDITIONAL);
		String cond_var = "";
		if (is_conditional) {
			cond_var = String.format("%s%d", GPS_INTRA_MERGE_IS_FIRST, b.find_info_int(GPS_INT_INTRA_MERGED_CONDITIONAL_NO));
		}

		// ---------------------------------------------------------
		// Generate Receiver Routine
		// ---------------------------------------------------------
		if (b.has_receiver()) {
			set_receiver_generate(true);
			Body.NL();

			if (is_conditional) {
				temp = String.format("if (!%s) {", cond_var);
				Body.pushln(temp);
			}

			Body.pushln("// Begin msg receive");
			Body.pushln("for (MessageData _msg : _msgs) {");

			LinkedList<gm_gps_comm_unit> R = b.get_receivers();
			for (gm_gps_comm_unit U : R) {
				if (U.get_type() == gm_gps_comm_t.GPS_COMM_NESTED) {
					ast_foreach fe = U.fe;
					assert fe != null;

					Body.pushln("/*------");
					Body.pushln("(Nested Loop)");
					Body.flush();
					if (is_conditional)
						gm_reproduce.gm_baseindent_reproduce(5);
					else
						gm_reproduce.gm_baseindent_reproduce(4);
					fe.reproduce(0);
					gm_reproduce.gm_flush_reproduce();
					Body.pushln("-----*/");
					get_lib().generate_message_receive_begin(fe, Body, b, R.size() == 1);

					if (fe.get_body().get_nodetype() == ast_node_type.AST_SENTBLOCK) {
						generate_sent_block((ast_sentblock) fe.get_body(), false);
					} else {
						generate_sent(fe.get_body());
					}

					get_lib().generate_message_receive_end(Body, R.size() == 1);
				} else {
					ast_sentblock sb = U.sb;
					assert sb != null;
					Body.pushln("/*------");
					Body.pushln("(Random Write)");
					Body.pushln("{");
					Body.flush();
					if (is_conditional)
						gm_reproduce.gm_baseindent_reproduce(6);
					else
						gm_reproduce.gm_baseindent_reproduce(5);
					for (ast_sent s : sb.get_sents()) {
						if (s.find_info_ptr(GPS_FLAG_SENT_BLOCK_FOR_RANDOM_WRITE_ASSIGN) == sb)
							s.reproduce(0);
					}
					gm_reproduce.gm_flush_reproduce();
					Body.pushln("}");
					Body.pushln("-----*/");
					get_lib().generate_message_receive_begin(sb, U.sym, Body, b, R.size() == 1);

					for (ast_sent s : sb.get_sents()) {
						if (s.find_info_ptr(GPS_FLAG_SENT_BLOCK_FOR_RANDOM_WRITE_ASSIGN) == sb) {
							// implement receiving sentence
							generate_sent(s);
						}
					}

					get_lib().generate_message_receive_end(Body, R.size() == 1);
				}
			}
			set_receiver_generate(false);
			Body.pushln("}");
			if (is_conditional) {
				Body.pushln("}");
			}
			Body.NL();
			gm_reproduce.gm_baseindent_reproduce(3);
		}

		// ---------------------------------------------------------
		// Generate Main Routine
		// ---------------------------------------------------------
		if (b.get_num_sents() > 0) {
			// assert (b->get_num_sents() == 1);
			Body.pushln("/*------");
			Body.flush();
			b.reproduce_sents(false);
			Body.pushln("-----*/");
			Body.NL();

			LinkedList<ast_sent> sents = b.get_sents();
			int cnt = 0;
			for (ast_sent s : sents) {
				assert s.get_nodetype() == ast_node_type.AST_FOREACH;
				ast_foreach fe = (ast_foreach) s;
				ast_sent body = fe.get_body();
				if (cnt != 0)
					Body.NL();
				cnt++;
				if (fe.find_info_bool(GPS_FLAG_IS_INTRA_MERGED_CONDITIONAL)) {
					temp = String.format("if (!%s)", cond_var);
					Body.push(temp);
					if (body.get_nodetype() != ast_node_type.AST_SENTBLOCK)
						Body.pushln(" {");
					else
						Body.NL();
				}

				generate_sent(body);

				if (fe.find_info_bool(GPS_FLAG_IS_INTRA_MERGED_CONDITIONAL)) {
					if (body.get_nodetype() != ast_node_type.AST_SENTBLOCK)
						Body.pushln("}");
				}

			}
		}

		Body.pushln("}");
	}

	@Override
	public void do_generate_vertex_state_receive_global(gm_gps_basic_block b) {

		// load scalar variable
		HashMap<gm_symtab_entry, gps_syminfo> symbols = b.get_symbols();
		for (gm_symtab_entry sym : symbols.keySet()) {
			gps_syminfo local_info = symbols.get(sym);
			if (!local_info.is_scalar())
				continue;

			gps_syminfo global_info = (gps_syminfo) sym.find_info(GPS_TAG_BB_USAGE);
			assert global_info != null;

			if (sym.getType().is_node_iterator()) {
				// do nothing
			} else if (global_info.is_scoped_global()) {
				if (local_info.is_used_as_rhs()) {
					generate_scalar_var_def(sym, false);
					Body.push(" = ");
					if (global_info.is_argument()) {
						// read the parameter
						get_lib().generate_parameter_read_vertex(sym.getId(), Body);
					} else {
						// receive it from Broadcast
						get_lib().generate_broadcast_receive_vertex(sym.getId(), Body);
					}
					Body.pushln(";");
				}
			} else {
				// temporary scalar variables. Define it here
				generate_scalar_var_def(sym, true);
			}
		}

		if (b.find_info_bool(GPS_FLAG_IS_INTRA_MERGED_CONDITIONAL)) {
			String temp = String.format("%s%d", GPS_INTRA_MERGE_IS_FIRST, b.find_info_int(GPS_INT_INTRA_MERGED_CONDITIONAL_NO));
			get_lib().generate_receive_isFirst_vertex(temp, Body);
		}
	}

	void do_generate_input_output_formats() {
		ast_procdef proc = gm_main.FE.get_current_proc();

		String proc_name = proc.get_procname().get_genname();
		String vertex_id = get_lib().is_node_type_int() ? "IntWritable" : "LongWritable";
		String vertex_data = String.format("%sVertex.VertexData", proc_name);
		String edge_data;
		if (proc.find_info_bool(GPS_FLAG_USE_EDGE_PROP)) {
			edge_data = String.format("%sVertex.EdgeData", proc_name);
		} else {
			edge_data = String.format("NullWritable");
		}
		String message_data = String.format("%sVertex.MessageData", proc_name);

		Body.pushln("//----------------------------------------------");
		Body.pushln("// Vertex Input format");
		Body.pushln("//----------------------------------------------");

		Body.pushln(String.format("static class %sVertexInputFormat extends TextVertexInputFormat<%s, %s, %s, %s> {", proc_name, vertex_id, vertex_data,
				edge_data, message_data));
		Body.pushln("@Override");
		Body.pushln(String.format(" VertexReader<%s, %s, %s, %s>", vertex_id, vertex_data, edge_data, message_data));
		Body.pushln("createVertexReader(InputSplit split, TaskAttemptContext context) throws IOException {");
		Body.pushln(String.format("return new %sVertexReader(textInputFormat.createRecordReader(split, context));", proc_name));
		Body.pushln("}");
		Body.NL();

		Body.pushln(String.format("static class %sVertexReader extends TextVertexInputFormat.TextVertexReader<%s, %s, %s, %s> {", proc_name, vertex_id,
				vertex_data, edge_data, message_data));
		Body.pushln(String.format(" %sVertexReader(RecordReader<LongWritable, Text> lineRecordReader) {", proc_name));
		Body.pushln("super(lineRecordReader);");
		Body.pushln("}");
		Body.NL();

		Body.pushln("@Override");
		Body.pushln(String.format(" Vertex<%s, %s, %s, %s> getCurrentVertex() throws IOException, InterruptedException {", vertex_id, vertex_data, edge_data,
				message_data));
		Body.pushln(String.format("Vertex<%s, %s, %s, %s> vertex =", vertex_id, vertex_data, edge_data, message_data));
		Body.pushln(String.format("    BspUtils.<%s, %s, %s, %s> createVertex(getContext().getConfiguration());", vertex_id, vertex_data, edge_data,
				message_data));
		Body.NL();

		Body.pushln("Text line = getRecordReader().getCurrentValue();");
		Body.pushln("String[] values = line.toString().split(\"\\t\");");
		if (get_lib().is_node_type_int()) {
			Body.pushln("IntWritable vertexId = new IntWritable(Integer.parseInt(values[0]));");
		} else {
			Body.pushln("LongWritable vertexId = new LongWritable(Long.parseLong(values[0]));");
		}
		Body.pushln("double vertexValue = Double.parseDouble(values[1]);");
		Body.pushln(String.format("Map<%s, %s> edges = Maps.newHashMap();", vertex_id, edge_data));
		Body.pushln("for (int i = 2; i < values.length; i += 2) {");
		if (get_lib().is_node_type_int()) {
			Body.pushln("IntWritable edgeId = new IntWritable(Integer.parseInt(values[i]));");
		} else {
			Body.pushln("LongWritable edgeId = new LongWritable(Long.parseLong(values[i]));");
		}
		if (proc.find_info_bool(GPS_FLAG_USE_EDGE_PROP)) {
			Body.pushln("double edgeValue = Double.parseDouble(values[i+1]);");
			Body.pushln(String.format("edges.put(edgeId, new %s(edgeValue));", edge_data));
		} else {
			Body.pushln("edges.put(edgeId, NullWritable.get());");
		}
		Body.pushln("}");
		Body.pushln(String.format("vertex.initialize(vertexId, new %sVertex.VertexData(vertexValue), edges, null);", proc_name));
		Body.pushln("return vertex;");
		Body.pushln("}");
		Body.NL();

		Body.pushln("@Override");
		Body.pushln("public boolean nextVertex() throws IOException, InterruptedException {");
		Body.pushln("return getRecordReader().nextKeyValue();");
		Body.pushln("}");
		Body.pushln("}");
		Body.pushln("} // end of vertex input format");
		Body.NL();

		Body.pushln("// ----------------------------------------------");
		Body.pushln("// Vertex Output format");
		Body.pushln("// ----------------------------------------------");
		Body.pushln(String.format("static class %sVertexOutputFormat extends", proc_name));
		Body.pushln(String.format("TextVertexOutputFormat<%s, %s, %s> {", vertex_id, vertex_data, edge_data));
		Body.pushln("@Override");
		Body.pushln(String.format(" VertexWriter<%s, %s, %s> createVertexWriter(", vertex_id, vertex_data, edge_data));
		Body.pushln("TaskAttemptContext context) throws IOException, InterruptedException {");
		Body.pushln(String.format("return new %sVertexWriter(textOutputFormat.getRecordWriter(context));", proc_name));
		Body.pushln("}");
		Body.NL();

		Body.pushln(String.format("static class %sVertexWriter", proc_name));
		Body.pushln(String.format("extends TextVertexOutputFormat.TextVertexWriter<%s, %s, %s> {", vertex_id, vertex_data, edge_data));
		Body.pushln(String.format(" %sVertexWriter(RecordWriter<Text, Text> lineRecordReader) {", proc_name));
		Body.pushln("super(lineRecordReader);");
		Body.pushln("}");
		Body.NL();

		Body.pushln("@Override");
		Body.pushln("public void writeVertex(");
		Body.pushln(String.format("Vertex<%s, %s, %s, ?> vertex)", vertex_id, vertex_data, edge_data));
		Body.pushln("throws IOException, InterruptedException {");
		Body.pushln("StringBuffer sb = new StringBuffer(vertex.getId().toString());");
		Body.pushln("sb.append('\\t').append(vertex.getValue());");
		Body.NL();

		Body.pushln(String.format("for (Edge<%s, %s> edge : vertex.getEdges()) {", vertex_id, edge_data));
		if (proc.find_info_bool(GPS_FLAG_USE_EDGE_PROP)) {
			Body.pushln("sb.append('\\t').append(edge.getTargetVertexId());");
			Body.pushln("sb.append('\\t').append(edge.getValue());");
		} else {
			Body.pushln("sb.append('\\t').append(edge.getTargetVertexId());");
			Body.pushln("sb.append(\"\\t1.0\");");
		}
		Body.pushln("}");
		Body.NL();

		Body.pushln("getRecordWriter().write(new Text(sb.toString()), null);");
		Body.pushln("}");
		Body.pushln("}");
		Body.pushln("} // end of vertex output format");
	}

	@Override
	public void do_generate_job_configuration() {
		ast_procdef proc = gm_main.FE.get_current_proc();
		String proc_name = proc.get_procname().get_genname();

		// Iterate symbol table
		gm_symtab args = proc.get_symtab_var();
		assert args != null;
		HashSet<gm_symtab_entry> syms = args.get_entries();

		Body.NL();
		Body.pushln("//----------------------------------------------");
		Body.pushln("// Job Configuration");
		Body.pushln("//----------------------------------------------");
		Body.pushln("@Override");
		Body.pushln("public final int run(final String[] args) throws Exception {");
		Body.pushln("Options options = new Options();");
		Body.pushln("options.addOption(\"h\", \"help\", false, \"Help\");");
		Body.pushln("options.addOption(\"v\", \"verbose\", false, \"Verbose\");");
		Body.pushln("options.addOption(\"w\", \"workers\", true, \"Number of workers\");");
		Body.pushln("options.addOption(\"i\", \"input\", true, \"Input filename\");");
		Body.pushln("options.addOption(\"o\", \"output\", true, \"Output filename\");");
		String temp;
		for (gm_symtab_entry s : syms) {
			if (!s.getType().is_primitive() && (!s.getType().is_node()))
				continue;
			if (s.isReadable()) {
				temp = String.format("options.addOption(\"_%s\", \"%s\", true, \"%s\");", s.getId().get_genname(), s.getId().get_genname(), s.getId()
						.get_genname());
				Body.pushln(temp);
			}
		}
		Body.pushln("HelpFormatter formatter = new HelpFormatter();");
		Body.pushln("if (args.length == 0) {");
		Body.pushln("formatter.printHelp(getClass().getName(), options, true);");
		Body.pushln("return 0;");
		Body.pushln("}");
		Body.pushln("CommandLineParser parser = new PosixParser();");
		Body.pushln("CommandLine cmd = parser.parse(options, args);");
		Body.pushln("if (cmd.hasOption('h')) {");
		Body.pushln("formatter.printHelp(getClass().getName(), options, true);");
		Body.pushln("return 0;");
		Body.pushln("}");
		Body.pushln("if (!cmd.hasOption('w')) {");
		Body.pushln("LOG.info(\"Need to choose the number of workers (-w)\");");
		Body.pushln("return -1;");
		Body.pushln("}");
		Body.pushln("if (!cmd.hasOption('i')) {");
		Body.pushln("LOG.info(\"Need to set input path (-i)\");");
		Body.pushln("return -1;");
		Body.pushln("}");
		for (gm_symtab_entry s : syms) {
			if (!s.getType().is_primitive() && (!s.getType().is_node()))
				continue;
			if (s.isReadable()) {
				temp = String.format("if (!cmd.hasOption(\"%s\")) {", s.getId().get_genname());
				Body.pushln(temp);
				temp = String.format("LOG.info(\"Need to set procedure argument (--%s)\");", s.getId().get_genname());
				Body.pushln(temp);
				Body.pushln("return -1;");
				Body.pushln("}");
			}
		}
		Body.NL();
		Body.pushln("GiraphJob job = new GiraphJob(getConf(), getClass().getName());");
		Body.pushln("job.getConfiguration().setInt(GiraphJob.CHECKPOINT_FREQUENCY, 0);");
		temp = String.format("job.setMasterComputeClass(%sVertex.%sMasterCompute.class);", proc_name, proc_name);
		Body.pushln(temp);
		temp = String.format("job.setVertexClass(%sVertex.class);", proc_name);
		Body.pushln(temp);
		temp = String.format("job.setWorkerContextClass(%sVertex.%sWorkerContext.class);", proc_name, proc_name);
		Body.pushln(temp);
		temp = String.format("job.setVertexInputFormatClass(%sVertexInputFormat.class);", proc_name);
		Body.pushln(temp);
		Body.pushln("FileInputFormat.addInputPath(job.getInternalJob(), new Path(cmd.getOptionValue('i')));");
		Body.pushln("if (cmd.hasOption('o')) {");
		temp = String.format("job.setVertexOutputFormatClass(%sVertexOutputFormat.class);", proc_name);
		Body.pushln(temp);
		Body.pushln("FileOutputFormat.setOutputPath(job.getInternalJob(), new Path(cmd.getOptionValue('o')));");
		Body.pushln("}");
		Body.pushln("int workers = Integer.parseInt(cmd.getOptionValue('w'));");
		Body.pushln("job.setWorkerConfiguration(workers, workers, 100.0f);");
		for (gm_symtab_entry s : syms) {
			if (!s.getType().is_primitive() && (!s.getType().is_node()))
				continue;
			if (s.isReadable()) {
				String argname = s.getId().get_genname();
				temp = String.format("job.getConfiguration().");
				Body.push(temp);
				switch (s.getType().getTypeSummary()) {
				case GMTYPE_BOOL:
					temp = String.format("setBoolean(\"%s\", Boolean.parseBoolean(cmd.getOptionValue(\"%s\")));", argname, argname);
					break;
				case GMTYPE_INT:
					temp = String.format("setInt(\"%s\", Integer.parseInt(cmd.getOptionValue(\"%s\")));", argname, argname);
					break;
				case GMTYPE_LONG:
					temp = String.format("setLong(\"%s\", Long.parseLong(cmd.getOptionValue(\"%s\")));", argname, argname);
					break;
				case GMTYPE_FLOAT:
					temp = String.format("setFloat(\"%s\", Float.parseFloat(cmd.getOptionValue(\"%s\")));", argname, argname);
					break;
				// TODO Waiting for
				// https://issues.apache.org/jira/browse/HADOOP-8415 to be
				// accepted
				// case GMTYPE_DOUBLE: sprintf(temp,
				// "setDouble(\"%s\", Double.parseDouble(cmd.getOptionValue(\"%s\")));",
				// argname, argname); break;
				case GMTYPE_DOUBLE:
					temp = String.format("setFloat(\"%s\", Float.parseFloat(cmd.getOptionValue(\"%s\")));", argname, argname);
					break;
				case GMTYPE_NODE:
					if (get_lib().is_node_type_int()) {
						temp = String.format("setInt(\"%s\", Integer.parseInt(cmd.getOptionValue(\"%s\")));", argname, argname);
					} else {
						temp = String.format("setLong(\"%s\", Long.parseLong(cmd.getOptionValue(\"%s\")));", argname, argname);
					}
					break;
				default:
					assert false;
					break;
				}
				Body.pushln(temp);
			}
		}
		Body.NL();
		Body.pushln("boolean isVerbose = cmd.hasOption('v') ? true : false;");
		Body.pushln("if (job.run(isVerbose)) {");
		Body.pushln("return 0;");
		Body.pushln("} else {");
		Body.pushln("return -1;");
		Body.pushln("}");
		Body.pushln("} // end of job configuration");

		Body.NL();
		Body.pushln("@Override");
		Body.pushln("public Configuration getConf() {");
		Body.pushln("return conf;");
		Body.pushln("}");

		Body.NL();
		Body.pushln("@Override");
		Body.pushln("public void setConf(Configuration conf) {");
		Body.pushln("this.conf = conf;");
		Body.pushln("}");

		Body.NL();
		Body.pushln("public static void main(final String[] args) throws Exception {");
		temp = String.format("System.exit(ToolRunner.run(new %s(), args));", proc_name);
		Body.pushln(temp);
		Body.pushln("}");
	}

	// from code generator interface
	@Override
	public void generate_proc(ast_procdef proc) {
		write_headers();

		if (!gm_main.OPTIONS.get_arg_bool(gm_argopts.GMARGFLAG_GIRAPH_VERTEX_ONLY)) {
			begin_class();
			do_generate_global_variables();
		}

		do_generate_vertex_begin();
		do_generate_master();
		do_generate_vertex_body();
		do_generate_vertex_end();

		if (!gm_main.OPTIONS.get_arg_bool(gm_argopts.GMARGFLAG_GIRAPH_VERTEX_ONLY)) {
			do_generate_input_output_formats();
			do_generate_job_configuration();
			end_class();
		}
	}

}