package backend_gps;

import frontend.gm_symtab_entry;
import inc.GMTYPE_T;
import inc.GlobalMembersGm_backend_gps;
import inc.gm_code_writer;
import ast.AST_NODE_TYPE;
import ast.ast_assign;
import ast.ast_expr;
import ast.ast_expr_builtin;
import ast.ast_foreach;
import ast.ast_sent;
import ast.ast_sentblock;
import backend_gps.GlobalMembersGm_gps_lib;

import common.GlobalMembersGm_main;
import common.GlobalMembersGm_misc;
import common.gm_builtin_def;

public class GlobalMembersGm_gps_lib
{

    public static int get_java_type_size(GMTYPE_T gm_type)
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

    public static void genPutIOB(String name, GMTYPE_T gm_type, gm_code_writer Body, gm_gpslib lib)
    {
        if (GlobalMembersGm_defs.gm_is_node_compatible_type(gm_type))
            gm_type = GMTYPE_T.GMTYPE_NODE; //TODO setting input var?
        if (GlobalMembersGm_defs.gm_is_edge_compatible_type(gm_type))
            gm_type = GMTYPE_T.GMTYPE_EDGE; //TODO setting input var?

        // assumtion: IOB name is IOB
        Body.push("IOB.");
        switch (gm_type)
        {
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
                if (lib.is_node_type_int())
                {
                    Body.push("putInt");
                    break;
                }
                else
                {
                    Body.push("putLong");
                    break;
                }
            case GMTYPE_EDGE:
                if (lib.is_edge_type_int())
                {
                    Body.push("putInt");
                    break;
                }
                else
                {
                    Body.push("putLong");
                    break;
                }
            default:
                assert false;
                break;
        }
        Body.push("(");
        if (gm_type == GMTYPE_T.GMTYPE_BOOL)
        {
            Body.push(name);
            Body.push("?(byte)1:(byte)0");
        }
        else
        {
            Body.push(name);
        }
        Body.pushln(");");
    }
    public static void genGetIOB(String name, GMTYPE_T gm_type, gm_code_writer Body, gm_gpslib lib)
    {
        if (GlobalMembersGm_defs.gm_is_node_compatible_type(gm_type))
            gm_type = GMTYPE_T.GMTYPE_NODE;
        if (GlobalMembersGm_defs.gm_is_edge_compatible_type(gm_type))
            gm_type = GMTYPE_T.GMTYPE_EDGE;

        // assumtion: IOB name is IOB
        Body.push(name);
        Body.push("= IOB.");
        switch (gm_type)
        {
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

    public static void genReadByte(String name, GMTYPE_T gm_type, int offset, gm_code_writer Body, gm_gpslib lib)
    {
        if (GlobalMembersGm_defs.gm_is_node_compatible_type(gm_type))
        {
            gm_type = (lib.is_node_type_int()) ? GMTYPE_T.GMTYPE_INT : GMTYPE_T.GMTYPE_LONG;
        }
        // assumption: "byte[] _BA, int _idx"
        Body.push(name);
        Body.push("= Utils.");
        switch (gm_type)
        {
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
        String.format(str_buf, "_BA, _idx + %d);", offset);
        Body.pushln(str_buf);
    }

    //TODO set output vars?
    public static void get_java_parse_string(gm_gpslib L, GMTYPE_T gm_type, String name1, String name2)
    {
        switch (gm_type)
        {
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
                if (L.is_node_type_int())
                {
                    name1 = "Integer";
                    name2 = "parseInt";
                    break;
                }
                else
                {
                    name1 = "Long";
                    name2 = "parseLong";
                    break;
                }
            case GMTYPE_EDGE:
                if (L.is_edge_type_int())
                {
                    name1 = "Integer";
                    name2 = "parseInt";
                    break;
                }
                else
                {
                    name1 = "Long";
                    name2 = "parseLong";
                    break;
                }
            default:
                assert false;
                break;
        }

    }

    public static int get_total_size(gm_gps_communication_size_info I)
    {
        int sz = 0;
        sz += GlobalMembersGm_gps_lib.get_java_type_size(GMTYPE_T.GMTYPE_INT) * I.num_int;
        sz += GlobalMembersGm_gps_lib.get_java_type_size(GMTYPE_T.GMTYPE_BOOL) * I.num_bool;
        sz += GlobalMembersGm_gps_lib.get_java_type_size(GMTYPE_T.GMTYPE_LONG) * I.num_long;
        sz += GlobalMembersGm_gps_lib.get_java_type_size(GMTYPE_T.GMTYPE_DOUBLE) * I.num_double;
        sz += GlobalMembersGm_gps_lib.get_java_type_size(GMTYPE_T.GMTYPE_FLOAT) * I.num_float;

        return sz;
    }

    //C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
    ///#define MESSAGE_PER_TYPE_LOOP_BEGIN(info, SYMS, str_buf) std::list<gm_gps_congruent_msg_class*>& LOOPS = info->get_congruent_message_classes(); std::list<gm_gps_congruent_msg_class*>::iterator I; bool is_single = info->is_single_message(); bool is_first = true; for(I=LOOPS.begin(); I!=LOOPS.end(); I++) { gm_gps_communication_size_info& SYMS = *((*I)->sz_info); int sz = get_total_size(SYMS); if (!is_single && is_first) { is_first = false; sprintf(str_buf,"if (m_type == %d) ", SYMS.id); Body.push(str_buf); } else if (!is_single) { sprintf(str_buf,"else if (m_type == %d) ", SYMS.id); Body.push(str_buf); }
    //C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
    ///#define MESSAGE_PER_TYPE_LOOP_END() } if (!is_single) Body.pushln("//for empty messages (signaling only)");
    public static void generate_message_write_each(gm_gpslib lib, int cnt, GMTYPE_T gm_type, gm_code_writer Body)
    {
        for (int i = 0; i < cnt; i++)
        {
            String vname = lib.get_message_field_var_name(gm_type, i);
            GlobalMembersGm_gps_lib.genPutIOB(vname, gm_type, Body, lib);
            vname = null;
        }
    }
    public static void generate_message_read1_each(gm_gpslib lib, int cnt, GMTYPE_T gm_type, gm_code_writer Body)
    {
        for (int i = 0; i < cnt; i++)
        {
            String vname = lib.get_message_field_var_name(gm_type, i);
            GlobalMembersGm_gps_lib.genGetIOB(vname, gm_type, Body, lib);
            vname = null;
        }
    }
    public static void generate_message_read2_each(gm_gpslib lib, int cnt, GMTYPE_T gm_type, gm_code_writer Body, tangible.RefObject<Integer> offset)
    {
        for (int i = 0; i < cnt; i++)
        {
            String vname = lib.get_message_field_var_name(gm_type, i);
            GlobalMembersGm_gps_lib.genReadByte(vname, gm_type, offset.argvalue, Body, lib);
            offset.argvalue += GlobalMembersGm_gps_lib.get_java_type_size(gm_type);
            vname = null;
        }
    }
//C++ TO JAVA CONVERTER NOTE: This was formerly a static local variable declaration (not allowed in Java):
private static void generate_message_class_get_size_generate_message_class_read1(gm_gpslib lib, gm_gps_beinfo info, gm_code_writer Body) {}
//C++ TO JAVA CONVERTER NOTE: This was formerly a static local variable declaration (not allowed in Java):
private static void generate_message_class_get_size_generate_message_class_read2(gm_gpslib lib, gm_gps_beinfo info, gm_code_writer Body) {}
//C++ TO JAVA CONVERTER NOTE: This was formerly a static local variable declaration (not allowed in Java):
private static void generate_message_class_get_size_generate_message_class_read3(gm_gpslib lib, gm_gps_beinfo info, gm_code_writer Body) {}
//C++ TO JAVA CONVERTER NOTE: This was formerly a static local variable declaration (not allowed in Java):
private static void generate_message_class_get_size_generate_message_class_combine(gm_gpslib lib, gm_gps_beinfo info, gm_code_writer Body) {}
//C++ TO JAVA CONVERTER NOTE: This was formerly a static local variable declaration (not allowed in Java):
private static boolean generate_message_class_get_size_is_symbol_defined_in_bb(gm_gps_basic_block b, gm_symtab_entry e) {}

    public static void generate_message_class_get_size(gm_gps_beinfo info, gm_code_writer Body)
    {
        Body.pushln("@Override");
        Body.pushln("public int numBytes() {");
        String str_buf = new String(new char[1024]);

        java.util.LinkedList<gm_gps_congruent_msg_class> LOOPS = info.get_congruent_message_classes();
        boolean is_single = info.is_single_message();
        boolean is_first = true;
        for (gm_gps_congruent_msg_class c : LOOPS) {
        {
            gm_gps_communication_size_info SYMS = c.sz_info;
            int sz = GlobalMembersGm_gps_lib.get_total_size(SYMS);
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
            };
            if (info.is_single_message())
            {
                if (GlobalMembersGm_gps_lib.get_total_size(SYMS) == 0)
                    String.format(str_buf, "return 1; // empty message ");
                else
                    String.format(str_buf, "return %d; // data", GlobalMembersGm_gps_lib.get_total_size(SYMS));
            }
            else
                String.format(str_buf, "return (1+%d); // type + data", GlobalMembersGm_gps_lib.get_total_size(SYMS));
            Body.pushln(str_buf);
        () } if (!is_single) Body.pushln("//for empty messages (signaling only)");() if(!info.is_single_message()) Body.pushln("return 1; ");
        else if (info.is_empty_message())
            Body.pushln("return 0; ");
        Body.pushln("}");
    }

    //C++ TO JAVA CONVERTER NOTE: This static local variable declaration (not allowed in Java) has been moved just prior to the method:
    static void generate_message_class_write(gm_gpslib lib, gm_gps_beinfo info, gm_code_writer Body)
    {
        Body.pushln("@Override");
        Body.pushln("public void write(IoBuffer IOB) {");
        if (!info.is_single_message())
            Body.pushln("IOB.put(m_type);");
        String str_buf = new String(new char[1024]);
        java.util.LinkedList<gm_gps_congruent_msg_class> LOOPS = info.get_congruent_message_classes();
        java.util.Iterator<gm_gps_congruent_msg_class> I;
        boolean is_single = info.is_single_message();
        boolean is_first = true;
        for(I = LOOPS.iterator(); I.hasNext();)
        {
            gm_gps_communication_size_info SYMS = *((I.next()).sz_info);
            int sz = GlobalMembersGm_gps_lib.get_total_size(SYMS);
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
            if (info.is_single_message() && GlobalMembersGm_gps_lib.get_total_size(SYMS) == 0)
                Body.pushln("IOB.put((byte)0); // empty message");
            GlobalMembersGm_gps_lib.generate_message_write_each(lib, SYMS.num_int, GMTYPE_T.GMTYPE_INT, Body);
            GlobalMembersGm_gps_lib.generate_message_write_each(lib, SYMS.num_long, GMTYPE_T.GMTYPE_LONG, Body);
            GlobalMembersGm_gps_lib.generate_message_write_each(lib, SYMS.num_float, GMTYPE_T.GMTYPE_FLOAT, Body);
            GlobalMembersGm_gps_lib.generate_message_write_each(lib, SYMS.num_double, GMTYPE_T.GMTYPE_DOUBLE, Body);
            GlobalMembersGm_gps_lib.generate_message_write_each(lib, SYMS.num_bool, GMTYPE_T.GMTYPE_BOOL, Body);
            if (!info.is_single_message())
                Body.pushln("}");
        () } if (!is_single) Body.pushln("//for empty messages (signaling only)");() Body.pushln("}");
    }

    //C++ TO JAVA CONVERTER NOTE: This static local variable declaration (not allowed in Java) has been moved just prior to the method:
    static void generate_message_class_read1(gm_gpslib lib, gm_gps_beinfo info, gm_code_writer Body)
    {
        Body.pushln("@Override");
        Body.pushln("public void read(IoBuffer IOB) {");
        if (!info.is_single_message())
            Body.pushln("m_type = IOB.get();");
        String str_buf = new String(new char[1024]);
        java.util.LinkedList<gm_gps_congruent_msg_class> LOOPS = info.get_congruent_message_classes();
        boolean is_single = info.is_single_message();
        boolean is_first = true;
        for (gm_gps_congruent_msg_class c : LOOPS) {
            gm_gps_communication_size_info SYMS = c.sz_info;
            int sz = GlobalMembersGm_gps_lib.get_total_size(SYMS);
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
            if (info.is_single_message() && GlobalMembersGm_gps_lib.get_total_size(SYMS) == 0)
                Body.pushln("IOB.get(); // consume empty message byte");
            GlobalMembersGm_gps_lib.generate_message_read1_each(lib, SYMS.num_int, GMTYPE_T.GMTYPE_INT, Body);
            GlobalMembersGm_gps_lib.generate_message_read1_each(lib, SYMS.num_long, GMTYPE_T.GMTYPE_LONG, Body);
            GlobalMembersGm_gps_lib.generate_message_read1_each(lib, SYMS.num_float, GMTYPE_T.GMTYPE_FLOAT, Body);
            GlobalMembersGm_gps_lib.generate_message_read1_each(lib, SYMS.num_double, GMTYPE_T.GMTYPE_DOUBLE, Body);
            GlobalMembersGm_gps_lib.generate_message_read1_each(lib, SYMS.num_bool, GMTYPE_T.GMTYPE_BOOL, Body);
            if (!info.is_single_message())
                Body.pushln("}");
        () } if (!is_single) Body.pushln("//for empty messages (signaling only)");() Body.pushln("}");
    }

    //C++ TO JAVA CONVERTER NOTE: This static local variable declaration (not allowed in Java) has been moved just prior to the method:
    static void generate_message_class_read2(gm_gpslib lib, gm_gps_beinfo info, gm_code_writer Body)
    {
        Body.pushln("@Override");
        Body.pushln("public int read(byte[] _BA, int _idx) {");
        if (!info.is_single_message())
            Body.pushln("m_type = _BA[_idx];");
        String str_buf = new String(new char[1024]);
        java.util.LinkedList<gm_gps_congruent_msg_class> LOOPS = info.get_congruent_message_classes();
        boolean is_single = info.is_single_message();
        boolean is_first = true;
        for (gm_gps_congruent_msg_class c : LOOPS) {
            gm_gps_communication_size_info SYMS = c.sz_info;
            int sz = GlobalMembersGm_gps_lib.get_total_size(SYMS);
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
            int offset;
            if (info.is_single_message())
                offset = 0;
            else
                offset = 1;
            if (!info.is_single_message())
                Body.pushln("{");
            if (info.is_single_message() && (GlobalMembersGm_gps_lib.get_total_size(SYMS) == 0))
                Body.pushln("_idx++; // consume empty message byte");
		tangible.RefObject<Integer> tempRef_offset = new tangible.RefObject<Integer>(offset);
            GlobalMembersGm_gps_lib.generate_message_read2_each(lib, SYMS.num_int, GMTYPE_T.GMTYPE_INT, Body, tempRef_offset);
            offset = tempRef_offset.argvalue;
		tangible.RefObject<Integer> tempRef_offset2 = new tangible.RefObject<Integer>(offset);
            GlobalMembersGm_gps_lib.generate_message_read2_each(lib, SYMS.num_long, GMTYPE_T.GMTYPE_LONG, Body, tempRef_offset2);
            offset = tempRef_offset2.argvalue;
		tangible.RefObject<Integer> tempRef_offset3 = new tangible.RefObject<Integer>(offset);
            GlobalMembersGm_gps_lib.generate_message_read2_each(lib, SYMS.num_float, GMTYPE_T.GMTYPE_FLOAT, Body, tempRef_offset3);
            offset = tempRef_offset3.argvalue;
		tangible.RefObject<Integer> tempRef_offset4 = new tangible.RefObject<Integer>(offset);
            GlobalMembersGm_gps_lib.generate_message_read2_each(lib, SYMS.num_double, GMTYPE_T.GMTYPE_DOUBLE, Body, tempRef_offset4);
            offset = tempRef_offset4.argvalue;
		tangible.RefObject<Integer> tempRef_offset5 = new tangible.RefObject<Integer>(offset);
            GlobalMembersGm_gps_lib.generate_message_read2_each(lib, SYMS.num_bool, GMTYPE_T.GMTYPE_BOOL, Body, tempRef_offset5);
            offset = tempRef_offset5.argvalue;
            if (info.is_single_message())
            {
                if (GlobalMembersGm_gps_lib.get_total_size(SYMS) == 0)
                    String.format(str_buf, "return 1;");
                else
                    String.format(str_buf, "return %d;", GlobalMembersGm_gps_lib.get_total_size(SYMS));
            }
            else
                String.format(str_buf, "return 1 + %d;", GlobalMembersGm_gps_lib.get_total_size(SYMS));
            Body.pushln(str_buf);
            if (!info.is_single_message())
                Body.pushln("}");
        () } if (!is_single) Body.pushln("//for empty messages (signaling only)");() if(!info.is_single_message()) Body.pushln("return 1;");
        else if (info.is_empty_message())
            Body.pushln("return 0;");
        Body.pushln("}");
    }

    //C++ TO JAVA CONVERTER NOTE: This static local variable declaration (not allowed in Java) has been moved just prior to the method:
    static void generate_message_class_read3(gm_gpslib lib, gm_gps_beinfo info, gm_code_writer Body)
    {
        Body.pushln("@Override");
        Body.pushln("public int read(IoBuffer IOB, byte[] _BA, int _idx) {");
        if (!info.is_single_message())
            Body.pushln("byte m_type; IOB.get(_BA, _idx, 1); m_type = _BA[_idx];");
        String str_buf = new String(new char[1024]);
        java.util.LinkedList<gm_gps_congruent_msg_class> LOOPS = info.get_congruent_message_classes();
        boolean is_single = info.is_single_message();
        boolean is_first = true;
        for (gm_gps_congruent_msg_class c : LOOPS) {
        gm_gps_communication_size_info SYMS = c.sz_info;
            int sz = GlobalMembersGm_gps_lib.get_total_size(SYMS);
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
            int offset;
            if (!info.is_single_message())
                offset = 1;
            else
                offset = 0;
            if (!info.is_single_message())
                Body.pushln("{");
            int sz2 = GlobalMembersGm_gps_lib.get_total_size(SYMS);
            if (info.is_single_message() && (GlobalMembersGm_gps_lib.get_total_size(SYMS) == 0))
            {
                Body.pushln("//empty message(dummy byte)");
                sz2 = 1;
            }
            if (sz2 == 0)
            {
                String.format(str_buf, "//empty message");
            }
            else
            {
                String.format(str_buf, "IOB.get(_BA, _idx+%d, %d);", offset, sz2);
            }
            Body.pushln(str_buf);

            if (info.is_single_message())
                if (GlobalMembersGm_gps_lib.get_total_size(SYMS) == 0)
                    String.format(str_buf, "return 1;");
                else
                    String.format(str_buf, "return %d;", sz2);
            else
                String.format(str_buf, "return 1 + %d;", sz2);
            Body.pushln(str_buf);
            if (!info.is_single_message())
                Body.pushln("}");
        () } if (!is_single) Body.pushln("//for empty messages (signaling only)");() if(!info.is_single_message()) Body.pushln("return 1;");
        else if (info.is_empty_message())
            Body.pushln("return 0;");
        Body.pushln("}");
    }

    //C++ TO JAVA CONVERTER NOTE: This static local variable declaration (not allowed in Java) has been moved just prior to the method:
    static void generate_message_class_combine(gm_gpslib lib, gm_gps_beinfo info, gm_code_writer Body)
    {
        Body.pushln("@Override");
        Body.pushln("public void combine(byte[] _MQ, byte [] _tA) {");
        Body.pushln("//do nothing");

        Body.pushln("}");
    }

    void generate_message_class_details(gm_gps_beinfo info, gm_code_writer Body)
    {

        Body.pushln("// union of all message fields  ");
        gm_gps_communication_size_info size_info = info.get_max_communication_size();

        generate_message_fields_define(GMTYPE_T.GMTYPE_INT, size_info.num_int, Body);
        generate_message_fields_define(GMTYPE_T.GMTYPE_LONG, size_info.num_long, Body);
        generate_message_fields_define(GMTYPE_T.GMTYPE_FLOAT, size_info.num_float, Body);
        generate_message_fields_define(GMTYPE_T.GMTYPE_DOUBLE, size_info.num_double, Body);
        generate_message_fields_define(GMTYPE_T.GMTYPE_BOOL, size_info.num_bool, Body);
        Body.NL();

        GlobalMembersGm_gps_lib.generate_message_class_get_size(info, Body);
        GlobalMembersGm_gps_lib.generate_message_class_write(this, info, Body);
        generate_message_class_get_size_generate_message_class_read1(this, info, Body);
        generate_message_class_get_size_generate_message_class_read2(this, info, Body);
        generate_message_class_get_size_generate_message_class_read3(this, info, Body);
        generate_message_class_get_size_generate_message_class_combine(this, info, Body);
        Body.NL();
    }

    void generate_message_send(ast_foreach fe, gm_code_writer Body)
    {
        String temp = new String(new char[1024]);

        gm_gps_beinfo info = (gm_gps_beinfo) GlobalMembersGm_main.FE.get_current_backend_info();

        gm_gps_comm_t m_type = (fe == null) ? gm_gps_comm_t.GPS_COMM_INIT : gm_gps_comm_t.GPS_COMM_NESTED;

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
                String.format(temp, "if (%s.%s.length > 0) {", GlobalMembersGm_backend_gps.STATE_SHORT_CUT, GlobalMembersGm_backend_gps.GPS_REV_NODE_ID);
                Body.pushln(temp);
            }
            else
            {
                Body.pushln("if (getNeighborsSize() > 0) {");
            }
        }
        else
        {
            assert (fe != null) && (fe.get_iter_type() == GMTYPE_T.GMTYPE_NODEITER_NBRS);
            Body.pushln("for (Edge<EdgeData> _outEdge : getOutgoingEdges()) {");
            Body.pushln("// Sending messages to each neighbor");
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
                assert i != null;

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
            String fname = gm_gpslib.get_message_field_var_name(SYM.gm_type, SYM.idx);
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
                String.format(temp, "sendMessages(%s.%s, _msg);", GlobalMembersGm_backend_gps.STATE_SHORT_CUT, GlobalMembersGm_backend_gps.GPS_REV_NODE_ID);
                Body.pushln(temp);
            }
            else
            {
                Body.pushln("sendMessages(getNeighborIds(), _msg);");
            }
            Body.pushln("}");
        }
        else
        {
            Body.pushln("sendMessage(_outEdge.getNeighborId(), _msg);");
            if (sents_after_message.size() > 0)
            {
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

    //C++ TO JAVA CONVERTER NOTE: This static local variable declaration (not allowed in Java) has been moved just prior to the method:
    static boolean is_symbol_defined_in_bb(gm_gps_basic_block b, gm_symtab_entry e)
    {
        java.util.HashMap<gm_symtab_entry, gps_syminfo> SYMS = b.get_symbols();
        if ( ! SYMS.containsKey(e))
            return false;
        else
            return true;
    }

    void generate_message_receive_begin(ast_foreach fe, gm_code_writer Body, gm_gps_basic_block b, boolean is_only_comm)
    {
        gm_gps_beinfo info = (gm_gps_beinfo) GlobalMembersGm_main.FE.get_current_backend_info();
        gm_gps_comm_t comm_type = (fe == null) ? gm_gps_comm_t.GPS_COMM_INIT : gm_gps_comm_t.GPS_COMM_NESTED;
        gm_gps_comm_unit U = new gm_gps_comm_unit(comm_type, fe);
        generate_message_receive_begin(U, Body, b, is_only_comm);
    }
    void generate_message_receive_begin(ast_sentblock sb, gm_symtab_entry drv, gm_code_writer Body, gm_gps_basic_block b, boolean is_only_comm)
    {
        gm_gps_beinfo info = (gm_gps_beinfo) GlobalMembersGm_main.FE.get_current_backend_info();
        gm_gps_comm_t comm_type = gm_gps_comm_t.GPS_COMM_RANDOM_WRITE;
        gm_gps_comm_unit U = new gm_gps_comm_unit(comm_type, sb, drv);
        generate_message_receive_begin(U, Body, b, is_only_comm);
    }

    void generate_message_receive_begin(gm_gps_comm_unit U, gm_code_writer Body, gm_gps_basic_block b, boolean is_only_comm)
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
            if (e.getType().is_property() || e.getType().is_node_compatible() || e.getType().is_edge_compatible() || !generate_message_class_get_size_is_symbol_defined_in_bb(b, e))
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
            String fname = gm_gpslib.get_message_field_var_name(SYM.gm_type, SYM.idx);
            Body.push(fname);
            fname = null;
            Body.pushln(";");
        }
    }

    void generate_message_receive_end(gm_code_writer Body, boolean is_only_comm)
    {
        if (!is_only_comm)
        {
            Body.pushln("}");
        }
    }

    void generate_expr_nil(ast_expr e, gm_code_writer Body)
    {
        Body.push("(-1)");
    }

    void generate_expr_builtin(ast_expr_builtin be, gm_code_writer Body, boolean is_master)
    {
        gm_builtin_def def = be.get_builtin_def();
        java.util.LinkedList<ast_expr> ARGS = be.get_args();

        switch (def.get_method_id())
        {
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
                //Body.push("/*please check*/");
                Body.push("getGraphSize()");
                break;
            case GM_BLTIN_NODE_DEGREE:
                //Body.push("/*please check*/");
                Body.push("getNeighborsSize()");
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

    void generate_prepare_bb(gm_code_writer Body, gm_gps_basic_block bb)
    {
        String temp = new String(new char[1024]);

        if (bb.get_type() == gm_gps_bbtype_t.GM_GPS_BBTYPE_PREPARE1)
        {
            Body.pushln("// Preperation: creating reverse edges");
            String.format(temp, "%s %s = getId();", main.get_type_string(GMTYPE_T.GMTYPE_NODE), GlobalMembersGm_backend_gps.GPS_DUMMY_ID);
            Body.pushln(temp);

            generate_message_send(null, Body);

        }
        else if (bb.get_type() == gm_gps_bbtype_t.GM_GPS_BBTYPE_PREPARE2)
        {
            Body.pushln("//Preperation creating reverse edges");
            Body.pushln("int i = 0; // iterable does not have length(), so we have to count it");
            Body.pushln("for(MessageData _msg : _msgs) i++;");

            String.format(temp, "%s.%s = new %s[i];", GlobalMembersGm_backend_gps.STATE_SHORT_CUT, GlobalMembersGm_backend_gps.GPS_REV_NODE_ID, main.get_type_string(GMTYPE_T.GMTYPE_NODE));
            Body.pushln(temp);
            Body.NL();

            Body.pushln("i=0;");
            Body.pushln("for(MessageData _msg : _msgs) {");
            generate_message_receive_begin(null, Body, bb, true);
            String.format(temp, "%s.%s[i] = %s;", GlobalMembersGm_backend_gps.STATE_SHORT_CUT, GlobalMembersGm_backend_gps.GPS_REV_NODE_ID, GlobalMembersGm_backend_gps.GPS_DUMMY_ID);
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

    //-----------------------------------------------------------------------------

    boolean do_local_optimize()
    {
        String[] NAMES = { "[(nothing)]" };
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













