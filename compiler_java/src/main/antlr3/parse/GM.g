grammar GM;

options {
    language     = Java;
    output       = AST;
    backtrack    = true;
}

@parser::header {
    package parse;
    import ast.*;
    import inc.*;
}

@lexer::header {
    package parse;
}

/*extern void   GM_lex_begin_user_text();*/

/*******************************************************************************
    Parser section
*******************************************************************************/

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
        '(' arg_declist? ')'
        proc_return?
    |   proc_name
        '(' arg_declist? ';' arg_declist ')'
        proc_return?
    ;


proc_name
    :   T_PROC x=id
        { FE.GM_procdef_begin(x.value, false); }
    |   T_LOCAL x=id
        { FE.GM_procdef_begin(x.value, true); }
    ;


arg_declist
    :   x=arg_decl
        { FE.GM_procdef_add_argdecl(x.value); }
        ( ',' x=arg_decl { FE.GM_procdef_add_argdecl(x.value); } )*
    ;


proc_return
    /* return of function should be always primitive type */
    :   ':' prim_type
    |   ':' node_type
    /*| ':' graph_type */
    ;


arg_decl returns [ast_node value]
    :   x=arg_target ':' y=typedecl
        { retval.value = FE.GM_procdef_arg(x.value, y.value); }
    ;


arg_target returns [ast_node value]
    :   id_comma_list
        { retval.value = FE.GM_finish_id_comma_list(); }
    ;


typedecl returns [ast_node value]
    :   u=graph_type
        { retval.value = u.value; } 
    |   v=prim_type
    	{ retval.value = v.value; }
/*  |   w=property
    |   x=nodeedge_type
    |   y=set_type*/
    ;


graph_type returns [ast_node value]
    :   T_GRAPH
        { retval.value = FE.GM_graphtype_ref(GMTYPE_T.GMTYPE_GRAPH);  FE.GM_set_lineinfo(retval.value, 0, 0); }
    ;


prim_type returns [ast_node value]
    :   T_INT		{ retval.value = FE.GM_primtype_ref(GMTYPE_T.GMTYPE_INT);    FE.GM_set_lineinfo(retval.value, 0, 0); }
    |   T_LONG		{ retval.value = FE.GM_primtype_ref(GMTYPE_T.GMTYPE_LONG);   FE.GM_set_lineinfo(retval.value, 0, 0); }
    |   T_FLOAT		{ retval.value = FE.GM_primtype_ref(GMTYPE_T.GMTYPE_FLOAT);  FE.GM_set_lineinfo(retval.value, 0, 0); }
    |   T_DOUBLE	{ retval.value = FE.GM_primtype_ref(GMTYPE_T.GMTYPE_DOUBLE); FE.GM_set_lineinfo(retval.value, 0, 0); }
    |   T_BOOL		{ retval.value = FE.GM_primtype_ref(GMTYPE_T.GMTYPE_BOOL);   FE.GM_set_lineinfo(retval.value, 0, 0); }
    ;


nodeedge_type
    :   node_type
    |   edge_type
    ;


node_type
    :   T_NODE
        ( '(' id ')' )?
    ;


edge_type
    :   T_EDGE
        ( '(' id ')' )?
    ;


set_type
    :   T_NSET
        ( '(' id ')' )?
    |   T_NSEQ
        ( '(' id ')' )?
    |   T_NORDER
        ( '(' id ')' )?
    |   T_COLLECTION
        '<' set_type '>'
        ( '(' id ')' )?
    ;


property
    :   T_NODEPROP '<' prim_type '>'
        ( '(' id ')' )?
    |   T_NODEPROP '<' nodeedge_type '>'
        ( '(' id ')' )?
    |   T_NODEPROP '<' set_type '>'
        ( '(' id ')' )?
    |   T_EDGEPROP '<' prim_type '>'
        ( '(' id ')' )?
    |   T_EDGEPROP '<' nodeedge_type '>'
        ( '(' id ')' )?
    |   T_EDGEPROP '<' set_type '>'
        ( '(' id ')' )?
    ;


id_comma_list
    :   x=id
        { FE.GM_add_id_comma_list(x.value);}
        ( ',' x=id { FE.GM_add_id_comma_list(x.value); } )*
    ;


proc_body returns [ast_node value]
    :   x=sent_block
        { FE.GM_procdef_setbody(x.value); }
    ;


