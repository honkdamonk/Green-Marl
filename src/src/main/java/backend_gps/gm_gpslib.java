package backend_gps;
import static backend_gps.gm_gps_comm_t.GPS_COMM_INIT;
import static backend_gps.gm_gps_comm_t.GPS_COMM_NESTED;
import static backend_gps.gm_gps_comm_t.GPS_COMM_RANDOM_WRITE;
import static inc.GlobalMembersGm_backend_gps.GPS_DUMMY_ID;
import static inc.GlobalMembersGm_backend_gps.GPS_FLAG_COMM_DEF_ASSIGN;
import static inc.GlobalMembersGm_backend_gps.GPS_FLAG_EDGE_DEFINING_INNER;
import static inc.GlobalMembersGm_backend_gps.GPS_FLAG_NODE_VALUE_INIT;
import static inc.GlobalMembersGm_backend_gps.GPS_FLAG_SENT_BLOCK_FOR_RANDOM_WRITE_ASSIGN;
import static inc.GlobalMembersGm_backend_gps.GPS_KEY_FOR_STATE;
import static inc.GlobalMembersGm_backend_gps.GPS_LIST_EDGE_PROP_WRITE;
import static inc.GlobalMembersGm_backend_gps.GPS_MAP_EDGE_PROP_ACCESS;
import static inc.GlobalMembersGm_backend_gps.GPS_REV_NODE_ID;
import static inc.GlobalMembersGm_backend_gps.STATE_SHORT_CUT;
import frontend.GlobalMembersGm_frontend;
import frontend.gm_symtab_entry;
import inc.GMTYPE_T;
import inc.GM_PROP_USAGE_T;
import inc.GM_REDUCE_T;
import inc.gm_code_writer;
import inc.gm_graph_library;

import java.util.LinkedList;

import tangible.RefObject;
import ast.AST_NODE_TYPE;
import ast.ast_assign;
import ast.ast_expr;
import ast.ast_expr_builtin;
import ast.ast_foreach;
import ast.ast_id;
import ast.ast_procdef;
import ast.ast_sent;
import ast.ast_sentblock;
import ast.ast_typedecl;

import common.GlobalMembersGm_main;
import common.GlobalMembersGm_misc;
import common.gm_builtin_def;

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
//-----------------------------------------------------------------
//C++ TO JAVA CONVERTER NOTE: Java has no need of forward class declarations:
//class gm_gps_beinfo;
//C++ TO JAVA CONVERTER NOTE: Java has no need of forward class declarations:
//class gm_gps_communication_size_info;
//C++ TO JAVA CONVERTER NOTE: Java has no need of forward class declarations:
//class gm_gps_gen;

// Nothing happens in this class
public class gm_gpslib extends gm_graph_library {
	public gm_gpslib() {
		main = null;
	}

	public gm_gpslib(gm_gps_gen gen) {
		set_main(gen);
	}

	public final void set_main(gm_gps_gen gen) {
		main = gen;
	}

	public gm_gps_gen get_main() {
		return main;
	}

	public String get_header_info() {
		return "";
	}

	// virtual boolean do_local_optimize();

	// note: consume the return string immedately
	public String create_key_string(ast_id id) {
		str_buf = String.format("KEY_%s", id.get_genname());
		return str_buf;
	}

	// virtual void generate_prepare_bb(gm_code_writer Body, gm_gps_basic_block
	// b);

	public void generate_broadcast_reduce_initialize_master(ast_id id, gm_code_writer Body, GM_REDUCE_T reduce_type, String base_value) {
		Body.push("getGlobalObjectsMap().putOrUpdateGlobalObject(");
		Body.push(create_key_string(id));
		Body.push(",");
		Body.push("new ");
		generate_broadcast_variable_type(id.getTypeSummary(), Body, reduce_type); // create
																					// BV
																					// by
																					// type
		Body.push("(");
		Body.push(base_value);
		Body.push(")");
		Body.pushln(");");

	}

	public void generate_broadcast_prepare(gm_code_writer Body) {
		Body.pushln("getGlobalObjectsMap().clearNonDefaultObjects();");
	}

	public void generate_broadcast_state_master(String state_var, gm_code_writer Body) {
		Body.push("getGlobalObjectsMap().putOrUpdateGlobalObject(");
		Body.push(GPS_KEY_FOR_STATE);
		Body.push(",");
		Body.push("new IntOverwriteGlobalObject(");
		Body.push(state_var);
		Body.pushln("));");
	}

	public void generate_broadcast_isFirst_master(String is_first_var, gm_code_writer Body) {
		Body.push("getGlobalObjectsMap().putOrUpdateGlobalObject(\"");
		Body.push(is_first_var);
		Body.push("\",");
		Body.push("new BooleanOverwriteGlobalObject(");
		Body.push(is_first_var);
		Body.pushln("));");
	}

	public void generate_broadcast_variable_type(GMTYPE_T type_id, gm_code_writer Body) {
		generate_broadcast_variable_type(type_id, Body, GM_REDUCE_T.GMREDUCE_NULL);
	}

	public void generate_broadcast_variable_type(GMTYPE_T type_id, gm_code_writer Body, GM_REDUCE_T reduce_op)

	{
		// --------------------------------------
		// Generate following string
		// <Type><Reduce>BV( value )
		// --------------------------------------
		// Generate following string

		// ---------------------------------------------------
		// Type: Long, Int, Double, Float, Bool, NODE,EDGE
		// ---------------------------------------------------
		if (type_id.is_node_compatible_type())
			type_id = GMTYPE_T.GMTYPE_NODE;
		if (type_id.is_edge_compatible_type())
			type_id = GMTYPE_T.GMTYPE_EDGE;

		switch (type_id) {
		case GMTYPE_INT:
			Body.push("Int");
			break;
		case GMTYPE_DOUBLE:
			Body.push("Double");
			break;
		case GMTYPE_LONG:
			Body.push("Long");
			break;
		case GMTYPE_FLOAT:
			Body.push("Float");
			break;
		case GMTYPE_BOOL:
			Body.push("Boolean");
			break;
		case GMTYPE_NODE:
			if (is_node_type_int())
				Body.push("Int");
			else
				Body.push("Long");
			break;
		default:
			assert false;
			break;
		}

		// ---------------------------------------------------
		// Reduce Op: Min, Max, Plus, Mult, And, Or, Any
		// ---------------------------------------------------
		switch (reduce_op) {
		case GMREDUCE_NULL:
			Body.push("Overwrite");
			break;
		case GMREDUCE_PLUS:
			Body.push("Sum");
			break;
		case GMREDUCE_MULT:
			Body.push("Product");
			break;
		case GMREDUCE_MIN:
			Body.push("Min");
			break;
		case GMREDUCE_MAX:
			Body.push("Max");
			break;
		case GMREDUCE_AND:
			Body.push("AND");
			break;
		case GMREDUCE_OR:
			Body.push("OR");
			break;
		default:
			assert false;
			break;
		}
		Body.push("GlobalObject");
	}

	public void generate_broadcast_send_master(ast_id id, gm_code_writer Body) {
		// ---------------------------------------------------
		// create new BV
		// ---------------------------------------------------
		Body.push("getGlobalObjectsMap().putOrUpdateGlobalObject(");
		Body.push(create_key_string(id));
		Body.push(",");
		Body.push("new ");
		generate_broadcast_variable_type(id.getTypeSummary(), Body); // create
																		// BV by
																		// type

		// ---------------------------------------------------
		// Initial Value: Reading of Id
		// ---------------------------------------------------
		Body.push("(");
		get_main().generate_rhs_id(id);
		Body.push(")");
		Body.pushln(");");
	}

