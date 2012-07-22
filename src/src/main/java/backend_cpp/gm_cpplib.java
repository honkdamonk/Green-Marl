package backend_cpp;

import ast.ast_expr;
import ast.ast_expr_builtin;
import ast.ast_expr_builtin_field;
import ast.ast_field;
import ast.ast_foreach;
import ast.ast_id;
import ast.ast_nop;
import ast.ast_sent;
import ast.ast_typedecl;
import inc.GMTYPE_T;
import inc.GlobalMembersGm_backend_cpp;
import inc.GlobalMembersGm_defs;
import inc.gm_code_writer;
import inc.gm_graph_library;

import common.GlobalMembersGm_main;
import common.GlobalMembersGm_misc;
import common.GlobalMembersGm_transform_helper;
import common.gm_builtin_def;
import common.gm_method_id_t;
import common.gm_vocabulary;

//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define TO_STR(X) #X
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define DEF_STRING(X) static const char *X = "X"
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP(CLASS, DESC) class CLASS : public gm_compile_step { private: CLASS() {set_description(DESC);}public: virtual void process(ast_procdef*p); virtual gm_compile_step* get_instance(){return new CLASS();} static gm_compile_step* get_factory(){return new CLASS();} };
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP_FACTORY(CLASS) CLASS::get_factory()


//-----------------------------------------------------------------
// interface for graph library Layer
//  ==> will be deprecated
//-----------------------------------------------------------------
//C++ TO JAVA CONVERTER NOTE: Java has no need of forward class declarations:
//class gm_cpp_gen;
public class gm_cpplib extends gm_graph_library
{
	public gm_cpplib()
	{
		main = null;
	}
	public gm_cpplib(gm_cpp_gen gen)
	{
		main = gen;
	}
	public final void set_main(gm_cpp_gen gen)
	{
		main = gen;
	}

	public String get_header_info()
	{
		return "gm.h";
	}
	public String get_type_string(GMTYPE_T type)
	{
		if (GlobalMembersGm_defs.gm_is_graph_type(type))
		{
			return GlobalMembersGm_cpplib_words.GRAPH_T;
		}
		else if (GlobalMembersGm_defs.gm_is_nodeedge_type(type))
		{
			if (GlobalMembersGm_defs.gm_is_node_type(type))
				return GlobalMembersGm_cpplib_words.NODE_T;
			else
				return GlobalMembersGm_cpplib_words.EDGE_T;
		}
		else if (GlobalMembersGm_defs.gm_is_iter_type(type))
		{
			if (GlobalMembersGm_defs.gm_is_node_iter_type(type))
			{
				return GlobalMembersGm_cpplib_words.NODEITER_T;
			}
			else if (GlobalMembersGm_defs.gm_is_edge_iter_type(type))
			{
				return GlobalMembersGm_cpplib_words.EDGEITER_T;
			}
			else if (GlobalMembersGm_defs.gm_is_node_compatible_type(type))
				return GlobalMembersGm_cpplib_words.NODE_T;
			else if (GlobalMembersGm_defs.gm_is_edge_compatible_type(type))
				return GlobalMembersGm_cpplib_words.EDGE_T;
			else
			{
				assert false;
				return "ERROR";
			}
		}
		else if (GlobalMembersGm_defs.gm_is_collection_type(type))
		{
			assert GlobalMembersGm_defs.gm_is_node_collection_type(type) || GlobalMembersGm_defs.gm_is_collection_of_collection_type(type);
			if (GlobalMembersGm_defs.gm_is_set_collection_type(type))
				return GlobalMembersGm_cpplib_words.SET_T;
			else if (GlobalMembersGm_defs.gm_is_order_collection_type(type))
				return GlobalMembersGm_cpplib_words.ORDER_T;
			else if (GlobalMembersGm_defs.gm_is_sequence_collection_type(type))
				return GlobalMembersGm_cpplib_words.SEQ_T;
			else if (GlobalMembersGm_defs.gm_is_collection_of_collection_type(type))
				return GlobalMembersGm_cpplib_words.QUEUE_T;
			else
			{
				assert false;
				return "ERROR";
			}
		}
		else if (GlobalMembersGm_defs.gm_is_collection_of_collection_type(type))
		{
			return GlobalMembersGm_cpplib_words.QUEUE_T;
		}
		else
		{
			System.out.printf("type = %d %s\n", type, GlobalMembersGm_misc.gm_get_type_string(type));
			assert false;
			return "ERROR";
		}
	}
	public String get_type_string(ast_typedecl t)
	{
		return get_type_string(t.getTypeSummary());
	}