sent_block returns [ast_node value]
    :   sb_begin
    	{ FE.GM_start_sentblock(); }
        sent_list
        sb_end
        { retval.value = FE.GM_finish_sentblock(); }
    ;


sb_begin
    :   '{'
    ;

sb_end
    :   '}'
    ;


sent_list
    :   sent*
    ;


sent
    :   sent_assignment ';'
    |   sent_variable_decl ';'
    |   sent_block
    |   sent_foreach
    |   sent_if
    |   sent_reduce_assignment ';'
    |   sent_defer_assignment ';'
    |   sent_do_while ';'
    |   sent_while
    |   sent_return ';'
    |   sent_bfs
    |   sent_dfs
    |   sent_call ';'
    |   sent_user ';'
    |   sent_argminmax_assignment ';'
    |   ';'
    ;


sent_call
    :   built_in
    ;


sent_while
    :   T_WHILE
        '(' bool_expr ')'
        sent_block
    ;


sent_do_while
    :   T_DO
        sent_block
        T_WHILE
        '(' bool_expr ')'
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
    :   '(' id ':' id     '.' iterator1 ')'
    |   '(' id ':' id '+' '.' iterator1 ')'
    |   '(' id ':' id '-' '.' iterator1 ')'
    ;


foreach_filter
    :   '(' bool_expr ')'
    ;


iterator1
    :   T_NODES
    |   T_EDGES
    |   T_NBRS
    |   T_IN_NBRS
    |   T_UP_NBRS
    |   T_DOWN_NBRS
    |   T_ITEMS
    |   T_COMMON_NBRS '(' id ')'
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
    :   '(' id ':' id '^'? '.' T_NODES from_or_semi id ')'
    ;


from_or_semi
    :   T_FROM
    |   ';'
    ;


bfs_filters
    :   bfs_navigator
    |   bfs_filter
    |   bfs_navigator bfs_filter
    |   bfs_filter    bfs_navigator
    ;


bfs_navigator
    :   '[' expr ']'
    ;


bfs_filter
    :   '(' expr ')'
    ;


sent_variable_decl
    :   typedecl id '=' rhs
    |   typedecl var_target
    ;


var_target
    :   id_comma_list
    ;


sent_assignment
    :   lhs '=' rhs
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
    T_LE
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
    :   ( '@' id )?
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
   * The default action is to interpret it as If (x) {If (y) Else z;}, which is what C does.
   * */
    ;


sent_if
    :   T_IF '(' bool_expr ')'
        sent
        ( T_ELSE sent )?
    ;


sent_user
    :   expr_user
        ( T_DOUBLE_COLON '[' lhs_list ']' )?
    ;


expr
    :   left_recursive_expr
    |   not_left_recursive_expr
    ;


not_left_recursive_expr
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
        /* cannot be distinguished by the syntax, until type is available. due to vars */
    ;


left_recursive_expr
    :    conditional_expr
    ;


conditional_expr
    :   conditional_or_expr
        ('?' expr ':' conditional_expr)?
    ;


conditional_or_expr
    :   conditional_and_expr
        ('||' conditional_and_expr)*
    ;


conditional_and_expr
    :   equality_expr
        ('&&' equality_expr)*
    ;


equality_expr
    :   relational_expr
        (
            ( '==' | '!=' )
            relational_expr
        )*
    ;


relational_expr
    :   additive_expr
        (relational_op additive_expr)*
    ;


relational_op
    :   '<='
    |   '>='
    |   '<'
    |   '>'
    ;


additive_expr
    :   multiplicative_expr
        (
            ('+' | '-')
            multiplicative_expr
        )*
    ;


multiplicative_expr
    :   not_left_recursive_expr
        (
            ('*' | '/' | '%')
            not_left_recursive_expr
        )*
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
        ( ',' lhs_list )*
    ;


scala
    :   id
    ;


field
    :   id '.' id
    /*| id T_RARROW id                  { $$ = GM_field($1, $3, true);  }*/
    |   T_EDGE
        '(' id ')'
        '.' id
    ;


built_in
    :   id
        ( '.' id )?
        arg_list
    |   field
        '.' id
        arg_list
    ;


arg_list
    :   '(' expr_list? ')'
    ;


expr_list
    :   expr
        ( ',' expr_list )*
    ;


