package backend_giraph;

import inc.GMTYPE_T;
import inc.GM_REDUCE_T;
import inc.GlobalMembersGm_backend_gps;
import inc.GlobalMembersGm_defs;
import inc.gm_code_writer;
import ast.ast_assign;
import ast.ast_id;
import ast.ast_sentblock;
import ast.ast_typedecl;
import backend_cpp.*;
import backend_giraph.*;
import backend_gps.gm_gps_gen;
import backend_gps.gm_gpslib;
import backend_gps.gps_syminfo;
import common.*;
import frontend.*;
import opt.*;
import tangible.*;

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
public class gm_giraphlib extends gm_gpslib
{
	public gm_giraphlib()
	{
		main = null;
	}
	public gm_giraphlib(gm_giraph_gen gen)
	{
		set_main(gen);
	}
	public final void set_main(gm_giraph_gen gen)
	{
		main = (gm_gps_gen) gen;
	}
	public final gm_giraph_gen get_main()
	{
		return (gm_giraph_gen) main;
	}

//	virtual void generate_prepare_bb(gm_code_writer Body, gm_gps_basic_block b);

	public void generate_broadcast_reduce_initialize_master(ast_id id, gm_code_writer Body, GM_REDUCE_T reduce_op_type, String base_value)
	{
		String temp = new String(new char[1024]);
		Body.push("((");
		generate_broadcast_variable_type(id.getTypeSummary(), Body, reduce_op_type);
		String.format(temp, ") getAggregator(%s)).setAggregatedValue(%s);", create_key_string(id), base_value);
		Body.pushln(temp);
	}
	public void generate_broadcast_state_master(String state_var, gm_code_writer Body)
	{
		String temp = new String(new char[1024]);
		String.format(temp, "((IntOverwriteAggregator) getAggregator(%s)).setAggregatedValue(%s);", GlobalMembersGm_backend_gps.GPS_KEY_FOR_STATE, state_var);
		Body.pushln(temp);
	}
	public void generate_broadcast_isFirst_master(String is_first_var, gm_code_writer Body)
	{
		String temp = new String(new char[1024]);
		String.format(temp, "((BooleanOverwriteAggregator) getAggregator(\"%s\")).setAggregatedValue(%s);", is_first_var, is_first_var);
		Body.pushln(temp);
	}
	public void generate_broadcast_variable_type(GMTYPE_T type_id, gm_code_writer Body, GM_REDUCE_T reduce_op)
    
