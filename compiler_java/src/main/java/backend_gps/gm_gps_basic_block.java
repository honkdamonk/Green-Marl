package backend_gps;

import ast.ast_extra_info;
import ast.ast_foreach;
import ast.ast_id;
import ast.ast_if;
import ast.ast_sent;
import ast.ast_sentblock;
import ast.ast_while;

import common.gm_main;
import common.gm_reproduce;

import frontend.gm_symtab_entry;

public class gm_gps_basic_block {
	public gm_gps_basic_block(int _id) {
		this(_id, gm_gps_bbtype_t.GM_GPS_BBTYPE_SEQ);
	}

	public gm_gps_basic_block(int _id, gm_gps_bbtype_t _type) // ,_has_sender(false)
	{
		this.id = _id;
		this.type = _type;
		this.after_vertex = false;
		for_info = ast_id.new_id("", 0, 0);
	}

	public void dispose() {
		for (gm_symtab_entry key : symbols.keySet()) {
			gps_syminfo s = symbols.get(key);
			if (s != null)
				s.dispose();
		}
	}

	public final void prepare_iter() {
		I = sents.iterator();
	}

	public final ast_sent get_next() {
		if (I.hasNext()) {
			ast_sent s = I.next();
			return s;
		} else
			return null;
	}

	public final java.util.LinkedList<ast_sent> get_sents() {
		return sents;
	}

	public final void add_sent(ast_sent s) {
		sents.addLast(s);
	}

	public final void add_sent_front(ast_sent s) {
		sents.addFirst(s);
	}

	public final int get_num_sents() {
		return sents.size();
	}

	public final ast_sent get_1st_sent() {
		return sents.getFirst();
	}

	public final int get_id() {
		return id;
	}

	public final gm_gps_bbtype_t get_type() {
		return type;
	}

	public final boolean is_after_vertex() {
		return after_vertex;
	}

	public final void set_type(gm_gps_bbtype_t t) {
		type = t;
	}

	public final void set_id(int i) {
		id = i;
	}

	public final void set_after_vertex(boolean b) {
		after_vertex = b;
	}

	public final int get_num_exits() {
		return exits.size();
	}

	public final gm_gps_basic_block get_nth_exit(int n) {
		return exits.get(n);
	}

	// -------------------------------
	// <exit convention>
	// if: then[0], else[1]
	// while: body[0], exit[1]
	// -------------------------------
	public final void add_exit(gm_gps_basic_block b) {
		add_exit(b, true);
	}

	public final void add_exit(gm_gps_basic_block b, boolean add_reverse) {
		assert b != this;
		exits.add(b);
		if (add_reverse) // add reverse link
			b.add_entry(this);
	}

	public final void remove_all_exits() {
		exits.clear();
	}

	public final void remove_all_entries() {
		entries.clear();
	}

	public final int get_num_entries() {
		return entries.size();
	}

	public final void add_entry(gm_gps_basic_block b) {
		assert b != this;
		entries.add(b);
	}

	public final void update_entry_from(gm_gps_basic_block old, gm_gps_basic_block new_one) {
		assert new_one != this;
		for (int i = 0; i < (int) entries.size(); i++) {
			if (entries.get(i) == old) {
				entries.set(i, new_one);
				return;
			}
		}
		assert false;
	}

	public final void update_exit_to(gm_gps_basic_block old, gm_gps_basic_block new_one) {
		assert new_one != this;
		for (int i = 0; i < (int) exits.size(); i++) {
			if (exits.get(i) == old) {
				exits.set(i, new_one);
				return;
			}
		}
		assert false;
	}

	public final gm_gps_basic_block get_nth_entry(int n) {
		return entries.get(n);
	}

