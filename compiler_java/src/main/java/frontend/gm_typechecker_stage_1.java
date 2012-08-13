package frontend;

import static common.GM_ERRORS_AND_WARNINGS.GM_ERROR_INVALID_ITERATOR_FOR_RARROW;
import static common.GM_ERRORS_AND_WARNINGS.GM_ERROR_INVALID_OUTPUT_TYPE;
import static common.GM_ERRORS_AND_WARNINGS.GM_ERROR_NEED_BFS_ITERATION;
import static common.GM_ERRORS_AND_WARNINGS.GM_ERROR_NEED_ITERATOR;
import static common.GM_ERRORS_AND_WARNINGS.GM_ERROR_NONGRAPH_FIELD;
import static common.GM_ERRORS_AND_WARNINGS.GM_ERROR_NONNODE_TARGET;
import static common.GM_ERRORS_AND_WARNINGS.GM_ERROR_TARGET_MISMATCH;
import static common.GM_ERRORS_AND_WARNINGS.GM_ERROR_WRONG_PROPERTY;
import static frontend.gm_typecheck.GM_READ_AVAILABLE;
import static frontend.gm_typecheck.GM_READ_NOT_AVAILABLE;
import static frontend.gm_typecheck.GM_WRITE_AVAILABLE;
import static frontend.gm_typecheck.GM_WRITE_NOT_AVAILABLE;
import inc.GMTYPE_T;

import java.util.HashSet;
import java.util.LinkedList;

import tangible.RefObject;

import ast.AST_NODE_TYPE;
import ast.ast_argdecl;
import ast.ast_assign;
import ast.ast_bfs;
import ast.ast_expr;
import ast.ast_expr_builtin;
import ast.ast_expr_builtin_field;
import ast.ast_expr_foreign;
import ast.ast_expr_reduce;
import ast.ast_field;
import ast.ast_foreach;
import ast.ast_foreign;
import ast.ast_id;
import ast.ast_idlist;
import ast.ast_node;
import ast.ast_procdef;
import ast.ast_sent;
import ast.ast_typedecl;
import ast.ast_vardecl;

import common.GM_ERRORS_AND_WARNINGS;
import common.GlobalMembersGm_error;
import common.GlobalMembersGm_main;
import common.gm_apply;

/**
 * <b>Type-check Step 1:</b><br>
 * 
 * (1) create a hierarchy of symbol tables<br>
 * (2) Add symbols into symbol table<br>
 * (3) create a connection ID <-> symbol<br>
 * (4) Check rules related to ID<br>
 * 
 * <dd>- procedure:<br> <dd><dd>* return should be primitive or node/edge<br>
 * <dd><dd>* output args should be primitive or node/edge<br> <dd>-
 * declarations: target graph should be well defined.<br>
 * (property, node, edge, collection)<br>
 * 
 * <dd>- property should be primitive or node/edge<br>
 * 
 * <dd>- iterators: <br> <dd><dd>* target graph(set) should be well defined<br>
 * <dd><dd>* node iterator should begin from node, edge iterator should begin
 * from edge<br> <dd><dd>* up/down should be start from bfs iterator<br> <dd>-
 * bfs<br> <dd><dd>* src should be a graph<br> <dd><dd>* root should be a node
 * and belong to the same src<br> <dd>- property access:<br> <dd><dd>* target
 * graph should match<br>
 */
public class gm_typechecker_stage_1 extends gm_apply {

	public static final int SHOULD_BE_A_GRAPH = 1;
	public static final int SHOULD_BE_A_COLLECTION = 2;
	public static final int SHOULD_BE_A_NODE_COMPATIBLE = 3;
	public static final int SHOULD_BE_A_PROPERTY = 4;
	public static final int ANY_THING = 0;

	// symbol tables
	private LinkedList<gm_symtab> var_syms = new LinkedList<gm_symtab>();
	private LinkedList<gm_symtab> field_syms = new LinkedList<gm_symtab>();
	private LinkedList<gm_symtab> proc_syms = new LinkedList<gm_symtab>();

	private gm_symtab curr_sym = null;
	private gm_symtab curr_field = null;
	private gm_symtab curr_proc = null;

	private boolean _is_okay = true;

	public gm_typechecker_stage_1() {
		set_for_expr(true);
		set_for_sent(true);
		set_for_proc(true);
	}