	public String max_node_index(ast_id graph)
	{
		String.format(str_buf, "%s.%s()", graph.get_genname(), GlobalMembersGm_cpplib_words.NUM_NODES);
		return str_buf;
	}
	public String max_edge_index(ast_id graph)
	{
		String.format(str_buf, "%s.%s()", graph.get_genname(), GlobalMembersGm_cpplib_words.NUM_EDGES);
		return str_buf;
	}
	public String node_index(ast_id iter)
	{
		// should check iterator type????
		return iter.get_genname();
	}
	public String edge_index(ast_id iter)
	{
		// should check iterator type????
		return iter.get_genname();
	}

	public boolean do_local_optimize()
	{
		String[] NAMES = {"[(nothing)]"};
	//C++ TO JAVA CONVERTER WARNING: This 'sizeof' ratio was replaced with a direct reference to the array length:
	//ORIGINAL LINE: const int COUNT = sizeof(NAMES) / sizeof(String);
		final int COUNT = NAMES.length;
    
		boolean is_okay = true;
    
		for (int i = 0; i < COUNT; i++)
		{
			GlobalMembersGm_main.gm_begin_minor_compiler_stage(i + 1, NAMES[i]);
			{
				switch (i)
				{
					case 0:
						break;
					case COUNT:
					default:
						assert false;
						break;
				}
			}
			GlobalMembersGm_main.gm_end_minor_compiler_stage();
			if (!is_okay)
				break;
		}
		return is_okay;
	}

