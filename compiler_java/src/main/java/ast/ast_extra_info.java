package ast;

// any information can be added to nodes
public class ast_extra_info {

	public int ival = 0;
	public boolean bval = false;
	public float fval = 0.0f;
	public Object ptr1 = null;
	public Object ptr2 = null;

	public ast_extra_info() {
	}

	public ast_extra_info(boolean b) {
		bval = b;
	}

	public ast_extra_info(int i) {
		ival = i;
	}

	public ast_extra_info(float f) {
		fval = f;
	}

	public ast_extra_info(Object p1, Object p2) {
		ptr1 = p1;
		ptr2 = p2;
	}

	public ast_extra_info copy() {
		ast_extra_info i = new ast_extra_info();
		i.base_copy(this);
		return i;
	}

	public void base_copy(ast_extra_info from) {
		ival = from.ival;
		bval = from.bval;
		fval = from.fval;
		ptr1 = from.ptr1;
		ptr2 = from.ptr2;
	}

}