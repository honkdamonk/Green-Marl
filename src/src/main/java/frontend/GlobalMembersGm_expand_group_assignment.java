package frontend;

import inc.GMTYPE_T;
import ast.ast_assign;
import ast.ast_field;
import ast.ast_foreach;
import ast.ast_id;

import common.GlobalMembersGm_main;
import common.GlobalMembersGm_new_sents_after_tc;

public class GlobalMembersGm_expand_group_assignment {
	// C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced
	// in-line:
	// /#define TO_STR(X) #X
	// C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced
	// in-line:
	// /#define DEF_STRING(X) static const char *X = "X"
	// C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced
	// in-line:
	// /#define GM_COMPILE_STEP(CLASS, DESC) class CLASS : public
	// gm_compile_step { private: CLASS() {set_description(DESC);}public:
	// virtual void process(ast_procdef*p); virtual gm_compile_step*
	// get_instance(){return new CLASS();} static gm_compile_step*
	// get_factory(){return new CLASS();} };
	// C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced
	// in-line:
	// /#define GM_COMPILE_STEP_FACTORY(CLASS) CLASS::get_factory()
	// C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced
	// in-line:
	// /#define AUX_INFO(X,Y) "X"":""Y"
	// /#define GM_BLTIN_MUTATE_GROW 1
	// /#define GM_BLTIN_MUTATE_SHRINK 2
	// C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced
	// in-line:
	// /#define GM_BLTIN_FLAG_TRUE true
	// ====================================================================
	// syntax sugar elimination (after type resolution)
	// ---------------------------------------------------
	// Group assignment -> foreach
	// e.g.>
	// G.A = G.B + 1;
	// =>
	// Foreach(_t:G.Nodes)
	// _t.A = _t.B + 1;
	// ====================================================================

	public static ast_foreach create_surrounding_fe(ast_assign a) {
		ast_field lhs = a.get_lhs_field(); // G.A
		ast_id first = lhs.get_first();
		ast_id second = lhs.get_second();

		// iterator : temp
		// source : graph
		// iter-type all nodes or all edges
		// body : assignment statement
		// const char* temp_name =
		// TEMP_GEN.getTempName("t"); // should I use first->get_orgname())?
		String temp_name = GlobalMembersGm_main.FE.voca_temp_name("t");
		ast_id it = ast_id.new_id(temp_name, first.get_line(), first.get_col());
		ast_id src = first.copy(true);
		src.set_line(first.get_line());
		src.set_col(first.get_col());
		GMTYPE_T iter;
		if (second.getTypeSummary().is_node_property_type())
			iter = GMTYPE_T.GMTYPE_NODEITER_ALL;
		else if (second.getTypeSummary().is_edge_property_type())
			iter = GMTYPE_T.GMTYPE_EDGEITER_ALL;
		else {
			assert false;
			throw new AssertionError();
		}

		ast_foreach fe_new = GlobalMembersGm_new_sents_after_tc.gm_new_foreach_after_tc(it, src, a, iter);

		return fe_new;
	}
}