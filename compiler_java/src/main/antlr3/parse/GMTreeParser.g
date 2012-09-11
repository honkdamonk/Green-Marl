tree grammar GMTreeParser;

options {
    language     = Java;
    tokenVocab   = GM;
    ASTLabelType = Tree;
    backtrack    = true;
}

@header {
    package parse;
    import ast.*;
    import inc.*;
}

prog
    :   proc_def*
    ;

proc_def
    :   proc_head
    	proc_body
        { FE.GM_procdef_finish(); }
    ;

proc_head
    :   proc_name '(' arg_declist?                 ')' proc_return?
    |   proc_name '(' arg_declist? ';' arg_declist ')' proc_return?
    ;


proc_name
    :   T_PROC x=id
        { FE.GM_procdef_begin(x, false); }
    |   T_LOCAL x=id
        { FE.GM_procdef_begin(x, true); }
    ;

arg_declist
    :   ( x=arg_decl { FE.GM_procdef_add_argdecl(x); } )*
    ;

/* return of function should be always primitive type */
proc_return
    :   ':' x=prim_type
    	{ FE.GM_procdef_return_type(x); }
    |   ':' x=node_type
    	{ FE.GM_procdef_return_type(x); }
    ;

arg_decl returns [ast_node value]
    :   x=arg_target ':' y=typedecl
        { value = FE.GM_procdef_arg(x, y); }
    ;

arg_target returns [ast_node value]
    :   id_comma_list
        { value = FE.GM_finish_id_comma_list(); }
    ;

typedecl returns [ast_node value]
    :   x=graph_type
        { value = x; } 
    |   x=prim_type
    	{ value = x; }
    |   x=property
    	{ value = x; }
    |   x=nodeedge_type
    	{ value = x; }
    |   x=set_type
    	{ value = x; }
    ;

graph_type returns [ast_node value]
    :   T_GRAPH
        { value = FE.GM_graphtype_ref(GMTYPE_T.GMTYPE_GRAPH);  
          FE.GM_set_lineinfo(value, $T_GRAPH.getLine(), $T_GRAPH.getCharPositionInLine()); }
    ;


prim_type returns [ast_node value]
    :   T_INT
    	{ value = FE.GM_primtype_ref(GMTYPE_T.GMTYPE_INT);
          FE.GM_set_lineinfo(value, $T_INT.getLine(), $T_INT.getCharPositionInLine()); }
    |   T_LONG
    	{ value = FE.GM_primtype_ref(GMTYPE_T.GMTYPE_LONG);
          FE.GM_set_lineinfo(value, $T_LONG.getLine(), $T_LONG.getCharPositionInLine()); }
    |   T_FLOAT
    	{ value = FE.GM_primtype_ref(GMTYPE_T.GMTYPE_FLOAT);
          FE.GM_set_lineinfo(value, $T_FLOAT.getLine(), $T_FLOAT.getCharPositionInLine()); }
    |   T_DOUBLE
    	{ value = FE.GM_primtype_ref(GMTYPE_T.GMTYPE_DOUBLE); 
          FE.GM_set_lineinfo(value, $T_DOUBLE.getLine(), $T_DOUBLE.getCharPositionInLine()); }
    |   T_BOOL
    	{ value = FE.GM_primtype_ref(GMTYPE_T.GMTYPE_BOOL);
          FE.GM_set_lineinfo(value, $T_BOOL.getLine(), $T_BOOL.getCharPositionInLine()); }
    ;

nodeedge_type returns [ast_node value]
    :   x=node_type
    	{ value = x; }
    |   x=edge_type
    	{ value = x; }
    ;


node_type returns [ast_node value]
    :   T_NODE ( '(' x=id ')' )?
    	{ value = FE.GM_nodetype_ref(x);
    	  FE.GM_set_lineinfo(value, $T_NODE.getLine(), $T_NODE.getCharPositionInLine()); }
    ;


edge_type returns [ast_node value]
    :   T_EDGE ( '(' x=id ')' )?
    	{ value = FE.GM_edgetype_ref(x);
    	  FE.GM_set_lineinfo(value, $T_EDGE.getLine(), $T_EDGE.getCharPositionInLine()); }
    ;