	// --------------------------------------------------------
	// Add arguments to the current symbol table
	@Override
	public boolean apply(ast_procdef p) {
		boolean is_okay = true;
		// add arguments to the current symbol table
		LinkedList<ast_argdecl> in_args = p.get_in_args();
		for (ast_argdecl a : in_args) {
			ast_typedecl type = a.get_type();
			boolean b = gm_check_type_is_well_defined(type, curr_sym);
			is_okay = b && is_okay;
			if (b) {
				ast_idlist idlist = a.get_idlist();
				gm_symtab S = type.is_property() ? curr_field : curr_sym;
				for (int i = 0; i < idlist.get_length(); i++) {
					ast_id id = idlist.get_item(i);
					is_okay = gm_declare_symbol(S, id, type, GM_READ_AVAILABLE, GM_WRITE_NOT_AVAILABLE) && is_okay;
					if (is_okay) {
						id.getSymInfo().setArgument(true);
					}
				}
			}
		}

		LinkedList<ast_argdecl> out_args = p.get_out_args();
		for (ast_argdecl a : out_args) {
			ast_typedecl type = a.get_type();
			boolean b = gm_check_type_is_well_defined(type, curr_sym);
			is_okay = b && is_okay;
			if (b) {
				// ast_idlist idlist = a.get_idlist();
				// only primitives or nodes or edges can be an output
				if (!type.is_primitive() && !type.is_nodeedge()) {
					GlobalMembersGm_error.gm_type_error(GM_ERROR_INVALID_OUTPUT_TYPE, type.get_line(), type.get_col());
					is_okay = false;
				} else {
					ast_idlist idlist = a.get_idlist();
					for (int i = 0; i < idlist.get_length(); i++) {
						ast_id id = idlist.get_item(i);
						is_okay = gm_declare_symbol(curr_sym, id, type, GM_READ_NOT_AVAILABLE, GM_WRITE_AVAILABLE) && is_okay;
						if (is_okay) {
							id.getSymInfo().setArgument(true);
						}
					}
				}
			}
		}

		// ---------------------------------------
		// crete return type
		// ---------------------------------------
		ast_typedecl ret;
		ret = p.get_return_type();
		if (ret == null) {
			ret = ast_typedecl.new_void();
			p.set_return_type(ret);
		}
		is_okay = gm_check_type_is_well_defined(ret, curr_sym) && is_okay;
		if (!ret.is_void() && !ret.is_primitive() && !ret.is_nodeedge()) {
			GlobalMembersGm_error.gm_type_error(GM_ERROR_INVALID_OUTPUT_TYPE, ret.get_line(), ret.get_col());
			is_okay = false;
		}

		set_okay(is_okay);
		return is_okay;

	}

	@Override
	public boolean apply(ast_expr p) {
		boolean is_okay = true;
		switch (p.get_opclass()) {
		case GMEXPR_ID: {
			is_okay = find_symbol_id(p.get_id());
			break;
		}
		case GMEXPR_FIELD: {
			is_okay = find_symbol_field(p.get_field());
			break;
		}
		case GMEXPR_REDUCE: {
			ast_expr_reduce r = (ast_expr_reduce) p;
			GMTYPE_T iter_type = r.get_iter_type();
			is_okay = gm_symbol_check_iter_header(r.get_iterator(), r.get_source(), iter_type, r.get_source2());
			if (iter_type.is_unknown_collection_iter_type()) // resolve unknown
																// iterator
				r.set_iter_type(r.get_iterator().getTypeSummary());
			break;
		}
		case GMEXPR_BUILTIN: {
			ast_expr_builtin b = (ast_expr_builtin) p;
			ast_id i = b.get_driver();
			if (i != null)
				is_okay = find_symbol_id(i);
			if (is_okay) {
				@SuppressWarnings("unused")
				GMTYPE_T source_type = (i == null) ? GMTYPE_T.GMTYPE_VOID : i.getTypeSummary();
			}
			break;
		}
		case GMEXPR_BUILTIN_FIELD: {
			ast_expr_builtin_field builtinField = (ast_expr_builtin_field) p;
			ast_field field = builtinField.get_field_driver();
			is_okay = find_symbol_field(field);
			is_okay &= find_symbol_id(field.get_first());
			break;
		}
		case GMEXPR_FOREIGN: {
			ast_expr_foreign f = (ast_expr_foreign) p;
			LinkedList<ast_node> L = f.get_parsed_nodes();
			for (ast_node n : L) {
				if (n == null)
					continue;
				if (n.get_nodetype() == AST_NODE_TYPE.AST_FIELD) {
					is_okay = find_symbol_field((ast_field) n) && is_okay;
				} else {
					boolean b = find_symbol_id((ast_id) n, false);
					if (!b) {
						b = find_symbol_field_id((ast_id) n);
					}
					is_okay = b && is_okay;
				}
			}
			break;
		}
		default:
			break;
		}

		set_okay(is_okay);

		return is_okay;
	}

