package ast;

import static ast.ast_node_type.AST_BFS;
import static ast.ast_node_type.AST_END;
import static ast.ast_node_type.AST_EXPR_RDC;
import static ast.ast_node_type.AST_FOREACH;
import static ast.ast_node_type.AST_PROCDEF;
import static ast.ast_node_type.AST_SENTBLOCK;
import static frontend.symtab_types.GM_SYMTAB_ARG;
import static frontend.symtab_types.GM_SYMTAB_FIELD;
import static frontend.symtab_types.GM_SYMTAB_PROC;
import static frontend.symtab_types.GM_SYMTAB_VAR;
import frontend.symtab_types;
import frontend.gm_scope;
import frontend.gm_symtab;
import inc.gm_code_writer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import common.gm_apply;
import common.gm_traverse;

public abstract class ast_node {
	
	protected static final gm_code_writer Out = new gm_code_writer();

	protected int line = 0;
	protected int col = 0;
	protected HashMap<String, ast_extra_info> extra = new HashMap<String, ast_extra_info>();
	protected ast_node_type nodetype = null;
	protected ast_node parent = null;
	protected gm_symtab sym_vars = null;
	protected gm_symtab sym_fields = null;
	protected gm_symtab sym_procs = null;

	protected ast_node(ast_node_type nt) {
		nodetype = nt;
	}

	protected ast_node() {
		nodetype = AST_END;
	}

	public final void set_nodetype(ast_node_type nt) {
		nodetype = nt;
	}

	public void dispose() {
		extra.clear();
	}

	public final ast_node_type get_nodetype() {
		return nodetype;
	}

	public final ast_node get_parent() {
		return parent;
	}

	public final void set_parent(ast_node p) {
		parent = p;
	}

	public boolean is_sentence() {
		return false;
	}

	public boolean is_expr() {
		return false;
	}

	public final boolean has_symtab() {
		return (nodetype == AST_SENTBLOCK) || (nodetype == AST_FOREACH) || (nodetype == AST_PROCDEF) || (nodetype == AST_EXPR_RDC) || (nodetype == AST_BFS);
	}

	// for parser debug
	public abstract void reproduce(int id_level); // defined in reproduce.cc

	public abstract void dump_tree(int id_level); // defined in dump_tree.cc

	// defined in traverse.cc
	public void traverse(gm_apply a, boolean is_post, boolean is_pre) {
		assert false;
	}

	public final void traverse_pre(gm_apply a) {
		traverse(a, false, true);
	}

	public final void traverse_post(gm_apply a) {
		traverse(a, true, false);
	}

	public final void traverse_both(gm_apply a) {
		traverse(a, true, true);
	}

	public void apply_symtabs(gm_apply a, boolean is_post) {
		assert has_scope();
		boolean post_apply = is_post && a.has_separate_post_apply();
		symtab_types t = get_nodetype() == AST_PROCDEF ? GM_SYMTAB_ARG : GM_SYMTAB_VAR;
		if (post_apply) {
			a.apply2(get_symtab_var(), t);
		} else {
			a.apply(get_symtab_var(), t);
		}
		gm_traverse.apply_symtab_each(a, get_symtab_var(), t, is_post);

		if (post_apply) {
			a.apply2(get_symtab_field(), GM_SYMTAB_FIELD);
		} else {
			a.apply(get_symtab_field(), GM_SYMTAB_FIELD);
		}
		gm_traverse.apply_symtab_each(a, get_symtab_field(), GM_SYMTAB_FIELD, is_post);

		if (post_apply) {
			a.apply2(get_symtab_proc(), GM_SYMTAB_PROC);
		} else {
			a.apply(get_symtab_proc(), GM_SYMTAB_PROC);
		}
		gm_traverse.apply_symtab_each(a, get_symtab_proc(), GM_SYMTAB_PROC, is_post);
	}

	// scoped elements
	public boolean has_scope() {
		return false;
	}

	public gm_symtab get_symtab_var() {
		assert has_scope();
		return sym_vars;
	}

	public gm_symtab get_symtab_field() {
		assert has_scope();
		return sym_fields;
	}

	public gm_symtab get_symtab_proc() {
		assert has_scope();
		return sym_procs;
	}

	public void get_this_scope(gm_scope s) {
		s.push_symtabs(sym_vars, sym_fields, sym_procs);
	}

	public void set_symtab_var(gm_symtab v) {
		assert has_scope();
		sym_vars = v;
	}

	public void set_symtab_field(gm_symtab f) {
		assert has_scope();
		sym_fields = f;
	}

	public void set_symtab_proc(gm_symtab p) {
		assert has_scope();
		sym_procs = p;
	}

	public void delete_symtabs() {
		sym_vars = null;
		sym_fields = null;
		sym_procs = null;
	}

	public void create_symtabs() {
		sym_vars = new gm_symtab(GM_SYMTAB_VAR, this);
		sym_fields = new gm_symtab(GM_SYMTAB_FIELD, this);
		sym_procs = new gm_symtab(GM_SYMTAB_PROC, this);
	}

	public int get_line() {
		return line;
	}

	public int get_col() {
		return col;
	}

	public final void set_line(int l) {
		line = l;
	}

	public final void set_col(int c) {
		col = c;
	}

	public final void copy_line_info(ast_node n) {
		this.col = n.col;
		this.line = n.line;
	}