set_type returns [ast_node value]
    :   T_NSET ( '(' y=id ')' )?
    	{ value = FE.GM_settype_ref(GMTYPE_T.GMTYPE_NSET, y);
    	  FE.GM_set_lineinfo(value, $T_NSET.getLine(), $T_NSET.getCharPositionInLine()); }
    |   T_NSEQ ( '(' y=id ')' )?
    	{ value = FE.GM_settype_ref(GMTYPE_T.GMTYPE_NSEQ, y);
    	  FE.GM_set_lineinfo(value, $T_NSEQ.getLine(), $T_NSEQ.getCharPositionInLine()); }
    |   T_NORDER ( '(' y=id ')' )?
    	{ value = FE.GM_settype_ref(GMTYPE_T.GMTYPE_NORDER, y);
    	  FE.GM_set_lineinfo(value, $T_NORDER.getLine(), $T_NORDER.getCharPositionInLine()); }
    |   T_COLLECTION '<' x=set_type '>' ( '(' y=id ')' )?
    	{ value = FE.GM_queuetype_ref(x, y);
    	  FE.GM_set_lineinfo(value, $T_COLLECTION.getLine(), $T_COLLECTION.getCharPositionInLine()); }
    ;

property returns [ast_node value]
    :   T_NODEPROP '<' x=prim_type '>' ( '(' y=id ')' )?
    	{ value = FE.GM_nodeprop_ref(x, y);
      	  FE.GM_set_lineinfo(value, $T_NODEPROP.getLine(), $T_NODEPROP.getCharPositionInLine()); }
    |   T_NODEPROP '<' x=nodeedge_type '>' ( '(' y=id ')' )?
    	{ value = FE.GM_nodeprop_ref(x, y);
    	  FE.GM_set_lineinfo(value, $T_NODEPROP.getLine(), $T_NODEPROP.getCharPositionInLine()); }
    |   T_NODEPROP '<' x=set_type '>' ( '(' y=id ')' )?
    	{ value = FE.GM_nodeprop_ref(x, y);
    	  FE.GM_set_lineinfo(value, $T_NODEPROP.getLine(), $T_NODEPROP.getCharPositionInLine()); }
    |   T_EDGEPROP '<' x=prim_type '>' ( '(' y=id ')' )?
    	{ value = FE.GM_edgeprop_ref(x, y);
    	  FE.GM_set_lineinfo(value, $T_EDGEPROP.getLine(), $T_EDGEPROP.getCharPositionInLine()); }
    |   T_EDGEPROP '<' x=nodeedge_type '>' ( '(' y=id ')' )?
    	{ value = FE.GM_edgeprop_ref(x, y);
    	  FE.GM_set_lineinfo(value, $T_EDGEPROP.getLine(), $T_EDGEPROP.getCharPositionInLine()); }
    |   T_EDGEPROP '<' x=set_type '>' ( '(' y=id ')' )?
    	{ value = FE.GM_edgeprop_ref(x, y);
    	  FE.GM_set_lineinfo(value, $T_EDGEPROP.getLine(), $T_EDGEPROP.getCharPositionInLine()); }
    ;

id_comma_list
    :   ( x=id { FE.GM_add_id_comma_list(x); } )*
    ;

proc_body returns [ast_node value]
    :   x=sent_block { FE.GM_procdef_setbody(x); }
    ;


sent_block returns [ast_node value]
    :   sb_begin
    	{ FE.GM_start_sentblock(); }
        sent_list
        sb_end
        { value = FE.GM_finish_sentblock(); }
    ;

sb_begin
    :   '{'
    ;

sb_end
    :   '}'
    ;

sent_list returns [ast_node value]
    :   (x=sent { if (x!=null) FE.GM_add_sent(x); } )*
    ;
    
sent returns [ast_node value]
    :   x=sent_assignment ';'
    	{ value = x; }
    |   x=sent_variable_decl ';'
    	{ value = x; }
    |   x=sent_block
    	{ value = x; }
    |   x=sent_foreach
    	{ value = x; }
    |   x=sent_if
    	{ value = x; }
    |   x=sent_reduce_assignment ';'
    	{ value = x; }
    |   x=sent_defer_assignment ';'
    	{ value = x; }
    |   x=sent_do_while ';'
    	{ value = x; }
    |   x=sent_while
    	{ value = x; }
    |   x=sent_return ';'
    	{ value = x; }
    |   x=sent_bfs
    	{ value = x; }
    |   x=sent_dfs
    	{ value = x; }
    |   x=sent_call ';'
    	{ value = x; }
    |   x=sent_user ';'
    	{ value = x; }
    |   x=sent_argminmax_assignment ';'
    	{ value = x; }
    |   ';'
    	{ value = null; }
    ;
    
