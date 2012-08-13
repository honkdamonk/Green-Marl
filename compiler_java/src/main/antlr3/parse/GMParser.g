parser grammar GMParser;

options {
    language   = Java;
    output     = AST;
    superClass = AbstractGMParser;
    tokenVocab = GMLexer;
    backtrack  = true;
}

@header {
    package parse;
}

/*
%{
    #include <stdio.h>
    #include <string.h>
    #include <assert.h>
    #include 'gm_frontend_api.h'
    #define YYERROR_VERBOSE 1
    extern void   GM_lex_begin_user_text();

    extern void yyerror(const char* str);
    extern int yylex();
%}
*/

/* operator precedence, Lower is higher */
/* %glr-parser */
prog
    :   proc_def*
    ;


proc_def
    :   proc_head
        proc_body
    ;


proc_head
    :   proc_name
        T_PAREN_OPEN arg_declist? T_PAREN_CLOSE
        proc_return?
    |   proc_name
        T_PAREN_OPEN arg_declist? T_SEMICOLON arg_declist T_PAREN_CLOSE
        proc_return?
    ;


proc_name
    :   T_PROC  id
    |   T_LOCAL id
    ;


arg_declist
    :   arg_decl ( T_COMMA arg_decl )*
    ;


proc_return
    :   T_COLON prim_type
        /* return of function should be always primitive type */
    |   T_COLON node_type
    /*| T_COLON graph_type */
    ;


arg_decl
    :   arg_target T_COLON typedecl
    ;


arg_target
    :   id_comma_list
    ;


typedecl
    :   prim_type
    |   graph_type
    |   property
    |   nodeedge_type
    |   set_type
    ;


graph_type
    :   T_GRAPH
    ;


prim_type
    :   T_INT
    |   T_LONG
    |   T_FLOAT
    |   T_DOUBLE
    |   T_BOOL
    ;


nodeedge_type
    :   node_type
    |   edge_type
    ;


node_type
    :   T_NODE
        ( T_PAREN_OPEN id T_PAREN_CLOSE )?
    ;


edge_type
    :   T_EDGE
        ( T_PAREN_OPEN id T_PAREN_CLOSE )?
    ;


set_type
    :   T_NSET
        ( T_PAREN_OPEN id T_PAREN_CLOSE )?
    |   T_NSEQ
        ( T_PAREN_OPEN id T_PAREN_CLOSE )?
    |   T_NORDER
        ( T_PAREN_OPEN id T_PAREN_CLOSE )?
    |   T_COLLECTION
    	T_ANGLE_OPEN set_type T_ANGLE_CLOSE
    	( T_PAREN_OPEN id T_PAREN_CLOSE )?
    ;


property
    :   T_NODEPROP T_ANGLE_OPEN prim_type T_ANGLE_CLOSE
        ( T_PAREN_OPEN id T_PAREN_CLOSE )?
    |   T_NODEPROP T_ANGLE_OPEN nodeedge_type T_ANGLE_CLOSE
        ( T_PAREN_OPEN id T_PAREN_CLOSE )?
    |   T_NODEPROP T_ANGLE_OPEN set_type T_ANGLE_CLOSE
        ( T_PAREN_OPEN id T_PAREN_CLOSE )?
    |   T_EDGEPROP T_ANGLE_OPEN prim_type T_ANGLE_CLOSE
        ( T_PAREN_OPEN id T_PAREN_CLOSE )?
    |   T_EDGEPROP T_ANGLE_OPEN nodeedge_type T_ANGLE_CLOSE
        ( T_PAREN_OPEN id T_PAREN_CLOSE )?
    |   T_EDGEPROP T_ANGLE_OPEN set_type T_ANGLE_CLOSE
        ( T_PAREN_OPEN id T_PAREN_CLOSE )?
    ;


id_comma_list
    :   id ( T_COMMA id )*
    ;


proc_body
    :   sent_block
    ;


sent_block
    :   sb_begin
        sent_list
        sb_end
    ;


