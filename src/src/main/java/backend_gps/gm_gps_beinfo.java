package backend_gps;

import frontend.gm_symtab_entry;
import inc.GMTYPE_T;
import inc.gm_backend_info;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import ast.ast_foreach;
import ast.ast_procdef;
import ast.ast_sent;
import ast.ast_sentblock;

import common.GlobalMembersGm_main;

//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define TO_STR(X) #X
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define DEF_STRING(X) static const char *X = "X"

// backend information per each procedure
public class gm_gps_beinfo extends gm_backend_info {

	public gm_gps_beinfo(ast_procdef d) {
		body = d;
		comm_id = 0;
		basicblock_id = 0;
		total_node_prop_size = 0;
		total_edge_prop_size = 0;
		rand_used = false;
		bb_entry = null;
	}

	public void dispose() {
	}

	public final void set_entry_basic_block(gm_gps_basic_block b) {
		bb_entry = b;
	}

	public final gm_gps_basic_block get_entry_basic_block() {
		return bb_entry;
	}

	public final LinkedList<gm_gps_basic_block> get_basic_blocks() {
		return bb_blocks;
	}

	// -------------------------------------------------------------------
	// inner loops (a.k.a. communication loops) are seperately managed.
	// -------------------------------------------------------------------
	// public final HashSet<gm_gps_comm_unit, gm_gps_comm_unit>
	// get_communication_loops() {
	public final HashSet<gm_gps_comm_unit> get_communication_loops() {
		return comm_loops;
	}

	public final void add_communication_unit_nested(ast_foreach fe) {
		gm_gps_comm_unit U = new gm_gps_comm_unit(gm_gps_comm_t.GPS_COMM_NESTED, fe);
		add_communication_unit(U);
	}

	public final void add_communication_unit_initializer() {
		gm_gps_comm_unit U = new gm_gps_comm_unit(gm_gps_comm_t.GPS_COMM_INIT, null);
		add_communication_unit(U);
	}

	public final void add_communication_unit_random_write(ast_sentblock sb, gm_symtab_entry drv) {
		gm_gps_comm_unit U = new gm_gps_comm_unit(gm_gps_comm_t.GPS_COMM_RANDOM_WRITE, sb, drv);
		add_communication_unit(U);
	}

	public final gm_gps_communication_symbol_info find_communication_symbol_info(gm_gps_comm_unit C, gm_symtab_entry sym) {
		assert comm_symbol_info.containsKey(C);
		LinkedList<gm_gps_communication_symbol_info> sym_info = comm_symbol_info.get(C);
		Iterator<gm_gps_communication_symbol_info> I;
		for (I = sym_info.iterator(); I.hasNext();) {
			gm_gps_communication_symbol_info S = I.next();
			if (S.symbol == sym) // found
				return S;
		}

		return null;
	}

	// -------------------------------------------------------------
	// GPS Backend inforation per procedure
	// -------------------------------------------------------------

	// prepare to manage a communication loop
	public final void add_communication_unit(gm_gps_comm_unit C) {
		// C++ TO JAVA CONVERTER WARNING: The following line was determined to
		// be a copy constructor call - this should be verified and a copy
		// constructor should be created if it does not yet exist:
		// ORIGINAL LINE: if (comm_loops.find(C) != comm_loops.end())
		if (comm_loops.contains(C)) // already added
			return;

		comm_loops.add(C);

		LinkedList<gm_gps_communication_symbol_info> new_list = new LinkedList<gm_gps_communication_symbol_info>();
		comm_symbol_info.put(C, new_list); // create empty list by copying.

		gm_gps_communication_size_info S = new gm_gps_communication_size_info();
		S.id = issue_comm_id();
		comm_size_info.put(C, S); // create zero-sized communication

	}

	public final void add_communication_symbol_nested(ast_foreach fe, gm_symtab_entry sym) {
		gm_gps_comm_unit U = new gm_gps_comm_unit(gm_gps_comm_t.GPS_COMM_NESTED, fe);
		add_communication_symbol(U, sym);
	}

