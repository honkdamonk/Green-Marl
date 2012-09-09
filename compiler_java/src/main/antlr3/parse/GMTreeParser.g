tree grammar GMTreeParser;

options {
    tokenVocab   = GM;
    ASTLabelType = Tree;
    backtrack    = true;
}

@header {
    package parse;
    import ast.*;
    import inc.GMTYPE_T;
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
    :   proc_name
        arg_declist?
        proc_return?
    |   proc_name
        arg_declist? ';' arg_declist
        proc_return?
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
    :   prim_type
    |   node_type
    ;

arg_decl returns [ast_node value]
    :   x=arg_target y=typedecl
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
    :   T_NODE x=id?
    	{ value = FE.GM_nodetype_ref(x);
    	  FE.GM_set_lineinfo(value, $T_NODE.getLine(), $T_NODE.getCharPositionInLine()); }
    ;


edge_type returns [ast_node value]
    :   T_EDGE x=id?
    	{ value = FE.GM_edgetype_ref(x);
    	  FE.GM_set_lineinfo(value, $T_EDGE.getLine(), $T_EDGE.getCharPositionInLine()); }
    ;

set_type returns [ast_node value]
    :   T_NSET y=id?
    	{ value = FE.GM_settype_ref(GMTYPE_T.GMTYPE_NSET, y);
    	  FE.GM_set_lineinfo(value, $T_NSET.getLine(), $T_NSET.getCharPositionInLine()); }
    |   T_NSEQ y=id?
    	{ value = FE.GM_settype_ref(GMTYPE_T.GMTYPE_NSEQ, y);
    	  FE.GM_set_lineinfo(value, $T_NSEQ.getLine(), $T_NSEQ.getCharPositionInLine()); }
    |   T_NORDER y=id?
    	{ value = FE.GM_settype_ref(GMTYPE_T.GMTYPE_NORDER, y);
    	  FE.GM_set_lineinfo(value, $T_NORDER.getLine(), $T_NORDER.getCharPositionInLine()); }
    |   T_COLLECTION x=set_type y=id?
    	{ value = FE.GM_queuetype_ref(x, y);
    	  FE.GM_set_lineinfo(value, $T_COLLECTION.getLine(), $T_COLLECTION.getCharPositionInLine()); }
    ;

property returns [ast_node value]
    :   T_NODEPROP x=prim_type y=id?
    	{ value = FE.GM_nodeprop_ref(x, y);
      	  FE.GM_set_lineinfo(value, $T_NODEPROP.getLine(), $T_NODEPROP.getCharPositionInLine()); }
    |   T_NODEPROP x=nodeedge_type y=id?
    	{ value = FE.GM_nodeprop_ref(x, y);
    	  FE.GM_set_lineinfo(value, $T_NODEPROP.getLine(), $T_NODEPROP.getCharPositionInLine()); }
    |   T_NODEPROP x=set_type y=id?
    	{ value = FE.GM_nodeprop_ref(x, y);
    	  FE.GM_set_lineinfo(value, $T_NODEPROP.getLine(), $T_NODEPROP.getCharPositionInLine()); }
    |   T_EDGEPROP x=prim_type y=id?
    	{ value = FE.GM_edgeprop_ref(x, y);
    	  FE.GM_set_lineinfo(value, $T_EDGEPROP.getLine(), $T_EDGEPROP.getCharPositionInLine()); }
    |   T_EDGEPROP x=nodeedge_type y=id?
    	{ value = FE.GM_edgeprop_ref(x, y);
    	  FE.GM_set_lineinfo(value, $T_EDGEPROP.getLine(), $T_EDGEPROP.getCharPositionInLine()); }
    |   T_EDGEPROP x=set_type y=id?
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
    :   { FE.GM_start_sentblock(); }
        sent_list
        { value = FE.GM_finish_sentblock(); }
    ;

sent_list returns [ast_node value]
    :   (x=sent { if (x!=null) FE.GM_add_sent(x); } )*
    ;
    
sent returns [ast_node value]
    :   x=sent_assignment
    	{ value = x; }
    |   x=sent_variable_decl
    	{ value = x; }
/*    |   x=sent_block
    	{ value = x; }*/
    |   x=sent_foreach
    	{ value = x; }
    |   x=sent_if
    	{ value = x; }
    |   x=sent_reduce_assignment
    	{ value = x; }
    |   x=sent_defer_assignment
    	{ value = x; }
    |   x=sent_do_while
    	{ value = x; }
    |   x=sent_while
    	{ value = x; }
    |   x=sent_return
    	{ value = x; }
    |   x=sent_bfs
    	{ value = x; }
    |   x=sent_dfs
    	{ value = x; }
    |   x=sent_call
    	{ value = x; }
    |   x=sent_user
    	{ value = x; }
    |   x=sent_argminmax_assignment
    	{ value = x; }
    |   ';'
    	{ value = null; }
    ;
    
sent_call returns [ast_node value]
    :   built_in
    ;


sent_while returns [ast_node value]
    :   T_WHILE
        bool_expr
        sent_block
    ;


sent_do_while returns [ast_node value]
    :   T_DO
        sent_block
        T_WHILE
        bool_expr
    ;
    
sent_foreach returns [ast_node value]
    :   T_FOREACH
        foreach_header
        foreach_filter?
        sent
    |   T_FOR
        foreach_header
        foreach_filter?
        sent
    ;


foreach_header returns [ast_node value]
    :   id id     iterator1
    |   id id '+' iterator1
    |   id id '-' iterator1
    ;


foreach_filter returns [ast_node value]
    :   bool_expr
    ;


iterator1 returns [ast_node value]
    :   T_NODES
    |   T_EDGES
    |   T_NBRS
    |   T_IN_NBRS
    |   T_UP_NBRS
    |   T_DOWN_NBRS
    |   T_ITEMS
    |   T_COMMON_NBRS id
    ;


sent_dfs returns [ast_node value]
    :   T_DFS
        bfs_header_format
        bfs_filters?
        sent_block
        dfs_post?
    ;


sent_bfs returns [ast_node value]
    :   T_BFS
        bfs_header_format
        bfs_filters?
        sent_block
        bfs_reverse?
    ;


dfs_post returns [ast_node value]
    :   T_POST
        bfs_filter?
        sent_block
    ;


bfs_reverse returns [ast_node value]
    :   T_BACK
        bfs_filter?
        sent_block
    ;


bfs_header_format returns [ast_node value]
    :   id id T_NODES from_or_semi id
    ;


from_or_semi returns [ast_node value]
    :   T_FROM
    |   ';'
    ;


bfs_filters returns [ast_node value]
    :   bfs_navigator
    |   bfs_filter
    |   bfs_navigator bfs_filter
    |   bfs_filter    bfs_navigator
    ;


bfs_navigator returns [ast_node value]
    :   expr
    ;


bfs_filter returns [ast_node value]
    :   expr
    ;


sent_variable_decl returns [ast_node value]
    :   typedecl id rhs
    |   typedecl var_target
    ;


var_target returns [ast_node value]
    :   id_comma_list
    ;


sent_assignment returns [ast_node value]
    :   lhs '=' rhs
    ;


sent_reduce_assignment returns [ast_node value]
    :   lhs
        reduce_eq
        rhs
        optional_bind
    |   lhs
        T_PLUSPLUS
        optional_bind
    ;


sent_defer_assignment returns [ast_node value]
    :
    lhs
    T_LE
    rhs
    optional_bind
    ;


sent_argminmax_assignment returns [ast_node value]
    :
    lhs_list2
    minmax_eq
    rhs_list2
    optional_bind
    ;


optional_bind returns [ast_node value]
    :   ( '@' id )?
    ;


reduce_eq returns [ast_node value]
    :   T_PLUSEQ
    |   T_MULTEQ
    |   T_MINEQ
    |   T_MAXEQ
    |   T_ANDEQ
    |   T_OREQ
    ;


minmax_eq returns [ast_node value]
    :   T_MINEQ
    |   T_MAXEQ
    ;


rhs returns [ast_node value]
    :   expr
    ;


sent_return returns [ast_node value]
    :   T_RETURN
        expr
    |   T_RETURN
   /* This causes a shift-reduce conflict: What would be If (x) If (y) Else z;
   * The default action is to interpret it as If (x) {If (y) Else z;}, which is what C does.
   * */
    ;


sent_if returns [ast_node value]
    :   T_IF '(' bool_expr ')'
        sent
        ( T_ELSE sent )?
    ;


sent_user returns [ast_node value]
    :   expr_user
        ( T_DOUBLE_COLON '[' lhs_list ']' )?
    ;


expr returns [ast_node value]
    :   left_recursive_expr
    |   not_left_recursive_expr
    ;


not_left_recursive_expr returns [ast_node value]
    :   '(' expr ')'
    |   '|' expr '|'
    |   '-' expr
    |   '!' expr
    |   '(' prim_type ')' expr
    |   reduce_op
        '(' id ':' id '.' iterator1 ')'
        ( '(' expr ')' )?
        '{' expr '}'
    |   reduce_op2
        '(' id ':' id '.' iterator1 ')'
        ( '(' expr ')' )?
    |   BOOL_VAL
    |   INT_NUM
    |   FLOAT_NUM
    |   inf
    |   T_NIL
    |   scala
    |   field
    |   built_in
    |   expr_user
        /* cannot be distinguished by the syntax,
        until type is available. due to vars */
    ;


left_recursive_expr returns [ast_node value]
    :    conditional_expr
    ;


conditional_expr returns [ast_node value]
    :   conditional_or_expr
        ('?' expr ':' conditional_expr)?
    ;


conditional_or_expr returns [ast_node value]
    :   conditional_and_expr
        ('||' conditional_and_expr)*
    ;


conditional_and_expr returns [ast_node value]
    :   equality_expr
        ('&&' equality_expr)*
    ;


equality_expr returns [ast_node value]
    :   relational_expr
        (
            ( '==' | '!=' )
            relational_expr
        )*
    ;


relational_expr returns [ast_node value]
    :   additive_expr
        (relational_op additive_expr)*
    ;


relational_op returns [ast_node value]
    :   '<='
    |   '>='
    |   '<'
    |   '>'
    ;


additive_expr returns [ast_node value]
    :   multiplicative_expr
        (
            ('+' | '-')
            multiplicative_expr
        )*
    ;


multiplicative_expr returns [ast_node value]
    :   not_left_recursive_expr
        (
            ('*' | '/' | '%')
            not_left_recursive_expr
        )*
    ;


bool_expr returns [ast_node value]
    :   expr
    ;


numeric_expr returns [ast_node value]
    :   expr
    ;


reduce_op returns [ast_node value]
    :   T_SUM
    |   T_PRODUCT
    |   T_MIN
    |   T_MAX
    |   T_EXIST
    |   T_ALL
    |   T_AVG
    ;


reduce_op2 returns [ast_node value]
    :   T_COUNT
    ;


inf returns [ast_node value]
    :   T_P_INF
    |   T_M_INF
    ;


lhs returns [ast_node value]
    :   scala
    |   field
    ;


lhs_list returns [ast_node value]
    :   lhs
        ( ',' lhs_list )*
    ;


scala returns [ast_node value]
    :   id
    ;


field returns [ast_node value]
    :   id '.' id
    |   T_EDGE
        '(' id ')'
        '.' id
    ;


built_in returns [ast_node value]
    :   id
        ( '.' id )?
        arg_list
    |   field
        '.' id
        arg_list
    ;


arg_list returns [ast_node value]
    :   expr_list?
    ;


expr_list returns [ast_node value]
    :   expr
        ( ',' expr_list )*
    ;


lhs_list2 returns [ast_node value]
    :   lhs lhs_list
    ;


rhs_list2 returns [ast_node value]
    :   expr expr_list
    ;


expr_user returns [ast_node value]
    :   'XXX'
    ;
/* USER_TEXT*/

id returns [ast_node value]
    :   ID { value = FE.GM_id($ID.getText(), $ID.getLine(), $ID.getCharPositionInLine()); }
    ;