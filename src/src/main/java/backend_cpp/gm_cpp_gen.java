package backend_cpp;

import frontend.gm_fe_fixup_bound_symbol;
import frontend.gm_fe_restore_vardecl;
import frontend.gm_symtab;
import frontend.gm_symtab_entry;
import inc.GMEXPR_CLASS;
import inc.GMTYPE_T;
import inc.GM_OPS_T;
import inc.GM_REDUCE_T;
import inc.GlobalMembersGm_backend_cpp;
import inc.gm_assignment_location_t;
import inc.gm_backend;
import inc.gm_code_writer;
import inc.gm_compile_step;
import inc.gm_ind_opt_move_propdecl;
import inc.gm_ind_opt_nonconf_reduce;
import inc.nop_reduce_scalar;
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
import common.GlobalMembersGm_apply_compiler_stage;
import common.GlobalMembersGm_error;
import common.GlobalMembersGm_main;
import common.GlobalMembersGm_misc;
import common.GlobalMembersGm_transform_helper;
import common.gm_builtin_def;
import common.gm_vocabulary;

//-----------------------------------------------------------------
// interface for graph library Layer
//-----------------------------------------------------------------
//C++ TO JAVA CONVERTER TODO TASK: Multiple inheritance is not available in Java:
public class gm_cpp_gen extends gm_backend, gm_code_generator
{
//C++ TO JAVA CONVERTER TODO TASK: Java has no concept of a 'friend' class:
//	friend class nop_reduce_scalar;
	public gm_cpp_gen()
	{
		super(Body);
		this.fname = null;
		this.dname = null;
		this.f_header = null;
		this.f_body = null;
		this._target_omp = false;
		this._pblock = false;
		glib = new gm_cpplib(this);
		init();
	}
	public gm_cpp_gen(gm_cpplib l)
	{
		super(Body);
		this.fname = null;
		this.dname = null;
		this.f_header = null;
		this.f_body = null;
		this._target_omp = false;
		this._pblock = false;
		assert l != null;
		glib = l;
		glib.set_main(this);
		init();
	}
	protected final void init()
	{
		init_opt_steps();
		init_gen_steps();
		build_up_language_voca();
	}

	public void dispose()
	{
		close_output_files();
	}
	public void setTargetDir(String d)
	{
		assert d != null;
		if (dname != null)
			dname = null;
		dname = new byte[d.length() + 1];
		dname = d;
	}
	public void setFileName(String f)
	{
		assert f != null;
		if (fname != null)
			fname = null;
		fname = new byte[f.length() + 1];
		fname = f;
	}

	public boolean do_local_optimize_lib()
	{
		assert get_lib() != null;
		return get_lib().do_local_optimize();
	}
	public boolean do_local_optimize()
	{
		// apply all the optimize steps to all procedures
		return GlobalMembersGm_apply_compiler_stage.gm_apply_compiler_stage(opt_steps);
	}
	public boolean do_generate()
	{
		if (!open_output_files())
			return false;
    
		do_generate_begin();
    
		boolean b = GlobalMembersGm_apply_compiler_stage.gm_apply_compiler_stage(this.gen_steps);
		assert b == true;
    
		do_generate_end();
    
		close_output_files();
    
		return true;
	}
	public void do_generate_begin()
	{
		//----------------------------------
		// header
		//----------------------------------
		add_ifdef_protection(fname);
		add_include("stdio.h", Header);
		add_include("stdlib.h", Header);
		add_include("stdint.h", Header);
		add_include("float.h", Header);
		add_include("limits.h", Header);
		add_include("cmath", Header);
		add_include("algorithm", Header);
		add_include("omp.h", Header);
		//add_include(get_lib()->get_header_info(), Header, false);
		add_include(GlobalMembersGm_backend_cpp.RT_INCLUDE, Header, false);
		Header.NL();
    
		//----------------------------------------
		// Body
		//----------------------------------------
		String.format(temp, "%s.h", fname);
		add_include(temp, Body, false);
		Body.NL();
	}
	public void do_generate_end()
	{
		Header.NL();
		Header.pushln("#endif");
	}

	/*
	 */

	protected java.util.LinkedList<gm_compile_step> opt_steps = new java.util.LinkedList<gm_compile_step>();
	protected java.util.LinkedList<gm_compile_step> gen_steps = new java.util.LinkedList<gm_compile_step>();

	public void build_up_language_voca()
	{
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
	public void init_opt_steps()
	{
		java.util.LinkedList<gm_compile_step> LIST = this.opt_steps;
    
		LIST.addLast(gm_cpp_opt_check_feasible.get_factory());
		LIST.addLast(gm_cpp_opt_defer.get_factory());
		LIST.addLast(gm_cpp_opt_select_par.get_factory());
		LIST.addLast(gm_cpp_opt_save_bfs.get_factory());
		LIST.addLast(gm_ind_opt_move_propdecl.get_factory());
		LIST.addLast(gm_fe_fixup_bound_symbol.get_factory());
		LIST.addLast(gm_ind_opt_nonconf_reduce.get_factory());
		LIST.addLast(gm_cpp_opt_reduce_scalar.get_factory());
	}
	public void init_gen_steps()
	{
		java.util.LinkedList<gm_compile_step> LIST = this.gen_steps;
    
		LIST.addLast(gm_cpp_gen_sanitize_name.get_factory());
		LIST.addLast(gm_cpp_gen_regular.get_factory());
		LIST.addLast(gm_cpp_gen_prop_decl.get_factory());
		LIST.addLast(gm_cpp_gen_mark_parallel.get_factory());
		LIST.addLast(gm_cpp_gen_misc_check.get_factory());
		LIST.addLast(gm_cpp_gen_check_bfs.get_factory());
		LIST.addLast(gm_fe_restore_vardecl.get_factory());
		LIST.addLast(gm_cpp_gen_proc.get_factory());
	}

	public void prepare_parallel_for()
	{
		if (is_under_parallel_sentblock())
			Body.pushln("#pragma omp for nowait"); // already under parallel region.
		else
			Body.pushln("#pragma omp parallel for");
	}
	protected int _ptr;
	protected int _indent;

	public final gm_cpplib get_lib()
	{
		return glib;
	}

	//std::list<const char*> local_names;

	public void set_target_omp(boolean b)
	{
		_target_omp = b;
	}
	public boolean is_target_omp()
	{
		return _target_omp;
	}

	// data structure for generation
	protected String fname; // current source file (without extension)
	protected String dname; // output directory

	protected gm_code_writer Header = new gm_code_writer();
	protected gm_code_writer Body = new gm_code_writer();
	protected FILE f_header;
	protected FILE f_body;
	public boolean open_output_files()
	{
		String temp = new String(new char[1024]);
		assert dname != null;
		assert fname != null;
    
		String.format(temp, "%s/%s.h", dname, fname);
		f_header = fopen(temp, "w");
		if (f_header == null)
		{
			GlobalMembersGm_error.gm_backend_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_FILEWRITE_ERROR, temp);
			return false;
		}
		Header.set_output_file(f_header);
    
		String.format(temp, "%s/%s.cc", dname, fname);
		f_body = fopen(temp, "w");
		if (f_body == null)
		{
			GlobalMembersGm_error.gm_backend_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_FILEWRITE_ERROR, temp);
			return false;
		}
		Body.set_output_file(f_body);
    