	public void generate_broadcast_receive_master(ast_id id, gm_code_writer Body, GM_REDUCE_T reduce_op_type) {
		// Read from BV to local value
		get_main().generate_lhs_id(id);
		Body.push(" = ");
		boolean need_paren = false;

		if (reduce_op_type != GM_REDUCE_T.GMREDUCE_NULL) {
			if (reduce_op_type == GM_REDUCE_T.GMREDUCE_MIN) {
				need_paren = true;
				Body.push("Math.min(");
				get_main().generate_rhs_id(id);
				Body.push(",");
			} else if (reduce_op_type == GM_REDUCE_T.GMREDUCE_MAX) {
				need_paren = true;
				Body.push("Math.max(");
				get_main().generate_rhs_id(id);
				Body.push(",");
			} else {
				get_main().generate_rhs_id(id);
				switch (reduce_op_type) {
				case GMREDUCE_PLUS:
					Body.push("+");
					break;
				case GMREDUCE_MULT:
					Body.push("*");
					break;
				case GMREDUCE_AND:
					Body.push("&&");
					break;
				case GMREDUCE_OR:
					Body.push("||");
					break;
				default:
					assert false;
					break;
				}
			}
		}

		Body.push("((");
		generate_broadcast_variable_type(id.getTypeSummary(), Body, reduce_op_type);
		Body.push(") ");
		Body.push("getGlobalObjectsMap().getGlobalObject(");
		Body.push(create_key_string(id));
		Body.push("))");
		Body.push(".getValue().getValue()");
		if (need_paren)
			Body.push(")");
		Body.pushln(";");
	}

	public void generate_headers(gm_code_writer Body) {
		Body.pushln("import gps.*;");
		Body.pushln("import gps.graph.*;");
		Body.pushln("import gps.node.*;");
		Body.pushln("import gps.writable.*;");
		Body.pushln("import gps.globalobjects.*;");
		Body.pushln("import org.apache.commons.cli.CommandLine;");
		Body.pushln("import org.apache.mina.core.buffer.IoBuffer;");
		Body.pushln("import java.io.IOException;");
		Body.pushln("import java.io.BufferedWriter;");
		Body.pushln("import java.util.Random;");
		Body.pushln("import java.lang.Math;");
	}

	public void generate_reduce_assign_vertex(ast_assign a, gm_code_writer Body, GM_REDUCE_T reduce_op_type) {
		assert a.is_target_scalar();
		ast_id id = a.get_lhs_scala();

		Body.push("getGlobalObjectsMap().putOrUpdateGlobalObject(");
		Body.push(create_key_string(id));
		Body.push(",");
		Body.push("new ");
		generate_broadcast_variable_type(id.getTypeSummary(), Body, reduce_op_type); // create
																						// BV
																						// by
																						// type

		// ---------------------------------------------------
		// Initial Value: Reading of Id
		// ---------------------------------------------------
		Body.push("(");
		get_main().generate_expr(a.get_rhs());
		Body.push(")");
		Body.pushln(");");
	}

	public void generate_broadcast_receive_vertex(ast_id id, gm_code_writer Body) {
		Body.push("((");
		generate_broadcast_variable_type(id.getTypeSummary(), Body);
		Body.push(")");
		Body.push("getGlobalObjectsMap().getGlobalObject(");
		Body.push(create_key_string(id));
		Body.push(")).getValue().getValue()");
	}

	public void generate_vertex_prop_class_details(java.util.HashSet<gm_symtab_entry> prop, gm_code_writer Body, boolean is_edge_prop) {
		String temp = new String(new char[1024]);
		int total = is_edge_prop ? ((gm_gps_beinfo) GlobalMembersGm_main.FE.get_current_backend_info()).get_total_edge_property_size()
				: ((gm_gps_beinfo) GlobalMembersGm_main.FE.get_current_backend_info()).get_total_node_property_size();

		Body.pushln("@Override");
		Body.push("public int numBytes() {return ");
		temp = String.format("%d;}", total);
		Body.pushln(temp);

		Body.pushln("@Override");
		Body.pushln("public void write(IoBuffer IOB) {");
		for (gm_symtab_entry sym : prop) {
			genPutIOB(sym.getId().get_genname(), sym.getType().getTargetTypeSummary(), Body, this);
		}
		Body.pushln("}");

		Body.pushln("@Override");
		Body.pushln("public void read(IoBuffer IOB) {");
		for (gm_symtab_entry sym : prop) {
			genGetIOB(sym.getId().get_genname(), sym.getType().getTargetTypeSummary(), Body, this);
		}
		Body.pushln("}");

		Body.pushln("@Override");
		Body.pushln("public int read(byte[] _BA, int _idx) {");
		for (gm_symtab_entry sym : prop) {
			gps_syminfo syminfo = (gps_syminfo) sym.find_info(GlobalMembersGps_syminfo.GPS_TAG_BB_USAGE);
			int base = syminfo.get_start_byte();
			genReadByte(sym.getId().get_genname(), sym.getType().getTargetTypeSummary(), base, Body, this);
		}
		temp = String.format("return %d;", total);
		Body.pushln(temp);
		Body.pushln("}");

		Body.pushln("@Override");
		Body.pushln("public int read(IoBuffer IOB, byte[] _BA, int _idx) {");
		temp = String.format("IOB.get(_BA, _idx, %d);", total);
		Body.pushln(temp);
		temp = String.format("return %d;", total);
		Body.pushln(temp);
		Body.pushln("}");

		Body.pushln("@Override");
		Body.pushln("public void combine(byte[] _MQ, byte [] _tA) {");
		Body.pushln(" // do nothing");
		Body.pushln("}");

		Body.pushln("@Override");
		Body.pushln("public String toString() {");
		Body.push("return \"\"");
		boolean firstProperty = true;
		for (gm_symtab_entry sym : prop) {
			// this property is set to procedure argument only
			if (sym.find_info(GlobalMembersGm_frontend.GMUSAGE_PROPERTY) == null) {
				// printf("no argument property :%s\n",
				// sym->getId()->get_genname());
				continue;
			}
			// Used as input only
			if (sym.find_info_int(GlobalMembersGm_frontend.GMUSAGE_PROPERTY) == GM_PROP_USAGE_T.GMUSAGE_IN.getValue()) {
				// printf("used as input only :%s\n",
				// sym->getId()->get_genname());
				continue;
			}
			Body.push(" + \"");
			if (firstProperty) {
				firstProperty = false;
			} else {
				Body.push("\\t");
			}
			temp = String.format("%s: \" + %s", sym.getId().get_genname(), sym.getId().get_genname());
			Body.push(temp);
		}
		Body.pushln(";");
		Body.pushln("}");

		// Edge Property is read-only
		// if (is_edge_prop && prop.size() > 0) {
		ast_procdef proc = GlobalMembersGm_main.FE.get_current_proc();
		if ((is_edge_prop && prop.size() > 0) || (!is_edge_prop && proc.find_info_bool(GPS_FLAG_NODE_VALUE_INIT))) {
			Body.pushln("//Input Data Parsing");
			Body.pushln("@Override");
			Body.pushln("public void read(String inputString) {");
			int total_count = 0;
			if (is_edge_prop)
				total_count = prop.size();
			else {
				for (gm_symtab_entry e : prop) {
					if ((e.find_info_int(GlobalMembersGm_frontend.GMUSAGE_PROPERTY) == GM_PROP_USAGE_T.GMUSAGE_IN.getValue())
							|| (e.find_info_int(GlobalMembersGm_frontend.GMUSAGE_PROPERTY) == GM_PROP_USAGE_T.GMUSAGE_INOUT.getValue()))
						total_count++;
				}
			}

			if (total_count == 1) {
				for (gm_symtab_entry sym : prop) {
					if (!is_edge_prop && (sym.find_info_int(GlobalMembersGm_frontend.GMUSAGE_PROPERTY) != GM_PROP_USAGE_T.GMUSAGE_IN.getValue())
							&& (sym.find_info_int(GlobalMembersGm_frontend.GMUSAGE_PROPERTY) != GM_PROP_USAGE_T.GMUSAGE_INOUT.getValue()))
						continue;
					RefObject<String> name1_ref = new RefObject<String>(null);
					RefObject<String> name2_ref = new RefObject<String>(null);
					get_java_parse_string(this, sym.getType().getTargetTypeSummary(), name1_ref, name2_ref);
					String name1 = name1_ref.argvalue;
					String name2 = name2_ref.argvalue;
					temp = String.format("this.%s = %s.%s(inputString);", sym.getId().get_genname(), name1, name2);
					Body.pushln(temp);
				}
			} else {
				Body.pushln("String[] split = inputString.split(\"###\");");
				boolean firstProperty1 = true; // FIXME: was firstProperty -
												// seems to be a bug in the cpp
												// gm-compiler
				int cnt = 0;
				for (gm_symtab_entry sym : prop) {
					if (!is_edge_prop && (sym.find_info_int(GlobalMembersGm_frontend.GMUSAGE_PROPERTY) != GM_PROP_USAGE_T.GMUSAGE_IN.getValue())
							&& (sym.find_info_int(GlobalMembersGm_frontend.GMUSAGE_PROPERTY) != GM_PROP_USAGE_T.GMUSAGE_INOUT.getValue()))
						continue;
					RefObject<String> name1_ref = new RefObject<String>(null);
					RefObject<String> name2_ref = new RefObject<String>(null);
					get_java_parse_string(this, sym.getType().getTargetTypeSummary(), name1_ref, name2_ref);
					String name1 = name1_ref.argvalue;
					String name2 = name2_ref.argvalue;
					temp = String.format("this.%s = %s.%s((split[%d]==null)?\"0\":split[%d]);", sym.getId().get_genname(), name1, name2, cnt, cnt);
					Body.pushln(temp);
					cnt++;
				}
			}
			Body.pushln("}");
		}
	}

