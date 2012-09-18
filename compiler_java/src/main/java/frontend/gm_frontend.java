package frontend;

import static ast.ast_node_type.AST_ARGDECL;
import static ast.ast_node_type.AST_EXPR;
import static ast.ast_node_type.AST_EXPR_BUILTIN;
import static ast.ast_node_type.AST_EXPR_FOREIGN;
import static ast.ast_node_type.AST_FIELD;
import static ast.ast_node_type.AST_ID;
import static ast.ast_node_type.AST_IDLIST;
import static ast.ast_node_type.AST_MAPACCESS;
import static ast.ast_node_type.AST_SENTBLOCK;
import static ast.ast_node_type.AST_TYPEDECL;
import inc.expr_list;
import inc.gm_assignment;
import inc.gm_backend_info;
import inc.gm_compile_step;
import inc.gm_ops;
import inc.gm_procinfo;
import inc.gm_reduce;
import inc.gm_type;
import inc.lhs_list;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import parse.start_parse;
import tangible.Extern;
import ast.ast_argdecl;
import ast.ast_assign;
import ast.ast_assign_mapentry;
import ast.ast_bfs;
import ast.ast_call;
import ast.ast_expr;
import ast.ast_expr_builtin;
import ast.ast_expr_builtin_field;
import ast.ast_expr_foreign;
import ast.ast_expr_mapaccess;
import ast.ast_expr_reduce;
import ast.ast_field;
import ast.ast_foreach;
import ast.ast_foreign;
import ast.ast_id;
import ast.ast_idlist;
import ast.ast_if;
import ast.ast_mapaccess;
import ast.ast_maptypedecl;
import ast.ast_node;
import ast.ast_procdef;
import ast.ast_return;
import ast.ast_sent;
import ast.ast_sentblock;
import ast.ast_typedecl;
import ast.ast_vardecl;
import ast.ast_while;

import common.gm_apply_compiler_stage;
import common.gm_error;
import common.gm_main;
import common.gm_traverse;
import common.gm_vocabulary;

public class gm_frontend {

	public static final String GMUSAGE_PROPERTY = "GMUSAGE_PROPERTY";

	private final LinkedList<gm_compile_step> local_steps = new LinkedList<gm_compile_step>();

	/** temporary fields used during parsing */
	private final LinkedList<ast_sentblock> blocks = new LinkedList<ast_sentblock>();
	private ast_procdef curr_proc = null;
	private ast_idlist curr_idlist = null;
	private Iterator<ast_procdef> I;
	private final ArrayList<ast_procdef> procs = new ArrayList<ast_procdef>();
	private final Map<ast_procdef, gm_procinfo> proc_info = new HashMap<ast_procdef, gm_procinfo>();
	private ast_procdef _curr_proc;

	/** a hack for debug */
	public boolean vardecl_removed = false; // a temporary hack

	/** frontend module implementation */
	public gm_frontend() {
		init_steps();
	}

	/** interface to parser */
	public final int start_parse(String fname) {
		return start_parse.parse(fname);
	}

	// void clean_up(); // clean-up intermediate structures (for iterative mode)

	/** procedure definition */
	public final void start_new_procdef(ast_procdef a) {
		curr_proc = a;
	}

	public final ast_procdef get_current_procdef() {
		return curr_proc;
	}

	public final void finish_procdef() {
		procs.add(curr_proc);
		curr_proc = null;
	}

	public final void start_sentblock(ast_sentblock b) {
		blocks.addLast(b);
	}

	public final ast_sentblock get_current_sentblock() {
		return blocks.getLast();
	}

	public final void end_sentblock() {
		blocks.removeLast();
	}

	public final ast_idlist get_current_idlist() {
		if (curr_idlist == null)
			curr_idlist = new ast_idlist();
		return curr_idlist;
	}

	public final void finish_id_comma_list() {
		curr_idlist = null;
	}

	// -------------------------------------------------------
	// Interface to compiler main
	public final boolean do_local_frontend_process() {
		// create information objects for all procedures
		for (ast_procdef p : procs) {
			proc_info.put(p, new gm_procinfo(p));
		}

		// now apply frontend steps
		return gm_apply_compiler_stage.apply(local_steps);
	}

