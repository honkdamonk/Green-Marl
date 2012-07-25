package ast;

// any information can be added to nodes
public class ast_extra_info {
	public int ival;
	public boolean bval;
	public float fval;
	public Object ptr1;
	public Object ptr2;

	public ast_extra_info() {
		this.ival = 0;
		this.bval = false;
		this.fval = 0;
		this.ptr1 = null;
		this.ptr2 = null;
	}

	public ast_extra_info(boolean b) {
		this.ival = 0;
		this.bval = b;
		this.fval = 0;
		this.ptr1 = null;
		this.ptr2 = null;
	}

	public ast_extra_info(int i) {
		this.ival = i;
		this.bval = false;
		this.fval = 0;
		this.ptr1 = null;
		this.ptr2 = null;
	}

	public ast_extra_info(float f) {
		this.ival = 0;
		this.bval = false;
		this.fval = f;
		this.ptr1 = null;
		this.ptr2 = null;
	}

	public ast_extra_info(Object p1, Object p2) {
		this.ival = 0;
		this.bval = false;
		this.fval = 0;
		this.ptr1 = p1;
		this.ptr2 = p2;
	}

	public void dispose() {
	}

	public ast_extra_info copy() {
		ast_extra_info i = new ast_extra_info();
		i.base_copy(this);
		return i;
	}

	public void base_copy(ast_extra_info from) {
		// this = from;
		ival = from.ival;
		bval = from.bval;
		fval = from.fval;
		ptr1 = from.ptr1;
		ptr2 = from.ptr2;

	}

}