	public void generate_receive_state_vertex(String state_var, gm_code_writer Body) {
		String temp = new String(new char[1024]);
		temp = String.format("int %s = ((IntOverwriteGlobalObject) getGlobalObjectsMap().getGlobalObject(", state_var);
		Body.push(temp);
		Body.push(GPS_KEY_FOR_STATE);
		Body.pushln(")).getValue().getValue();");
	}

	public void generate_receive_isFirst_vertex(String is_first_var, gm_code_writer Body) {
		String temp = new String(new char[1024]);
		temp = String.format("boolean %s = ((BooleanOverwriteGlobalObject) getGlobalObjectsMap().getGlobalObject(\"%s\"", is_first_var, is_first_var);
		Body.push(temp);
		Body.pushln(")).getValue().getValue();");
	}

	public void generate_message_fields_define(GMTYPE_T gm_type, int count, gm_code_writer Body) {
		for (int i = 0; i < count; i++) {
			String str = main.get_type_string(gm_type);
			String vname = get_message_field_var_name(gm_type, i);
			str_buf = String.format("%s %s;", str, vname);
			Body.pushln(str_buf);
			vname = null;
		}
	}

	// virtual void generate_message_class_details(gm_gps_beinfo info,
	// gm_code_writer Body);

	public void generate_vertex_prop_access_lhs(ast_id id, gm_code_writer Body) {
		String temp = new String(new char[1024]);
		temp = String.format("%s.%s", STATE_SHORT_CUT, id.get_genname());
		Body.push(temp);
	}

	public void generate_vertex_prop_access_lhs_edge(ast_id id, gm_code_writer Body) {
		String temp = new String(new char[1024]);
		temp = String.format("_outEdge.getEdgeValue().%s", id.get_genname());
		Body.push(temp);
	}

	public void generate_vertex_prop_access_rhs(ast_id id, gm_code_writer Body) {
		generate_vertex_prop_access_lhs(id, Body);
	}

	public void generate_vertex_prop_access_rhs_edge(ast_id id, gm_code_writer Body) {
		generate_vertex_prop_access_lhs_edge(id, Body);
	}

	public void generate_vertex_prop_access_remote_lhs(ast_id id, gm_code_writer Body) {
		String temp = new String(new char[1024]);
		temp = String.format("_remote_%s", id.get_genname());
		Body.push(temp);
	}

	public void generate_vertex_prop_access_remote_lhs_edge(ast_id id, gm_code_writer Body) {
		String temp = new String(new char[1024]);
		temp = String.format("_remote_%s", id.get_genname());
		Body.push(temp);
	}

	public void generate_vertex_prop_access_remote_rhs(ast_id id, gm_code_writer Body) {
		generate_vertex_prop_access_remote_lhs(id, Body);
	}

	public void generate_vertex_prop_access_prepare(gm_code_writer Body) {
		String temp = new String(new char[1024]);
		temp = String.format("VertexData %s = getValue();", STATE_SHORT_CUT);
		Body.pushln(temp);
	}

	public void generate_node_iterator_rhs(ast_id id, gm_code_writer Body) {
		Body.push("getId()");
	}

	public int get_type_size(ast_typedecl t) {
		return get_type_size(t.getTypeSummary());
	}

	public int get_type_size(GMTYPE_T gm_type) {
		if (gm_type == GMTYPE_T.GMTYPE_NODE) {
			if (this.is_node_type_int())
				return 4;
			else
				return 8;
		} else if (gm_type == GMTYPE_T.GMTYPE_EDGE) {
			assert false;
			return 0;
		}

		return get_java_type_size(gm_type);
	}

	// caller should delete var_name later
	public String get_message_field_var_name(GMTYPE_T gm_type, int index) {

		String str = main.get_type_string(gm_type);
		return String.format("%c%d", str.charAt(0), index);
	}

	// virtual void generate_message_send(ast_foreach fe, gm_code_writer Body);

	// virtual void generate_message_receive_begin(ast_foreach fe,
	// gm_code_writer Body, gm_gps_basic_block b, boolean is_only_comm);
	// virtual void generate_message_receive_begin(ast_sentblock sb,
	// gm_symtab_entry drv, gm_code_writer Body, gm_gps_basic_block b, boolean
	// is_only_comm);
	// virtual void generate_message_receive_begin(gm_gps_comm_unit U,
	// gm_code_writer Body, gm_gps_basic_block b, boolean is_only_comm);

	// virtual void generate_message_receive_end(gm_code_writer Body, boolean
	// is_only_comm);

