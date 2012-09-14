package backend_cpp;

import static backend_cpp.gm_cpplib_words.BEGIN;
import static backend_cpp.gm_cpplib_words.EDGEITER_T;
import static backend_cpp.gm_cpplib_words.EDGE_IDX;
import static backend_cpp.gm_cpplib_words.EDGE_T;
import static backend_cpp.gm_cpplib_words.FROM_IDX;
import static backend_cpp.gm_cpplib_words.GRAPH_T;
import static backend_cpp.gm_cpplib_words.MAP_T;
import static backend_cpp.gm_cpplib_words.NODEITER_T;
import static backend_cpp.gm_cpplib_words.NODE_IDX;
import static backend_cpp.gm_cpplib_words.NODE_T;
import static backend_cpp.gm_cpplib_words.NUM_EDGES;
import static backend_cpp.gm_cpplib_words.NUM_NODES;
import static backend_cpp.gm_cpplib_words.ORDER_T;
import static backend_cpp.gm_cpplib_words.QUEUE_T;
import static backend_cpp.gm_cpplib_words.RANDOM_NODE;
import static backend_cpp.gm_cpplib_words.R_BEGIN;
import static backend_cpp.gm_cpplib_words.R_EDGE_IDX;
import static backend_cpp.gm_cpplib_words.R_NODE_IDX;
import static backend_cpp.gm_cpplib_words.SEQ_T;
import static backend_cpp.gm_cpplib_words.SET_T;
import static inc.GMTYPE_T.GMTYPE_BOOL;
import static inc.GMTYPE_T.GMTYPE_INT;
import inc.GMTYPE_T;
import inc.gm_code_writer;
import inc.gm_graph_library;
import inc.nop_enum_cpp;
import ast.ast_expr;
import ast.ast_expr_builtin;
import ast.ast_expr_builtin_field;
import ast.ast_field;
import ast.ast_foreach;
import ast.ast_id;
import ast.ast_maptypedecl;
import ast.ast_nop;
import ast.ast_sent;
import ast.ast_typedecl;

import common.gm_builtin_def;
import common.gm_main;
import common.gm_method_id_t;
import common.gm_transform_helper;
import common.gm_vocabulary;

//-----------------------------------------------------------------
// interface for graph library Layer
//  ==> will be deprecated
//-----------------------------------------------------------------
class gm_cpplib extends gm_graph_library {

	// private static final int SMALL = 1;
	private static final int MEDIUM = 2;
	// private static final int LARGE = 3;

	private String str_buf;
	private gm_cpp_gen main;

	gm_cpplib(gm_cpp_gen gen) {
		main = gen;
	}

	final void set_main(gm_cpp_gen gen) {
		main = gen;
	}

	String get_header_info() {
		return "gm.h";
	}

	String get_type_string(GMTYPE_T type) {
		if (type.is_graph_type()) {
			return GRAPH_T;
		} else if (type.is_nodeedge_type()) {
			if (type.is_node_type())
				return NODE_T;
			else
				return EDGE_T;
		} else if (type.is_iter_type()) {
			if (type.is_node_iter_type()) {
				return NODEITER_T;
			} else if (type.is_edge_iter_type()) {
				return EDGEITER_T;
			} else if (type.is_node_compatible_type())
				return NODE_T;
			else if (type.is_edge_compatible_type())
				return EDGE_T;
			else {
				assert false;
				return "ERROR";
			}
		} else if (type.is_collection_type()) {
			assert type.is_node_collection_type() || type.is_collection_of_collection_type();
			if (type.is_set_collection_type()) {
				return SET_T;
			} else if (type.is_order_collection_type()) {
				return ORDER_T;
			} else if (type.is_sequence_collection_type()) {
				return SEQ_T;
			} else if (type.is_collection_of_collection_type()) {
				return QUEUE_T;
			} else if (type.is_map_type()) {
				return MAP_T;
			} else {
				assert false;
				return "ERROR";
			}
		} else if (type.is_collection_of_collection_type()) {
			return QUEUE_T;
		} else {
			System.out.printf("type = %d %s\n", type, type.get_type_string());
			assert false;
			return "ERROR";
		}
	}

