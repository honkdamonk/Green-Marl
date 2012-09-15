package ast;

import inc.nop_enum_cpp;
import common.gm_dumptree;
import common.gm_apply;

// a dummy nop IR.
// May be used in back-end processing
public class ast_nop extends ast_sent {

	private nop_enum_cpp subtype;

	protected ast_nop() {
		super(ast_node_type.AST_NOP);
		subtype = nop_enum_cpp.NOP_REDUCE_SCALAR;
	}

	protected ast_nop(nop_enum_cpp nopReduceScalar) {
		super(ast_node_type.AST_NOP);
		set_subtype(nopReduceScalar);
	}

	public final nop_enum_cpp get_subtype() {
		return subtype;
	}

	public final void set_subtype(nop_enum_cpp nopReduceScalar) {
		subtype = nopReduceScalar;
	}

	@Override
	public void reproduce(int ind_level) {
		Out.pushln("//NOP");
	}

	@Override
	public void dump_tree(int ind_level) {
		gm_dumptree.IND(ind_level);
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