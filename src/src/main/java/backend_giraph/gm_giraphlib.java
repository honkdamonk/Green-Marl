package backend_giraph;

import frontend.gm_symtab_entry;
import inc.GMTYPE_T;
import inc.GM_REDUCE_T;
import inc.GlobalMembersGm_backend_gps;
import inc.gm_code_writer;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ast.AST_NODE_TYPE;
import ast.ast_assign;
import ast.ast_expr;
import ast.ast_expr_builtin;
import ast.ast_foreach;
import ast.ast_id;
import ast.ast_sent;
import ast.ast_sentblock;
import ast.ast_typedecl;
import backend_gps.GlobalMembersGps_syminfo;
import backend_gps.gm_gps_basic_block;
import backend_gps.gm_gps_bbtype_t;
import backend_gps.gm_gps_beinfo;
import backend_gps.gm_gps_comm_t;
import backend_gps.gm_gps_comm_unit;
import backend_gps.gm_gps_communication_size_info;
import backend_gps.gm_gps_communication_symbol_info;
import backend_gps.gm_gps_congruent_msg_class;
import backend_gps.gm_gps_edge_access_t;
import backend_gps.gm_gps_gen;
import backend_gps.gm_gpslib;
import backend_gps.gps_syminfo;

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
//class gm_giraph_gen;

// Nothing happens in this class
public class gm_giraphlib extends gm_gpslib {
	public gm_giraphlib() {
		main = null;
	}

	public gm_giraphlib(gm_giraph_gen gen) {
		set_main(gen);
	}

	public final void set_main(gm_giraph_gen gen) {
		main = (gm_gps_gen) gen;
	}

	public final gm_giraph_gen get_main() {
		return (gm_giraph_gen) main;
	}

	// virtual void generate_prepare_bb(gm_code_writer Body, gm_gps_basic_block
	// b);

	public void generate_broadcast_reduce_initialize_master(ast_id id, gm_code_writer Body, GM_REDUCE_T reduce_op_type, String base_value) {
		String temp = new String(new char[1024]);
		Body.push("((");
		generate_broadcast_variable_type(id.getTypeSummary(), Body, reduce_op_type);
		temp = String.format(") getAggregator(%s)).setAggregatedValue(%s);", create_key_string(id), base_value);
		Body.pushln(temp);
	}

	public void generate_broadcast_state_master(String state_var, gm_code_writer Body) {
		String temp = new String(new char[1024]);
		temp = String.format("((IntOverwriteAggregator) getAggregator(%s)).setAggregatedValue(%s);", GlobalMembersGm_backend_gps.GPS_KEY_FOR_STATE, state_var);
		Body.pushln(temp);
	}

