package ast;

import common.gm_traverse;
import common.gm_apply;

//==========================================================================
public abstract class ast_sent extends ast_node {

	private int eline = 0;
	private boolean _par = false;

	protected ast_sent(AST_NODE_TYPE y) {
		super(y);
	}

	// save original empty lines before this sentence
	public final int get_empty_lines_before() {
		return eline;
	}

	public final void set_empty_lines_before(int t) {
		eline = t;
	}

	public void traverse(gm_apply a, boolean is_post, boolean is_pre) {
		a.set_current_sent(this);

		if (has_symtab())
			a.begin_context(this);
		boolean for_symtab = a.is_for_symtab();
		boolean for_sent = a.is_for_sent();

		if (is_pre) {
			if (for_sent)
				a.apply(this);
			if (has_symtab() && for_symtab)
				apply_symtabs(a, gm_traverse.PRE_APPLY);
		}

		traverse_sent(a, is_post, is_pre);

		a.set_current_sent(this);

		if (is_post) {
			if (has_symtab() && for_symtab) {
				apply_symtabs(a, gm_traverse.POST_APPLY);
			}
			if (for_sent) {
				if (a.has_separate_post_apply())
					a.apply2(this);
				else
					a.apply(this);
			}
		}

		if (has_symtab())
			a.end_context(this);
	}

	public void traverse_sent(gm_apply a, boolean is_post, boolean is_pre) {
		assert false;
	}

	@Override
	public boolean is_sentence() {
		return true;
	}

	// when to set this variable? (-> should be just before code gen.)
	public boolean is_under_parallel_execution() {
		return _par;
	}

	public void set_under_parallel_execution(boolean b) {
		_par = b;
	}

}