sent_call returns [ast_node value]
    :   x=built_in
    	{ value = FE.GM_new_call_sent(x, true); }
    ;


sent_while returns [ast_node value]
    :   T_WHILE
        '(' x=bool_expr ')'
        y=sent_block
        { value = FE.GM_while(x, y); }
    ;


sent_do_while returns [ast_node value]
    :   T_DO
        y=sent_block
        T_WHILE
        '(' x=bool_expr ')'
        { value = FE.GM_dowhile(x, y); }
    ;
    
sent_foreach returns [ast_node value]
    :   T_FOREACH
        x=foreach_header
        y=foreach_filter?
        z=sent
        { value = FE.GM_foreach(x.p1, x.p2, x.i1, z, y, false, x.b1, x.p3);
          FE.GM_set_lineinfo(value, $T_FOREACH.getLine(), $T_FOREACH.getCharPositionInLine()); }
    |   T_FOR
        x=foreach_header
        y=foreach_filter?
        z=sent
        { value = FE.GM_foreach(x.p1, x.p2, x.i1, z, y, true, x.b1, x.p3);
          FE.GM_set_lineinfo(value, $T_FOR.getLine(), $T_FOR.getCharPositionInLine()); }
    ;


foreach_header returns [ast_node p1, ast_node p2, boolean b1, GMTYPE_T i1, ast_node p3]
    :   '(' x=id ':' y=id     '.' z=iterator1 ')'
    	{ retval.p1 = x; retval.p2 = y; retval.b1 = false; retval.i1 = z.i1; retval.p3 = z.p1; }
    |   '(' x=id ':' y=id '+' '.' z=iterator1 ')'
    	{ retval.p1 = x; retval.p2 = y; retval.b1 = false; retval.i1 = z.i1; retval.p3 = z.p1; }
    |   '(' x=id ':' y=id '-' '.' z=iterator1 ')'
    	{ retval.p1 = x; retval.p2 = y; retval.b1 = true;  retval.i1 = z.i1; retval.p3 = z.p1; }
    ;


foreach_filter returns [ast_node value]
    :   x=bool_expr
    	{ value = x; }
    ;


iterator1 returns [GMTYPE_T i1, ast_node p1]
    :   T_NODES
    	{ retval.i1 = GMTYPE_T.GMTYPE_NODEITER_ALL; retval.p1 = null; }
    |   T_EDGES
    	{ retval.i1 = GMTYPE_T.GMTYPE_EDGEITER_ALL; retval.p1 = null; }
    |   T_NBRS
    	{ retval.i1 = GMTYPE_T.GMTYPE_NODEITER_NBRS; retval.p1 = null; }
    |   T_IN_NBRS
    	{ retval.i1 = GMTYPE_T.GMTYPE_NODEITER_IN_NBRS; retval.p1 = null; }
    |   T_UP_NBRS
    	{ retval.i1 = GMTYPE_T.GMTYPE_NODEITER_UP_NBRS; retval.p1 = null; }
    |   T_DOWN_NBRS
    	{ retval.i1 = GMTYPE_T.GMTYPE_NODEITER_DOWN_NBRS; retval.p1 = null; }
    |   T_ITEMS
    	{ retval.i1 = GMTYPE_T.GMTYPE_ITER_ANY; retval.p1 = null; /* should be resolved after typechecking */}
    |   T_COMMON_NBRS '(' x=id ')'
    	{ retval.i1 = GMTYPE_T.GMTYPE_NODEITER_COMMON_NBRS; retval.p1 = x; }
    ;


sent_dfs returns [ast_node value]
    :   T_DFS
        w=bfs_header_format
        x=bfs_filters
        y=sent_block
        z=dfs_post
        { value = FE.GM_bfs(w.p1, w.p2, w.p3, x.p1, x.p2, z.p2, y, z.p1, w.b1, false);
          FE.GM_set_lineinfo(value, $T_DFS.getLine(), $T_DFS.getCharPositionInLine()); }
    ;