	public void generate_sent_nop(ast_nop f)
	{
		int subtype = f.get_subtype();
		switch (subtype)
		{
			default:
				assert false;
				break;
		}
	}
	public void generate_expr_builtin(ast_expr_builtin e, gm_code_writer Body)
	{
    
		if (e.driver_is_field())
		{
			generate_expr_builtin_field((ast_expr_builtin_field) e, Body);
			return;
		}
    
		ast_id i = e.get_driver(); // driver
		gm_builtin_def def = e.get_builtin_def();
		ast_sent s = GlobalMembersGm_transform_helper.gm_find_parent_sentence(e);
		assert def != null;
		assert s != null;
		GMTYPE_T src_type = def.get_source_type_summary();
		gm_method_id_t method_id = def.get_method_id();
		boolean under_parallel = s.is_under_parallel_execution();
		boolean add_thread_id = false;
    
		String func_name;
		switch (src_type)
		{
			case GMTYPE_GRAPH:
				func_name = get_function_name_graph(method_id);
				break;
			case GMTYPE_NODE:
				switch (method_id)
				{
					case GM_BLTIN_NODE_DEGREE:
						assert i.getTypeInfo().get_target_graph_id() != null;
						String.format(str_buf, "(%s.%s[%s+1] - %s.%s[%s])", i.getTypeInfo().get_target_graph_id().get_genname(), GlobalMembersGm_cpplib_words.BEGIN, i.get_genname(), i.getTypeInfo().get_target_graph_id().get_genname(), GlobalMembersGm_cpplib_words.BEGIN, i.get_genname());
						Body.push(str_buf);
						break;
					case GM_BLTIN_NODE_IN_DEGREE:
						assert i.getTypeInfo().get_target_graph_id() != null;
						String.format(str_buf, "(%s.%s[%s+1] - %s.%s[%s])", i.getTypeInfo().get_target_graph_id().get_genname(), GlobalMembersGm_cpplib_words.R_BEGIN, i.get_genname(), i.getTypeInfo().get_target_graph_id().get_genname(), GlobalMembersGm_cpplib_words.R_BEGIN, i.get_genname());
						Body.push(str_buf);
						break;
					case GM_BLTIN_NODE_IS_NBR:
						assert i.getTypeInfo().get_target_graph_id() != null;
						String.format(str_buf, "%s.is_neighbor(", i.getTypeInfo().get_target_graph_id().get_genname());
						Body.push(str_buf);
						main.generate_expr(e.get_args().getFirst());
						String.format(str_buf, ",%s)", i.get_genname());
						Body.push(str_buf);
						break;
					case GM_BLTIN_NODE_HAS_EDGE_TO:
						assert i.getTypeInfo().get_target_graph_id() != null;
						String.format(str_buf, "%s.has_edge_to(", i.getTypeInfo().get_target_graph_id().get_genname());
						Body.push(str_buf);
						main.generate_expr(e.get_args().getFirst());
						String.format(str_buf, ",%s)", i.get_genname());
						Body.push(str_buf);
						break;
					case GM_BLTIN_NODE_RAND_NBR:
						assert i.getTypeInfo().get_target_graph_id() != null;
						String.format(str_buf, "%s.pick_random_out_neighbor(%s)", i.getTypeInfo().get_target_graph_id().get_genname(), i.get_genname());
						Body.push(str_buf);
						break;
					default:
						assert false;
						break;
				}
				return;
			case GMTYPE_NODEITER_NBRS:
			case GMTYPE_NODEITER_IN_NBRS:
			case GMTYPE_NODEITER_UP_NBRS:
			case GMTYPE_NODEITER_DOWN_NBRS:
				switch (method_id)
				{
					case GM_BLTIN_NODE_TO_EDGE:
					{
						String alias_name = i.getSymInfo().find_info_string(GlobalMembersGm_backend_cpp.CPPBE_INFO_NEIGHBOR_ITERATOR);
						assert alias_name != null;
						assert alias_name.length() > 0;
						String.format(str_buf, "%s", alias_name);
					}
						break;
					default:
						assert false;
						break;
				}
				Body.push(str_buf);
				return;
			case GMTYPE_EDGE:
				switch (method_id)
				{
					case GM_BLTIN_EDGE_FROM:
					{
						String.format(str_buf, "%s.%s[%s]", i.getTypeInfo().get_target_graph_id().get_genname(), GlobalMembersGm_cpplib_words.FROM_IDX, i.get_genname());
					}
						break;
					case GM_BLTIN_EDGE_TO:
					{
						String.format(str_buf, "%s.%s[%s]", i.getTypeInfo().get_target_graph_id().get_genname(), GlobalMembersGm_cpplib_words.NODE_IDX, i.get_genname());
					}
						break;
					default:
						assert false;
						break;
				}
				Body.push(str_buf);
				return;
			case GMTYPE_NSET:
				func_name = get_function_name_nset(method_id, under_parallel);
				break;
			case GMTYPE_NORDER:
				func_name = get_function_name_norder(method_id);
				break;
			case GMTYPE_NSEQ:
				func_name = get_function_name_nseq(method_id);
				break;
			default:
				assert false;
				break;
		}
    
		String.format(str_buf, "%s.%s(", i.get_genname(), func_name);
		Body.push(str_buf);
		add_arguments_and_thread(Body, e, add_thread_id);
	}
	public void generate_expr_nil(ast_expr e, gm_code_writer Body)
	{
		if (e.get_type_summary() == GMTYPE_T.GMTYPE_NIL_EDGE)
		{
			Body.push("gm_graph::NIL_EDGE");
		}
		else if (e.get_type_summary() == GMTYPE_T.GMTYPE_NIL_NODE)
		{
			Body.push("gm_graph::NIL_NODE");
		}
		else
		{
			assert false;
		}
	}

	public boolean add_collection_def(ast_id i)
	{
		Body.push("(");
    
		ast_typedecl t = i.getTypeInfo();
		if (t.is_set_collection() || t.is_order_collection() || t.is_collection_of_collection())
		{
			// total size;
			assert t.get_target_graph_id() != null;
    
			if (!t.is_collection_of_collection())
				Body.push(t.get_target_graph_id().get_genname());
			if (t.is_node_collection())
			{
				Body.push(".");
				Body.push(GlobalMembersGm_cpplib_words.NUM_NODES);
				Body.push("()");
			}
			else if (t.is_edge_collection())
			{
				Body.push(".");
				Body.push(GlobalMembersGm_cpplib_words.NUM_EDGES);
				Body.push("()");
			}
			else if (t.is_collection_of_collection())
			{
				assert true;
			}
			else
			{
				assert false;
			}
		}
		if (t.is_order_collection())
			Body.push(", ");
    
		if (t.is_order_collection() || t.is_sequence_collection() || t.is_collection_of_collection())
		{
			Body.push(GlobalMembersGm_backend_cpp.MAX_THREADS);
			Body.push("()");
		}
    
		Body.pushln(");");
    
		return false;
	}

