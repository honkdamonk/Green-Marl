package frontend;

import static frontend.gm_range_type_t.GM_RANGE_LEVEL;
import static frontend.gm_range_type_t.GM_RANGE_LEVEL_DOWN;
import static frontend.gm_range_type_t.GM_RANGE_LEVEL_UP;
import static frontend.gm_range_type_t.GM_RANGE_LINEAR;
import static frontend.gm_range_type_t.GM_RANGE_RANDOM;
import static frontend.gm_range_type_t.GM_RANGE_SINGLE;
import inc.GMTYPE_T;
import inc.GM_REDUCE_T;

import java.util.HashMap;
import java.util.LinkedList;

import ast.AST_NODE_TYPE;
import ast.ast_bfs;
import ast.ast_expr;
import ast.ast_expr_builtin;
import ast.ast_expr_foreign;
import ast.ast_expr_reduce;
import ast.ast_field;
import ast.ast_foreach;
import ast.ast_id;
import ast.ast_node;
import ast.ast_sent;
import ast.ast_sentblock;
import ast.gm_rwinfo_list;
import ast.gm_rwinfo_map;

import common.GM_ERRORS_AND_WARNINGS;
import common.gm_error;
import common.gm_traverse;

public class GlobalMembersGm_rw_analysis {


}