	@Override
	public boolean apply(ast_sent s) {
		boolean is_okay = true;
		switch (s.get_nodetype()) {
		// Add variable declaration
		case AST_VARDECL: {
			ast_vardecl v = (ast_vardecl) s;
			ast_typedecl type = v.get_type();
			is_okay = gm_check_type_is_well_defined(type, curr_sym);

			// add current declaration
			if (is_okay) {
				ast_idlist idlist = v.get_idlist();
				gm_symtab S = type.is_property() ? curr_field : curr_sym;
				for (int i = 0; i < idlist.get_length(); i++) {
					ast_id id = idlist.get_item(i);
					is_okay = gm_declare_symbol(S, id, type, GM_READ_AVAILABLE, GM_WRITE_AVAILABLE) && is_okay;
				}
			}

			v.set_tc_finished(true); // why?
			break;
		}
		// check lhs and bound symbol
		case AST_ASSIGN: {
			ast_assign a = (ast_assign) s;
			// lhs
			if (a.is_target_scalar()) {
				ast_id id = a.get_lhs_scala();
				is_okay = find_symbol_id(id);
			} else {
				ast_field f = a.get_lhs_field();
				is_okay = find_symbol_field(f);
			}

			if (a.is_argminmax_assign()) {
				LinkedList<ast_node> L = a.get_lhs_list();
				for (ast_node n : L) {
					if (n.get_nodetype() == AST_NODE_TYPE.AST_ID) {
						ast_id id = (ast_id) n;
						is_okay = find_symbol_id(id) && is_okay;
					} else {
						ast_field f = (ast_field) n;
						is_okay = find_symbol_field(f) && is_okay;
					}
				}
			}

			// bound symbol
			ast_id bound = a.get_bound();
			if (bound != null) {
				is_okay = find_symbol_id(bound);
				if (is_okay) {
					// bound symbol must be iterator
					if (!bound.getTypeInfo().is_node_edge_iterator() && !bound.getTypeInfo().is_collection_iterator()) {
						GlobalMembersGm_error.gm_type_error(GM_ERROR_NEED_ITERATOR, bound);
						is_okay = false;
					}
				}
			}

			break;
		}
		// check bound symbol
		case AST_FOREACH: {
			ast_foreach fe = (ast_foreach) s;
			GMTYPE_T iter_type = adjust_iter_type(fe);
			is_okay = gm_symbol_check_iter_header(fe.get_iterator(), fe.get_source(), iter_type, fe.get_source2());
			if (!is_okay)
				break;
			if (iter_type.is_unknown_collection_iter_type()) // resolve unknown
																// iterator
			{
				fe.set_iter_type(fe.get_iterator().getTypeSummary());
			}
			break;
		}
		case AST_BFS: {
			ast_bfs bfs = (ast_bfs) s;
			is_okay = gm_symbol_check_bfs_header(bfs.get_iterator(), bfs.get_source(), bfs.get_root(), bfs.get_iter_type());

			// ---------------------------------------------
			// create 2nd iteator
			// ---------------------------------------------
			String tname = GlobalMembersGm_main.FE.voca_temp_name("nx");
			ast_id iter2 = ast_id.new_id(tname, bfs.get_iterator().get_line(), bfs.get_iterator().get_col());
			ast_typedecl type = ast_typedecl.new_nbr_iterator(bfs.get_iterator().copy(true), bfs.get_iter_type2());
			is_okay = gm_declare_symbol(curr_sym, iter2, type, GM_READ_AVAILABLE, GM_WRITE_NOT_AVAILABLE) && is_okay;
			if (type != null)
				type.dispose();
			tname = null;
			bfs.set_iterator2(iter2);
			break;
		}
		case AST_FOREIGN: {
			ast_foreign f = (ast_foreign) s;

			// -----------------------------------
			// examine mutation list
			// -----------------------------------
			LinkedList<ast_node> L = f.get_modified();
			for (ast_node node : L) {
				if (node.get_nodetype() == AST_NODE_TYPE.AST_ID) {
					ast_id id = (ast_id) node;
					boolean b = find_symbol_id(id, false);
					if (!b) {
						b = find_symbol_field_id(id);
					}
					is_okay = b && is_okay;
				} else if (node.get_nodetype() == AST_NODE_TYPE.AST_FIELD) {
					ast_field ff = (ast_field) node;
					is_okay = find_symbol_field(ff) && is_okay;
				} else {
					assert false;
				}
			}
			break;
		}
		// expressions will be considiered when apply(ast_expr*) is invoked
		case AST_SENTBLOCK:
		case AST_CALL:
		case AST_WHILE:
		case AST_IF:
		case AST_NOP:
		case AST_RETURN:
			break;
		default:
			System.out.println("type = " + s.get_nodetype().get_nodetype_string());
			assert false;
			break;
		}
		set_okay(is_okay);

		return is_okay;
	}

