package frontend;

import inc.nop_enum_cpp;
import ast.ast_nop;

public class ast_temp_marker extends ast_nop {

	public ast_temp_marker() {
		set_subtype(nop_enum_cpp.NOP_REDUCE_SCALAR);
	}
}