	// for debug
	public void print() {
		System.out.print("[--------------------\n");
		System.out.printf("%d (%s):\n", id, (type == gm_gps_bbtype_t.GM_GPS_BBTYPE_SEQ) ? "SEQ" : (type == gm_gps_bbtype_t.GM_GPS_BBTYPE_IF_COND) ? "IF"
				: (type == gm_gps_bbtype_t.GM_GPS_BBTYPE_WHILE_COND) ? "WHILE" : (type == gm_gps_bbtype_t.GM_GPS_BBTYPE_BEGIN_VERTEX) ? "PAR" : "???");
		// printf("\tnum_entries:%d\n", entries.size());
		System.out.print("\t[ ");
		for (int i = 0; i < entries.size(); i++) {
			System.out.printf("%d ", entries.get(i).get_id());
		}
		System.out.print("]=>...\n");

		reproduce_sents();
		System.out.print("\t...=>[ ");
		System.out.print("\n");
		for (int i = 0; i < exits.size(); i++) {
			System.out.printf("%d ", exits.get(i).get_id());
		}
		System.out.print("]\n");

		System.out.print("--------------------]\n");
	}

	public void reproduce_sents() {
		reproduce_sents(true);
	}

	public void reproduce_sents(boolean reproduce_receiver) {
		if (type == gm_gps_bbtype_t.GM_GPS_BBTYPE_IF_COND) {
			prepare_iter();
			ast_sent s = get_next(); // should be only one sentence (if)

			ast_if i = (ast_if) s;
			i.get_cond().reproduce(1);

			gm_reproduce.gm_newline_reproduce();
			gm_reproduce.gm_flush_reproduce();
		} else if (type == gm_gps_bbtype_t.GM_GPS_BBTYPE_WHILE_COND) {
			prepare_iter();
			ast_sent s = get_next(); // should be only one sentence (if)

			ast_while i = (ast_while) s;
			i.get_cond().reproduce(1);

			gm_reproduce.gm_newline_reproduce();
			gm_reproduce.gm_flush_reproduce();
		} else if ((type == gm_gps_bbtype_t.GM_GPS_BBTYPE_BEGIN_VERTEX) || (type == gm_gps_bbtype_t.GM_GPS_BBTYPE_SEQ)) {

			if ((type == gm_gps_bbtype_t.GM_GPS_BBTYPE_BEGIN_VERTEX) && (has_receiver()) && reproduce_receiver) {
				gm_gps_beinfo info = (gm_gps_beinfo) gm_main.FE.get_current_backend_info();
				java.util.LinkedList<gm_gps_comm_unit> L = get_receivers();
				for (gm_gps_comm_unit U : L) {
					if (U.type_of_comm == gm_gps_comm_t.GPS_COMM_NESTED) {
						gm_reproduce.gm_push_reproduce("//Receive Nested Loop");
						gm_reproduce.gm_newline_reproduce();
						ast_foreach fe = U.fe;
						fe.reproduce(0);
					} else if (U.type_of_comm == gm_gps_comm_t.GPS_COMM_RANDOM_WRITE) {
						gm_reproduce.gm_push_reproduce("//Receive Random Write Sent");
						gm_reproduce.gm_newline_reproduce();
						java.util.LinkedList<ast_sent> LL = info.get_random_write_sents(U);
						for (ast_sent s : LL) {
							s.reproduce(0);
						}
					} else {
						assert false;
					}
				}
			}

			prepare_iter();
			ast_sent s = get_next();
			while (s != null) {
				s.reproduce(0);
				s = get_next();
				if ((type == gm_gps_bbtype_t.GM_GPS_BBTYPE_BEGIN_VERTEX) && (s != null))
					gm_reproduce.gm_newline_reproduce();
			}
			gm_reproduce.gm_flush_reproduce();
		} else if ((type == gm_gps_bbtype_t.GM_GPS_BBTYPE_PREPARE1) || (type == gm_gps_bbtype_t.GM_GPS_BBTYPE_PREPARE2)
				|| (type == gm_gps_bbtype_t.GM_GPS_BBTYPE_MERGED_TAIL) || (type == gm_gps_bbtype_t.GM_GPS_BBTYPE_MERGED_IF)) {
			// do nothing;
		} else {
			assert false;
		}
	}

	public final boolean is_vertex() {
		return (get_type() == gm_gps_bbtype_t.GM_GPS_BBTYPE_BEGIN_VERTEX);
	}

	// bool has_sender() {return _has_sender;}
	// void set_has_sender(bool b) {_has_sender = b;}
	public final boolean is_prepare() {
		return (get_type() == gm_gps_bbtype_t.GM_GPS_BBTYPE_PREPARE1) || (get_type() == gm_gps_bbtype_t.GM_GPS_BBTYPE_PREPARE2);
	}

