package frontend;

import ast.AST_NODE_TYPE;
import ast.ast_procdef;
import ast.ast_sent;
import ast.ast_sentblock;
import ast.ast_vardecl;

import common.GlobalMembersGm_transform_helper;
import common.GlobalMembersGm_traverse;
import common.gm_apply;

//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define TO_STR(X) #X
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define DEF_STRING(X) static const char *X = "X"
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP(CLASS, DESC) class CLASS : public gm_compile_step { private: CLASS() {set_description(DESC);}public: virtual void process(ast_procdef*p); virtual gm_compile_step* get_instance(){return new CLASS();} static gm_compile_step* get_factory(){return new CLASS();} };
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP_FACTORY(CLASS) CLASS::get_factory()

public class remove_vardecl_t extends gm_apply
{
	// POST Apply
	@Override
	public boolean apply(ast_sent b)
	{
		if (b.get_nodetype() != AST_NODE_TYPE.AST_SENTBLOCK)
			return true;
		ast_sentblock sb = (ast_sentblock) b;
		java.util.LinkedList<ast_sent> sents = sb.get_sents(); // need a copy
		java.util.LinkedList<ast_sent> stack = new java.util.LinkedList<ast_sent>();

		//--------------------------------------------
		// 1. find all var-decls
		// 3. delete var-decl
		//--------------------------------------------
		for (ast_sent z : sents)
		{
			if (z.get_nodetype() != AST_NODE_TYPE.AST_VARDECL)
				continue;
			ast_vardecl v = (ast_vardecl) z;

			stack.addLast(v);
		}

		// 3. delete var-decl
		for (ast_sent z : stack)
		{
			// now delete
			GlobalMembersGm_transform_helper.gm_ripoff_sent(z, false);
			if (z != null)
				z.dispose();
		}
		return true;
	}

	public final void do_removal(ast_procdef p)
	{
		set_all(false);
		set_for_sent(true);
		GlobalMembersGm_traverse.gm_traverse_sents(p, this, GlobalMembersGm_traverse.GM_POST_APPLY);
	}
}