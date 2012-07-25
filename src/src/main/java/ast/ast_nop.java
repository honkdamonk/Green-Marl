package ast;

import inc.nop_enum_cpp;
import common.GlobalMembersGm_dumptree;
import common.gm_apply;

// a dummy nop IR.
// May be used in back-end processing
//C++ TO JAVA CONVERTER NOTE: Java has no need of forward class declarations:
//class gm_symtab_entry;
//class gm_rwinfo;
//typedef std::list<gm_rwinfo*> gm_rwinfo_list;
//typedef std::map<gm_symtab_entry*, gm_rwinfo_list*> gm_rwinfo_map;
public class ast_nop extends ast_sent {
	protected ast_nop() {
		super(AST_NODE_TYPE.AST_NOP);
		this.subtype = nop_enum_cpp.NOP_REDUCE_SCALAR;
	}

	protected ast_nop(nop_enum_cpp nopReduceScalar) {
		super(AST_NODE_TYPE.AST_NOP);
		set_subtype(nopReduceScalar);
	}

	public void dispose() {
	}

	public final nop_enum_cpp get_subtype() {
		return subtype;
	}

	public final void set_subtype(nop_enum_cpp nopReduceScalar) {
		subtype = nopReduceScalar;
	}

	public void reproduce(int ind_level) {
		Out.pushln("//NOP");
	}

	public void dump_tree(int ind_level) {
		GlobalMembersGm_dumptree.IND(ind_level);
		assert parent != null;
		System.out.printf("[NOP %d]\n", get_subtype());
	}

	@Override
	public void traverse_sent(gm_apply a, boolean is_post, boolean is_pre) {
	}

	public boolean do_rw_analysis() {
		return true;
	}

	private nop_enum_cpp subtype;
}