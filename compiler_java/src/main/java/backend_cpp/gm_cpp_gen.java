package backend_cpp;

import frontend.gm_fe_fixup_bound_symbol;
import frontend.gm_fe_restore_vardecl;
import frontend.gm_symtab;
import frontend.gm_symtab_entry;
import inc.BackendGenerator;
import inc.GMEXPR_CLASS;
import inc.GMTYPE_T;
import inc.GM_OPS_T;
import inc.GM_REDUCE_T;
import inc.gm_assignment_location_t;
import inc.gm_code_writer;
import inc.gm_compile_step;
import inc.gm_ind_opt_move_propdecl;
import inc.gm_ind_opt_nonconf_reduce;
import inc.nop_enum_cpp;
import inc.nop_reduce_scalar;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import tangible.RefObject;
import ast.AST_NODE_TYPE;
import ast.ast_argdecl;
import ast.ast_assign;
import ast.ast_bfs;
import ast.ast_call;
import ast.ast_expr;
import ast.ast_expr_builtin;
import ast.ast_expr_builtin_field;
import ast.ast_extra_info_list;
import ast.ast_extra_info_set;
import ast.ast_field;
import ast.ast_foreach;
import ast.ast_id;
import ast.ast_idlist;
import ast.ast_node;
import ast.ast_nop;
import ast.ast_procdef;
import ast.ast_return;
import ast.ast_sent;
import ast.ast_sentblock;
import ast.ast_typedecl;
import ast.ast_vardecl;

import common.GM_ERRORS_AND_WARNINGS;
import common.gm_apply_compiler_stage;
import common.gm_error;
import common.gm_main;
import common.gm_transform_helper;
import common.gm_builtin_def;
import common.gm_method_id_t;
import common.gm_vocabulary;

//-----------------------------------------------------------------
// interface for graph library Layer
//-----------------------------------------------------------------
public class gm_cpp_gen extends BackendGenerator {
	
	//-----------------------------------
	// define labels, which is nothing but a string 
	//-----------------------------------
	public static String LABEL_PAR_SCOPE = "LABEL_PAR_SCOPE";
	public static String CPPBE_INFO_HAS_BFS = "CPPBE_INFO_HAS_BFS";
	public static String CPPBE_INFO_IS_PROC_ENTRY = "CPPBE_INFO_IS_PROC_ENTRY";
	public static String CPPBE_INFO_HAS_PROPDECL = "CPPBE_INFO_HAS_PROPDECL";
	public static String CPPBE_INFO_BFS_SYMBOLS = "CPPBE_INFO_BFS_SYMBOLS";
	public static String CPPBE_INFO_BFS_NAME = "CPPBE_INFO_BFS_NAME";
	public static String CPPBE_INFO_BFS_LIST = "CPPBE_INFO_BFS_LIST";
	public static String CPPBE_INFO_COLLECTION_LIST = "CPPBE_INFO_COLLECTION_LIST";
	public static String CPPBE_INFO_COLLECTION_ITERATOR = "CPPBE_INFO_COLLECTION_ITERATOR";
	public static String CPPBE_INFO_COMMON_NBR_ITERATOR = "CPPBE_INFO_COMMON_NBR_ITERATOR";
	public static String CPPBE_INFO_NEIGHBOR_ITERATOR = "CPPBE_INFO_NEIGHBOR_ITERATOR";
	public static String CPPBE_INFO_USE_REVERSE_EDGE = "CPPBE_INFO_USE_REVERSE_EDGE";
	public static String CPPBE_INFO_USE_DOWN_NBR = "CPPBE_INFO_USE_DOWN_NBR";
	public static String CPPBE_INFO_NEED_SEMI_SORT = "CPPBE_INFO_NEED_SEMI_SORT";
	public static String CPPBE_INFO_NEED_FROM_INFO = "CPPBE_INFO_NEED_FROM_INFO";

	//----------------------------------------
	// For runtime
	//----------------------------------------
	public static String MAX_THREADS = "gm_rt_get_num_threads";
	public static String THREAD_ID = "gm_rt_thread_id";
	public static String ALLOCATE_BOOL = "gm_rt_allocate_bool";
	public static String ALLOCATE_LONG = "gm_rt_allocate_long";
	public static String ALLOCATE_INT = "gm_rt_allocate_int";
	public static String ALLOCATE_DOUBLE = "gm_rt_allocate_double";
	public static String ALLOCATE_FLOAT = "gm_rt_allocate_float";
	public static String ALLOCATE_NODE = "gm_rt_allocate_node_t";
	public static String ALLOCATE_EDGE = "gm_rt_allocate_edge_t";
	public static String ALLOCATE_COLLECTION = "gm_rt_allocate_collection";
	public static String DEALLOCATE = "gm_rt_deallocate";
	public static String CLEANUP_PTR = "gm_rt_cleanup";
	public static String RT_INIT = "gm_rt_initialize";
	public static String BFS_TEMPLATE = "gm_bfs_template";
	public static String DFS_TEMPLATE = "gm_dfs_template";
	public static String DO_BFS_FORWARD = "do_bfs_forward";
	public static String DO_BFS_REVERSE = "do_bfs_reverse";
	public static String DO_DFS = "do_dfs";
	public static String RT_INCLUDE = "gm.h";
	public static String PREPARE = "prepare";
	public static String FREEZE = "freeze";
	public static String MAKE_REVERSE = "make_reverse_edges";
	public static String SEMI_SORT = "do_semi_sort";
	public static String IS_SEMI_SORTED = "is_semi_sorted";
	public static String PREPARE_FROM_INFO = "prepare_edge_source";

	// data structure for generation
	protected String fname = null; // current source file (without extension)
	protected String dname = null; // output directory

	protected gm_code_writer Header = new gm_code_writer();
	// protected gm_code_writer _Body = new gm_code_writer();

	protected File f_header = null;
	protected PrintStream ps_header = null;
	protected File f_body = null;
	protected PrintStream ps_body = null;
	
	protected String i_temp; // temporary variable name
	protected String temp;
	
	protected LinkedList<gm_compile_step> opt_steps = new LinkedList<gm_compile_step>();
	protected LinkedList<gm_compile_step> gen_steps = new LinkedList<gm_compile_step>();
	
	protected boolean _target_omp = false;
	protected gm_cpplib glib; // graph library
	
	protected int _ptr;
	protected int _indent;
	
	protected boolean _pblock = false;

	public gm_cpp_gen() {
		super();
		glib = new gm_cpplib(this);
		init();
	}

	public gm_cpp_gen(gm_cpplib l) {
		super();
		assert l != null;
		glib = l;
		glib.set_main(this);
		init();
	}

	protected final void init() {
		init_opt_steps();
		init_gen_steps();
		build_up_language_voca();
	}

	@Override
	public void dispose() {
		close_output_files();
	}

	public void setTargetDir(String d) {
		assert d != null;
		dname = d;
	}

	public void setFileName(String f) {
		fname = f;
	}

	public boolean do_local_optimize_lib() {
		assert get_lib() != null;
		return get_lib().do_local_optimize();
	}