	public void generate_broadcast_isFirst_master(String is_first_var, gm_code_writer Body) {
		String temp = new String(new char[1024]);
		temp = String.format("((BooleanOverwriteAggregator) getAggregator(\"%s\")).setAggregatedValue(%s);", is_first_var, is_first_var);
		Body.pushln(temp);
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
			type_id = GMTYPE_T.GMTYPE_NODE; // TODO setting argument?
		if (type_id.is_edge_compatible_type())
			type_id = GMTYPE_T.GMTYPE_EDGE; // TODO setting argument?

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
			Body.push("And");
			break;
		case GMREDUCE_OR:
			Body.push("Or");
			break;
		default:
			assert false;
			break;
		}
		Body.push("Aggregator");
	}

	public void generate_broadcast_send_master(ast_id id, gm_code_writer Body) {
		// ---------------------------------------------------
		// create new BV
		// ---------------------------------------------------
		String temp = new String(new char[1024]);
		Body.push("((");
		generate_broadcast_variable_type(id.getTypeSummary(), Body);
		temp = String.format(") getAggregator(%s)).setAggregatedValue(", create_key_string(id));
		Body.push(temp);

		// ---------------------------------------------------
		// Initial Value: Reading of Id
		// ---------------------------------------------------
		get_main().generate_rhs_id(id);
		Body.pushln(");");
	}

	public void generate_broadcast_receive_master(ast_id id, gm_code_writer Body, GM_REDUCE_T reduce_op_type) {
		String temp = new String(new char[1024]);
		generate_broadcast_variable_type(id.getTypeSummary(), Body, reduce_op_type);
		temp = String.format(" %sAggregator = (", id.get_genname());
		Body.push(temp);
		generate_broadcast_variable_type(id.getTypeSummary(), Body, reduce_op_type);
		temp = String.format(") getAggregator(%s);", create_key_string(id));
		Body.pushln(temp);

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

		temp = String.format("%sAggregator.getAggregatedValue().get()", id.get_genname());
		Body.push(temp);
		if (need_paren)
			Body.push(")");
		Body.pushln(";");
	}

	public void generate_headers(gm_code_writer Body) {
		Body.pushln("import java.io.DataInput;");
		Body.pushln("import java.io.DataOutput;");
		Body.pushln("import java.io.IOException;");
		Body.pushln("import java.lang.Math;");
		Body.pushln("import java.util.Iterator;");
		Body.pushln("import java.util.Map;");
		Body.pushln("import java.util.Random;");
		Body.pushln("import org.apache.commons.cli.*;");
		Body.pushln("import org.apache.giraph.aggregators.*;");
		Body.pushln("import org.apache.giraph.graph.*;");
		Body.pushln("import org.apache.giraph.lib.*;");
		Body.pushln("import org.apache.hadoop.conf.Configuration;");
		Body.pushln("import org.apache.hadoop.fs.Path;");
		Body.pushln("import org.apache.hadoop.io.*;");
		Body.pushln("import org.apache.hadoop.mapreduce.InputSplit;");
		Body.pushln("import org.apache.hadoop.mapreduce.RecordReader;");
		Body.pushln("import org.apache.hadoop.mapreduce.RecordWriter;");
		Body.pushln("import org.apache.hadoop.mapreduce.TaskAttemptContext;");
		Body.pushln("import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;");
		Body.pushln("import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;");
		Body.pushln("import org.apache.hadoop.util.Tool;");
		Body.pushln("import org.apache.hadoop.util.ToolRunner;");
		Body.pushln("import org.apache.log4j.Logger;");
		Body.pushln("import com.google.common.collect.Maps;");
	}

	public void generate_reduce_assign_vertex(ast_assign a, gm_code_writer Body, GM_REDUCE_T reduce_op_type) {
		assert a.is_target_scalar();
		ast_id id = a.get_lhs_scala();

		String temp = new String(new char[1024]);
		Body.push("((");
		generate_broadcast_variable_type(id.getTypeSummary(), Body, reduce_op_type);
		temp = String.format(") getAggregator(%s)).aggregate(", create_key_string(id));
		Body.push(temp);

		// ---------------------------------------------------
		// Initial Value: Reading of Id
		// ---------------------------------------------------
		Body.push("(");
		get_main().generate_expr(a.get_rhs());
		Body.push(")");
		Body.pushln(");");
	}

	public void generate_broadcast_receive_vertex(ast_id id, gm_code_writer Body) {
		String temp = new String(new char[1024]);
		Body.push("((");
		generate_broadcast_variable_type(id.getTypeSummary(), Body);
		temp = String.format(") getAggregator(%s)).getAggregatedValue().get()", create_key_string(id));
		Body.push(temp);
	}

	public void generate_parameter_read_vertex(ast_id id, gm_code_writer Body) {
		String temp = new String(new char[1024]);
		Body.push("getConf().");
		switch (id.getTypeSummary()) {
		case GMTYPE_BOOL:
			temp = String.format("getBoolean(\"%s\", false)", id.get_genname());
			break;
		case GMTYPE_INT:
			temp = String.format("getInt(\"%s\", -1)", id.get_genname());
			break;
		case GMTYPE_LONG:
			temp = String.format("getLong(\"%s\", -1L)", id.get_genname());
			break;
		case GMTYPE_FLOAT:
			temp = String.format("getFloat(\"%s\", -1.0f)", id.get_genname());
			break;
		// TODO Waiting for https://issues.apache.org/jira/browse/HADOOP-8415 to
		// be accepted:
		// case GMTYPE_DOUBLE: sprintf(temp, "getDouble(\"%s\", -1.0)",
		// id->get_genname()); break;
		case GMTYPE_DOUBLE:
			temp = String.format("getFloat(\"%s\", -1.0f)", id.get_genname());
			break;
		case GMTYPE_NODE:
			if (is_node_type_int()) {
				temp = String.format("getInt(\"%s\", -1)", id.get_genname());
			} else {
				temp = String.format("getLong(\"%s\", -1L)", id.get_genname());
			}
			break;
		default:
			assert false;
			break;
		}
		Body.push(temp);
	}

	public void generate_master_class_details(java.util.HashSet<gm_symtab_entry> prop, gm_code_writer Body) {
		Body.pushln("@Override");
		Body.pushln("public void write(DataOutput out) throws IOException {");
		Body.pushln("out.writeInt(_master_state);");
		Body.pushln("out.writeInt(_master_state_nxt);");
		Body.pushln("out.writeBoolean(_master_should_start_workers);");
		Body.pushln("out.writeBoolean(_master_should_finish);");

		for (gm_symtab_entry sym : prop) {
			gps_syminfo syminfo = (gps_syminfo) sym.find_info(GlobalMembersGps_syminfo.GPS_TAG_BB_USAGE);
			if (!syminfo.is_used_in_master())
				continue;

			genPutIOB(sym.getId().get_genname(), sym.getType().getTypeSummary(), Body, this);
		}
		Body.pushln("}");

		Body.pushln("@Override");
		Body.pushln("public void readFields(DataInput in) throws IOException {");
		Body.pushln("_master_state = in.readInt();");
		Body.pushln("_master_state_nxt = in.readInt();");
		Body.pushln("_master_should_start_workers = in.readBoolean();");
		Body.pushln("_master_should_finish = in.readBoolean();");

		for (gm_symtab_entry sym : prop) {
			gps_syminfo syminfo = (gps_syminfo) sym.find_info(GlobalMembersGps_syminfo.GPS_TAG_BB_USAGE);
			if (!syminfo.is_used_in_master())
				continue;

			genGetIOB(sym.getId().get_genname(), sym.getType().getTypeSummary(), Body, this);
		}
		Body.pushln("}");
	}

	public void generate_vertex_prop_class_details(java.util.HashSet<gm_symtab_entry> prop, gm_code_writer Body, boolean is_edge_prop) {
		String temp = new String(new char[1024]);
		int total = is_edge_prop ? ((gm_gps_beinfo) GlobalMembersGm_main.FE.get_current_backend_info()).get_total_edge_property_size()
				: ((gm_gps_beinfo) GlobalMembersGm_main.FE.get_current_backend_info()).get_total_node_property_size();

		if (is_edge_prop) {
			Body.pushln("public EdgeData() {");
			Body.pushln("// Default constructor needed for Giraph");
			Body.pushln("}");
			Body.NL();
			Body.pushln("public EdgeData(double input) {");
		} else {
			Body.pushln("public VertexData() {");
			Body.pushln("// Default constructor needed for Giraph");
			Body.pushln("}");
			Body.NL();
			Body.pushln("public VertexData(double input) {");
		}
		Body.pushln("// Assign input data to node property if desired");
		Body.pushln("}");
		Body.NL();

		Body.pushln("@Override");
		Body.pushln("public void write(DataOutput out) throws IOException {");
		for (gm_symtab_entry sym : prop) {
			genPutIOB(sym.getId().get_genname(), sym.getType().getTargetTypeSummary(), Body, this);
		}
		if (GlobalMembersGm_main.FE.get_current_proc_info().find_info_bool(GlobalMembersGm_backend_gps.GPS_FLAG_USE_REVERSE_EDGE)) {
			temp = String.format("out.writeInt(%s.length);", GlobalMembersGm_backend_gps.GPS_REV_NODE_ID);
			Body.pushln(temp);
			String.format(temp, "for (%s node : %s) {", GlobalMembersGm_main.PREGEL_BE.get_lib().is_node_type_int() ? "IntWritable" : "LongWritable",
					GlobalMembersGm_backend_gps.GPS_REV_NODE_ID);
			Body.pushln(temp);
			Body.pushln("node.write(out);");
			Body.pushln("}");
		}
		Body.pushln("}");

		Body.pushln("@Override");
		Body.pushln("public void readFields(DataInput in) throws IOException {");
		for (gm_symtab_entry sym : prop) {
			genGetIOB(sym.getId().get_genname(), sym.getType().getTargetTypeSummary(), Body, this);
		}
		if (GlobalMembersGm_main.FE.get_current_proc_info().find_info_bool(GlobalMembersGm_backend_gps.GPS_FLAG_USE_REVERSE_EDGE)) {
			Body.pushln("int _node_count = in.readInt();");
			temp = String.format("%s = new %s[_node_count];", GlobalMembersGm_backend_gps.GPS_REV_NODE_ID, GlobalMembersGm_main.PREGEL_BE.get_lib()
					.is_node_type_int() ? "IntWritable" : "LongWritable");
			Body.pushln(temp);
			Body.pushln("for (int i = 0; i < _node_count; i++) {");
			temp = String.format("%s[i].readFields(in);", GlobalMembersGm_backend_gps.GPS_REV_NODE_ID);
			Body.pushln(temp);
			Body.pushln("}");
		}
		Body.pushln("}");

		Body.pushln("@Override");
		Body.pushln("public String toString() {");
		Body.pushln("// Implement output fields here for VertexOutputWriter");
		Body.pushln("return \"1.0\";");
		Body.pushln("}");

	}

	public void generate_receive_state_vertex(String state_var, gm_code_writer Body) {
		String temp = new String(new char[1024]);
		temp = String.format("int %s = ((IntOverwriteAggregator) getAggregator(%s)).getAggregatedValue().get();", state_var,
				GlobalMembersGm_backend_gps.GPS_KEY_FOR_STATE);
		Body.pushln(temp);
	}

	public void generate_receive_isFirst_vertex(String is_first_var, gm_code_writer Body) {
		String temp = new String(new char[1024]);
		temp = String.format("boolean %s = ((BooleanOverwriteAggregator) getAggregator(\"%s\"", is_first_var, is_first_var);
		Body.push(temp);
		Body.pushln(")).getAggregatedValue().get();");
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
		temp = String.format("%s.%s", GlobalMembersGm_backend_gps.STATE_SHORT_CUT, id.get_genname());
		Body.push(temp);
	}

	public void generate_vertex_prop_access_lhs_edge(ast_id id, gm_code_writer Body) {
		String temp = new String(new char[1024]);
		temp = String.format("_outEdgeData.%s", id.get_genname());
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
		temp = String.format("VertexData %s = getVertexValue();", GlobalMembersGm_backend_gps.STATE_SHORT_CUT);
		Body.pushln(temp);
	}

	public void generate_node_iterator_rhs(ast_id id, gm_code_writer Body) {
		// TODO
		Body.push("getVertexId().get()");
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

		String temp = new String(new char[1024]);
		String str = main.get_type_string(gm_type);
		temp = String.format("%c%d", str.charAt(0), index);
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
	public void generate_message_send_for_random_write(ast_sentblock sb, gm_symtab_entry sym, gm_code_writer Body) {
		String temp = new String(new char[1024]);

		temp = String.format("sendMsg(new %s(", GlobalMembersGm_main.PREGEL_BE.get_lib().is_node_type_int() ? "IntWritable" : "LongWritable");
		Body.push(temp);
		get_main().generate_rhs_id(sym.getId());
		temp = String.format("), %s);", get_random_write_message_name(sym));
		Body.pushln(temp);
	}

	// virtual void generate_expr_builtin(ast_expr_builtin e, gm_code_writer
	// Body, boolean is_master);

	/* TODO: added methods from GlobalMembersGm_giraph_lib.java, clean up */

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

	public static void genPutIOB(String name, GMTYPE_T gm_type, gm_code_writer Body, gm_giraphlib lib) {
		if (gm_type.is_node_compatible_type())
			gm_type = GMTYPE_T.GMTYPE_NODE; // TODO setting input var?
		if (gm_type.is_edge_compatible_type())
			gm_type = GMTYPE_T.GMTYPE_EDGE; // TODO setting input var?

		Body.push("out.");
		switch (gm_type) {
		case GMTYPE_INT:
			Body.push("writeInt");
			break;
		case GMTYPE_LONG:
			Body.push("writeLong");
			break;
		case GMTYPE_FLOAT:
			Body.push("writeFloat");
			break;
		case GMTYPE_DOUBLE:
			Body.push("writeDouble");
			break;
		case GMTYPE_BOOL:
			Body.push("writeBoolean");
			break;
		case GMTYPE_NODE:
			if (lib.is_node_type_int()) {
				Body.push("writeInt");
				break;
			} else {
				Body.push("writeLong");
				break;
			}
		case GMTYPE_EDGE:
			if (lib.is_edge_type_int()) {
				Body.push("writeInt");
				break;
			} else {
				Body.push("writeLong");
				break;
			}
		default:
			assert false;
			break;
		}
		Body.push("(");
		Body.push(name);
		Body.pushln(");");
	}

	public static void genGetIOB(String name, GMTYPE_T gm_type, gm_code_writer Body, gm_giraphlib lib) {
		if (gm_type.is_node_compatible_type())
			gm_type = GMTYPE_T.GMTYPE_NODE;
		if (gm_type.is_edge_compatible_type())
			gm_type = GMTYPE_T.GMTYPE_EDGE;

		Body.push(name);
		Body.push(" = in.");
		switch (gm_type) {
		case GMTYPE_INT:
			Body.push("readInt()");
			break;
		case GMTYPE_LONG:
			Body.push("readLong()");
			break;
		case GMTYPE_FLOAT:
			Body.push("readFloat()");
			break;
		case GMTYPE_DOUBLE:
			Body.push("readDouble()");
			break;
		case GMTYPE_BOOL:
			Body.push("readBoolean()");
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
	// /#define MESSAGE_PER_TYPE_LOOP_END() }
	public static void generate_message_write_each(gm_giraphlib lib, int cnt, GMTYPE_T gm_type, gm_code_writer Body) {
		for (int i = 0; i < cnt; i++) {
			String vname = lib.get_message_field_var_name(gm_type, i);
			genPutIOB(vname, gm_type, Body, lib);
			vname = null;
		}
	}

	public static void generate_message_read1_each(gm_giraphlib lib, int cnt, GMTYPE_T gm_type, gm_code_writer Body) {
		for (int i = 0; i < cnt; i++) {
			String vname = lib.get_message_field_var_name(gm_type, i);
			genGetIOB(vname, gm_type, Body, lib);
			vname = null;
		}
	}

	private static void generate_message_class_write_generate_message_class_read1(gm_giraphlib lib, gm_gps_beinfo info, gm_code_writer Body) {
		Body.pushln("@Override");
		Body.pushln("public void readFields(DataInput in) throws IOException {");
		if (!info.is_single_message())
			Body.pushln("m_type = in.readByte();");
		String str_buf;
		List<gm_gps_congruent_msg_class> LOOPS = info.get_congruent_message_classes();
		boolean is_single = info.is_single_message();
		boolean is_first = true;
		for (gm_gps_congruent_msg_class element : LOOPS) {
			gm_gps_communication_size_info SYMS = element.sz_info;
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
				Body.pushln("in.readByte(); // consume empty message byte");
			generate_message_read1_each(lib, SYMS.num_int, GMTYPE_T.GMTYPE_INT, Body);
			generate_message_read1_each(lib, SYMS.num_long, GMTYPE_T.GMTYPE_LONG, Body);
			generate_message_read1_each(lib, SYMS.num_float, GMTYPE_T.GMTYPE_FLOAT, Body);
			generate_message_read1_each(lib, SYMS.num_double, GMTYPE_T.GMTYPE_DOUBLE, Body);
			generate_message_read1_each(lib, SYMS.num_bool, GMTYPE_T.GMTYPE_BOOL, Body);
			if (!info.is_single_message())
				Body.pushln("}");
		}
		Body.pushln("}");
	}

	private static boolean generate_message_class_write_is_symbol_defined_in_bb(gm_gps_basic_block b, gm_symtab_entry e) {
		Map<gm_symtab_entry, gps_syminfo> SYMS = b.get_symbols();
		if (!SYMS.containsKey(e))
			return false;
		else
			return true;
	}

	public static void generate_message_class_write(gm_giraphlib lib, gm_gps_beinfo info, gm_code_writer Body)
    {
        Body.pushln("@Override");
        Body.pushln("public void write(DataOutput out) throws IOException {");
        if (!info.is_single_message())
            Body.pushln("out.writeByte(m_type);");
        String str_buf = new String(new char[1024]);
        java.util.LinkedList<gm_gps_congruent_msg_class> LOOPS = info.get_congruent_message_classes();
        boolean is_single = info.is_single_message();
        boolean is_first = true;
        for (gm_gps_congruent_msg_class c : LOOPS) {
            gm_gps_communication_size_info SYMS = c.sz_info;
            int sz = get_total_size(SYMS);
            if (!is_single && is_first)
            {
                is_first = false;
                str_buf = String.format("if (m_type == %d) ", SYMS.id);
                Body.push(str_buf);
            }
            else if (!is_single)
            {
                str_buf = String.format("else if (m_type == %d) ", SYMS.id);
                Body.push(str_buf);
            }
            if(!info.is_single_message())
                Body.pushln("{");
            if (info.is_single_message() && get_total_size(SYMS) == 0)
                Body.pushln("out.writeByte((byte)0); // empty message");
            generate_message_write_each(lib, SYMS.num_int, GMTYPE_T.GMTYPE_INT, Body);
            generate_message_write_each(lib, SYMS.num_long, GMTYPE_T.GMTYPE_LONG, Body);
            generate_message_write_each(lib, SYMS.num_float, GMTYPE_T.GMTYPE_FLOAT, Body);
            generate_message_write_each(lib, SYMS.num_double, GMTYPE_T.GMTYPE_DOUBLE, Body);
            generate_message_write_each(lib, SYMS.num_bool, GMTYPE_T.GMTYPE_BOOL, Body);
            if (!info.is_single_message())
                Body.pushln("}");
        }
        Body.pushln("}");
    }

	// C++ TO JAVA CONVERTER NOTE: This static local variable declaration (not
	// allowed in Java) has been moved just prior to the method:
	static void generate_message_class_read1(gm_giraphlib lib, gm_gps_beinfo info, gm_code_writer Body)
    {
        Body.pushln("@Override");
        Body.pushln("public void readFields(DataInput in) throws IOException {");
        if (!info.is_single_message())
            Body.pushln("m_type = in.readByte();");
        String str_buf = new String(new char[1024]);
        java.util.LinkedList<gm_gps_congruent_msg_class> LOOPS = info.get_congruent_message_classes();
        boolean is_single = info.is_single_message();
        boolean is_first = true;
        for(gm_gps_congruent_msg_class c : LOOPS)
        {
            gm_gps_communication_size_info SYMS = c.sz_info;
            int sz = get_total_size(SYMS);
            if (!is_single && is_first)
            {
                is_first = false;
                str_buf = String.format("if (m_type == %d) ", SYMS.id);
                Body.push(str_buf);
            }
            else if (!is_single)
            {
                str_buf = String.format("else if (m_type == %d) ", SYMS.id);
                Body.push(str_buf);
            }
            if(!info.is_single_message())
                Body.pushln("{");
            if (info.is_single_message() && get_total_size(SYMS) == 0)
                Body.pushln("in.readByte(); // consume empty message byte");
            generate_message_read1_each(lib, SYMS.num_int, GMTYPE_T.GMTYPE_INT, Body);
            generate_message_read1_each(lib, SYMS.num_long, GMTYPE_T.GMTYPE_LONG, Body);
            generate_message_read1_each(lib, SYMS.num_float, GMTYPE_T.GMTYPE_FLOAT, Body);
            generate_message_read1_each(lib, SYMS.num_double, GMTYPE_T.GMTYPE_DOUBLE, Body);
            generate_message_read1_each(lib, SYMS.num_bool, GMTYPE_T.GMTYPE_BOOL, Body);
            if (!info.is_single_message())
                Body.pushln("}");
        }
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

		generate_message_class_write(this, info, Body);
		generate_message_class_write_generate_message_class_read1(this, info, Body);
	}

	public void generate_message_send(ast_foreach fe, gm_code_writer Body) {
		String temp = new String(new char[1024]);

		gm_gps_beinfo info = (gm_gps_beinfo) GlobalMembersGm_main.FE.get_current_backend_info();

		gm_gps_comm_t m_type = (fe == null) ? gm_gps_comm_t.GPS_COMM_INIT : gm_gps_comm_t.GPS_COMM_NESTED;

		gm_gps_comm_unit U = new gm_gps_comm_unit(m_type, fe);

		java.util.LinkedList<gm_gps_communication_symbol_info> LIST = info.get_all_communication_symbols(U);

		gm_gps_communication_size_info SINFO = info.find_communication_size_info(U);

		boolean need_separate_message = (fe == null) ? false : fe.find_info_bool(GlobalMembersGm_backend_gps.GPS_FLAG_EDGE_DEFINING_INNER);
		boolean is_in_neighbors = (fe != null) && (fe.get_iter_type() == GMTYPE_T.GMTYPE_NODEITER_IN_NBRS);

		if (!need_separate_message) {
			Body.pushln("// Sending messages to all neighbors (if there is a neighbor)");
			if (is_in_neighbors) {
				temp = String.format("if (%s.%s.length > 0) {", GlobalMembersGm_backend_gps.STATE_SHORT_CUT, GlobalMembersGm_backend_gps.GPS_REV_NODE_ID); // TODO
				Body.pushln(temp);
			} else {
				Body.pushln("if (getNumOutEdges() > 0) {");
			}
		} else {
			assert (fe != null) && (fe.get_iter_type() == GMTYPE_T.GMTYPE_NODEITER_NBRS);
			Body.pushln("// Sending messages to each neighbor");
			temp = String.format("Iterator<%s> neighbors = this.getOutEdgesIterator();",
					GlobalMembersGm_main.PREGEL_BE.get_lib().is_node_type_int() ? "IntWritable" : "LongWritable");
			Body.pushln(temp);
			Body.pushln("while (neighbors.hasNext()) {");
			temp = String.format("%s _neighborId = neighbors.next();", GlobalMembersGm_main.PREGEL_BE.get_lib().is_node_type_int() ? "IntWritable"
					: "LongWritable");
			Body.pushln(temp);
			Body.pushln("EdgeData _outEdgeData = this.getEdgeValue(_neighborId);");
		}

		// check if any edge updates that should be done before message sending
		java.util.LinkedList<ast_sent> sents_after_message = new java.util.LinkedList<ast_sent>();

		if ((fe != null) && (fe.has_info_list(GlobalMembersGm_backend_gps.GPS_LIST_EDGE_PROP_WRITE))) {
			java.util.LinkedList<Object> L = fe.get_info_list(GlobalMembersGm_backend_gps.GPS_LIST_EDGE_PROP_WRITE);

			for (Object obj : L) {
				ast_sent s = (ast_sent) obj;
				assert s.get_nodetype() == AST_NODE_TYPE.AST_ASSIGN;
				ast_assign a = (ast_assign) s;
				assert !a.is_target_scalar();
				gm_symtab_entry e = a.get_lhs_field().get_second().getSymInfo();
				// C++ TO JAVA CONVERTER TODO TASK: Java does not have an
				// equivalent for pointers to value types:
				// ORIGINAL LINE: int* i = (int*)
				// fe->find_info_map_value(GPS_MAP_EDGE_PROP_ACCESS, e);
				Integer i = (Integer) fe.find_info_map_value(GlobalMembersGm_backend_gps.GPS_MAP_EDGE_PROP_ACCESS, e);
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
				if (s.find_info_bool(GlobalMembersGm_backend_gps.GPS_FLAG_COMM_DEF_ASSIGN)) {
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
				String.format(temp, "for (%s node : %s.%s) {", GlobalMembersGm_main.PREGEL_BE.get_lib().is_node_type_int() ? "IntWritable" : "LongWritable",
						GlobalMembersGm_backend_gps.STATE_SHORT_CUT, GlobalMembersGm_backend_gps.GPS_REV_NODE_ID);
				Body.pushln(temp);
				Body.pushln("sendMsg(node, _msg);");
				Body.pushln("}");
			} else {
				Body.pushln("sendMsgToAllEdges(_msg);");
			}
			Body.pushln("}");
		} else {
			Body.pushln("sendMsg(_neighborId, _msg);");
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
		gm_gps_comm_t comm_type = (fe == null) ? gm_gps_comm_t.GPS_COMM_INIT : gm_gps_comm_t.GPS_COMM_NESTED;
		gm_gps_comm_unit U = new gm_gps_comm_unit(comm_type, fe);
		generate_message_receive_begin(U, Body, b, is_only_comm);
	}

	public void generate_message_receive_begin(ast_sentblock sb, gm_symtab_entry drv, gm_code_writer Body, gm_gps_basic_block b, boolean is_only_comm) {
		gm_gps_beinfo info = (gm_gps_beinfo) GlobalMembersGm_main.FE.get_current_backend_info();
		gm_gps_comm_t comm_type = gm_gps_comm_t.GPS_COMM_RANDOM_WRITE;
		gm_gps_comm_unit U = new gm_gps_comm_unit(comm_type, sb, drv);
		generate_message_receive_begin(U, Body, b, is_only_comm);
	}

	public void generate_message_receive_begin(gm_gps_comm_unit U, gm_code_writer Body, gm_gps_basic_block b, boolean is_only_comm) {
		gm_gps_beinfo info = (gm_gps_beinfo) GlobalMembersGm_main.FE.get_current_backend_info();

		java.util.LinkedList<gm_gps_communication_symbol_info> LIST = info.get_all_communication_symbols(U);
		// int comm_id = info->find_communication_size_info(fe).id;
		int comm_id = (info.find_communication_size_info(U)).msg_class.id;

		String temp = new String(new char[1024]);
		if (!is_only_comm && !info.is_single_message()) {
			temp = String.format("if (_msg.m_type == %d) {", comm_id);
			Body.pushln(temp);
		}

		for (gm_gps_communication_symbol_info SYM : LIST) {
			gm_symtab_entry e = SYM.symbol;

			// check it once again later
			if (e.getType().is_property() || e.getType().is_node_compatible() || e.getType().is_edge_compatible()
					|| !generate_message_class_write_is_symbol_defined_in_bb(b, e)) {
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

	public void generate_expr_builtin(ast_expr_builtin be, gm_code_writer Body, boolean is_master) {
		gm_builtin_def def = be.get_builtin_def();
		java.util.LinkedList<ast_expr> ARGS = be.get_args();

		switch (def.get_method_id()) {
		case GM_BLTIN_TOP_DRAND: // rand function
			Body.push("(new Random()).nextDouble()");
			break;

		case GM_BLTIN_TOP_IRAND: // rand function
			Body.push("(new Random()).nextInt(");
			get_main().generate_expr(ARGS.getFirst());
			Body.push(")");
			break;

		case GM_BLTIN_GRAPH_NUM_NODES:
			Body.push("getNumVertices()");
			break;
		case GM_BLTIN_NODE_DEGREE:
			Body.push("getNumOutEdges()");
			break;
		case GM_BLTIN_NODE_IN_DEGREE:
			Body.push(GlobalMembersGm_backend_gps.STATE_SHORT_CUT);
			Body.push(".");
			Body.push(GlobalMembersGm_backend_gps.GPS_REV_NODE_ID);
			Body.push(".length");
			break;

		default:
			assert false;
			break;
		}
	}

	public void generate_prepare_bb(gm_code_writer Body, gm_gps_basic_block bb) {
		String temp = new String(new char[1024]);

		if (bb.get_type() == gm_gps_bbtype_t.GM_GPS_BBTYPE_PREPARE1) {
			Body.pushln("// Preperation: creating reverse edges");
			temp = String.format("%s %s = getVertexId().get();", main.get_type_string(GMTYPE_T.GMTYPE_NODE), GlobalMembersGm_backend_gps.GPS_DUMMY_ID);
			Body.pushln(temp);

			generate_message_send(null, Body);

		} else if (bb.get_type() == gm_gps_bbtype_t.GM_GPS_BBTYPE_PREPARE2) {
			Body.pushln("//Preperation creating reverse edges");
			Body.pushln("int i = 0; // iterable does not have length(), so we have to count it");
			Body.pushln("while (_msgs.hasNext()) {");
			Body.pushln("_msgs.next();");
			Body.pushln("i++;");
			Body.pushln("}");

			temp = String.format("%s.%s = new %s[i];", GlobalMembersGm_backend_gps.STATE_SHORT_CUT, GlobalMembersGm_backend_gps.GPS_REV_NODE_ID,
					GlobalMembersGm_main.PREGEL_BE.get_lib().is_node_type_int() ? "IntWritable" : "LongWritable");
			Body.pushln(temp);
			Body.NL();

			Body.pushln("i=0;");
			Body.pushln("MessageData _msg;");
			Body.pushln("while (_msgs.hasNext()) {");
			Body.pushln("_msg = _msgs.next();");
			//TODO hope this is correct!
			this.generate_message_receive_begin((ast_foreach) null, Body, bb, true);
			temp = String.format("%s.%s[i] = new %s(%s);", GlobalMembersGm_backend_gps.STATE_SHORT_CUT, GlobalMembersGm_backend_gps.GPS_REV_NODE_ID,
					GlobalMembersGm_main.PREGEL_BE.get_lib().is_node_type_int() ? "IntWritable" : "LongWritable", GlobalMembersGm_backend_gps.GPS_DUMMY_ID);
			Body.pushln(temp);
			generate_message_receive_end(Body, true);
			Body.pushln("i++;");
			Body.pushln("}");
		} else {
			assert false;
		}

	}
}