sent_bfs returns [ast_node value]
    :   T_BFS
        w=bfs_header_format
        x=bfs_filters
        y=sent_block
        z=bfs_reverse
        { value = FE.GM_bfs(w.p1, w.p2, w.p3, x.p1, x.p2, z.p2, y, z.p1, w.b1, true);
          FE.GM_set_lineinfo(value, $T_BFS.getLine(), $T_BFS.getCharPositionInLine()); }
    ;


dfs_post returns [ast_node p1, ast_node p2]
	:	// empty
		{ retval.p1 = null; retval.p2 = null; }
    |   T_POST
        x=bfs_filter?
        y=sent_block
        { retval.p1 = y; retval.p2 = x; }
    ;


bfs_reverse returns [ast_node p1, ast_node p2]
	:	// empty
		{ retval.p1 = null; retval.p2 = null; }
    |   T_BACK
        x=bfs_filter?
        y=sent_block
        { retval.p1 = y; retval.p2 = x; }
    ;


bfs_header_format returns [ast_node p1, ast_node p2, boolean b1, ast_node p3]
    :   '(' w=id ':' x=id y=opt_tp '.' T_NODES from_or_semi z=id ')'
    	{ retval.p1 = w; // it
          retval.p2 = x; // source
          retval.b1 = y; // optional tp
          retval.p3 = z; // source
        }
    ;


opt_tp returns [boolean x]
	:	// empty
		{ x = false; }
	|	'^'
		{ x = true; }
	;


from_or_semi
    :   T_FROM
    |   ';'
    ;


bfs_filters returns [ast_node p1, ast_node p2]
	:	// empty
		{ retval.p1 = null; retval.p2 = null; }
    |   x=bfs_navigator
    	{ retval.p1 = x;    retval.p2 = null; }
    |   y=bfs_filter
    	{ retval.p1 = null; retval.p2 = y; }
    |   x=bfs_navigator y=bfs_filter
    	{ retval.p1 = x;    retval.p2 = y; }
    |   y=bfs_filter    x=bfs_navigator
    	{ retval.p1 = x;    retval.p2 = y; }
    ;


bfs_navigator returns [ast_node value]
    :   '[' x=expr ']'
    	{ value = x; }
    ;


bfs_filter returns [ast_node value]
    :   '(' x=expr ')'
    	{ value = x; }
    ;


sent_variable_decl returns [ast_node value]
    :   x=typedecl y=id '=' z=rhs
    	{ value = FE.GM_vardecl_and_assign(x, y, z); }
    |   x=typedecl y=var_target
		{ value = FE.GM_vardecl_prim(x, y); }
    ;


var_target returns [ast_node value]
    :   id_comma_list
    	{ value = FE.GM_finish_id_comma_list(); }
    ;


sent_assignment returns [ast_node value]
    :   x=lhs y='=' z=rhs
    	{ value = FE.GM_normal_assign(x, z);
    	  FE.GM_set_lineinfo(value, y.getLine(), y.getCharPositionInLine()); }
    ;


sent_reduce_assignment returns [ast_node value]
    :   w=lhs
        x=reduce_eq
        y=rhs
        z=optional_bind
        { value = FE.GM_reduce_assign(w, y, z, x);
          FE.GM_set_lineinfo(value, 0, 0); } /* TODO: should be x.getLine(), x.getCharPositionInLine() */
    |   w=lhs
        T_PLUSPLUS
        z=optional_bind
        { value = FE.GM_reduce_assign(w, FE.GM_expr_ival(1, $T_PLUSPLUS.getLine(), $T_PLUSPLUS.getCharPositionInLine()), z, GM_REDUCE_T.GMREDUCE_PLUS); }
    ;


sent_defer_assignment returns [ast_node value]
    :
    w=lhs
    T_LE
    y=rhs
    z=optional_bind
    { value = FE.GM_defer_assign(w, y, z);
      FE.GM_set_lineinfo(value, $T_LE.getLine(), $T_LE.getCharPositionInLine()); }
    ;


sent_argminmax_assignment returns [ast_node value]
    :
    w=lhs_list2
    x=minmax_eq
    y=rhs_list2
    z=optional_bind
    { value = FE.GM_argminmax_assign(w.p1, y.p1, z, x, w.l_list, y.e_list);
      FE.GM_set_lineinfo(value, 0, 0); } /* TODO: should be x.getLine(), x.getCharPositionInLine() */
    ;


