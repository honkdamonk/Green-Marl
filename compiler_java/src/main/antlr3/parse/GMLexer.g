lexer grammar GMLexer;

options {
    language   = Java;
    superClass = AbstractGMLexer;
}

@header {
    package parse;
}

/* Keywords */
T_LOCAL 		: 'Local' ;
T_PROC 			: 'Procedure' | 'Proc' ;
T_BFS 			: 'InBFS' ;
T_DFS 			: 'InDFS' ;
T_POST 			: 'InPost' ;
T_RBFS 			: 'InRBFS' ;
T_FROM 			: 'From' ;
T_TO 			: 'To' ;
T_BACK 			: 'InReverse' ;
T_GRAPH 		: 'Graph' ;
T_NODE 			: 'Node' ;
T_EDGE 			: 'Edge' ;
T_NODEPROP 		: 'Node_Property' | 'Node_Prop' | 'N_P' ;
T_EDGEPROP 		: 'Edge_Property' | 'Edge_Prop' | 'E_P' ;
T_NSET 			: 'Node_Set' | 'N_S' ;
T_NORDER     	: 'Node_Order' | 'N_O' ;
T_NSEQ       	: 'Node_Seq' | 'Node_Sequence' | 'N_Q' ;
T_COLLECTION 	: 'Collection' ;
T_INT 			: 'Int' ;
T_LONG 			: 'Long' ;
T_FLOAT 		: 'Float' ;
T_DOUBLE       	: 'Double' ;
T_BOOL          : 'Bool' ;
T_NODES         : 'Nodes' ;
T_EDGES         : 'Edges' ;
T_NBRS          : 'Nbrs' | 'OutNbrs' ; 
T_IN_NBRS       : 'InNbrs' ; 
T_UP_NBRS       : 'UpNbrs' ; 
T_DOWN_NBRS     : 'DownNbrs' ;
T_ITEMS         : 'Items' ;
T_COMMON_NBRS   : 'CommonNbrs' ;
T_FOREACH 		: 'Foreach' ;
T_FOR 			: 'For' ;
T_AND 			: 'And' | '&&' ;
T_OR 			: 'Or' | '||' ;
T_TEST_EQ		: '==' ;
T_TEST_NEQ		: '!=' ;
T_TEST_LE		: '<=' ;
T_TEST_GE		: '>=' ;
BOOL_VAL 		: 'True' | 'False'; /*yylval.bval = true/false;*/
T_IF 			: 'If' ;
T_ELSE 			: 'Else' ;
T_WHILE 		: 'While' ;
T_RETURN 		: 'Return' ;
T_DO 			: 'Do' ;
T_PLUSEQ 		: '+=' ;
T_PLUSPLUS 		: '++' ;
T_MULTEQ 		: '*=' ;
T_ANDEQ 		: '&=' ;
T_OREQ 			: '|=' ;
T_MINEQ 		: 'min=' ;
T_MAXEQ 		: 'max=' ;
T_SUM 			: 'Sum' ;
T_AVG 			: 'Avg' ;
T_COUNT 		: 'Count' ;
T_PRODUCT 		: 'Product' ;
T_MAX 			: 'Max' ;
T_MIN 			: 'Min' ;
T_P_INF 		: '+INF' | 'INF' ;
T_M_INF 		: '-INF' ;
T_DOUBLE_COLON 	: '::' ;
T_ALL 			: 'All' ;
T_EXIST 		: 'Exist' ;
T_NIL 			: 'NIL' ;
T_RARROW 		: '->' ;
T_SEMICOLON     : ';' ;
T_COLON			: ':' ;
T_DOT			: '.' ;
T_COMMA			: ',' ;
T_PAREN_OPEN	: '(' ;
T_PAREN_CLOSE	: ')' ;
T_SQUARE_OPEN	: '[' ;
T_SQUARE_CLOSE	: ']' ;
T_ANGLE_OPEN	: '<' ;
T_ANGLE_CLOSE	: '>' ;
T_CURLY_OPEN	: '{' ;
T_CURLY_CLOSE	: '}' ;
T_PLUS			: '+' ;
T_MINUS			: '-' ;
T_STAR			: '*' ;
T_SLASH			: '/' ;
T_PIPE			: '|' ;
T_AT			: '@' ;
T_PERCENT		: '%' ;
T_QUESTION		: '?' ;
T_EXCLAMATION	: '!' ;
T_CARET			: '^' ;
T_EQUALS		: '=' ;

/* Char classes */
fragment
DIGIT
    :   '0'..'9'
    ;

fragment
LETTER
    :   'a'..'z'
    |   'A'..'Z'
    ;

fragment
ALPHANUM
    :   LETTER
        ( LETTER | DIGIT | '_' )*
    ;

/* User text */
USER_TEXT
    :   'XXX'
    ;

/* Numbers and Identifies */
ID
    :   ALPHANUM
    ;   /*yylval.text = yytext*/
FLOAT_NUM
    :   DIGIT+ '.' DIGIT*
    ;   /*yylval.fval = atof(yytext)*/
INT_NUM
    :   DIGIT+
    ;   /*yylval.ival = atoi(yytext)*/
