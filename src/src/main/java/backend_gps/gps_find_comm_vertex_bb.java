package backend_gps;

import ast.AST_NODE_TYPE;
import ast.ast_assign;
import ast.ast_field;
import ast.ast_foreach;
import ast.ast_sent;
import ast.ast_sentblock;
import frontend.gm_symtab_entry;
import inc.GlobalMembersGm_backend_gps;
import inc.gps_apply_bb_ast;

//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define TO_STR(X) #X
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define DEF_STRING(X) static const char *X = "X"
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP(CLASS, DESC) class CLASS : public gm_compile_step { private: CLASS() {set_description(DESC);}public: virtual void process(ast_procdef*p); virtual gm_compile_step* get_instance(){return new CLASS();} static gm_compile_step* get_factory(){return new CLASS();} };
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP_FACTORY(CLASS) CLASS::get_factory()


//------------------------------------------------------------------
// Split Basic Blocks
//     - Find BB that contains communication, and add into a list
//     - Mark communication foreach statement (assign ID)
//------------------------------------------------------------------
public class gps_find_comm_vertex_bb extends gps_apply_bb_ast
{

	public gps_find_comm_vertex_bb(gm_gps_beinfo g)
	{
		gen = g;
		set_for_sent(true);
		current_outer_loop = null;
	}

	@Override
	public boolean apply(ast_sent s)
	{
		// receiver should be empty.
		assert is_under_receiver_traverse() == false;

		gm_gps_basic_block curr = get_curr_BB();

		// only look at vertex BB
		if (!curr.is_vertex())
			return true;

		// neighborhood looking foreach statement is a communicating bb
		if (s.get_nodetype() == AST_NODE_TYPE.AST_FOREACH)
		{
			ast_foreach fe = (ast_foreach) s;
			if (fe.find_info_bool(GlobalMembersGm_backend_gps.GPS_FLAG_IS_OUTER_LOOP))
			{
				current_outer_loop = fe;

			}
			else if (fe.find_info_bool(GlobalMembersGm_backend_gps.GPS_FLAG_IS_INNER_LOOP))
			{

				gen.add_communication_unit_nested(fe); // adding inner loop

				// add the foreach loop as 'receiver' of this state, temporariliy.
				// (Receiver loop will be moved to the 'next' state, after split)
				curr.add_nested_receiver(fe);

				// list of bbs that should be splited
				target_bb.add(curr);

				// mark current outer loop to have communication
				assert current_outer_loop != null;
				current_outer_loop.add_info_bool(GlobalMembersGm_backend_gps.GPS_FLAG_HAS_COMMUNICATION, true);
			}
			else
			{
				assert false;
			}

			//curr->set_has_sender(true);

		}
		else if (s.get_nodetype() == AST_NODE_TYPE.AST_ASSIGN)
		{
			if (s.find_info_ptr(GlobalMembersGm_backend_gps.GPS_FLAG_SENT_BLOCK_FOR_RANDOM_WRITE_ASSIGN) != null)
			{
				ast_assign a = (ast_assign) s;
				ast_sentblock sb = (ast_sentblock)(s.find_info_ptr(GlobalMembersGm_backend_gps.GPS_FLAG_SENT_BLOCK_FOR_RANDOM_WRITE_ASSIGN));
				ast_field f = a.get_lhs_field();
				gm_symtab_entry sym = f.get_first().getSymInfo();

				gen.add_communication_unit_random_write(sb, sym);
				gen.add_random_write_sent(sb, sym, s);
				curr.add_random_write_receiver(sb, sym);

				target_bb.add(curr);

				assert current_outer_loop != null;
				current_outer_loop.add_info_bool(GlobalMembersGm_backend_gps.GPS_FLAG_HAS_COMMUNICATION_RANDOM, true);
			}
		}

		return true;
	}

	public final java.util.HashSet<gm_gps_basic_block> get_target_basic_blocks()
	{
		return target_bb;
	}

	private gm_gps_beinfo gen;
	private java.util.HashSet<gm_gps_basic_block> target_bb = new java.util.HashSet<gm_gps_basic_block>();
	private ast_foreach current_outer_loop;

}