optional_bind returns [ast_node value]
    :	// empty
    	{ value = null; }
    |   '@' x=id
    	{ value = x; }
    ;


reduce_eq returns [GM_REDUCE_T value]
    :   T_PLUSEQ
    	{ value = GM_REDUCE_T.GMREDUCE_PLUS; }
    |   T_MULTEQ
    	{ value = GM_REDUCE_T.GMREDUCE_MULT; }
    |   T_MINEQ
    	{ value = GM_REDUCE_T.GMREDUCE_MIN; }
    |   T_MAXEQ
    	{ value = GM_REDUCE_T.GMREDUCE_MAX; }
    |   T_ANDEQ
    	{ value = GM_REDUCE_T.GMREDUCE_AND; }
    |   T_OREQ
    	{ value = GM_REDUCE_T.GMREDUCE_OR; }
    ;


minmax_eq returns [GM_REDUCE_T value]
    :   T_MINEQ
    	{ value = GM_REDUCE_T.GMREDUCE_MIN; }
    |   T_MAXEQ
    	{ value = GM_REDUCE_T.GMREDUCE_MAX; }
    ;


rhs returns [ast_node value]
    :   x=expr
    	{ value = x; }
    ;


sent_return returns [ast_node value]
    :   T_RETURN
        x=expr?
        { value = FE.GM_return(x, $T_RETURN.getLine(), $T_RETURN.getCharPositionInLine()); }
    ;

/* This causes a shift-reduce conflict: What would be If (x) If (y) Else z;
 * The default action is to interpret it as If (x) {If (y) Else z;}, which is what C does.
 * */
sent_if returns [ast_node value]
    :   T_IF '(' x=bool_expr ')'
        y=sent
        ( T_ELSE z=sent )?
        { value = FE.GM_if(x, y, z); }
    ;


sent_user returns [ast_node value]
	:	x=expr_user
		{ value = FE.GM_foreign_sent(x); }
    |   x=expr_user T_DOUBLE_COLON '[' y=lhs_list ']'
    	{ value = FE.GM_foreign_sent_mut(x, y); }
    ;


expr returns [ast_node value]
    :   x=left_recursive_expr
    	{ value = x; }
    |   x=not_left_recursive_expr
    	{ value = x; }
    ;


not_left_recursive_expr returns [ast_node value]
    :   '(' e1=expr ')'
    	{ value = e1; }
    |   op='|' e1=expr '|'
    	{ value = FE.GM_expr_uop(e1, GM_OPS_T.GMOP_ABS, op.getLine(), op.getCharPositionInLine()); }
    |   op='-' e1=expr
    	{ value = FE.GM_expr_uop(e1, GM_OPS_T.GMOP_NEG, op.getLine(), op.getCharPositionInLine()); }
    |   op='!' e1=expr
    	{ value = FE.GM_expr_luop(e1, GM_OPS_T.GMOP_NOT, op.getLine(), op.getCharPositionInLine()); }
    |   op='(' pt=prim_type ')' e1=expr
    	{ value = FE.GM_expr_conversion(e1, pt, op.getLine(), op.getCharPositionInLine()); }
    |   rop=reduce_op
        '(' i1=id ':' i2=id '.' it=iterator1 ')'
        ( '(' e1=expr ')' )?
        '{' e2=expr '}'
        { value = FE.GM_expr_reduceop(rop, i1, i2, it.i1, e1, e2, it.p1, 0, 0); } /* TODO: should be rop.getLine(), rop.getCharPositionInLine() */
    |   rop=reduce_op2
        '(' i1=id ':' i2=id '.' it=iterator1 ')'
        ( '(' e1=expr ')' )?
        { value = FE.GM_expr_reduceop(rop, i1, i2, it.i1, 
          FE.GM_expr_ival(1, 0, 0), /* TODO: should be rop.getLine(), rop.getCharPositionInLine() */
          e1, it.p1, 0, 0); } /* TODO: should be rop.getLine(), rop.getCharPositionInLine() */
    |   b=BOOL_VAL
    	{ value = FE.GM_expr_bval(b.getText().equals("True") ? true : false, b.getLine(), b.getCharPositionInLine()); }
    |   i=INT_NUM
    	{ value = FE.GM_expr_ival(Integer.parseInt(i.getText()), i.getLine(), i.getCharPositionInLine()); }
    |   f=FLOAT_NUM
    	{ value = FE.GM_expr_fval(Double.parseDouble(f.getText()), f.getLine(), f.getCharPositionInLine()); }
    |   nf=inf
    	{ value = FE.GM_expr_inf(nf, 0, 0); } /* TODO: should be inf.getLine(), inf.getCharPositionInLine() */
    |   nil=T_NIL
    	{ value = FE.GM_expr_nil(nil.getLine(), nil.getCharPositionInLine()); }
    |   s=scala
    	{ value = FE.GM_expr_id_access(s); }
    |   fld=field
    	{ value = FE.GM_expr_field_access(fld); }
    |   bi=built_in
    	{ value = bi; }
    |   eu=expr_user
    	{ value = eu; }
    ;