	/** reproduce: method implementations for ast debuggin */
	public final void reproduce() {

		for (ast_procdef p : procs) {
			p.reproduce(0);
		}
	}

	public final void dump_tree() {

		for (ast_procdef p : procs) {
			p.dump_tree(0);
		}
	}

	public final int get_num_procs() {
		return procs.size();
	}

	public final ast_procdef get_current_proc() {
		return _curr_proc;
	}

	public final void prepare_proc_iteration() {
		I = procs.iterator();
	}

	public final ast_procdef get_next_proc() {
		if (I.hasNext()) {
			ast_procdef p = I.next();
			gm_error.gm_set_curr_procname(p.get_procname().get_orgname());
			set_current_proc(p);
			return p;
		} else {
			gm_error.gm_set_curr_procname("");
			set_current_proc(null);
			return null;
		}
	}

	public final gm_procinfo get_proc_info(ast_procdef proc) {
		return proc_info.get(proc);
	}

	public final gm_procinfo get_current_proc_info() {
		return proc_info.get(_curr_proc);
	}

	public final gm_backend_info get_backend_info(ast_procdef proc) {
		return proc_info.get(proc).get_be_info();
	}

	public final gm_backend_info get_current_backend_info() {
		return proc_info.get(_curr_proc).get_be_info();
	}

	/** short-cut to current procedure's vocaburary */
	public final void voca_add(String value) {
		proc_info.get(_curr_proc).add_voca(value);
	}

	public final boolean voca_isin(String value) {
		return proc_info.get(_curr_proc).isin_voca(value);
	}

	public final void voca_clear() {
		proc_info.get(_curr_proc).clear_voca();
	}

	public final String voca_temp_name(String base, gm_vocabulary extra1) {
		return voca_temp_name(base, extra1, false);
	}

	public final String voca_temp_name(String base) {
		return voca_temp_name(base, null, false);
	}

	public final String voca_temp_name(String base, gm_vocabulary extra1, boolean try_org_name_first) {
		return proc_info.get(_curr_proc).generate_temp_name(base, extra1, try_org_name_first);
	}

	public final String voca_temp_name_and_add(String base, String suffix, gm_vocabulary extra1) {
		return voca_temp_name_and_add(base, suffix, extra1, false);
	}

	public final String voca_temp_name_and_add(String base, String suffix) {
		return voca_temp_name_and_add(base, suffix, null, false);
	}

	public final String voca_temp_name_and_add(String base, String suffix, gm_vocabulary extra1, boolean insert_underscore_prefix_if_not_already) {
		String temp;
		if (insert_underscore_prefix_if_not_already && (base.charAt(0) != '_'))
			temp = String.format("_%s%s", base, suffix);
		else
			temp = String.format("%s%s", base, suffix);
		return voca_temp_name_and_add(temp, extra1, true);
	}

	public final String voca_temp_name_and_add(String base, gm_vocabulary extra1) {
		return voca_temp_name_and_add(base, extra1, false);
	}

	public final String voca_temp_name_and_add(String base) {
		return voca_temp_name_and_add(base, null, false);
	}

	public final String voca_temp_name_and_add(String base, gm_vocabulary extra1, boolean try_org_name_first) {
		String c = proc_info.get(_curr_proc).generate_temp_name(base, extra1, try_org_name_first);
		voca_add(c);
		return c;
	}

	private void set_current_proc(ast_procdef p) {
		_curr_proc = p;
	}

	/** local frontend process */
	private void init_steps() {
		LinkedList<gm_compile_step> LIST = local_steps;

		LIST.addLast(gm_fe_check_syntax_rules.get_factory());
		LIST.addLast(gm_fe_syntax_sugar.get_factory());
		LIST.addLast(gm_fe_typecheck_step1.get_factory());
		LIST.addLast(gm_fe_typecheck_step2.get_factory());
		LIST.addLast(gm_fe_typecheck_step3.get_factory());
		LIST.addLast(gm_fe_typecheck_step4.get_factory());
		LIST.addLast(gm_fe_typecheck_step5.get_factory());
		LIST.addLast(gm_fe_expand_group_assignment.get_factory());
		LIST.addLast(gm_fe_fixup_bound_symbol.get_factory());
		LIST.addLast(gm_fe_rw_analysis.get_factory());
		LIST.addLast(gm_fe_reduce_error_check.get_factory());
		LIST.addLast(gm_fe_rw_analysis_check2.get_factory());
		LIST.addLast(gm_fe_remove_vardecl.get_factory());
		LIST.addLast(gm_fe_check_property_argument_usage.get_factory());
	}