	@Override
	public void begin_context(ast_node n) {
		assert n.has_scope();
		n.get_symtab_var().set_parent(curr_sym);
		n.get_symtab_field().set_parent(curr_field);
		n.get_symtab_proc().set_parent(curr_proc);

		var_syms.addLast(curr_sym);
		field_syms.addLast(curr_field);
		proc_syms.addLast(curr_proc);

		curr_sym = n.get_symtab_var();
		curr_field = n.get_symtab_field();
		curr_proc = n.get_symtab_proc();

		// printf("push\n");
	}

	@Override
	public void end_context(ast_node n) {
		assert n.has_scope();
		curr_sym = var_syms.getLast();
		curr_field = field_syms.getLast();
		curr_proc = proc_syms.getLast();

		var_syms.removeLast();
		field_syms.removeLast();
		proc_syms.removeLast();
		// printf("pop\n");
	}

	private final void set_okay(boolean b) {
		_is_okay = _is_okay && b;
	}

	public final boolean is_okay() {
		return _is_okay;
	}

	private final boolean find_symbol_field(ast_field f) {
		ast_id driver = f.get_first();
		ast_id field = f.get_second();

		boolean is_okay = true;
		is_okay = gm_find_and_connect_symbol(driver, curr_sym) && is_okay;
		is_okay = gm_find_and_connect_symbol(field, curr_field) && is_okay;

		if (is_okay) {

			ast_typedecl name_type = driver.getTypeInfo();
			ast_typedecl field_type = field.getTypeInfo();
			assert name_type != null;
			assert field_type != null;

			// check the type of driver
			if (!(name_type.is_graph() || name_type.is_collection() || name_type.is_nodeedge() || name_type.is_node_edge_iterator() || name_type
					.is_collection_iterator())) // for group assignment - for
												// group assignment
			{
				GlobalMembersGm_error.gm_type_error(GM_ERROR_NONGRAPH_FIELD, driver);
				is_okay = false;
			}

			if (!field_type.is_property()) {
				GlobalMembersGm_error.gm_type_error(GM_ERROR_WRONG_PROPERTY, field, "property");
				is_okay = false;
			}

			if (!is_okay)
				return false;

			// n.X ==> n is node iterator, X is node prop
			// Edge(n).Y ==> n is nbr iterator, Y is edge prop. Edge(n) is the
			// current edge that goes to n

			if (f.is_rarrow()) {
				GMTYPE_T type = name_type.getTypeSummary();
				if (!(type.is_inout_nbr_node_iter_type() || (type == GMTYPE_T.GMTYPE_NODEITER_BFS))) {
					// not BFS, not in-out
					GlobalMembersGm_error.gm_type_error(GM_ERROR_INVALID_ITERATOR_FOR_RARROW, driver);
					return false;
				}
				if (!field_type.is_edge_property()) {
					GlobalMembersGm_error.gm_type_error(GM_ERROR_WRONG_PROPERTY, field, "Edge_Property");
					return false;
				}
			} else {

				if (name_type.is_graph() || name_type.is_collection()) {
					// to be resolved more later (group assignment)
				} else if (name_type.is_node_compatible()) {
					if (!field_type.is_node_property()) {
						GlobalMembersGm_error.gm_type_error(GM_ERROR_WRONG_PROPERTY, field, "Node_Property");
						return false;
					}
				} else if (name_type.is_edge_compatible()) {
					if (!field_type.is_edge_property()) {
						GlobalMembersGm_error.gm_type_error(GM_ERROR_WRONG_PROPERTY, field, "Edge_Property");
						return false;
					}
				} else {
					assert false;
				}
			}

			// check target graph matches
			if (!gm_check_target_graph(driver, field)) {
				GlobalMembersGm_error.gm_type_error(GM_ERROR_TARGET_MISMATCH, driver, field);
				return false;
			}
		}

		return is_okay;
	}