	public void build_up_language_voca(gm_vocabulary V)
	{
		V.add_word(GlobalMembersGm_cpplib_words.NODE_T);
		V.add_word(GlobalMembersGm_cpplib_words.EDGE_T);
		V.add_word(GlobalMembersGm_cpplib_words.GRAPH_T);
		V.add_word(GlobalMembersGm_cpplib_words.SET_T);
		V.add_word(GlobalMembersGm_cpplib_words.ORDER_T);
		V.add_word(GlobalMembersGm_cpplib_words.NODE_IDX);
		V.add_word(GlobalMembersGm_cpplib_words.EDGE_IDX);
		V.add_word(GlobalMembersGm_cpplib_words.R_NODE_IDX);
		V.add_word(GlobalMembersGm_cpplib_words.R_EDGE_IDX);
		V.add_word(GlobalMembersGm_cpplib_words.BEGIN);
		V.add_word(GlobalMembersGm_cpplib_words.R_BEGIN);
	}

	public boolean need_up_initializer(ast_foreach f)
	{
		GMTYPE_T iter_type = f.get_iter_type();
		if (GlobalMembersGm_defs.gm_is_iteration_on_collection(iter_type))
			return true;
		else if (GlobalMembersGm_defs.gm_is_common_nbr_iter_type(iter_type))
			return true;
		return false;
	}
	public boolean need_down_initializer(ast_foreach f)
	{
		GMTYPE_T iter_type = f.get_iter_type();
    
		if (GlobalMembersGm_defs.gm_is_iteration_on_collection(iter_type))
		{
			return true;
		}
		else if (GlobalMembersGm_defs.gm_is_common_nbr_iter_type(iter_type))
		{
			return false;
		}
		// in/out/up/down
		else if (GlobalMembersGm_defs.gm_is_iteration_on_neighbors_compatible(iter_type))
		{
			return true;
		}
    
		return false;
	}
	public void generate_up_initializer(ast_foreach f, gm_code_writer Body)
	{
		GMTYPE_T iter_type = f.get_iter_type();
		ast_id source = f.get_source();
		if (GlobalMembersGm_defs.gm_is_iteration_on_collection(iter_type))
		{
			assert!f.is_parallel();
			// for temp
			String iter_type_str = f.is_parallel() ? "par_iter" : f.is_reverse_iteration() ? "rev_iter" : "seq_iter";
    
			String prep_str = f.is_parallel() ? "prepare_par_iteration" : f.is_reverse_iteration() ? "prepare_rev_iteration" : "prepare_seq_iteration";
    
			// get a list
			String typeString = null;
			if (GlobalMembersGm_defs.gm_is_collection_of_collection_type(source.getTypeSummary()))
				String.format(str_buf, "%s<%s>::%s", get_type_string(source.getTypeInfo()), get_type_string(source.getTargetTypeInfo()), iter_type_str);
			else
				String.format(str_buf, "%s::%s", get_type_string(source.getTypeInfo()), iter_type_str);
			Body.push(str_buf);
    
			String a_name = GlobalMembersGm_main.FE.voca_temp_name_and_add(f.get_iterator().get_orgname(), "_I");
			f.add_info_string(GlobalMembersGm_backend_cpp.CPPBE_INFO_COLLECTION_ITERATOR, a_name);
			String.format(str_buf, " %s", a_name);
			Body.push(str_buf);
			a_name = null;
    
			String.format(str_buf, " = %s.%s();", source.get_genname(), prep_str);
			Body.pushln(str_buf);
		}
		else if (GlobalMembersGm_defs.gm_is_common_nbr_iter_type(iter_type))
		{
			ast_id graph = source.getTypeInfo().get_target_graph_id();
			ast_id source2 = f.get_source2();
			assert source2 != null;
			String a_name = GlobalMembersGm_main.FE.voca_temp_name_and_add(f.get_iterator().get_orgname(), "_I");
			f.add_info_string(GlobalMembersGm_backend_cpp.CPPBE_INFO_COMMON_NBR_ITERATOR, a_name);
			Body.pushln("// Iterate over Common neighbors");
			String.format(str_buf, "gm_common_neighbor_iter %s(%s, %s, %s);", a_name, graph.get_genname(), source.get_genname(), source2.get_genname());
			Body.pushln(str_buf);
		}
	}
	public void generate_down_initializer(ast_foreach f, gm_code_writer Body)
	{
		GMTYPE_T iter_type = f.get_iter_type();
		ast_id iter = f.get_iterator();
		ast_id source = f.get_source();
    
		if (GlobalMembersGm_defs.gm_is_iteration_on_collection(iter_type))
		{
			assert f.find_info(GlobalMembersGm_backend_cpp.CPPBE_INFO_COLLECTION_ITERATOR) != null;
			String lst_iter_name = f.find_info_string(GlobalMembersGm_backend_cpp.CPPBE_INFO_COLLECTION_ITERATOR);
			String type_name;
			if (GlobalMembersGm_defs.gm_is_collection_of_collection_type(source.getTypeSummary()))
				type_name = get_type_string(source.getTargetTypeInfo());
			else
				type_name = source.getTypeInfo().is_node_collection() ? GlobalMembersGm_cpplib_words.NODE_T : GlobalMembersGm_cpplib_words.EDGE_T;
    
			String.format(str_buf, "%s %s = %s.get_next();", type_name, f.get_iterator().get_genname(), lst_iter_name);
			Body.pushln(str_buf);
		}
		else if (GlobalMembersGm_defs.gm_is_iteration_on_neighbors_compatible(iter_type))
		{
			String alias_name = f.find_info_string(GlobalMembersGm_backend_cpp.CPPBE_INFO_NEIGHBOR_ITERATOR);
			String type_name = get_type_string(iter_type);
			String graph_name = source.getTypeInfo().get_target_graph_id().get_genname();
			String var_name = iter.get_genname();
			String array_name;
    
			// should be neighbor iterator
			assert GlobalMembersGm_defs.gm_is_iteration_on_nodes(iter_type);
    
			// [XXX] should be changed if G is transposed!
			array_name = GlobalMembersGm_defs.gm_is_iteration_use_reverse(iter_type) ? GlobalMembersGm_cpplib_words.R_NODE_IDX : GlobalMembersGm_cpplib_words.NODE_IDX;
    
			if (GlobalMembersGm_defs.gm_is_iteration_on_down_neighbors(iter_type))
			{
				String.format(str_buf, "if (!is_down_edge(%s)) continue;", alias_name);
				Body.pushln(str_buf);
    
				String.format(str_buf, "%s %s = %s.%s [%s];", type_name, var_name, graph_name, array_name, alias_name);
				Body.pushln(str_buf);
			}
			else if (GlobalMembersGm_defs.gm_is_iteration_on_updown_levels(iter_type))
			{
				String.format(str_buf, "%s %s = %s.%s [%s];", type_name, var_name, graph_name, array_name, alias_name);
				Body.pushln(str_buf);
    
				String.format(str_buf, "if (get_level(%s) != (get_curr_level() %c 1)) continue;", iter.get_genname(), GlobalMembersGm_defs.gm_is_iteration_on_up_neighbors(iter_type) ? '-' : '+');
				Body.pushln(str_buf);
			}
			else
			{
				String.format(str_buf, "%s %s = %s.%s [%s];", type_name, var_name, graph_name, array_name, alias_name);
				Body.pushln(str_buf);
			}
		}
	}
	public void generate_foreach_header(ast_foreach fe, gm_code_writer Body)
	{
		ast_id source = fe.get_source();
		ast_id iter = fe.get_iterator();
		GMTYPE_T type = fe.get_iter_type();
    
		if (GlobalMembersGm_defs.gm_is_iteration_on_all_graph(type))
		{
	//C++ TO JAVA CONVERTER TODO TASK: Java does not have an equivalent for pointers to value types:
	//ORIGINAL LINE: sbyte* graph_name = source->get_genname();
			byte graph_name = source.get_genname();
	//C++ TO JAVA CONVERTER TODO TASK: Java does not have an equivalent for pointers to value types:
	//ORIGINAL LINE: sbyte* it_name = iter->get_genname();
			byte it_name = iter.get_genname();
			String.format(str_buf, "for (%s %s = 0; %s < %s.%s(); %s ++) ", get_type_string(type), it_name, it_name, graph_name, GlobalMembersGm_defs.gm_is_iteration_on_nodes(type) ? GlobalMembersGm_cpplib_words.NUM_NODES : GlobalMembersGm_cpplib_words.NUM_EDGES, it_name);
    
			Body.pushln(str_buf);
		}
		else if (GlobalMembersGm_defs.gm_is_common_nbr_iter_type(type))
		{
    
			String iter_name = fe.find_info_string(GlobalMembersGm_backend_cpp.CPPBE_INFO_COMMON_NBR_ITERATOR);
	//C++ TO JAVA CONVERTER TODO TASK: Java does not have an equivalent for pointers to value types:
	//ORIGINAL LINE: sbyte* graph_name = source->get_genname();
			byte graph_name = source.get_genname();
	//C++ TO JAVA CONVERTER TODO TASK: Java does not have an equivalent for pointers to value types:
	//ORIGINAL LINE: sbyte* it_name = iter->get_genname();
			byte it_name = iter.get_genname();
			String.format(str_buf, "for (%s %s = %s.get_next(); %s != gm_graph::NIL_NODE ; %s = %s.get_next()) ", GlobalMembersGm_cpplib_words.NODE_T, it_name, iter_name, it_name, it_name, iter_name);
    
			Body.pushln(str_buf);
    
			// NBRS, UP_NBRS, DOWN_NBRS, ...
		}
		else if (GlobalMembersGm_defs.gm_is_iteration_on_neighbors_compatible(type))
		{
    
			assert GlobalMembersGm_defs.gm_is_iteration_on_nodes(type);
    
			//-----------------------------------------------
			// create additional information
			//-----------------------------------------------
			String a_name = GlobalMembersGm_main.FE.voca_temp_name_and_add(iter.get_orgname(), "_idx");
			fe.add_info_string(GlobalMembersGm_backend_cpp.CPPBE_INFO_NEIGHBOR_ITERATOR, a_name);
			ast_id iterator = fe.get_iterator();
			iterator.getSymInfo().add_info_string(GlobalMembersGm_backend_cpp.CPPBE_INFO_NEIGHBOR_ITERATOR, a_name);
			a_name = null;
    
			// [todo] check name-conflict
			String alias_name = fe.find_info_string(GlobalMembersGm_backend_cpp.CPPBE_INFO_NEIGHBOR_ITERATOR);
			String graph_name = source.getTypeInfo().get_target_graph_id().get_genname();
			String array_name = GlobalMembersGm_defs.gm_is_iteration_use_reverse(type) ? GlobalMembersGm_cpplib_words.R_BEGIN : GlobalMembersGm_cpplib_words.BEGIN;
			String src_name = source.get_genname();
    
			String.format(str_buf, "for (%s %s = %s.%s[%s];", GlobalMembersGm_cpplib_words.EDGE_T, alias_name, graph_name, array_name, src_name);
			Body.push(str_buf);
			String.format(str_buf, "%s < %s.%s[%s+1] ; %s ++) ", alias_name, graph_name, array_name, src_name, alias_name);
			Body.pushln(str_buf);
    
			// SET_TYPE
		}
		else if (GlobalMembersGm_defs.gm_is_iteration_on_collection(type))
		{
    
			assert!fe.is_parallel();
			assert GlobalMembersGm_defs.gm_is_node_collection_iter_type(type) || GlobalMembersGm_defs.gm_is_collection_of_collection_iter_type(type);
    
			String iter_name = fe.find_info_string(GlobalMembersGm_backend_cpp.CPPBE_INFO_COLLECTION_ITERATOR);
			String.format(str_buf, "while (%s.has_next())", iter_name);
			Body.pushln(str_buf);
    
		}
		else
		{
			assert false;
		}
    
		return;
	}