	private ast_procdef get_procedure(int i) {
		return procs.get(i);
	}

	public final boolean is_vardecl_removed() {
		return vardecl_removed;
	}

	public final void set_vardecl_removed(boolean b) {
		vardecl_removed = b;
	}

	public void restore_vardecl_all() {
		gm_apply_compiler_stage.gm_apply_all_proc(gm_fe_restore_vardecl.get_factory());
	}

	public void print_rwinfo() {
		for (int i = 0; i < get_num_procs(); i++) {
			ast_procdef proc = get_procedure(i);
			System.out.printf("PROC: %s\n", proc.get_procname().get_orgname());
			gm_print_rw_info P = new gm_print_rw_info();
			gm_traverse.gm_traverse_sents(proc, P);
			System.out.print("\n");
		}
		return;
	}

	public static void GM_procdef_begin(ast_node id, boolean b) {
		assert id.get_nodetype() == AST_ID;
		ast_procdef def = ast_procdef.begin_new_procdef((ast_id) id);
		def.set_local(b);
		gm_main.FE.start_new_procdef(def);
	}

	public static void GM_procdef_finish() {
		gm_main.FE.finish_procdef();
	}

	public static void GM_procdef_add_argdecl(ast_node n) {
		assert n.get_nodetype() == AST_ARGDECL;
		gm_main.FE.get_current_procdef().add_argdecl((ast_argdecl) n);
	}

	public static void GM_procdef_add_out_argdecl(ast_node n) {
		assert n.get_nodetype() == AST_ARGDECL;
		gm_main.FE.get_current_procdef().add_out_argdecl((ast_argdecl) n);
	}

	public static void GM_procdef_setbody(ast_node v) {
		assert v.get_nodetype() == AST_SENTBLOCK;
		ast_sentblock b = (ast_sentblock) v;
		gm_main.FE.get_current_procdef().set_sentblock(b);
		// FE.finish_procdef();
	}

	public static ast_node GM_procdef_arg(ast_node id_list, ast_node type) {
		assert id_list.get_nodetype() == AST_IDLIST;
		assert type.get_nodetype() == AST_TYPEDECL;

		ast_argdecl arg = ast_argdecl.new_argdecl((ast_idlist) id_list, (ast_typedecl) type);
		return arg;
	}

	public static void GM_procdef_return_type(ast_node rt) {
		assert rt.get_nodetype() == AST_TYPEDECL;
		gm_main.FE.get_current_procdef().set_return_type((ast_typedecl) rt);
	}

	public static void GM_start_sentblock() {
		ast_sentblock newblock = ast_sentblock.new_sentblock();
		gm_main.FE.start_sentblock(newblock);
	}

	public static ast_node GM_finish_sentblock() {
		ast_sentblock b = gm_main.FE.get_current_sentblock();
		gm_main.FE.end_sentblock();
		return b;
	}

	// extern int GM_get_empty_lines();

	public static void GM_add_sent(ast_node s) {
		assert s.is_sentence();
		ast_sent sent = (ast_sent) s;
		ast_sentblock b = gm_main.FE.get_current_sentblock();
		assert b != null;
		b.add_sent(sent);
		sent.set_empty_lines_before(Extern.GM_get_empty_lines());
	}

	public static ast_node GM_expr_id_access(ast_node id) {
		assert id.get_nodetype() == AST_ID;
		ast_id i = (ast_id) id;
		ast_node n = ast_expr.new_id_expr(i);
		n.set_line(i.get_line());
		n.set_col(i.get_col());
		return n;
	}

	public static ast_node GM_expr_field_access(ast_node field) {
		assert field.get_nodetype() == AST_FIELD;
		ast_field f = (ast_field) field;
		ast_node n = ast_expr.new_field_expr(f);
		n.set_line(f.get_first().get_line());
		n.set_col(f.get_first().get_col());
		return n;
	}