	private final boolean find_symbol_id(ast_id id) {
		return find_symbol_id(id, true);
	}

	private final boolean find_symbol_id(ast_id id, boolean print_error) {
		return gm_find_and_connect_symbol(id, curr_sym, print_error);
	}

	private final boolean find_symbol_field_id(ast_id id) {
		return gm_find_and_connect_symbol(id, curr_field);
	}

	private final boolean gm_symbol_check_iter_header(ast_id it, ast_id src, GMTYPE_T iter_type, ast_id src2) {
		boolean is_okay = true;
		// GRAPH
		if (iter_type.is_iteration_on_all_graph()) {
			is_okay = gm_check_target_is_defined(src, curr_sym, SHOULD_BE_A_GRAPH);
		}
		// items - collection
		else if (iter_type.is_iteration_on_collection()) {
			is_okay = gm_check_target_is_defined(src, curr_sym, SHOULD_BE_A_COLLECTION);
		}
		// items - property
		else if (iter_type.is_iteration_on_property()) {
			is_okay = gm_check_target_is_defined(src, curr_field, SHOULD_BE_A_PROPERTY);
		}
		// out.in.up.down
		else if (iter_type.is_iteration_on_neighbors_compatible()) {
			ast_id n = src; // f->get_source();
			is_okay = gm_find_and_connect_symbol(n, curr_sym); // source

			if (is_okay) {

				ast_typedecl type = n.getTypeInfo();
				if (!type.is_node_compatible()) {
					GlobalMembersGm_error.gm_type_error(GM_ERROR_NONNODE_TARGET, n, n);
					is_okay = false;
				}

				// In/Down is only available inside BFS -> checked at step 2
				if (iter_type.is_iteration_on_updown_levels()) {
					if (n.getTypeSummary().is_iteration_bfs()) {
						GlobalMembersGm_error.gm_type_error(GM_ERROR_NEED_BFS_ITERATION, n);
						is_okay = false;
					}
				}

				if (is_okay && iter_type.is_common_nbr_iter_type()) {
					assert src2 != null;
					is_okay = gm_find_and_connect_symbol(src2, curr_sym); // source

					if (is_okay) {
						// check if two sources have the same graph
						gm_symtab_entry e1 = src.getTypeInfo().get_target_graph_sym();
						gm_symtab_entry e2 = src2.getTypeInfo().get_target_graph_sym();
						assert e1 != null;
						if (e1 != e2) {
							GlobalMembersGm_error.gm_type_error(GM_ERROR_TARGET_MISMATCH, src2.get_line(), src2.get_col());
							is_okay = false;
						}
					}

				}
			}
		} else {
			System.out.println(iter_type.get_type_string());
			assert false;
		}

		if (!is_okay)
			return false;

		// --------------------------------------
		// create iterator
		// --------------------------------------
		ast_typedecl type;
		if (iter_type.is_iteration_on_collection()) {
			type = ast_typedecl.new_set_iterator(src.copy(true), iter_type);
		} else if (iter_type.is_iteration_on_property()) {
			type = ast_typedecl.new_property_iterator(src.copy(true), iter_type);
		} else if (iter_type.is_iteration_on_neighbors_compatible()) {
			type = ast_typedecl.new_nbr_iterator(src.copy(true), iter_type);
		} else {
			type = ast_typedecl.new_nodeedge_iterator(src.copy(true), iter_type);
		}

		if (iter_type.is_iteration_on_property())
			is_okay = gm_declare_symbol(curr_sym, it, type, GM_READ_AVAILABLE, GM_WRITE_NOT_AVAILABLE, curr_field);
		else if (src.getTypeInfo().is_collection_of_collection())
			is_okay = gm_declare_symbol(curr_sym, it, type, GM_READ_AVAILABLE, GM_WRITE_NOT_AVAILABLE, null, src.getTargetTypeSummary());
		else
			is_okay = gm_declare_symbol(curr_sym, it, type, GM_READ_AVAILABLE, GM_WRITE_NOT_AVAILABLE);

		if (type != null)
			type.dispose();

		return is_okay;
	}