	private String getMapTypeString(int type) {
		if (type == MEDIUM) {
			return "gm_map_medium";
		} else {
			assert (false);
			return null;
		}
	}

	private String getMapDefaultValueForType(GMTYPE_T type) {
		if (type.is_float_type()) {
			return "0.0";
		} else if (type.is_integer_type()) {
			return "0";
		} else if (type.is_boolean_type()) {
			return "false";
		} else if (type.is_node_type()) {
			return "gm_graph::NIL_NODE";
		} else if (type.is_edge_type()) {
			return "gm_graph::NIL_EDGE";
		} else {
			// we only support primitives, nodes and edges in maps (yet)
			assert (false);
		}
		return null;
	}

	private String getAdditionalMapParameters(int mapType) {
		switch (mapType) {
		case MEDIUM:
			return "gm_rt_get_num_threads(), ";
		default:
			assert (false);
			return "ERROR";
		}
	}

	void add_map_def(ast_maptypedecl map, ast_id mapId) {

		int mapType = MEDIUM; // TODO: implement compiler optimization to figure
								// out what is best here
		GMTYPE_T keyType = map.getKeyTypeSummary();
		GMTYPE_T valueType = map.getValueTypeSummary();
		if (valueType == GMTYPE_BOOL) {
			valueType = GMTYPE_INT;
		}

		// Output: MapType<KeyType, ValueType> VariableName(AdditionalParameters
		// DefaultValue);
		String typeBuffer = String.format("%s<%s, %s>", getMapTypeString(mapType), get_type_string(keyType), get_type_string(valueType));

		String parameterBuffer = String.format("(%s %s)", getAdditionalMapParameters(mapType), getMapDefaultValueForType(valueType));

		String buffer = String.format("%s %s%s;", typeBuffer, mapId.get_genname(), parameterBuffer);
		Body.pushln(buffer);
	}

	private String get_function_name_map(gm_method_id_t methodId) {
		return get_function_name_map(methodId, false);
	}

	private String get_function_name_map(gm_method_id_t methodId, boolean in_parallel) {

		switch (methodId) {
		case GM_BLTIN_MAP_SIZE:
			return "size";
		case GM_BLTIN_MAP_CLEAR:
			return "clear";
		case GM_BLTIN_MAP_HAS_MAX_VALUE:
		case GM_BLTIN_MAP_HAS_MIN_VALUE:
		case GM_BLTIN_MAP_HAS_KEY:
		case GM_BLTIN_MAP_GET_MAX_KEY:
		case GM_BLTIN_MAP_GET_MIN_KEY:
		case GM_BLTIN_MAP_GET_MAX_VALUE:
		case GM_BLTIN_MAP_GET_MIN_VALUE: {
			if (in_parallel)
				// if it is in parallel we do not have to use the inherent
				// parallelism of the map so this is not a bug!!!
				return get_function_name_map_seq(methodId);
			else
				return get_function_name_map_par(methodId);
		}
		default:
			assert (false);
			return "ERROR";
		}
	}

	private String get_function_name_map_seq(gm_method_id_t methodId) {
		switch (methodId) {
		case GM_BLTIN_MAP_HAS_MAX_VALUE:
			return "hasMaxValue";
		case GM_BLTIN_MAP_HAS_MIN_VALUE:
			return "hasMinValue";
		case GM_BLTIN_MAP_HAS_KEY:
			return "hasKey";
		case GM_BLTIN_MAP_GET_MAX_KEY:
			return "getMaxKey";
		case GM_BLTIN_MAP_GET_MIN_KEY:
			return "getMinKey";
		case GM_BLTIN_MAP_GET_MAX_VALUE:
			return "getMaxValue";
		case GM_BLTIN_MAP_GET_MIN_VALUE:
			return "getMinValue";
		default:
			assert (false);
			return null;
		}
	}