	public static ast_node GM_expr_map_access(ast_node mapAccess) {
		assert (mapAccess != null);
		assert (mapAccess.get_nodetype() == AST_MAPACCESS);
		return ast_expr_mapaccess.new_expr_mapaccess((ast_mapaccess) mapAccess);
	}

	public static ast_node GM_expr_ival(int lval, int l, int c) {
		ast_node n = ast_expr.new_ival_expr(lval);
		n.set_line(l);
		n.set_col(c);
		return n;
	}

	public static ast_node GM_expr_fval(double fval, int l, int c) {
		ast_node n = ast_expr.new_fval_expr(fval);
		n.set_line(l);
		n.set_col(c);
		return n;
	}

	public static ast_node GM_expr_bval(boolean bval, int l, int c) {
		ast_node n = ast_expr.new_bval_expr(bval);
		n.set_line(l);
		n.set_col(c);
		return n;
	}

	public static ast_node GM_expr_inf(boolean is_plus, int l, int c) {
		ast_node n = ast_expr.new_inf_expr(is_plus);
		n.set_line(l);
		n.set_col(c);
		return n;
	}

	public static ast_node GM_expr_nil(int l, int c) {
		ast_node n = ast_expr.new_nil_expr();
		n.set_line(l);
		n.set_col(c);
		return n;
	}

	public static ast_node GM_expr_biop(ast_node left, ast_node right, gm_ops op, int l, int c) {
		ast_node n = ast_expr.new_biop_expr(op, (ast_expr) left, (ast_expr) right);
		n.set_line(l);
		n.set_col(c);
		return n;
	}

	public static ast_node GM_expr_uop(ast_node left, gm_ops op, int l, int c) {
		ast_node n = ast_expr.new_uop_expr(op, (ast_expr) left);
		n.set_line(l);
		n.set_col(c);
		return n;
	}

	public static ast_node GM_expr_lbiop(ast_node left, ast_node right, gm_ops op, int l, int c) {
		ast_node n = ast_expr.new_lbiop_expr(op, (ast_expr) left, (ast_expr) right);
		n.set_line(l);
		n.set_col(c);
		return n;
	}

	public static ast_node GM_expr_luop(ast_node left, gm_ops op, int l, int c) {
		ast_node n = ast_expr.new_luop_expr(op, (ast_expr) left);
		n.set_line(l);
		n.set_col(c);
		return n;
	}

	public static ast_node GM_expr_comp(ast_node left, ast_node right, gm_ops op, int l, int c) {
		assert op.is_eq_or_less_op();
		ast_node n = ast_expr.new_comp_expr(op, (ast_expr) left, (ast_expr) right);
		n.set_line(l);
		n.set_col(c);
		return n;
	}

	public static ast_node GM_expr_conversion(ast_node left, ast_node type, int l, int c) {
		assert ((ast_typedecl) type).is_primitive();
		gm_type target_type = ((ast_typedecl) type).get_typeid();
		ast_node n = ast_expr.new_typeconv_expr(target_type, (ast_expr) left);
		n.set_line(l);
		n.set_col(c);
		return n;
	}

	public static ast_node GM_expr_reduceop(gm_reduce op, ast_node iter, ast_node src, gm_type iter_op, ast_node body, ast_node filter, ast_node src2, int l,
			int c) {
		assert iter.get_nodetype() == AST_ID;
		assert src.get_nodetype() == AST_ID;
		assert body.is_expr();

		if (filter != null) {
			assert (filter.get_nodetype() == AST_EXPR) || (filter.get_nodetype() == AST_EXPR_BUILTIN);
		}
		if (src2 != null)
			assert src2.get_nodetype() == AST_ID;
		assert iter_op.is_iter_type();
		assert (op == gm_reduce.GMREDUCE_MAX) || (op == gm_reduce.GMREDUCE_MIN) || (op == gm_reduce.GMREDUCE_PLUS) || (op == gm_reduce.GMREDUCE_MULT)
				|| (op == gm_reduce.GMREDUCE_AND) || (op == gm_reduce.GMREDUCE_OR) || (op == gm_reduce.GMREDUCE_AVG);

		ast_expr_reduce n = ast_expr_reduce.new_reduce_expr(op, (ast_id) iter, (ast_id) src, iter_op, (ast_expr) body, (ast_expr) filter);

		n.set_source2((ast_id) src2);
		n.set_line(l);
		n.set_col(c);
		return n;
	}

