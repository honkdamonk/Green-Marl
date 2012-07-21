package backend_giraph;

import ast.AST_NODE_TYPE;
import ast.ast_assign;
import ast.ast_expr;
import ast.ast_sent;
import ast.ast_sentblock;
import backend_cpp.*;
import backend_giraph.*;
import backend_gps.gm_gps_basic_block;
import backend_gps.gm_gps_bbtype_t;
import backend_gps.gm_gps_comm_t;
import backend_gps.gm_gps_comm_unit;
import backend_gps.gm_gps_communication_size_info;
import backend_gps.gm_gps_communication_symbol_info;
import backend_gps.gm_gps_congruent_msg_class;
import backend_gps.gm_gps_edge_access_t;
import common.*;
import frontend.*;
import inc.*;
import opt.*;
import tangible.*;

public class GlobalMembersGm_giraph_lib
{

    public static int get_java_type_size(int gm_type)
    {
        switch (gm_type)
        {
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

    public static void genPutIOB(String name, int gm_type, gm_code_writer Body, gm_giraphlib lib)
    {
        if (GlobalMembersGm_defs.gm_is_node_compatible_type(gm_type))
            gm_type = GMTYPE_T.GMTYPE_NODE.getValue();
        if (GlobalMembersGm_defs.gm_is_edge_compatible_type(gm_type))
            gm_type = GMTYPE_T.GMTYPE_EDGE.getValue();

        Body.push("out.");
        switch (gm_type)
        {
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
                if (lib.is_node_type_int())
                {
                    Body.push("writeInt");
                    break;
                }
                else
                {
                    Body.push("writeLong");
                    break;
                }
            case GMTYPE_EDGE:
                if (lib.is_edge_type_int())
                {
                    Body.push("writeInt");
                    break;
                }
                else
                {
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
    public static void genGetIOB(String name, int gm_type, gm_code_writer Body, gm_giraphlib lib)
    {
        if (GlobalMembersGm_defs.gm_is_node_compatible_type(gm_type))
            gm_type = GMTYPE_T.GMTYPE_NODE.getValue();
        if (GlobalMembersGm_defs.gm_is_edge_compatible_type(gm_type))
            gm_type = GMTYPE_T.GMTYPE_EDGE.getValue();

        Body.push(name);
        Body.push(" = in.");
        switch (gm_type)
        {
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
                if (lib.is_node_type_int())
                {
                    Body.push("getInt()");
                    break;
                }
                else
                {
                    Body.push("getLong()");
                    break;
                }
            case GMTYPE_EDGE:
                if (lib.is_edge_type_int())
                {
                    Body.push("getInt()");
                    break;
                }
                else
                {
                    Body.push("getLong()");
                    break;
                }
            default:
                assert false;
                break;
        }
        Body.pushln(";");
    }

    public static int get_total_size(gm_gps_communication_size_info I)
    {
        int sz = 0;
        sz += GlobalMembersGm_giraph_lib.get_java_type_size(GMTYPE_T.GMTYPE_INT) * I.num_int;
        sz += GlobalMembersGm_giraph_lib.get_java_type_size(GMTYPE_T.GMTYPE_BOOL) * I.num_bool;
        sz += GlobalMembersGm_giraph_lib.get_java_type_size(GMTYPE_T.GMTYPE_LONG) * I.num_long;
        sz += GlobalMembersGm_giraph_lib.get_java_type_size(GMTYPE_T.GMTYPE_DOUBLE) * I.num_double;
        sz += GlobalMembersGm_giraph_lib.get_java_type_size(GMTYPE_T.GMTYPE_FLOAT) * I.num_float;

        return sz;
    }

    //C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
    ///#define MESSAGE_PER_TYPE_LOOP_BEGIN(info, SYMS, str_buf) std::list<gm_gps_congruent_msg_class*>& LOOPS = info->get_congruent_message_classes(); std::list<gm_gps_congruent_msg_class*>::iterator I; bool is_single = info->is_single_message(); bool is_first = true; for(I=LOOPS.begin(); I!=LOOPS.end(); I++) { gm_gps_communication_size_info& SYMS = *((*I)->sz_info); int sz = get_total_size(SYMS); if (!is_single && is_first) { is_first = false; sprintf(str_buf,"if (m_type == %d) ", SYMS.id); Body.push(str_buf); } else if (!is_single) { sprintf(str_buf,"else if (m_type == %d) ", SYMS.id); Body.push(str_buf); }
    //C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
    ///#define MESSAGE_PER_TYPE_LOOP_END() }
    public static void generate_message_write_each(gm_giraphlib lib, int cnt, int gm_type, gm_code_writer Body)
    {
        for (int i = 0; i < cnt; i++)
        {
            String vname = lib.get_message_field_var_name(gm_type, i);
            GlobalMembersGm_giraph_lib.genPutIOB(vname, gm_type, Body, lib);
            vname = null;
        }
    }
    public static void generate_message_read1_each(gm_giraphlib lib, int cnt, int gm_type, gm_code_writer Body)
    {
        for (int i = 0; i < cnt; i++)
        {
            String vname = lib.get_message_field_var_name(gm_type, i);
            GlobalMembersGm_giraph_lib.genGetIOB(vname, gm_type, Body, lib);
            vname = null;
        }
    }
//C++ TO JAVA CONVERTER NOTE: This was formerly a static local variable declaration (not allowed in Java):
private static void generate_message_class_write_generate_message_class_read1(gm_giraphlib lib, gm_gps_beinfo info, gm_code_writer& Body)
//C++ TO JAVA CONVERTER NOTE: This was formerly a static local variable declaration (not allowed in Java):
private static boolean generate_message_class_write_is_symbol_defined_in_bb(gm_gps_basic_block* b, gm_symtab_entry *e)

    public static void generate_message_class_write(gm_giraphlib lib, gm_gps_beinfo info, gm_code_writer Body)
    {
        Body.pushln("@Override");
        Body.pushln("public void write(DataOutput out) throws IOException {");
        if (!info.is_single_message())
            Body.pushln("out.writeByte(m_type);");
        String str_buf = new String(new char[1024]);
        java.util.LinkedList<gm_gps_congruent_msg_class> LOOPS = info.get_congruent_message_classes();
        java.util.Iterator<gm_gps_congruent_msg_class> I;
        boolean is_single = info.is_single_message();
        boolean is_first = true;
        for(I = LOOPS.iterator(); I.hasNext();)
        {
            gm_gps_communication_size_info SYMS = *((I.next()).sz_info);
            int sz = GlobalMembersGm_giraph_lib.get_total_size(SYMS);
            if (!is_single && is_first)
            {
                is_first = false;
                String.format(str_buf,"if (m_type == %d) ", SYMS.id);
                Body.push(str_buf);
            }
            else if (!is_single)
            {
                String.format(str_buf,"else if (m_type == %d) ", SYMS.id);
                Body.push(str_buf);
            }
            if(!info.is_single_message())
                Body.pushln("{");
            if (info.is_single_message() && GlobalMembersGm_giraph_lib.get_total_size(SYMS) == 0)
                Body.pushln("out.writeByte((byte)0); // empty message");
            GlobalMembersGm_giraph_lib.generate_message_write_each(lib, SYMS.num_int, GMTYPE_T.GMTYPE_INT, Body);
            GlobalMembersGm_giraph_lib.generate_message_write_each(lib, SYMS.num_long, GMTYPE_T.GMTYPE_LONG, Body);
            GlobalMembersGm_giraph_lib.generate_message_write_each(lib, SYMS.num_float, GMTYPE_T.GMTYPE_FLOAT, Body);
            GlobalMembersGm_giraph_lib.generate_message_write_each(lib, SYMS.num_double, GMTYPE_T.GMTYPE_DOUBLE, Body);
            GlobalMembersGm_giraph_lib.generate_message_write_each(lib, SYMS.num_bool, GMTYPE_T.GMTYPE_BOOL, Body);
            if (!info.is_single_message())
                Body.pushln("}");
        () }() Body.pushln("}");
    }

    //C++ TO JAVA CONVERTER NOTE: This static local variable declaration (not allowed in Java) has been moved just prior to the method:
    //static void generate_message_class_read1(gm_giraphlib* lib, gm_gps_beinfo* info, gm_code_writer& Body)
    {
        Body.pushln("@Override");
        Body.pushln("public void readFields(DataInput in) throws IOException {");
        if (!info.is_single_message())
            Body.pushln("m_type = in.readByte();");
        String str_buf = new String(new char[1024]);
        java.util.LinkedList<gm_gps_congruent_msg_class> LOOPS = info.get_congruent_message_classes();
        java.util.Iterator<gm_gps_congruent_msg_class> I;
        boolean is_single = info.is_single_message();
        boolean is_first = true;
        for(I = LOOPS.iterator(); I.hasNext();)
        {
            gm_gps_communication_size_info SYMS = *((I.next()).sz_info);
            int sz = GlobalMembersGm_giraph_lib.get_total_size(SYMS);
            if (!is_single && is_first)
            {
                is_first = false;
                String.format(str_buf,"if (m_type == %d) ", SYMS.id);
                Body.push(str_buf);
            }
            else if (!is_single)
            {
                String.format(str_buf,"else if (m_type == %d) ", SYMS.id);
                Body.push(str_buf);
            }
            if(!info.is_single_message())
                Body.pushln("{");
            if (info.is_single_message() && GlobalMembersGm_giraph_lib.get_total_size(SYMS) == 0)
                Body.pushln("in.readByte(); // consume empty message byte");
            GlobalMembersGm_giraph_lib.generate_message_read1_each(lib, SYMS.num_int, GMTYPE_T.GMTYPE_INT, Body);
            GlobalMembersGm_giraph_lib.generate_message_read1_each(lib, SYMS.num_long, GMTYPE_T.GMTYPE_LONG, Body);
            GlobalMembersGm_giraph_lib.generate_message_read1_each(lib, SYMS.num_float, GMTYPE_T.GMTYPE_FLOAT, Body);
            GlobalMembersGm_giraph_lib.generate_message_read1_each(lib, SYMS.num_double, GMTYPE_T.GMTYPE_DOUBLE, Body);
            GlobalMembersGm_giraph_lib.generate_message_read1_each(lib, SYMS.num_bool, GMTYPE_T.GMTYPE_BOOL, Body);
            if (!info.is_single_message())
                Body.pushln("}");
        () }() Body.pushln("}");
    }

    void gm_giraphlib.generate_message_class_details(gm_gps_beinfo info, gm_code_writer& Body)
    {

        Body.pushln("// union of all message fields  ");
        gm_gps_communication_size_info size_info = info.get_max_communication_size();

        generate_message_fields_define(GMTYPE_T.GMTYPE_INT, size_info.num_int, Body);
        generate_message_fields_define(GMTYPE_T.GMTYPE_LONG, size_info.num_long, Body);
        generate_message_fields_define(GMTYPE_T.GMTYPE_FLOAT, size_info.num_float, Body);
        generate_message_fields_define(GMTYPE_T.GMTYPE_DOUBLE, size_info.num_double, Body);
        generate_message_fields_define(GMTYPE_T.GMTYPE_BOOL, size_info.num_bool, Body);
        Body.NL();

        GlobalMembersGm_giraph_lib.generate_message_class_write(this, info, Body);
        generate_message_class_write_generate_message_class_read1(this, info, Body);
    }

    void gm_giraphlib.generate_message_send(ast_foreach* fe, gm_code_writer& Body)
    {
        String temp = new String(new char[1024]);

        gm_gps_beinfo info = (gm_gps_beinfo) GlobalMembersGm_main.FE.get_current_backend_info();

        int m_type = (fe == null) ? gm_gps_comm_t.GPS_COMM_INIT : gm_gps_comm_t.GPS_COMM_NESTED;

        gm_gps_comm_unit U = new gm_gps_comm_unit(m_type, fe);

        java.util.LinkedList<gm_gps_communication_symbol_info> LIST = info.get_all_communication_symbols(U);

        gm_gps_communication_size_info SINFO = info.find_communication_size_info(U);

        boolean need_separate_message = (fe == null) ? false : fe.find_info_bool(GlobalMembersGm_backend_gps.GPS_FLAG_EDGE_DEFINING_INNER);
        boolean is_in_neighbors = (fe != null) && (fe.get_iter_type() == GMTYPE_T.GMTYPE_NODEITER_IN_NBRS);

        if (!need_separate_message)
        {
            Body.pushln("// Sending messages to all neighbors (if there is a neighbor)");
            if (is_in_neighbors)
            {
                String.format(temp, "if (%s.%s.length > 0) {", GlobalMembersGm_backend_gps.STATE_SHORT_CUT, GlobalMembersGm_backend_gps.GPS_REV_NODE_ID); //TODO
                Body.pushln(temp);
            }
            else
            {
                Body.pushln("if (getNumOutEdges() > 0) {");
            }
        }
        else
        {
            assert (fe != null) && (fe.get_iter_type() == GMTYPE_T.GMTYPE_NODEITER_NBRS);
            Body.pushln("// Sending messages to each neighbor");
            String.format(temp, "Iterator<%s> neighbors = this.getOutEdgesIterator();", GlobalMembersGm_main.PREGEL_BE.get_lib().is_node_type_int() ? "IntWritable" : "LongWritable");
            Body.pushln(temp);
            Body.pushln("while (neighbors.hasNext()) {");
            String.format(temp, "%s _neighborId = neighbors.next();", GlobalMembersGm_main.PREGEL_BE.get_lib().is_node_type_int() ? "IntWritable" : "LongWritable");
            Body.pushln(temp);
            Body.pushln("EdgeData _outEdgeData = this.getEdgeValue(_neighborId);");
        }

        // check if any edge updates that should be done before message sending
        java.util.LinkedList<ast_sent> sents_after_message = new java.util.LinkedList<ast_sent>();

        if ((fe != null) && (fe.has_info_list(GlobalMembersGm_backend_gps.GPS_LIST_EDGE_PROP_WRITE)))
        {
            java.util.LinkedList<Object > L = fe.get_info_list(GlobalMembersGm_backend_gps.GPS_LIST_EDGE_PROP_WRITE);

            java.util.Iterator<Object > I;
            for (I = L.iterator(); I.hasNext();)
            {
                ast_sent s = (ast_sent) I.next();
                assert s.get_nodetype() == AST_NODE_TYPE.AST_ASSIGN;
                ast_assign a = (ast_assign) s;
                assert !a.is_target_scalar();
                gm_symtab_entry e = a.get_lhs_field().get_second().getSymInfo();
//C++ TO JAVA CONVERTER TODO TASK: Java does not have an equivalent for pointers to value types:
//ORIGINAL LINE: int* i = (int*) fe->find_info_map_value(GPS_MAP_EDGE_PROP_ACCESS, e);
                int i = (int) fe.find_info_map_value(GlobalMembersGm_backend_gps.GPS_MAP_EDGE_PROP_ACCESS, e);
                assert i!= null;

                if (i == gm_gps_edge_access_t.GPS_ENUM_EDGE_VALUE_SENT_WRITE.getValue())
                {
                    sents_after_message.addLast(s);
                }
                else
                {
                    get_main().generate_sent(s);
                }
            }
        }

        Body.push("MessageData _msg = new MessageData(");

        // todo: should this always be a byte?
        String.format(str_buf, "(byte) %d", SINFO.msg_class.id);
        Body.push(str_buf);
        Body.pushln(");");

        //------------------------------------------------------------
        // create message variables
        //------------------------------------------------------------
        if (fe != null)
        {
            assert fe.get_body().get_nodetype() == AST_NODE_TYPE.AST_SENTBLOCK;
            java.util.Iterator<ast_sent> J;
            ast_sentblock sb = (ast_sentblock) fe.get_body();
            for (J = sb.get_sents().iterator(); J.hasNext();)
            {
                ast_sent s = J.next();
                if (s.find_info_bool(GlobalMembersGm_backend_gps.GPS_FLAG_COMM_DEF_ASSIGN))
                {
                    get_main().generate_sent(s);
                }
            }
        }

        java.util.Iterator<gm_gps_communication_symbol_info> I;
        for (I = LIST.iterator(); I.hasNext();)
        {
            gm_gps_communication_symbol_info SYM = I.next();
            Body.push("_msg.");
            String fname = gm_giraphlib.get_message_field_var_name(SYM.gm_type, SYM.idx);
            Body.push(fname);
            fname = null;
            Body.push(" = ");
            gm_symtab_entry e = SYM.symbol;
            if (e.getType().is_node_property())
            {
                generate_vertex_prop_access_rhs(e.getId(), Body);
            }
            else if (e.getType().is_edge_property())
            {
                generate_vertex_prop_access_rhs_edge(e.getId(), Body);
            }
            else
            {
                get_main().generate_rhs_id(e.getId());
            }
            Body.pushln(";");
        }

        if (!need_separate_message)
        {
            if (is_in_neighbors)
            {
                String.format(temp, "for (%s node : %s.%s) {", GlobalMembersGm_main.PREGEL_BE.get_lib().is_node_type_int() ? "IntWritable" : "LongWritable", GlobalMembersGm_backend_gps.STATE_SHORT_CUT, GlobalMembersGm_backend_gps.GPS_REV_NODE_ID);
                Body.pushln(temp);
                Body.pushln("sendMsg(node, _msg);");
                Body.pushln("}");
            }
            else
            {
                Body.pushln("sendMsgToAllEdges(_msg);");
            }
            Body.pushln("}");
        }
        else
        {
            Body.pushln("sendMsg(_neighborId, _msg);");
            if (sents_after_message.size() > 0)
            {
                Body.NL();
                java.util.Iterator<ast_sent> I;
                for (I = sents_after_message.iterator(); I.hasNext();)
                {
                    ast_sent s = I.next();
                    get_main().generate_sent(s);
                }

                sents_after_message.clear();
            }
            Body.pushln("}");
        }
        assert sents_after_message.size() == 0;
    }

    //C++ TO JAVA CONVERTER NOTE: This static local variable declaration (not allowed in Java) has been moved just prior to the method:
    //static boolean is_symbol_defined_in_bb(gm_gps_basic_block* b, gm_symtab_entry *e)
    {
        java.util.HashMap<gm_symtab_entry, gps_syminfo> SYMS = b.get_symbols();
        if ( ! SYMS.containsKey(e))
            return false;
        else
            return true;
    }

    void gm_giraphlib.generate_message_receive_begin(ast_foreach* fe, gm_code_writer& Body, gm_gps_basic_block* b, boolean is_only_comm)
    {
        gm_gps_beinfo info = (gm_gps_beinfo) GlobalMembersGm_main.FE.get_current_backend_info();
        int comm_type = (fe == null) ? gm_gps_comm_t.GPS_COMM_INIT : gm_gps_comm_t.GPS_COMM_NESTED;
        gm_gps_comm_unit U = new gm_gps_comm_unit(comm_type, fe);
        generate_message_receive_begin(U, Body, b, is_only_comm);
    }
    void gm_giraphlib.generate_message_receive_begin(ast_sentblock* sb, gm_symtab_entry* drv, gm_code_writer& Body, gm_gps_basic_block* b, boolean is_only_comm)
    {
        gm_gps_beinfo info = (gm_gps_beinfo) GlobalMembersGm_main.FE.get_current_backend_info();
        int comm_type = gm_gps_comm_t.GPS_COMM_RANDOM_WRITE.getValue();
        gm_gps_comm_unit U = new gm_gps_comm_unit(comm_type, sb, drv);
        generate_message_receive_begin(U, Body, b, is_only_comm);
    }

    void gm_giraphlib.generate_message_receive_begin(gm_gps_comm_unit& U, gm_code_writer& Body, gm_gps_basic_block *b, boolean is_only_comm)
    {
        gm_gps_beinfo info = (gm_gps_beinfo) GlobalMembersGm_main.FE.get_current_backend_info();

        java.util.LinkedList<gm_gps_communication_symbol_info> LIST = info.get_all_communication_symbols(U);
        //int comm_id = info->find_communication_size_info(fe).id;
        int comm_id = (info.find_communication_size_info(U)).msg_class.id;

        String temp = new String(new char[1024]);
        if (!is_only_comm && !info.is_single_message())
        {
            String.format(temp, "if (_msg.m_type == %d) {", comm_id);
            Body.pushln(temp);
        }

        java.util.Iterator<gm_gps_communication_symbol_info> I;
        for (I = LIST.iterator(); I.hasNext();)
        {
            gm_gps_communication_symbol_info SYM = I.next();
            gm_symtab_entry e = SYM.symbol;

            // check it once again later
            if (e.getType().is_property() || e.getType().is_node_compatible() || e.getType().is_edge_compatible() || !generate_message_class_write_is_symbol_defined_in_bb(b, e))
            {
                String str = main.get_type_string(SYM.gm_type);
                Body.push(str);
                Body.SPC();
            }
            if (e.getType().is_property())
            {
                generate_vertex_prop_access_remote_lhs(e.getId(), Body);
            }
            else
            {
                Body.push(e.getId().get_genname());
            }
            Body.push(" = ");
            Body.push("_msg.");
            String fname = gm_giraphlib.get_message_field_var_name(SYM.gm_type, SYM.idx);
            Body.push(fname);
            fname = null;
            Body.pushln(";");
        }
    }

    void gm_giraphlib.generate_message_receive_end(gm_code_writer& Body, boolean is_only_comm)
    {
        if (!is_only_comm)
        {
            Body.pushln("}");
        }
    }

    void gm_giraphlib.generate_expr_builtin(ast_expr_builtin* be, gm_code_writer& Body, boolean is_master)
    {
        gm_builtin_def def = be.get_builtin_def();
        java.util.LinkedList<ast_expr> ARGS = be.get_args();

        switch (def.get_method_id())
        {
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

    void gm_giraphlib.generate_prepare_bb(gm_code_writer& Body, gm_gps_basic_block* bb)
    {
        String temp = new String(new char[1024]);

        if (bb.get_type() == gm_gps_bbtype_t.GM_GPS_BBTYPE_PREPARE1)
        {
            Body.pushln("// Preperation: creating reverse edges");
            String.format(temp, "%s %s = getVertexId().get();", main.get_type_string(GMTYPE_T.GMTYPE_NODE), GlobalMembersGm_backend_gps.GPS_DUMMY_ID);
            Body.pushln(temp);

            generate_message_send(null, Body);

        }
        else if (bb.get_type() == gm_gps_bbtype_t.GM_GPS_BBTYPE_PREPARE2)
        {
            Body.pushln("//Preperation creating reverse edges");
            Body.pushln("int i = 0; // iterable does not have length(), so we have to count it");
            Body.pushln("while (_msgs.hasNext()) {");
            Body.pushln("_msgs.next();");
            Body.pushln("i++;");
            Body.pushln("}");

            String.format(temp, "%s.%s = new %s[i];", GlobalMembersGm_backend_gps.STATE_SHORT_CUT, GlobalMembersGm_backend_gps.GPS_REV_NODE_ID, GlobalMembersGm_main.PREGEL_BE.get_lib().is_node_type_int() ? "IntWritable" : "LongWritable");
            Body.pushln(temp);
            Body.NL();

            Body.pushln("i=0;");
            Body.pushln("MessageData _msg;");
            Body.pushln("while (_msgs.hasNext()) {");
            Body.pushln("_msg = _msgs.next();");
            generate_message_receive_begin(null, Body, bb, true);
            String.format(temp, "%s.%s[i] = new %s(%s);", GlobalMembersGm_backend_gps.STATE_SHORT_CUT, GlobalMembersGm_backend_gps.GPS_REV_NODE_ID, GlobalMembersGm_main.PREGEL_BE.get_lib().is_node_type_int() ? "IntWritable" : "LongWritable", GlobalMembersGm_backend_gps.GPS_DUMMY_ID);
            Body.pushln(temp);
            generate_message_receive_end(Body, true);
            Body.pushln("i++;");
            Body.pushln("}");
        }
        else
        {
            assert false;
        }

    }

}

//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define TO_STR(X) #X
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define DEF_STRING(X) static const char *X = "X"
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP(CLASS, DESC) class CLASS : public gm_compile_step { private: CLASS() {set_description(DESC);}public: virtual void process(ast_procdef*p); virtual gm_compile_step* get_instance(){return new CLASS();} static gm_compile_step* get_factory(){return new CLASS();} };
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP_FACTORY(CLASS) CLASS::get_factory()
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define AUX_INFO(X,Y) "X"":""Y"
///#define GM_BLTIN_MUTATE_GROW 1
///#define GM_BLTIN_MUTATE_SHRINK 2
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_BLTIN_FLAG_TRUE true



// scalar variable broadcast
// master --> vertex
















