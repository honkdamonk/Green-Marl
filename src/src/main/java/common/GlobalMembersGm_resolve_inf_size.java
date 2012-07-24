package common;

import inc.GMTYPE_T;

import java.util.LinkedList;

import ast.ast_expr;
import ast.ast_expr_builtin;
import ast.ast_expr_reduce;

public class GlobalMembersGm_resolve_inf_size {
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

	public static boolean gm_resolve_size_of_inf_expr(ast_expr e, GMTYPE_T dest_type) {
		if (e.get_type_summary().is_inf_type()) {
			e.set_type_summary(dest_type.get_sized_inf_type());
		} else if (e.get_type_summary().is_nil_type()) {
			if (dest_type.is_node_type()) {
				e.set_type_summary(GMTYPE_T.GMTYPE_NIL_NODE);
			} else if (dest_type.is_edge_type()) {
				e.set_type_summary(GMTYPE_T.GMTYPE_NIL_EDGE);
			} else {
				e.set_type_summary(GMTYPE_T.GMTYPE_NIL_NODE);
			}
		}

		switch (e.get_opclass()) {
		case GMEXPR_ID:
		case GMEXPR_FIELD:
		case GMEXPR_IVAL:
		case GMEXPR_FVAL:
		case GMEXPR_BVAL:
		case GMEXPR_INF:
		case GMEXPR_NIL:
			break;

		case GMEXPR_UOP:
		case GMEXPR_LUOP:
			if (e.get_type_summary().is_inf_type()) {
				GlobalMembersGm_resolve_inf_size.gm_resolve_size_of_inf_expr(e.get_left_op(), dest_type);
			} // type conversion
			else {
				GlobalMembersGm_resolve_inf_size.gm_resolve_size_of_inf_expr(e.get_left_op(), e.get_type_summary());
			}
			break;

		case GMEXPR_BIOP:
		case GMEXPR_LBIOP:
			GlobalMembersGm_resolve_inf_size.gm_resolve_size_of_inf_expr(e.get_left_op(), dest_type);
			GlobalMembersGm_resolve_inf_size.gm_resolve_size_of_inf_expr(e.get_right_op(), dest_type);
			break;
		case GMEXPR_TER:
			GlobalMembersGm_resolve_inf_size.gm_resolve_size_of_inf_expr(e.get_cond_op(), GMTYPE_T.GMTYPE_BOOL);
			GlobalMembersGm_resolve_inf_size.gm_resolve_size_of_inf_expr(e.get_left_op(), dest_type);
			GlobalMembersGm_resolve_inf_size.gm_resolve_size_of_inf_expr(e.get_right_op(), dest_type);
			break;

		case GMEXPR_REDUCE: {
			ast_expr_reduce r = (ast_expr_reduce) e;
			ast_expr f = r.get_filter();
			ast_expr b = r.get_body();
			if (f != null) {
				GlobalMembersGm_resolve_inf_size.gm_resolve_size_of_inf_expr(f, GMTYPE_T.GMTYPE_BOOL);
			}
			GlobalMembersGm_resolve_inf_size.gm_resolve_size_of_inf_expr(b, dest_type);
			break;
		}

		case GMEXPR_BUILTIN_FIELD:
		case GMEXPR_BUILTIN: {
			// for each argument check type check
			ast_expr_builtin r = (ast_expr_builtin) e;
			gm_builtin_def def = r.get_builtin_def();

			LinkedList<ast_expr> ARGS = r.get_args();
			int i = 0;
			for (ast_expr e_arg : ARGS) {
				GMTYPE_T arg_type = def.get_arg_type(i);
				GlobalMembersGm_resolve_inf_size.gm_resolve_size_of_inf_expr(e_arg, arg_type);
				i++;
			}
			break;
		}

		case GMEXPR_COMP: {
			// check left and right
			GMTYPE_T l_type = e.get_left_op().get_type_summary();
			GMTYPE_T r_type = e.get_right_op().get_type_summary();

			if (l_type.is_inf_type() && r_type.is_inf_type()) {
				l_type = GMTYPE_T.GMTYPE_INT;
				r_type = GMTYPE_T.GMTYPE_INT;
			} else if (l_type.is_inf_type()) {
				l_type = r_type;
			} else if (r_type.is_inf_type()) {
				r_type = l_type;
			}

			GlobalMembersGm_resolve_inf_size.gm_resolve_size_of_inf_expr(e.get_left_op(), l_type);
			GlobalMembersGm_resolve_inf_size.gm_resolve_size_of_inf_expr(e.get_right_op(), r_type);

			break;
		}
		case GMEXPR_FOREIGN: {
			break;

		}

		default:
			assert false;
			break;
		}

		return true;
	}
}