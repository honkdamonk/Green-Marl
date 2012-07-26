package backend_gps;

import ast.AST_NODE_TYPE;
import ast.ast_assign;
import ast.ast_expr;
import ast.ast_field;
import ast.ast_foreach;
import ast.ast_id;
import ast.ast_if;
import ast.ast_sent;
import ast.ast_sentblock;
import inc.GMTYPE_T;
import inc.GM_OPS_T;

import common.GlobalMembersGm_main;
import common.GlobalMembersGm_new_sents_after_tc;
import common.GlobalMembersGm_transform_helper;
import common.gm_apply;

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

//-----------------------------------------------------
// replace random-write in sequential context
//-----------------------------------------------------

public class gm_gps_opt_remove_master_random_write_t extends gm_apply {

	public gm_gps_opt_remove_master_random_write_t() {
		set_for_sent(true);
		set_separate_post_apply(true);
		depth = 0;
	}

	// pre
	@Override
	public boolean apply(ast_sent s) {
		if (s.get_nodetype() == AST_NODE_TYPE.AST_FOREACH) {
			if (((ast_foreach) s).is_parallel())
				depth++;
		}

		if ((depth == 0) && (s.get_nodetype() == AST_NODE_TYPE.AST_ASSIGN)) {
			ast_assign a = (ast_assign) s;
			if (!a.is_target_scalar() && !a.is_reduce_assign()) {
				if (a.get_lhs_field().get_first().getTypeInfo().get_target_graph_sym() != null)
					targets.addLast(a);
			}
		}
		return true;
	}

	@Override
	public boolean apply2(ast_sent s) {
		if (s.get_nodetype() == AST_NODE_TYPE.AST_FOREACH) {
			if (((ast_foreach) s).is_parallel())
				depth--;
		}
		return true;
	}

	public final void post_process() {
		for (ast_assign a : targets) {
			GlobalMembersGm_transform_helper.gm_make_it_belong_to_sentblock(a);
			ast_sentblock sb = (ast_sentblock) a.get_parent();

			String name = GlobalMembersGm_main.FE.voca_temp_name_and_add("_t", null, true);
			ast_id id = ast_id.new_id(name, a.get_line(), a.get_col());
			ast_sentblock foreach_sb = ast_sentblock.new_sentblock();
			ast_foreach foreach_out = GlobalMembersGm_new_sents_after_tc.gm_new_foreach_after_tc(id, a.get_lhs_field().get_first().getTypeInfo()
					.get_target_graph_id().copy(true), foreach_sb, GMTYPE_T.GMTYPE_NODEITER_ALL);
			GlobalMembersGm_transform_helper.gm_add_sent_after(a, foreach_out);
			name = null;
			GlobalMembersGm_transform_helper.gm_ripoff_sent(a);

			ast_expr check = ast_expr.new_comp_expr(GM_OPS_T.GMOP_EQ, ast_expr.new_id_expr(foreach_out.get_iterator().copy(true)),
					ast_expr.new_id_expr(a.get_lhs_field().get_first().copy(true)));

			ast_field f = a.get_lhs_field();
			f.get_first().setSymInfo(foreach_out.get_iterator().getSymInfo());

			ast_if iff = ast_if.new_if(check, a, null);
			foreach_sb.add_sent(iff);
		}
	}

	private java.util.LinkedList<ast_assign> targets = new java.util.LinkedList<ast_assign>();
	private int depth;
}