package backend_gps;

import static backend_gps.GPSConstants.GPS_FLAG_COMM_DEF_ASSIGN;
import static backend_gps.GPSConstants.GPS_FLAG_COMM_SYMBOL;
import static backend_gps.GPSConstants.GPS_FLAG_EDGE_DEFINED_INNER;
import static backend_gps.GPSConstants.GPS_FLAG_EDGE_DEFINING_WRITE;
import static backend_gps.GPSConstants.GPS_FLAG_IS_INTRA_MERGED_CONDITIONAL;
import static backend_gps.GPSConstants.GPS_FLAG_NODE_VALUE_INIT;
import static backend_gps.GPSConstants.GPS_FLAG_RANDOM_WRITE_SYMBOLS_FOR_SB;
import static backend_gps.GPSConstants.GPS_FLAG_SENT_BLOCK_FOR_RANDOM_WRITE_ASSIGN;
import static backend_gps.GPSConstants.GPS_FLAG_USE_EDGE_PROP;
import static backend_gps.GPSConstants.GPS_FLAG_USE_REVERSE_EDGE;
import static backend_gps.GPSConstants.GPS_INTRA_MERGE_IS_FIRST;
import static backend_gps.GPSConstants.GPS_INT_INTRA_MERGED_CONDITIONAL_NO;
import static backend_gps.GPSConstants.GPS_LIST_INTRA_MERGED_CONDITIONAL;
import static backend_gps.GPSConstants.GPS_PREPARE_STEP1;
import static backend_gps.GPSConstants.GPS_RET_VALUE;
import static backend_gps.GPSConstants.GPS_REV_NODE_ID;
import static inc.gps_apply_bb.GPS_TAG_BB_USAGE;
import frontend.gm_symtab;
import frontend.gm_symtab_entry;
import inc.BackendGenerator;
import inc.gm_expr_class;
import inc.gm_type;
import inc.gm_ops;
import inc.gm_reduce;
import inc.gm_code_writer;
import inc.gm_compile_step;
import inc.gm_ind_opt_flip_edges;
import inc.gm_ind_opt_loop_merge;
import inc.gm_ind_opt_move_propdecl;
import inc.gm_ind_opt_propagate_trivial_writes;
import inc.gm_ind_opt_remove_unused_scalar;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import ast.ast_node_type;
import ast.ast_assign;
import ast.ast_bfs;
import ast.ast_expr;
import ast.ast_expr_builtin;
import ast.ast_field;
import ast.ast_foreach;
import ast.ast_id;
import ast.ast_if;
import ast.ast_node;
import ast.ast_nop;
import ast.ast_procdef;
import ast.ast_return;
import ast.ast_sent;
import ast.ast_sentblock;
import ast.ast_typedecl;
import ast.ast_vardecl;
import ast.ast_while;
import backend_cpp.gm_cpp_opt_defer;

import common.gm_errors_and_warnings;
import common.gm_apply_compiler_stage;
import common.gm_builtin_def;
import common.gm_error;
import common.gm_main;
import common.gm_reproduce;

//-----------------------------------------------------------------
// interface for graph library Layer
//-----------------------------------------------------------------
// state number,
// begin sentence
// is pararell
public class gm_gps_gen extends BackendGenerator {

	protected LinkedList<gm_compile_step> opt_steps = new LinkedList<gm_compile_step>();
	protected LinkedList<gm_compile_step> gen_steps = new LinkedList<gm_compile_step>();

	protected String dname = null;
	protected String fname = null;
	protected gm_code_writer Body = new gm_code_writer();
	protected File f_body = null;
	protected PrintStream ps_body;
	private gm_gpslib glib = new gm_gpslib(this); // graph library

	public boolean _is_master_gen;
	public boolean _is_receiver_gen;

	public gm_gps_gen() {
		super();
		init_opt_steps();
		init_gen_steps();
	}

	@Override
	public void dispose() {
		close_output_files();
		dname = null;
		fname = null;
	}

	@Override
	public void setTargetDir(String d) {
		if (dname != null)
			System.out.printf("%s = \n", dname);
		assert d != null;
		dname = d;
	}

	@Override
	public void setFileName(String f) {
		assert f != null;
		fname = f;
	}

	public final String getFileName() {
		return fname;
	}

	@Override
	public boolean do_local_optimize_lib() {
		return get_lib().do_local_optimize();
	}

	@Override
	public boolean do_local_optimize() {
		// -----------------------------------
		// [TODO]
		// currently, there should be one and only one top-level procedure
		// -----------------------------------
		if (gm_main.FE.get_num_procs() != 1) {
			gm_error.gm_backend_error(gm_errors_and_warnings.GM_ERROR_GPS_NUM_PROCS, "");
			return false;
		}

		// -----------------------------------
		// prepare backend information struct
		// -----------------------------------
		gm_main.FE.prepare_proc_iteration();
		ast_procdef p;
		while ((p = gm_main.FE.get_next_proc()) != null) {
			gm_main.FE.get_proc_info(p).set_be_info(new gm_gps_beinfo(p));
		}

		// -----------------------------------
		// Now apply all the steps
		// -----------------------------------
		return gm_apply_compiler_stage.gm_apply_compiler_stage(get_opt_steps());
	}

	@Override
	public boolean do_generate() {

		gm_main.FE.prepare_proc_iteration();
		ast_procdef proc = gm_main.FE.get_next_proc();

		// Check whether procedure name is the same as the filename
		if (!proc.get_procname().get_genname().equals(fname)) {
			gm_error.gm_backend_error(gm_errors_and_warnings.GM_ERROR_GPS_PROC_NAME, proc.get_procname().get_genname(), fname);
			return false;
		}

		if (!open_output_files())
			return false;

		boolean b = gm_apply_compiler_stage.gm_apply_compiler_stage(get_gen_steps());

		close_output_files();

		return b;
	}

	public gm_gpslib get_lib() {
		return glib;
	}

	public void print_basicblock() {
		gm_print_bb_t T = new gm_print_bb_t();
		gm_apply_compiler_stage.gm_apply_all_proc(T);
	}

