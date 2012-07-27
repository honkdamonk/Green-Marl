package ast;

import frontend.gm_symtab_entry;
import inc.GMTYPE_T;

import common.GlobalMembersGm_dumptree;

//==========================================================================

public class ast_typedecl extends ast_node { // property or type
	private ast_typedecl() {
		super(AST_NODE_TYPE.AST_TYPEDECL);
		this.target_type = null;
		this.target_graph = null;
		this.target_collection = null;
		this.target_nbr = null;
		this.target_nbr2 = null;
		this._well_defined = false;
		this.type_id = GMTYPE_T.GMTYPE_INVALID;
	}

	// give a deep copy
	public final ast_typedecl copy() {
		ast_typedecl p = new ast_typedecl();
		p.type_id = this.type_id;
		p.target_type = (this.target_type == null) ? null : this.target_type.copy();
		p.target_graph = (this.target_graph == null) ? null : this.target_graph.copy(true);
		p.target_collection = (this.target_collection == null) ? null : this.target_collection.copy(true);
		p.target_nbr = (this.target_nbr == null) ? null : this.target_nbr.copy(true);
		p.target_nbr2 = (this.target_nbr2 == null) ? null : this.target_nbr2.copy(true);
		p.line = this.line;
		p.col = this.col;
		p._well_defined = this._well_defined;

		return p;
	}

	public void dispose() {
		if (target_type != null)
			target_type.dispose();
		if (target_graph != null)
			target_graph.dispose(); // gets deleted twice (sometimes) why??? o.O
		if (target_collection != null)
			target_collection.dispose();
		if (target_nbr != null)
			target_nbr.dispose();
	}

	public static ast_typedecl new_primtype(GMTYPE_T ptype_id) {
		ast_typedecl t = new ast_typedecl();
		t.type_id = ptype_id;
		return t;
	}

	public static ast_typedecl new_graphtype(GMTYPE_T gtype_id) {
		ast_typedecl t = new ast_typedecl();
		t.type_id = gtype_id;
		return t;
	}

	public static ast_typedecl new_nodetype(ast_id tg) {
		ast_typedecl t = new ast_typedecl();
		t.type_id = GMTYPE_T.GMTYPE_NODE;
		if (tg == null) // no graph defined for this node - we will handle this
						// later (typecheck step 1)
			return t;
		t.target_graph = tg;
		tg.set_parent(t);
		return t;
	}

	public static ast_typedecl new_edgetype(ast_id tg) {
		ast_typedecl t = new ast_typedecl();
		t.type_id = GMTYPE_T.GMTYPE_EDGE;
		if (tg == null) // no graph defined for this edge - we will handle this
						// later (typecheck step 1)
			return t;
		t.target_graph = tg;
		tg.set_parent(t);
		return t;
	}

	public static ast_typedecl new_nodeedge_iterator(ast_id tg, GMTYPE_T iter_type) {
		assert iter_type.is_all_graph_iter_type();
		ast_typedecl t = new ast_typedecl();
		t.type_id = iter_type;
		t.target_graph = tg;
		tg.set_parent(t);
		return t;
	}

	public static ast_typedecl new_nbr_iterator(ast_id tg, GMTYPE_T iter_type) {
		assert iter_type.is_any_nbr_iter_type();
		ast_typedecl t = new ast_typedecl();
		t.type_id = iter_type;
		t.target_nbr = tg;
		tg.set_parent(t);
		return t;
	}

	public static ast_typedecl new_common_nbr_iterator(ast_id tg, ast_id tg2, GMTYPE_T iter_type) {
		assert iter_type.is_any_nbr_iter_type();
		ast_typedecl t = new ast_typedecl();
		t.type_id = iter_type;
		t.target_nbr = tg;
		t.target_nbr2 = tg2;
		tg.set_parent(t);
		tg2.set_parent(t);
		return t;
	}

	public static ast_typedecl new_set(ast_id tg, GMTYPE_T set_type) {
		ast_typedecl t = new ast_typedecl();
		t.type_id = set_type;
		if (tg == null) // no graph defined for this set - we will handle this
						// later (typecheck step 1)
			return t;
		t.target_graph = tg;
		tg.set_parent(t);
		return t;
	}

	public static ast_typedecl new_queue(ast_id targetGraph, ast_typedecl collectionType) {
		ast_typedecl typeDecl = new ast_typedecl();
		typeDecl.type_id = GMTYPE_T.GMTYPE_COLLECTION;
		typeDecl.target_type = collectionType;
		if (targetGraph == null) // no graph defined for this queue - we will
									// handle this later (typecheck step 1)
			return typeDecl;
		typeDecl.target_graph = targetGraph;
		targetGraph.set_parent(typeDecl);
		return typeDecl;
	}

	public static ast_typedecl new_set_iterator(ast_id set, GMTYPE_T iter_type) {
		// deprecated
		ast_typedecl t = new ast_typedecl();
		t.type_id = iter_type;
		t.target_collection = set;
		set.set_parent(t);
		return t;
	}

