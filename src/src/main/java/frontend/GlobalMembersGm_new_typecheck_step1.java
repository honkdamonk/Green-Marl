package frontend;

import inc.GMTYPE_T;
import ast.ast_id;
import ast.ast_typedecl;

import common.GM_ERRORS_AND_WARNINGS;
import common.GlobalMembersGm_error;
import common.GlobalMembersGm_main;
import common.GlobalMembersGm_misc;

public class GlobalMembersGm_new_typecheck_step1
{

	// check id1 and id2 have same target graph symbol
	//   graph -> itself
	//   property/set/node/edge -> bound graph
	// returns is_okay;
	public static boolean gm_check_target_graph(ast_id id1, ast_id id2)
	{
		ast_typedecl t1 = id1.getTypeInfo();
		assert t1 != null;
		ast_typedecl t2 = id2.getTypeInfo();
		assert t2 != null;
		gm_symtab_entry e1;
		gm_symtab_entry e2;
		if (t1.is_graph())
			e1 = id1.getSymInfo();
		else
			e1 = t1.get_target_graph_sym();

		if (t2.is_graph())
			e2 = id2.getSymInfo();
		else
			e2 = t2.get_target_graph_sym();

		if (e1 != e2)
		{
			//printf("id1 = %s, typd = %s %p\n", id1->get_orgname(), gm_get_type_string(t1->getTypeSummary()), e1);
			//printf("id2 = %s, typd = %s %p\n", id2->get_orgname(), gm_get_type_string(t2->getTypeSummary()), e2);
		}
		return (e1 == e2);
	}
public static boolean gm_find_and_connect_symbol(ast_id id, gm_symtab begin)
{
	return gm_find_and_connect_symbol(id, begin, true);
}

//C++ TO JAVA CONVERTER NOTE: Java does not allow default values for parameters. Overloaded methods are inserted above.
//ORIGINAL LINE: static boolean gm_find_and_connect_symbol(ast_id* id, gm_symtab* begin, boolean print_error = true)
	public static boolean gm_find_and_connect_symbol(ast_id id, gm_symtab begin, boolean print_error)
	{
		assert id != null;
		assert id.get_orgname() != null;

		gm_symtab_entry se = begin.find_symbol(id);
		if (se == null)
		{
			if (print_error)
				GlobalMembersGm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_UNDEFINED, id);
			return false;
		}

		if (id.getSymInfo() != null)
		{
			assert id.getSymInfo() == se;
		}
		else
		{
			id.setSymInfo(se);
		}

		return true;
	}

	public static final int SHOULD_BE_A_GRAPH = 1;
	public static final int SHOULD_BE_A_COLLECTION = 2;
	public static final int SHOULD_BE_A_NODE_COMPATIBLE = 3;
	public static final int SHOULD_BE_A_PROPERTY = 4;
	public static final int ANY_THING = 0;
public static boolean gm_check_target_is_defined(ast_id target, gm_symtab vars)
{
	return gm_check_target_is_defined(target, vars, ANY_THING);
}

	//-------------------------------------------------
	// check target-id is well defined as a graph/collection/node
	// (This funcition also connects target-id with symbol entry)
	//-------------------------------------------------
