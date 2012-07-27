package frontend;

import inc.gm_backend_info;
import inc.gm_compile_step;
import inc.gm_procinfo;

import java.util.LinkedList;

import tangible.Extern;
import ast.ast_idlist;
import ast.ast_procdef;
import ast.ast_sentblock;

import common.GlobalMembersGm_apply_compiler_stage;
import common.GlobalMembersGm_error;
import common.GlobalMembersGm_traverse;
import common.gm_vocabulary;

//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP(CLASS, DESC) class CLASS : public gm_compile_step { private: CLASS() {set_description(DESC);}public: virtual void process(ast_procdef*p); virtual gm_compile_step* get_instance(){return new CLASS();} static gm_compile_step* get_factory(){return new CLASS();} };
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP_FACTORY(CLASS) CLASS::get_factory()

public class gm_frontend {

	// ------------------------------------------------
	// frontend module implementation
	// ------------------------------------------------
	public gm_frontend() {
		this.curr_proc = null;
		this.curr_idlist = null;
		this.vardecl_removed = false;
		init_steps();
	}

	public void dispose() {
		// delete all procs
		for (int i = 0; i < (int) procs.size(); i++) {
			if (procs.get(i) != null)
				procs.get(i).dispose();
		}
	}

	// ----------------------------------------------------
	// interface to parser
	// ----------------------------------------------------
	public final int start_parse(String fname) {
		// start lexer
		if (Extern.GM_start_parse(fname) == 0) {
			System.out.printf("Error in loading %s\n", fname);
			return 0;
		}

		// start parser
		return Extern.yyparse();
	}

	// void clean_up(); // clean-up intermediate structures (for iterative mode)

	// procedure definition
	public final void start_new_procdef(ast_procdef a) {
		curr_proc = a;
	}

	public final ast_procdef get_current_procdef() {
		return curr_proc;
	}

	public final void finish_procdef() {
		procs.add(curr_proc);
		curr_proc = null;
	}

	public final void start_sentblock(ast_sentblock b) {
		blocks.addLast(b);
	}

	public final ast_sentblock get_current_sentblock() {
		return blocks.getLast();
	}

	public final void end_sentblock() {
		blocks.removeLast();
	}

	public final ast_idlist get_current_idlist() {
		if (curr_idlist == null)
			curr_idlist = new ast_idlist();
		return curr_idlist;
	}

	public final void finish_id_comma_list() {
		curr_idlist = null;
	}

	// ----------------------------------
	// tempoprary fields used during parsing
	// ----------------------------------
	private java.util.LinkedList<ast_sentblock> blocks = new java.util.LinkedList<ast_sentblock>();
	private ast_procdef curr_proc;
	private ast_idlist curr_idlist;

	// -------------------------------------------------------
	// Interface to compiler main
	public final boolean do_local_frontend_process() {
		// create information objects for all procedures
		for (ast_procdef p : procs) {
			proc_info.put(p, new gm_procinfo(p));
		}

		// now apply frontend steps
		return GlobalMembersGm_apply_compiler_stage.gm_apply_compiler_stage(local_steps);
	}

	// ----------------------------------------------------------------------------------------
	// reproduce: method implementations for ast debuggin
	// ----------------------------------------------------------------------------------------
	public final void reproduce() {

		for (ast_procdef p : procs) {
			p.reproduce(0);
		}
	}

	public final void dump_tree() {

		for (ast_procdef p : procs) {
			p.dump_tree(0);
		}
	}

	// void print_rwinfo(); // rw_analysis.cc

	// -------------------------------------------------------
	// Interface to other compiler components
	public final int get_num_procs() {
		return procs.size();
	}

	public final ast_procdef get_current_proc() {
		return _curr_proc;
	}

	public final void prepare_proc_iteration() {
		I = procs.iterator();
	}

