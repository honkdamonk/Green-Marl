package ast;

import inc.GMTYPE_T;
import common.GlobalMembersGm_dumptree;

import frontend.gm_symtab_entry;

// access of identifier
//C++ TO JAVA CONVERTER NOTE: Java has no need of forward class declarations:
//class ast_typedecl;
public class ast_id extends ast_node {
	
	public String name;

	private gm_symtab_entry info;
	private String gen_name;

	private boolean instant_assignment;	
	
	private ast_id() {
		super(AST_NODE_TYPE.AST_ID);
		this.name = null;
		this.info = null;
		this.gen_name = null;
		this.instant_assignment = false;
	}

	private ast_id(String org, int l, int c) {
		super(AST_NODE_TYPE.AST_ID);
		this.info = null;
		this.gen_name = null;
		this.instant_assignment = false;
		if (org != null) {
			name = org;
		} else {
			name = null;
		}
		set_line(l);
		set_col(c);
	}
	
	// C++ TO JAVA CONVERTER TODO TASK: Java has no concept of a 'friend' class:
	// friend class gm_symtab_entry;
	public void dispose() {
		name = null;
		gen_name = null; // if name is not usable in generator
	}

	// make a copy of id reference
	// [NOTE] pointer to symbol table entry is *not* copied if cp_syminfo is
	// false
	public final ast_id copy() {
		return copy(false);
	}

	public final ast_id copy(boolean cp_syminfo) {
		ast_id cp;
		cp = new ast_id(get_orgname(), line, col); // name can be null here.
													// [xxx] WHY?
		if (cp_syminfo) {
			cp.info = this.info;
		}
		cp.set_instant_assigned(is_instantly_assigned());
		return cp;
	}

	// -------------------------------------------------
	// Type information related to this id
	// set up by type-checker during local_typecheck
	// -------------------------------------------------
	public final gm_symtab_entry getSymInfo() {
		return info;
	}

	public final void setSymInfo(gm_symtab_entry e) {
		setSymInfo(e, false);
	}

	public final void setSymInfo(gm_symtab_entry e, boolean is_symtab_entry) {
		info = e;
		if (!is_symtab_entry)
			use_names_from_symbol();
	}

	public ast_typedecl getTypeInfo() {
		assert info != null;
		return info.getType();
	}

	public GMTYPE_T getTypeSummary() {
		assert info != null;
		return info.getType().getTypeSummary();
	}

	// return TypeDecl->getTypeSummary. returns one of GMTYPE_*

	// only called for property types
	public ast_typedecl getTargetTypeInfo() {
		assert info != null;
		return info.getType().get_target_type();
	}

	public GMTYPE_T getTargetTypeSummary() {
		assert info != null;
		return info.getType().getTargetTypeSummary();
	}

	public static ast_id new_id(String org, int line, int col) {
		return new ast_id(org, line, col);
	}

	public String get_orgname() {
		if ((info == null) || (info.getId() == this)) {
			if (name != null)
				return name;
			else {
				System.out.printf("line:%d, col:%d, name:%p\n", line, col, name);
				assert false;
				return null;
			}
		} else {
			assert info != null;
			return get_orgname_from_symbol();
		}
	}

	// copy is saved. old name is deleted
	public final void set_orgname(String c) {
		name = c;
	}

	public String get_genname() {
		if ((info == null) || (info.getId() == this)) {
			if (gen_name != null)
				return gen_name;
			else if (name != null)
				return name;
			else
				assert false;
			return null;
		} else {
			assert info != null;
			return get_genname_from_symbol();
		}
	}

	// copy is saved. old name is deleted
	public final void set_genname(String c) {
		gen_name = c;
	}

	public void reproduce(int ind_level) {
		Out.push(get_orgname());
	}

	public void dump_tree(int ind_lv) {
		// assert(parent!=NULL);
		GlobalMembersGm_dumptree.IND(ind_lv);
		System.out.printf("%s", get_orgname());
	}

	public final boolean is_instantly_assigned() {
		return instant_assignment;
	}

	public final void set_instant_assigned(boolean value) {
		instant_assignment = value;
	}

	public String get_orgname_from_symbol() {
		assert info != null;
		assert info.getId() != this;
		return info.getId().get_orgname();
	}

	public String get_genname_from_symbol() {
		assert info != null;
		assert info.getId() != this;
		return info.getId().get_genname();
	}

	public void use_names_from_symbol() {
		assert info != null;
		if (name != null)
			name = null;
		name = null;
		if (gen_name != null)
			gen_name = null;
		gen_name = null;
	}

}