	public final void add_communication_symbol_initializer(gm_symtab_entry sym) {
		gm_gps_comm_unit U = new gm_gps_comm_unit(gm_gps_comm_t.GPS_COMM_INIT, null);
		add_communication_symbol(U, sym);
	}

	public final void add_communication_symbol_random_write(ast_sentblock sb, gm_symtab_entry drv, gm_symtab_entry sym) {
		gm_gps_comm_unit U = new gm_gps_comm_unit(gm_gps_comm_t.GPS_COMM_RANDOM_WRITE, sb, drv);
		add_communication_symbol(U, sym);
	}

	// Add a symbol to a communication loop
	public final void add_communication_symbol(gm_gps_comm_unit C, gm_symtab_entry sym) {
		assert comm_symbol_info.containsKey(C);

		LinkedList<gm_gps_communication_symbol_info> sym_info = comm_symbol_info.get(C);
		Iterator<gm_gps_communication_symbol_info> I;
		gm_gps_communication_size_info size_info = comm_size_info.get(C);

		GMTYPE_T target_type;
		if (sym.getType().is_property()) {
			target_type = sym.getType().getTargetTypeSummary();
		} else if (sym.getType().is_primitive()) {
			target_type = sym.getType().getTypeSummary();
		} else if (sym.getType().is_node_compatible()) {
			if (GlobalMembersGm_main.PREGEL_BE.get_lib().is_node_type_int())
				target_type = GMTYPE_T.GMTYPE_INT;
			else
				target_type = GMTYPE_T.GMTYPE_LONG;
		} else {
			assert false;
			throw new AssertionError();
		}
		int idx = 0;
		for (I = sym_info.iterator(); I.hasNext();) {
			gm_gps_communication_symbol_info S = I.next();
			if (S.symbol == sym) // already added
				return;
			if (S.gm_type == target_type)
				idx++;
		}

		gm_gps_communication_symbol_info II = new gm_gps_communication_symbol_info();
		II.symbol = sym;
		II.gm_type = target_type;
		II.idx = idx;

		// add sym-info
		sym_info.addLast(II); // add by copy

		// update size-info
		if (target_type == GMTYPE_T.GMTYPE_INT)
			size_info.num_int = (idx + 1);
		else if (target_type == GMTYPE_T.GMTYPE_BOOL)
			size_info.num_bool = (idx + 1);
		else if (target_type == GMTYPE_T.GMTYPE_LONG)
			size_info.num_long = (idx + 1);
		else if (target_type == GMTYPE_T.GMTYPE_FLOAT)
			size_info.num_float = (idx + 1);
		else if (target_type == GMTYPE_T.GMTYPE_DOUBLE)
			size_info.num_double = (idx + 1);
		else {
			System.out.printf("symbol = %s, target type = %d\n", sym.getId().get_genname(), target_type);
			assert false;
		}

	}

	public final LinkedList<gm_gps_communication_symbol_info> get_all_communication_symbols_nested(ast_foreach fe, int comm_type) {
		gm_gps_comm_unit U = new gm_gps_comm_unit(gm_gps_comm_t.GPS_COMM_NESTED, fe);
		return get_all_communication_symbols(U);
	}

	// find communication info
	public final LinkedList<gm_gps_communication_symbol_info> get_all_communication_symbols(gm_gps_comm_unit U)

	{
		assert comm_symbol_info.containsKey(U);
		return comm_symbol_info.get(U);
	}

	public final LinkedList<ast_sent> get_random_write_sents(gm_gps_comm_unit U) {
		return random_write_sents.get(U);
	}

	public final LinkedList<ast_sent> get_random_write_sents(ast_sentblock sb, gm_symtab_entry sym) {
		gm_gps_comm_unit U = new gm_gps_comm_unit(gm_gps_comm_t.GPS_COMM_RANDOM_WRITE, sb, sym);
		return get_random_write_sents(U);
	}