	// random write
	public void generate_message_create_for_random_write(ast_sentblock sb, gm_symtab_entry sym, gm_code_writer Body) {
		gm_gps_beinfo info = (gm_gps_beinfo) GlobalMembersGm_main.FE.get_current_backend_info();
		gm_gps_comm_t m_type = GPS_COMM_RANDOM_WRITE;

		gm_gps_comm_unit U = new gm_gps_comm_unit(m_type, sb, sym);

		gm_gps_communication_size_info SINFO = info.find_communication_size_info(U);

		Body.NL();
		Body.push("MessageData ");
		Body.push(get_random_write_message_name(sym));
		Body.push(" = new MessageData(");
		// todo: should this always be a byte?
		str_buf = String.format("(byte) %d);", SINFO.msg_class.id);
		Body.pushln(str_buf);
	}

	public void generate_message_payload_packing_for_random_write(ast_assign a, gm_code_writer Body) {
		assert !a.is_argminmax_assign();
		assert !a.is_target_scalar();

		ast_sentblock sb = (ast_sentblock) a.find_info_ptr(GPS_FLAG_SENT_BLOCK_FOR_RANDOM_WRITE_ASSIGN);
		assert sb != null;

		// driver
		gm_symtab_entry sym = a.get_lhs_field().get_first().getSymInfo();

		// traverse rhs and put values in the message
		// printf("sb:%p, sym:%p\n", sb, sym);
		gps_random_write_rhs_t T = new gps_random_write_rhs_t(sb, sym, this, Body);

		a.get_rhs().traverse_post(T);

	}

	public void generate_message_send_for_random_write(ast_sentblock sb, gm_symtab_entry sym, gm_code_writer Body) {
		Body.push("sendMessage(");
		get_main().generate_rhs_id(sym.getId());
		Body.push(",");
		Body.push(get_random_write_message_name(sym));
		Body.pushln(");");
	}

	// virtual void generate_expr_builtin(ast_expr_builtin e, gm_code_writer
	// Body, boolean is_master);

	// virtual void generate_expr_nil(ast_expr e, gm_code_writer Body);
	public final String get_random_write_message_name(gm_symtab_entry sym) {
		str_buf = String.format("_msg_%s", sym.getId().get_genname());
		return str_buf;
	}

	// true if node == int false, if node == long
	public boolean is_node_type_int() {
		return true;
	}

	public boolean is_edge_type_int() {
		return true;
	}

	protected String str_buf = new String(new char[1024 * 8]);
	protected gm_gps_gen main;

	/* TODO Inserted from gm_gps_lib.java, clean up */

	public static int get_java_type_size(GMTYPE_T gm_type) {
		switch (gm_type) {
		case GMTYPE_INT:
			return 4;
		case GMTYPE_LONG:
			return 8;
		case GMTYPE_FLOAT:
			return 4;
		case GMTYPE_DOUBLE:
			return 8;
		case GMTYPE_BOOL:
			return 1;
		default:
			System.out.printf("type = %s\n", GlobalMembersGm_misc.gm_get_type_string(gm_type));
			assert false;
			return 0;
		}
	}

	public static void genPutIOB(String name, GMTYPE_T gm_type, gm_code_writer Body, gm_gpslib lib) {
		if (gm_type.is_node_compatible_type())
			gm_type = GMTYPE_T.GMTYPE_NODE; // TODO setting input var?
		if (gm_type.is_edge_compatible_type())
			gm_type = GMTYPE_T.GMTYPE_EDGE; // TODO setting input var?

		// assumtion: IOB name is IOB
		Body.push("IOB.");
		switch (gm_type) {
		case GMTYPE_INT:
			Body.push("putInt");
			break;
		case GMTYPE_LONG:
			Body.push("putLong");
			break;
		case GMTYPE_FLOAT:
			Body.push("putFloat");
			break;
		case GMTYPE_DOUBLE:
			Body.push("putDouble");
			break;
		case GMTYPE_BOOL:
			Body.push("put");
			break;
		case GMTYPE_NODE:
			if (lib.is_node_type_int()) {
				Body.push("putInt");
				break;
			} else {
				Body.push("putLong");
				break;
			}
		case GMTYPE_EDGE:
			if (lib.is_edge_type_int()) {
				Body.push("putInt");
				break;
			} else {
				Body.push("putLong");
				break;
			}
		default:
			assert false;
			break;
		}
		Body.push("(");
		if (gm_type == GMTYPE_T.GMTYPE_BOOL) {
			Body.push(name);
			Body.push("?(byte)1:(byte)0");
		} else {
			Body.push(name);
		}
		Body.pushln(");");
	}

	public static void genGetIOB(String name, GMTYPE_T gm_type, gm_code_writer Body, gm_gpslib lib) {
		if (gm_type.is_node_compatible_type())
			gm_type = GMTYPE_T.GMTYPE_NODE;
		if (gm_type.is_edge_compatible_type())
			gm_type = GMTYPE_T.GMTYPE_EDGE;

		// assumtion: IOB name is IOB
		Body.push(name);
		Body.push("= IOB.");
		switch (gm_type) {
		case GMTYPE_INT:
			Body.push("getInt()");
			break;
		case GMTYPE_LONG:
			Body.push("getLong()");
			break;
		case GMTYPE_FLOAT:
			Body.push("getFloat()");
			break;
		case GMTYPE_DOUBLE:
			Body.push("getDouble()");
			break;
		case GMTYPE_BOOL:
			Body.push("get()==0?false:true");
			break;
		case GMTYPE_NODE:
			if (lib.is_node_type_int()) {
				Body.push("getInt()");
				break;
			} else {
				Body.push("getLong()");
				break;
			}
		case GMTYPE_EDGE:
			if (lib.is_edge_type_int()) {
				Body.push("getInt()");
				break;
			} else {
				Body.push("getLong()");
				break;
			}
		default:
			assert false;
			break;
		}
		Body.pushln(";");
	}

	public static void genReadByte(String name, GMTYPE_T gm_type, int offset, gm_code_writer Body, gm_gpslib lib) {
		if (gm_type.is_node_compatible_type()) {
			gm_type = (lib.is_node_type_int()) ? GMTYPE_T.GMTYPE_INT : GMTYPE_T.GMTYPE_LONG;
		}
		// assumption: "byte[] _BA, int _idx"
		Body.push(name);
		Body.push("= Utils.");
		switch (gm_type) {
		case GMTYPE_INT:
			Body.push("byteArrayToIntBigEndian(");
			break;
		case GMTYPE_LONG:
			Body.push("byteArrayToLongBigEndian(");
			break;
		case GMTYPE_FLOAT:
			Body.push("byteArrayToFloatBigEndian(");
			break;
		case GMTYPE_DOUBLE:
			Body.push("byteArrayToDoubleBigEndian(");
			break;
		case GMTYPE_BOOL:
			Body.push("byteArrayToBooleanBigEndian(");
			break;
		default:
			assert false;
			break;
		}
		String str_buf = new String(new char[1024]);
		str_buf = String.format("_BA, _idx + %d);", offset);
		Body.pushln(str_buf);
	}

