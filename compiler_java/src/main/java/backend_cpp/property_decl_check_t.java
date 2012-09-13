package backend_cpp;

import ast.AST_NODE_TYPE;
import ast.ast_sent;
import ast.ast_sentblock;

import common.gm_apply;

import frontend.gm_symtab;

//--------------------------------------------------------------------
// Checking routines for temporary procedure declaration and removal
//   (1) Check if there are any temoprary properties in ths procedure
//   (2) Mark each sentence-block if it has property declaration
//   (3) Mark entry sentence block
//--------------------------------------------------------------------
class property_decl_check_t extends gm_apply {
	
	boolean has_prop_decl = false;
	
	@Override
	public boolean apply(ast_sent s) {
		if (s.get_nodetype() == AST_NODE_TYPE.AST_SENTBLOCK) {
			ast_sentblock sb = (ast_sentblock) s;
			gm_symtab e = sb.get_symtab_field();

			if (e.get_entries().size() != 0) {
				has_prop_decl = true;
				s.add_info_bool(gm_cpp_gen.CPPBE_INFO_HAS_PROPDECL, true);
			}
		}
		return true;
	}

}