sb_begin
    :   T_CURLY_OPEN
    ;


sb_end
    :   T_CURLY_CLOSE
    ;


sent_list
    :   sent*
    ;


sent
    :   sent_assignment T_SEMICOLON
    |   sent_variable_decl T_SEMICOLON
    |   sent_block
    |   sent_foreach
    |   sent_if
    |   sent_reduce_assignment T_SEMICOLON
    |   sent_defer_assignment T_SEMICOLON
    |   sent_do_while T_SEMICOLON
    |   sent_while
    |   sent_return T_SEMICOLON
    |   sent_bfs
    |   sent_dfs
    |   sent_call T_SEMICOLON
    |   sent_user T_SEMICOLON
    |   sent_argminmax_assignment T_SEMICOLON
    |   T_SEMICOLON
    ;


sent_call
    :   built_in
    ;


sent_while
    :   T_WHILE
        T_PAREN_OPEN bool_expr T_PAREN_CLOSE
        sent_block
    ;


sent_do_while
    :   T_DO
        sent_block
        T_WHILE
        T_PAREN_OPEN bool_expr T_PAREN_CLOSE
    ;


sent_foreach
    :   T_FOREACH
        foreach_header
        foreach_filter?
        sent
    |   T_FOR
        foreach_header
        foreach_filter?
        sent
    ;


foreach_header
    :   T_PAREN_OPEN id T_COLON id     T_DOT iterator1 T_PAREN_CLOSE
    |   T_PAREN_OPEN id T_COLON id T_PLUS T_DOT iterator1 T_PAREN_CLOSE
    |   T_PAREN_OPEN id T_COLON id T_MINUS T_DOT iterator1 T_PAREN_CLOSE
    ;


foreach_filter
    :   T_PAREN_OPEN bool_expr T_PAREN_CLOSE
    ;


iterator1
    :   T_NODES
    |   T_EDGES
    |   T_NBRS
    |   T_IN_NBRS
    |   T_UP_NBRS
    |   T_DOWN_NBRS
    |   T_ITEMS
    |   T_COMMON_NBRS T_PAREN_OPEN id T_PAREN_CLOSE
    ;


sent_dfs
    :   T_DFS
    	bfs_header_format
    	bfs_filters?
    	sent_block
    	dfs_post?
    ;


sent_bfs
    :   T_BFS
    	bfs_header_format
    	bfs_filters?
    	sent_block
    	bfs_reverse?
    ;


dfs_post
    :   T_POST
        bfs_filter?
        sent_block
    ;


bfs_reverse
    :   T_BACK
        bfs_filter?
        sent_block
    ;


bfs_header_format
    :   T_PAREN_OPEN id T_COLON id T_CARET? T_DOT T_NODES from_or_semi id T_PAREN_CLOSE
    ;


from_or_semi
    :   T_FROM
    |   T_SEMICOLON
    ;


bfs_filters
    :   bfs_navigator
    |   bfs_filter
    |   bfs_navigator bfs_filter
    |   bfs_filter    bfs_navigator
    ;


bfs_navigator
    :   T_SQUARE_OPEN expr T_SQUARE_CLOSE
    ;


bfs_filter
    :   T_PAREN_OPEN expr T_PAREN_CLOSE
    ;


sent_variable_decl
    :   typedecl
    	var_target
    |   typedecl
        id
        T_EQUALS
        rhs
    ;


var_target
    :   id_comma_list
    ;


sent_assignment
    :   lhs T_EQUALS rhs
    ;


sent_reduce_assignment
    :   lhs
    	reduce_eq
    	rhs
    	optional_bind
    |   lhs
        T_PLUSPLUS
        optional_bind
    ;


sent_defer_assignment
    :
    lhs
    T_TEST_LE
    rhs
    optional_bind
    ;


sent_argminmax_assignment
    :
    lhs_list2
    minmax_eq
    rhs_list2
    optional_bind
    ;


optional_bind
    :   ( T_AT id )?
    ;


