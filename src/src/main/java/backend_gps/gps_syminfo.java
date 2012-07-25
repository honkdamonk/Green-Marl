package backend_gps;

import ast.ast_extra_info;
import inc.GM_REDUCE_T;

public class gps_syminfo extends ast_extra_info {
	public gps_syminfo(boolean sc) {
		this._scalar = sc;
		this._is_arg = false;
		this._used_in_multiple_BB = false;
		this.last_BB = -1;
		this.used_as_rhs = false;
		this.used_as_lhs = false;
		this.used_as_reduce = false;
		this.used_in_vertex = false;
		this.used_in_master = false;
		this.used_in_receiver = false;
		this.reduce_op_type = GM_REDUCE_T.GMREDUCE_NULL;
		this.start_byte = 0;
		this.scope = gm_gps_scope_t.GPS_SCOPE_GLOBAL;
	}

	public void dispose() {
	}

	public final void set_is_argument(boolean b) {
		_is_arg = b;
	}

	public final boolean is_argument() {
		return _is_arg;
	}

	public final boolean is_scalar() {
		return _scalar;
	}

	public final void add_usage_in_BB(int bb_no, gm_gps_symbol_usage_t usage, gm_gps_symbol_usage_location_t context) {
		add_usage_in_BB(bb_no, usage, context, GM_REDUCE_T.GMREDUCE_NULL);
	}

	// C++ TO JAVA CONVERTER NOTE: Java does not allow default values for
	// parameters. Overloaded methods are inserted above.
	// ORIGINAL LINE: void add_usage_in_BB(int bb_no, int usage, int context,
	// int reduce_type = GMREDUCE_NULL)
	public final void add_usage_in_BB(int bb_no, gm_gps_symbol_usage_t usage, gm_gps_symbol_usage_location_t context, GM_REDUCE_T reduce_type) {
		// if not already in
		for (int i = 0; i < (int) used_BB.size(); i++) {
			if ((used_BB.get(i) == bb_no) && (used_type.get(i) == usage) && (used_context.get(i) == context))
				return;
		}

		if (last_BB == -1)
			last_BB = bb_no;

		else if (last_BB != bb_no)
			_used_in_multiple_BB = true;

		used_BB.add(bb_no);
		used_type.add(usage);
		used_context.add(context);

		if (usage == gm_gps_symbol_usage_t.GPS_SYM_USED_AS_RHS) {
			used_as_rhs = true;
		} else if (usage == gm_gps_symbol_usage_t.GPS_SYM_USED_AS_LHS) {
			used_as_lhs = true;
		} else if (usage == gm_gps_symbol_usage_t.GPS_SYM_USED_AS_REDUCE) {
			used_as_reduce = true;
			reduce_op_type = reduce_type;
		} else {
			assert false;
		}

		switch (context) {
		case GPS_CONTEXT_MASTER:
			used_in_master = true;
			break;
		case GPS_CONTEXT_VERTEX:
			used_in_vertex = true;
			break;
		// case GPS_CONTEXT_SENDER: used_in_sender = true; break;
		case GPS_CONTEXT_RECEIVER:
			used_in_receiver = true;
			break;
		default:
			assert false;
			break;
		}

		// reduce_op_type = reduce_type; //TODO why was this here? only set if
		// symbol is used as reduce (see 13 lines back)
		// reduce_op_type is initialized to GMREDUCE_NULL and can only be
		// overwritten by one reduce operation, this is checked
		// in gm_reduce_error_check.cc
	}

	public final boolean is_used_in_multiple_BB() {
		return _used_in_multiple_BB;
	}

	public final boolean is_used_as_rhs() {
		return used_as_rhs;
	}

	public final boolean is_used_as_lhs() {
		return used_as_lhs;
	}

	public final boolean is_used_as_reduce() {
		return used_as_reduce;
	}

	public final boolean is_used_in_vertex() {
		return used_in_vertex;
	}

	public final boolean is_used_in_master() {
		return used_in_master;
	}

	public final boolean is_used_in_receiver() {
		return used_in_receiver;
	}

	public final GM_REDUCE_T get_reduce_type() {
		return reduce_op_type;
	}

	// for message/state
	public final int get_start_byte() {
		return start_byte;
	}

	public final void set_start_byte(int b) {
		start_byte = b;
	}

	// where the symbol is defined?
	public final gm_gps_scope_t get_scope() {
		return scope;
	}

	public final void set_scope(gm_gps_scope_t scope2) {
		scope = scope2;
	}

	public final boolean is_scoped_global() {
		return scope == gm_gps_scope_t.GPS_SCOPE_GLOBAL;
	}

	public final boolean is_scoped_outer() {
		return scope == gm_gps_scope_t.GPS_SCOPE_OUTER;
	}

	public final boolean is_scoped_inner() {
		return scope == gm_gps_scope_t.GPS_SCOPE_INNER;
	}

	private boolean _used_in_multiple_BB;
	private int last_BB;
	private boolean _scalar;
	private boolean _is_arg;
	private java.util.ArrayList<Integer> used_BB = new java.util.ArrayList<Integer>();
	private java.util.ArrayList<gm_gps_symbol_usage_t> used_type = new java.util.ArrayList<gm_gps_symbol_usage_t>();
	private java.util.ArrayList<gm_gps_symbol_usage_location_t> used_context = new java.util.ArrayList<gm_gps_symbol_usage_location_t>();
	private gm_gps_scope_t scope; // GPS_SCOPE_XX

	private boolean used_as_rhs;
	private boolean used_as_lhs;
	private boolean used_as_reduce;

	private boolean used_in_vertex;
	private boolean used_in_master;
	// bool used_in_sender;
	private boolean used_in_receiver;

	private GM_REDUCE_T reduce_op_type;
	private int start_byte;
}