	public void generate_expr_builtin_field(ast_expr_builtin_field builtinExpr, gm_code_writer body)
	{
    
		ast_field driver = builtinExpr.get_field_driver();
		gm_builtin_def definition = builtinExpr.get_builtin_def();
		ast_sent sent = GlobalMembersGm_transform_helper.gm_find_parent_sentence(builtinExpr);
    
		assert definition != null;
		assert sent != null;
    
		GMTYPE_T sourceType = definition.get_source_type_summary();
		gm_method_id_t methodId = definition.get_method_id();
    
		boolean parallelExecution = sent.is_under_parallel_execution();
		boolean addThreadId = false;
    
		String functionName;
		switch (sourceType)
		{
			case GMTYPE_NSET:
				functionName = get_function_name_nset(methodId, parallelExecution);
				break;
			case GMTYPE_NSEQ:
				functionName = get_function_name_nseq(methodId);
				break;
			case GMTYPE_NORDER:
				functionName = get_function_name_norder(methodId);
				break;
			default:
				assert false;
				break;
		}
    
		String.format(str_buf, "%s[%s].%s(", driver.get_second().get_genname(), driver.get_first().get_genname(), functionName);
		body.push(str_buf);
		add_arguments_and_thread(body, builtinExpr, addThreadId);
    
	}
	public String get_function_name_graph(gm_method_id_t methodId)
	{
		switch (methodId)
		{
			case GM_BLTIN_GRAPH_NUM_NODES:
				return GlobalMembersGm_cpplib_words.NUM_NODES;
			case GM_BLTIN_GRAPH_NUM_EDGES:
				return GlobalMembersGm_cpplib_words.NUM_EDGES;
			case GM_BLTIN_GRAPH_RAND_NODE:
				return GlobalMembersGm_cpplib_words.RANDOM_NODE;
			default:
				assert false;
				return "ERROR";
		}
	}
	public String get_function_name_nset(gm_method_id_t methodId, boolean in_parallel)
	{
		switch (methodId)
		{
			case GM_BLTIN_SET_HAS:
				return "is_in";
			case GM_BLTIN_SET_REMOVE:
				return in_parallel ? "remove_par" : "remove_seq";
			case GM_BLTIN_SET_ADD:
				return in_parallel ? "add_par" : "add_seq";
			case GM_BLTIN_SET_UNION:
				return "union_";
			case GM_BLTIN_SET_COMPLEMENT:
				return "complement";
			case GM_BLTIN_SET_INTERSECT:
				return "intersect";
			case GM_BLTIN_SET_SUBSET:
				return "is_subset";
			case GM_BLTIN_SET_SIZE:
				return "get_size";
			default:
				assert false;
				return "ERROR";
		}
	}
	public String get_function_name_nseq(gm_method_id_t methodId)
	{
		switch (methodId)
		{
			case GM_BLTIN_SET_ADD:
				return "push_front";
			case GM_BLTIN_SET_ADD_BACK:
				return "push_back";
			case GM_BLTIN_SET_REMOVE:
				return "pop_front";
			case GM_BLTIN_SET_REMOVE_BACK:
				return "pop_back";
			case GM_BLTIN_SET_SIZE:
				return "get_size";
			default:
				assert false;
				return "ERROR";
		}
	}
	public String get_function_name_norder(gm_method_id_t methodId)
	{
		switch (methodId)
		{
			case GM_BLTIN_SET_ADD:
				return "push_front";
			case GM_BLTIN_SET_ADD_BACK:
				return "push_back";
			case GM_BLTIN_SET_REMOVE:
				return "pop_front";
			case GM_BLTIN_SET_REMOVE_BACK:
				return "pop_back";
			case GM_BLTIN_SET_HAS:
				return "is_in";
			case GM_BLTIN_SET_SIZE:
				return "get_size";
			default:
				assert false;
				return "ERROR";
		}
	}
	public void add_arguments_and_thread(gm_code_writer body, ast_expr_builtin builtinExpr, boolean addThreadId)
	{
		main.generate_expr_list(builtinExpr.get_args());
		if (addThreadId)
			body.push(",gm_rt_thread_id()");
		body.push(")");
	}

	private String str_buf = new String(new char[1024 * 8]);
	private gm_cpp_gen main;
}