	// TODO set output vars?
	public static void get_java_parse_string(gm_gpslib L, GMTYPE_T gm_type, RefObject<String> name1_ref, RefObject<String> name2_ref) {
		String name1;
		String name2;

		switch (gm_type) {
		case GMTYPE_INT:
			name1 = "Integer";
			name2 = "parseInt";
			break;
		case GMTYPE_LONG:
			name1 = "Long";
			name2 = "parseLong";
			break;
		case GMTYPE_FLOAT:
			name1 = "Float";
			name2 = "parseFloat";
			break;
		case GMTYPE_DOUBLE:
			name1 = "Double";
			name2 = "parseDouble";
			break;
		case GMTYPE_BOOL:
			name1 = "Boolean";
			name2 = "parseBoolean";
			break;
		case GMTYPE_NODE:
			if (L.is_node_type_int()) {
				name1 = "Integer";
				name2 = "parseInt";
				break;
			} else {
				name1 = "Long";
				name2 = "parseLong";
				break;
			}
		case GMTYPE_EDGE:
			if (L.is_edge_type_int()) {
				name1 = "Integer";
				name2 = "parseInt";
				break;
			} else {
				name1 = "Long";
				name2 = "parseLong";
				break;
			}
		default:
			assert false;
			throw new AssertionError();
		}

		name1_ref.argvalue = name1;
		name2_ref.argvalue = name2;
	}

	public static int get_total_size(gm_gps_communication_size_info I) {
		int sz = 0;
		sz += get_java_type_size(GMTYPE_T.GMTYPE_INT) * I.num_int;
		sz += get_java_type_size(GMTYPE_T.GMTYPE_BOOL) * I.num_bool;
		sz += get_java_type_size(GMTYPE_T.GMTYPE_LONG) * I.num_long;
		sz += get_java_type_size(GMTYPE_T.GMTYPE_DOUBLE) * I.num_double;
		sz += get_java_type_size(GMTYPE_T.GMTYPE_FLOAT) * I.num_float;

		return sz;
	}

	// C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced
	// in-line:
	// /#define MESSAGE_PER_TYPE_LOOP_BEGIN(info, SYMS, str_buf)
	// std::list<gm_gps_congruent_msg_class*>& LOOPS =
	// info->get_congruent_message_classes();
	// std::list<gm_gps_congruent_msg_class*>::iterator I; bool is_single =
	// info->is_single_message(); bool is_first = true; for(I=LOOPS.begin();
	// I!=LOOPS.end(); I++) { gm_gps_communication_size_info& SYMS =
	// *((*I)->sz_info); int sz = get_total_size(SYMS); if (!is_single &&
	// is_first) { is_first = false; sprintf(str_buf,"if (m_type == %d) ",
	// SYMS.id); Body.push(str_buf); } else if (!is_single) {
	// sprintf(str_buf,"else if (m_type == %d) ", SYMS.id); Body.push(str_buf);
	// }
	// C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced
	// in-line:
	// /#define MESSAGE_PER_TYPE_LOOP_END() } if (!is_single)
	// Body.pushln("//for empty messages (signaling only)");
	public static void generate_message_write_each(gm_gpslib lib, int cnt, GMTYPE_T gm_type, gm_code_writer Body) {
		for (int i = 0; i < cnt; i++) {
			String vname = lib.get_message_field_var_name(gm_type, i);
			genPutIOB(vname, gm_type, Body, lib);
			vname = null;
		}
	}

	public static void generate_message_read1_each(gm_gpslib lib, int cnt, GMTYPE_T gm_type, gm_code_writer Body) {
		for (int i = 0; i < cnt; i++) {
			String vname = lib.get_message_field_var_name(gm_type, i);
			genGetIOB(vname, gm_type, Body, lib);
			vname = null;
		}
	}

	public static void generate_message_read2_each(gm_gpslib lib, int cnt, GMTYPE_T gm_type, gm_code_writer Body, tangible.RefObject<Integer> offset) {
		for (int i = 0; i < cnt; i++) {
			String vname = lib.get_message_field_var_name(gm_type, i);
			genReadByte(vname, gm_type, offset.argvalue, Body, lib);
			offset.argvalue += get_java_type_size(gm_type);
			vname = null;
		}
	}

	// C++ TO JAVA CONVERTER NOTE: This was formerly a static local variable
	// declaration (not allowed in Java):
	//private static void generate_message_class_get_size_generate_message_class_read1(gm_gpslib lib, gm_gps_beinfo info, gm_code_writer Body) {
	//}

	// C++ TO JAVA CONVERTER NOTE: This was formerly a static local variable
	// declaration (not allowed in Java):
	//private static void generate_message_class_get_size_generate_message_class_read2(gm_gpslib lib, gm_gps_beinfo info, gm_code_writer Body) {
	//}

	// C++ TO JAVA CONVERTER NOTE: This was formerly a static local variable
	// declaration (not allowed in Java):
	//private static void generate_message_class_get_size_generate_message_class_read3(gm_gpslib lib, gm_gps_beinfo info, gm_code_writer Body) {
	//}

	// C++ TO JAVA CONVERTER NOTE: This was formerly a static local variable
	// declaration (not allowed in Java):
	//private static void generate_message_class_get_size_generate_message_class_combine(gm_gpslib lib, gm_gps_beinfo info, gm_code_writer Body) {
	//}

	// C++ TO JAVA CONVERTER NOTE: This was formerly a static local variable
	// declaration (not allowed in Java):
	//private static boolean generate_message_class_get_size_is_symbol_defined_in_bb(gm_gps_basic_block b, gm_symtab_entry e) {
	//}

	public static void generate_message_class_get_size(gm_gps_beinfo info, gm_code_writer Body) {
		Body.pushln("@Override");
		Body.pushln("public int numBytes() {");
		String str_buf;

		LinkedList<gm_gps_congruent_msg_class> LOOPS = info.get_congruent_message_classes();
		boolean is_single = info.is_single_message();
		boolean is_first = true;
		for (gm_gps_congruent_msg_class c : LOOPS) {

			gm_gps_communication_size_info SYMS = c.sz_info;
			int sz = get_total_size(SYMS);
			if (!is_single && is_first) {
				is_first = false;
				str_buf = String.format("if (m_type == %d) ", SYMS.id);
				Body.push(str_buf);
			} else if (!is_single) {
				str_buf = String.format("else if (m_type == %d) ", SYMS.id);
				Body.push(str_buf);
			}
			;
			if (info.is_single_message()) {
				if (get_total_size(SYMS) == 0)
					str_buf = String.format("return 1; // empty message ");
				else
					str_buf = String.format("return %d; // data", get_total_size(SYMS));
			} else
				str_buf = String.format("return (1+%d); // type + data", get_total_size(SYMS));
			Body.pushln(str_buf);
		}
		if (!is_single)
			Body.pushln("//for empty messages (signaling only)");
		if (!info.is_single_message())
			Body.pushln("return 1; ");
		else if (info.is_empty_message())
			Body.pushln("return 0; ");
		Body.pushln("}");
	}