lhs_list2
    :   '<' lhs ';' lhs_list '>'
    ;


rhs_list2
    :   '<' expr ';' expr_list '>'
    ;


expr_user
    :   '[' 'XXX' ']'
    ;
/* USER_TEXT*/

id returns [ast_node value]
    :   x=ID
        { retval.value = FE.GM_id(x.getText(), 0, 0); }
    ;

/*******************************************************************************
    Lexer section
*******************************************************************************/

/* Keywords */
T_LOCAL : 'Local' ;
T_PROC : 'Procedure' | 'Proc' ;
T_BFS : 'InBFS' ;
T_DFS : 'InDFS' ;
T_POST : 'InPost' ;
T_RBFS : 'InRBFS' ;
T_FROM : 'From' ;
T_TO : 'To' ;
T_BACK : 'InReverse' ;
T_GRAPH : 'Graph' ;
T_NODE : 'Node' ;
T_EDGE : 'Edge' ;
T_NODEPROP : 'Node_Property' | 'Node_Prop' | 'N_P' ;
T_EDGEPROP : 'Edge_Property' | 'Edge_Prop' | 'E_P' ;
T_NSET : 'Node_Set' | 'N_S' ;
T_NORDER : 'Node_Order' | 'N_O' ;
T_NSEQ : 'Node_Seq' | 'Node_Sequence' | 'N_Q' ;
T_COLLECTION : 'Collection' ;
T_INT : 'Int' ;
T_LONG : 'Long' ;
T_FLOAT : 'Float' ;
T_DOUBLE : 'Double' ;
T_BOOL : 'Bool' ;
T_NODES : 'Nodes' ;
T_EDGES : 'Edges' ;
T_NBRS : 'Nbrs' | 'OutNbrs' ;
T_IN_NBRS : 'InNbrs' ;
T_UP_NBRS : 'UpNbrs' ;
T_DOWN_NBRS : 'DownNbrs' ;
T_ITEMS : 'Items' ;
T_COMMON_NBRS : 'CommonNbrs' ;
T_FOREACH : 'Foreach' ;
T_FOR : 'For' ;
T_AND : 'And' | '&&' ;
T_OR : 'Or' | '||' ;
T_EQ : '==' ;
T_NEQ : '!=' ;
T_LE : '<=' ;
T_GE : '>=' ;
BOOL_VAL : 'True' | 'False'; /*yylval.bval = true/false;*/
T_IF : 'If' ;
T_ELSE : 'Else' ;
T_WHILE : 'While' ;
T_RETURN : 'Return' ;
T_DO : 'Do' ;
T_PLUSEQ : '+=' ;
T_PLUSPLUS : '++' ;
T_MULTEQ : '*=' ;
T_ANDEQ : '&=' ;
T_OREQ : '|=' ;
T_MINEQ : 'min=' ;
T_MAXEQ : 'max=' ;
T_SUM : 'Sum' ;
T_AVG : 'Avg' ;
T_COUNT : 'Count' ;
T_PRODUCT : 'Product' ;
T_MAX : 'Max' ;
T_MIN : 'Min' ;
T_P_INF : '+INF' | 'INF' ;
T_M_INF : '-INF' ;
T_DOUBLE_COLON : '::' ;
T_ALL : 'All' ;
T_EXIST : 'Exist' ;
T_NIL : 'NIL' ;
T_RARROW : '->' ;

/* Char classes */
fragment DIGIT : '0'..'9' ;
fragment LETTER : 'a'..'z' | 'A'..'Z' ;
fragment ALPHANUM : LETTER (LETTER | DIGIT | '_')* ;

/* Numbers and Identifies */
ID : ALPHANUM ; /*yylval.text = yytext*/
FLOAT_NUM : DIGIT+ '.' DIGIT* ; /*yylval.fval = atof(yytext)*/
INT_NUM : DIGIT+ ; /*yylval.ival = atoi(yytext)*/

/* Whitespace and comments */
WS  
    :   (' ' | '\r' | '\t' | '\u000C' | '\n' )
        { skip(); }          
    ;
    
COMMENT
    :   '/*'
        (options {greedy=false;} : . )* 
        '*/'
        { skip(); }
    ;

LINE_COMMENT
    :   '//' ~('\n'|'\r')*  ('\r\n' | '\r' | '\n')?
        { skip(); }
    ;   
