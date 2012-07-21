package opt;

import ast.ast_assign;
import ast.ast_expr;
import ast.ast_expr_reduce;
import ast.ast_sent;
import ast.ast_sentblock;
import inc.GMTYPE_T;
import inc.GM_OPS_T;
import inc.GM_REDUCE_T;
import inc.GlobalMembersGm_defs;
import inc.gm_assignment_t;

import common.GlobalMembersGm_add_symbol;
import common.GlobalMembersGm_transform_helper;
import common.gm_method_id_t;

import frontend.gm_symtab_entry;

public class GlobalMembersGm_syntax_sugar2
{
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

	//----------------------------------------------------
	// syntax sugar elimination (after type resolution)
	//   Reduce op(e.g. Sum) --> initialization + foreach + reduce assign(e.g. +=)
	//----------------------------------------------------

	//====================================================================
	// static functions in this file
	public static gm_symtab_entry insert_def_and_init_before(String vname, int prim_type, ast_sent curr, ast_expr default_val)
	{
		//-------------------------------------------------------------
		//assumption:
		//  A. vname does not conflict upward or downward
		//  B. default_val has well ripped-off. (i.e. top scope is null)
		//  C. default_val has correct type_summary
		//-------------------------------------------------------------
		assert GlobalMembersGm_defs.gm_is_prim_type(prim_type);

		//-------------------------------------------------------------
		// 1. find enclosing sentence block
		//-------------------------------------------------------------
		GlobalMembersGm_transform_helper.gm_make_it_belong_to_sentblock(curr);
		ast_sentblock sb = (ast_sentblock) curr.get_parent();

		//-------------------------------------------------------------
		// 2. Add new symbol to the current bound
		//-------------------------------------------------------------
		gm_symtab_entry e = GlobalMembersGm_add_symbol.gm_add_new_symbol_primtype(sb, prim_type, (String) vname);

		//-------------------------------------------------------------
		// 3. add initialization sentence
		//-------------------------------------------------------------
		if (default_val != null)
		{
			//prinf("def_val = %p, new_id = %p\n", default_val, new_id);
			//assert(gm_is_compatible_type_for_assign(prim_type, default_val->get_type_summary()));
			ast_assign init_a = ast_assign.new_assign_scala(e.getId().copy(true), default_val, gm_assignment_t.GMASSIGN_NORMAL);
			GlobalMembersGm_transform_helper.gm_add_sent_before(curr, init_a);
		}

		return e;
	}
	public static void replace_avg_to_varaible(ast_sent s, ast_expr rhs, gm_symtab_entry e)
	{
		replace_avg_to_varaible_t T = new replace_avg_to_varaible_t(rhs, e);
		GlobalMembersGm_transform_helper.gm_replace_expr_general(s, T);
	}
	//static void mark_

	public static String OPT_FLAG_NESTED_REDUCTION = "X";
	public static String OPT_SYM_NESTED_REDUCTION_TARGET = "X";
	public static String OPT_SYM_NESTED_REDUCTION_BOUND = "X";
	public static String OPT_SB_NESTED_REDUCTION_SCOPE = "X";

	public static int find_count_function(int source_type, int iter_type)
	{
		if (GlobalMembersGm_defs.gm_is_graph_type(source_type))
		{
			if (GlobalMembersGm_defs.gm_is_all_graph_node_iter_type(iter_type))
			{
				return gm_method_id_t.GM_BLTIN_GRAPH_NUM_NODES;
			}
			else if (GlobalMembersGm_defs.gm_is_all_graph_node_iter_type(iter_type))
			{
				return gm_method_id_t.GM_BLTIN_GRAPH_NUM_EDGES;
			}
		}
		else if (GlobalMembersGm_defs.gm_is_node_compatible_type(source_type))
		{
			if (iter_type == GMTYPE_T.GMTYPE_NODEITER_IN_NBRS.getValue())
			{
				return gm_method_id_t.GM_BLTIN_NODE_IN_DEGREE;
			}
			else if (iter_type == GMTYPE_T.GMTYPE_NODEITER_NBRS.getValue())
			{
				return gm_method_id_t.GM_BLTIN_NODE_DEGREE;
			}
		}
		else if (GlobalMembersGm_defs.gm_is_collection_type(source_type))
		{
			if (GlobalMembersGm_defs.gm_is_collection_iter_type(iter_type))
				return gm_method_id_t.GM_BLTIN_SET_SIZE;
		}

		return gm_method_id_t.GM_BLTIN_END;
	}

