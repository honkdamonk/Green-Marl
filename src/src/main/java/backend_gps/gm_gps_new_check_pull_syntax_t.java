package backend_gps;

import ast.AST_NODE_TYPE;
import ast.ast_assign;
import ast.ast_field;
import ast.ast_id;
import ast.ast_node;
import ast.ast_sent;
import frontend.gm_symtab_entry;
import inc.GlobalMembersGm_backend_gps;

import common.GM_ERRORS_AND_WARNINGS;
import common.GlobalMembersGm_error;
import common.gm_apply;

//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define TO_STR(X) #X
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define DEF_STRING(X) static const char *X = "X"
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP(CLASS, DESC) class CLASS : public gm_compile_step { private: CLASS() {set_description(DESC);}public: virtual void process(ast_procdef*p); virtual gm_compile_step* get_instance(){return new CLASS();} static gm_compile_step* get_factory(){return new CLASS();} };
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP_FACTORY(CLASS) CLASS::get_factory()

//------------------------------------------------------------------------
//   Check if there is 'pulling' syntax
//          Foreach (s: G.Nodes)
//            Foreach(t: s.Nbrs)
//                s.A = .... ; // error PULL  
//------------------------------------------------------------------------
public class gm_gps_new_check_pull_syntax_t extends gm_apply {
	public gm_gps_new_check_pull_syntax_t() {
		set_for_sent(true);
		_error = false;

	}

	// write to OUT_SCOPE in INNER_LOOP is an error
	public final boolean apply(ast_sent s) {
		if (s.get_nodetype() == AST_NODE_TYPE.AST_ASSIGN) {
			ast_assign a = (ast_assign) s;
			int context = s.find_info_int(GlobalMembersGm_backend_gps.GPS_INT_SYNTAX_CONTEXT);
			gm_gps_new_scope_analysis_t scope;
			if (a.is_target_scalar()) {
				scope = get_scope_from_id(a.get_lhs_scala().getSymInfo());
			} else {
				scope = get_scope_from_driver(a.get_lhs_field().get_first().getSymInfo());
			}

			if (a.has_lhs_list()) {
				for (ast_node n : a.get_lhs_list()) {
					gm_gps_new_scope_analysis_t scope2;
					if (n.get_nodetype() == AST_NODE_TYPE.AST_ID) {
						scope2 = get_scope_from_id(((ast_id) n).getSymInfo());
					} else {
						scope2 = get_scope_from_driver(((ast_field) n).get_first().getSymInfo());
					}

					assert scope == scope2;
				}
			}

			// writing to out-scope inside inner-loop.
			if ((context == gm_gps_new_scope_analysis_t.GPS_NEW_SCOPE_IN.getValue()) && (scope == gm_gps_new_scope_analysis_t.GPS_NEW_SCOPE_OUT)) {
				GlobalMembersGm_error.gm_backend_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_GPS_PULL_SYNTAX, s.get_line(), s.get_col());
				_error = true;
			}
		} else if (s.get_nodetype() == AST_NODE_TYPE.AST_CALL) {
			assert false;
		} else if (s.get_nodetype() == AST_NODE_TYPE.AST_FOREIGN) {
			assert false;
			// should check out-scope is modified
		}
		return true;
	}

	public final boolean is_error() {
		return _error;
	}

	private boolean _error;

	private gm_gps_new_scope_analysis_t get_scope_from_id(gm_symtab_entry e) {
		return gm_gps_new_scope_analysis_t.forValue(e.find_info_int(GlobalMembersGm_backend_gps.GPS_INT_SYMBOL_SCOPE));
	}

	private gm_gps_new_scope_analysis_t get_scope_from_driver(gm_symtab_entry e) {
		return get_scope_from_driver(e, false);
	}

	// C++ TO JAVA CONVERTER NOTE: Java does not allow default values for
	// parameters. Overloaded methods are inserted above.
	// ORIGINAL LINE: int get_scope_from_driver(gm_symtab_entry* e, boolean
	// is_rarrow = false)
	private gm_gps_new_scope_analysis_t get_scope_from_driver(gm_symtab_entry e, boolean is_rarrow) {
		if (e.find_info_bool(GlobalMembersGm_backend_gps.GPS_FLAG_IS_INNER_LOOP))
			return gm_gps_new_scope_analysis_t.GPS_NEW_SCOPE_IN;
		else if (e.find_info_bool(GlobalMembersGm_backend_gps.GPS_FLAG_IS_OUTER_LOOP))
			return gm_gps_new_scope_analysis_t.GPS_NEW_SCOPE_OUT;
		else if (e.find_info_bool(GlobalMembersGm_backend_gps.GPS_FLAG_IS_EDGE_ITERATOR))
			return gm_gps_new_scope_analysis_t.GPS_NEW_SCOPE_EDGE;
		else
			return gm_gps_new_scope_analysis_t.GPS_NEW_SCOPE_RANDOM;
	}
}