left_recursive_expr returns [ast_node value]
    :   x=conditional_expr
    	{ value = x; }
    ;


conditional_expr returns [ast_node value]
    :   x=conditional_or_expr
    	{ value = x; }
        (
        	op='?' y=expr ':' z=conditional_expr
        	{ value = FE.GM_expr_ternary(x, y, z, op.getLine(), op.getCharPositionInLine()); }
        )?
    ;


conditional_or_expr returns [ast_node value]
    :   x=conditional_and_expr
    	{ value = x; }
        (
        	T_OR y=conditional_and_expr
        	{ value = FE.GM_expr_lbiop(x, y, GM_OPS_T.GMOP_OR, $T_OR.getLine(), $T_OR.getCharPositionInLine()); }
        )*
    ;


conditional_and_expr returns [ast_node value]
    :   x=equality_expr
    	{ value = x; }
        (
        	T_AND y=equality_expr
        	{ value = FE.GM_expr_lbiop(x, y, GM_OPS_T.GMOP_AND, $T_AND.getLine(), $T_AND.getCharPositionInLine()); }
        )*
    ;


equality_expr returns [ast_node value]
    :   x=relational_expr
    	{ value = x; }
        (
            (
            	T_EQ y=relational_expr
            	{ value = FE.GM_expr_comp(x, y, GM_OPS_T.GMOP_EQ, $T_EQ.getLine(), $T_EQ.getCharPositionInLine()); }
            ) | (
            	T_NEQ y=relational_expr
            	{ value = FE.GM_expr_comp(x, y, GM_OPS_T.GMOP_NEQ, $T_NEQ.getLine(), $T_NEQ.getCharPositionInLine()); }
            )
        )*
    ;


relational_expr returns [ast_node value]
    :   x=additive_expr
    	{ value = x; }
        (
        	(
        		T_LE y=additive_expr
        		{ value = FE.GM_expr_comp(x, y, GM_OPS_T.GMOP_LE, $T_LE.getLine(), $T_LE.getCharPositionInLine()); }
        	) | (
        		T_GE y=additive_expr
        		{ value = FE.GM_expr_comp(x, y, GM_OPS_T.GMOP_GE, $T_GE.getLine(), $T_GE.getCharPositionInLine()); }
        	) | (
        		op='<' y=additive_expr
        		{ value = FE.GM_expr_comp(x, y, GM_OPS_T.GMOP_LT, op.getLine(), op.getCharPositionInLine()); }
        	) | (
        		op='>' y=additive_expr
        		{ value = FE.GM_expr_comp(x, y, GM_OPS_T.GMOP_GT, op.getLine(), op.getCharPositionInLine()); }
        	)
        )*
    ;

  
additive_expr returns [ast_node value]
    :   x=multiplicative_expr
    	{ value = x; }
        (
            (
            	op='+' y=multiplicative_expr
            	{ value = FE.GM_expr_biop(x, y, GM_OPS_T.GMOP_ADD, op.getLine(), op.getCharPositionInLine()); }
            ) | (
            	op='-' y=multiplicative_expr
            	{ value = FE.GM_expr_biop(x, y, GM_OPS_T.GMOP_SUB, op.getLine(), op.getCharPositionInLine()); }
            )
        )*
    ;


