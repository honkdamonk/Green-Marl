package opt;

import ast.AST_NODE_TYPE;
import ast.ast_assign;
import ast.ast_expr;
import ast.ast_field;
import ast.ast_foreach;
import ast.ast_id;
import ast.ast_sent;
import ast.ast_sentblock;
import ast.ast_typedecl;
import backend_cpp.*;
import backend_giraph.*;
import common.*;
import frontend.*;
import inc.*;
import tangible.*;

public class Replace_PropertyItarator_With_NodeIterator extends gm_apply
{


	public Replace_PropertyItarator_With_NodeIterator()
	{
		this.newIteratorName = null;
		this.oldIteratorName = null;
		this.fe = null;
		this.iterType = 0;
		set_for_sent(true);
	}

	@Override
	public boolean apply(ast_sent sent)
	{
		if (sent.get_nodetype() != AST_NODE_TYPE.AST_FOREACH)
			return true;
		fe = (ast_foreach) sent;
		if (!GlobalMembersGm_defs.gm_is_property_iter_type(fe.get_iter_type()))
			return true;
		else
			return changeForeach();
	}


	private String newIteratorName;
	private String oldIteratorName;
	private ast_foreach fe;
	private int iterType;

	//For(s: prop.Items) -> For(n: G.Nodes) {Set s = n.prop
	private boolean changeForeach()
	{

		ast_id newIterator = getNewIterator();
		assert newIterator != null;

		ast_sent newBody = getNewBody();
		assert newBody != null;

		assemble(newIterator, newBody);

		return true;
	}

	private ast_id getNewIterator()
	{

		ast_id oldIterator = fe.get_iterator();
		oldIteratorName = oldIterator.get_genname();
		newIteratorName = getUniqueName();
		ast_id newIterator = ast_id.new_id(newIteratorName, 0, 0);

		iterType = getNewIterType();
		ast_id sourceGraph = fe.get_source().getTargetTypeInfo().get_target_graph_id();
		ast_typedecl type = ast_typedecl.new_nodeedge_iterator(sourceGraph.copy(true), iterType);
		if (!declare_symbol(fe.get_symtab_var(), newIterator, type, GlobalMembersGm_typecheck.GM_READ_AVAILABLE, GlobalMembersGm_typecheck.GM_WRITE_NOT_AVAILABLE))
			assert false;

		return newIterator;
	}

	private int getNewIterType()
	{
		int sourceType = fe.get_source().getTypeSummary();
		if (GlobalMembersGm_defs.gm_is_node_property_type(sourceType))
			return GMTYPE_T.GMTYPE_NODEITER_ALL;
		else if (GlobalMembersGm_defs.gm_is_edge_property_type(sourceType))
			return GMTYPE_T.GMTYPE_EDGEITER_ALL;
		else
		{
			assert false;
			return 0;
		}
	}

	private ast_sent getNewBody()
	{

		ast_sentblock newBody;
		if (fe.get_body().get_nodetype() == AST_NODE_TYPE.AST_SENTBLOCK)
			newBody = (ast_sentblock) fe.get_body();
		else
			newBody = ast_sentblock.new_sentblock();

		ast_assign assign = createAssignStatement();
		java.util.LinkedList<ast_sent> statements = newBody.get_sents();
		statements.addFirst(assign);

		return newBody;
	}

	private ast_assign createAssignStatement()
	{
		ast_id leftHandSide = createLeftHandSide();
		ast_expr rightHandSide = createRightHandSide();
		return ast_assign.new_assign_scala(leftHandSide, rightHandSide);
	}

	private ast_id createLeftHandSide()
	{
		ast_id leftHandSide = ast_id.new_id(oldIteratorName, 0, 0);
		find_and_connect_symbol(leftHandSide, fe.get_symtab_var());
		leftHandSide.set_instant_assigned(true);

		ast_typedecl type = ast_typedecl.new_set(leftHandSide, getTypeOfSourceItems());
		gm_symtab_entry fakeEntry = new gm_symtab_entry(leftHandSide.copy(), type);
		leftHandSide.setSymInfo(fakeEntry);

		return leftHandSide;
	}

	private int getTypeOfSourceItems()
	{
		ast_id source = fe.get_source();
		assert GlobalMembersGm_defs.gm_is_collection_type(source.getTargetTypeSummary());
		return source.getTargetTypeSummary();
	}

	private ast_expr createRightHandSide()
	{
		ast_id first = ast_id.new_id(newIteratorName, 0, 0);
		find_and_connect_symbol(first, fe.get_symtab_var());

		ast_id second = ast_id.new_id(fe.get_source().get_genname(), 0, 0);
		find_and_connect_symbol(second, fe.get_symtab_field());

		ast_field field = ast_field.new_field(first, second);
		return ast_expr.new_field_expr(field);
	}

	private void assemble(ast_id newIterator, ast_sent newBody)
	{
		fe.get_iterator() = null;
		fe.set_iterator(newIterator);
		fe.set_body(newBody);

		fe.set_iter_type(iterType);
	}

	private static String getUniqueName()
	{
		return GlobalMembersGm_main.FE.voca_temp_name_and_add("iter_aux", "");
	}

	private static boolean declare_symbol(gm_symtab SYM, ast_id id, ast_typedecl type, boolean is_readable, boolean is_writeable)
	{

		gm_symtab_entry old_e;
		boolean is_okay = SYM.check_duplicate_and_add_symbol(id, type, old_e, is_readable, is_writeable);
		if (!is_okay)
			GlobalMembersGm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_DUPLICATE, id, old_e.getId());

		find_and_connect_symbol(id, SYM);

		if (is_okay)
			GlobalMembersGm_main.FE.voca_add(id.get_orgname());

		return is_okay;
	}

	private static boolean find_and_connect_symbol(ast_id id, gm_symtab begin)
	{
		assert id != null;
		assert id.get_orgname() != null;

		gm_symtab_entry se = begin.find_symbol(id);
		if (se == null)
		{
			GlobalMembersGm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_UNDEFINED, id);
			return false;
		}

		if (id.getSymInfo() != null)
			assert id.getSymInfo() == se;
		else
			id.setSymInfo(se);

		return true;
	}
}