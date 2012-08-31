package frontend;

import ast.AST_NODE_TYPE;
import ast.ast_bfs;
import ast.ast_foreach;
import ast.ast_sent;

import common.gm_apply;

//----------------------------------------------------------------------
// main entry to rw_analysis.
// (procedure locally) gather rw info and save it into each ast node.
//----------------------------------------------------------------------
//bool gm_frontend::do_rw_analysis(ast_procdef* p)

//===========================================================================

public class gm_delete_rw_analysis extends gm_apply {
	
	@Override
	public boolean apply(ast_sent n) {
		gm_rwinfo_sets rwi = (gm_rwinfo_sets) n.find_info(gm_rw_analysis.GM_INFOKEY_RW);
		if (rwi != null) {
			if (rwi != null)
				rwi.dispose();
			n.add_info(gm_rw_analysis.GM_INFOKEY_RW, null);
		}
		if (n.get_nodetype() == AST_NODE_TYPE.AST_FOREACH) {
			gm_bound_set_info bsi = gm_rw_analysis.gm_get_bound_set_info((ast_foreach) n);
			if (bsi != null)
				bsi.dispose();
			n.add_info(gm_rw_analysis.GM_INFOKEY_BOUND, null);
		} else if (n.get_nodetype() == AST_NODE_TYPE.AST_BFS) {
			gm_bound_set_info bsi = gm_rw_analysis.gm_get_bound_set_info((ast_bfs) n);
			if (bsi != null)
				bsi.dispose();
			n.add_info(gm_rw_analysis.GM_INFOKEY_BOUND, null);
		}

		return true;
	}
}