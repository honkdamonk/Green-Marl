package backend_gps;

import inc.GMTYPE_T;
import inc.GM_PROP_USAGE_T;
import inc.GM_REDUCE_T;
import inc.GlobalMembersGm_backend_gps;
import inc.GlobalMembersGm_defs;
import inc.gm_code_writer;
import inc.gm_graph_library;
import ast.ast_assign;
import ast.ast_id;
import ast.ast_procdef;
import ast.ast_sentblock;
import ast.ast_typedecl;
import backend_giraph.GlobalMembersGm_giraph_lib;

import common.GlobalMembersGm_main;
import common.GlobalMembersGm_misc;

import frontend.GlobalMembersGm_frontend;
import frontend.gm_symtab_entry;

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

	public final gm_gps_gen get_main() {
		return main;
	}

	public String get_header_info() {
		return "";
	}

	// virtual boolean do_local_optimize();

	// note: consume the return string immedately
	public String create_key_string(ast_id id) {
		String.format(str_buf, "KEY_%s", id.get_genname());
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
		Body.push(GlobalMembersGm_backend_gps.GPS_KEY_FOR_STATE);
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
		String.format(temp, "%d;}", total);
		Body.pushln(temp);

		java.util.Iterator<gm_symtab_entry> I;

		Body.pushln("@Override");
		Body.pushln("public void write(IoBuffer IOB) {");
		for (I = prop.iterator(); I.hasNext();) {
			gm_symtab_entry sym = I.next();
			GlobalMembersGm_giraph_lib.genPutIOB(sym.getId().get_genname(), sym.getType().getTargetTypeSummary(), Body, this);
		}
		Body.pushln("}");

		Body.pushln("@Override");
		Body.pushln("public void read(IoBuffer IOB) {");
		for (I = prop.iterator(); I.hasNext();) {
			gm_symtab_entry sym = I.next();
			GlobalMembersGm_giraph_lib.genGetIOB(sym.getId().get_genname(), sym.getType().getTargetTypeSummary(), Body, this);
		}
		Body.pushln("}");

		Body.pushln("@Override");
		Body.pushln("public int read(byte[] _BA, int _idx) {");
		for (I = prop.iterator(); I.hasNext();) {
			gm_symtab_entry sym = I.next();
			gps_syminfo syminfo = (gps_syminfo) sym.find_info(GlobalMembersGps_syminfo.GPS_TAG_BB_USAGE);
			int base = syminfo.get_start_byte();
			GlobalMembersGm_gps_lib.genReadByte(sym.getId().get_genname(), sym.getType().getTargetTypeSummary(), base, Body, this);
		}
		String.format(temp, "return %d;", total);
		Body.pushln(temp);
		Body.pushln("}");

		Body.pushln("@Override");
		Body.pushln("public int read(IoBuffer IOB, byte[] _BA, int _idx) {");
		String.format(temp, "IOB.get(_BA, _idx, %d);", total);
		Body.pushln(temp);
		String.format(temp, "return %d;", total);
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
		for (I = prop.iterator(); I.hasNext();) {
			gm_symtab_entry sym = I.next();
			if (sym.find_info(GlobalMembersGm_frontend.GMUSAGE_PROPERTY) == null) // this
																					// property
																					// is
																					// set
																					// to
																					// procedure
																					// argument
																					// only
			{
				// printf("no argument property :%s\n",
				// sym->getId()->get_genname());
				continue;
			}
			if (sym.find_info_int(GlobalMembersGm_frontend.GMUSAGE_PROPERTY) == GM_PROP_USAGE_T.GMUSAGE_IN) // Used
																											// as
																											// input
																											// only
			{
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
			String.format(temp, "%s: \" + %s", sym.getId().get_genname(), sym.getId().get_genname());
			Body.push(temp);
		}
		Body.pushln(";");
		Body.pushln("}");

		// Edge Property is read-only
		// if (is_edge_prop && prop.size() > 0) {
		ast_procdef proc = GlobalMembersGm_main.FE.get_current_proc();
		if ((is_edge_prop && prop.size() > 0) || (!is_edge_prop && proc.find_info_bool(GlobalMembersGm_backend_gps.GPS_FLAG_NODE_VALUE_INIT))) {
			Body.pushln("//Input Data Parsing");
			Body.pushln("@Override");
			Body.pushln("public void read(String inputString) {");
			int total_count = 0;
			if (is_edge_prop)
				total_count = prop.size();
			else {
				for (I = prop.iterator(); I.hasNext();) {
					gm_symtab_entry e = I.next();
					if ((e.find_info_int(GlobalMembersGm_frontend.GMUSAGE_PROPERTY) == GM_PROP_USAGE_T.GMUSAGE_IN)
							|| (e.find_info_int(GlobalMembersGm_frontend.GMUSAGE_PROPERTY) == GM_PROP_USAGE_T.GMUSAGE_INOUT))
						total_count++;
				}
			}

			if (total_count == 1) {
				for (I = prop.iterator(); I.hasNext();) {
					gm_symtab_entry sym = I.next();
					if (!is_edge_prop && (sym.find_info_int(GlobalMembersGm_frontend.GMUSAGE_PROPERTY) != GM_PROP_USAGE_T.GMUSAGE_IN)
							&& (sym.find_info_int(GlobalMembersGm_frontend.GMUSAGE_PROPERTY) != GM_PROP_USAGE_T.GMUSAGE_INOUT))
						continue;
					String name1;
					String name2;
					GlobalMembersGm_gps_lib.get_java_parse_string(this, sym.getType().getTargetTypeSummary(), name1, name2);
					String.format(temp, "this.%s = %s.%s(inputString);", sym.getId().get_genname(), name1, name2);
					Body.pushln(temp);
				}
			} else {
				Body.pushln("String[] split = inputString.split(\"###\");");
				boolean firstProperty = true;
				int cnt = 0;
				for (I = prop.iterator(); I.hasNext();) {
					gm_symtab_entry sym = I.next();
					String name1;
					String name2;
					if (!is_edge_prop && (sym.find_info_int(GlobalMembersGm_frontend.GMUSAGE_PROPERTY) != GM_PROP_USAGE_T.GMUSAGE_IN)
							&& (sym.find_info_int(GlobalMembersGm_frontend.GMUSAGE_PROPERTY) != GM_PROP_USAGE_T.GMUSAGE_INOUT))
						continue;

					GlobalMembersGm_gps_lib.get_java_parse_string(this, sym.getType().getTargetTypeSummary(), name1, name2);
					String.format(temp, "this.%s = %s.%s((split[%d]==null)?\"0\":split[%d]);", sym.getId().get_genname(), name1, name2, cnt, cnt);
					Body.pushln(temp);
					cnt++;
				}
			}
			Body.pushln("}");
		}
	}

	public void generate_receive_state_vertex(String state_var, gm_code_writer Body) {
		String temp = new String(new char[1024]);
		String.format(temp, "int %s = ((IntOverwriteGlobalObject) getGlobalObjectsMap().getGlobalObject(", state_var);
		Body.push(temp);
		Body.push(GlobalMembersGm_backend_gps.GPS_KEY_FOR_STATE);
		Body.pushln(")).getValue().getValue();");
	}

	public void generate_receive_isFirst_vertex(String is_first_var, gm_code_writer Body) {
		String temp = new String(new char[1024]);
		String.format(temp, "boolean %s = ((BooleanOverwriteGlobalObject) getGlobalObjectsMap().getGlobalObject(\"%s\"", is_first_var, is_first_var);
		Body.push(temp);
		Body.pushln(")).getValue().getValue();");
	}

	public void generate_message_fields_define(int gm_type, int count, gm_code_writer Body) {
		for (int i = 0; i < count; i++) {
			String str = main.get_type_string(gm_type);
			String vname = get_message_field_var_name(gm_type, i);
			String.format(str_buf, "%s %s;", str, vname);
			Body.pushln(str_buf);
			vname = null;
		}
	}

	// virtual void generate_message_class_details(gm_gps_beinfo info,
	// gm_code_writer Body);

	public void generate_vertex_prop_access_lhs(ast_id id, gm_code_writer Body) {
		String temp = new String(new char[1024]);
		String.format(temp, "%s.%s", GlobalMembersGm_backend_gps.STATE_SHORT_CUT, id.get_genname());
		Body.push(temp);
	}

	public void generate_vertex_prop_access_lhs_edge(ast_id id, gm_code_writer Body) {
		String temp = new String(new char[1024]);
		String.format(temp, "_outEdge.getEdgeValue().%s", id.get_genname());
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
		String.format(temp, "_remote_%s", id.get_genname());
		Body.push(temp);
	}

	public void generate_vertex_prop_access_remote_lhs_edge(ast_id id, gm_code_writer Body) {
		String temp = new String(new char[1024]);
		String.format(temp, "_remote_%s", id.get_genname());
		Body.push(temp);
	}

	public void generate_vertex_prop_access_remote_rhs(ast_id id, gm_code_writer Body) {
		generate_vertex_prop_access_remote_lhs(id, Body);
	}

	public void generate_vertex_prop_access_prepare(gm_code_writer Body) {
		String temp = new String(new char[1024]);
		String.format(temp, "VertexData %s = getValue();", GlobalMembersGm_backend_gps.STATE_SHORT_CUT);
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

		return GlobalMembersGm_giraph_lib.get_java_type_size(gm_type);
	}

	// caller should delete var_name later
	public String get_message_field_var_name(int gm_type, int index) {

		String temp = new String(new char[1024]);
		String str = main.get_type_string(gm_type);
		String.format(temp, "%c%d", str.charAt(0), index);
		return GlobalMembersGm_misc.gm_strdup(temp);
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
		int m_type = gm_gps_comm_t.GPS_COMM_RANDOM_WRITE.getValue();

		gm_gps_comm_unit U = new gm_gps_comm_unit(m_type, sb, sym);

		gm_gps_communication_size_info SINFO = info.find_communication_size_info(U);

		Body.NL();
		Body.push("MessageData ");
		Body.push(get_random_write_message_name(sym));
		Body.push(" = new MessageData(");
		// todo: should this always be a byte?
		String.format(str_buf, "(byte) %d);", SINFO.msg_class.id);
		Body.pushln(str_buf);
	}

	public void generate_message_payload_packing_for_random_write(ast_assign a, gm_code_writer Body) {
		assert !a.is_argminmax_assign();
		assert !a.is_target_scalar();

		ast_sentblock sb = (ast_sentblock) a.find_info_ptr(GlobalMembersGm_backend_gps.GPS_FLAG_SENT_BLOCK_FOR_RANDOM_WRITE_ASSIGN);
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
		String.format(str_buf, "_msg_%s", sym.getId().get_genname());
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
}