	public static ast_typedecl new_collection_iterator(ast_id set, GMTYPE_T iter_type) {
		ast_typedecl t = new ast_typedecl();
		t.type_id = iter_type;
		t.target_collection = set;
		set.set_parent(t);
		return t;
	}

	public static ast_typedecl new_nodeprop(ast_typedecl type, ast_id tg) {
		ast_typedecl t = new ast_typedecl();
		t.type_id = GMTYPE_T.GMTYPE_NODEPROP;
		t.target_type = type;
		type.set_parent(t);
		if (tg == null) // no graph defined for this property - we will handle
						// this later (typecheck step 1)
			return t;
		t.target_graph = tg;
		tg.set_parent(t);
		return t;
	}

	public static ast_typedecl new_edgeprop(ast_typedecl type, ast_id tg) {
		ast_typedecl t = new ast_typedecl();
		t.type_id = GMTYPE_T.GMTYPE_EDGEPROP;
		t.target_type = type;
		type.set_parent(t);
		if (tg == null) // no graph defined for this property - we will handle
						// this later (typecheck step 1)
			return t;
		t.target_graph = tg;
		tg.set_parent(t);
		return t;
	}

	public static ast_typedecl new_property_iterator(ast_id property, GMTYPE_T iter_type) {
		ast_typedecl typeDecl = new ast_typedecl();
		typeDecl.type_id = iter_type;
		typeDecl.target_collection = property;
		property.set_parent(typeDecl);
		return typeDecl;
	}

	public static ast_typedecl new_void() {
		ast_typedecl t = new ast_typedecl();
		t.type_id = GMTYPE_T.GMTYPE_VOID;
		return t;
	}

	public final GMTYPE_T get_typeid() {
		return type_id;
	}

	public final void set_typeid(GMTYPE_T s) {
		type_id = s;
	}

	// seed gm_frontend_api.h
	public final boolean is_primitive() {
		return type_id.is_prim_type();
	}

	public final boolean is_graph() {
		return type_id.is_graph_type();
	}

	public final boolean is_node_property() {
		return type_id.is_node_property_type();
	}

	public final boolean is_edge_property() {
		return type_id.is_edge_property_type();
	}

	public final boolean is_property() {
		return type_id.is_property_type();
	}

	public final boolean is_node() {
		return type_id.is_node_type();
	}

	public final boolean is_edge() {
		return type_id.is_edge_type();
	}

	public final boolean is_nodeedge() {
		return type_id.is_nodeedge_type();
	}

	public final boolean is_collection() {
		return type_id.is_collection_type();
	}

	public final boolean is_collection_of_collection() {
		return type_id.is_collection_of_collection_type();
	}

	public final boolean is_node_collection() {
		return type_id.is_node_collection_type();
	}

	public final boolean is_edge_collection() {
		return type_id.is_edge_collection_type();
	}

	public final boolean is_collection_iterator() {
		return type_id.is_collection_iter_type();
	}

	public final boolean is_unknown_collection_iterator() {
		return type_id.is_unknown_collection_iter_type();
	}

	public final boolean is_node_iterator() {
		return type_id.is_node_iter_type();
	}

	public final boolean is_edge_iterator() {
		return type_id.is_edge_iter_type();
	}

	public final boolean is_node_edge_iterator() {
		return is_node_iterator() || is_edge_iterator();
	}

	public final boolean is_property_iterator() {
		return type_id.is_property_iter_type();
	}

	public final boolean is_numeric() {
		return type_id.is_numeric_type();
	}

	public final boolean is_node_compatible() {
		return type_id.is_node_compatible_type();
	}

	public final boolean is_edge_compatible() {
		return type_id.is_edge_compatible_type();
	}

	public final boolean is_node_edge_compatible() {
		return type_id.is_node_edge_compatible_type();
	}

	public final boolean is_boolean() {
		return type_id.is_boolean_type();
	}

	public final boolean is_reverse_iterator() {
		return type_id.is_iteration_use_reverse();
	}

	public final boolean has_target_graph() {
		return type_id.has_target_graph_type();
	}

	public final boolean is_void() {
		return type_id.is_void_type();
	}

	public final boolean is_all_graph_iterator() {
		return type_id.is_all_graph_iter_type();
	}

	public final boolean is_any_nbr_iterator() {
		return type_id.is_any_nbr_iter_type();
	}

	public final boolean is_common_nbr_iterator() {
		return type_id.is_common_nbr_iter_type();
	}

	public final boolean is_sequence_collection() {
		return type_id.is_sequence_collection_type();
	}

	public final boolean is_order_collection() {
		return type_id.is_order_collection_type();
	}

	public final boolean is_set_collection() {
		return type_id.is_set_collection_type();
	}

	public final boolean is_sequential_collection() {
		return type_id.is_sequential_collection_type();
	}