	public static ast_node GM_expr_foreign(String text, int l, int c) {
		ast_expr_foreign expr = ast_expr_foreign.new_expr_foreign(text);
		expr.set_line(l);
		expr.set_col(c);
		expr.parse_foreign_syntax(); // parse again!
		return expr;

	}

	public static ast_node GM_expr_builtin_expr(ast_node id, ast_node id2, expr_list l) {
		ast_id i = (ast_id) id;
		ast_id i2 = (ast_id) id2;
		ast_expr n = ast_expr_builtin.new_builtin_expr(i, i2.get_orgname(), l);
		if (i != null) {
			n.set_line(i.get_line());
			n.set_col(i.get_col());
		} else {
			n.set_line(i2.get_line());
			n.set_col(i2.get_col());
		}

		if (id2 != null)
			id2.dispose(); // this is only temporary
		return n;
	}

	public static ast_node GM_expr_builtin_field_expr(ast_node id, ast_node id2, expr_list list) {

		ast_field field = (ast_field) id;
		ast_id builtin = (ast_id) id2;
		ast_expr newExpression = ast_expr_builtin_field.new_builtin_field_expr(field, builtin.get_orgname(), list);

		if (id == null) {
			newExpression.set_line(field.get_line());
			newExpression.set_col(field.get_col());
		} else {
			newExpression.set_line(builtin.get_line());
			newExpression.set_col(builtin.get_col());
		}
		if (id2 != null)
			id2.dispose(); // this is only temporary
		return newExpression;
	}

	public static ast_node GM_expr_ternary(ast_node cond, ast_node left, ast_node right, int l, int c) {
		assert cond.is_expr();
		assert left.is_expr();
		assert right.is_expr();
		ast_expr n = ast_expr.new_ternary_expr((ast_expr) cond, (ast_expr) left, (ast_expr) right);
		n.set_line(l);
		n.set_col(c);
		return n;
	}

	// type declarations
	public static ast_node GM_graphtype_ref(gm_type graph_type_id) {
		return ast_typedecl.new_graphtype(graph_type_id);
	}

	public static ast_node GM_primtype_ref(gm_type prim_type_id) {
		return ast_typedecl.new_primtype(prim_type_id);
	}

	public static ast_node GM_settype_ref(gm_type set_type_id, ast_node id) {
		if (id != null)
			assert id.get_nodetype() == AST_ID;
		return ast_typedecl.new_set((ast_id) id, set_type_id);
	}

	public static ast_node GM_queuetype_ref(ast_node collectionType, ast_node id) {
		if (id != null)
			assert id.get_nodetype() == AST_ID;
		assert collectionType.get_nodetype() == AST_TYPEDECL;
		return ast_typedecl.new_queue((ast_id) id, (ast_typedecl) collectionType);
	}

	public static ast_node GM_maptype_ref(ast_node key, ast_node value) {
		assert (key != null);
		assert (value != null);
		assert (key.get_nodetype() == AST_TYPEDECL);
		assert (value.get_nodetype() == AST_TYPEDECL);
		ast_typedecl keyType = (ast_typedecl) key;
		ast_typedecl valueType = (ast_typedecl) value;
		assert (keyType.getTypeSummary().can_be_key_type());
		assert (valueType.getTypeSummary().can_be_value_type());
		return ast_maptypedecl.new_map(keyType, valueType);
	}

	public static ast_node GM_nodeprop_ref(ast_node typedecl, ast_node id) {
		assert typedecl.get_nodetype() == AST_TYPEDECL;
		if (id == null)
			return ast_typedecl.new_nodeprop((ast_typedecl) typedecl, null);
		assert id.get_nodetype() == AST_ID;
		return ast_typedecl.new_nodeprop((ast_typedecl) typedecl, (ast_id) id);
	}