	// C++ TO JAVA CONVERTER NOTE: This static local variable declaration (not
	// allowed in Java) has been moved just prior to the method:
	static void generate_message_class_write(gm_gpslib lib, gm_gps_beinfo info, gm_code_writer Body) {
		Body.pushln("@Override");
		Body.pushln("public void write(IoBuffer IOB) {");
		if (!info.is_single_message())
			Body.pushln("IOB.put(m_type);");
		String str_buf;
		LinkedList<gm_gps_congruent_msg_class> LOOPS = info.get_congruent_message_classes();
		boolean is_single = info.is_single_message();
		boolean is_first = true;
		for (gm_gps_congruent_msg_class c : LOOPS) {
			gm_gps_communication_size_info SYMS = c.sz_info;
			int sz = get_total_size(SYMS);
			if (!is_single && is_first) {
				is_first = false;
				str_buf = String.format("if (m_type == %d) ", SYMS.id);
				Body.push(str_buf);
			} else if (!is_single) {
				str_buf = String.format("else if (m_type == %d) ", SYMS.id);
				Body.push(str_buf);
			}
			if (!info.is_single_message())
				Body.pushln("{");
			if (info.is_single_message() && get_total_size(SYMS) == 0)
				Body.pushln("IOB.put((byte)0); // empty message");
			generate_message_write_each(lib, SYMS.num_int, GMTYPE_T.GMTYPE_INT, Body);
			generate_message_write_each(lib, SYMS.num_long, GMTYPE_T.GMTYPE_LONG, Body);
			generate_message_write_each(lib, SYMS.num_float, GMTYPE_T.GMTYPE_FLOAT, Body);
			generate_message_write_each(lib, SYMS.num_double, GMTYPE_T.GMTYPE_DOUBLE, Body);
			generate_message_write_each(lib, SYMS.num_bool, GMTYPE_T.GMTYPE_BOOL, Body);
			if (!info.is_single_message())
				Body.pushln("}");
		}
		if (!is_single)
			Body.pushln("//for empty messages (signaling only)");
		Body.pushln("}");
	}

	// C++ TO JAVA CONVERTER NOTE: This static local variable declaration (not
	// allowed in Java) has been moved just prior to the method:
	static void generate_message_class_read1(gm_gpslib lib, gm_gps_beinfo info, gm_code_writer Body) {
		Body.pushln("@Override");
		Body.pushln("public void read(IoBuffer IOB) {");
		if (!info.is_single_message())
			Body.pushln("m_type = IOB.get();");
		String str_buf;
		LinkedList<gm_gps_congruent_msg_class> LOOPS = info.get_congruent_message_classes();
		boolean is_single = info.is_single_message();
		boolean is_first = true;
		for (gm_gps_congruent_msg_class c : LOOPS) {
			gm_gps_communication_size_info SYMS = c.sz_info;
			int sz = get_total_size(SYMS);
			if (!is_single && is_first) {
				is_first = false;
				str_buf = String.format("if (m_type == %d) ", SYMS.id);
				Body.push(str_buf);
			} else if (!is_single) {
				str_buf = String.format("else if (m_type == %d) ", SYMS.id);
				Body.push(str_buf);
			}
			if (!info.is_single_message())
				Body.pushln("{");
			if (info.is_single_message() && get_total_size(SYMS) == 0)
				Body.pushln("IOB.get(); // consume empty message byte");
			generate_message_read1_each(lib, SYMS.num_int, GMTYPE_T.GMTYPE_INT, Body);
			generate_message_read1_each(lib, SYMS.num_long, GMTYPE_T.GMTYPE_LONG, Body);
			generate_message_read1_each(lib, SYMS.num_float, GMTYPE_T.GMTYPE_FLOAT, Body);
			generate_message_read1_each(lib, SYMS.num_double, GMTYPE_T.GMTYPE_DOUBLE, Body);
			generate_message_read1_each(lib, SYMS.num_bool, GMTYPE_T.GMTYPE_BOOL, Body);
			if (!info.is_single_message())
				Body.pushln("}");
		}
		if (!is_single)
			Body.pushln("//for empty messages (signaling only)");
		Body.pushln("}");
	}

	// C++ TO JAVA CONVERTER NOTE: This static local variable declaration (not
	// allowed in Java) has been moved just prior to the method:
	static void generate_message_class_read2(gm_gpslib lib, gm_gps_beinfo info, gm_code_writer Body) {
		Body.pushln("@Override");
		Body.pushln("public int read(byte[] _BA, int _idx) {");
		if (!info.is_single_message())
			Body.pushln("m_type = _BA[_idx];");
		String str_buf;
		LinkedList<gm_gps_congruent_msg_class> LOOPS = info.get_congruent_message_classes();
		boolean is_single = info.is_single_message();
		boolean is_first = true;
		for (gm_gps_congruent_msg_class c : LOOPS) {
			gm_gps_communication_size_info SYMS = c.sz_info;
			int sz = get_total_size(SYMS);
			if (!is_single && is_first) {
				is_first = false;
				str_buf = String.format("if (m_type == %d) ", SYMS.id);
				Body.push(str_buf);
			} else if (!is_single) {
				str_buf = String.format("else if (m_type == %d) ", SYMS.id);
				Body.push(str_buf);
			}
			int offset;
			if (info.is_single_message())
				offset = 0;
			else
				offset = 1;
			if (!info.is_single_message())
				Body.pushln("{");
			if (info.is_single_message() && (get_total_size(SYMS) == 0))
				Body.pushln("_idx++; // consume empty message byte");
			tangible.RefObject<Integer> tempRef_offset = new tangible.RefObject<Integer>(offset);
			generate_message_read2_each(lib, SYMS.num_int, GMTYPE_T.GMTYPE_INT, Body, tempRef_offset);
			offset = tempRef_offset.argvalue;
			tangible.RefObject<Integer> tempRef_offset2 = new tangible.RefObject<Integer>(offset);
			generate_message_read2_each(lib, SYMS.num_long, GMTYPE_T.GMTYPE_LONG, Body, tempRef_offset2);
			offset = tempRef_offset2.argvalue;
			tangible.RefObject<Integer> tempRef_offset3 = new tangible.RefObject<Integer>(offset);
			generate_message_read2_each(lib, SYMS.num_float, GMTYPE_T.GMTYPE_FLOAT, Body, tempRef_offset3);
			offset = tempRef_offset3.argvalue;
			tangible.RefObject<Integer> tempRef_offset4 = new tangible.RefObject<Integer>(offset);
			generate_message_read2_each(lib, SYMS.num_double, GMTYPE_T.GMTYPE_DOUBLE, Body, tempRef_offset4);
			offset = tempRef_offset4.argvalue;
			tangible.RefObject<Integer> tempRef_offset5 = new tangible.RefObject<Integer>(offset);
			generate_message_read2_each(lib, SYMS.num_bool, GMTYPE_T.GMTYPE_BOOL, Body, tempRef_offset5);
			offset = tempRef_offset5.argvalue;
			if (info.is_single_message()) {
				if (get_total_size(SYMS) == 0)
					str_buf = "return 1;";
				else
					str_buf = String.format("return %d;", get_total_size(SYMS));
			} else
				str_buf = String.format("return 1 + %d;", get_total_size(SYMS));
			Body.pushln(str_buf);
			if (!info.is_single_message())
				Body.pushln("}");
		}
		if (!is_single)
			Body.pushln("//for empty messages (signaling only)");
		if (!info.is_single_message())
			Body.pushln("return 1;");
		else if (info.is_empty_message())
			Body.pushln("return 0;");
		Body.pushln("}");
	}

