package backend_gps;

import java.util.HashMap;

import tangible.Pair;
import ast.ast_sent;
import ast.ast_sentblock;

import common.gm_method_id_t;

import frontend.gm_symtab_entry;

public class GlobalMembersGm_gps_opt_simplify_expr1 {
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
	// /#define GM_BLTIN_MUTATE_GROW 1
	// /#define GM_BLTIN_MUTATE_SHRINK 2
	// C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced
	// in-line:
	// /#define GM_BLTIN_FLAG_TRUE true

	// ----------------------------------------------------
	// Foreach(n:G.Nodes)
	// Foreach(t:n.Nbrs) {
	// LHS = .. n..Builtin() + ... + ...
	// }
	// ==>
	// Foreach(n:G.Nodes)
	// Foreach(t:n.Nbrs) {
	// TEMP = n.Builtin();
	// LHS = ... TEMP + ... + ...
	// }
	// ----------------------------------------------------

	public static boolean contains_built_in_through_driver(ast_sent s, gm_symtab_entry e) {
		gps_opt_check_contain_builtin_through_t T = new gps_opt_check_contain_builtin_through_t(e);
		s.traverse_pre(T);

		return T.has_it();
	}

	public static void replace_built_in(ast_sent s, gm_symtab_entry e, ast_sentblock scope,
			HashMap<Pair<ast_sentblock, gm_method_id_t>, gm_symtab_entry> already_defined_map) {
		gps_opt_replace_builtin_t T = new gps_opt_replace_builtin_t(e, scope, already_defined_map);
		s.traverse_post(T);

	}

}