	/** symbol checking for foreach and in-place reduction */
	private final boolean gm_symbol_check_bfs_header(ast_id it, ast_id src, ast_id root, GMTYPE_T iter_type) {
		// check source: should be a graph
		boolean is_okay = true;
		is_okay = gm_check_target_is_defined(src, curr_sym, SHOULD_BE_A_GRAPH);
		// check root:
		is_okay = gm_find_and_connect_symbol(root, curr_sym) && is_okay;
		if (is_okay) {
			// root should be a node. and target should be the graph
			ast_typedecl t_root = root.getTypeInfo();
			if (!t_root.is_node_compatible()) {
				GlobalMembersGm_error.gm_type_error(GM_ERROR_NONNODE_TARGET, root, root);
				is_okay = false;
			}
		}

		if (is_okay) {
			// check root is a node of src
			is_okay = gm_check_target_graph(src, root);
			if (!is_okay)
				GlobalMembersGm_error.gm_type_error(GM_ERROR_TARGET_MISMATCH, src, root);
		}

		// -----------------------------------------
		// create iteator
		// -----------------------------------------
		ast_typedecl type = ast_typedecl.new_nodeedge_iterator(src.copy(true), iter_type);
		is_okay = gm_declare_symbol(curr_sym, it, type, GM_READ_AVAILABLE, GM_WRITE_NOT_AVAILABLE) && is_okay;
		if (type != null)
			type.dispose();

		return is_okay;
	}

	/**
	 * if sourceId is defined as a field variable (= is a property) the iter
	 * type should be a property iterator
	 */
	private GMTYPE_T adjust_iter_type(ast_foreach fe) {
		if (curr_field.find_symbol(fe.get_source()) != null) {
			ast_id source = fe.get_source();
			gm_symtab_entry tabEntry = curr_field.find_symbol(source);
			GMTYPE_T targetType = tabEntry.getType().getTargetTypeSummary();
			GMTYPE_T newIterType = mapTargetToIterType(targetType);
			fe.set_iter_type(newIterType);
			return newIterType;
		} else {
			return fe.get_iter_type();
		}
	}

	private static GMTYPE_T mapTargetToIterType(GMTYPE_T targetType) {
		switch (targetType) {
		case GMTYPE_NSET:
		case GMTYPE_ESET:
			return GMTYPE_T.GMTYPE_PROPERTYITER_SET;
		case GMTYPE_NSEQ:
		case GMTYPE_ESEQ:
			return GMTYPE_T.GMTYPE_PROPERTYITER_SEQ;
		case GMTYPE_NORDER:
		case GMTYPE_EORDER:
			return GMTYPE_T.GMTYPE_PROPERTYITER_ORDER;
		default:
			assert false;
			return GMTYPE_T.GMTYPE_INVALID;
		}
	}

	/**
	 * check id1 and id2 have same target graph symbol graph -> itself
	 * property/set/node/edge -> bound graph returns is_okay;
	 */
	private static boolean gm_check_target_graph(ast_id id1, ast_id id2) {
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

		if (e1 != e2) {
			// printf("id1 = %s, typd = %s %p\n", id1->get_orgname(),
			// gm_get_type_string(t1->getTypeSummary()), e1);
			// printf("id2 = %s, typd = %s %p\n", id2->get_orgname(),
			// gm_get_type_string(t2->getTypeSummary()), e2);
		}
		return (e1 == e2);
	}

	private static boolean gm_find_and_connect_symbol(ast_id id, gm_symtab begin) {
		return gm_find_and_connect_symbol(id, begin, true);
	}

	private static boolean gm_find_and_connect_symbol(ast_id id, gm_symtab begin, boolean print_error) {
		assert id != null;
		assert id.get_orgname() != null;

		gm_symtab_entry se = begin.find_symbol(id);
		if (se == null) {
			if (print_error)
				GlobalMembersGm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_UNDEFINED, id);
			return false;
		}

		if (id.getSymInfo() != null) {
			assert id.getSymInfo() == se;
		} else {
			id.setSymInfo(se);
		}