	// C++ TO JAVA CONVERTER NOTE: This static local variable declaration (not
	// allowed in Java) has been moved just prior to the method:
	static void generate_message_class_read3(gm_gpslib lib, gm_gps_beinfo info, gm_code_writer Body) {
		Body.pushln("@Override");
		Body.pushln("public int read(IoBuffer IOB, byte[] _BA, int _idx) {");
		if (!info.is_single_message())
			Body.pushln("byte m_type; IOB.get(_BA, _idx, 1); m_type = _BA[_idx];");
		String str_buf;
		LinkedList<gm_gps_congruent_msg_class> LOOPS = info.get_congruent_message_classes();
		boolean is_single = info.is_single_message();
		boolean is_first = true;
		for (gm_gps_congruent_msg_class c : LOOPS) {
			gm_gps_communication_size_info SYMS = c.sz_info;
			int sz = get_total_size(SYMS);
			if (!is_single && is_first) {
				is_first = false;
				str_buf = String.format("if (m_type == %d) ", SYMS.id);
				Body.push(str_buf);
			} else if (!is_single) {
				str_buf = String.format("else if (m_type == %d) ", SYMS.id);
				Body.push(str_buf);
			}
			int offset;
			if (!info.is_single_message())
				offset = 1;
			else
				offset = 0;
			if (!info.is_single_message())
				Body.pushln("{");
			int sz2 = get_total_size(SYMS);
			if (info.is_single_message() && (get_total_size(SYMS) == 0)) {
				Body.pushln("//empty message(dummy byte)");
				sz2 = 1;
			}
			if (sz2 == 0) {
				str_buf = "//empty message";
			} else {
				str_buf = String.format("IOB.get(_BA, _idx+%d, %d);", offset, sz2);
			}
			Body.pushln(str_buf);

			if (info.is_single_message())
				if (get_total_size(SYMS) == 0)
					str_buf = "return 1;";
				else
					str_buf = String.format("return %d;", sz2);
			else
				str_buf = String.format("return 1 + %d;", sz2);
			Body.pushln(str_buf);
			if (!info.is_single_message())
				Body.pushln("}");
		}
		if (!is_single)
			Body.pushln("//for empty messages (signaling only)");
		if (!info.is_single_message())
			Body.pushln("return 1;");
		else if (info.is_empty_message())
			Body.pushln("return 0;");
		Body.pushln("}");
	}

	// C++ TO JAVA CONVERTER NOTE: This static local variable declaration (not
	// allowed in Java) has been moved just prior to the method:
	static void generate_message_class_combine(gm_gpslib lib, gm_gps_beinfo info, gm_code_writer Body) {
		Body.pushln("@Override");
		Body.pushln("public void combine(byte[] _MQ, byte [] _tA) {");
		Body.pushln("//do nothing");

		Body.pushln("}");
	}

	public void generate_message_class_details(gm_gps_beinfo info, gm_code_writer Body) {

		Body.pushln("// union of all message fields  ");
		gm_gps_communication_size_info size_info = info.get_max_communication_size();

		generate_message_fields_define(GMTYPE_T.GMTYPE_INT, size_info.num_int, Body);
		generate_message_fields_define(GMTYPE_T.GMTYPE_LONG, size_info.num_long, Body);
		generate_message_fields_define(GMTYPE_T.GMTYPE_FLOAT, size_info.num_float, Body);
		generate_message_fields_define(GMTYPE_T.GMTYPE_DOUBLE, size_info.num_double, Body);
		generate_message_fields_define(GMTYPE_T.GMTYPE_BOOL, size_info.num_bool, Body);
		Body.NL();

		generate_message_class_get_size(info, Body);
		generate_message_class_write(this, info, Body);
		generate_message_class_read1(this, info, Body);
		generate_message_class_read2(this, info, Body);
		generate_message_class_read3(this, info, Body);
		generate_message_class_combine(this, info, Body);
		Body.NL();
	}

	public void generate_message_send(ast_foreach fe, gm_code_writer Body) {

		gm_gps_beinfo info = (gm_gps_beinfo) GlobalMembersGm_main.FE.get_current_backend_info();

		gm_gps_comm_t m_type = (fe == null) ? GPS_COMM_INIT : GPS_COMM_NESTED;

		gm_gps_comm_unit U = new gm_gps_comm_unit(m_type, fe);

		LinkedList<gm_gps_communication_symbol_info> LIST = info.get_all_communication_symbols(U);

		gm_gps_communication_size_info SINFO = info.find_communication_size_info(U);

		boolean need_separate_message = (fe == null) ? false : fe.find_info_bool(GPS_FLAG_EDGE_DEFINING_INNER);
		boolean is_in_neighbors = (fe != null) && (fe.get_iter_type() == GMTYPE_T.GMTYPE_NODEITER_IN_NBRS);

		if (!need_separate_message) {
			Body.pushln("// Sending messages to all neighbors (if there is a neighbor)");
			if (is_in_neighbors) {
				String temp = String.format("if (%s.%s.length > 0) {", STATE_SHORT_CUT, GPS_REV_NODE_ID);
				Body.pushln(temp);
			} else {
				Body.pushln("if (getNeighborsSize() > 0) {");
			}
		} else {
			assert (fe != null) && (fe.get_iter_type() == GMTYPE_T.GMTYPE_NODEITER_NBRS);
			Body.pushln("for (Edge<EdgeData> _outEdge : getOutgoingEdges()) {");
			Body.pushln("// Sending messages to each neighbor");
		}

		// check if any edge updates that should be done before message sending
		LinkedList<ast_sent> sents_after_message = new LinkedList<ast_sent>();

		if ((fe != null) && (fe.has_info_list(GPS_LIST_EDGE_PROP_WRITE))) {
			LinkedList<Object> L = fe.get_info_list(GPS_LIST_EDGE_PROP_WRITE);

			for (Object obj : L) {
				ast_sent s = (ast_sent) obj;
				assert s.get_nodetype() == AST_NODE_TYPE.AST_ASSIGN;
				ast_assign a = (ast_assign) s;
				assert !a.is_target_scalar();
				gm_symtab_entry e = a.get_lhs_field().get_second().getSymInfo();
				Integer i = (Integer) fe.find_info_map_value(GPS_MAP_EDGE_PROP_ACCESS, e);
				assert i != null;

				if (i == gm_gps_edge_access_t.GPS_ENUM_EDGE_VALUE_SENT_WRITE.getValue()) {
					sents_after_message.addLast(s);
				} else {
					get_main().generate_sent(s);
				}
			}
		}

		Body.push("MessageData _msg = new MessageData(");

		// todo: should this always be a byte?
		str_buf = String.format("(byte) %d", SINFO.msg_class.id);
		Body.push(str_buf);
		Body.pushln(");");

		// ------------------------------------------------------------
		// create message variables
		// ------------------------------------------------------------
		if (fe != null) {
			assert fe.get_body().get_nodetype() == AST_NODE_TYPE.AST_SENTBLOCK;
			ast_sentblock sb = (ast_sentblock) fe.get_body();
			for (ast_sent s : sb.get_sents()) {
				if (s.find_info_bool(GPS_FLAG_COMM_DEF_ASSIGN)) {
					get_main().generate_sent(s);
				}
			}
		}

		for (gm_gps_communication_symbol_info SYM : LIST) {
			Body.push("_msg.");
			String fname = get_message_field_var_name(SYM.gm_type, SYM.idx);
			Body.push(fname);
			fname = null;
			Body.push(" = ");
			gm_symtab_entry e = SYM.symbol;
			if (e.getType().is_node_property()) {
				generate_vertex_prop_access_rhs(e.getId(), Body);
			} else if (e.getType().is_edge_property()) {
				generate_vertex_prop_access_rhs_edge(e.getId(), Body);
			} else {
				get_main().generate_rhs_id(e.getId());
			}
			Body.pushln(";");
		}

		if (!need_separate_message) {
			if (is_in_neighbors) {
				String temp = String.format("sendMessages(%s.%s, _msg);", STATE_SHORT_CUT, GPS_REV_NODE_ID);
				Body.pushln(temp);
			} else {
				Body.pushln("sendMessages(getNeighborIds(), _msg);");
			}
			Body.pushln("}");
		} else {
			Body.pushln("sendMessage(_outEdge.getNeighborId(), _msg);");
			if (sents_after_message.size() > 0) {
				Body.NL();
				for (ast_sent s : sents_after_message) {
					get_main().generate_sent(s);
				}

				sents_after_message.clear();
			}
			Body.pushln("}");
		}
		assert sents_after_message.size() == 0;
	}