	public static ast_node GM_edgeprop_ref(ast_node typedecl, ast_node id) {
		assert typedecl.get_nodetype() == AST_TYPEDECL;
		if (id == null)
			return ast_typedecl.new_edgeprop((ast_typedecl) typedecl, null);
		assert id.get_nodetype() == AST_ID;
		return ast_typedecl.new_edgeprop((ast_typedecl) typedecl, (ast_id) id);
	}

	public static ast_node GM_nodetype_ref(ast_node id) {
		if (id == null)
			return ast_typedecl.new_nodetype(null);
		assert id.get_nodetype() == AST_ID;
		return ast_typedecl.new_nodetype((ast_id) id);
	}

	public static ast_node GM_edgetype_ref(ast_node id) {
		if (id == null)
			return ast_typedecl.new_edgetype(null);
		assert id.get_nodetype() == AST_ID;
		return ast_typedecl.new_edgetype((ast_id) id);
	}

	public static ast_node GM_vardecl_prim(ast_node type, ast_node names) {
		assert type.get_nodetype() == AST_TYPEDECL;
		assert names.get_nodetype() == AST_IDLIST;
		return ast_vardecl.new_vardecl((ast_typedecl) type, (ast_idlist) names);
	}

	public static ast_node GM_vardecl_and_assign(ast_node type, ast_node id, ast_node expr) {
		assert type.get_nodetype() == AST_TYPEDECL;
		assert id.get_nodetype() == AST_ID;
		assert expr.is_expr();
		return ast_vardecl.new_vardecl_init((ast_typedecl) type, (ast_id) id, (ast_expr) expr);
	}

	/*
	 * void GM_bfs_setbody(void* h, void* b) { ast_bfsiter* head =
	 * (ast_bfsiter*) h; ast_sentblock* block = (ast_sentblock*) b;
	 * head->set_sent_block(block); }
	 * 
	 * void* GM_bfs_begin(void* id1, void *id2, void* id3) { ast_bfsiter* i =
	 * ast_bfsiter::new_bfsiter((ast_id*)id1, (ast_id*)id2, (ast_id*)id3);
	 * return i; }
	 */

	public static ast_node GM_id(String orgname, int line, int col) {
		assert orgname != null;
		return ast_id.new_id(orgname, line, col);
	}

	public static ast_node GM_field(ast_node id1, ast_node id2, boolean is_rarrow) {
		assert id1.get_nodetype() == AST_ID;
		assert id2.get_nodetype() == AST_ID;
		return ast_field.new_field((ast_id) id1, (ast_id) id2, is_rarrow);
	}

	public static ast_node GM_map_access(ast_node mapId, ast_node keyExpr) {
		assert (mapId != null);
		assert (keyExpr != null);
		assert (mapId.get_nodetype() == AST_ID);
		assert (keyExpr.get_nodetype() == AST_EXPR);
		return ast_mapaccess.new_mapaccess((ast_id) mapId, (ast_expr) keyExpr);
	}

	public static void GM_add_id_comma_list(ast_node id) {
		assert id.get_nodetype() == AST_ID;
		ast_idlist idlist = gm_main.FE.get_current_idlist();
		idlist.add_id((ast_id) id);
		id.set_parent(idlist);
	}

	public static ast_node GM_finish_id_comma_list() {
		ast_idlist idlist = gm_main.FE.get_current_idlist();
		gm_main.FE.finish_id_comma_list();
		return idlist;
	}

	public static ast_node GM_normal_assign(ast_node lhs, ast_node rhs) {
		assert rhs.is_expr();

		if (lhs.get_nodetype() == AST_ID) {
			return ast_assign.new_assign_scala((ast_id) lhs, (ast_expr) rhs, gm_assignment.GMASSIGN_NORMAL, null, gm_reduce.GMREDUCE_NULL);

		} else if (lhs.get_nodetype() == AST_FIELD) {
			return ast_assign.new_assign_field((ast_field) lhs, (ast_expr) rhs, gm_assignment.GMASSIGN_NORMAL, null, gm_reduce.GMREDUCE_NULL);
		} else if (lhs.get_nodetype() == AST_MAPACCESS) {
			return ast_assign_mapentry.new_mapentry_assign((ast_mapaccess) lhs, (ast_expr) rhs);
		}
		assert false;
		return null;
	}