	{
		//--------------------------------------
		// Generate following string
		//   <Type><Reduce>BV( value )
		//--------------------------------------
		// Generate following string
    
		//---------------------------------------------------
		// Type:  Long, Int, Double, Float, Bool, NODE,EDGE
		//---------------------------------------------------
		if (GlobalMembersGm_defs.gm_is_node_compatible_type(type_id))
			type_id = GMTYPE_T.GMTYPE_NODE; //TODO setting argument?
		if (GlobalMembersGm_defs.gm_is_edge_compatible_type(type_id))
			type_id = GMTYPE_T.GMTYPE_EDGE; //TODO setting argument?
    
		switch (type_id)
		{
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
    
		//---------------------------------------------------
		// Reduce Op: Min, Max, Plus, Mult, And, Or, Any
		//---------------------------------------------------
		switch (reduce_op)
		{
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
	public void generate_broadcast_send_master(ast_id id, gm_code_writer Body)
	{
		//---------------------------------------------------
		// create new BV
		//---------------------------------------------------
		String temp = new String(new char[1024]);
		Body.push("((");
		generate_broadcast_variable_type(id.getTypeSummary(), Body);
		String.format(temp, ") getAggregator(%s)).setAggregatedValue(", create_key_string(id));
		Body.push(temp);
    
		//---------------------------------------------------
		// Initial Value: Reading of Id
		//---------------------------------------------------
		get_main().generate_rhs_id(id);
		Body.pushln(");");
	}
	public void generate_broadcast_receive_master(ast_id id, gm_code_writer Body, GM_REDUCE_T reduce_op_type)
	{
		String temp = new String(new char[1024]);
		generate_broadcast_variable_type(id.getTypeSummary(), Body, reduce_op_type);
		String.format(temp, " %sAggregator = (", id.get_genname());
		Body.push(temp);
		generate_broadcast_variable_type(id.getTypeSummary(), Body, reduce_op_type);
		String.format(temp, ") getAggregator(%s);", create_key_string(id));
		Body.pushln(temp);
    
		// Read from BV to local value
		get_main().generate_lhs_id(id);
		Body.push(" = ");
		boolean need_paren = false;
    
		if (reduce_op_type != GM_REDUCE_T.GMREDUCE_NULL)
		{
			if (reduce_op_type == GM_REDUCE_T.GMREDUCE_MIN)
			{
				need_paren = true;
				Body.push("Math.min(");
				get_main().generate_rhs_id(id);
				Body.push(",");
			}
			else if (reduce_op_type == GM_REDUCE_T.GMREDUCE_MAX)
			{
				need_paren = true;
				Body.push("Math.max(");
				get_main().generate_rhs_id(id);
				Body.push(",");
			}
			else
			{
				get_main().generate_rhs_id(id);
				switch (reduce_op_type)
				{
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
    
		String.format(temp, "%sAggregator.getAggregatedValue().get()", id.get_genname());
		Body.push(temp);
		if (need_paren)
			Body.push(")");
		Body.pushln(";");
	}
	public void generate_headers(gm_code_writer Body)
	{
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
	public void generate_reduce_assign_vertex(ast_assign a, gm_code_writer Body, GM_REDUCE_T reduce_op_type)
	{
		assert a.is_target_scalar();
		ast_id id = a.get_lhs_scala();
    
		String temp = new String(new char[1024]);
		Body.push("((");
		generate_broadcast_variable_type(id.getTypeSummary(), Body, reduce_op_type);
		String.format(temp, ") getAggregator(%s)).aggregate(", create_key_string(id));
		Body.push(temp);
    
		//---------------------------------------------------
		// Initial Value: Reading of Id
		//---------------------------------------------------
		Body.push("(");
		get_main().generate_expr(a.get_rhs());
		Body.push(")");
		Body.pushln(");");
	}

	public void generate_broadcast_receive_vertex(ast_id id, gm_code_writer Body)
	{
		String temp = new String(new char[1024]);
		Body.push("((");
		generate_broadcast_variable_type(id.getTypeSummary(), Body);
		String.format(temp, ") getAggregator(%s)).getAggregatedValue().get()", create_key_string(id));
		Body.push(temp);
	}
	public void generate_parameter_read_vertex(ast_id id, gm_code_writer Body)
	{
		String temp = new String(new char[1024]);
		Body.push("getConf().");
		switch (id.getTypeSummary())
		{
			case GMTYPE_BOOL:
				String.format(temp, "getBoolean(\"%s\", false)", id.get_genname());
				break;
			case GMTYPE_INT:
				String.format(temp, "getInt(\"%s\", -1)", id.get_genname());
				break;
			case GMTYPE_LONG:
				String.format(temp, "getLong(\"%s\", -1L)", id.get_genname());
				break;
			case GMTYPE_FLOAT:
				String.format(temp, "getFloat(\"%s\", -1.0f)", id.get_genname());
				break;
				//TODO Waiting for https://issues.apache.org/jira/browse/HADOOP-8415 to be accepted:
				//case GMTYPE_DOUBLE: sprintf(temp, "getDouble(\"%s\", -1.0)", id->get_genname()); break;
			case GMTYPE_DOUBLE:
				String.format(temp, "getFloat(\"%s\", -1.0f)", id.get_genname());
				break;
			case GMTYPE_NODE:
				is_node_type_int() ? sprintf(temp, "getInt(\"%s\", -1)", id.get_genname()) : sprintf(temp, "getLong(\"%s\", -1L)", id.get_genname());
				break;
			default:
				assert false;
				break;
		}
		Body.push(temp);
	}

	public void generate_master_class_details(java.util.HashSet<gm_symtab_entry> prop, gm_code_writer Body)
	{
		java.util.Iterator<gm_symtab_entry> I;
    
		Body.pushln("@Override");
		Body.pushln("public void write(DataOutput out) throws IOException {");
		Body.pushln("out.writeInt(_master_state);");
		Body.pushln("out.writeInt(_master_state_nxt);");
		Body.pushln("out.writeBoolean(_master_should_start_workers);");
		Body.pushln("out.writeBoolean(_master_should_finish);");
    
		for (I = prop.iterator(); I.hasNext();)
		{
			gm_symtab_entry sym = I.next();
			gps_syminfo syminfo = (gps_syminfo) sym.find_info(GlobalMembersGps_syminfo.GPS_TAG_BB_USAGE);
			if (!syminfo.is_used_in_master())
				continue;
    
			GlobalMembersGm_giraph_lib.genPutIOB(sym.getId().get_genname(), sym.getType().getTypeSummary(), Body, this);
		}
		Body.pushln("}");
    
		Body.pushln("@Override");
		Body.pushln("public void readFields(DataInput in) throws IOException {");
		Body.pushln("_master_state = in.readInt();");
		Body.pushln("_master_state_nxt = in.readInt();");
		Body.pushln("_master_should_start_workers = in.readBoolean();");
		Body.pushln("_master_should_finish = in.readBoolean();");
    
		for (I = prop.iterator(); I.hasNext();)
		{
			gm_symtab_entry sym = I.next();
			gps_syminfo syminfo = (gps_syminfo) sym.find_info(GlobalMembersGps_syminfo.GPS_TAG_BB_USAGE);
			if (!syminfo.is_used_in_master())
				continue;
    
			GlobalMembersGm_giraph_lib.genGetIOB(sym.getId().get_genname(), sym.getType().getTypeSummary(), Body, this);
		}
		Body.pushln("}");
	}

	public void generate_vertex_prop_class_details(java.util.HashSet<gm_symtab_entry> prop, gm_code_writer Body, boolean is_edge_prop)
	{
		String temp = new String(new char[1024]);
		int total = is_edge_prop ? ((gm_gps_beinfo) GlobalMembersGm_main.FE.get_current_backend_info()).get_total_edge_property_size() : ((gm_gps_beinfo) GlobalMembersGm_main.FE.get_current_backend_info()).get_total_node_property_size();
    
		java.util.Iterator<gm_symtab_entry> I;
    
		if (is_edge_prop)
		{
			Body.pushln("public EdgeData() {");
			Body.pushln("// Default constructor needed for Giraph");
			Body.pushln("}");
			Body.NL();
			Body.pushln("public EdgeData(double input) {");
		}
		else
		{
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
		for (I = prop.iterator(); I.hasNext();)
		{
			gm_symtab_entry sym = I.next();
			GlobalMembersGm_giraph_lib.genPutIOB(sym.getId().get_genname(), sym.getType().getTargetTypeSummary(), Body, this);
		}
		if (GlobalMembersGm_main.FE.get_current_proc_info().find_info_bool(GlobalMembersGm_backend_gps.GPS_FLAG_USE_REVERSE_EDGE))
		{
			String.format(temp, "out.writeInt(%s.length);", GlobalMembersGm_backend_gps.GPS_REV_NODE_ID);
			Body.pushln(temp);
			String.format(temp, "for (%s node : %s) {", GlobalMembersGm_main.PREGEL_BE.get_lib().is_node_type_int() ? "IntWritable" : "LongWritable", GlobalMembersGm_backend_gps.GPS_REV_NODE_ID);
			Body.pushln(temp);
			Body.pushln("node.write(out);");
			Body.pushln("}");
		}
		Body.pushln("}");
    
		Body.pushln("@Override");
		Body.pushln("public void readFields(DataInput in) throws IOException {");
		for (I = prop.iterator(); I.hasNext();)
		{
			gm_symtab_entry sym = I.next();
			GlobalMembersGm_giraph_lib.genGetIOB(sym.getId().get_genname(), sym.getType().getTargetTypeSummary(), Body, this);
		}
		if (GlobalMembersGm_main.FE.get_current_proc_info().find_info_bool(GlobalMembersGm_backend_gps.GPS_FLAG_USE_REVERSE_EDGE))
		{
			Body.pushln("int _node_count = in.readInt();");
			String.format(temp, "%s = new %s[_node_count];", GlobalMembersGm_backend_gps.GPS_REV_NODE_ID, GlobalMembersGm_main.PREGEL_BE.get_lib().is_node_type_int() ? "IntWritable" : "LongWritable");
			Body.pushln(temp);
			Body.pushln("for (int i = 0; i < _node_count; i++) {");
			String.format(temp, "%s[i].readFields(in);", GlobalMembersGm_backend_gps.GPS_REV_NODE_ID);
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
	public void generate_receive_state_vertex(String state_var, gm_code_writer Body)
	{
		String temp = new String(new char[1024]);
		String.format(temp, "int %s = ((IntOverwriteAggregator) getAggregator(%s)).getAggregatedValue().get();", state_var, GlobalMembersGm_backend_gps.GPS_KEY_FOR_STATE);
		Body.pushln(temp);
	}
	public void generate_receive_isFirst_vertex(String is_first_var, gm_code_writer Body)
	{
		String temp = new String(new char[1024]);
		String.format(temp, "boolean %s = ((BooleanOverwriteAggregator) getAggregator(\"%s\"", is_first_var, is_first_var);
		Body.push(temp);
		Body.pushln(")).getAggregatedValue().get();");
	}

	public void generate_message_fields_define(int gm_type, int count, gm_code_writer Body)
	{
		for (int i = 0; i < count; i++)
		{
			String str = main.get_type_string(gm_type);
			String vname = get_message_field_var_name(gm_type, i);
			String.format(str_buf, "%s %s;", str, vname);
			Body.pushln(str_buf);
			vname = null;
		}
	}
//	virtual void generate_message_class_details(gm_gps_beinfo info, gm_code_writer Body);

	public void generate_vertex_prop_access_lhs(ast_id id, gm_code_writer Body)
	{
		String temp = new String(new char[1024]);
		String.format(temp, "%s.%s", GlobalMembersGm_backend_gps.STATE_SHORT_CUT, id.get_genname());
		Body.push(temp);
	}
	public void generate_vertex_prop_access_lhs_edge(ast_id id, gm_code_writer Body)
	{
		String temp = new String(new char[1024]);
		String.format(temp, "_outEdgeData.%s", id.get_genname());
		Body.push(temp);
	}
	public void generate_vertex_prop_access_rhs(ast_id id, gm_code_writer Body)
	{
		generate_vertex_prop_access_lhs(id, Body);
	}
	public void generate_vertex_prop_access_rhs_edge(ast_id id, gm_code_writer Body)
	{
		generate_vertex_prop_access_lhs_edge(id, Body);
	}

	public void generate_vertex_prop_access_remote_lhs(ast_id id, gm_code_writer Body)
	{
		String temp = new String(new char[1024]);
		String.format(temp, "_remote_%s", id.get_genname());
		Body.push(temp);
	}
	public void generate_vertex_prop_access_remote_lhs_edge(ast_id id, gm_code_writer Body)
	{
		String temp = new String(new char[1024]);
		String.format(temp, "_remote_%s", id.get_genname());
		Body.push(temp);
	}
	public void generate_vertex_prop_access_remote_rhs(ast_id id, gm_code_writer Body)
	{
		generate_vertex_prop_access_remote_lhs(id, Body);
	}
	public void generate_vertex_prop_access_prepare(gm_code_writer Body)
	{
		String temp = new String(new char[1024]);
		String.format(temp, "VertexData %s = getVertexValue();", GlobalMembersGm_backend_gps.STATE_SHORT_CUT);
		Body.pushln(temp);
	}

	public void generate_node_iterator_rhs(ast_id id, gm_code_writer Body)
	{
		//TODO
		Body.push("getVertexId().get()");
	}
	public int get_type_size(ast_typedecl t)
	{
		return get_type_size(t.getTypeSummary());
	}
	public int get_type_size(int gm_type)
	{
		if (gm_type == GMTYPE_T.GMTYPE_NODE.getValue())
		{
			if (this.is_node_type_int())
				return 4;
			else
				return 8;
		}
		else if (gm_type == GMTYPE_T.GMTYPE_EDGE.getValue())
		{
			assert false;
			return 0;
		}
    
		return GlobalMembersGm_giraph_lib.get_java_type_size(gm_type);
	}

	// caller should delete var_name later
	public String get_message_field_var_name(int gm_type, int index)
	{
    
		String temp = new String(new char[1024]);
		String str = main.get_type_string(gm_type);
		String.format(temp, "%c%d", str.charAt(0), index);
		return GlobalMembersGm_misc.gm_strdup(temp);
	}
//	virtual void generate_message_send(ast_foreach fe, gm_code_writer Body);

//	virtual void generate_message_receive_begin(ast_foreach fe, gm_code_writer Body, gm_gps_basic_block b, boolean is_only_comm);
//	virtual void generate_message_receive_begin(ast_sentblock sb, gm_symtab_entry drv, gm_code_writer Body, gm_gps_basic_block b, boolean is_only_comm);
//	virtual void generate_message_receive_begin(gm_gps_comm_unit U, gm_code_writer Body, gm_gps_basic_block b, boolean is_only_comm);

//	virtual void generate_message_receive_end(gm_code_writer Body, boolean is_only_comm);

	// random write
	public void generate_message_send_for_random_write(ast_sentblock sb, gm_symtab_entry sym, gm_code_writer Body)
	{
		String temp = new String(new char[1024]);
    
		String.format(temp, "sendMsg(new %s(", GlobalMembersGm_main.PREGEL_BE.get_lib().is_node_type_int() ? "IntWritable" : "LongWritable");
		Body.push(temp);
		get_main().generate_rhs_id(sym.getId());
		String.format(temp, "), %s);", get_random_write_message_name(sym));
		Body.pushln(temp);
	}

//	virtual void generate_expr_builtin(ast_expr_builtin e, gm_code_writer Body, boolean is_master);
}