	public boolean do_local_optimize() {
		// apply all the optimize steps to all procedures
		return gm_apply_compiler_stage.gm_apply_compiler_stage(opt_steps);
	}

	public boolean do_generate() {
		if (!open_output_files())
			return false;

		do_generate_begin();

		boolean b = gm_apply_compiler_stage.gm_apply_compiler_stage(this.gen_steps);
		assert b == true;

		do_generate_end();

		close_output_files();

		return true;
	}

	public void do_generate_begin() {
		// ----------------------------------
		// header
		// ----------------------------------
		add_ifdef_protection(fname);
		add_include("stdio.h", Header);
		add_include("stdlib.h", Header);
		add_include("stdint.h", Header);
		add_include("float.h", Header);
		add_include("limits.h", Header);
		add_include("cmath", Header);
		add_include("algorithm", Header);
		add_include("omp.h", Header);
		// add_include(get_lib()->get_header_info(), Header, false);
		add_include(RT_INCLUDE, Header, false);
		Header.NL();

		// ----------------------------------------
		// _Body
		// ----------------------------------------
		temp = String.format("%s.h", fname);
		add_include(temp, _Body, false);
		_Body.NL();
	}

	public void do_generate_end() {
		Header.NL();
		Header.pushln("#endif");
	}

	public void build_up_language_voca() {
		gm_vocabulary V = get_language_voca();

		// list of c++ reserved words
		V.add_word("int");
		V.add_word("unsigned");
		V.add_word("char");
		V.add_word("void");
		V.add_word("short");
		V.add_word("long");
		V.add_word("while");
		V.add_word("for");
		V.add_word("continue");
		V.add_word("break");
		V.add_word("double");
		V.add_word("float");
		V.add_word("if");
		V.add_word("else");
		V.add_word("do");
		V.add_word("return");
		V.add_word("register");
		V.add_word("volatile");
		V.add_word("public");
		V.add_word("class");
		V.add_word("switch");
		V.add_word("case");
		V.add_word("virtual");
		V.add_word("struct");
		V.add_word("typedef");
		V.add_word("auto");
		V.add_word("int32_t");
		V.add_word("int64_t");
		V.add_word("uint32_t");
		V.add_word("uint64_t");

		// some confisung well-known proc names
		V.add_word("rand");
		V.add_word("printf");
		V.add_word("log");
		V.add_word("exp");
		V.add_word("pow");

		get_lib().build_up_language_voca(V);
	}

	public void init_opt_steps() {
		LinkedList<gm_compile_step> LIST = this.opt_steps;

		LIST.addLast(gm_cpp_opt_check_feasible.get_factory());
		LIST.addLast(gm_cpp_opt_defer.get_factory());
		LIST.addLast(gm_cpp_opt_select_par.get_factory());
		LIST.addLast(gm_cpp_opt_save_bfs.get_factory());
		LIST.addLast(gm_ind_opt_move_propdecl.get_factory());
		LIST.addLast(gm_fe_fixup_bound_symbol.get_factory());
		LIST.addLast(gm_ind_opt_nonconf_reduce.get_factory());
		LIST.addLast(gm_cpp_opt_reduce_scalar.get_factory());
	}

	public void init_gen_steps() {
		LinkedList<gm_compile_step> LIST = this.gen_steps;

		LIST.addLast(gm_cpp_gen_sanitize_name.get_factory());
		LIST.addLast(gm_cpp_gen_regular.get_factory());
		LIST.addLast(gm_cpp_gen_prop_decl.get_factory());
		LIST.addLast(gm_cpp_gen_mark_parallel.get_factory());
		LIST.addLast(gm_cpp_gen_misc_check.get_factory());
		LIST.addLast(gm_cpp_gen_check_bfs.get_factory());
		LIST.addLast(gm_fe_restore_vardecl.get_factory());
		LIST.addLast(gm_cpp_gen_proc.get_factory());
	}

	public void prepare_parallel_for() {
		if (is_under_parallel_sentblock())
			_Body.pushln("#pragma omp for nowait"); // already under parallel
													// region.
		else
			_Body.pushln("#pragma omp parallel for");
	}

	public final gm_cpplib get_lib() {
		return glib;
	}

	// std::list<const char*> local_names;

	public void set_target_omp(boolean b) {
		_target_omp = b;
	}

	public boolean is_target_omp() {
		return _target_omp;
	}

	public boolean open_output_files() {
		String temp;
		assert dname != null;
		assert fname != null;

		temp = String.format("%s/%s.h", dname, fname);
		f_header = new File(temp);
		try {
			FileOutputStream fos = new FileOutputStream(f_header);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			ps_header = new PrintStream(bos);
		} catch (FileNotFoundException e1) {
			gm_error.gm_backend_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_FILEWRITE_ERROR, temp);
			return false;
		}
		Header.setOutputFile(ps_header);