	public final ast_procdef get_next_proc() {
		ast_procdef p = I.next();
		if (p == null) {
			GlobalMembersGm_error.gm_set_curr_procname("");
			set_current_proc(null);
			return null;
		} else {
			GlobalMembersGm_error.gm_set_curr_procname(p.get_procname().get_orgname());
			set_current_proc(p);
			return p;
		}
	}

	public final gm_procinfo get_proc_info(ast_procdef proc) {
		return proc_info.get(proc);
	}

	public final gm_procinfo get_current_proc_info() {
		return proc_info.get(_curr_proc);
	}

	public final gm_backend_info get_backend_info(ast_procdef proc) {
		return proc_info.get(proc).get_be_info();
	}

	public final gm_backend_info get_current_backend_info() {
		return proc_info.get(_curr_proc).get_be_info();
	}

	// -------------------------------------------------------
	// short-cut to current procedure's vocaburary
	// -------------------------------------------------------
	@Deprecated
	public final void voca_add(tangible.RefObject<String> n) {
		proc_info.get(_curr_proc).add_voca(n);
	}
	
	public final void voca_add(String value) {
		proc_info.get(_curr_proc).add_voca(value);
	}

	public final boolean voca_isin(String value) {
		return proc_info.get(_curr_proc).isin_voca(value);
	}

	public final void voca_clear() {
		proc_info.get(_curr_proc).clear_voca();
	}

	public final String voca_temp_name(String base, gm_vocabulary extra1) {
		return voca_temp_name(base, extra1, false);
	}

	public final String voca_temp_name(String base) {
		return voca_temp_name(base, null, false);
	}

	// C++ TO JAVA CONVERTER NOTE: Java does not allow default values for
	// parameters. Overloaded methods are inserted above.
	// ORIGINAL LINE: sbyte* voca_temp_name(String base, gm_vocabulary* extra1 =
	// null, boolean try_org_name_first = false)
	public final String voca_temp_name(String base, gm_vocabulary extra1, boolean try_org_name_first) {
		return proc_info.get(_curr_proc).generate_temp_name(base, extra1, try_org_name_first);
	}

	public final String voca_temp_name_and_add(String base, String suffix, gm_vocabulary extra1) {
		return voca_temp_name_and_add(base, suffix, extra1, false);
	}

	public final String voca_temp_name_and_add(String base, String suffix) {
		return voca_temp_name_and_add(base, suffix, null, false);
	}

	// C++ TO JAVA CONVERTER NOTE: Java does not allow default values for
	// parameters. Overloaded methods are inserted above.
	// ORIGINAL LINE: sbyte* voca_temp_name_and_add(String base, String suffix,
	// gm_vocabulary* extra1 = null, boolean
	// insert_underscore_prefix_if_not_already = false)
	public final String voca_temp_name_and_add(String base, String suffix, gm_vocabulary extra1, boolean insert_underscore_prefix_if_not_already) {
		String temp = new String(new char[1024]);
		if (insert_underscore_prefix_if_not_already && (base.charAt(0) != '_'))
			temp = String.format("_%s%s", base, suffix);
		else
			temp = String.format("%s%s", base, suffix);
		return voca_temp_name_and_add(temp, extra1, true);
	}

	public final String voca_temp_name_and_add(String base, gm_vocabulary extra1) {
		return voca_temp_name_and_add(base, extra1, false);
	}

	public final String voca_temp_name_and_add(String base) {
		return voca_temp_name_and_add(base, null, false);
	}

	// C++ TO JAVA CONVERTER NOTE: Java does not allow default values for
	// parameters. Overloaded methods are inserted above.
	// ORIGINAL LINE: sbyte* voca_temp_name_and_add(String base, gm_vocabulary*
	// extra1 = null, boolean try_org_name_first = false)
	public final String voca_temp_name_and_add(String base, gm_vocabulary extra1, boolean try_org_name_first) {
		// C++ TO JAVA CONVERTER TODO TASK: Java does not have an equivalent for
		// pointers to value types:
		// ORIGINAL LINE: sbyte* c =
		// proc_info[_curr_proc]->generate_temp_name(base, extra1,
		// try_org_name_first);
		String c = proc_info.get(_curr_proc).generate_temp_name(base, extra1, try_org_name_first);
		voca_add(c);
		return c;
	}

