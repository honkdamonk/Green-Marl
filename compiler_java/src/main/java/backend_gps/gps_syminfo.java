package backend_gps;

import inc.gm_reduce;

import java.util.ArrayList;

import ast.ast_extra_info;

public class gps_syminfo extends ast_extra_info {
	
	private boolean _used_in_multiple_BB = false;
	private int last_BB = -1;
	private boolean _scalar;
	private boolean _is_arg = false;
	private final ArrayList<Integer> used_BB = new ArrayList<Integer>();
	private final ArrayList<gm_gps_symbol_usage> used_type = new ArrayList<gm_gps_symbol_usage>();
	private final ArrayList<gm_gps_symbol_usage_location> used_context = new ArrayList<gm_gps_symbol_usage_location>();
	private gm_gps_scope scope = gm_gps_scope.GPS_SCOPE_GLOBAL;

	private boolean used_as_rhs = false;
	private boolean used_as_lhs = false;
	private boolean used_as_reduce = false;

	private boolean used_in_vertex = false;
	private boolean used_in_master = false;
	// bool used_in_sender;
	private boolean used_in_receiver = false;

	private gm_reduce reduce_op_type = gm_reduce.GMREDUCE_NULL;
	private int start_byte = 0;
	
	public gps_syminfo(boolean sc) {
		_scalar = sc;
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

	public final void add_usage_in_BB(int bb_no, gm_gps_symbol_usage usage, gm_gps_symbol_usage_location context) {
		add_usage_in_BB(bb_no, usage, context, gm_reduce.GMREDUCE_NULL);
	}

	public final void add_usage_in_BB(int bb_no, gm_gps_symbol_usage usage, gm_gps_symbol_usage_location context, gm_reduce reduce_type) {
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

		if (usage == gm_gps_symbol_usage.GPS_SYM_USED_AS_RHS) {
			used_as_rhs = true;
		} else if (usage == gm_gps_symbol_usage.GPS_SYM_USED_AS_LHS) {
			used_as_lhs = true;
		} else if (usage == gm_gps_symbol_usage.GPS_SYM_USED_AS_REDUCE) {
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

	public final gm_reduce get_reduce_type() {
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
	public final gm_gps_scope get_scope() {
		return scope;
	}

	public final void set_scope(gm_gps_scope scope2) {
		scope = scope2;
	}

	public final boolean is_scoped_global() {
		return scope == gm_gps_scope.GPS_SCOPE_GLOBAL;
	}

	public final boolean is_scoped_outer() {
		return scope == gm_gps_scope.GPS_SCOPE_OUTER;
	}

	public final boolean is_scoped_inner() {
		return scope == gm_gps_scope.GPS_SCOPE_INNER;
	}

}