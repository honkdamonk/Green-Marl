package ast;

import frontend.SYMTAB_TYPES;
import frontend.gm_scope;
import frontend.gm_symtab;
import inc.gm_code_writer;

import java.util.HashMap;

import common.GlobalMembersGm_traverse;
import common.gm_apply;

public abstract class ast_node {
	gm_code_writer Out; // TODO stub!

	// C++ TO JAVA CONVERTER TODO TASK: Java has no concept of a 'friend' class:
	// friend class gm_apply;
	protected ast_node(AST_NODE_TYPE nt) {
		this.nodetype = nt;
		this.parent = null;
		this.line = 0;
		this.col = 0;
		this.sym_vars = null;
		this.sym_procs = null;
		this.sym_fields = null;
	}

	protected ast_node() {
		this.nodetype = AST_NODE_TYPE.AST_END;
		this.parent = null;
		this.line = 0;
		this.col = 0;
		this.sym_vars = null;
		this.sym_procs = null;
		this.sym_fields = null;
	}

	protected AST_NODE_TYPE nodetype;
	protected ast_node parent;

	public final void set_nodetype(AST_NODE_TYPE nt) {
		nodetype = nt;
	}

	public void dispose() {
		// java.util.Iterator<String, ast_extra_info> i;
		// for (i = extra.iterator(); i.hasNext();)
		// {
		// if (i.next().getValue() != null)
		// i.next().getValue().dispose();
		// }
		extra.clear();
	}

	public final AST_NODE_TYPE get_nodetype() {
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
		return (nodetype == AST_NODE_TYPE.AST_SENTBLOCK) || (nodetype == AST_NODE_TYPE.AST_FOREACH) || (nodetype == AST_NODE_TYPE.AST_PROCDEF)
				|| (nodetype == AST_NODE_TYPE.AST_EXPR_RDC) || (nodetype == AST_NODE_TYPE.AST_BFS);
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
		int t = get_nodetype() == (AST_NODE_TYPE.AST_PROCDEF) != 0 ? SYMTAB_TYPES.GM_SYMTAB_ARG : SYMTAB_TYPES.GM_SYMTAB_VAR;
		if (post_apply) {
			a.apply2(get_symtab_var(), t);
		} else {
			a.apply(get_symtab_var(), t);
		}
		GlobalMembersGm_traverse.apply_symtab_each(a, get_symtab_var(), t, is_post);

		if (post_apply) {
			a.apply2(get_symtab_field(), SYMTAB_TYPES.GM_SYMTAB_FIELD);
		} else {
			a.apply(get_symtab_field(), SYMTAB_TYPES.GM_SYMTAB_FIELD);
		}
		GlobalMembersGm_traverse.apply_symtab_each(a, get_symtab_field(), SYMTAB_TYPES.GM_SYMTAB_FIELD, is_post);

		if (post_apply) {
			a.apply2(get_symtab_proc(), SYMTAB_TYPES.GM_SYMTAB_PROC);
		} else {
			a.apply(get_symtab_proc(), SYMTAB_TYPES.GM_SYMTAB_PROC);
		}
		GlobalMembersGm_traverse.apply_symtab_each(a, get_symtab_proc(), SYMTAB_TYPES.GM_SYMTAB_PROC, is_post);
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

	protected gm_symtab sym_vars;
	protected gm_symtab sym_fields;
	protected gm_symtab sym_procs;

	public void delete_symtabs() {
		sym_vars = null;
		sym_fields = null;
		sym_procs = null;
	}

	public void create_symtabs() {
		sym_vars = new gm_symtab(SYMTAB_TYPES.GM_SYMTAB_VAR, this);
		sym_fields = new gm_symtab(SYMTAB_TYPES.GM_SYMTAB_FIELD, this);
		sym_procs = new gm_symtab(SYMTAB_TYPES.GM_SYMTAB_PROC, this);
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
		String s = id;
		java.util.Iterator<String, ast_extra_info> i = extra.find(s);
		if (i == extra.end())
			return null;
		else
			return i.next().getValue();
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
		java.util.HashSet<Object> S = ((ast_extra_info_set) find_info(id)).get_set();
		S.add(element);
	}

	public java.util.HashSet<Object> get_info_set(String id) {
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
		java.util.LinkedList<Object> S = ((ast_extra_info_list) find_info(id)).get_list();
		S.addLast(element);
	}

	public java.util.LinkedList<Object> get_info_list(String id) {
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
		HashMap<Object, Object> S = ((ast_extra_info_map) find_info(id)).get_map();
		S.put(k, v);
	}

	public Object find_info_map_value(String id, Object key) {
		if (find_info(id) == null) {
			ast_extra_info_map INFO = new ast_extra_info_map();
			add_info(id, INFO);
		}
		HashMap<Object, Object> S = ((ast_extra_info_map) find_info(id)).get_map();

		// if not in the map? NULL will be retuned. right?
		if (!S.containsKey(key)) {
			S.put(key, null);
		}
		return S.get(key);
	}

	public HashMap<Object, Object> get_info_map(String id) {
		ast_extra_info_map INFO = ((ast_extra_info_map) find_info(id));
		assert INFO != null;
		return INFO.get_map();
	}

	public void copy_info_from(ast_node n) {
		java.util.Iterator<String, ast_extra_info> I;
		for (I = n.extra.begin(); I.hasNext();) {
			String s = I.next().getKey();
			ast_extra_info e = I.next().getValue();
			if (!this.extra.containsKey(s)) {
				this.extra.put(s, e.copy());
			}
		}
	}

	protected int line;
	protected int col;
	protected HashMap<String, ast_extra_info> extra = new HashMap<String, ast_extra_info>();
}