	public final void add_random_write_sent(gm_gps_comm_unit U, ast_sent s) {
		LinkedList<ast_sent> L = random_write_sents.get(U);
		L.addLast(s);
	}

	public final void add_random_write_sent(ast_sentblock sb, gm_symtab_entry sym, ast_sent s) {
		gm_gps_comm_unit U = new gm_gps_comm_unit(gm_gps_comm_t.GPS_COMM_RANDOM_WRITE, sb, sym);
		add_random_write_sent(U, s);
	}

	public final gm_gps_communication_size_info find_communication_size_info_nested(ast_foreach fe, int comm_type) {
		gm_gps_comm_unit U = new gm_gps_comm_unit(gm_gps_comm_t.GPS_COMM_NESTED, fe);
		return find_communication_size_info(U);
	}

	/*
	 * gm_gps_communication_symbol_info&
	 * gm_gps_beinfo::find_communication_symbol_info( ast_foreach* fe, int
	 * comm_type, gm_symtab_entry* sym) { gm_gps_comm_unit U(comm_type, fe);
	 * assert(comm_symbol_info.find(U) != comm_symbol_info.end());
	 * 
	 * std::list<gm_gps_communication_symbol_info>& sym_info =
	 * comm_symbol_info[U];
	 * std::list<gm_gps_communication_symbol_info>::iterator I;
	 * for(I=sym_info.begin(); I!= sym_info.end();I++) {
	 * gm_gps_communication_symbol_info& S = *I; if (S.symbol == sym) return S;
	 * // already added }
	 * 
	 * assert(false); }
	 */

	public final gm_gps_communication_size_info find_communication_size_info(gm_gps_comm_unit U) {
		assert comm_size_info.containsKey(U);
		return comm_size_info.get(U);
	}

	// get maximum communication size over all comm-loops
	public final void compute_max_communication_size() {
		for (gm_gps_comm_unit value : comm_loops) {
			ast_foreach fe = value.fe;
			gm_gps_communication_size_info size_info = comm_size_info.get(value);

			if (max_comm_size.num_int < size_info.num_int) {
				max_comm_size.num_int = size_info.num_int;
			}
			;
			if (max_comm_size.num_bool < size_info.num_bool) {
				max_comm_size.num_bool = size_info.num_bool;
			}
			;
			if (max_comm_size.num_long < size_info.num_long) {
				max_comm_size.num_long = size_info.num_long;
			}
			;
			if (max_comm_size.num_float < size_info.num_float) {
				max_comm_size.num_float = size_info.num_float;
			}
			;
			if (max_comm_size.num_double < size_info.num_double) {
				max_comm_size.num_double = size_info.num_double;
			}
			;
		}

		assert comm_loops.size() <= 255;
	}

	public final gm_gps_communication_size_info get_max_communication_size() {
		return max_comm_size;
	}

	public final void set_rand_used(boolean b) {
		rand_used = b;
	}

	public final boolean is_rand_used() {
		return rand_used;
	}

	public final void set_total_node_property_size(int s) {
		total_node_prop_size = s;
	}

	public final void set_total_edge_property_size(int s) {
		total_edge_prop_size = s;
	}

	public final int get_total_node_property_size() {
		return total_node_prop_size;
	}

	public final int get_total_edge_property_size() {
		return total_edge_prop_size;
	}

	public final int issue_comm_id() {
		return comm_id++;
	}

	public final int issue_basicblock_id() {
		return basicblock_id++;
	}

	public final HashSet<gm_symtab_entry> get_scalar_symbols() {
		return scalar;
	}

	public final HashSet<gm_symtab_entry> get_node_prop_symbols() {
		return node_prop;
	}

	public final HashSet<gm_symtab_entry> get_edge_prop_symbols() {
		return edge_prop;
	}

	public final gm_gps_congruent_msg_class add_congruent_message_class(gm_gps_communication_size_info sz, gm_gps_basic_block bb) {
		gm_gps_congruent_msg_class C = new gm_gps_congruent_msg_class();
		C.id = congruent_msg.size();
		C.sz_info = sz;
		C.add_receiving_basic_block(bb);

		congruent_msg.addLast(C);

		return C;
	}