	// void init();

	private void set_current_proc(ast_procdef p) {
		_curr_proc = p;
	}

	private java.util.Iterator<ast_procdef> I;
	private java.util.ArrayList<ast_procdef> procs = new java.util.ArrayList<ast_procdef>();
	private java.util.HashMap<ast_procdef, gm_procinfo> proc_info = new java.util.HashMap<ast_procdef, gm_procinfo>();
	private ast_procdef _curr_proc;

	// void init_op_type_rules(); // operator type checkgin rules

	// --------------------------------------------------------
	// local frontend process
	// --------------------------------------------------------

	private void init_steps() {
		// C++ TO JAVA CONVERTER WARNING: The following line was determined to
		// be a copy constructor call - this should be verified and a copy
		// constructor should be created if it does not yet exist:
		// ORIGINAL LINE: java.util.LinkedList<gm_compile_step*>& LIST =
		// this->local_steps;
		LinkedList<gm_compile_step> LIST = new LinkedList<gm_compile_step>(this.local_steps);

		LIST.addLast(gm_fe_check_syntax_rules.get_factory());
		LIST.addLast(gm_fe_syntax_sugar.get_factory());
		LIST.addLast(gm_fe_typecheck_step1.get_factory());
		LIST.addLast(gm_fe_typecheck_step2.get_factory());
		LIST.addLast(gm_fe_typecheck_step3.get_factory());
		LIST.addLast(gm_fe_typecheck_step4.get_factory());
		LIST.addLast(gm_fe_typecheck_step5.get_factory());
		LIST.addLast(gm_fe_expand_group_assignment.get_factory());
		LIST.addLast(gm_fe_fixup_bound_symbol.get_factory());
		LIST.addLast(gm_fe_rw_analysis.get_factory());
		LIST.addLast(gm_fe_reduce_error_check.get_factory());
		LIST.addLast(gm_fe_rw_analysis_check2.get_factory());
		LIST.addLast(gm_fe_remove_vardecl.get_factory());
		LIST.addLast(gm_fe_check_property_argument_usage.get_factory());
	}

	private java.util.LinkedList<gm_compile_step> local_steps = new java.util.LinkedList<gm_compile_step>();

	private ast_procdef get_procedure(int i) {
		return procs.get(i);
	}

	// a hack for debug
	public boolean vardecl_removed; // a temporary hack
	// void restore_vardecl_all();

	public final boolean is_vardecl_removed() {
		return vardecl_removed;
	}

	public final void set_vardecl_removed(boolean b) {
		vardecl_removed = b;
	}

	// C++ TO JAVA CONVERTER WARNING: The original C++ declaration of the
	// following method implementation was not found:
	public void restore_vardecl_all() {
		java.util.LinkedList<gm_compile_step> L = new java.util.LinkedList<gm_compile_step>();
		GlobalMembersGm_apply_compiler_stage.gm_apply_all_proc(gm_fe_restore_vardecl.get_factory());

	}

	public void print_rwinfo() {
		for (int i = 0; i < get_num_procs(); i++) {
			ast_procdef proc = get_procedure(i);
			System.out.printf("PROC: %s\n", proc.get_procname().get_orgname());
			gm_print_rw_info P = new gm_print_rw_info();
			GlobalMembersGm_traverse.gm_traverse_sents(proc, P);
			System.out.print("\n");
		}
		return;
	}

	public void init_op_type_rules() {
		GlobalMembersGm_typecheck_oprules.init_op_rules();
	}

	public void init() {
		init_op_type_rules();
	}
}