	private String get_function_name_map_par(gm_method_id_t methodId) {
		switch (methodId) {
		case GM_BLTIN_MAP_HAS_MAX_VALUE:
			return "hasMaxValue_par";
		case GM_BLTIN_MAP_HAS_MIN_VALUE:
			return "hasMinValue_par";
		case GM_BLTIN_MAP_HAS_KEY:
			return "hasKey_par";
		case GM_BLTIN_MAP_GET_MAX_KEY:
			return "getMaxKey_par";
		case GM_BLTIN_MAP_GET_MIN_KEY:
			return "getMinKey_par";
		case GM_BLTIN_MAP_GET_MAX_VALUE:
			return "getMaxValue_par";
		case GM_BLTIN_MAP_GET_MIN_VALUE:
			return "getMinValue_par";
		default:
			assert (false);
			return null;
		}
	}

	String get_type_string(ast_typedecl t) {
		return get_type_string(t.getTypeSummary());
	}

	String max_node_index(ast_id graph) {
		str_buf = String.format("%s.%s()", graph.get_genname(), NUM_NODES);
		return str_buf;
	}

	String max_edge_index(ast_id graph) {
		str_buf = String.format("%s.%s()", graph.get_genname(), NUM_EDGES);
		return str_buf;
	}

	String node_index(ast_id iter) {
		// should check iterator type????
		return iter.get_genname();
	}

	String edge_index(ast_id iter) {
		// should check iterator type????
		return iter.get_genname();
	}

	@Override
	public boolean do_local_optimize() {
		String[] NAMES = { "[(nothing)]" };
		final int COUNT = NAMES.length;

		boolean is_okay = true;

		for (int i = 0; i < COUNT; i++) {
			gm_main.gm_begin_minor_compiler_stage(i + 1, NAMES[i]);
			{
				if (i != 0) {
					assert false;
					break;
				}
			}
			gm_main.gm_end_minor_compiler_stage();
			if (!is_okay)
				break;
		}
		return is_okay;
	}

	void generate_sent_nop(ast_nop f) {
		nop_enum_cpp subtype = f.get_subtype();
		switch (subtype) {
		default:
			assert false;
			break;
		} // FIXME wtf? o.O
	}

