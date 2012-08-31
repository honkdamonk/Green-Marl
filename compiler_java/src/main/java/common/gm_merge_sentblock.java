package common;

import ast.AST_NODE_TYPE;
import ast.ast_sent;
import ast.ast_sentblock;
import frontend.gm_symtab;

public class gm_merge_sentblock
{
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define TO_STR(X) #X
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define DEF_STRING(X) static const char *X = "X"
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define GM_COMPILE_STEP(CLASS, DESC) class CLASS : public gm_compile_step { private: CLASS() {set_description(DESC);}public: virtual void process(ast_procdef*p); virtual gm_compile_step* get_instance(){return new CLASS();} static gm_compile_step* get_factory(){return new CLASS();} };
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define GM_COMPILE_STEP_FACTORY(CLASS) CLASS::get_factory()

	//---------------------------------------------------------
	// Merge two sentence blocks (P, Q) into one. (P)
	// [Assumption: the two sentblocks are siblings]
	//   - All the sentences in Q is moved to P.
	//   - Name conflicts are resolved ==> should not happen (see handle_vardecl)
	//   - Symbol table is kept valid.
	//   - (RW analysis is re-done here?)
	//---------------------------------------------------------
	
	public static void gm_merge_sentblock(ast_sentblock P, ast_sentblock Q)
	{
		gm_merge_sentblock(P, Q, false);
	}
	
	public static void gm_merge_sentblock(ast_sentblock P, ast_sentblock Q, boolean delete_Q_after)
	{
		//(assumption) type-checking is already done
		//(assumption) var-decl has been hoisted up.

		assert P != null;
		assert Q != null;

		// 1. resolve name conflict in each direction
		//resolve_name_conflict(P,Q);
		//resolve_name_conflict(Q,P);

		// 2. merge symbol tables
		gm_symtab V = P.get_symtab_var();
		gm_symtab F = P.get_symtab_field();
		gm_symtab R = P.get_symtab_proc();
		V.merge(Q.get_symtab_var());
		F.merge(Q.get_symtab_field());
		R.merge(Q.get_symtab_proc());

		// 3. move sentence one-by-one
		// (3.1) keep decls at the highest position
		// (3.2) other sentneces at the bottem
		ast_sent anchor = null;
		java.util.LinkedList<ast_sent> Ps = P.get_sents();
		for (ast_sent s : Ps)
		{
			if (s.get_nodetype() != AST_NODE_TYPE.AST_VARDECL) // stop at the first non-decl sentence
				break;
			anchor = s;
		}

		java.util.LinkedList<ast_sent> Qs = Q.get_sents(); // work on a copy
		for (ast_sent s : Qs)
		{
			gm_transform_helper.gm_ripoff_sent(s);
			if (s.get_nodetype() == AST_NODE_TYPE.AST_VARDECL)
			{
				if (anchor == null)
					gm_transform_helper.gm_insert_sent_begin_of_sb(P, s);
				else
					gm_transform_helper.gm_add_sent_after(anchor, s);

				anchor = s;
			}
			else
			{
				gm_transform_helper.gm_insert_sent_end_of_sb(P, s);
			}
		}

		if (delete_Q_after)
			if (Q != null)
			Q.dispose();
	}
}