	//------------------------------------------------------------------------------
	// Optimization for nested reduction 
	// _S  = Sum(x) { Sum(y) { Sum(z){ Sum(w){a} 
	//                                 * b
	//                               } 
	//                       }
	//                +c  
	//              }
	//  X : has_nested: yes    (because rhs = Sum(Y) + c)
	//      is_nested: no      
	//      has_other_rhs: yes (becuase of + c)
	//
	//  Y : has_nested: yes    (becuase rhs = Sum(z))
	//      is_nested:  yes    (because inside Sum (x) )     
	//      has_other_rhs: no  (becuase rhs = Sum(z) only)
	//
	//  Z : has_nested: no     (because rhs = Sum(a)*b)
	//      is_nested:  yes    (because inside Sum(y) 
	//      has_other_rhs: N/A
	//
	//  W : has_nested: no
	//      is_nested: no
	//      has_other_rhs: N/A
	//
	// ==> result
	//
	//  _S0 = 0;
	//  Foreach(x) { 
	//      Foreach(y) {
	//          Foreach(z) {
	//             _S1 = 0;
	//             Foreach(w) {
	//                _S1 +=  a;
	//             }
	//             _S0 += _S1*b;  @x// 
	//          }
	//      }
	//      _S0 += c;  @x
	//  }  

	public static boolean check_is_reduce_op(int rtype, int op)
	{
		if ((rtype == GM_REDUCE_T.GMREDUCE_PLUS.getValue()) && (op == GM_OPS_T.GMOP_ADD.getValue()))
			return true;
		else if ((rtype == GM_REDUCE_T.GMREDUCE_MULT.getValue()) && (op == GM_OPS_T.GMOP_MULT.getValue()))
			return true;
		else if ((rtype == GM_REDUCE_T.GMREDUCE_MIN.getValue()) && (op == GM_OPS_T.GMOP_MIN.getValue()))
			return true;
		else if ((rtype == GM_REDUCE_T.GMREDUCE_MAX.getValue()) && (op == GM_OPS_T.GMOP_MAX.getValue()))
			return true;
		else if ((rtype == GM_REDUCE_T.GMREDUCE_OR.getValue()) && (op == GM_OPS_T.GMOP_OR.getValue()))
			return true;
		else if ((rtype == GM_REDUCE_T.GMREDUCE_AND.getValue()) && (op == GM_OPS_T.GMOP_AND.getValue()))
			return true;
		return false;
	}

	public static boolean check_has_nested(ast_expr body, int rtype, tangible.RefObject<Boolean> has_other_rhs, ast_expr_reduce b1, ast_expr_reduce b2)
	{
		if (rtype == GM_REDUCE_T.GMREDUCE_AVG.getValue())
			return false;

		//---------------------------------
		// case 1
		//    SUM ( SUM)
		// case 2
		//    SUM ( SUM + SUM)
		// case 3
		//    SUM ( SUM + alpha)
		//---------------------------------
		has_other_rhs.argvalue = false;
		b1 = null;
		b2 = null;
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define CHECK_SAME_REDUCTION(expr, rtype) ((expr)->is_reduction() && (((ast_expr_reduce*)(expr))->get_reduce_type() == rtype))
		if (((body).is_reduction() && (((ast_expr_reduce)(body)).get_reduce_type() == rtype)))
		{
			b1 = (ast_expr_reduce) body;
			return true;
		}
		else if (body.is_biop())
		{
			int op = body.get_optype();
			if (GlobalMembersGm_syntax_sugar2.check_is_reduce_op(rtype, op))
			{
				// check each argument
				if (((body.get_left_op()).is_reduction() && (((ast_expr_reduce)(body.get_left_op())).get_reduce_type() == rtype)))
				{
					b1 = (ast_expr_reduce) body.get_left_op();
				}
				if (((body.get_right_op()).is_reduction() && (((ast_expr_reduce)(body.get_right_op())).get_reduce_type() == rtype)))
				{
					b2 = (ast_expr_reduce) body.get_right_op();
				}

				boolean is_nested = (b1 != null) || (b2 != null);
				has_other_rhs.argvalue = is_nested && ((b1 == null) || (b2 == null));

				return is_nested;
			}

			return false;
		}

		return false;
	}
}