	void generate_expr_builtin(ast_expr_builtin e, gm_code_writer Body) {

		if (e.driver_is_field()) {
			generate_expr_builtin_field((ast_expr_builtin_field) e, Body);
			return;
		}

		ast_id i = e.get_driver(); // driver
		gm_builtin_def def = e.get_builtin_def();
		ast_sent s = gm_transform_helper.gm_find_parent_sentence(e);
		assert def != null;
		assert s != null;
		GMTYPE_T src_type = def.get_source_type_summary();
		gm_method_id_t method_id = def.get_method_id();
		boolean under_parallel = s.is_under_parallel_execution();
		boolean add_thread_id = false;

		String func_name;
		switch (src_type) {
		case GMTYPE_GRAPH:
			func_name = get_function_name_graph(method_id);
			break;
		case GMTYPE_NODE:
			switch (method_id) {
			case GM_BLTIN_NODE_DEGREE:
				assert i.getTypeInfo().get_target_graph_id() != null;
				str_buf = String.format("(%s.%s[%s+1] - %s.%s[%s])", i.getTypeInfo().get_target_graph_id().get_genname(), BEGIN, i.get_genname(), i
						.getTypeInfo().get_target_graph_id().get_genname(), BEGIN, i.get_genname());
				Body.push(str_buf);
				break;
			case GM_BLTIN_NODE_IN_DEGREE:
				assert i.getTypeInfo().get_target_graph_id() != null;
				str_buf = String.format("(%s.%s[%s+1] - %s.%s[%s])", i.getTypeInfo().get_target_graph_id().get_genname(), R_BEGIN, i.get_genname(), i
						.getTypeInfo().get_target_graph_id().get_genname(), R_BEGIN, i.get_genname());
				Body.push(str_buf);
				break;
			case GM_BLTIN_NODE_IS_NBR:
				assert i.getTypeInfo().get_target_graph_id() != null;
				str_buf = String.format("%s.is_neighbor(", i.getTypeInfo().get_target_graph_id().get_genname());
				Body.push(str_buf);
				main.generate_expr(e.get_args().getFirst());
				str_buf = String.format(",%s)", i.get_genname());
				Body.push(str_buf);
				break;
			case GM_BLTIN_NODE_HAS_EDGE_TO:
				assert i.getTypeInfo().get_target_graph_id() != null;
				str_buf = String.format("%s.has_edge_to(", i.getTypeInfo().get_target_graph_id().get_genname());
				Body.push(str_buf);
				main.generate_expr(e.get_args().getFirst());
				str_buf = String.format(",%s)", i.get_genname());
				Body.push(str_buf);
				break;
			case GM_BLTIN_NODE_RAND_NBR:
				assert i.getTypeInfo().get_target_graph_id() != null;
				str_buf = String.format("%s.pick_random_out_neighbor(%s)", i.getTypeInfo().get_target_graph_id().get_genname(), i.get_genname());
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
			switch (method_id) {
			case GM_BLTIN_NODE_TO_EDGE: {
				String alias_name = i.getSymInfo().find_info_string(gm_cpp_gen.CPPBE_INFO_NEIGHBOR_ITERATOR);
				assert alias_name != null;
				assert alias_name.length() > 0;
				str_buf = String.format("%s", alias_name);
			}
				break;
			default:
				assert false;
				break;
			}
			Body.push(str_buf);
			return;
		case GMTYPE_EDGE:
			switch (method_id) {
			case GM_BLTIN_EDGE_FROM:
				str_buf = String.format("%s.%s[%s]", i.getTypeInfo().get_target_graph_id().get_genname(), FROM_IDX, i.get_genname());
				break;
			case GM_BLTIN_EDGE_TO:
				str_buf = String.format("%s.%s[%s]", i.getTypeInfo().get_target_graph_id().get_genname(), NODE_IDX, i.get_genname());
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
			throw new AssertionError();
		}

		str_buf = String.format("%s.%s(", i.get_genname(), func_name);
		Body.push(str_buf);
		add_arguments_and_thread(Body, e, add_thread_id);
	}

	void generate_expr_nil(ast_expr e, gm_code_writer Body) {
		if (e.get_type_summary() == GMTYPE_T.GMTYPE_NIL_EDGE) {
			Body.push("gm_graph::NIL_EDGE");
		} else if (e.get_type_summary() == GMTYPE_T.GMTYPE_NIL_NODE) {
			Body.push("gm_graph::NIL_NODE");
		} else {
			assert false;
		}
	}

	boolean add_collection_def(ast_id i) {
		Body.push("(");

		ast_typedecl t = i.getTypeInfo();
		if (t.is_set_collection() || t.is_order_collection() || t.is_collection_of_collection()) {
			// total size;
			assert t.get_target_graph_id() != null;

			if (!t.is_collection_of_collection())
				Body.push(t.get_target_graph_id().get_genname());
			if (t.is_node_collection()) {
				Body.push(".");
				Body.push(NUM_NODES);
				Body.push("()");
			} else if (t.is_edge_collection()) {
				Body.push(".");
				Body.push(NUM_EDGES);
				Body.push("()");
			} else if (t.is_collection_of_collection()) {
				assert true;
			} else {
				assert false;
			}
		}
		if (t.is_order_collection())
			Body.push(", ");

		if (t.is_order_collection() || t.is_sequence_collection() || t.is_collection_of_collection()) {
			Body.push(gm_cpp_gen.MAX_THREADS);
			Body.push("()");
		}

		Body.pushln(");");

		return false;
	}

	void build_up_language_voca(gm_vocabulary V) {
		V.add_word(NODE_T);
		V.add_word(EDGE_T);
		V.add_word(GRAPH_T);
		V.add_word(SET_T);
		V.add_word(ORDER_T);
		V.add_word(NODE_IDX);
		V.add_word(EDGE_IDX);
		V.add_word(R_NODE_IDX);
		V.add_word(R_EDGE_IDX);
		V.add_word(BEGIN);
		V.add_word(R_BEGIN);
	}

	boolean need_up_initializer(ast_foreach f) {
		GMTYPE_T iter_type = f.get_iter_type();
		if (iter_type.is_iteration_on_collection())
			return true;
		else if (iter_type.is_common_nbr_iter_type())
			return true;
		return false;
	}

	boolean need_down_initializer(ast_foreach f) {
		GMTYPE_T iter_type = f.get_iter_type();

		if (iter_type.is_iteration_on_collection()) {
			return true;
		} else if (iter_type.is_common_nbr_iter_type()) {
			return false;
		}
		// in/out/up/down
		else if (iter_type.is_iteration_on_neighbors_compatible()) {
			return true;
		}

		return false;
	}

	void generate_up_initializer(ast_foreach f, gm_code_writer Body) {
		GMTYPE_T iter_type = f.get_iter_type();
		ast_id source = f.get_source();
		if (iter_type.is_iteration_on_collection()) {
			assert !f.is_parallel();
			// for temp
			String iter_type_str = f.is_parallel() ? "par_iter" : f.is_reverse_iteration() ? "rev_iter" : "seq_iter";

			String prep_str = f.is_parallel() ? "prepare_par_iteration" : f.is_reverse_iteration() ? "prepare_rev_iteration" : "prepare_seq_iteration";

			// get a list
			if (source.getTypeSummary().is_collection_of_collection_type())
				str_buf = String.format("%s<%s>::%s", get_type_string(source.getTypeInfo()), get_type_string(source.getTargetTypeInfo()), iter_type_str);
			else
				str_buf = String.format("%s::%s", get_type_string(source.getTypeInfo()), iter_type_str);
			Body.push(str_buf);

			String a_name = gm_main.FE.voca_temp_name_and_add(f.get_iterator().get_orgname(), "_I");
			f.add_info_string(gm_cpp_gen.CPPBE_INFO_COLLECTION_ITERATOR, a_name);
			str_buf = String.format(" %s", a_name);
			Body.push(str_buf);
			a_name = null;

			str_buf = String.format(" = %s.%s();", source.get_genname(), prep_str);
			Body.pushln(str_buf);
		} else if (iter_type.is_common_nbr_iter_type()) {
			ast_id graph = source.getTypeInfo().get_target_graph_id();
			ast_id source2 = f.get_source2();
			assert source2 != null;
			String a_name = gm_main.FE.voca_temp_name_and_add(f.get_iterator().get_orgname(), "_I");
			f.add_info_string(gm_cpp_gen.CPPBE_INFO_COMMON_NBR_ITERATOR, a_name);
			Body.pushln("// Iterate over Common neighbors");
			str_buf = String.format("gm_common_neighbor_iter %s(%s, %s, %s);", a_name, graph.get_genname(), source.get_genname(), source2.get_genname());
			Body.pushln(str_buf);
		}
	}

	void generate_down_initializer(ast_foreach f, gm_code_writer Body) {
		GMTYPE_T iter_type = f.get_iter_type();
		ast_id iter = f.get_iterator();
		ast_id source = f.get_source();

		if (iter_type.is_iteration_on_collection()) {
			assert f.find_info(gm_cpp_gen.CPPBE_INFO_COLLECTION_ITERATOR) != null;
			String lst_iter_name = f.find_info_string(gm_cpp_gen.CPPBE_INFO_COLLECTION_ITERATOR);
			String type_name;
			if (source.getTypeSummary().is_collection_of_collection_type())
				type_name = get_type_string(source.getTargetTypeInfo());
			else
				type_name = source.getTypeInfo().is_node_collection() ? NODE_T : EDGE_T;

			if (iter_type.is_collection_of_collection_iter_type()) {
				str_buf = String.format("%s& %s = %s.get_next();", type_name, f.get_iterator().get_genname(), lst_iter_name);
			} else {
				str_buf = String.format("%s %s = %s.get_next();", type_name, f.get_iterator().get_genname(), lst_iter_name);
			}
			Body.pushln(str_buf);
		} else if (iter_type.is_iteration_on_neighbors_compatible()) {
			String alias_name = f.find_info_string(gm_cpp_gen.CPPBE_INFO_NEIGHBOR_ITERATOR);
			String type_name = get_type_string(iter_type);
			String graph_name = source.getTypeInfo().get_target_graph_id().get_genname();
			String var_name = iter.get_genname();
			String array_name;

			// should be neighbor iterator
			assert iter_type.is_iteration_on_nodes();

			// [XXX] should be changed if G is transposed!
			array_name = iter_type.is_iteration_use_reverse() ? R_NODE_IDX : NODE_IDX;

			if (iter_type.is_iteration_on_down_neighbors()) {
				str_buf = String.format("if (!is_down_edge(%s)) continue;", alias_name);
				Body.pushln(str_buf);

				str_buf = String.format("%s %s = %s.%s [%s];", type_name, var_name, graph_name, array_name, alias_name);
				Body.pushln(str_buf);
			} else if (iter_type.is_iteration_on_updown_levels()) {
				str_buf = String.format("%s %s = %s.%s [%s];", type_name, var_name, graph_name, array_name, alias_name);
				Body.pushln(str_buf);

				str_buf = String.format("if (get_level(%s) != (get_curr_level() %c 1)) continue;", iter.get_genname(),
						iter_type.is_iteration_on_up_neighbors() ? '-' : '+');
				Body.pushln(str_buf);
			} else {
				str_buf = String.format("%s %s = %s.%s [%s];", type_name, var_name, graph_name, array_name, alias_name);
				Body.pushln(str_buf);
			}
		}
	}

	void generate_foreach_header(ast_foreach fe, gm_code_writer Body) {
		ast_id source = fe.get_source();
		ast_id iter = fe.get_iterator();
		GMTYPE_T type = fe.get_iter_type();

		if (type.is_iteration_on_all_graph()) {
			String graph_name;
			if (source.getTypeSummary().is_node_property_type() || source.getTypeSummary().is_edge_property_type()) {
				graph_name = source.getTypeInfo().get_target_graph_id().get_orgname();
			} else {
				graph_name = source.get_genname();
			}
			String it_name = iter.get_genname();
			str_buf = String.format("for (%s %s = 0; %s < %s.%s(); %s ++) ", get_type_string(type), it_name, it_name, graph_name,
					type.is_iteration_on_nodes() ? NUM_NODES : NUM_EDGES, it_name);

			Body.pushln(str_buf);
		} else if (type.is_common_nbr_iter_type()) {

			String iter_name = fe.find_info_string(gm_cpp_gen.CPPBE_INFO_COMMON_NBR_ITERATOR);
			source.get_genname();
			String it_name = iter.get_genname();
			str_buf = String.format("for (%s %s = %s.get_next(); %s != gm_graph::NIL_NODE ; %s = %s.get_next()) ", NODE_T, it_name, iter_name, it_name,
					it_name, iter_name);

			Body.pushln(str_buf);

			// NBRS, UP_NBRS, DOWN_NBRS, ...
		} else if (type.is_iteration_on_neighbors_compatible()) {

			assert type.is_iteration_on_nodes();

			// -----------------------------------------------
			// create additional information
			// -----------------------------------------------
			String a_name = gm_main.FE.voca_temp_name_and_add(iter.get_orgname(), "_idx");
			fe.add_info_string(gm_cpp_gen.CPPBE_INFO_NEIGHBOR_ITERATOR, a_name);
			ast_id iterator = fe.get_iterator();
			iterator.getSymInfo().add_info_string(gm_cpp_gen.CPPBE_INFO_NEIGHBOR_ITERATOR, a_name);
			a_name = null;

			// [todo] check name-conflict
			String alias_name = fe.find_info_string(gm_cpp_gen.CPPBE_INFO_NEIGHBOR_ITERATOR);
			String graph_name = source.getTypeInfo().get_target_graph_id().get_genname();
			String array_name = type.is_iteration_use_reverse() ? R_BEGIN : BEGIN;
			String src_name = source.get_genname();

			str_buf = String.format("for (%s %s = %s.%s[%s];", EDGE_T, alias_name, graph_name, array_name, src_name);
			Body.push(str_buf);
			str_buf = String.format("%s < %s.%s[%s+1] ; %s ++) ", alias_name, graph_name, array_name, src_name, alias_name);
			Body.pushln(str_buf);

			// SET_TYPE
		} else if (type.is_iteration_on_collection()) {

			assert !fe.is_parallel();
			assert type.is_node_collection_iter_type() || type.is_collection_of_collection_iter_type();

			String iter_name = fe.find_info_string(gm_cpp_gen.CPPBE_INFO_COLLECTION_ITERATOR);
			str_buf = String.format("while (%s.has_next())", iter_name);
			Body.pushln(str_buf);

		} else {
			assert false;
		}

		return;
	}

	void generate_expr_builtin_field(ast_expr_builtin_field builtinExpr, gm_code_writer body) {

		ast_field driver = builtinExpr.get_field_driver();
		gm_builtin_def definition = builtinExpr.get_builtin_def();
		ast_sent sent = gm_transform_helper.gm_find_parent_sentence(builtinExpr);

		assert definition != null;
		assert sent != null;

		GMTYPE_T sourceType = definition.get_source_type_summary();
		gm_method_id_t methodId = definition.get_method_id();

		boolean parallelExecution = sent.is_under_parallel_execution();
		boolean addThreadId = false;

		String functionName = null;
		switch (sourceType) {
		case GMTYPE_NSET:
			functionName = get_function_name_nset(methodId, parallelExecution);
			break;
		case GMTYPE_NSEQ:
			functionName = get_function_name_nseq(methodId);
			break;
		case GMTYPE_NORDER:
			functionName = get_function_name_norder(methodId);
			break;
		case GMTYPE_MAP:
			functionName = get_function_name_map(methodId);
			break;
		default:
			assert false;
			break;
		}

		str_buf = String.format("%s[%s].%s(", driver.get_second().get_genname(), driver.get_first().get_genname(), functionName);
		body.push(str_buf);
		add_arguments_and_thread(body, builtinExpr, addThreadId);

	}

	String get_function_name_graph(gm_method_id_t methodId) {
		switch (methodId) {
		case GM_BLTIN_GRAPH_NUM_NODES:
			return NUM_NODES;
		case GM_BLTIN_GRAPH_NUM_EDGES:
			return NUM_EDGES;
		case GM_BLTIN_GRAPH_RAND_NODE:
			return RANDOM_NODE;
		default:
			assert false;
			return "ERROR";
		}
	}

	String get_function_name_nset(gm_method_id_t methodId, boolean in_parallel) {
		switch (methodId) {
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

	String get_function_name_nseq(gm_method_id_t methodId) {
		switch (methodId) {
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

	String get_function_name_norder(gm_method_id_t methodId) {
		switch (methodId) {
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

	void add_arguments_and_thread(gm_code_writer body, ast_expr_builtin builtinExpr, boolean addThreadId) {
		main.generate_expr_list(builtinExpr.get_args());
		if (addThreadId)
			body.push(",gm_rt_thread_id()");
		body.push(")");
	}

	String get_primitive_type_string(GMTYPE_T type_id) {
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
			assert (false);
			return "??";
		}
	}

	String getTypeString(GMTYPE_T type) {
		if (type.is_prim_type()) {
			return get_primitive_type_string(type);
		} else if (type.is_node_type()) {
			return NODE_T;
		} else if (type.is_edge_type()) {
			return EDGE_T;
		} else {
			assert (false);
		}
		return null;
	}
}