package ast;

import inc.nop_enum_cpp;
import common.GlobalMembersGm_dumptree;
import common.gm_apply;

// a dummy nop IR.
// May be used in back-end processing
public class ast_nop extends ast_sent {
	
	private nop_enum_cpp subtype;
	
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

}