		temp = String.format("%s/%s.cc", dname, fname);
		f_body = new File(temp);
		try {
			FileOutputStream fos = new FileOutputStream(f_body);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			ps_body = new PrintStream(bos);
		} catch (FileNotFoundException e) {
			gm_error.gm_backend_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_FILEWRITE_ERROR, temp);
			return false;
		}
		_Body.setOutputFile(ps_body);

		get_lib().set_code_writer(_Body);
		return true;

	}

	public void close_output_files() {
		if (f_header != null) {
			Header.flush();
			ps_header.close();
			f_header = null;
		}
		if (f_body != null) {
			_Body.flush();
			ps_body.close();
			f_body = null;
		}
	}

	public void add_include(String string, gm_code_writer out) {
		add_include(string, out, true, "");
	}

	public void add_include(String string, gm_code_writer out, boolean is_clib) {
		add_include(string, out, is_clib, "");
	}

	// some common sentence
	public void add_include(String String, gm_code_writer Out, boolean is_clib, String str2) {
		Out.push("#include ");
		if (is_clib)
			Out.push('<');
		else
			Out.push('"');
		Out.push(String);
		Out.push(str2);
		if (is_clib)
			Out.push('>');
		else
			Out.push('"');
		Out.NL();
	}

	public void add_ifdef_protection(String s) {
		Header.push("#ifndef GM_GENERATED_CPP_");
		Header.pushToUpper(s);
		Header.pushln("_H");
		Header.push("#define GM_GENERATED_CPP_");
		Header.pushToUpper(s);
		Header.pushln("_H");
		Header.NL();
	}

	// ------------------------------------------------------------------------------
	// Generate Method from gm_code_generator
	// ------------------------------------------------------------------------------
	public void generate_rhs_id(ast_id id) {
		generate_lhs_id(id);
	}

	public void generate_rhs_field(ast_field f) {
		generate_lhs_field(f);
	}

	public void generate_expr_builtin(ast_expr ee) {
		ast_expr_builtin e = (ast_expr_builtin) ee;

		gm_builtin_def def = e.get_builtin_def();

		ast_id driver;
		if (e.driver_is_field())
			driver = ((ast_expr_builtin_field) e).get_field_driver().get_second();
		else
			driver = e.get_driver();

		assert def != null;
		gm_method_id_t method_id = def.get_method_id();
		if (driver == null) {
			boolean add_thread_id = false;
			String func_name = get_function_name(method_id, new RefObject<Boolean>(add_thread_id));
			_Body.push(func_name);
			_Body.push('(');
			generate_expr_list(e.get_args());
			if (add_thread_id) {
				if (e.get_args().size() > 0)
					_Body.push(",gm_rt_thread_id()");
				else
					_Body.push("gm_rt_thread_id()");
			}
			_Body.push(")");
		} else {
			get_lib().generate_expr_builtin((ast_expr_builtin) e, _Body);
		}
	}

	public void generate_expr_minmax(ast_expr e) {
		if (e.get_optype() == GM_OPS_T.GMOP_MIN) {
			_Body.push(" std::min(");
		} else {
			_Body.push(" std::max(");
		}
		generate_expr(e.get_left_op());
		_Body.push(",");
		generate_expr(e.get_right_op());
		_Body.push(") ");
	}

	public void generate_expr_abs(ast_expr e) {
		_Body.push(" std::abs(");
		generate_expr(e.get_left_op());
		_Body.push(") ");
	}

	public void generate_expr_inf(ast_expr e) {
		String temp;
		assert e.get_opclass() == GMEXPR_CLASS.GMEXPR_INF;
		GMTYPE_T t = e.get_type_summary();
		switch (t) {
		case GMTYPE_INF:
		case GMTYPE_INF_INT:
			temp = String.format("%s", e.is_plus_inf() ? "INT_MAX" : "INT_MIN");
			break;
		case GMTYPE_INF_LONG:
			temp = String.format("%s", e.is_plus_inf() ? "LLONG_MAX" : "LLONG_MIN");
			break;
		case GMTYPE_INF_FLOAT:
			temp = String.format("%s", e.is_plus_inf() ? "FLT_MAX" : "FLT_MIN");
			break;
		case GMTYPE_INF_DOUBLE:
			temp = String.format("%s", e.is_plus_inf() ? "DBL_MAX" : "DBL_MIN");
			break;
		default:
			System.out.printf("what type is it? %d", t);
			assert false;
			temp = String.format("%s", e.is_plus_inf() ? "INT_MAX" : "INT_MIN");
			break;
		}
		_Body.push(temp);
		return;
	}

	public void generate_expr_nil(ast_expr ee) {
		get_lib().generate_expr_nil(ee, _Body);
	}

	public String get_type_string(GMTYPE_T type_id) {

		if (type_id.is_prim_type()) {
			switch (type_id) {
			case GMTYPE_BYTE:
				return "int8_t";
			case GMTYPE_SHORT:
				return "int16_t";
			case GMTYPE_INT:
				return "int32_t";
			case GMTYPE_LONG:
				return "int64_t";
			case GMTYPE_FLOAT:
				return "float";
			case GMTYPE_DOUBLE:
				return "double";
			case GMTYPE_BOOL:
				return "bool";
			default:
				assert false;
				return "??";
			}
		} else {
			return get_lib().get_type_string(type_id);
		}
	}

	public String get_type_string(ast_typedecl t) {
		if ((t == null) || (t.is_void())) {
			return "void";
		}

		if (t.is_primitive()) {
			return get_type_string(t.get_typeid());
		} else if (t.is_property()) {
			ast_typedecl t2 = t.get_target_type();
			assert t2 != null;
			if (t2.is_primitive()) {
				switch (t2.get_typeid()) {
				case GMTYPE_BYTE:
					return "char_t*";
				case GMTYPE_SHORT:
					return "int16_t*";
				case GMTYPE_INT:
					return "int32_t*";
				case GMTYPE_LONG:
					return "int64_t*";
				case GMTYPE_FLOAT:
					return "float*";
				case GMTYPE_DOUBLE:
					return "double*";
				case GMTYPE_BOOL:
					return "bool*";
				default:
					assert false;
					break;
				}
			} else if (t2.is_nodeedge()) {
				return String.format("%s*", get_lib().get_type_string(t2));
			} else if (t2.is_collection()) {
				return String.format("%s<%s>&", gm_cpplib_words.PROP_OF_COL, get_lib().get_type_string(t2));
			} else {
				assert false;
			}
		} else
			return get_lib().get_type_string(t);

		return "ERROR";
	}

	public void generate_lhs_id(ast_id id) {
		_Body.push(id.get_genname());
	}

	public void generate_lhs_field(ast_field f) {
		_Body.push(f.get_second().get_genname());
		_Body.push('[');
		if (f.is_rarrow()) {
			String alias_name = f.get_first().getSymInfo().find_info_string(CPPBE_INFO_NEIGHBOR_ITERATOR);
			assert alias_name != null;
			assert alias_name.length() > 0;
			_Body.push(alias_name);
		} else if (f.getTypeInfo().is_node_property()) {
			_Body.push(get_lib().node_index(f.get_first()));
		} else if (f.getTypeInfo().is_edge_property())
			_Body.push(get_lib().edge_index(f.get_first()));
		else {
			assert false;
		}
		_Body.push(']');
		return;
	}

	public void generate_sent_nop(ast_nop n) {
		if (n.get_subtype() == nop_enum_cpp.NOP_REDUCE_SCALAR) {
			((nop_reduce_scalar) n).generate(this);
		} else {
			/* otherwise ask library to hande it */
			get_lib().generate_sent_nop(n);
		}
	}

	public void generate_sent_reduce_assign(ast_assign a) {
		if (a.is_argminmax_assign()) {
			generate_sent_reduce_argmin_assign(a);
			return;
		}

		else if ((a.get_reduce_type() == GM_REDUCE_T.GMREDUCE_AND) || (a.get_reduce_type() == GM_REDUCE_T.GMREDUCE_OR)) {
			generate_sent_reduce_assign_boolean(a);
			return;
		}

		// implement reduction using compare and swap
		// ---------------------------------------
		// {
		// <type> OLD, NEW
		// do {
		// OLD = LHS;
		// NEW = LHS <op> RHS;
		// <optional break> (for min/max)
		// } while (!_bool_comp_swap(&LHS, OLD, NEW))
		// }
		// ---------------------------------------
		ast_typedecl lhs_target_type = (a.get_lhs_type() == gm_assignment_location_t.GMASSIGN_LHS_SCALA) ? a.get_lhs_scala().getTypeInfo() : a.get_lhs_field()
				.getTypeInfo().get_target_type();

		String temp_var_base = (a.get_lhs_type() == gm_assignment_location_t.GMASSIGN_LHS_SCALA) ? a.get_lhs_scala().get_orgname() : a.get_lhs_field()
				.get_second().get_orgname();

		GM_REDUCE_T r_type = a.get_reduce_type();

		String temp_var_old;
		String temp_var_new;
		boolean is_scalar = (a.get_lhs_type() == gm_assignment_location_t.GMASSIGN_LHS_SCALA);

		temp_var_old = gm_main.FE.voca_temp_name_and_add(temp_var_base, "_old");
		temp_var_new = gm_main.FE.voca_temp_name_and_add(temp_var_base, "_new");

		_Body.pushln("// reduction");
		_Body.pushln("{ ");

		String temp = String.format("%s %s, %s;", get_type_string(lhs_target_type), temp_var_old, temp_var_new);
		_Body.pushln(temp);

		_Body.pushln("do {");
		temp = String.format("%s = ", temp_var_old);
		_Body.push(temp);
		if (is_scalar)
			generate_rhs_id(a.get_lhs_scala());
		else
			generate_rhs_field(a.get_lhs_field());

		_Body.pushln(";");
		if (r_type == GM_REDUCE_T.GMREDUCE_PLUS) {
			temp = String.format("%s = %s + (", temp_var_new, temp_var_old);
			_Body.push(temp);
		} else if (r_type == GM_REDUCE_T.GMREDUCE_MULT) {
			temp = String.format("%s = %s * (", temp_var_new, temp_var_old);
			_Body.push(temp);
		} else if (r_type == GM_REDUCE_T.GMREDUCE_MAX) {
			temp = String.format("%s = std::max (%s, ", temp_var_new, temp_var_old);
			_Body.push(temp);
		} else if (r_type == GM_REDUCE_T.GMREDUCE_OR) {
			temp = String.format("%s = %s || (", temp_var_new, temp_var_old);
			_Body.push(temp);
		} else if (r_type == GM_REDUCE_T.GMREDUCE_AND) {
			temp = String.format("%s = %s && (", temp_var_new, temp_var_old);
			_Body.push(temp);
		} else if (r_type == GM_REDUCE_T.GMREDUCE_MIN) {
			temp = String.format("%s = std::min (%s, ", temp_var_new, temp_var_old);
			_Body.push(temp);
		} else {
			assert false;
		}

		generate_expr(a.get_rhs());
		_Body.pushln(");");
		if ((r_type == GM_REDUCE_T.GMREDUCE_MAX) || (r_type == GM_REDUCE_T.GMREDUCE_MIN)) {
			temp = String.format("if (%s == %s) break;", temp_var_old, temp_var_new);
			_Body.pushln(temp);
		}
		_Body.push("} while (_gm_atomic_compare_and_swap(&(");
		if (is_scalar)
			generate_rhs_id(a.get_lhs_scala());
		else
			generate_rhs_field(a.get_lhs_field());

		temp = String.format("), %s, %s)==false); ", temp_var_old, temp_var_new);
		_Body.pushln(temp);
		_Body.pushln("}");

		temp_var_new = null;
		temp_var_old = null;

		return;
	}

	@Override
	public void generate_sent_defer_assign(ast_assign a) {
		assert false;
	} // should not be here

	public void generate_sent_vardecl(ast_vardecl v) {
		ast_typedecl t = v.get_type();

		if (t.is_collection_of_collection()) {
			_Body.push(get_type_string(t));
			_Body.push("<");
			_Body.push(get_type_string(t.getTargetTypeSummary()));
			_Body.push("> ");
			ast_idlist idl = v.get_idlist();
			assert idl.get_length() == 1;
			generate_lhs_id(idl.get_item(0));
			get_lib().add_collection_def(idl.get_item(0));
			return;
		}

		_Body.pushSpace(get_type_string(t));

		if (t.is_property()) {
			ast_idlist idl = v.get_idlist();
			assert idl.get_length() == 1;
			generate_lhs_id(idl.get_item(0));
			declare_prop_def(t, idl.get_item(0));
		} else if (t.is_collection()) {
			ast_idlist idl = v.get_idlist();
			assert idl.get_length() == 1;
			generate_lhs_id(idl.get_item(0));
			get_lib().add_collection_def(idl.get_item(0));
		} else if (t.is_primitive()) {
			generate_idlist_primitive(v.get_idlist());
			_Body.pushln(";");
		} else {
			generate_idlist(v.get_idlist());
			_Body.pushln(";");
		}
	}

	public void generate_sent_foreach(ast_foreach f) {
		boolean need_init_before = get_lib().need_up_initializer(f);

		if (need_init_before) {
			assert f.get_parent().get_nodetype() == AST_NODE_TYPE.AST_SENTBLOCK;
			get_lib().generate_up_initializer(f, _Body);
		}

		if (f.is_parallel()) {
			_Body.NL();
			prepare_parallel_for();
		}

		get_lib().generate_foreach_header(f, _Body);

		if (get_lib().need_down_initializer(f)) {
			_Body.pushln("{");
			get_lib().generate_down_initializer(f, _Body);

			if (f.get_body().get_nodetype() != AST_NODE_TYPE.AST_SENTBLOCK) {
				generate_sent(f.get_body());
			} else {
				// '{' '} already handled
				generate_sent_block((ast_sentblock) f.get_body(), false);
			}
			_Body.pushln("}");

		} else if (f.get_body().get_nodetype() == AST_NODE_TYPE.AST_SENTBLOCK) {
			generate_sent(f.get_body());
		} else {
			_Body.pushIndent();
			generate_sent(f.get_body());
			_Body.popIndent();
			_Body.NL();
		}
	}

	public void generate_sent_bfs(ast_bfs bfs) {
		_Body.NL();

		// -------------------------------------------
		// (1) create BFS object
		// -------------------------------------------
		String bfs_name = bfs.find_info_string(CPPBE_INFO_BFS_NAME);
		String bfs_inst_name = bfs.is_bfs() ? gm_main.FE.voca_temp_name_and_add("_BFS", "") : gm_main.FE.voca_temp_name_and_add(
				"_DFS", "");
		temp = String.format("%s %s", bfs_name, bfs_inst_name);
		_Body.push(temp);
		_Body.push('(');

		// -------------------------------------------
		// give every entry that are used
		// -------------------------------------------
		ast_extra_info_set syms = (ast_extra_info_set) bfs.find_info(CPPBE_INFO_BFS_SYMBOLS);
		assert syms != null;
		HashSet<Object> S = syms.get_set();
		boolean is_first = true;
		for (Object sym : S) {
			if (!is_first) {
				_Body.push(", ");
			}
			is_first = false;
			gm_symtab_entry e = (gm_symtab_entry) sym;
			_Body.push(e.getId().get_genname());
		}
		_Body.pushln(");");

		// -------------------------------------------
		// (2) Make a call to it
		// -------------------------------------------
		if (bfs.is_bfs()) {
			temp = String.format("%s.%s(%s, %s());", bfs_inst_name, PREPARE, bfs.get_root().get_genname(), MAX_THREADS);
			_Body.pushln(temp);
			temp = String.format("%s.%s();", bfs_inst_name, DO_BFS_FORWARD);
			_Body.pushln(temp);

			if (bfs.get_bbody() != null) {
				temp = String.format("%s.%s();", bfs_inst_name, DO_BFS_REVERSE);
				_Body.pushln(temp);
			}
		} // DFS
		else {
			temp = String.format("%s.%s(%s);", bfs_inst_name, PREPARE, bfs.get_root().get_genname());
			_Body.pushln(temp);
			temp = String.format("%s.%s();", bfs_inst_name, DO_DFS);
			_Body.pushln(temp);
		}

		bfs_inst_name = null;
		return;
	}

	public void generate_sent_block(ast_sentblock sb) {
		generate_sent_block(sb, true);
	}

	public void generate_sent_block(ast_sentblock sb, boolean need_br) {
		if (is_target_omp()) {
			boolean is_par_scope = sb.find_info_bool(LABEL_PAR_SCOPE);
			if (is_par_scope) {
				assert is_under_parallel_sentblock() == false;
				set_under_parallel_sentblock(true);
				need_br = true;
				_Body.pushln("#pragma omp parallel");
			}
		}

		if (need_br)
			_Body.pushln("{");

		// sentblock exit
		generate_sent_block_enter(sb);

		LinkedList<ast_sent> sents = sb.get_sents();
		boolean vardecl_started = false;
		boolean other_started = false;
		for (ast_sent s : sents) {
			// insert newline after end of VARDECL
			if (!vardecl_started) {
				if (s.get_nodetype() == AST_NODE_TYPE.AST_VARDECL)
					vardecl_started = true;
			} else {
				if (other_started == false) {
					if (s.get_nodetype() != AST_NODE_TYPE.AST_VARDECL) {
						_Body.NL();
						other_started = true;
					}
				}
			}
			generate_sent(s);
		}

		// sentblock exit
		generate_sent_block_exit(sb);

		if (need_br)
			_Body.pushln("}");

		if (is_under_parallel_sentblock())
			set_under_parallel_sentblock(false);

		return;
	}

	public void generate_sent_return(ast_return r) {
		if (gm_main.FE.get_current_proc().find_info_bool(CPPBE_INFO_HAS_PROPDECL)) {
			_Body.push(CLEANUP_PTR);
			_Body.pushln("();");
		}

		_Body.push("return");
		if (r.get_expr() != null) {
			_Body.SPC();
			generate_expr(r.get_expr());
		}
		_Body.pushln("; ");
	}

	public void generate_sent_call(ast_call c) {
		assert c.is_builtin_call();
		generate_expr_builtin(c.get_builtin());
		_Body.pushln(";");
	}

	public void generate_sent_assign(ast_assign a) {

		if (a.is_target_scalar()) {
			ast_id leftHandSide = a.get_lhs_scala();
			if (leftHandSide.is_instantly_assigned()) // we have to add the
														// variable declaration
														// here
			{
				_Body.push(get_lib().get_type_string(leftHandSide.getTypeSummary()));
				_Body.push(" ");
			}
			generate_lhs_id(a.get_lhs_scala());
		} else {
			generate_lhs_field(a.get_lhs_field());
		}

		_Body.push(" = ");

		generate_expr(a.get_rhs());

		_Body.pushln(" ;");
	}

	public void generate_sent_block_enter(ast_sentblock sb) {
		if (sb.find_info_bool(CPPBE_INFO_IS_PROC_ENTRY) && !gm_main.FE.get_current_proc().is_local()) {
			_Body.pushln("//Initializations");
			temp = String.format("%s();", RT_INIT);
			_Body.pushln(temp);

			// ----------------------------------------------------
			// freeze graph instances
			// ----------------------------------------------------
			ast_procdef proc = gm_main.FE.get_current_proc();
			gm_symtab vars = proc.get_symtab_var();
			gm_symtab fields = proc.get_symtab_field();
			// std::vector<gm_symtab_entry*>& E = vars-> get_entries();
			// for(int i=0;i<E.size();i++) {
			// gm_symtab_entry* e = E[i];
			HashSet<gm_symtab_entry> E = vars.get_entries();
			for (gm_symtab_entry e : E) {
				if (e.getType().is_graph()) {
					temp = String.format("%s.%s();", e.getId().get_genname(), FREEZE);
					_Body.pushln(temp);

					// currrently every graph is an argument
					if (e.find_info_bool(CPPBE_INFO_USE_REVERSE_EDGE)) {
						temp = String.format("%s.%s();", e.getId().get_genname(), MAKE_REVERSE);
						_Body.pushln(temp);
					}
					if (e.find_info_bool(CPPBE_INFO_NEED_SEMI_SORT)) {
						boolean has_edge_prop = false;
						// Semi-sorting must be done before edge-property
						// creation
						// std::vector<gm_symtab_entry*>& F = fields->
						// get_entries();
						// for(int j=0;j<F.size();j++) {
						// gm_symtab_entry* f = F[j];
						HashSet<gm_symtab_entry> F = fields.get_entries();
						for (gm_symtab_entry f : F) {
							if ((f.getType().get_target_graph_sym() == e) && (f.getType().is_edge_property()))
								has_edge_prop = true;
						}
						if (has_edge_prop) {
							_Body.pushln("//[xxx] edge property must be created before semi-sorting");
							temp = String.format("assert(%s.%s());", e.getId().get_genname(), IS_SEMI_SORTED);
							_Body.pushln(temp);
						} else {
							temp = String.format("%s.%s();", e.getId().get_genname(), SEMI_SORT);
							_Body.pushln(temp);
						}
					}

					if (e.find_info_bool(CPPBE_INFO_NEED_FROM_INFO)) {
						temp = String.format("%s.%s();", e.getId().get_genname(), PREPARE_FROM_INFO);
						_Body.pushln(temp);
					}
				}
			}
			_Body.NL();
		}

	}

	public void generate_sent_block_exit(ast_sentblock sb) {
		boolean has_prop_decl = sb.find_info_bool(CPPBE_INFO_HAS_PROPDECL);
		boolean is_proc_entry = sb.find_info_bool(CPPBE_INFO_IS_PROC_ENTRY);
		boolean has_return_ahead = gm_transform_helper.gm_check_if_end_with_return(sb);

		if (has_prop_decl && !has_return_ahead) {
			if (is_proc_entry) {
				_Body.NL();
				temp = String.format("%s();", CLEANUP_PTR);
				_Body.pushln(temp);
			} else {
				_Body.NL();
				gm_symtab tab = sb.get_symtab_field();
				// std::vector<gm_symtab_entry*>& entries = tab->get_entries();
				// std::vector<gm_symtab_entry*>::iterator I;
				HashSet<gm_symtab_entry> entries = tab.get_entries();
				for (gm_symtab_entry e : entries) {
					temp = String.format("%s(%s,%s());", DEALLOCATE, e.getId().get_genname(), THREAD_ID);
					_Body.pushln(temp);
				}
			}
		}

	}

	public void generate_idlist(ast_idlist idl) {
		int z = idl.get_length();
		for (int i = 0; i < z; i++) {
			ast_id id = idl.get_item(i);
			generate_lhs_id(id);
			if (i < z - 1)
				_Body.pushSpace(',');
		}
	}

	public void generate_proc(ast_procdef proc) {
		// -------------------------------
		// declare function name
		// -------------------------------
		generate_proc_decl(proc, false); // declare in header file

		// -------------------------------
		// BFS definitions
		// -------------------------------
		if (proc.find_info_bool(CPPBE_INFO_HAS_BFS)) {
			ast_extra_info_list L = (ast_extra_info_list) proc.find_info(CPPBE_INFO_BFS_LIST);
			assert L != null;
			_Body.NL();
			_Body.pushln("// BFS/DFS definitions for the procedure");
			for (Object info : L.get_list()) {
				ast_bfs bfs = (ast_bfs) info;
				generate_bfs_def(bfs);
			}
		}

		// -------------------------------
		// function definition
		// -------------------------------
		generate_proc_decl(proc, true); // declare in _Body file
		generate_sent(proc.get_body());
		_Body.NL();

		return;
	}

	public void generate_proc_decl(ast_procdef proc, boolean is_body_file) {
		// declare in the header or _Body
		gm_code_writer Out = is_body_file ? _Body : Header;

		if (!is_body_file && proc.is_local())
			return;

		if (proc.is_local())
			Out.push("static ");

		// return type
		Out.pushSpace(get_type_string(proc.get_return_type()));
		Out.push(proc.get_procname().get_genname());
		Out.push('(');

		int max_arg_per_line = 2;
		int arg_curr = 0;
		int remain_args = proc.get_in_args().size() + proc.get_out_args().size();
		{
			LinkedList<ast_argdecl> lst = proc.get_in_args();
			for (ast_argdecl decl : lst) {
				remain_args--;
				arg_curr++;

				ast_typedecl T = decl.get_type();
				Out.push(get_type_string(T));
				if (T.is_primitive() || T.is_property())
					Out.push(" ");
				else
					Out.push("& ");

				assert decl.get_idlist().get_length() == 1;
				Out.push(decl.get_idlist().get_item(0).get_genname());
				if (remain_args > 0) {
					Out.push(", ");
				}

				if ((arg_curr == max_arg_per_line) && (remain_args > 0)) {
					Out.NL();
					arg_curr = 0;
				}
			}
		}
		{
			LinkedList<ast_argdecl> lst = proc.get_out_args();
			for (ast_argdecl decl : lst) {
				remain_args--;
				arg_curr++;

				Out.push(get_type_string(decl.get_type()));
				Out.pushSpace("&");
				Out.push(decl.get_idlist().get_item(0).get_genname());
				if (remain_args > 0) {
					Out.push(", ");
				}
				if ((arg_curr == max_arg_per_line) && (remain_args > 0)) {
					Out.NL();
					arg_curr = 0;
				}
			}
		}

		Out.push(')');
		if (!is_body_file)
			Out.push(';');

		Out.NL();
		return;
	}

	protected final boolean is_under_parallel_sentblock() {
		return _pblock;
	}

	protected final void set_under_parallel_sentblock(boolean b) {
		_pblock = b;
	}

	public void declare_prop_def(ast_typedecl t, ast_id id) {
		ast_typedecl t2 = t.get_target_type();
		assert t2 != null;

		_Body.push(" = ");
		switch (t2.getTypeSummary()) {
		case GMTYPE_INT:
			_Body.push(ALLOCATE_INT);
			break;
		case GMTYPE_LONG:
			_Body.push(ALLOCATE_LONG);
			break;
		case GMTYPE_BOOL:
			_Body.push(ALLOCATE_BOOL);
			break;
		case GMTYPE_DOUBLE:
			_Body.push(ALLOCATE_DOUBLE);
			break;
		case GMTYPE_FLOAT:
			_Body.push(ALLOCATE_FLOAT);
			break;
		case GMTYPE_NODE:
			_Body.push(ALLOCATE_NODE);
			break;
		case GMTYPE_EDGE:
			_Body.push(ALLOCATE_EDGE);
			break;
		case GMTYPE_NSET:
		case GMTYPE_ESET:
		case GMTYPE_NSEQ:
		case GMTYPE_ESEQ:
		case GMTYPE_NORDER:
		case GMTYPE_EORDER: {
			// TODO: get some information here to check if lazy init is better
			boolean lazyInitialization = false;
			String temp = String.format("%s<%s, %s>", ALLOCATE_COLLECTION, get_lib().get_type_string(t2), (lazyInitialization ? "true" : "false"));
			_Body.push(temp);
			break;
		}
		default:
			assert false;
			break;
		}

		_Body.push('(');
		if (t.is_node_property()) {
			_Body.push(get_lib().max_node_index(t.get_target_graph_id()));
		} else if (t.is_edge_property()) {
			_Body.push(get_lib().max_edge_index(t.get_target_graph_id()));
		}
		_Body.push(',');
		_Body.push(THREAD_ID);
		_Body.pushln("());");

		/*
		 */

		// register to memory controller
	}

	public void generate_sent_reduce_argmin_assign(ast_assign a) {
		assert a.is_argminmax_assign();

		// -----------------------------------------------
		// <LHS; L1,...> min= <RHS; R1,...>;
		//
		// {
		// RHS_temp = RHS;
		// if (LHS > RHS_temp) {
		// <type> L1_temp = R1;
		// ...
		// LOCK(LHS) // if LHS is scalar,
		// // if LHS is field, lock the node
		// if (LHS > RHS_temp) {
		// LHS = RHS_temp;
		// L1 = L1_temp; // no need to lock for L1.
		// ...
		// }
		// UNLOCK(LHS)
		// }
		// }
		// -----------------------------------------------
		_Body.pushln("{ // argmin(argmax) - test and test-and-set");
		String rhs_temp;
		GMTYPE_T t;
		if (a.is_target_scalar()) {
			t = a.get_lhs_scala().getTypeSummary();
			rhs_temp = (String) gm_main.FE.voca_temp_name_and_add(a.get_lhs_scala().get_genname(), "_new");
		} else {
			t = a.get_lhs_field().get_second().getTargetTypeSummary();
			rhs_temp = (String) gm_main.FE.voca_temp_name_and_add(a.get_lhs_field().get_second().get_genname(), "_new");
		}
		_Body.push(get_type_string(t));
		_Body.SPC();
		_Body.push(rhs_temp);
		_Body.push(" = ");
		generate_expr(a.get_rhs());
		_Body.pushln(";");

		_Body.push("if (");
		if (a.is_target_scalar()) {
			generate_rhs_id(a.get_lhs_scala());
		} else {
			generate_rhs_field(a.get_lhs_field());
		}
		if (a.get_reduce_type() == GM_REDUCE_T.GMREDUCE_MIN) {
			_Body.push(">");
		} else {
			_Body.push("<");
		}
		_Body.push(rhs_temp);
		_Body.pushln(") {");

		LinkedList<ast_node> L = a.get_lhs_list();
		LinkedList<ast_expr> R = a.get_rhs_list();
		Iterator<ast_expr> J;
		String[] names = new String[L.size()];
		int i = 0;
		J = R.iterator();
		for (ast_node n : L) {
			ast_id id;
			GMTYPE_T type;
			if (n.get_nodetype() == AST_NODE_TYPE.AST_ID) {
				id = (ast_id) n;
				type = id.getTypeSummary();
			} else {
				assert n.get_nodetype() == AST_NODE_TYPE.AST_FIELD;
				ast_field f = (ast_field) n;
				id = f.get_second();
				type = id.getTargetTypeSummary();
			}
			names[i] = (String) gm_main.FE.voca_temp_name_and_add(id.get_genname(), "_arg");
			_Body.push(get_type_string(type));
			_Body.SPC();
			_Body.push(names[i]);
			_Body.push(" = ");
			generate_expr(J.next());
			_Body.pushln(";");
			i++;
		}

		// LOCK, UNLOCK
		String LOCK_FN_NAME;
		String UNLOCK_FN_NAME;
		if (a.is_target_scalar()) {
			LOCK_FN_NAME = "gm_spinlock_acquire_for_ptr";
			UNLOCK_FN_NAME = "gm_spinlock_release_for_ptr";
		} else {
			ast_id drv = a.get_lhs_field().get_first();
			if (drv.getTypeInfo().is_node_compatible()) {
				LOCK_FN_NAME = "gm_spinlock_acquire_for_node";
				UNLOCK_FN_NAME = "gm_spinlock_release_for_node";
			} else if (drv.getTypeInfo().is_edge_compatible()) {
				LOCK_FN_NAME = "gm_spinlock_acquire_for_edge";
				UNLOCK_FN_NAME = "gm_spinlock_release_for_edge";
			} else {
				assert false;
				throw new AssertionError();
			}
		}

		_Body.push(LOCK_FN_NAME);
		_Body.push("(");
		if (a.is_target_scalar()) {
			_Body.push("&");
			generate_rhs_id(a.get_lhs_scala());
		} else {
			generate_rhs_id(a.get_lhs_field().get_first());
		}
		_Body.pushln(");");

		_Body.push("if (");
		if (a.is_target_scalar()) {
			generate_rhs_id(a.get_lhs_scala());
		} else {
			generate_rhs_field(a.get_lhs_field());
		}
		if (a.get_reduce_type() == GM_REDUCE_T.GMREDUCE_MIN) {
			_Body.push(">");
		} else {
			_Body.push("<");
		}
		_Body.push(rhs_temp);
		_Body.pushln(") {");

		// lhs = rhs_temp
		if (a.is_target_scalar()) {
			generate_lhs_id(a.get_lhs_scala());
		} else {
			generate_lhs_field(a.get_lhs_field());
		}
		_Body.push(" = ");
		_Body.push(rhs_temp);
		_Body.pushln(";");

		i = 0;
		for (ast_node n : L) {
			if (n.get_nodetype() == AST_NODE_TYPE.AST_ID) {
				generate_lhs_id((ast_id) n);
			} else {
				generate_lhs_field((ast_field) n);
			}
			_Body.push(" = ");
			_Body.push(names[i]);
			_Body.pushln(";");
			i++;
		}

		_Body.pushln("}"); // end of inner if

		_Body.push(UNLOCK_FN_NAME);
		_Body.push("(");
		if (a.is_target_scalar()) {
			_Body.push("&");
			generate_rhs_id(a.get_lhs_scala());
		} else {
			generate_rhs_id(a.get_lhs_field().get_first());
		}
		_Body.pushln(");");

		_Body.pushln("}"); // end of outer if

		_Body.pushln("}"); // end of reduction
		// clean-up
		for (i = 0; i < (int) L.size(); i++)
			names[i] = null;
		names = null;
		rhs_temp = null;
	}

	public void generate_sent_reduce_assign_boolean(ast_assign a) {
		// implement reduction using compare and swap
		// ---------------------------------------
		// bool NEW
		// NEW = RHS;
		// // for or-reduction
		// if (NEW) LHS = TRUE
		// // for and-reduciton
		// if (!NEW) LHS = FALSE
		// ---------------------------------------
		String temp_var_base = (a.get_lhs_type() == gm_assignment_location_t.GMASSIGN_LHS_SCALA) ? a.get_lhs_scala().get_orgname() : a.get_lhs_field()
				.get_second().get_orgname();

		String temp_var_new;
		temp_var_new = gm_main.FE.voca_temp_name_and_add(temp_var_base, "_new");
		boolean is_scalar = (a.get_lhs_type() == gm_assignment_location_t.GMASSIGN_LHS_SCALA);

		_Body.pushln("// boolean reduction (no need CAS)");
		_Body.pushln("{ ");

		temp = String.format("bool %s;", temp_var_new);
		_Body.pushln(temp);
		temp = String.format("%s = ", temp_var_new);
		_Body.push(temp);
		generate_expr(a.get_rhs());
		_Body.pushln(";");

		if (a.get_reduce_type() == GM_REDUCE_T.GMREDUCE_AND) {
			_Body.pushln("// and-reduction");
			temp = String.format("if ((!%s) ", temp_var_new);
			_Body.push(temp); // new value is false
			temp = String.format("&& ( ");
			_Body.push(temp); // old value is true
			if (is_scalar)
				generate_rhs_id(a.get_lhs_scala());
			else
				generate_rhs_field(a.get_lhs_field());
			_Body.pushln("))");
			_Body.pushIndent();
			if (is_scalar)
				generate_rhs_id(a.get_lhs_scala());
			else
				generate_rhs_field(a.get_lhs_field());
			_Body.pushln(" = false;");
			_Body.popIndent();
		} else if (a.get_reduce_type() == GM_REDUCE_T.GMREDUCE_OR) {
			_Body.pushln("// or-reduction");
			temp = String.format("if ((%s) ", temp_var_new);
			_Body.push(temp); // new value is true
			temp = String.format("&& (! ");
			_Body.push(temp); // old value is false
			if (is_scalar)
				generate_rhs_id(a.get_lhs_scala());
			else
				generate_rhs_field(a.get_lhs_field());
			_Body.pushln("))");
			_Body.pushIndent();
			if (is_scalar)
				generate_rhs_id(a.get_lhs_scala());
			else
				generate_rhs_field(a.get_lhs_field());
			_Body.pushln(" = true;");
			_Body.popIndent();
		} else {
			assert false;
		}
		_Body.pushln("}");
		temp_var_new = null;
	}

	public void generate_bfs_def(ast_bfs bfs) {
		String bfs_name = bfs.find_info_string(CPPBE_INFO_BFS_NAME);
		String level_t = "short";
		String use_multithread = "" + is_target_omp();
		String save_child = ""+ bfs.find_info_bool(CPPBE_INFO_USE_DOWN_NBR);
		String use_reverse_edge = "" +bfs.is_transpose();
		String has_navigator = Boolean.toString(bfs.get_navigator() != null);

		String has_pre_visit = Boolean.toString((bfs.get_fbody() != null) && (bfs.get_fbody().get_sents().size() >= 1));

		String has_post_visit = Boolean.toString((bfs.get_bbody() != null) && (bfs.get_bbody().get_sents().size() >= 1));

		ast_extra_info_set info = (ast_extra_info_set) bfs.find_info(CPPBE_INFO_BFS_SYMBOLS);
		HashSet<Object> SET = info.get_set();
		gm_symtab_entry graph_sym = (gm_symtab_entry) (SET.iterator().next());
		String template_name = (bfs.is_bfs() ? BFS_TEMPLATE : DFS_TEMPLATE);

		temp = String.format("class %s : public %s", bfs_name, template_name);
		_Body.pushln(temp);
		_Body.pushIndent();
		if (bfs.is_bfs()) {
			temp = String.format("<%s, %s, %s, %s, %s>", level_t, use_multithread, has_navigator, use_reverse_edge, save_child);
		} else {
			temp = String.format("<%s, %s, %s, %s>", has_pre_visit, has_post_visit, has_navigator, use_reverse_edge);
		}

		_Body.pushln(temp);
		_Body.popIndent();
		_Body.pushln("{");

		_Body.popIndent();
		_Body.pushln("public:");
		_Body.pushIndent();

		temp = String.format("%s(", bfs_name);
		_Body.push(temp);

		// ------------------------------------------
		// constructor
		// ------------------------------------------
		boolean is_first = true;
		int total = SET.size();
		int i = 0;
		int NL = 3;
		for (Object tmp : SET) {
			gm_symtab_entry sym = (gm_symtab_entry) tmp;
			if (!is_first)
				_Body.push(", ");
			if ((i > 0) && (i != total) && ((i % NL) == 0))
				_Body.NL();
			is_first = false;
			ast_typedecl t = sym.getType();
			_Body.push(get_type_string(t));
			_Body.push("&");
			_Body.push(" _");
			_Body.push(sym.getId().get_genname());
			i++;
		}
		_Body.pushln(")");
		temp = String.format(": %s", template_name);
		_Body.push(temp);
		if (bfs.is_bfs()) {
			temp = String.format("<%s, %s, %s, %s, %s>", level_t, use_multithread, has_navigator, use_reverse_edge, save_child);
		} else {
			temp = String.format("<%s, %s, %s, %s>", has_pre_visit, has_post_visit, has_navigator, use_reverse_edge);
		}
		_Body.push(temp);
		temp = String.format("(_%s),", graph_sym.getId().get_genname());
		_Body.pushln(temp);

		// init list
		is_first = true;
		NL = 6;
		i = 0;
		for (Object tmp : SET) {
			gm_symtab_entry sym = (gm_symtab_entry) tmp;
			if (!is_first)
				_Body.push(", ");
			if ((i > 0) && (i != total) && ((i % NL) == 0))
				_Body.NL();
			is_first = false;
			_Body.push(sym.getId().get_genname());
			_Body.push('(');
			_Body.push('_');
			_Body.push(sym.getId().get_genname());
			_Body.push(')');
			i++;
		}
		_Body.pushln("{}");
		_Body.NL();

		// -------------------------------------------------
		// list of scope variables
		// -------------------------------------------------
		_Body.popIndent();
		_Body.pushln("private:  // list of varaibles");
		_Body.pushIndent();
		for (Object tmp : SET) {
			gm_symtab_entry sym = (gm_symtab_entry) tmp;
			ast_typedecl t = sym.getType();
			_Body.push(get_type_string(t));
			_Body.push("& ");
			_Body.push(sym.getId().get_genname());
			_Body.pushln(";");
			i++;
		}
		_Body.NL();

		_Body.popIndent();
		_Body.pushln("protected:");
		_Body.pushIndent();

		ast_id iter = bfs.get_iterator();
		String a_name = gm_main.FE.voca_temp_name_and_add(iter.get_orgname(), "_idx");
		iter.getSymInfo().add_info_string(CPPBE_INFO_NEIGHBOR_ITERATOR, a_name);
		a_name = null;

		generate_bfs_body_fw(bfs);
		generate_bfs_body_bw(bfs);
		generate_bfs_navigator(bfs);
		_Body.NL();

		_Body.NL();
		_Body.pushln("};");
		_Body.NL();
	}

	public void generate_bfs_body_fw(ast_bfs bfs) {
		assert bfs.get_f_filter() == null;
		// should be changed into if already

		if (bfs.is_bfs()) {
			_Body.push("virtual void visit_fw(");
		} else {
			_Body.push("virtual void visit_pre(");
		}
		_Body.push(get_lib().get_type_string(GMTYPE_T.GMTYPE_NODE));
		_Body.SPC();
		_Body.push(bfs.get_iterator().get_genname());
		_Body.push(") ");
		if ((bfs.get_fbody() == null) || (bfs.get_fbody().get_sents().size() == 0)) {
			_Body.pushln("{}");
		} else {
			_Body.NL();
			ast_sentblock sb = bfs.get_fbody();
			generate_sent_block(sb);
			_Body.NL();
		}
	}

	public void generate_bfs_body_bw(ast_bfs bfs) {
		assert bfs.get_b_filter() == null;
		// should be changed into if already

		if (bfs.is_bfs()) {
			_Body.push("virtual void visit_rv(");
		} else {
			_Body.push("virtual void visit_post(");
		}

		_Body.push(get_lib().get_type_string(GMTYPE_T.GMTYPE_NODE));
		_Body.SPC();
		_Body.push(bfs.get_iterator().get_genname());
		_Body.push(") ");
		if (bfs.get_bbody() == null) {
			_Body.pushln("{}");
		} else {
			_Body.NL();
			ast_sentblock sb = bfs.get_bbody();
			generate_sent_block(sb);
			_Body.NL();
		}
	}

	public void generate_bfs_navigator(ast_bfs bfs) {
		_Body.push("virtual bool check_navigator(");
		_Body.push(get_lib().get_type_string(GMTYPE_T.GMTYPE_NODE));
		_Body.SPC();
		_Body.push(bfs.get_iterator().get_genname());
		_Body.push(", ");
		_Body.push(get_lib().get_type_string(GMTYPE_T.GMTYPE_EDGE));
		_Body.SPC();
		String alias_name = bfs.get_iterator().getSymInfo().find_info_string(CPPBE_INFO_NEIGHBOR_ITERATOR);
		assert alias_name != null;
		assert alias_name.length() > 0;
		_Body.push(alias_name);
		_Body.push(") ");
		if (bfs.get_navigator() == null) {
			_Body.pushln("{return true;}");

		} else {
			_Body.NL();
			_Body.pushln("{");
			_Body.push("return (");
			ast_expr nv = bfs.get_navigator();
			generate_expr(nv);
			_Body.pushln(");");
			_Body.pushln("}");
		}
	}

	public String get_function_name(gm_method_id_t methodId, tangible.RefObject<Boolean> addThreadId) {
		switch (methodId) {
		case GM_BLTIN_TOP_DRAND:
			addThreadId.argvalue = true;
			return "gm_rt_uniform";
		case GM_BLTIN_TOP_IRAND:
			addThreadId.argvalue = true;
			return "gm_rt_rand";
		case GM_BLTIN_TOP_LOG:
			return "log";
		case GM_BLTIN_TOP_EXP:
			return "exp";
		case GM_BLTIN_TOP_POW:
			return "pow";
		default:
			assert false;
			return "ERROR";
		}
	}

	public void generate_idlist_primitive(ast_idlist idList) {
		int length = idList.get_length();
		for (int i = 0; i < length; i++) {
			ast_id id = idList.get_item(i);
			generate_lhs_id(id);
			generate_lhs_default(id.getTypeSummary());
			if (i < length - 1)
				_Body.pushSpace(',');
		}
	}

	public void generate_lhs_default(GMTYPE_T type) {
		switch (type) {
		case GMTYPE_BYTE:
		case GMTYPE_SHORT:
		case GMTYPE_INT:
		case GMTYPE_LONG:
			_Body.pushSpace(" = 0");
			break;
		case GMTYPE_FLOAT:
		case GMTYPE_DOUBLE:
			_Body.pushSpace(" = 0.0");
			break;
		case GMTYPE_BOOL:
			_Body.pushSpace(" = false");
			break;
		default:
			assert false;
			return;
		}
	}

}