	public static ast_node GM_reduce_assign(ast_node lhs, ast_node rhs, ast_node id, gm_reduce reduce_type) {
		assert rhs.is_expr();
		if (id != null)
			assert id.get_nodetype() == AST_ID;

		if (lhs.get_nodetype() == AST_ID) {
			return ast_assign.new_assign_scala((ast_id) lhs, (ast_expr) rhs, gm_assignment.GMASSIGN_REDUCE, (ast_id) id, reduce_type);

		} else if (lhs.get_nodetype() == AST_FIELD) {
			return ast_assign.new_assign_field((ast_field) lhs, (ast_expr) rhs, gm_assignment.GMASSIGN_REDUCE, (ast_id) id, reduce_type);
		} else {
			assert false;
			return null;
		}
	}

	public static ast_node GM_argminmax_assign(ast_node lhs, ast_node rhs, ast_node id, gm_reduce reduce_type, lhs_list l_list, expr_list r_list) {
		assert rhs.is_expr();
		if (id != null)
			assert id.get_nodetype() == AST_ID;
		ast_assign a;

		if (lhs.get_nodetype() == AST_ID) {
			a = ast_assign.new_assign_scala((ast_id) lhs, (ast_expr) rhs, gm_assignment.GMASSIGN_REDUCE, (ast_id) id, reduce_type);

		} else if (lhs.get_nodetype() == AST_FIELD) {
			a = ast_assign.new_assign_field((ast_field) lhs, (ast_expr) rhs, gm_assignment.GMASSIGN_REDUCE, (ast_id) id, reduce_type);
		} else if (lhs.get_nodetype() == AST_MAPACCESS) {
			return ast_assign_mapentry.new_mapentry_reduce_assign((ast_mapaccess) lhs, (ast_expr) rhs, reduce_type);
		} else {
			assert false;
			throw new AssertionError();
		}

		a.set_argminmax_assign(true);
		a.set_lhs_list(l_list.LIST); // shallow copy
		a.set_rhs_list(r_list.LIST);
		for (ast_node i : l_list.LIST) {
			i.set_parent(a);
		}
		for (ast_expr e : r_list.LIST) {
			e.set_parent(a);
		}

		return a;
	}

	public static ast_node GM_defer_assign(ast_node lhs, ast_node rhs, ast_node id) {
		assert rhs.is_expr();
		if (id != null)
			assert id.get_nodetype() == AST_ID;

		if (lhs.get_nodetype() == AST_ID) {
			return ast_assign.new_assign_scala((ast_id) lhs, (ast_expr) rhs, gm_assignment.GMASSIGN_DEFER, (ast_id) id, gm_reduce.GMREDUCE_DEFER);

		} else if (lhs.get_nodetype() == AST_FIELD) {
			return ast_assign.new_assign_field((ast_field) lhs, (ast_expr) rhs, gm_assignment.GMASSIGN_DEFER, (ast_id) id, gm_reduce.GMREDUCE_DEFER);
		}

		assert false;
		return null;
	}

	public static ast_node GM_foreach(ast_node id, ast_node source, gm_type iter_typ, ast_node sent, ast_node filter, boolean is_seq, boolean is_backward,
			ast_node source2) {
		assert id.get_nodetype() == AST_ID;
		assert source.get_nodetype() == AST_ID;
		assert sent.is_sentence();
		if (filter != null)
			assert (filter.get_nodetype() == AST_EXPR) || (filter.get_nodetype() == AST_EXPR_BUILTIN);
		if (source2 != null) {
			assert source2.get_nodetype() == AST_ID;
		}

		assert iter_typ.is_iter_type();
		ast_id i = (ast_id) id;
		ast_id s = (ast_id) source;
		ast_sent b = (ast_sent) sent;
		ast_expr e = (ast_expr) filter;

		ast_foreach fe = ast_foreach.new_foreach(i, s, b, iter_typ, e);
		fe.set_sequential(is_seq);
		fe.set_reverse_iteration(is_backward);
		fe.set_source2((ast_id) source2);
		return fe;
	}