	public final LinkedList<gm_gps_congruent_msg_class> get_congruent_message_classes() {
		return congruent_msg;
	}

	public final boolean is_single_message() {
		return congruent_msg.size() < 2;
	}

	public final boolean is_empty_message() {
		return congruent_msg.size() == 0;
	}

	private ast_procdef body;

	private gm_gps_basic_block bb_entry; // entry for the procedure basic blocks
											// (DAG)
	private LinkedList<gm_gps_basic_block> bb_blocks = new LinkedList<gm_gps_basic_block>(); // same
																								// as
																								// above
																								// DAG,
																								// but
																								// flattened
																								// as
																								// list
	// (created by gm_gps_bb_find_reachable.cc)

	private HashSet<gm_symtab_entry> scalar = new HashSet<gm_symtab_entry>(); // list
																				// of
																				// persistent
																				// master
																				// symbols
	private HashSet<gm_symtab_entry> node_prop = new HashSet<gm_symtab_entry>(); // list
																					// of
																					// persistent
																					// property
																					// symbols
	private HashSet<gm_symtab_entry> edge_prop = new HashSet<gm_symtab_entry>(); // list
																					// of
																					// persistent
																					// property
																					// symbols
	private int total_node_prop_size;
	private int total_edge_prop_size;

	// map of inner loops (possible communications) and
	// symbols used for the communication in the loop.
	// private HashMap<gm_gps_comm_unit,
	// LinkedList<gm_gps_communication_symbol_info>, gm_gps_comm_unit>
	// comm_symbol_info = new HashMap<gm_gps_comm_unit,
	// LinkedList<gm_gps_communication_symbol_info>, gm_gps_comm_unit>();
	private HashMap<gm_gps_comm_unit, LinkedList<gm_gps_communication_symbol_info>> comm_symbol_info = new HashMap<gm_gps_comm_unit, LinkedList<gm_gps_communication_symbol_info>>();
	// private HashMap<gm_gps_comm_unit, gm_gps_communication_size_info,
	// gm_gps_comm_unit> comm_size_info = new HashMap<gm_gps_comm_unit,
	// gm_gps_communication_size_info, gm_gps_comm_unit>();
	private HashMap<gm_gps_comm_unit, gm_gps_communication_size_info> comm_size_info = new HashMap<gm_gps_comm_unit, gm_gps_communication_size_info>();

	private gm_gps_communication_size_info max_comm_size = new gm_gps_communication_size_info();

	// set of communications
	// private HashSet<gm_gps_comm_unit, gm_gps_comm_unit> comm_loops = new
	// HashSet<gm_gps_comm_unit, gm_gps_comm_unit>();
	private HashSet<gm_gps_comm_unit> comm_loops = new HashSet<gm_gps_comm_unit>();

	// private HashMap<gm_gps_comm_unit, LinkedList<ast_sent>, gm_gps_comm_unit>
	// random_write_sents = new HashMap<gm_gps_comm_unit, LinkedList<ast_sent>,
	// gm_gps_comm_unit>();
	private HashMap<gm_gps_comm_unit, LinkedList<ast_sent>> random_write_sents = new HashMap<gm_gps_comm_unit, LinkedList<ast_sent>>();

	// congruent message class information
	private LinkedList<gm_gps_congruent_msg_class> congruent_msg = new LinkedList<gm_gps_congruent_msg_class>();

	private int comm_id;
	private int basicblock_id;
	private boolean rand_used;

}
// C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
// /#define GM_COMPILE_STEP(CLASS, DESC) class CLASS : public gm_compile_step {
// private: CLASS() {set_description(DESC);}public: virtual void
// process(ast_procdef*p); virtual gm_compile_step* get_instance(){return new
// CLASS();} static gm_compile_step* get_factory(){return new CLASS();} };
// C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
// /#define GM_COMPILE_STEP_FACTORY(CLASS) CLASS::get_factory()