multiplicative_expr returns [ast_node value]
    :   x=not_left_recursive_expr
    	{ value = x; }
        (
            (
            	op='*' y=not_left_recursive_expr
            	{ value = FE.GM_expr_biop(x, y, GM_OPS_T.GMOP_MULT, op.getLine(), op.getCharPositionInLine()); }
            ) | (
            	op='/' y=not_left_recursive_expr
            	{ value = FE.GM_expr_biop(x, y, GM_OPS_T.GMOP_DIV, op.getLine(), op.getCharPositionInLine()); }
            ) | (
            	op='%' y=not_left_recursive_expr
            	{ value = FE.GM_expr_biop(x, y, GM_OPS_T.GMOP_MOD, op.getLine(), op.getCharPositionInLine()); }
            )
        )*
    ;

/* bool/numeric expr cannot be distinguished by the syntax,
until type is available. due to vars */

bool_expr returns [ast_node value]
    :   x=expr
    	{ value = x; }
    ;


numeric_expr returns [ast_node value]
    :   x=expr
    	{ value = x; }
    ;


reduce_op returns [GM_REDUCE_T value]
    :   T_SUM
    	{ value = GM_REDUCE_T.GMREDUCE_PLUS; }
    |   T_PRODUCT
    	{ value = GM_REDUCE_T.GMREDUCE_MULT; }
    |   T_MIN
    	{ value = GM_REDUCE_T.GMREDUCE_MIN; }
    |   T_MAX
    	{ value = GM_REDUCE_T.GMREDUCE_MAX; }
    |   T_EXIST
    	{ value = GM_REDUCE_T.GMREDUCE_OR; }
    |   T_ALL
    	{ value = GM_REDUCE_T.GMREDUCE_AND; }
    |   T_AVG
    	{ value = GM_REDUCE_T.GMREDUCE_AVG; /* syntactic sugar*/ }
    ;


reduce_op2 returns [GM_REDUCE_T value]
    :   T_COUNT
    	{ value = GM_REDUCE_T.GMREDUCE_PLUS; }
    ;


inf returns [boolean value]
    :   T_P_INF
    	{ value = true; }
    |   T_M_INF
    	{ value = false; }
    ;


lhs returns [ast_node value]
    :   x=scala
    	{ value = x; }
    |   x=field
    	{ value = x; }
    ;


lhs_list returns [lhs_list value]
    :   x=lhs
    	{ value = FE.GM_single_lhs_list(x); }
        ( ',' y=lhs_list { value = FE.GM_add_lhs_list_front(x, y); } )*
    ;


scala returns [ast_node value]
    :   x=id
    	{ value = x; }
    ;


field returns [ast_node value]
    :   x=id '.' y=id
    	{ value = FE.GM_field(x, y, false); }
    |   T_EDGE
        '(' x=id ')'
        '.' y=id
        { value = FE.GM_field(x, y, true); }
    ;


built_in returns [ast_node value]
    :   ( x=id '.' )?
        y=id
        z=arg_list
        { value = FE.GM_expr_builtin_expr(x, y, z); }
    |   x=field
        '.' y=id
        z=arg_list
        { value = FE.GM_expr_builtin_field_expr(x, y, z); }
    ;


arg_list returns [expr_list value]
    :   '(' x=expr_list ')'
    	{ value = x; }
    |	'(' ')'
    	{ value = FE.GM_empty_expr_list(); }
    ;


expr_list returns [expr_list value]
    :   x=expr { value = FE.GM_single_expr_list(x); }
        ( ',' y=expr_list { value = FE.GM_add_expr_front(x, y); } )*
    ;


lhs_list2 returns [ast_node p1, lhs_list l_list]
    :   '<' x=lhs ';' y=lhs_list '>'
    	{ retval.p1 = x; retval.l_list = y; }
    ;


rhs_list2 returns [ast_node p1, expr_list e_list]
    :   '<' x=expr ';' y=expr_list '>'
    	{ retval.p1 = x; retval.e_list = y; }
    ;


expr_user returns [ast_node value]
    :   { /* FE.GM_lex_begin_user_text(); */ } 
    	'[' x='XXX' ']'
    	{ value = FE.GM_expr_foreign(x.getText(), x.getLine(), x.getCharPositionInLine()); }
    ;
/* USER_TEXT*/

id returns [ast_node value]
    :   ID { value = FE.GM_id($ID.getText(), $ID.getLine(), $ID.getCharPositionInLine()); }
    ;