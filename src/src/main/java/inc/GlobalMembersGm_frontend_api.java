package inc;


public class GlobalMembersGm_frontend_api
{

	// functions defined in GM_Lex
	//extern int GM_start_parse(tangible.RefObject<String> fname);
	//extern void GM_print_parse_error(String err_msg);
	//extern void GM_set_parse_error(boolean b);
	//extern boolean GM_is_parse_error();

	//---------------------------------------------------
	// AST Build functions
	// defined in GM_Frontend.cc (called by parser)
	//---------------------------------------------------
	//extern void GM_procdef_begin(ast_node id, boolean is_local);
	//extern void GM_procdef_add_argdecl(ast_node node);
	//extern void GM_procdef_add_out_argdecl(ast_node node);
	//extern void GM_procdef_setbody(ast_node body);
	//extern void GM_procdef_return_type(ast_node rt);
	//extern ast_node GM_procdef_arg(ast_node id, ast_node type);
	//extern void GM_procdef_finish();

	// creating expressions.  // l: line, c:column
	//extern ast_node GM_expr_ival(int lval, int l, int c);
	//extern ast_node GM_expr_fval(double fval, int l, int c);
	//extern ast_node GM_expr_bval(boolean b, int l, int c);
	//extern ast_node GM_expr_inf(boolean is_plus, int l, int c);
	//extern ast_node GM_expr_nil(int l, int c);
	//extern ast_node GM_expr_biop(ast_node left, ast_node right, int op, int l, int c);
	//extern ast_node GM_expr_uop(ast_node left, int op, int l, int c);
	//extern ast_node GM_expr_lbiop(ast_node left, ast_node right, int op, int l, int c);
	//extern ast_node GM_expr_luop(ast_node left, int op, int l, int c);
	//extern ast_node GM_expr_comp(ast_node left, ast_node right, int op, int l, int c);
	//extern ast_node GM_expr_id_access(ast_node id);
	//extern ast_node GM_expr_field_access(ast_node field);
	//extern ast_node GM_expr_conversion(ast_node left, ast_node type, int l, int c);
	//extern ast_node GM_expr_foreign(String text, int l, int c);
	//extern ast_node GM_expr_reduceop(int op, ast_node iter, ast_node src, int iter_op, ast_node body, ast_node filter, ast_node src2, int l, int c);
	//extern ast_node GM_expr_ternary(ast_node cond, ast_node left, ast_node right, int l, int c);
	//extern ast_node GM_expr_builtin_expr(ast_node id, ast_node id2, expr_list l);
	//extern ast_node GM_expr_builtin_field_expr(ast_node id, ast_node id2, expr_list list);

	//extern void GM_start_sentblock();
	//extern ast_node GM_finish_sentblock();
	//extern void GM_add_sent(ast_node s); // add sentence to current sentence block

	//extern ast_node GM_vardecl_prim(ast_node type, ast_node names);
	//extern ast_node GM_vardecl_and_assign(ast_node type, ast_node name, ast_node expr);

	//extern ast_node GM_normal_assign(ast_node lhs, ast_node rhs);
	//extern ast_node GM_reduce_assign(ast_node lhs, ast_node rhs, ast_node itor, int gm_reduce_t);
	//extern ast_node GM_argminmax_assign(ast_node lhs, ast_node rhs, ast_node itor, int gm_reduce_t, lhs_list l_list, expr_list r_list);
	//extern ast_node GM_defer_assign(ast_node lhs, ast_node rhs, ast_node itor);

	//extern ast_node GM_if(ast_node cond, ast_node then, ast_node els);
	//extern ast_node GM_while(ast_node cond, ast_node body);
	//extern ast_node GM_dowhile(ast_node cond, ast_node body);
	//extern ast_node GM_return(ast_node expr, int l, int c);

	//extern ast_node GM_foreach(ast_node id, ast_node source, int iter_typ, ast_node sent, ast_node expr, boolean is_seq, boolean is_backward, ast_node source2);
	//extern ast_node GM_bfs(ast_node it, ast_node source, ast_node root, ast_node navigator, ast_node f_filter, ast_node b_filter, ast_node f_sent, ast_node b_sent, boolean use_tp, boolean is_bfs);

	//extern ast_node GM_graphtype_ref(int graph_type_id);
	//extern ast_node GM_primtype_ref(int prim_type_id);
	//extern ast_node GM_nodeprop_ref(ast_node typedecl, ast_node id);
	//extern ast_node GM_nodetype_ref(ast_node id);
	//extern ast_node GM_edgeprop_ref(ast_node typedecl, ast_node id);
	//extern ast_node GM_edgetype_ref(ast_node id);

	//extern ast_node GM_settype_ref(int set_type_id, ast_node id);
	//extern ast_node GM_queuetype_ref(ast_node collectionType, ast_node id);

	//extern ast_node GM_id(tangible.RefObject<String> orgname, int line, int col);
	//extern ast_node GM_field(ast_node id, ast_node field, boolean is_rarrow);

	//extern void GM_add_id_comma_list(ast_node id);
	//extern ast_node GM_finish_id_comma_list();

	//extern void GM_set_lineinfo(ast_node n, int line, int col);

	//extern expr_list GM_empty_expr_list();
	//extern expr_list GM_single_expr_list(ast_node id);
	//extern expr_list GM_add_expr_front(ast_node id, expr_list list);

	//extern lhs_list GM_single_lhs_list(ast_node id);
	//extern lhs_list GM_add_lhs_list_front(ast_node id, lhs_list list);

	//extern ast_node GM_new_call_sent(ast_node n, boolean is_builtin);

	//extern ast_node GM_foreign_sent(ast_node foreign);
	//extern ast_node GM_foreign_sent_mut(ast_node foreign, lhs_list list);

}