	// C++ TO JAVA CONVERTER NOTE: This static local variable declaration (not
	// allowed in Java) has been moved just prior to the method:
	static boolean is_symbol_defined_in_bb(gm_gps_basic_block b, gm_symtab_entry e) {
		java.util.HashMap<gm_symtab_entry, gps_syminfo> SYMS = b.get_symbols();
		if (!SYMS.containsKey(e))
			return false;
		else
			return true;
	}

	public void generate_message_receive_begin(ast_foreach fe, gm_code_writer Body, gm_gps_basic_block b, boolean is_only_comm) {
		gm_gps_beinfo info = (gm_gps_beinfo) GlobalMembersGm_main.FE.get_current_backend_info();
		gm_gps_comm_t comm_type = (fe == null) ? GPS_COMM_INIT : GPS_COMM_NESTED;
		gm_gps_comm_unit U = new gm_gps_comm_unit(comm_type, fe);
		generate_message_receive_begin(U, Body, b, is_only_comm);
	}

	public void generate_message_receive_begin(ast_sentblock sb, gm_symtab_entry drv, gm_code_writer Body, gm_gps_basic_block b, boolean is_only_comm) {
		gm_gps_beinfo info = (gm_gps_beinfo) GlobalMembersGm_main.FE.get_current_backend_info();
		gm_gps_comm_t comm_type = GPS_COMM_RANDOM_WRITE;
		gm_gps_comm_unit U = new gm_gps_comm_unit(comm_type, sb, drv);
		generate_message_receive_begin(U, Body, b, is_only_comm);
	}

	public void generate_message_receive_begin(gm_gps_comm_unit U, gm_code_writer Body, gm_gps_basic_block b, boolean is_only_comm) {
		gm_gps_beinfo info = (gm_gps_beinfo) GlobalMembersGm_main.FE.get_current_backend_info();

		LinkedList<gm_gps_communication_symbol_info> LIST = info.get_all_communication_symbols(U);
		// int comm_id = info->find_communication_size_info(fe).id;
		int comm_id = (info.find_communication_size_info(U)).msg_class.id;

		if (!is_only_comm && !info.is_single_message()) {
			String temp = String.format("if (_msg.m_type == %d) {", comm_id);
			Body.pushln(temp);
		}

		for (gm_gps_communication_symbol_info SYM : LIST) {
			gm_symtab_entry e = SYM.symbol;

			// check it once again later
			if (e.getType().is_property() || e.getType().is_node_compatible() || e.getType().is_edge_compatible()
					|| !is_symbol_defined_in_bb(b, e)) {
				String str = main.get_type_string(SYM.gm_type);
				Body.push(str);
				Body.SPC();
			}
			if (e.getType().is_property()) {
				generate_vertex_prop_access_remote_lhs(e.getId(), Body);
			} else {
				Body.push(e.getId().get_genname());
			}
			Body.push(" = ");
			Body.push("_msg.");
			String fname = get_message_field_var_name(SYM.gm_type, SYM.idx);
			Body.push(fname);
			fname = null;
			Body.pushln(";");
		}
	}

	public void generate_message_receive_end(gm_code_writer Body, boolean is_only_comm) {
		if (!is_only_comm) {
			Body.pushln("}");
		}
	}

	void generate_expr_nil(ast_expr e, gm_code_writer Body) {
		Body.push("(-1)");
	}

	public void generate_expr_builtin(ast_expr_builtin be, gm_code_writer Body, boolean is_master) {
		gm_builtin_def def = be.get_builtin_def();
		LinkedList<ast_expr> ARGS = be.get_args();

		switch (def.get_method_id()) {
		case GM_BLTIN_TOP_DRAND: // rand function
			Body.push("(new java.util.Random()).nextDouble()");
			break;

		case GM_BLTIN_TOP_IRAND: // rand function
			Body.push("(new java.util.Random()).nextInt(");
			get_main().generate_expr(ARGS.getFirst());
			Body.push(")");
			break;

		case GM_BLTIN_GRAPH_RAND_NODE: // random node function
			Body.push("(new java.util.Random()).nextInt(");
			Body.push("getGraphSize()");
			Body.push(")");
			break;

		case GM_BLTIN_GRAPH_NUM_NODES:
			// Body.push("/*please check*/");
			Body.push("getGraphSize()");
			break;
		case GM_BLTIN_NODE_DEGREE:
			// Body.push("/*please check*/");
			Body.push("getNeighborsSize()");
			break;
		case GM_BLTIN_NODE_IN_DEGREE:
			Body.push(STATE_SHORT_CUT);
			Body.push(".");
			Body.push(GPS_REV_NODE_ID);
			Body.push(".length");
			break;

		default:
			assert false;
			break;
		}
	}

	public void generate_prepare_bb(gm_code_writer Body, gm_gps_basic_block bb) {

		if (bb.get_type() == gm_gps_bbtype_t.GM_GPS_BBTYPE_PREPARE1) {
			Body.pushln("// Preperation: creating reverse edges");
			String temp = String.format("%s %s = getId();", main.get_type_string(GMTYPE_T.GMTYPE_NODE), GPS_DUMMY_ID);
			Body.pushln(temp);

			generate_message_send(null, Body);

		} else if (bb.get_type() == gm_gps_bbtype_t.GM_GPS_BBTYPE_PREPARE2) {
			Body.pushln("//Preperation creating reverse edges");
			Body.pushln("int i = 0; // iterable does not have length(), so we have to count it");
			Body.pushln("for(MessageData _msg : _msgs) i++;");

			String temp = String.format("%s.%s = new %s[i];", STATE_SHORT_CUT, GPS_REV_NODE_ID, main.get_type_string(GMTYPE_T.GMTYPE_NODE));
			Body.pushln(temp);
			Body.NL();

			Body.pushln("i=0;");
			Body.pushln("for(MessageData _msg : _msgs) {");
			generate_message_receive_begin((ast_foreach) null, Body, bb, true);
			temp = String.format("%s.%s[i] = %s;", STATE_SHORT_CUT, GPS_REV_NODE_ID, GPS_DUMMY_ID);
			Body.pushln(temp);
			generate_message_receive_end(Body, true);
			Body.pushln("i++;");
			Body.pushln("}");
		} else {
			assert false;
		}

	}

	// -----------------------------------------------------------------------------

	public boolean do_local_optimize() {
		String[] NAMES = { "[(nothing)]" };
		final int COUNT = NAMES.length;

		boolean is_okay = true;

		for (int i = 0; i < COUNT; i++) {
			GlobalMembersGm_main.gm_begin_minor_compiler_stage(i + 1, NAMES[i]);
			switch (i) {
			case 0:
				break;
			default:
				assert false;
				break;
			}
			GlobalMembersGm_main.gm_end_minor_compiler_stage();
			if (!is_okay)
				break;
		}
		return is_okay;
	}
}