	public void init_opt_steps() {
		LinkedList<gm_compile_step> L = get_opt_steps();
		L.addLast(gm_cpp_opt_defer.get_factory());
		L.addLast(gm_gps_opt_transform_bfs.get_factory());
		L.addLast(gm_gps_opt_edge_iteration.get_factory()); // expand edge
															// iteration
		L.addLast(gm_ind_opt_propagate_trivial_writes.get_factory());
		L.addLast(gm_ind_opt_remove_unused_scalar.get_factory());
		L.addLast(gm_ind_opt_move_propdecl.get_factory());
		L.addLast(gm_gps_opt_simplify_expr1.get_factory());
		L.addLast(gm_gps_opt_edge_iteration.get_factory());
		// L.push_back(GM_COMPILE_STEP_FACTORY(gm_gps_opt_find_nested_loops_test));

		L.addLast(gm_gps_opt_insert_temp_property.get_factory());
		L.addLast(gm_gps_opt_split_loops_for_flipping.get_factory());
		L.addLast(gm_ind_opt_flip_edges.get_factory());

		L.addLast(gm_ind_opt_move_propdecl.get_factory());
		L.addLast(gm_gps_opt_remove_master_random_write.get_factory());
		L.addLast(gm_ind_opt_loop_merge.get_factory());

		L.addLast(gm_gps_opt_check_synthesizable.get_factory());
	}

	public void init_gen_steps() {
		LinkedList<gm_compile_step> L = get_gen_steps();
		// no more change of AST at this point
		L.addLast(gm_gps_opt_check_reverse_edges.get_factory());
		L.addLast(gm_gps_new_check_depth_two.get_factory());
		L.addLast(gm_gps_new_analyze_scope_sent_var.get_factory());
		L.addLast(gm_gps_new_analyze_scope_rhs_lhs.get_factory());
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

		L.addLast(gm_gps_gen_class.get_factory());
	}

	protected final LinkedList<gm_compile_step> get_opt_steps() {
		return opt_steps;
	}

	protected final LinkedList<gm_compile_step> get_gen_steps() {
		return gen_steps;
	}

	// ----------------------------------
	// stages in backend gen
	// ----------------------------------

	public void write_headers() {
		ast_procdef proc = gm_main.FE.get_current_proc();

		Body.pushlnf("package gps.examples.gm.%s;", proc.get_procname().get_genname()); // hardcoded
		get_lib().generate_headers(Body);
		Body.NL();

	}

	public void begin_class() {
		ast_procdef proc = gm_main.FE.get_current_proc();
		Body.push("public class ");
		Body.push(proc.get_procname().get_genname());
		Body.push("{");
		Body.NL();
		Body.NL();
	}

	public void end_class() {
		Body.pushln("}");
	}

	public boolean open_output_files() {

		assert dname != null;
		assert fname != null;

		String temp = String.format("%s/%s.java", dname, fname);
		f_body = new File(temp);
		try {
			FileOutputStream fos = new FileOutputStream(f_body);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			ps_body = new PrintStream(bos);
		} catch (FileNotFoundException e) {
			gm_error.gm_backend_error(gm_errors_and_warnings.GM_ERROR_FILEWRITE_ERROR, temp);
			return false;
		}
		Body.setOutputFile(ps_body);

		get_lib().set_code_writer(Body);
		return true;
	}

	public void close_output_files() {
		if (f_body != null) {
			Body.flush();
			ps_body.close();
			f_body = null;
		}
	}

