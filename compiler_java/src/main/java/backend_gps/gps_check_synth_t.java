package backend_gps;

import static backend_gps.GPSConstants.GPS_FLAG_USE_EDGE_PROP;
import inc.GMTYPE_T;
import ast.AST_NODE_TYPE;
import ast.ast_procdef;
import ast.ast_sent;

import common.GM_ERRORS_AND_WARNINGS;
import common.gm_error;
import common.gm_apply;

import frontend.SYMTAB_TYPES;
import frontend.gm_symtab_entry;

//------------------------------------------------
// Check basic things about if the program is synthesizable
//
//  1. BFS (yet) /DFS is not supported.
//      
//  2. Collections are not avaialble (yet). e.g. NodeSet(G) 
//
//  3. There should be one and only one Graph (as in argument)
//
//  4. There must be not 'Return' in non-master context
//
//  5. Some built-in functions are not supported
//------------------------------------------------

// check condition 1-4
public class gps_check_synth_t extends gm_apply {

	private boolean _error = false;
	private boolean _graph_defined = false;
	private int foreach_depth = 0;
	private ast_procdef proc;

	public gps_check_synth_t(ast_procdef p) {
		set_for_symtab(true);
		set_for_sent(true);
		set_separate_post_apply(true);
		proc = p;
	}

	public final boolean is_error() {
		return _error;
	}

	// pre apply
	@Override
	public boolean apply(ast_sent s) {
		if (s.get_nodetype() == AST_NODE_TYPE.AST_BFS) {
			gm_error.gm_backend_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_GPS_UNSUPPORTED_OP, s.get_line(), s.get_col(), "BFS or DFS");
			_error = true;
		}

		if (s.get_nodetype() == AST_NODE_TYPE.AST_RETURN) {
			if (foreach_depth > 0) {
				gm_error.gm_backend_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_GPS_UNSUPPORTED_OP, s.get_line(), s.get_col(), "Return inside foreach");
				_error = true;
			}
		}

		if (s.get_nodetype() == AST_NODE_TYPE.AST_FOREACH) {
			foreach_depth++;
		}

		return true;
	}

	@Override
	public boolean apply2(ast_sent s) {
		if (s.get_nodetype() == AST_NODE_TYPE.AST_FOREACH) {
			foreach_depth--;
		}
		return true;
	}

	// visit entry
	@Override
	public boolean apply(gm_symtab_entry e, SYMTAB_TYPES symtab_type) {
		GMTYPE_T type_id = e.getType().get_typeid();
		if (type_id.is_collection_type()) {
			gm_error.gm_backend_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_GPS_UNSUPPORTED_COLLECTION, e.getId().get_line(), e.getId().get_col(), e
					.getId().get_orgname());
			_error = true;
		}

		else if (type_id.is_edge_property_type()) {
			/*
			 * gm_backend_error( GM_ERROR_GPS_UNSUPPORTED_COLLECTION,
			 * e->getId()->get_line(), e->getId()->get_col(),
			 * e->getId()->get_orgname()); _error = true;
			 */
			proc.add_info_bool(GPS_FLAG_USE_EDGE_PROP, true);
		}

		else if (type_id.is_graph_type()) {
			if (_graph_defined) {
				gm_error.gm_backend_error(GM_ERRORS_AND_WARNINGS.GM_ERROR_GPS_MULTIPLE_GRAPH, e.getId().get_line(), e.getId().get_col(), e.getId()
						.get_orgname());
				_error = true;
			}
			_graph_defined = true;
		}

		return true;
	}

	public final boolean is_graph_defined() {
		return _graph_defined;
	}
}