		get_lib().set_code_writer(Body);
		return true;
    
	}
	public void close_output_files()
	{
		if (f_header != null)
		{
			Header.flush();
			fclose(f_header);
			f_header = null;
		}
		if (f_body != null)
		{
			Body.flush();
			fclose(f_body);
			f_body = null;
		}
	}

	protected boolean _target_omp;
	protected gm_cpplib glib; // graph library

	// some common sentence
	public void add_include(String String, gm_code_writer Out, boolean is_clib, String str2)
	{
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
	public void add_ifdef_protection(String s)
	{
		Header.push("#ifndef GM_GENERATED_CPP_");
		Header.push_to_upper(s);
		Header.pushln("_H");
		Header.push("#define GM_GENERATED_CPP_");
		Header.push_to_upper(s);
		Header.pushln("_H");
		Header.NL();
	}

	//------------------------------------------------------------------------------
	// Generate Method from gm_code_generator
	//------------------------------------------------------------------------------
	public void generate_rhs_id(ast_id id)
	{
		generate_lhs_id(id);
	}
	public void generate_rhs_field(ast_field f)
	{
		generate_lhs_field(f);
	}
	public void generate_expr_builtin(ast_expr ee)
	{
		ast_expr_builtin e = (ast_expr_builtin) ee;
    
		gm_builtin_def def = e.get_builtin_def();
    
		ast_id driver;
		if (e.driver_is_field())
			driver = ((ast_expr_builtin_field) e).get_field_driver().get_second();
		else
			driver = e.get_driver();
    
		assert def != null;
		int method_id = def.get_method_id();
		if (driver == null)
		{
			boolean add_thread_id = false;
			String func_name = get_function_name(method_id, add_thread_id);
			Body.push(func_name);
			Body.push('(');
			generate_expr_list(e.get_args());
			if (add_thread_id)
			{
				if (e.get_args().size() > 0)
					Body.push(",gm_rt_thread_id()");
				else
					Body.push("gm_rt_thread_id()");
			}
			Body.push(")");
		}
		else
		{
			get_lib().generate_expr_builtin((ast_expr_builtin) e, Body);
		}
	}
	
	public void generate_expr_minmax(ast_expr e)
	{
		if (e.get_optype() == GM_OPS_T.GMOP_MIN)
		{
			Body.push(" std::min(");
		}
		else
		{
			Body.push(" std::max(");
		}
		generate_expr(e.get_left_op());
		Body.push(",");
		generate_expr(e.get_right_op());
		Body.push(") ");
	}
	
	public void generate_expr_abs(ast_expr e)
	{
		Body.push(" std::abs(");
		generate_expr(e.get_left_op());
		Body.push(") ");
	}
	
	public void generate_expr_inf(ast_expr e)
	{
		String temp = temp_str;
		assert e.get_opclass() == GMEXPR_CLASS.GMEXPR_INF;
		GMTYPE_T t = e.get_type_summary();
		switch (t)
		{
			case GMTYPE_INF:
			case GMTYPE_INF_INT:
				String.format(temp, "%s", e.is_plus_inf() ? "INT_MAX" : "INT_MIN");
				break;
			case GMTYPE_INF_LONG:
				String.format(temp, "%s", e.is_plus_inf() ? "LLONG_MAX" : "LLONG_MIN");
				break;
			case GMTYPE_INF_FLOAT:
				String.format(temp, "%s", e.is_plus_inf() ? "FLT_MAX" : "FLT_MIN");
				break;
			case GMTYPE_INF_DOUBLE:
				String.format(temp, "%s", e.is_plus_inf() ? "DBL_MAX" : "DBL_MIN");
				break;
			default:
				System.out.printf("what type is it? %d", t);
				assert false;
				String.format(temp, "%s", e.is_plus_inf() ? "INT_MAX" : "INT_MIN");
				break;
		}
		Body.push(temp);
		return;
	}
	
	public void generate_expr_nil(ast_expr ee)
	{
		get_lib().generate_expr_nil(ee, Body);
	}

	public String get_type_string(GMTYPE_T type_id)
	{
    
		if (type_id.is_prim_type())
		{
			switch (type_id)
			{
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
		}
		else
		{
			return get_lib().get_type_string(type_id);
		}
	}
	public String get_type_string(ast_typedecl t)
	{
		if ((t == null) || (t.is_void()))
		{
			return "void";
		}
    
		if (t.is_primitive())
		{
			return get_type_string(t.get_typeid());
		}
		else if (t.is_property())
		{
			ast_typedecl t2 = t.get_target_type();
			assert t2 != null;
			if (t2.is_primitive())
			{
				switch (t2.get_typeid())
				{
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
			}
			else if (t2.is_nodeedge())
			{
				String temp = new String(new char[128]);
				String.format(temp, "%s*", get_lib().get_type_string(t2));
				return GlobalMembersGm_misc.gm_strdup(temp);
			}
			else if (t2.is_collection())
			{
				String temp = new String(new char[128]);
				String.format(temp, "%s<%s>&", GlobalMembersGm_cpplib_words.PROP_OF_COL, get_lib().get_type_string(t2));
				return GlobalMembersGm_misc.gm_strdup(temp);
			}
			else
			{
				assert false;
			}
		}
		else
			return get_lib().get_type_string(t);
    
		return "ERROR";
	}
	public void generate_lhs_id(ast_id id)
	{
		Body.push(id.get_genname());
	}
	public void generate_lhs_field(ast_field f)
	{
		Body.push(f.get_second().get_genname());
		Body.push('[');
		if (f.is_rarrow())
		{
			String alias_name = f.get_first().getSymInfo().find_info_string(GlobalMembersGm_backend_cpp.CPPBE_INFO_NEIGHBOR_ITERATOR);
			assert alias_name != null;
			assert alias_name.length() > 0;
			Body.push(alias_name);
		}
		else if (f.getTypeInfo().is_node_property())
		{
			Body.push(get_lib().node_index(f.get_first()));
		}
		else if (f.getTypeInfo().is_edge_property())
			Body.push(get_lib().edge_index(f.get_first()));
		else
		{
			assert false;
		}
		Body.push(']');
		return;
	}
	public void generate_sent_nop(ast_nop n)
	{
		switch (n.get_subtype())
		{
			case NOP_REDUCE_SCALAR:
			{
				((nop_reduce_scalar) n).generate(this);
				break;
			}
    
				/* otherwise ask library to hande it */
			default:
			{
				get_lib().generate_sent_nop(n);
				break;
			}
		}
    
		return;
	}
	public void generate_sent_reduce_assign(ast_assign a)
	{
		if (a.is_argminmax_assign())
		{
			generate_sent_reduce_argmin_assign(a);
			return;
		}
    
		else if ((a.get_reduce_type() == GM_REDUCE_T.GMREDUCE_AND) || (a.get_reduce_type() == GM_REDUCE_T.GMREDUCE_OR))
		{
			generate_sent_reduce_assign_boolean(a);
			return;
		}
    
		// implement reduction using compare and swap
		//---------------------------------------
		//  {
		//    <type> OLD, NEW
		//    do {
		//      OLD = LHS;
		//      NEW = LHS <op> RHS;
		//      <optional break> (for min/max)
		//    } while (!__bool_comp_swap(&LHS, OLD, NEW))
		//  }
		//---------------------------------------
		ast_typedecl lhs_target_type = (a.get_lhs_type() == gm_assignment_location_t.GMASSIGN_LHS_SCALA) ? a.get_lhs_scala().getTypeInfo() : a.get_lhs_field().getTypeInfo().get_target_type();
    
		String temp_var_base = (a.get_lhs_type() == gm_assignment_location_t.GMASSIGN_LHS_SCALA) ? a.get_lhs_scala().get_orgname() : a.get_lhs_field().get_second().get_orgname();
    
		GM_REDUCE_T r_type = a.get_reduce_type();
    
		String temp_var_old;
		String temp_var_new;
		boolean is_scalar = (a.get_lhs_type() == gm_assignment_location_t.GMASSIGN_LHS_SCALA);
    
		temp_var_old = GlobalMembersGm_main.FE.voca_temp_name_and_add(temp_var_base, "_old");
		temp_var_new = GlobalMembersGm_main.FE.voca_temp_name_and_add(temp_var_base, "_new");
    
		Body.pushln("// reduction");
		Body.pushln("{ ");
    
		String.format(temp, "%s %s, %s;", get_type_string(lhs_target_type), temp_var_old, temp_var_new);
		Body.pushln(temp);
    
		Body.pushln("do {");
		String.format(temp, "%s = ", temp_var_old);
		Body.push(temp);
		if (is_scalar)
			generate_rhs_id(a.get_lhs_scala());
		else
			generate_rhs_field(a.get_lhs_field());
    
		Body.pushln(";");
		if (r_type == GM_REDUCE_T.GMREDUCE_PLUS)
		{
			String.format(temp, "%s = %s + (", temp_var_new, temp_var_old);
			Body.push(temp);
		}
		else if (r_type == GM_REDUCE_T.GMREDUCE_MULT)
		{
			String.format(temp, "%s = %s * (", temp_var_new, temp_var_old);
			Body.push(temp);
		}
		else if (r_type == GM_REDUCE_T.GMREDUCE_MAX)
		{
			String.format(temp, "%s = std::max (%s, ", temp_var_new, temp_var_old);
			Body.push(temp);
		}
		else if (r_type == GM_REDUCE_T.GMREDUCE_OR)
		{
			String.format(temp, "%s = %s || (", temp_var_new, temp_var_old);
			Body.push(temp);
		}
		else if (r_type == GM_REDUCE_T.GMREDUCE_AND)
		{
			String.format(temp, "%s = %s && (", temp_var_new, temp_var_old);
			Body.push(temp);
		}
		else if (r_type == GM_REDUCE_T.GMREDUCE_MIN)
		{
			String.format(temp, "%s = std::min (%s, ", temp_var_new, temp_var_old);
			Body.push(temp);
		}
		else
		{
			assert false;
		}
    
		generate_expr(a.get_rhs());
		Body.pushln(");");
		if ((r_type == GM_REDUCE_T.GMREDUCE_MAX) || (r_type == GM_REDUCE_T.GMREDUCE_MIN))
		{
			String.format(temp, "if (%s == %s) break;", temp_var_old, temp_var_new);
			Body.pushln(temp);
		}
		Body.push("} while (_gm_atomic_compare_and_swap(&(");
		if (is_scalar)
			generate_rhs_id(a.get_lhs_scala());
		else
			generate_rhs_field(a.get_lhs_field());
    
		String.format(temp, "), %s, %s)==false); ", temp_var_old, temp_var_new);
		Body.pushln(temp);
		Body.pushln("}");
    
		temp_var_new = null;
		temp_var_old = null;
    
		return;
	}
	@Override
	public void generate_sent_defer_assign(ast_assign a)
	{
		assert false;
	} // should not be here
	public void generate_sent_vardecl(ast_vardecl v)
	{
		ast_typedecl t = v.get_type();
    
		if (t.is_collection_of_collection())
		{
			Body.push(get_type_string(t));
			ast_typedecl targetType = t.get_target_type();
			Body.push("<");
			Body.push(get_type_string(t.getTargetTypeSummary()));
			Body.push("> ");
			ast_idlist idl = v.get_idlist();
			assert idl.get_length() == 1;
			generate_lhs_id(idl.get_item(0));
			get_lib().add_collection_def(idl.get_item(0));
			return;
		}
    
		Body.push_spc(get_type_string(t));
    
		if (t.is_property())
		{
			ast_idlist idl = v.get_idlist();
			assert idl.get_length() == 1;
			generate_lhs_id(idl.get_item(0));
			declare_prop_def(t, idl.get_item(0));
		}
		else if (t.is_collection())
		{
			ast_idlist idl = v.get_idlist();
			assert idl.get_length() == 1;
			generate_lhs_id(idl.get_item(0));
			get_lib().add_collection_def(idl.get_item(0));
		}
		else if (t.is_primitive())
		{
			generate_idlist_primitive(v.get_idlist());
			Body.pushln(";");
		}
		else
		{
			generate_idlist(v.get_idlist());
			Body.pushln(";");
		}
	}
	public void generate_sent_foreach(ast_foreach f)
	{
    
		int ptr;
		boolean need_init_before = get_lib().need_up_initializer(f);
    
		if (need_init_before)
		{
			assert f.get_parent().get_nodetype() == AST_NODE_TYPE.AST_SENTBLOCK;
			get_lib().generate_up_initializer(f, Body);
		}
    
		if (f.is_parallel())
		{
			Body.NL();
			prepare_parallel_for();
		}
    
		get_lib().generate_foreach_header(f, Body);
    
		if (get_lib().need_down_initializer(f))
		{
			Body.pushln("{");
			get_lib().generate_down_initializer(f, Body);
    
			if (f.get_body().get_nodetype() != AST_NODE_TYPE.AST_SENTBLOCK)
			{
				generate_sent(f.get_body());
			}
			else
			{
				// '{' '} already handled
				generate_sent_block((ast_sentblock) f.get_body(), false);
			}
			Body.pushln("}");
    
		}
		else if (f.get_body().get_nodetype() == AST_NODE_TYPE.AST_SENTBLOCK)
		{
			generate_sent(f.get_body());
		}
		else
		{
			Body.push_indent();
			generate_sent(f.get_body());
			Body.pop_indent();
			Body.NL();
		}
	}
	public void generate_sent_bfs(ast_bfs[] bfs)
	{
		Body.NL();
    
		//-------------------------------------------
		// (1) create BFS object
		//-------------------------------------------
		String bfs_name = bfs.find_info_string(GlobalMembersGm_backend_cpp.CPPBE_INFO_BFS_NAME);
		String bfs_inst_name = bfs.is_bfs() ? GlobalMembersGm_main.FE.voca_temp_name_and_add("_BFS", "") : GlobalMembersGm_main.FE.voca_temp_name_and_add("_DFS", "");
		String.format(temp, "%s %s", bfs_name, bfs_inst_name);
		Body.push(temp);
		Body.push('(');
    
		//-------------------------------------------
		// give every entry that are used
		//-------------------------------------------
		ast_extra_info_set syms = (ast_extra_info_set) bfs.find_info(GlobalMembersGm_backend_cpp.CPPBE_INFO_BFS_SYMBOLS);
		assert syms != null;
		java.util.HashSet<Object > S = syms.get_set();
		java.util.Iterator<Object > I;
		boolean is_first = true;
		for (I = S.iterator(); I.hasNext();)
		{
			if (!is_first)
			{
				Body.push(", ");
			}
			is_first = false;
			gm_symtab_entry e = (gm_symtab_entry) I.next();
			Body.push(e.getId().get_genname());
		}
		Body.pushln(");");
    
		//-------------------------------------------
		// (2) Make a call to it
		//-------------------------------------------
		if (bfs.is_bfs())
		{
			String.format(temp, "%s.%s(%s, %s());", bfs_inst_name, GlobalMembersGm_backend_cpp.PREPARE, bfs.get_root().get_genname(), GlobalMembersGm_backend_cpp.MAX_THREADS);
			Body.pushln(temp);
			String.format(temp, "%s.%s();", bfs_inst_name, GlobalMembersGm_backend_cpp.DO_BFS_FORWARD);
			Body.pushln(temp);
    
			if (bfs.get_bbody() != null)
			{
				String.format(temp, "%s.%s();", bfs_inst_name, GlobalMembersGm_backend_cpp.DO_BFS_REVERSE);
				Body.pushln(temp);
			}
		} // DFS
		else
		{
			String.format(temp, "%s.%s(%s);", bfs_inst_name, GlobalMembersGm_backend_cpp.PREPARE, bfs.get_root().get_genname());
			Body.pushln(temp);
			String.format(temp, "%s.%s();", bfs_inst_name, GlobalMembersGm_backend_cpp.DO_DFS);
			Body.pushln(temp);
		}
    
		bfs_inst_name = null;
		return;
	}
	public void generate_sent_block(ast_sentblock sb)
	{
		generate_sent_block(sb, true);
	}
	public void generate_sent_block(ast_sentblock sb, boolean need_br)
	{
		if (is_target_omp())
		{
			boolean is_par_scope = sb.find_info_bool(GlobalMembersGm_backend_cpp.LABEL_PAR_SCOPE);
			if (is_par_scope)
			{
				assert is_under_parallel_sentblock() == false;
				set_under_parallel_sentblock(true);
				need_br = true;
				Body.pushln("#pragma omp parallel");
			}
		}
    
		if (need_br)
			Body.pushln("{");
    
		// sentblock exit
		generate_sent_block_enter(sb);
    
		java.util.LinkedList<ast_sent> sents = sb.get_sents();
		java.util.Iterator<ast_sent> it;
		boolean vardecl_started = false;
		boolean other_started = false;
		for (it = sents.iterator(); it.hasNext();)
		{
			// insert newline after end of VARDECL
			ast_sent s = it.next();
			if (!vardecl_started)
			{
				if (s.get_nodetype() == AST_NODE_TYPE.AST_VARDECL)
					vardecl_started = true;
			}
			else
			{
				if (other_started == false)
				{
					if (s.get_nodetype() != AST_NODE_TYPE.AST_VARDECL)
					{
						Body.NL();
						other_started = true;
					}
				}
			}
			generate_sent(it.next());
		}
    
		// sentblock exit
		generate_sent_block_exit(sb);
    
		if (need_br)
			Body.pushln("}");
    
		if (is_under_parallel_sentblock())
			set_under_parallel_sentblock(false);
    
		return;
	}
	public void generate_sent_return(ast_return r)
	{
		if (GlobalMembersGm_main.FE.get_current_proc().find_info_bool(GlobalMembersGm_backend_cpp.CPPBE_INFO_HAS_PROPDECL))
		{
			Body.push(GlobalMembersGm_backend_cpp.CLEANUP_PTR);
			Body.pushln("();");
		}
    
		Body.push("return");
		if (r.get_expr() != null)
		{
			Body.SPC();
			generate_expr(r.get_expr());
		}
		Body.pushln("; ");
	}
	public void generate_sent_call(ast_call c)
	{
		assert c.is_builtin_call();
		generate_expr_builtin(c.get_builtin());
		Body.pushln(";");
	}
	public void generate_sent_assign(ast_assign a)
	{
    
		if (a.is_target_scalar())
		{
			ast_id leftHandSide = a.get_lhs_scala();
			if (leftHandSide.is_instantly_assigned()) //we have to add the variable declaration here
			{
				Body.push(get_lib().get_type_string(leftHandSide.getTypeSummary()));
				Body.push(" ");
			}
			generate_lhs_id(a.get_lhs_scala());
		}
		else
		{
			generate_lhs_field(a.get_lhs_field());
		}
    
		Body.push(" = ");
    
		generate_expr(a.get_rhs());
    
		Body.pushln(" ;");
	}

	public void generate_sent_block_enter(ast_sentblock sb)
	{
		if (sb.find_info_bool(GlobalMembersGm_backend_cpp.CPPBE_INFO_IS_PROC_ENTRY) && !GlobalMembersGm_main.FE.get_current_proc().is_local())
		{
			Body.pushln("//Initializations");
			String.format(temp, "%s();", GlobalMembersGm_backend_cpp.RT_INIT);
			Body.pushln(temp);
    
			//----------------------------------------------------
			// freeze graph instances
			//----------------------------------------------------
			ast_procdef proc = GlobalMembersGm_main.FE.get_current_proc();
			gm_symtab vars = proc.get_symtab_var();
			gm_symtab fields = proc.get_symtab_field();
			//std::vector<gm_symtab_entry*>& E = vars-> get_entries();
			//for(int i=0;i<E.size();i++) {
			// gm_symtab_entry* e = E[i];
			java.util.HashSet<gm_symtab_entry> E = vars.get_entries();
			java.util.Iterator<gm_symtab_entry> I;
			for (I = E.iterator(); I.hasNext();)
			{
				gm_symtab_entry e = I.next();
				if (e.getType().is_graph())
				{
					String.format(temp, "%s.%s();", e.getId().get_genname(), GlobalMembersGm_backend_cpp.FREEZE);
					Body.pushln(temp);
    
					// currrently every graph is an argument
					if (e.find_info_bool(GlobalMembersGm_backend_cpp.CPPBE_INFO_USE_REVERSE_EDGE))
					{
						String.format(temp, "%s.%s();", e.getId().get_genname(), GlobalMembersGm_backend_cpp.MAKE_REVERSE);
						Body.pushln(temp);
					}
					if (e.find_info_bool(GlobalMembersGm_backend_cpp.CPPBE_INFO_NEED_SEMI_SORT))
					{
						boolean has_edge_prop = false;
						// Semi-sorting must be done before edge-property creation
						//std::vector<gm_symtab_entry*>& F = fields-> get_entries();
						//for(int j=0;j<F.size();j++) {
						// gm_symtab_entry* f = F[j];
						java.util.HashSet<gm_symtab_entry> F = fields.get_entries();
						java.util.Iterator<gm_symtab_entry> J;
						for (J = F.iterator(); J.hasNext();)
						{
							gm_symtab_entry f = J.next();
							if ((f.getType().get_target_graph_sym() == e) && (f.getType().is_edge_property()))
								has_edge_prop = true;
						}
						if (has_edge_prop)
						{
							Body.pushln("//[xxx] edge property must be created before semi-sorting");
							String.format(temp, "assert(%s.%s());", e.getId().get_genname(), GlobalMembersGm_backend_cpp.IS_SEMI_SORTED);
							Body.pushln(temp);
						}
						else
						{
							String.format(temp, "%s.%s();", e.getId().get_genname(), GlobalMembersGm_backend_cpp.SEMI_SORT);
							Body.pushln(temp);
						}
					}
    
					if (e.find_info_bool(GlobalMembersGm_backend_cpp.CPPBE_INFO_NEED_FROM_INFO))
					{
						String.format(temp, "%s.%s();", e.getId().get_genname(), GlobalMembersGm_backend_cpp.PREPARE_FROM_INFO);
						Body.pushln(temp);
					}
				}
			}
			Body.NL();
		}
    
	}
	public void generate_sent_block_exit(ast_sentblock sb)
	{
		boolean has_prop_decl = sb.find_info_bool(GlobalMembersGm_backend_cpp.CPPBE_INFO_HAS_PROPDECL);
		boolean is_proc_entry = sb.find_info_bool(GlobalMembersGm_backend_cpp.CPPBE_INFO_IS_PROC_ENTRY);
		boolean has_return_ahead = GlobalMembersGm_transform_helper.gm_check_if_end_with_return(sb);
    
		if (has_prop_decl && !has_return_ahead)
		{
			if (is_proc_entry)
			{
				Body.NL();
				String.format(temp, "%s();", GlobalMembersGm_backend_cpp.CLEANUP_PTR);
				Body.pushln(temp);
			}
			else
			{
				Body.NL();
				gm_symtab tab = sb.get_symtab_field();
				//std::vector<gm_symtab_entry*>& entries = tab->get_entries();
				//std::vector<gm_symtab_entry*>::iterator I;
				java.util.HashSet<gm_symtab_entry> entries = tab.get_entries();
				java.util.Iterator<gm_symtab_entry> I;
				for (I = entries.iterator(); I.hasNext();)
				{
					gm_symtab_entry e = I.next();
					String.format(temp, "%s(%s,%s());", GlobalMembersGm_backend_cpp.DEALLOCATE, e.getId().get_genname(), GlobalMembersGm_backend_cpp.THREAD_ID);
					Body.pushln(temp);
				}
			}
		}
    
	}

	public void generate_idlist(ast_idlist idl)
	{
		int z = idl.get_length();
		for (int i = 0; i < z; i++)
		{
			ast_id id = idl.get_item(i);
			generate_lhs_id(id);
			if (i < z - 1)
				Body.push_spc(',');
		}
	}
	public void generate_proc(ast_procdef proc)
	{
		//-------------------------------
		// declare function name 
		//-------------------------------
		generate_proc_decl(proc, false); // declare in header file
    
		//-------------------------------
		// BFS definitions
		//-------------------------------
		if (proc.find_info_bool(GlobalMembersGm_backend_cpp.CPPBE_INFO_HAS_BFS))
		{
			ast_extra_info_list L = (ast_extra_info_list) proc.find_info(GlobalMembersGm_backend_cpp.CPPBE_INFO_BFS_LIST);
			assert L != null;
			java.util.Iterator<Object > I;
			Body.NL();
			Body.pushln("// BFS/DFS definitions for the procedure");
			for (I = L.get_list().iterator(); I.hasNext();)
			{
				ast_bfs bfs = (ast_bfs) I.next();
				generate_bfs_def(bfs);
			}
		}
    
		//-------------------------------
		// function definition
		//-------------------------------
		generate_proc_decl(proc, true); // declare in body file
		generate_sent(proc.get_body());
		Body.NL();
    
		return;
	}
	public void generate_proc_decl(ast_procdef proc, boolean is_body_file)
	{
		// declare in the header or body
		gm_code_writer Out = is_body_file ? Body : Header;
    
		if (!is_body_file && proc.is_local())
			return;
    
		if (proc.is_local())
			Out.push("static ");
    
		// return type
		Out.push_spc(get_type_string(proc.get_return_type()));
		Out.push(proc.get_procname().get_genname());
		Out.push('(');
    
		int max_arg_per_line = 2;
		int arg_curr = 0;
		int remain_args = proc.get_in_args().size() + proc.get_out_args().size();
		{
			java.util.LinkedList<ast_argdecl> lst = proc.get_in_args();
			java.util.Iterator<ast_argdecl> i;
			for (i = lst.iterator(); i.hasNext();)
			{
				remain_args--;
				arg_curr++;
    
				ast_typedecl T = (i.next()).get_type();
				Out.push(get_type_string(T));
				if (T.is_primitive() || T.is_property())
					Out.push(" ");
				else
					Out.push("& ");
    
				assert (i.next()).get_idlist().get_length() == 1;
				Out.push((i.next()).get_idlist().get_item(0).get_genname());
				if (remain_args > 0)
				{
					Out.push(", ");
				}
    
				if ((arg_curr == max_arg_per_line) && (remain_args > 0))
				{
					Out.NL();
					arg_curr = 0;
				}
			}
		}
		{
			java.util.LinkedList<ast_argdecl> lst = proc.get_out_args();
			java.util.Iterator<ast_argdecl> i;
			for (i = lst.iterator(); i.hasNext();)
			{
				remain_args--;
				arg_curr++;
    
				Out.push(get_type_string((i.next()).get_type()));
				Out.push_spc("&");
				Out.push((i.next()).get_idlist().get_item(0).get_genname());
				if (remain_args > 0)
				{
					Out.push(", ");
				}
				if ((arg_curr == max_arg_per_line) && (remain_args > 0))
				{
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

	protected final boolean is_under_parallel_sentblock()
	{
		return _pblock;
	}
	protected final void set_under_parallel_sentblock(boolean b)
	{
		_pblock = b;
	}

	public void declare_prop_def(ast_typedecl t, ast_id id)
	{
		ast_typedecl t2 = t.get_target_type();
		assert t2 != null;
    
		Body.push(" = ");
		switch (t2.getTypeSummary())
		{
			case GMTYPE_INT:
				Body.push(GlobalMembersGm_backend_cpp.ALLOCATE_INT);
				break;
			case GMTYPE_LONG:
				Body.push(GlobalMembersGm_backend_cpp.ALLOCATE_LONG);
				break;
			case GMTYPE_BOOL:
				Body.push(GlobalMembersGm_backend_cpp.ALLOCATE_BOOL);
				break;
			case GMTYPE_DOUBLE:
				Body.push(GlobalMembersGm_backend_cpp.ALLOCATE_DOUBLE);
				break;
			case GMTYPE_FLOAT:
				Body.push(GlobalMembersGm_backend_cpp.ALLOCATE_FLOAT);
				break;
			case GMTYPE_NODE:
				Body.push(GlobalMembersGm_backend_cpp.ALLOCATE_NODE);
				break;
			case GMTYPE_EDGE:
				Body.push(GlobalMembersGm_backend_cpp.ALLOCATE_EDGE);
				break;
			case GMTYPE_NSET:
			case GMTYPE_ESET:
			case GMTYPE_NSEQ:
			case GMTYPE_ESEQ:
			case GMTYPE_NORDER:
			case GMTYPE_EORDER:
			{
				String temp = new String(new char[128]);
				boolean lazyInitialization = false; //TODO: get some information here to check if lazy init is better
				String.format(temp, "%s<%s, %s>", GlobalMembersGm_backend_cpp.ALLOCATE_COLLECTION, get_lib().get_type_string(t2), (lazyInitialization ? "true" : "false"));
				Body.push(temp);
				break;
			}
			default:
				assert false;
				break;
		}
    
		Body.push('(');
		if (t.is_node_property())
		{
			Body.push(get_lib().max_node_index(t.get_target_graph_id()));
		}
		else if (t.is_edge_property())
		{
			Body.push(get_lib().max_edge_index(t.get_target_graph_id()));
		}
		Body.push(',');
		Body.push(GlobalMembersGm_backend_cpp.THREAD_ID);
		Body.pushln("());");
    
		/*
		 */
    
		// register to memory controller
	}
	public void generate_sent_reduce_argmin_assign(ast_assign a)
	{
		assert a.is_argminmax_assign();
    
		//-----------------------------------------------
		// <LHS; L1,...> min= <RHS; R1,...>;
		//
		// {
		//    RHS_temp = RHS;
		//    if (LHS > RHS_temp) {
		//        <type> L1_temp = R1;
		//        ...
		//        LOCK(LHS) // if LHS is scalar, 
		//                  // if LHS is field, lock the node 
		//            if (LHS > RHS_temp) {
		//               LHS = RHS_temp;
		//               L1 = L1_temp;  // no need to lock for L1. 
		//               ...
		//            }
		//        UNLOCK(LHS)
		//    }
		// }
		//-----------------------------------------------
		Body.pushln("{ // argmin(argmax) - test and test-and-set");
		String rhs_temp;
		int t;
		if (a.is_target_scalar())
		{
			t = a.get_lhs_scala().getTypeSummary();
			rhs_temp = (String) GlobalMembersGm_main.FE.voca_temp_name_and_add(a.get_lhs_scala().get_genname(), "_new");
		}
		else
		{
			t = a.get_lhs_field().get_second().getTargetTypeSummary();
			rhs_temp = (String) GlobalMembersGm_main.FE.voca_temp_name_and_add(a.get_lhs_field().get_second().get_genname(), "_new");
		}
		Body.push(get_type_string(t));
		Body.SPC();
		Body.push(rhs_temp);
		Body.push(" = ");
		generate_expr(a.get_rhs());
		Body.pushln(";");
    
		Body.push("if (");
		if (a.is_target_scalar())
		{
			generate_rhs_id(a.get_lhs_scala());
		}
		else
		{
			generate_rhs_field(a.get_lhs_field());
		}
		if (a.get_reduce_type() == GM_REDUCE_T.GMREDUCE_MIN)
		{
			Body.push(">");
		}
		else
		{
			Body.push("<");
		}
		Body.push(rhs_temp);
		Body.pushln(") {");
    
		java.util.LinkedList<ast_node> L = a.get_lhs_list();
		java.util.LinkedList<ast_expr> R = a.get_rhs_list();
		java.util.Iterator<ast_node> I;
		java.util.Iterator<ast_expr> J;
		String[] names = new byte[L.size()];
		int i = 0;
		J = R.iterator();
		for (I = L.iterator(); I.hasNext(); I++, J++, i++)
		{
			ast_node n = I.next();
			ast_id id;
			int type;
			if (n.get_nodetype() == AST_NODE_TYPE.AST_ID)
			{
				id = (ast_id) n;
				type = id.getTypeSummary();
			}
			else
			{
				assert n.get_nodetype() == AST_NODE_TYPE.AST_FIELD;
				ast_field f = (ast_field) n;
				id = f.get_second();
				type = id.getTargetTypeSummary();
			}
			names[i] = (String) GlobalMembersGm_main.FE.voca_temp_name_and_add(id.get_genname(), "_arg");
			Body.push(get_type_string(type));
			Body.SPC();
			Body.push(names[i]);
			Body.push(" = ");
			generate_expr(J.next());
			Body.pushln(";");
		}
    
		// LOCK, UNLOCK
		String LOCK_FN_NAME;
		String UNLOCK_FN_NAME;
		if (a.is_target_scalar())
		{
			LOCK_FN_NAME = "gm_spinlock_acquire_for_ptr";
			UNLOCK_FN_NAME = "gm_spinlock_release_for_ptr";
		}
		else
		{
			ast_id drv = a.get_lhs_field().get_first();
			if (drv.getTypeInfo().is_node_compatible())
			{
				LOCK_FN_NAME = "gm_spinlock_acquire_for_node";
				UNLOCK_FN_NAME = "gm_spinlock_release_for_node";
			}
			else if (drv.getTypeInfo().is_edge_compatible())
			{
				LOCK_FN_NAME = "gm_spinlock_acquire_for_edge";
				UNLOCK_FN_NAME = "gm_spinlock_release_for_edge";
			}
			else
			{
				assert false;
			}
		}
    
		Body.push(LOCK_FN_NAME);
		Body.push("(");
		if (a.is_target_scalar())
		{
			Body.push("&");
			generate_rhs_id(a.get_lhs_scala());
		}
		else
		{
			generate_rhs_id(a.get_lhs_field().get_first());
		}
		Body.pushln(");");
    
		Body.push("if (");
		if (a.is_target_scalar())
		{
			generate_rhs_id(a.get_lhs_scala());
		}
		else
		{
			generate_rhs_field(a.get_lhs_field());
		}
		if (a.get_reduce_type() == GM_REDUCE_T.GMREDUCE_MIN)
		{
			Body.push(">");
		}
		else
		{
			Body.push("<");
		}
		Body.push(rhs_temp);
		Body.pushln(") {");
    
		// lhs = rhs_temp
		if (a.is_target_scalar())
		{
			generate_lhs_id(a.get_lhs_scala());
		}
		else
		{
			generate_lhs_field(a.get_lhs_field());
		}
		Body.push(" = ");
		Body.push(rhs_temp);
		Body.pushln(";");
    
		i = 0;
		for (I = L.iterator(); I.hasNext(); I++, i++)
		{
			ast_node n = I.next();
			if (n.get_nodetype() == AST_NODE_TYPE.AST_ID)
			{
				generate_lhs_id((ast_id) n);
			}
			else
			{
				generate_lhs_field((ast_field) n);
			}
			Body.push(" = ");
			Body.push(names[i]);
			Body.pushln(";");
		}
    
		Body.pushln("}"); // end of inner if
    
		Body.push(UNLOCK_FN_NAME);
		Body.push("(");
		if (a.is_target_scalar())
		{
			Body.push("&");
			generate_rhs_id(a.get_lhs_scala());
		}
		else
		{
			generate_rhs_id(a.get_lhs_field().get_first());
		}
		Body.pushln(");");
    
		Body.pushln("}"); // end of outer if
    
		Body.pushln("}"); // end of reduction
		// clean-up
		for (i = 0; i < (int) L.size(); i++)
			names[i] = null;
		names = null;
		rhs_temp = null;
	}
	public void generate_sent_reduce_assign_boolean(ast_assign a)
	{
		// implement reduction using compare and swap
		//---------------------------------------
		//  bool NEW
		//  NEW = RHS;
		//  // for or-reduction
		//  if (NEW) LHS = TRUE
		//  // for and-reduciton
		//  if (!NEW) LHS = FALSE
		//---------------------------------------
		String temp_var_base = (a.get_lhs_type() == gm_assignment_location_t.GMASSIGN_LHS_SCALA) ? a.get_lhs_scala().get_orgname() : a.get_lhs_field().get_second().get_orgname();
    
		String temp_var_new;
		temp_var_new = GlobalMembersGm_main.FE.voca_temp_name_and_add(temp_var_base, "_new");
		boolean is_scalar = (a.get_lhs_type() == gm_assignment_location_t.GMASSIGN_LHS_SCALA);
    
		Body.pushln("// boolean reduction (no need CAS)");
		Body.pushln("{ ");
    
		String.format(temp, "bool %s;", temp_var_new);
		Body.pushln(temp);
		String.format(temp, "%s = ", temp_var_new);
		Body.push(temp);
		generate_expr(a.get_rhs());
		Body.pushln(";");
    
		if (a.get_reduce_type() == GM_REDUCE_T.GMREDUCE_AND)
		{
			Body.pushln("// and-reduction");
			String.format(temp, "if ((!%s) ", temp_var_new);
			Body.push(temp); // new value is false
			String.format(temp, "&& ( ");
			Body.push(temp); // old value is true
			if (is_scalar)
				generate_rhs_id(a.get_lhs_scala());
			else
				generate_rhs_field(a.get_lhs_field());
			Body.pushln("))");
			Body.push_indent();
			if (is_scalar)
				generate_rhs_id(a.get_lhs_scala());
			else
				generate_rhs_field(a.get_lhs_field());
			Body.pushln(" = false;");
			Body.pop_indent();
		}
		else if (a.get_reduce_type() == GM_REDUCE_T.GMREDUCE_OR)
		{
			Body.pushln("// or-reduction");
			String.format(temp, "if ((%s) ", temp_var_new);
			Body.push(temp); // new value is true
			String.format(temp, "&& (! ");
			Body.push(temp); // old value is false
			if (is_scalar)
				generate_rhs_id(a.get_lhs_scala());
			else
				generate_rhs_field(a.get_lhs_field());
			Body.pushln("))");
			Body.push_indent();
			if (is_scalar)
				generate_rhs_id(a.get_lhs_scala());
			else
				generate_rhs_field(a.get_lhs_field());
			Body.pushln(" = true;");
			Body.pop_indent();
		}
		else
		{
			assert false;
		}
		Body.pushln("}");
		temp_var_new = null;
	}

	protected boolean _pblock;

	public void generate_bfs_def(ast_bfs bfs)
	{
		String bfs_name = bfs.find_info_string(GlobalMembersGm_backend_cpp.CPPBE_INFO_BFS_NAME);
		String level_t = "short";
		String use_multithread = GlobalMembersGm_cpp_gen_bfs.bool_string(is_target_omp());
		String save_child = GlobalMembersGm_cpp_gen_bfs.bool_string(bfs.find_info_bool(GlobalMembersGm_backend_cpp.CPPBE_INFO_USE_DOWN_NBR));
		String use_reverse_edge = GlobalMembersGm_cpp_gen_bfs.bool_string(bfs.is_transpose());
		String has_navigator = GlobalMembersGm_cpp_gen_bfs.bool_string(bfs.get_navigator() != null);
    
		String has_pre_visit = GlobalMembersGm_cpp_gen_bfs.bool_string((bfs.get_fbody() != null) && (bfs.get_fbody().get_sents().size() >= 1));
    
		String has_post_visit = GlobalMembersGm_cpp_gen_bfs.bool_string((bfs.get_bbody() != null) && (bfs.get_bbody().get_sents().size() >= 1));
    
		ast_extra_info_set info = (ast_extra_info_set) bfs.find_info(GlobalMembersGm_backend_cpp.CPPBE_INFO_BFS_SYMBOLS);
		java.util.HashSet<Object > SET = info.get_set();
		java.util.Iterator<Object > S;
		gm_symtab_entry graph_sym = (gm_symtab_entry)(*(SET.iterator()));
		String template_name = (bfs.is_bfs() ? GlobalMembersGm_backend_cpp.BFS_TEMPLATE : GlobalMembersGm_backend_cpp.DFS_TEMPLATE);
    
		String.format(temp, "class %s : public %s", bfs_name, template_name);
		Body.pushln(temp);
		Body.push_indent();
		if (bfs.is_bfs())
		{
			String.format(temp, "<%s, %s, %s, %s, %s>", level_t, use_multithread, has_navigator, use_reverse_edge, save_child);
		}
		else
		{
			String.format(temp, "<%s, %s, %s, %s>", has_pre_visit, has_post_visit, has_navigator, use_reverse_edge);
		}
    
		Body.pushln(temp);
		Body.pop_indent();
		Body.pushln("{");
    
		Body.pop_indent();
		Body.pushln("public:");
		Body.push_indent();
    
		String.format(temp, "%s(", bfs_name);
		Body.push(temp);
    
		//------------------------------------------
		// constructor
		//------------------------------------------
		boolean is_first = true;
		int total = SET.size();
		int i = 0;
		int NL = 3;
		for (S = SET.iterator(); S.hasNext(); S++, i++)
		{
			if (!is_first)
				Body.push(", ");
			if ((i > 0) && (i != total) && ((i % NL) == 0))
				Body.NL();
			is_first = false;
			gm_symtab_entry sym = (gm_symtab_entry)(S.next());
			ast_typedecl t = sym.getType();
			Body.push(get_type_string(t));
			Body.push("&");
			Body.push(" _");
			Body.push(sym.getId().get_genname());
		}
		Body.pushln(")");
		String.format(temp, ": %s", template_name);
		Body.push(temp);
		if (bfs.is_bfs())
		{
			String.format(temp, "<%s, %s, %s, %s, %s>", level_t, use_multithread, has_navigator, use_reverse_edge, save_child);
		}
		else
		{
			String.format(temp, "<%s, %s, %s, %s>", has_pre_visit, has_post_visit, has_navigator, use_reverse_edge);
		}
		Body.push(temp);
		String.format(temp, "(_%s),", graph_sym.getId().get_genname());
		Body.pushln(temp);
    
		// init list
		is_first = true;
		NL = 6;
		i = 0;
		for (S = SET.iterator(); S.hasNext(); S++, i++)
		{
			if (!is_first)
				Body.push(", ");
			if ((i > 0) && (i != total) && ((i % NL) == 0))
				Body.NL();
			is_first = false;
			gm_symtab_entry sym = (gm_symtab_entry)(S.next());
			Body.push(sym.getId().get_genname());
			Body.push('(');
			Body.push('_');
			Body.push(sym.getId().get_genname());
			Body.push(')');
		}
		Body.pushln("{}");
		Body.NL();
    
		//-------------------------------------------------
		// list of scope variables
		//-------------------------------------------------
		Body.pop_indent();
		Body.pushln("private:  // list of varaibles");
		Body.push_indent();
		for (S = SET.iterator(); S.hasNext(); S++, i++)
		{
			gm_symtab_entry sym = (gm_symtab_entry)(S.next());
			ast_typedecl t = sym.getType();
			Body.push(get_type_string(t));
			Body.push("& ");
			Body.push(sym.getId().get_genname());
			Body.pushln(";");
		}
		Body.NL();
    
		Body.pop_indent();
		Body.pushln("protected:");
		Body.push_indent();
    
		ast_id iter = bfs.get_iterator();
		String a_name = GlobalMembersGm_main.FE.voca_temp_name_and_add(iter.get_orgname(), "_idx");
		iter.getSymInfo().add_info_string(GlobalMembersGm_backend_cpp.CPPBE_INFO_NEIGHBOR_ITERATOR, a_name);
		a_name = null;
    
		generate_bfs_body_fw(bfs);
		generate_bfs_body_bw(bfs);
		generate_bfs_navigator(bfs);
		Body.NL();
    
		Body.NL();
		Body.pushln("};");
		Body.NL();
	}
	public void generate_bfs_body_fw(ast_bfs bfs)
	{
		assert bfs.get_f_filter() == null;
		// should be changed into if already
    
		if (bfs.is_bfs())
		{
			Body.push("virtual void visit_fw(");
		}
		else
		{
			Body.push("virtual void visit_pre(");
		}
		Body.push(get_lib().get_type_string(GMTYPE_T.GMTYPE_NODE));
		Body.SPC();
		Body.push(bfs.get_iterator().get_genname());
		Body.push(") ");
		if ((bfs.get_fbody() == null) || (bfs.get_fbody().get_sents().size() == 0))
		{
			Body.pushln("{}");
		}
		else
		{
			Body.NL();
			ast_sentblock sb = bfs.get_fbody();
			generate_sent_block(sb);
			Body.NL();
		}
	}
	public void generate_bfs_body_bw(ast_bfs bfs)
	{
		assert bfs.get_b_filter() == null;
		// should be changed into if already
    
		if (bfs.is_bfs())
		{
			Body.push("virtual void visit_rv(");
		}
		else
		{
			Body.push("virtual void visit_post(");
		}
    
		Body.push(get_lib().get_type_string(GMTYPE_T.GMTYPE_NODE));
		Body.SPC();
		Body.push(bfs.get_iterator().get_genname());
		Body.push(") ");
		if (bfs.get_bbody() == null)
		{
			Body.pushln("{}");
		}
		else
		{
			Body.NL();
			ast_sentblock sb = bfs.get_bbody();
			generate_sent_block(sb);
			Body.NL();
		}
	}
	public void generate_bfs_navigator(ast_bfs bfs)
	{
		Body.push("virtual bool check_navigator(");
		Body.push(get_lib().get_type_string(GMTYPE_T.GMTYPE_NODE));
		Body.SPC();
		Body.push(bfs.get_iterator().get_genname());
		Body.push(", ");
		Body.push(get_lib().get_type_string(GMTYPE_T.GMTYPE_EDGE));
		Body.SPC();
		String alias_name = bfs.get_iterator().getSymInfo().find_info_string(GlobalMembersGm_backend_cpp.CPPBE_INFO_NEIGHBOR_ITERATOR);
		assert alias_name != null;
		assert alias_name.length() > 0;
		Body.push(alias_name);
		Body.push(") ");
		if (bfs.get_navigator() == null)
		{
			Body.pushln("{return true;}");
    
		}
		else
		{
			Body.NL();
			Body.pushln("{");
			Body.push("return (");
			ast_expr nv = bfs.get_navigator();
			generate_expr(nv);
			Body.pushln(");");
			Body.pushln("}");
		}
	}

	protected String i_temp; // temporary variable name
	protected String temp = new String(new char[2048]);

	public String get_function_name(int methodId, tangible.RefObject<Boolean> addThreadId)
	{
		switch (methodId)
		{
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
	public void generate_idlist_primitive(ast_idlist idList)
	{
		int length = idList.get_length();
		for (int i = 0; i < length; i++)
		{
			ast_id id = idList.get_item(i);
			generate_lhs_id(id);
			generate_lhs_default(id.getTypeSummary());
			if (i < length - 1)
				Body.push_spc(',');
		}
	}
	public void generate_lhs_default(int type)
	{
		switch (type)
		{
			case GMTYPE_BYTE:
			case GMTYPE_SHORT:
			case GMTYPE_INT:
			case GMTYPE_LONG:
				Body.push_spc(" = 0");
				break;
			case GMTYPE_FLOAT:
			case GMTYPE_DOUBLE:
				Body.push_spc(" = 0.0");
				break;
			case GMTYPE_BOOL:
				Body.push_spc(" = false");
				break;
			default:
				assert false;
				return;
		}
	}
}