	// void do_generate_main();
	// bool do_merge_msg_information();
	public void do_generate_master() {
		do_generate_shared_variables_keys();
		set_master_generate(true);
		do_generate_master_class();
		do_generate_master_scalar();
		do_generate_master_states();
		Body.pushln("}"); // finish master class
		Body.NL();

	}

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
			Body.pushlnf("case %d: _master_state_%d(); break;", id, id);
		}
		Body.pushln("}");
		Body.pushln("} while (!_master_should_start_workers && !_master_should_finish);");
		Body.NL();
		Body.pushln("}");
		Body.NL();

		Body.pushln("//@ Override");
		Body.pushln("public void compute(int superStepNo) {");

		Body.pushln("_master_state_machine();");
		Body.NL();

		Body.pushln("if (_master_should_finish) { ");
		Body.pushln("// stop the system ");
		Body.pushln("this.continueComputation = false;");
		Body.pushln("return;");
		Body.pushln("}");
		Body.NL();

		Body.pushln("if (_master_should_start_workers) { ");
		Body.pushln(" // start workers with state _master_state");
		Body.pushln("}");
		// Body.pushln("super.compute(superStepNo);");
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

	public void do_generate_master_class() {
		ast_procdef proc = gm_main.FE.get_current_proc();

		// --------------------------------------------------------------------
		// create master class
		// --------------------------------------------------------------------

		Body.pushlnf("public static class %sMaster extends Master {", proc.get_procname().get_genname());
		Body.pushln("// Control fields");
		boolean prep = gm_main.FE.get_current_proc_info().find_info_bool(GPS_FLAG_USE_REVERSE_EDGE);
		Body.pushlnf("private int     _master_state                = %d;", !prep ? 0 : GPS_PREPARE_STEP1);
		Body.pushlnf("private int     _master_state_nxt            = %d;", !prep ? 0 : GPS_PREPARE_STEP1);
		Body.pushln("private boolean _master_should_start_workers = false;");
		Body.pushln("private boolean _master_should_finish        = false;");
		Body.NL();

		// --------------------------------------------------------------------
		// constructor
		// (with command-line argument parsing)
		// --------------------------------------------------------------------
		Body.pushlnf("public %sMaster (CommandLine line) {", proc.get_procname().get_genname());

		Body.pushln("// parse command-line arguments (if any)");
		Body.pushln("HashMap<String,String> arg_map = new HashMap<String,String>();");
		Body.pushln("gps.node.Utils.parseOtherOptions(line, arg_map);");
		Body.NL();

		// Iterate symbol table and
		gm_symtab args = proc.get_symtab_var();
		assert args != null;
		HashSet<gm_symtab_entry> syms = args.get_entries();
		for (gm_symtab_entry s : syms) {
			// input argument
			if (!s.getType().is_primitive() && (!s.getType().is_node()))
				continue;
			if (s.isReadable()) {
				Body.pushlnf("if (arg_map.containsKey(\"%s\")) {", s.getId().get_genname());
				Body.pushlnf("String s = arg_map.get(\"%s\");", s.getId().get_genname());
				Body.pushf("%s = ", s.getId().get_genname());
				switch (s.getType().getTypeSummary()) {
				case GMTYPE_BOOL:
					Body.pushln("Boolean.parseBoolean(s);");
					break;
				case GMTYPE_INT:
					Body.pushln("Integer.parseInt(s);");
					break;
				case GMTYPE_LONG:
					Body.pushln("Long.parseLong(s);");
					break;
				case GMTYPE_FLOAT:
					Body.pushln("Float.parseFloat(s);");
					break;
				case GMTYPE_DOUBLE:
					Body.pushln("Double.parseDouble(s);");
					break;
				case GMTYPE_NODE:
					if (get_lib().is_node_type_int())
						Body.pushln("Integer.parseInt(s);");
					else
						Body.pushln("Long.parseLong(s);");
					break;
				default:
					assert false;
					break;
				}
				Body.pushln("}");
			}
		}
		Body.pushln("}");
		Body.NL();

		// --------------------------------------------------------------------
		// A method that saves final output values
		// --------------------------------------------------------------------
		Body.pushln("//save output");
		Body.pushln("public void writeOutput(BufferedWriter bw) throws IOException {");
		ast_typedecl t = proc.get_return_type();
		if ((t != null) && (!t.is_void())) {
			Body.pushlnf("bw.write(\"%s:\\t\" + %s + \"\\n\");", GPS_RET_VALUE, GPS_RET_VALUE);
		}
		for (gm_symtab_entry s : syms) {
			// output arguments
			if (!s.getType().is_primitive())
				continue;
			if (s.isWriteable()) {
				Body.pushlnf("bw.write(\"%s:\\t\" + %s + \"\\n\");", s.getId().get_genname(), s.getId().get_genname());
			}
		}
		Body.pushln("}");
		Body.NL();
	}

	public void do_generate_master_scalar() {
		Body.pushln("//----------------------------------------------------------");
		Body.pushln("// Scalar Variables ");
		Body.pushln("//----------------------------------------------------------");

		gm_gps_beinfo info = (gm_gps_beinfo) gm_main.FE.get_current_backend_info();
		HashSet<gm_symtab_entry> scalar = info.get_scalar_symbols();

		for (gm_symtab_entry e : scalar) {
			gps_syminfo syminfo = (gps_syminfo) e.find_info(GPS_TAG_BB_USAGE);

			// printf("%s\n", e->getId()->get_genname());
			if (!syminfo.is_used_in_master() && !syminfo.is_used_in_vertex() && !syminfo.is_argument()) {
				// printf("%s is not used in master\n",
				// e->getId()->get_genname());
				continue;
			}
			// if (!syminfo->is_used_in_master() && !syminfo->is_argument())
			// continue;
			// if (!syminfo->is_used_in_master() &&
			// !syminfo->is_used_in_vertex() && !syminfo->is_argument())
			// continue;

			Body.pushlnf("private %s %s;", get_type_string(e.getType(), true), e.getId().get_genname());
		}

		ast_procdef proc = gm_main.FE.get_current_proc();
		ast_typedecl t = proc.get_return_type();
		if ((t != null) && (!t.is_void())) {
			Body.pushlnf("private %s %s; // the final return value of the procedure", get_type_string(t, true), GPS_RET_VALUE);
		}

		// Intra-Loop Merging
		if (proc.has_info(GPS_LIST_INTRA_MERGED_CONDITIONAL)) {
			LinkedList<Object> L = proc.get_info_list(GPS_LIST_INTRA_MERGED_CONDITIONAL);
			for (Object obj : L) {
				gm_gps_basic_block bb = (gm_gps_basic_block) obj;
				Body.pushlnf("private boolean %s%d = true;", GPS_INTRA_MERGE_IS_FIRST, bb.get_id());
			}
		}

		Body.NL();
	}

	public void do_generate_master_state_body(gm_gps_basic_block b) {
		int id = b.get_id();
		gm_gps_bbtype type = b.get_type();

		Body.pushlnf("private void _master_state_%d() {", id);
		Body.pushln("/*------");
		Body.flush();
		b.reproduce_sents();
		Body.pushln("-----*/");
		Body.pushlnf("System.out.println(\"Running _master_state %d\");", id);
		if (type == gm_gps_bbtype.GM_GPS_BBTYPE_BEGIN_VERTEX) {

			// generate Broadcast
			do_generate_scalar_broadcast_send(b);
			get_lib().generate_broadcast_state_master("_master_state", Body);
			if (b.find_info_bool(GPS_FLAG_IS_INTRA_MERGED_CONDITIONAL)) {
				int cond_bb_no = b.find_info_int(GPS_INT_INTRA_MERGED_CONDITIONAL_NO);
				String temp = String.format("%s%d", GPS_INTRA_MERGE_IS_FIRST, cond_bb_no);
				get_lib().generate_broadcast_isFirst_master(temp, Body);
			}
			Body.NL();

			// generate next statement
			assert b.get_num_exits() == 1;
			int n = b.get_nth_exit(0).get_id();
			Body.pushlnf("_master_state_nxt = %d;", n);
			Body.pushln("_master_should_start_workers = true;");
		} else if (type == gm_gps_bbtype.GM_GPS_BBTYPE_SEQ) {
			if (b.is_after_vertex()) {
				assert b.get_num_entries() == 1;
				do_generate_scalar_broadcast_receive(b);
			}

			// define local variables
			HashMap<gm_symtab_entry, gps_syminfo> symbols = b.get_symbols();
			for (gm_symtab_entry sym : symbols.keySet()) {
				gps_syminfo local_info = symbols.get(sym);
				// TODO: why is sym->isArgument() != local_info->is_argument()?
				if (!local_info.is_scalar() || sym.isArgument())
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
					Body.pushlnf("if (!%s%d) {", GPS_INTRA_MERGE_IS_FIRST, cond_bb_no);
				}

				generate_sent(s);

				if (s.find_info_bool(GPS_FLAG_IS_INTRA_MERGED_CONDITIONAL)) {
					Body.pushln("}");
				}

				s = b.get_next();
			}

			if (b.get_num_exits() == 0) {
				Body.NL();
				Body.pushln("_master_should_finish = true;");
			} else {
				int n = b.get_nth_exit(0).get_id();
				Body.pushlnf("_master_state_nxt = %d;", n);
			}
		} else if (type == gm_gps_bbtype.GM_GPS_BBTYPE_IF_COND) {

			Body.push("boolean _expression_result = ");

			// generate sentences
			ast_sent s = b.get_1st_sent();
			assert s != null;
			assert s.get_nodetype() == ast_node_type.AST_IF;
			ast_if i = (ast_if) s;
			generate_expr(i.get_cond());
			Body.pushln(";");

			Body.pushlnf("if (_expression_result) _master_state_nxt = %d;\nelse _master_state_nxt = %d;", b.get_nth_exit(0).get_id(), b.get_nth_exit(1)
					.get_id());
		} else if (type == gm_gps_bbtype.GM_GPS_BBTYPE_WHILE_COND) {
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

			Body.pushlnf("if (_expression_result) _master_state_nxt = %d;\nelse _master_state_nxt = %d;\n", b.get_nth_exit(0).get_id(),
					b.get_nth_exit(1).get_id()); // exit - continue while

			if (b.find_info_bool(GPS_FLAG_IS_INTRA_MERGED_CONDITIONAL)) {
				Body.pushlnf("if (!_expression_result) %s%d=true; // reset is_first\n\n", GPS_INTRA_MERGE_IS_FIRST, b.get_id());
			}

		} else if ((type == gm_gps_bbtype.GM_GPS_BBTYPE_PREPARE1) || (type == gm_gps_bbtype.GM_GPS_BBTYPE_PREPARE2)) {

			// generate Broadcast
			do_generate_scalar_broadcast_send(b);
			get_lib().generate_broadcast_state_master("_master_state", Body);

			Body.pushln("// Preparation Step;");
			assert b.get_num_exits() == 1;
			int n = b.get_nth_exit(0).get_id();
			Body.pushlnf("_master_state_nxt = %d;", n);
			Body.pushln("_master_should_start_workers = true;");
		} else if (type == gm_gps_bbtype.GM_GPS_BBTYPE_MERGED_TAIL) {
			Body.pushln("// Intra-Loop Merged (tail)");
			int source_id = b.find_info_int(GPS_INT_INTRA_MERGED_CONDITIONAL_NO);
			Body.pushlnf("if (%s%d) _master_state_nxt = %d;", GPS_INTRA_MERGE_IS_FIRST, source_id, b.get_nth_exit(0).get_id());
			Body.pushlnf("else _master_state_nxt = %d;", b.get_nth_exit(1).get_id());
			Body.pushlnf("%s%d = false;\n", GPS_INTRA_MERGE_IS_FIRST, source_id);
		} else if (type == gm_gps_bbtype.GM_GPS_BBTYPE_MERGED_IF) {
			Body.pushln("// Intra-Loop Merged (head)");
			int source_id = b.find_info_int(GPS_INT_INTRA_MERGED_CONDITIONAL_NO);
			Body.pushlnf("if (%s%d) _master_state_nxt = %d;", GPS_INTRA_MERGE_IS_FIRST, source_id, b.get_nth_exit(0).get_id());
			Body.pushlnf("else _master_state_nxt = %d;", b.get_nth_exit(1).get_id());
			String temp = String.format("%s%d = false;\n", GPS_INTRA_MERGE_IS_FIRST, source_id);
		} else {
			assert false;
		}

		Body.pushln("}"); // end of state function
	}

	public void do_generate_scalar_broadcast_send(gm_gps_basic_block b) {
		get_lib().generate_broadcast_prepare(Body);

		// check if scalar variable is used inside the block
		HashMap<gm_symtab_entry, gps_syminfo> syms = b.get_symbols();
		for (gm_symtab_entry key : syms.keySet()) {
			gps_syminfo local_info = syms.get(key);
			gps_syminfo global_info = (gps_syminfo) key.find_info(GPS_TAG_BB_USAGE);
			if (!global_info.is_scalar())
				continue;
			if (local_info.is_used_as_reduce()) {
				gm_reduce reduce_type = local_info.get_reduce_type();

				// printf("being used as reduce :%s\n",
				// I->first->getId()->get_genname());
				get_lib().generate_broadcast_reduce_initialize_master(key.getId(), Body, reduce_type,
						reduce_type.get_reduce_base_value(key.getType().getTypeSummary()));
				// [TODO] global argmax
				continue;
			}
			if (!global_info.is_used_in_master() && !global_info.is_argument())
				continue;
			if (local_info.is_used_as_rhs()) {
				// create a broad cast variable
				get_lib().generate_broadcast_send_master(key.getId(), Body);
			}
		}
	}

	public void do_generate_scalar_broadcast_receive(gm_gps_basic_block b) {
		assert b.get_num_entries() == 1;
		gm_gps_basic_block pred = b.get_nth_entry(0);
		assert pred.is_vertex();

		// check if scalar variable is modified inside the block
		HashMap<gm_symtab_entry, gps_syminfo> syms = pred.get_symbols();
		for (gm_symtab_entry key : syms.keySet()) {
			gps_syminfo local_info = syms.get(key);
			gps_syminfo global_info = (gps_syminfo) key.find_info(GPS_TAG_BB_USAGE);
			if (!global_info.is_scalar())
				continue;
			if (!global_info.is_used_in_master())
				continue;
			if (local_info.is_used_as_lhs() || local_info.is_used_as_reduce()) {
				// create a broad cast variable
				get_lib().generate_broadcast_receive_master(key.getId(), Body, local_info.get_reduce_type());
			}
		}
	}

	public void do_generate_shared_variables_keys() {
		Body.pushln("// Keys for shared_variables ");
		gm_gps_beinfo info = (gm_gps_beinfo) gm_main.FE.get_current_backend_info();
		HashSet<gm_symtab_entry> scalar = info.get_scalar_symbols();

		for (gm_symtab_entry sym : scalar) {
			gps_syminfo syminfo = (gps_syminfo) sym.find_info(GPS_TAG_BB_USAGE);
			assert syminfo != null;

			/*
			 * printf("%s, used_in_vertex = %c, used_in_master =%c\n",
			 * sym->getId()->get_orgname(), syminfo->is_used_in_vertex() ? 'Y' :
			 * 'N', syminfo->is_used_in_master() ? 'Y' : 'N');
			 */

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

	public void do_generate_vertex() {
		set_master_generate(false);
		do_generate_vertex_class();
		do_generate_vertex_property_class(false);

		if (gm_main.FE.get_current_proc().find_info_bool(GPS_FLAG_USE_EDGE_PROP))
			do_generate_vertex_property_class(true);

		do_generate_message_class();
	}

	public void do_generate_vertex_property_class(boolean is_edge_prop) {
		Body.pushln("//----------------------------------------------");
		if (is_edge_prop)
			Body.pushln("// Edge Property Class");
		else
			Body.pushln("// Vertex Property Class");
		Body.pushln("//----------------------------------------------");

		ast_procdef proc = gm_main.FE.get_current_proc();
		assert proc != null;
		Body.pushlnf("public static class %s extends MinaWritable {", is_edge_prop ? "EdgeData" : "VertexData");

		// list out property
		Body.pushln("// properties");
		gm_gps_beinfo info = (gm_gps_beinfo) gm_main.FE.get_current_backend_info();
		HashSet<gm_symtab_entry> prop = is_edge_prop ? info.get_edge_prop_symbols() : info.get_node_prop_symbols();
		for (gm_symtab_entry sym : prop) {
			// gps_syminfo* syminfo = (gps_syminfo*)
			// sym->find_info(TAG_BB_USAGE);
			Body.pushlnf("%s %s;", get_type_string(sym.getType().get_target_type(), is_master_generate()), sym.getId().get_genname());
		}

		if (gm_main.FE.get_current_proc_info().find_info_bool(GPS_FLAG_USE_REVERSE_EDGE)) {
			Body.pushlnf("%s [] %s; //reverse edges (node IDs) {should this to be marshalled?}", get_lib().is_node_type_int() ? "int" : "long",
					GPS_REV_NODE_ID);
		}

		Body.NL();
		get_lib().generate_vertex_prop_class_details(prop, Body, is_edge_prop);

		Body.pushln("} // end of data class"); // end of class
		Body.NL();

	}

	public void do_generate_vertex_class() {

		String proc_name = gm_main.FE.get_current_proc().get_procname().get_genname();
		Body.pushln("//----------------------------------------------");
		Body.pushln("// Main Vertex Class");
		Body.pushln("//----------------------------------------------");
		Body.pushlnf("public static class %sVertex", proc_name);
		Body.pushIndent();
		if (gm_main.FE.get_current_proc().find_info_bool(GPS_FLAG_USE_EDGE_PROP)) {
			Body.pushlnf("extends Vertex< %s.VertexData, %s.EdgeData, %s.MessageData > {", proc_name, proc_name, proc_name);
		} else {
			Body.pushlnf("extends NullEdgeVertex< %s.VertexData, %s.MessageData > {", proc_name, proc_name);
		}
		Body.popIndent();

		do_generate_vertex_constructor();
		do_generate_vertex_get_initial_state_method();
		do_generate_vertex_states();

		Body.pushln("} // end of Vertex");
		Body.NL();

		Body.pushln("//----------------------------------------------");
		Body.pushln("// Factory Class");
		Body.pushln("//----------------------------------------------");
		Body.pushlnf("public static class %sVertexFactory", proc_name);
		Body.pushIndent();
		if (gm_main.FE.get_current_proc().find_info_bool(GPS_FLAG_USE_EDGE_PROP)) {
			Body.pushlnf("extends VertexFactory< %s.VertexData, %s.EdgeData, %s.MessageData > {", proc_name, proc_name, proc_name);
		} else {
			Body.pushlnf("extends NullEdgeVertexFactory< %s.VertexData, %s.MessageData > {", proc_name, proc_name);
		}
		Body.popIndent();
		Body.pushln("@Override");
		if (gm_main.FE.get_current_proc().find_info_bool(GPS_FLAG_USE_EDGE_PROP)) {
			Body.pushlnf("public Vertex< %s.VertexData, %s.EdgeData, %s.MessageData > newInstance(CommandLine line) {", proc_name, proc_name, proc_name);
		} else {
			Body.pushlnf("public NullEdgeVertex< %s.VertexData, %s.MessageData > newInstance(CommandLine line) {", proc_name, proc_name);
		}
		Body.pushlnf("return new %sVertex(line);", proc_name);
		Body.pushln("}");

		Body.pushln("} // end of VertexFactory");
		Body.NL();

	}

	public void do_generate_vertex_constructor() {

		String proc_name = gm_main.FE.get_current_proc().get_procname().get_genname();
		Body.NL();
		Body.pushlnf("public %sVertex(CommandLine line) {", proc_name);
		Body.pushln("// todo: how to tell if we should parse the command lines or not");
		Body.pushln("// --> no need. master will parse the command line and sent it to the workers");
		Body.pushln("}");
	}

	public void do_generate_vertex_get_initial_state_method() {
		Body.NL();
		Body.pushln("@Override");
		Body.pushln("public VertexData getInitialValue(int id) {");
		Body.pushln("return new VertexData();");
		Body.pushln("}");
	}

	public void do_generate_message_class() {
		Body.pushln("//----------------------------------------------");
		Body.pushln("// Message Data ");
		Body.pushln("//----------------------------------------------");

		ast_procdef proc = gm_main.FE.get_current_proc();
		assert proc != null;
		gm_gps_beinfo info = (gm_gps_beinfo) gm_main.FE.get_current_backend_info();
		Body.pushlnf("public static class MessageData extends MinaWritable {");

		if (info.is_single_message()) {
			Body.pushln("//single message type; argument ignored");
			Body.pushln("public MessageData(byte type) {}");
		} else {
			Body.pushln("byte m_type;");
			Body.pushln("public MessageData(byte type) {m_type = type;}");
		}
		Body.NL();

		do_generate_message_class_default_constructor();

		get_lib().generate_message_class_details(info, Body);

		Body.pushln("} // end of message-data");
		Body.NL();
	}

	public void do_generate_message_class_default_constructor() {
		Body.NL();
		Body.pushln("public MessageData() {");
		Body.pushln("// default constructor that is required for constructing a " + "representative instance for IncomingMessageStore.");
		Body.pushln("}");
	}

	public void do_generate_vertex_states() {

		String proc_name = gm_main.FE.get_current_proc().get_procname().get_genname();
		Body.NL();
		Body.pushln("@Override");
		Body.pushlnf("public void compute(Iterable<%s.MessageData> _msgs, int _superStepNo) {", proc_name);
		Body.pushln("// todo: is there any way to get this value quickly?");
		Body.pushln("// (this can be done by the first node and saved into a static field)");
		get_lib().generate_receive_state_vertex("_state_vertex", Body);

		Body.pushln("switch(_state_vertex) { ");
		gm_gps_beinfo info = (gm_gps_beinfo) gm_main.FE.get_current_backend_info();
		LinkedList<gm_gps_basic_block> bb_blocks = info.get_basic_blocks();
		int cnt = 0;
		for (gm_gps_basic_block b : bb_blocks) {
			if ((!b.is_prepare()) && (!b.is_vertex()))
				continue;
			int id = b.get_id();
			Body.pushlnf("case %d: _vertex_state_%d(_msgs); break;", id, id);
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

	public void do_generate_vertex_state_body(gm_gps_basic_block b) {
		int id = b.get_id();
		gm_gps_bbtype type = b.get_type();

		Body.pushlnf("private void _vertex_state_%d(Iterable<%s.MessageData> _msgs) {", id, gm_main.FE.get_current_proc().get_procname()
				.get_genname());

		get_lib().generate_vertex_prop_access_prepare(Body);

		do_generate_vertex_state_receive_global(b);

		if (b.is_prepare()) {
			get_lib().generate_prepare_bb(Body, b);
			Body.pushln("}");
			return;
		}

		assert type == gm_gps_bbtype.GM_GPS_BBTYPE_BEGIN_VERTEX;
		boolean is_conditional = b.find_info_bool(GPS_FLAG_IS_INTRA_MERGED_CONDITIONAL);
		String cond_var = "";
		if (is_conditional)
			cond_var = String.format("%s%d", GPS_INTRA_MERGE_IS_FIRST, b.find_info_int(GPS_INT_INTRA_MERGED_CONDITIONAL_NO));

		// ---------------------------------------------------------
		// Generate Receiver Routine
		// ---------------------------------------------------------
		if (b.has_receiver()) {
			set_receiver_generate(true);
			Body.NL();

			if (is_conditional) {
				Body.pushlnf("if (!%s) {", cond_var);
			}

			Body.pushln("// Begin msg receive");
			Body.pushln("for(MessageData _msg : _msgs) {");

			LinkedList<gm_gps_comm_unit> R = b.get_receivers();
			for (gm_gps_comm_unit U : R) {
				if (U.get_type() == gm_gps_comm.GPS_COMM_NESTED) {
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
					LinkedList<ast_sent> sents = sb.get_sents();
					for (ast_sent s : sents) {
						if (s.find_info_ptr(GPS_FLAG_SENT_BLOCK_FOR_RANDOM_WRITE_ASSIGN) == sb)
							s.reproduce(0);
					}
					gm_reproduce.gm_flush_reproduce();
					Body.pushln("}");
					Body.pushln("-----*/");
					get_lib().generate_message_receive_begin(sb, U.sym, Body, b, R.size() == 1);

					for (ast_sent s : sents) {
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
					Body.pushf("if (!%s)", cond_var);
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
					// receive it from Broadcast
					generate_scalar_var_def(sym, false);
					Body.push(" = ");
					get_lib().generate_broadcast_receive_vertex(sym.getId(), Body);
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

	public void generate_scalar_var_def(gm_symtab_entry sym, boolean finish_sent) {
		// skip edge iteration
		if (sym.find_info_bool(GPS_FLAG_EDGE_DEFINED_INNER))
			return;

		assert sym.getType().is_primitive() || sym.getType().is_node_compatible();

		Body.pushf("%s %s", get_type_string(sym.getType(), is_master_generate()), sym.getId().get_genname());

		if (finish_sent)
			Body.pushln(";");
	}

	public void do_generate_job_configuration() {

		ast_procdef proc = gm_main.FE.get_current_proc();

		Body.NL();
		Body.pushln("// job description for the system");
		Body.pushln("public static class JobConfiguration extends GPSJobConfiguration {");
		Body.pushln("@Override");
		Body.pushln("public Class<?> getMasterClass() {");
		Body.pushlnf("return %sMaster.class;", proc.get_procname().get_genname());
		Body.pushln("}");

		Body.pushln("@Override");
		Body.pushln("public Class<?> getVertexClass() {");
		Body.pushlnf("return %sVertex.class;", proc.get_procname().get_genname());
		Body.pushln("}");

		Body.pushln("@Override");
		Body.pushln("public Class<?> getVertexFactoryClass() {");
		Body.pushlnf("return %sVertexFactory.class;", proc.get_procname().get_genname());
		Body.pushln("}");

		Body.pushln("@Override");
		Body.pushln("public Class<?> getVertexValueClass() {");
		Body.pushlnf("return VertexData.class;");
		Body.pushln("}");

		Body.pushln("@Override");
		Body.pushln("public Class<?> getEdgeValueClass() {");
		if (gm_main.FE.get_current_proc().find_info_bool(GPS_FLAG_USE_EDGE_PROP)) {
			Body.pushlnf("return EdgeData.class;");
		} else {
			Body.pushlnf("return NullWritable.class;");
		}
		Body.pushln("}");

		Body.pushln("@Override");
		Body.pushln("public Class<?> getMessageValueClass() {");
		// [XXX]
		Body.pushlnf("return MessageData.class;");
		Body.pushln("}");

		// check if node property value parsing is required
		if (proc.find_info_bool(GPS_FLAG_NODE_VALUE_INIT)) {
			Body.pushln("@Override");
			Body.pushln("public boolean hasVertexValuesInInput() {");
			// [XXX]
			Body.pushln("return true;");
			Body.pushln("}");
		}

		Body.pushln("}");

	}

	public final gm_code_writer get_code() {
		return Body;
	}

	// from code generator interface
	@Override
	public String get_type_string(gm_type gm_type) {
		switch (gm_type) {
		case GMTYPE_INT:
			return "int";
		case GMTYPE_LONG:
			return "long";
		case GMTYPE_FLOAT:
			return "float";
		case GMTYPE_DOUBLE:
			return "double";
		case GMTYPE_BOOL:
			return "boolean";
		case GMTYPE_NODE:
			return get_lib().is_node_type_int() ? "int" : "long";
		default:
			assert false;
			return "ERROR";
		}
	}

	public String get_type_string(ast_typedecl T, boolean is_master) {
		if (T.is_primitive() || T.is_node()) {
			return (get_type_string(T.get_typeid()));
		} else if (T.is_node_compatible()) {
			return (get_type_string(gm_type.GMTYPE_NODE));
		} else {
			assert false;
			// to be done
		}
		return "???";
	}

	public void generate_proc(ast_procdef proc) {
		write_headers();
		begin_class();
		do_generate_master();

		do_generate_vertex();

		do_generate_job_configuration();

		end_class();
	}

	@Override
	public void generate_rhs_id(ast_id i) {
		if (i.getSymInfo().getType().is_node_iterator()) {
			if (i.getSymInfo().find_info_bool(GPS_FLAG_COMM_SYMBOL)) {
				if (!this.is_receiver_generate()) {
					get_lib().generate_node_iterator_rhs(i, Body);
				} else {
					generate_lhs_id(i);
				}
			} else {
				// generate_lhs_id(i);
				get_lib().generate_node_iterator_rhs(i, Body);
			}
		} else {
			generate_lhs_id(i);
		}
	}

	@Override
	public void generate_rhs_field(ast_field f) {
		generate_lhs_field(f);
	}

	@Override
	public void generate_expr_builtin(ast_expr e) {
		ast_expr_builtin be = (ast_expr_builtin) e;
		gm_builtin_def def = be.get_builtin_def();
		LinkedList<ast_expr> ARGS = be.get_args();

		switch (def.get_method_id()) {
		case GM_BLTIN_TOP_DRAND: // rand function
		case GM_BLTIN_TOP_IRAND: // rand function
		case GM_BLTIN_GRAPH_NUM_NODES:
		case GM_BLTIN_GRAPH_RAND_NODE:
		case GM_BLTIN_NODE_DEGREE:
		case GM_BLTIN_NODE_IN_DEGREE:
			get_lib().generate_expr_builtin(be, Body, is_master_generate());
			break;

		case GM_BLTIN_TOP_LOG: // log function
			Body.push("Math.log(");
			assert ARGS.getFirst() != null;
			generate_expr(ARGS.getFirst());
			Body.push(")");
			break;
		case GM_BLTIN_TOP_EXP: // exp function
			Body.push("Math.exp(");
			assert ARGS.getFirst() != null;
			generate_expr(ARGS.getFirst());
			Body.push(")");
			break;
		case GM_BLTIN_TOP_POW: // pow function
			Body.push("Math.pow(");
			assert ARGS.getFirst() != null;
			generate_expr(ARGS.getFirst());
			Body.push(",");
			assert ARGS.getLast() != null;
			generate_expr(ARGS.getLast());
			Body.push(")");
			break;
		default:
			assert false;
			break;
		}
	}

	@Override
	public void generate_expr_minmax(ast_expr e) {
		if (e.get_optype() == gm_ops.GMOP_MIN)
			Body.push("java.math.min(");
		else if (e.get_optype() == gm_ops.GMOP_MAX) {
			Body.push("java.math.max(");
		} else {
			assert false;
		}

		generate_expr(e.get_left_op());
		Body.push(",");
		generate_expr(e.get_right_op());
		Body.push(")");
	}

	@Override
	public void generate_expr_abs(ast_expr e) {
		Body.push("Math.abs(");
		generate_expr(e.get_left_op());
		Body.push(")");
	}

	@Override
	public void generate_lhs_id(ast_id i) {
		Body.push(i.get_genname());
	}

	@Override
	public void generate_expr_inf(ast_expr e) {
		assert e.get_opclass() == gm_expr_class.GMEXPR_INF;
		gm_type t = e.get_type_summary();
		switch (t) {
		case GMTYPE_INF:
		case GMTYPE_INF_INT:
			Body.pushf("Integer.%s", e.is_plus_inf() ? "MAX_VALUE" : "MIN_VALUE"); // temporary
			break;
		case GMTYPE_INF_LONG:
			Body.pushf("Long.%s", e.is_plus_inf() ? "MAX_VALUE" : "MIN_VALUE"); // temporary
			break;
		case GMTYPE_INF_FLOAT:
			Body.pushf("Float.%s", e.is_plus_inf() ? "MAX_VALUE" : "MIN_VALUE"); // temporary
			break;
		case GMTYPE_INF_DOUBLE:
			Body.pushf("Double.%s", e.is_plus_inf() ? "MAX_VALUE" : "MIN_VALUE"); // temporary
			break;
		default:
			Body.pushf("%s", e.is_plus_inf() ? "Integer.MAX_VALUE" : "Integer.MIN_VALUE"); // temporary
			break;
		}
		return;
	}

	@Override
	public void generate_expr_nil(ast_expr e) {
		get_lib().generate_expr_nil(e, Body);
	}

	@Override
	public void generate_lhs_field(ast_field f) {
		ast_id prop = f.get_second();
		if (is_master_generate()) {
			assert false;
		} else if (is_receiver_generate()) {
			if (f.getSourceTypeSummary() == gm_type.GMTYPE_NODEITER_ALL) {
				get_lib().generate_vertex_prop_access_remote_lhs(prop, Body);
			} else if (f.get_first().getTypeInfo().is_edge()) {
				get_lib().generate_vertex_prop_access_remote_lhs_edge(prop, Body);
			} else {
				assert f.get_first().getTypeInfo().is_node_compatible();
				get_lib().generate_vertex_prop_access_lhs(prop, Body);
			}
		} // vertex generate;
		else {
			// assert(f->getSourceTypeSummary() == GMTYPE_NODEITER_ALL);
			if (f.get_first().getTypeInfo().is_edge()) {
				get_lib().generate_vertex_prop_access_lhs_edge(prop, Body);
			} else {
				assert f.get_first().getTypeInfo().is_node_compatible();
				get_lib().generate_vertex_prop_access_lhs(prop, Body);
			}
		}
	}

	@Override
	public void generate_sent_nop(ast_nop n) {
		assert false;
	}

	@Override
	public void generate_sent_reduce_assign(ast_assign a) {
		if (is_master_generate()) {
			// [to be done]
			assert false;
		}

		if (a.find_info_ptr(GPS_FLAG_SENT_BLOCK_FOR_RANDOM_WRITE_ASSIGN) != null) {
			if (!is_receiver_generate()) {
				// generate random write messaging
				get_lib().generate_message_payload_packing_for_random_write(a, Body);
				return;
			}
		}

		if (a.is_target_scalar()) {
			// check target is global
			{
				get_lib().generate_reduce_assign_vertex(a, Body, a.get_reduce_type());
			}
			return;
		}

		// --------------------------------------------------
		// target is vertex-driven
		// reduction now became normal read/write
		// --------------------------------------------------
		if (a.is_argminmax_assign()) {
			assert (a.get_reduce_type() == gm_reduce.GMREDUCE_MIN) || (a.get_reduce_type() == gm_reduce.GMREDUCE_MAX);

			Body.push("if (");
			if (a.is_target_scalar())
				generate_rhs_id(a.get_lhs_scala());
			else
				generate_rhs_field(a.get_lhs_field());

			if (a.get_reduce_type() == gm_reduce.GMREDUCE_MIN)
				Body.push(" > ");
			else
				Body.push(" < ");

			generate_expr(a.get_rhs());
			Body.pushln(") {");
			if (a.is_target_scalar())
				generate_lhs_id(a.get_lhs_scala());
			else
				generate_lhs_field(a.get_lhs_field());
			Body.push(" = ");
			generate_expr(a.get_rhs());
			Body.pushln(";");

			LinkedList<ast_node> lhs_list = a.get_lhs_list();
			LinkedList<ast_expr> rhs_list = a.get_rhs_list();
			Iterator<ast_expr> J;
			J = rhs_list.iterator();
			for (ast_node n : lhs_list) {
				if (n.get_nodetype() == ast_node_type.AST_ID) {
					generate_lhs_id((ast_id) n);
				} else {
					generate_lhs_field((ast_field) n);
				}
				Body.push(" = ");
				generate_expr(J.next());
				Body.pushln(";");
			}

			Body.pushln("}");
		} else {
			if (a.is_target_scalar()) {
				generate_lhs_id(a.get_lhs_scala());
			} else {
				generate_lhs_field(a.get_lhs_field());
			}

			Body.push(" = ");

			if ((a.get_reduce_type() == gm_reduce.GMREDUCE_PLUS) || (a.get_reduce_type() == gm_reduce.GMREDUCE_MULT)
					|| (a.get_reduce_type() == gm_reduce.GMREDUCE_AND) || (a.get_reduce_type() == gm_reduce.GMREDUCE_OR)) {
				if (a.is_target_scalar())
					generate_rhs_id(a.get_lhs_scala());
				else
					generate_rhs_field(a.get_lhs_field());

				switch (a.get_reduce_type()) {
				case GMREDUCE_PLUS:
					Body.push(" + (");
					break;
				case GMREDUCE_MULT:
					Body.push(" * (");
					break;
				case GMREDUCE_AND:
					Body.push(" && (");
					break;
				case GMREDUCE_OR:
					Body.push(" || (");
					break;
				default:
					assert false;
					break;
				}
				generate_expr(a.get_rhs());
				Body.pushln(");");
			} else if ((a.get_reduce_type() == gm_reduce.GMREDUCE_MIN) || (a.get_reduce_type() == gm_reduce.GMREDUCE_MAX)) {
				if (a.get_reduce_type() == gm_reduce.GMREDUCE_MIN)
					Body.push("java.lang.Min(");
				else
					Body.push("java.lang.Max(");

				if (a.is_target_scalar())
					generate_rhs_id(a.get_lhs_scala());
				else
					generate_rhs_field(a.get_lhs_field());
				Body.push(",");
				generate_expr(a.get_rhs());

				Body.pushln(");");
			} else {
				assert false;
			}
		}
	}

	@Override
	public void generate_sent_defer_assign(ast_assign a) {
		assert false;
	}

	@Override
	public void generate_sent_vardecl(ast_vardecl a) {
		assert false;
	}

	@Override
	public void generate_sent_bfs(ast_bfs a) {
		assert false;
	}

	@Override
	public void generate_sent_foreach(ast_foreach fe) {
		// must be a sending foreach
		assert fe.get_iter_type().is_iteration_on_out_neighbors() || fe.get_iter_type().is_iteration_on_in_neighbors()
				|| fe.get_iter_type().is_iteration_on_down_neighbors();

		get_lib().generate_message_send(fe, Body);
	}

	@Override
	public void generate_sent_return(ast_return r) {
		if (r.get_expr() != null) {
			Body.push(GPS_RET_VALUE);
			Body.push(" = ");
			generate_expr(r.get_expr());
			Body.pushln(";");
		}
	}

	@Override
	public void generate_sent_assign(ast_assign a) {
		// normal assign
		if (is_master_generate()) {
			generate_sent_assign(a);
			return;
		}

		// edge defining write
		if (a.find_info_bool(GPS_FLAG_EDGE_DEFINING_WRITE)) {
			return;
		}

		if (is_receiver_generate()) {
			if (!a.is_target_scalar() && a.get_lhs_field().get_first().getSymInfo().find_info_bool(GPS_FLAG_EDGE_DEFINED_INNER)) {
				return;
			}

			if (a.find_info_bool(GPS_FLAG_COMM_DEF_ASSIGN)) // defined
															// in
															// re-write
															// rhs
			{
				return;
			}
		}

		// vertex or receiver generate
		if (a.find_info_ptr(GPS_FLAG_SENT_BLOCK_FOR_RANDOM_WRITE_ASSIGN) != null) {
			if (!is_receiver_generate()) {
				// generate random write messaging
				get_lib().generate_message_payload_packing_for_random_write(a, Body);
				return;
			}
		}

		if (a.is_target_scalar()) {
			ast_id i = a.get_lhs_scala();

			gps_syminfo syminfo = (gps_syminfo) i.getSymInfo().find_info(GPS_TAG_BB_USAGE);

			// normal assign
			if (!syminfo.is_scoped_global()) {
				generate_sent_assign(a);
				return;
			} else {
				// write to global scalar
				get_lib().generate_reduce_assign_vertex(a, Body, gm_reduce.GMREDUCE_NULL);
				return;

				// [TO BE DONE]
				// printf("need to implement: write to global %s\n",
				// i->get_genname());
				// assert(false);
			}
		} else {
			generate_sent_assign(a);
		}
	}

	@Override
	public void generate_sent_block(ast_sentblock sb, boolean need_brace) {
		LinkedList<ast_sent> sents = sb.get_sents();

		if (need_brace)
			Body.pushln("{");
		if (sb.has_info_set(GPS_FLAG_RANDOM_WRITE_SYMBOLS_FOR_SB)) {
			HashSet<Object> S = sb.get_info_set(GPS_FLAG_RANDOM_WRITE_SYMBOLS_FOR_SB);
			for (Object obj : S) {
				gm_symtab_entry sym = (gm_symtab_entry) obj;
				get_lib().generate_message_create_for_random_write(sb, sym, Body);
			}
			Body.NL();
		}

		for (ast_sent s : sents) {
			generate_sent(s);
		}

		if (sb.has_info_set(GPS_FLAG_RANDOM_WRITE_SYMBOLS_FOR_SB)) {
			Body.NL();

			HashSet<Object> S = sb.get_info_set(GPS_FLAG_RANDOM_WRITE_SYMBOLS_FOR_SB);
			for (Object obj : S) {
				gm_symtab_entry sym = (gm_symtab_entry) obj;
				get_lib().generate_message_send_for_random_write(sb, sym, Body);
			}
		}

		if (need_brace)
			Body.pushln("}");

	}

	public final void set_master_generate(boolean b) {
		_is_master_gen = b;
	}

	public final boolean is_master_generate() {
		return _is_master_gen;
	}

	public final void set_receiver_generate(boolean b) {
		_is_receiver_gen = b;
	}

	public final boolean is_receiver_generate() {
		return _is_receiver_gen;
	}

}