//C++ TO JAVA CONVERTER NOTE: Java does not allow default values for parameters. Overloaded methods are inserted above.
//ORIGINAL LINE: boolean gm_check_target_is_defined(ast_id* target, gm_symtab* vars, int should_be_what = ANY_THING)
	public static boolean gm_check_target_is_defined(ast_id target, gm_symtab vars, int should_be_what)
	{
		// check graph is defined
		assert target.get_orgname() != null;
		if (GlobalMembersGm_new_typecheck_step1.gm_find_and_connect_symbol(target, vars) == false)
			return false;

		switch (should_be_what)
		{
			case SHOULD_BE_A_GRAPH:
				if ((!target.getTypeInfo().is_graph()))
				{
					GlobalMembersGm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_NONGRAPH_TARGET, target, target);
					return false;
				}
				break;
			case SHOULD_BE_A_COLLECTION:
				if ((!target.getTypeInfo().is_collection()))
				{
					GlobalMembersGm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_NONSET_TARGET, target, target);
					return false;
				}
				break;
			case SHOULD_BE_A_NODE_COMPATIBLE:
				if ((!target.getTypeInfo().is_node_compatible()))
				{
					GlobalMembersGm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_NONNODE_TARGET, target, target);
					return false;
				}
				break;
			case SHOULD_BE_A_PROPERTY:
				if (!target.getTypeInfo().is_property())
				{
					GlobalMembersGm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_NONSET_TARGET, target, target);
					return false;
				}
				break;
		}

		return true;
	}

	//------------------------------------------------
	// Searches the symbol table and its parents for a 
	// single graph instance.
	// If exactly one is found it is returned.
	// If none is found, NULL is returned.
	// Else an assertion fails
	//------------------------------------------------
	public static ast_id gm_get_default_graph(gm_symtab symTab)
	{
		int foundCount = 0;
		ast_id targetGraph = null;
		do //search for a single graph instance in the symbol table
		{
			java.util.HashSet<gm_symtab_entry> entries = symTab.get_entries();
			for (gm_symtab_entry e : entries) {
				ast_typedecl entryType = e.getType();
				if (entryType.is_graph())
				{
					foundCount++;
					if (foundCount > 1)
					{
						GlobalMembersGm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_DEFAULT_GRAPH_AMBIGUOUS, targetGraph, e.getId());
						return null;
					}
					targetGraph = e.getId();
				}
			}
			symTab = symTab.get_parent();
		} while (symTab != null);
		return targetGraph;
	}

	public static boolean gm_check_graph_is_defined(ast_typedecl type, gm_symtab symTab)
	{
		ast_id graph = type.get_target_graph_id();

		if (graph == null)
		{
			//no associated graph found - try to find default graph
			graph = GlobalMembersGm_new_typecheck_step1.gm_get_default_graph(symTab);
			if (graph == null)
				return false;
			graph = graph.copy(true);
			type.set_target_graph_id(graph);
			graph.set_parent(type);
			symTab.set_default_graph_used();
		}
		return GlobalMembersGm_new_typecheck_step1.gm_check_target_is_defined(graph, symTab, SHOULD_BE_A_GRAPH);
	}

	//------------------------------------------------
	// (a) For node/edge/collection/all-graph iterator
	//     - check graph_id is defined and a graph
	//     - connect graph_id with the symbol
	// (b) For property
	//     - check graph_id is defined and a graph
	//     - connect graph id with the graph
	//     - check target_type is primitve 
	// (c) For collection iter
	//     - check collection_id is defined as a collection
	//     - connect collection_id with the symbol
	//     - update iter-type in typeinfo. (iter-type in foreach should be updateded separately)
	//     - copy graph_id from collection_id 
	// (d) For nbr iterator
	//     - check nbr_id is defined (as a node-compatible)
	//     - connect nbr_id with the symbol
	//     - copy graph_id from collection_id
	//------------------------------------------------
	public static boolean gm_check_type_is_well_defined(ast_typedecl type, gm_symtab SYM_V)
	{
		return GlobalMembersGm_new_typecheck_step1.gm_check_type_is_well_defined(type, SYM_V, GMTYPE_T.GMTYPE_INVALID);
	}

	public static boolean gm_check_type_is_well_defined(ast_typedecl type, gm_symtab SYM_V, GMTYPE_T targetType)
	{
		if (type.is_primitive() || type.is_void())
		{
			//nothing to do
		}
		else if (type.is_graph())
		{
			//if default graph is used, check if no other graph is defined
			if (SYM_V.is_default_graph_used() && SYM_V.get_graph_declaration_count() > 0)
			{
				GlobalMembersGm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_DEFAULT_GRAPH_AMBIGUOUS, (ast_id) type, "", "");
				return false;
			}
		}
		else if (type.is_collection() || type.is_nodeedge() || type.is_all_graph_iterator() || type.is_collection_of_collection())
		{
			boolean is_okay = GlobalMembersGm_new_typecheck_step1.gm_check_graph_is_defined(type, SYM_V);
			if (!is_okay)
				return is_okay;
		}
		else if (type.is_property())
		{
			boolean is_okay = GlobalMembersGm_new_typecheck_step1.gm_check_graph_is_defined(type, SYM_V);
			if (!is_okay)
				return false;

			ast_typedecl target_type = type.get_target_type();
			if (target_type.is_nodeedge() || target_type.is_collection())
			{
				is_okay &= GlobalMembersGm_new_typecheck_step1.gm_check_type_is_well_defined(target_type, SYM_V);
				if (!is_okay)
					return false;
			}
			else if (!target_type.is_primitive())
			{
				GlobalMembersGm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_NEED_PRIMITIVE, type.get_line(), type.get_col());
				return false;
			}
		}
		else if (type.is_collection_iterator())
		{
			ast_id col = type.get_target_collection_id();
			assert col != null;
			boolean is_okay = GlobalMembersGm_new_typecheck_step1.gm_check_target_is_defined(col, SYM_V, SHOULD_BE_A_COLLECTION);
			if (!is_okay)
				return false;

			// update collection iter type
			if (type.is_unknown_collection_iterator())
			{
				GMTYPE_T iterType = col.getTypeSummary().get_natural_collection_iterator();

				if (iterType == GMTYPE_T.GMTYPE_ITER_UNDERSPECIFIED && targetType != GMTYPE_T.GMTYPE_INVALID)
				{
					iterType = targetType.get_specified_collection_iterator();
				}

				type.setTypeSummary(iterType);
			}

			// copy graph_id
			type.set_target_graph_id(col.getTypeInfo().get_target_graph_id().copy(true));
		}
		else if (type.is_property_iterator())
		{
			ast_id property = type.get_target_property_id();
			assert property != null;
			boolean is_okay = GlobalMembersGm_new_typecheck_step1.gm_check_target_is_defined(property, SYM_V, SHOULD_BE_A_PROPERTY);
			if (!is_okay)
				return false;

			type.set_target_graph_id(property.getTypeInfo().get_target_graph_id().copy(true));

		}
		else if (type.is_common_nbr_iterator() || type.is_any_nbr_iterator())
		{
			ast_id node = type.get_target_nbr_id();
			assert node != null;
			boolean is_okay = GlobalMembersGm_new_typecheck_step1.gm_check_target_is_defined(node, SYM_V, SHOULD_BE_A_NODE_COMPATIBLE);
			if (!is_okay)
				return is_okay;

			// copy graph_id
			//printf("copying graph id = %s\n", node->getTypeInfo()->get_target_graph_id()->get_orgname());
			type.set_target_graph_id(node.getTypeInfo().get_target_graph_id().copy(true));
		}
		else
		{
			System.out.printf("%s", GlobalMembersGm_misc.gm_get_type_string(type.getTypeSummary()));
			assert false;
		}

		type.set_well_defined(true);
		return true;
	}

	//---------------------
	// (This function can be used after type-checking)
	// add a (copy of) symbol and (copy of) type into a symtab, error if symbol is duplicated
	//  (the original id is also connected to the symtab enntry)
	// (type should be well defined)
	//
	// The name is added to the current procedure vocaburary 
	//---------------------
	public static boolean gm_declare_symbol(gm_symtab SYM, ast_id id, ast_typedecl type, boolean is_readable, boolean is_writeable, gm_symtab SYM_ALT, GMTYPE_T targetType)
	{

		if (!type.is_well_defined())
		{
			assert!type.is_property();
			// if so SYM is FIELD actually.
			if (SYM_ALT != null)
			{
				if (!GlobalMembersGm_new_typecheck_step1.gm_check_type_is_well_defined(type, SYM_ALT, targetType))
					return false;
			}
			else if (!GlobalMembersGm_new_typecheck_step1.gm_check_type_is_well_defined(type, SYM, targetType))
			{
				return false;
			}
		}
		gm_symtab_entry old_e;
		boolean is_okay = SYM.check_duplicate_and_add_symbol(id, type, old_e, is_readable, is_writeable);
		if (!is_okay)
			GlobalMembersGm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_DUPLICATE, id, old_e.getId());

		GlobalMembersGm_new_typecheck_step1.gm_find_and_connect_symbol(id, SYM);

		if (is_okay)
			GlobalMembersGm_main.FE.voca_add(id.get_orgname());

		return is_okay;
	}

	public static boolean gm_declare_symbol(gm_symtab SYM, ast_id id, ast_typedecl type, boolean is_readable, boolean is_writeable, gm_symtab SYM_ALT)
	{
		return GlobalMembersGm_new_typecheck_step1.gm_declare_symbol(SYM, id, type, is_readable, is_writeable, SYM_ALT, GMTYPE_T.GMTYPE_INVALID);
	}

	public static boolean gm_declare_symbol(gm_symtab SYM, ast_id id, ast_typedecl type, boolean is_readable, boolean is_writeable)
	{
		return GlobalMembersGm_new_typecheck_step1.gm_declare_symbol(SYM, id, type, is_readable, is_writeable, null, GMTYPE_T.GMTYPE_INVALID);
	}
}