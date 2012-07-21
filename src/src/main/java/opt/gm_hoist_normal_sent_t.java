package opt;

import ast.AST_NODE_TYPE;
import ast.ast_assign;
import ast.ast_sent;
import ast.ast_sentblock;
import frontend.GlobalMembersGm_rw_analysis_check2;

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

//------------------------------------------------------
// hoist up target-type sentence as far as possible 
// (in the same sent block)
// target-type should not be a var-decl
//------------------------------------------------------
public abstract class gm_hoist_normal_sent_t extends gm_apply
{
	@Override
	public boolean apply(ast_sent s)
	{
		if (s.get_nodetype() != AST_NODE_TYPE.AST_SENTBLOCK)
			return true;

		ast_sentblock sb = (ast_sentblock) s;

		java.util.LinkedList<ast_sent> sents = sb.get_sents(); // make a copy of sentence list (right?)
		java.util.Iterator<ast_sent> i_out;
		for (i_out = sents.iterator(); i_out.hasNext();)
		{
			//--------------------------------------
			// find target assign sentence
			//--------------------------------------
			ast_sent target = i_out.next();
			if (!check_target((ast_assign) target))
				continue;

			java.util.LinkedList<ast_sent> sents2 = sb.get_sents(); // most up-to-date list
			java.util.LinkedList<ast_sent> stack = new java.util.LinkedList<ast_sent>();
			ast_sent top_position = null;

			//--------------------------------------
			// now find the possible upmost position
			//--------------------------------------
			java.util.Iterator<ast_sent> i_in;
			for (i_in = sents2.iterator(); i_in.hasNext();)
			{
				ast_sent S = i_in.next();
				if (S == target)
					break;
				if (stack.size() == 0)
				{
					// does not need to add into the queue
					if (check_trivial_pred(S))
					{
						top_position = S;
						continue;
					}
				}
				stack.addFirst(S);
			}

			//------------------------------------------------------------------------
			// stack contains list of sentences that have to check
			// top_position points the end of sentences that should precede target.
			//
			// Check dependency
			//------------------------------------------------------------------------
			while (stack.size() > 0)
			{
				ast_sent S = stack.getFirst();
				stack.removeFirst();
				if (GlobalMembersGm_rw_analysis_check2.gm_has_dependency(S, target))
				{
					top_position = S;
					break;
				}
			}

			GlobalMembersGm_transform_helper.gm_ripoff_sent(target, GlobalMembersGm_transform_helper.GM_NOFIX_SYMTAB);
			if (top_position == null) // move it to the beginning
			{
				GlobalMembersGm_transform_helper.gm_insert_sent_begin_of_sb(sb, target, GlobalMembersGm_transform_helper.GM_NOFIX_SYMTAB);
			} // move to the top
			else
			{
				GlobalMembersGm_transform_helper.gm_add_sent_after(top_position, target, GlobalMembersGm_transform_helper.GM_NOFIX_SYMTAB);
			}
		}
		return true;
	}

	protected abstract boolean check_target(ast_sent s); // if s is a sentence, you want to hoist up
	protected abstract boolean check_trivial_pred(ast_sent p); // if p is a trivial precedessor to your target
}