reduce_eq
    :   T_PLUSEQ
    |   T_MULTEQ
    |   T_MINEQ
    |   T_MAXEQ
    |   T_ANDEQ
    |   T_OREQ
    ;


minmax_eq
    :   T_MINEQ
    |   T_MAXEQ
    ;


rhs
    :   expr
    ;


sent_return
    :   T_RETURN
    	expr
    |   T_RETURN
   /* This causes a shift-reduce conflict: What would be If (x) If (y) Else z;
      The default action is to interpret it as If (x) {If (y) Else z;}, which is what C does.*/
    ;


sent_if
    :   T_IF T_PAREN_OPEN bool_expr T_PAREN_CLOSE
        sent
        ( T_ELSE sent )?
    ;


sent_user
    :   expr_user
        ( T_DOUBLE_COLON T_SQUARE_OPEN lhs_list T_SQUARE_CLOSE )?
    ;


expr
    :   T_PAREN_OPEN expr T_PAREN_CLOSE
    |   T_PIPE expr T_PIPE
    |   T_MINUS expr
    |   T_EXCLAMATION expr
    |   T_PAREN_OPEN prim_type T_PAREN_CLOSE expr
    |   reduce_op 
        T_PAREN_OPEN id T_COLON id T_DOT iterator1 T_PAREN_CLOSE
        ( T_PAREN_OPEN expr T_PAREN_CLOSE )?
        T_CURLY_OPEN expr T_CURLY_CLOSE
    |   reduce_op2
        T_PAREN_OPEN id T_COLON id T_DOT iterator1 T_PAREN_CLOSE
        ( T_PAREN_OPEN expr T_PAREN_CLOSE )?
    |   expr T_PERCENT expr
    |   expr T_STAR expr
    |   expr T_SLASH expr
    |   expr T_PLUS expr
    |   expr T_MINUS expr
    |   expr T_TEST_LE expr
    |   expr T_TEST_GE expr
    |   expr T_ANGLE_OPEN expr
    |   expr T_ANGLE_CLOSE expr
    |   expr T_TEST_EQ expr
    |   expr T_TEST_NEQ expr
    |   expr T_AND expr
    |   expr T_OR expr
    |   expr T_QUESTION expr T_COLON expr
    |   BOOL_VAL
    |   INT_NUM
    |   FLOAT_NUM
    |   inf
    |   T_NIL
    |   scala
    |   field
    |   built_in
    |   expr_user /* cannot be distinguished by the syntax, until type is available. due to vars */
    ;

bool_expr
    :   expr
    ;


numeric_expr
    :   expr
    ;


reduce_op
    :   T_SUM
    |   T_PRODUCT
    |   T_MIN
    |   T_MAX
    |   T_EXIST
    |   T_ALL
    |   T_AVG
    ;


reduce_op2
    :   T_COUNT
    ;


inf
    :   T_P_INF
    |   T_M_INF
    ;


lhs
    :   scala
    |   field
    ;


lhs_list
    :   lhs
        ( T_COMMA lhs_list )*
    ;


scala
    :   id
    ;


field
    :   id T_DOT id
    /*| id T_RARROW id                  { $$ = GM_field($1, $3, true);  }*/
    |   T_EDGE
        T_PAREN_OPEN id T_PAREN_CLOSE
        T_DOT id
    ;


built_in
    :   id
        ( T_DOT id )?
        arg_list
    |   field
        T_DOT id
        arg_list
    ;


arg_list
    :   T_PAREN_OPEN expr_list? T_PAREN_CLOSE
    ;


expr_list
    :   expr
    	( T_COMMA expr_list )*
    ;


lhs_list2
    :   T_ANGLE_OPEN lhs T_SEMICOLON lhs_list T_ANGLE_CLOSE
    ;


rhs_list2
    :   T_ANGLE_OPEN expr T_SEMICOLON expr_list T_ANGLE_CLOSE
    ;


expr_user
    :   T_SQUARE_OPEN USER_TEXT T_SQUARE_CLOSE
    ;

id
    :   ID
    ;