	// --------------------------------------
	// extra infomation attached to this node
	// --------------------------------------
	public boolean has_info(String id) {
		return extra.containsKey(id);
	}

	public ast_extra_info find_info(String id) {
		if (has_info(id)) {
			return extra.get(id);
		} else {
			return null;
		}
	}

	public boolean find_info_bool(String id) {
		ast_extra_info info = find_info(id);
		if (info == null)
			return false;
		else
			return info.bval;
	}

	public String find_info_string(String id) {
		ast_extra_info_string info = (ast_extra_info_string) find_info(id);
		if (info == null)
			return ""; // or NULL string?
		else
			return info.get_string();
	}

	public float find_info_float(String id) {
		ast_extra_info info = find_info(id);
		if (info == null)
			return 0;
		else
			return info.fval;
	}
	
	public Object find_info_obj(String id) {
		ast_extra_info info = find_info(id);
		if (info == null)
			return null;
		else
			return info.objval;
	}

	public int find_info_int(String id) {
		ast_extra_info info = find_info(id);
		if (info == null)
			return 0;
		else
			return info.ival;
	}

	public Object find_info_ptr(String id) {
		ast_extra_info info = find_info(id);
		if (info == null)
			return null;
		else
			return info.ptr1;
	}

	public Object find_info_ptr2(String id) {
		ast_extra_info info = find_info(id);
		if (info == null)
			return null;
		else
			return info.ptr2;
	}

	public void add_info(String id, ast_extra_info e) {
		// should I delete repeated entry?
		String s = id;
		extra.put(s, e);
	}

	public void add_info_int(String id, int i) {
		ast_extra_info e = find_info(id);
		if (e == null)
			add_info(id, new ast_extra_info(i));
		else
			e.ival = i;
	}

	public void add_info_bool(String id, boolean b) {
		ast_extra_info e = find_info(id);
		if (e == null)
			add_info(id, new ast_extra_info(b));
		else
			e.bval = b;
	}

	public void add_info_ptr(String id, Object ptr1) {
		add_info_ptr(id, ptr1, null);
	}

	public void add_info_ptr(String id, Object ptr1, Object ptr2) {
		add_info(id, new ast_extra_info(ptr1, ptr2));
	}

	public void add_info_float(String id, float f) {
		ast_extra_info e = find_info(id);
		if (e == null)
			add_info(id, new ast_extra_info(f));
		else
			e.fval = f;
	}
	
	public void add_info_obj(String id, Object obj) {
		ast_extra_info e = find_info(id);
		if (e == null)
			add_info(id, new ast_extra_info(obj));
		else
			e.objval = obj;
	}

	public void add_info_string(String id, String str) {
		add_info(id, new ast_extra_info_string(str));
	}

	public void remove_info(String id) {
		String s = id;
		extra.remove(s); // [XXX] need delete extra_info object
	}

	public void remove_all_info() {
		extra.clear();
	}

	public boolean has_info_set(String id) {
		return (find_info(id) != null);
	}

	public void add_info_set_element(String id, Object element) {
		if (find_info(id) == null) {
			ast_extra_info_set INFO = new ast_extra_info_set();
			add_info(id, INFO);
		}
		Set<Object> S = ((ast_extra_info_set) find_info(id)).get_set();
		S.add(element);
	}

	public HashSet<Object> get_info_set(String id) {
		ast_extra_info_set INFO = ((ast_extra_info_set) find_info(id));
		assert INFO != null;
		return INFO.get_set();
	}

	public boolean has_info_list(String id) {
		return (find_info(id) != null);
	}

	public void add_info_list_element(String id, Object element) {
		if (find_info(id) == null) {
			ast_extra_info_list INFO = new ast_extra_info_list();
			add_info(id, INFO);
		}
		LinkedList<Object> S = ((ast_extra_info_list) find_info(id)).get_list();
		S.addLast(element);
	}

	public LinkedList<Object> get_info_list(String id) {
		ast_extra_info_list INFO = ((ast_extra_info_list) find_info(id));
		assert INFO != null;
		return INFO.get_list();
	}

	public boolean has_info_map(String id) {
		return (find_info(id) != null);
	}

	public void add_info_map_key_value(String id, Object k, Object v) {
		if (find_info(id) == null) {
			ast_extra_info_map INFO = new ast_extra_info_map();
			add_info(id, INFO);
		}
		Map<Object, Object> S = ((ast_extra_info_map) find_info(id)).get_map();
		S.put(k, v);
	}

	public Object find_info_map_value(String id, Object key) {
		if (find_info(id) == null) {
			ast_extra_info_map INFO = new ast_extra_info_map();
			add_info(id, INFO);
		}
		Map<Object, Object> S = ((ast_extra_info_map) find_info(id)).get_map();
		return S.get(key);
	}

	public HashMap<Object, Object> get_info_map(String id) {
		ast_extra_info_map INFO = ((ast_extra_info_map) find_info(id));
		assert INFO != null;
		return INFO.get_map();
	}

	public void copy_info_from(ast_node n) {
		// TODO not tested!
		for (String s : n.extra.keySet()) {
			ast_extra_info e = n.extra.get(s);
			if (!this.extra.containsKey(s)) {
				this.extra.put(s, e.copy());
			}
		}
	}

}