	public void reproduce(int ind_level) {
		if (is_primitive()) {
			Out.push(type_id.get_type_string());
		} else if (is_graph()) {
			switch (type_id) {
			case GMTYPE_GRAPH:
				Out.push("Graph");
				break;
			default:
				assert false;
				break;
			}
		} else if (is_node_property()) {
			assert target_type != null;
			assert target_graph != null;
			Out.push("N_P <");
			target_type.reproduce(0);
			Out.push(">(");
			target_graph.reproduce(0);
			Out.push(')');
		} else if (is_edge_property()) {
			assert target_type != null;
			assert target_graph != null;
			Out.push("E_P <");
			target_type.reproduce(0);
			Out.push(">(");
			target_graph.reproduce(0);
			Out.push(')');
		} else if (is_node()) {
			assert target_graph != null;
			Out.push("Node (");
			target_graph.reproduce(0);
			Out.push(')');
		} else if (is_edge()) {
			assert target_graph != null;
			Out.push("Edge (");
			target_graph.reproduce(0);
			Out.push(')');
		} else if (is_collection()) {
			assert target_graph != null;
			Out.push(type_id.get_type_string());
			Out.push('(');
			target_graph.reproduce(0);
			Out.push(')');
		} else if (is_void()) {
			// do nothing
		} else {
			assert false;
		}
	}

	public void dump_tree(int ind_level) {
		assert parent != null;
		GlobalMembersGm_dumptree.IND(ind_level);
		System.out.print("<TYPE ");
		System.out.printf(" type:%s", type_id.get_type_string());
		if (is_property()) {
			System.out.print("\n");
			target_type.dump_tree(ind_level + 1);
			target_graph.dump_tree(0);
			System.out.print("\n");
			GlobalMembersGm_dumptree.IND(ind_level);
		}
		if (is_nodeedge()) {
			target_graph.dump_tree(0);
			System.out.print("\n");
			GlobalMembersGm_dumptree.IND(ind_level);
		}
		System.out.print(">");
	}

	// there is no copying of type

	public final gm_symtab_entry get_target_graph_sym() {
		if (is_collection_iterator()) {
			assert target_collection != null;
			assert target_collection.getTypeInfo() != null;
			assert target_collection.getTypeInfo().get_target_graph_sym() != null;
			return target_collection.getTypeInfo().get_target_graph_sym();
		} else if (is_collection() || is_property() || is_nodeedge() || is_node_iterator() || is_edge_iterator() || is_collection_of_collection()) {
			assert target_graph != null;
			assert target_graph.getSymInfo() != null;
			return target_graph.getSymInfo();
		} else {
			System.out.println("type = " + type_id.get_type_string());
			assert false;
			return null;
		}
	}

	public final ast_id get_target_graph_id() {
		return target_graph;
	}

	public final ast_id get_target_collection_id() {
		return target_collection;
	}

	public final ast_id get_target_property_id() {
		return target_collection;
	}

	public final ast_id get_target_nbr_id() {
		return target_nbr;
	}

	public final ast_id get_target_nbr2_id() {
		return target_nbr2;
	}

	public final ast_typedecl get_target_type() {
		return target_type;
	}

	public final GMTYPE_T getTypeSummary() // same as get type id
	{
		return type_id;
	}

	public final void setTypeSummary(GMTYPE_T s) {
		// type id might be overriden during type-checking
		set_typeid(s);
	}

	public final GMTYPE_T getTargetTypeSummary() {
		assert is_property() || is_collection_of_collection();
		assert target_type != null;
		return target_type.getTypeSummary();
	}

	public final void set_target_graph_id(ast_id i) {

		assert target_graph == null;
		assert i.getTypeInfo() != null;
		target_graph = i;
		i.set_parent(this);
	}

	public final boolean is_well_defined() {
		return _well_defined;
	}

	public final void set_well_defined(boolean b) {
		_well_defined = b;
	}

	// for the compiler generated symbols
	// (when scope is not available)
	public void enforce_well_defined() {

		if (is_collection() || is_nodeedge() || is_all_graph_iterator() || is_property()) {
			if (is_property())
				assert target_type != null;
			assert target_graph != null;
			assert target_graph.getSymInfo() != null;
		} else if (is_any_nbr_iterator()) {
			assert target_nbr != null;
			assert target_nbr.getSymInfo() != null;
			if (target_graph == null) {
				target_graph = target_nbr.getTypeInfo().get_target_graph_id().copy(true);
			}
		} else if (is_collection_iterator()) {
			assert target_collection != null;
			assert target_collection.getSymInfo() != null;
			if (target_graph == null) {
				target_graph = target_collection.getTypeInfo().get_target_graph_id().copy(true);
			}
		}

		set_well_defined(true);
	}

	// defined in gm_frontend_api.h
	private GMTYPE_T type_id;
	private ast_typedecl target_type; // for property
	private ast_id target_graph; // for property, node, edge, set
	private ast_id target_collection; // for set-iterator set
	private ast_id target_nbr; // for nbr-iterator
	private ast_id target_nbr2; // for common neighbor iterator
	private boolean _well_defined;
}