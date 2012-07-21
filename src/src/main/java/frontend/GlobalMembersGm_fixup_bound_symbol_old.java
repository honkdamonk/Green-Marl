import ast.ast_assign;
import ast.ast_expr;
import ast.ast_id;
import inc.GM_OPS_T;
import inc.GM_REDUCE_T;

public class GlobalMembersGm_fixup_bound_symbol_old
{

// used in later optimizations
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define TO_STR(X) #X
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define DEF_STRING(X) static const char *X = "X"
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define GM_COMPILE_STEP(CLASS, DESC) class CLASS : public gm_compile_step { private: CLASS() {set_description(DESC);}public: virtual void process(ast_procdef*p); virtual gm_compile_step* get_instance(){return new CLASS();} static gm_compile_step* get_factory(){return new CLASS();} };
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define GM_COMPILE_STEP_FACTORY(CLASS) CLASS::get_factory()

	public static void gm_make_normal_assign(ast_assign a)
	{
		//-----------------------------------
		// make it a normal assignment
		// LHS += <expr>
		// -->
		// LHS = LHS + <expr>
		//-----------------------------------
		assert a.is_reduce_assign();

		ast_expr base;
		if (a.is_target_scalar())
		{
			base = ast_expr.new_id_expr(a.get_lhs_scala().copy(true));
		}
		else
		{
			base = ast_expr.new_field_expr(a.get_lhs_field().copy(true));
		}

		ast_expr org_rhs = a.get_rhs();
		assert org_rhs != null;
		ast_expr new_rhs = null;
		switch (a.get_reduce_type())
		{
			case GMREDUCE_PLUS:
				new_rhs = ast_expr.new_biop_expr(GM_OPS_T.GMOP_ADD, base, org_rhs);
				break;
			case GMREDUCE_MULT:
				new_rhs = ast_expr.new_biop_expr(GM_OPS_T.GMOP_MULT, base, org_rhs);
				break;
			case GMREDUCE_MIN:
				new_rhs = ast_expr.new_biop_expr(GM_OPS_T.GMOP_MIN, base, org_rhs);
				break;
			case GMREDUCE_MAX:
				new_rhs = ast_expr.new_biop_expr(GM_OPS_T.GMOP_MAX, base, org_rhs);
				break;
			default:
				assert false;
				break;
		}

		a.set_rhs(new_rhs);
		a.set_assign_type(gm_assignment_t.GMASSIGN_NORMAL);
		a.set_reduce_type(GM_REDUCE_T.GMREDUCE_NULL);
		ast_id old_iter = a.get_bound(); //assert(old_iter != NULL);
		a.set_bound(null);
		if (old_iter != null)
		old_iter.dispose(); // can be null but delete is no harmful
	}
}