	public final void add_nested_receiver(ast_foreach fe) {
		gm_gps_comm_unit U = new gm_gps_comm_unit(gm_gps_comm_t.GPS_COMM_NESTED, fe);
		add_receiver(U);
	}

	public final void add_random_write_receiver(ast_sentblock sb, gm_symtab_entry sym) {
		gm_gps_comm_unit U = new gm_gps_comm_unit(gm_gps_comm_t.GPS_COMM_RANDOM_WRITE, sb, sym);
		add_receiver(U);
	}

	public final void add_receiver(gm_gps_comm_unit u) {
		receivers.addLast(u);
	}

	public final java.util.LinkedList<gm_gps_comm_unit> get_receivers() {
		return receivers;
	}

	public final void clear_receivers() {
		receivers.clear();
	}

	public final boolean has_receiver() {
		return receivers.size() > 0;
	}

	private java.util.Iterator<ast_sent> I;
	private java.util.LinkedList<ast_sent> sents = new java.util.LinkedList<ast_sent>();

	// std::list<ast_foreach*> receivers;
	private java.util.LinkedList<gm_gps_comm_unit> receivers = new java.util.LinkedList<gm_gps_comm_unit>();

	private java.util.ArrayList<gm_gps_basic_block> exits = new java.util.ArrayList<gm_gps_basic_block>();
	private java.util.ArrayList<gm_gps_basic_block> entries = new java.util.ArrayList<gm_gps_basic_block>();
	private int id;
	private gm_gps_bbtype_t type;
	private boolean after_vertex;
	// bool _has_sender;

	// map of used symbols inside this BB
	private java.util.HashMap<gm_symtab_entry, gps_syminfo> symbols = new java.util.HashMap<gm_symtab_entry, gps_syminfo>();

	public final gps_syminfo find_symbol_info(gm_symtab_entry sym) {
		if (!symbols.containsKey(sym))
			return null;
		else
			return symbols.get(sym);
	}

	public final void add_symbol_info(gm_symtab_entry sym, gps_syminfo info) {
		symbols.put(sym, info);
	}

	public final java.util.HashMap<gm_symtab_entry, gps_syminfo> get_symbols() {
		return symbols;
	}

	private ast_id for_info; // to use info methods defined in

	public final boolean has_info(String id) {
		return for_info.has_info(id);
	}

	public final ast_extra_info find_info(String id) {
		return for_info.find_info(id);
	}

	public final boolean find_info_bool(String id) {
		return for_info.find_info_bool(id);
	}

	public final String find_info_string(String id) {
		return for_info.find_info_string(id);
	}

	public final float find_info_float(String id) {
		return for_info.find_info_float(id);
	}

	public final int find_info_int(String id) {
		return for_info.find_info_int(id);
	}

	public final Object find_info_ptr(String id) {
		return for_info.find_info_ptr(id);
	}

	public final Object find_info_ptr2(String id) {
		return for_info.find_info_ptr2(id);
	}

	public final void add_info(String id, ast_extra_info e) {
		for_info.add_info(id, e);
	}

	public final void add_info_int(String id, int i) {
		for_info.add_info_int(id, i);
	}

	public final void add_info_bool(String id, boolean b) {
		for_info.add_info_bool(id, b);
	}

	public final void add_info_ptr(String id, Object ptr1) {
		add_info_ptr(id, ptr1, null);
	}

	public final void add_info_ptr(String id, Object ptr1, Object ptr2) {
		for_info.add_info_ptr(id, ptr1, ptr2);
	}

	public final void add_info_float(String id, float f) {
		for_info.add_info_float(id, f);
	}

	public final void add_info_string(String id, String str) {
		for_info.add_info_string(id, str);
	}

	public final void remove_info(String id) {
		for_info.remove_info(id);
	}

	public final void remove_all_info() {
		for_info.remove_all_info();
	}

	public final void copy_info_from(gm_gps_basic_block bb) {
		for_info.copy_info_from(bb.for_info);
	}
}