		return true;
	}

	/**
	 * check target-id is well defined as a graph/collection/node (This
	 * funcition also connects target-id with symbol entry)
	 */
	private static boolean gm_check_target_is_defined(ast_id target, gm_symtab vars, int should_be_what) {
		// check graph is defined
		assert target.get_orgname() != null;
		if (gm_find_and_connect_symbol(target, vars) == false)
			return false;

		switch (should_be_what) {
		case SHOULD_BE_A_GRAPH:
			if ((!target.getTypeInfo().is_graph())) {
				GlobalMembersGm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_NONGRAPH_TARGET, target, target);
				return false;
			}
			break;
		case SHOULD_BE_A_COLLECTION:
			if ((!target.getTypeInfo().is_collection())) {
				GlobalMembersGm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_NONSET_TARGET, target, target);
				return false;
			}
			break;
		case SHOULD_BE_A_NODE_COMPATIBLE:
			if ((!target.getTypeInfo().is_node_compatible())) {
				GlobalMembersGm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_NONNODE_TARGET, target, target);
				return false;
			}
			break;
		case SHOULD_BE_A_PROPERTY:
			if (!target.getTypeInfo().is_property()) {
				GlobalMembersGm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_NONSET_TARGET, target, target);
				return false;
			}
			break;
		}
		return true;
	}

	/**
	 * Searches the symbol table and its parents for a single graph instance.
	 * 
	 * @return If exactly one is found it is returned. If none is found, NULL is
	 *         returned. Else an assertion fails
	 */
	private static ast_id gm_get_default_graph(gm_symtab symTab) {
		int foundCount = 0;
		ast_id targetGraph = null;
		do // search for a single graph instance in the symbol table
		{
			HashSet<gm_symtab_entry> entries = symTab.get_entries();
			for (gm_symtab_entry e : entries) {
				ast_typedecl entryType = e.getType();
				if (entryType.is_graph()) {
					foundCount++;
					if (foundCount > 1) {
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

	private static boolean gm_check_graph_is_defined(ast_typedecl type, gm_symtab symTab) {
		ast_id graph = type.get_target_graph_id();

		if (graph == null) {
			// no associated graph found - try to find default graph
			graph = gm_get_default_graph(symTab);
			if (graph == null)
				return false;
			graph = graph.copy(true);
			type.set_target_graph_id(graph);
			graph.set_parent(type);
			symTab.set_default_graph_used();
		}
		return gm_check_target_is_defined(graph, symTab, SHOULD_BE_A_GRAPH);
	}

	/**
	 * (a) For node/edge/collection/all-graph iterator<br>
	 * <dd>- check graph_id is defined and a graph<br> <dd>- connect graph_id
	 * with the symbol<br>
	 * (b) For property<br> <dd>- check graph_id is defined and a graph<br> <dd>
	 * - connect graph id with the graph<br> <dd>- check target_type is primitve
	 * <br>
	 * (c) For collection iter<br> <dd>- check collection_id is defined as a
	 * collection<br> <dd>- connect collection_id with the symbol<br> <dd>-
	 * update iter-type in typeinfo. (iter-type in foreach should be updateded
	 * separately)<br> <dd>- copy graph_id from collection_id<br>
	 * (d) For nbr iterator<br> <dd>- check nbr_id is defined (as a
	 * node-compatible)<br> <dd>- connect nbr_id with the symbol<br> <dd>- copy
	 * graph_id from collection_id<br>
	 */
	public static boolean gm_check_type_is_well_defined(ast_typedecl type, gm_symtab SYM_V) {
		return gm_check_type_is_well_defined(type, SYM_V, GMTYPE_T.GMTYPE_INVALID);
	}

	public static boolean gm_check_type_is_well_defined(ast_typedecl type, gm_symtab SYM_V, GMTYPE_T targetType) {
		if (type.is_primitive() || type.is_void()) {
			// nothing to do
		} else if (type.is_graph()) {
			// if default graph is used, check if no other graph is defined
			if (SYM_V.is_default_graph_used() && SYM_V.get_graph_declaration_count() > 0) {
				GlobalMembersGm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_DEFAULT_GRAPH_AMBIGUOUS, type.get_line(), type.get_col(), "");
				return false;
			}
		} else if (type.is_collection() || type.is_nodeedge() || type.is_all_graph_iterator() || type.is_collection_of_collection()) {
			boolean is_okay = gm_check_graph_is_defined(type, SYM_V);
			if (!is_okay)
				return is_okay;
		} else if (type.is_property()) {
			boolean is_okay = gm_check_graph_is_defined(type, SYM_V);
			if (!is_okay)
				return false;

			ast_typedecl target_type = type.get_target_type();
			if (target_type.is_nodeedge() || target_type.is_collection()) {
				is_okay &= gm_check_type_is_well_defined(target_type, SYM_V);
				if (!is_okay)
					return false;
			} else if (!target_type.is_primitive()) {
				GlobalMembersGm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_NEED_PRIMITIVE, type.get_line(), type.get_col());
				return false;
			}
		} else if (type.is_collection_iterator()) {
			ast_id col = type.get_target_collection_id();
			assert col != null;
			boolean is_okay = gm_check_target_is_defined(col, SYM_V, SHOULD_BE_A_COLLECTION);
			if (!is_okay)
				return false;

			// update collection iter type
			if (type.is_unknown_collection_iterator()) {
				GMTYPE_T iterType = col.getTypeSummary().get_natural_collection_iterator();

				if (iterType == GMTYPE_T.GMTYPE_ITER_UNDERSPECIFIED && targetType != GMTYPE_T.GMTYPE_INVALID) {
					iterType = targetType.get_specified_collection_iterator();
				}

				type.setTypeSummary(iterType);
			}

			// copy graph_id
			type.set_target_graph_id(col.getTypeInfo().get_target_graph_id().copy(true));
		} else if (type.is_property_iterator()) {
			ast_id property = type.get_target_property_id();
			assert property != null;
			boolean is_okay = gm_check_target_is_defined(property, SYM_V, SHOULD_BE_A_PROPERTY);
			if (!is_okay)
				return false;

			type.set_target_graph_id(property.getTypeInfo().get_target_graph_id().copy(true));

		} else if (type.is_common_nbr_iterator() || type.is_any_nbr_iterator()) {
			ast_id node = type.get_target_nbr_id();
			assert node != null;
			boolean is_okay = gm_check_target_is_defined(node, SYM_V, SHOULD_BE_A_NODE_COMPATIBLE);
			if (!is_okay)
				return is_okay;
			type.set_target_graph_id(node.getTypeInfo().get_target_graph_id().copy(true));
		} else {
			System.out.println(type.getTypeSummary().get_type_string());
			assert false;
		}

		type.set_well_defined(true);
		return true;
	}

	/**
	 * (This function can be used after type-checking) add a (copy of) symbol
	 * and (copy of) type into a symtab, error if symbol is duplicated (the
	 * original id is also connected to the symtab enntry) (type should be well
	 * defined)
	 * 
	 * The name is added to the current procedure vocaburary
	 */
	public static boolean gm_declare_symbol(gm_symtab SYM, ast_id id, ast_typedecl type, boolean is_readable, boolean is_writeable, gm_symtab SYM_ALT,
			GMTYPE_T targetType) {

		if (!type.is_well_defined()) {
			assert !type.is_property();
			// if so SYM is FIELD actually.
			if (SYM_ALT != null) {
				if (!gm_check_type_is_well_defined(type, SYM_ALT, targetType))
					return false;
			} else if (!gm_check_type_is_well_defined(type, SYM, targetType)) {
				return false;
			}
		}
		RefObject<gm_symtab_entry> old_e = new RefObject<gm_symtab_entry>(null);
		boolean is_okay = SYM.check_duplicate_and_add_symbol(id, type, old_e, is_readable, is_writeable);
		if (!is_okay)
			GlobalMembersGm_error.gm_type_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_DUPLICATE, id, old_e.argvalue.getId());

		gm_find_and_connect_symbol(id, SYM);

		if (is_okay)
			GlobalMembersGm_main.FE.voca_add(id.get_orgname());

		return is_okay;
	}

	public static boolean gm_declare_symbol(gm_symtab SYM, ast_id id, ast_typedecl type, boolean is_readable, boolean is_writeable, gm_symtab SYM_ALT) {
		return gm_declare_symbol(SYM, id, type, is_readable, is_writeable, SYM_ALT, GMTYPE_T.GMTYPE_INVALID);
	}

	public static boolean gm_declare_symbol(gm_symtab SYM, ast_id id, ast_typedecl type, boolean is_readable, boolean is_writeable) {
		return gm_declare_symbol(SYM, id, type, is_readable, is_writeable, null, GMTYPE_T.GMTYPE_INVALID);
	}

}