	public static ast_node GM_bfs(ast_node it, ast_node source, ast_node root, ast_node navigator, ast_node f_filter, ast_node b_filter, ast_node f_sent,
			ast_node b_sent, boolean use_tp, boolean is_bfs) {
		assert it.get_nodetype() == AST_ID;
		assert source.get_nodetype() == AST_ID;
		assert root.get_nodetype() == AST_ID;
		if (navigator != null)
			assert navigator.is_expr();
		if (f_filter != null)
			assert f_filter.is_expr();
		if (b_filter != null)
			assert b_filter.is_expr();
		if (f_sent != null)
			assert f_sent.get_nodetype() == AST_SENTBLOCK;
		if (b_sent != null)
			assert b_sent.get_nodetype() == AST_SENTBLOCK;

		ast_bfs bfs = ast_bfs.new_bfs((ast_id) it, (ast_id) source, (ast_id) root, (ast_expr) navigator, (ast_expr) f_filter, (ast_expr) b_filter,
				(ast_sentblock) f_sent, (ast_sentblock) b_sent, use_tp, is_bfs);

		return bfs;
	}

	public static ast_node GM_if(ast_node cond, ast_node t, ast_node e) {
		assert cond.is_expr();
		assert t.is_sentence();
		if (e != null)
			assert e.is_sentence();

		return ast_if.new_if((ast_expr) cond, (ast_sent) t, (ast_sent) e);
	}

	public static ast_node GM_dowhile(ast_node cond, ast_node body) {
		assert cond.is_expr();
		assert body.get_nodetype() == AST_SENTBLOCK;

		return ast_while.new_do_while((ast_expr) cond, (ast_sentblock) body);
	}

	public static ast_node GM_while(ast_node cond, ast_node body) {
		assert cond.is_expr();
		assert body.get_nodetype() == AST_SENTBLOCK;

		return ast_while.new_while((ast_expr) cond, (ast_sentblock) body);
	}

	public static ast_node GM_return(ast_node expr, int l, int c) {
		if (expr != null)
			assert expr.is_expr();
		ast_return N = ast_return.new_return((ast_expr) expr);
		N.set_line(l);
		N.set_col(c);
		return N;
	}

	public static void GM_set_lineinfo(ast_node n, int line, int col) {
		n.set_line(line);
		n.set_col(col);
	}

	public static expr_list GM_empty_expr_list() {
		return new expr_list();
	}

	public static expr_list GM_single_expr_list(ast_node id) {
		expr_list e = new expr_list();
		assert id.is_expr();
		e.LIST.addLast((ast_expr) id);
		return e;
	}

	public static expr_list GM_add_expr_front(ast_node id, expr_list l) {
		assert id.is_expr();
		l.LIST.addFirst((ast_expr) id);
		return l;
	}

	public static ast_node GM_new_call_sent(ast_node n, boolean is_builtin) {
		if (is_builtin) {
			assert n.get_nodetype() == AST_EXPR_BUILTIN;
			return ast_call.new_builtin_call((ast_expr_builtin) n);
		}
		assert false;
		return null;
	}

	public static lhs_list GM_single_lhs_list(ast_node lhs) {
		assert (lhs.get_nodetype() == AST_FIELD) || (lhs.get_nodetype() == AST_ID);
		lhs_list l = new lhs_list();
		l.LIST.addLast(lhs);
		return l;
	}

	public static lhs_list GM_add_lhs_list_front(ast_node lhs, lhs_list list) {
		assert (lhs.get_nodetype() == AST_FIELD) || (lhs.get_nodetype() == AST_ID);
		list.LIST.addFirst(lhs);
		return list;
	}

	public static ast_node GM_foreign_sent(ast_node foreign) {
		assert foreign.get_nodetype() == AST_EXPR_FOREIGN;
		ast_expr_foreign f = (ast_expr_foreign) foreign;
		return ast_foreign.new_foreign(f);

	}

	public static ast_node GM_foreign_sent_mut(ast_node foreign, lhs_list list) {
		assert foreign.get_nodetype() == AST_EXPR_FOREIGN;
		ast_expr_foreign f = (ast_expr_foreign) foreign;
		return ast_foreign.new_foreign_mutate(f, list);
	}

}