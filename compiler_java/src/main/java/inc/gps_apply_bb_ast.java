package inc;

import static backend_gps.gm_gps_bbtype_t.GM_GPS_BBTYPE_BEGIN_VERTEX;
import static backend_gps.gm_gps_bbtype_t.GM_GPS_BBTYPE_IF_COND;
import static backend_gps.gm_gps_bbtype_t.GM_GPS_BBTYPE_MERGED_IF;
import static backend_gps.gm_gps_bbtype_t.GM_GPS_BBTYPE_MERGED_TAIL;
import static backend_gps.gm_gps_bbtype_t.GM_GPS_BBTYPE_PREPARE1;
import static backend_gps.gm_gps_bbtype_t.GM_GPS_BBTYPE_PREPARE2;
import static backend_gps.gm_gps_bbtype_t.GM_GPS_BBTYPE_SEQ;
import static backend_gps.gm_gps_bbtype_t.GM_GPS_BBTYPE_WHILE_COND;
import static backend_gps.gm_gps_comm_t.GPS_COMM_NESTED;
import static backend_gps.gm_gps_comm_t.GPS_COMM_RANDOM_WRITE;

import java.util.Iterator;
import java.util.LinkedList;

import ast.AST_NODE_TYPE;
import ast.ast_expr;
import ast.ast_foreach;
import ast.ast_if;
import ast.ast_sent;
import ast.ast_while;
import backend_gps.gm_gps_basic_block;
import backend_gps.gm_gps_bbtype_t;
import backend_gps.gm_gps_beinfo;
import backend_gps.gm_gps_comm_t;
import backend_gps.gm_gps_comm_unit;

import common.gm_main;

public class gps_apply_bb_ast extends gps_apply_bb {

	// set by traverse engine
	protected gm_gps_basic_block _curr;
	protected boolean _under_receiver;
	protected boolean _is_post;
	protected boolean _is_pre;
	protected boolean _check_receiver;
	protected gm_gps_comm_t _receiver_type; // GPS_COMM_NESTED, COMM_RAND_WRITE

	public gps_apply_bb_ast() {
		_curr = null;
		_under_receiver = false;
		_is_post = false;
		_is_pre = true;
		_receiver_type = GPS_COMM_NESTED;
		_check_receiver = true;
	}

	public void apply(gm_gps_basic_block b) {
		_curr = b;
		gm_gps_bbtype_t type = _curr.get_type();
		// printf("visiting :%d\n", _curr->get_id());
		if (type == GM_GPS_BBTYPE_SEQ) {
			// traverse sentence block and apply this
			_curr.prepare_iter();
			ast_sent s;
			s = _curr.get_next();
			while (s != null) {
				s.traverse(this, is_post(), is_pre());
				s = _curr.get_next();
			}
		} else if (type == GM_GPS_BBTYPE_BEGIN_VERTEX) {

			// traverse receiver
			if (_curr.has_receiver() && is_check_receiver()) {
				LinkedList<gm_gps_comm_unit> R = _curr.get_receivers();
				set_under_receiver_traverse(true);
				for (gm_gps_comm_unit U : R) {
					set_receiver_type(U.get_type());
					if (U.get_type() == GPS_COMM_NESTED) {
						ast_foreach fe = U.fe;
						fe.traverse(this, is_post(), is_pre());
					} else if (U.get_type() == GPS_COMM_RANDOM_WRITE) {
						gm_gps_beinfo info = (gm_gps_beinfo) gm_main.FE.get_current_backend_info();
						LinkedList<ast_sent> LL = info.get_random_write_sents(U);
						Iterator<ast_sent> II;
						for (II = LL.iterator(); II.hasNext();) {
							ast_sent s = II.next();
							s.traverse(this, is_post(), is_pre());
						}
					} else {
						assert false;
					}
				}
				set_under_receiver_traverse(false);
			}

			// traverse body
			if (_curr.get_num_sents() == 0)
				return;

			LinkedList<ast_sent> sents = _curr.get_sents();
			for (ast_sent s : sents) {
				assert s.get_nodetype() == AST_NODE_TYPE.AST_FOREACH;
				ast_foreach fe = (ast_foreach) s;
				fe.traverse(this, is_post(), is_pre());
			}
		} else if (type == GM_GPS_BBTYPE_IF_COND) {
			assert _curr.get_num_sents() == 1;
			// traverse cond expr
			ast_sent s = _curr.get_1st_sent();
			assert s.get_nodetype() == AST_NODE_TYPE.AST_IF;
			ast_if i = (ast_if) s;
			ast_expr c = i.get_cond();

			c.traverse(this, is_post(), is_pre());
		} else if (type == GM_GPS_BBTYPE_WHILE_COND) {
			assert _curr.get_num_sents() == 1;

			// traverse cond expr
			ast_sent s = _curr.get_1st_sent();
			assert s.get_nodetype() == AST_NODE_TYPE.AST_WHILE;
			ast_while w = (ast_while) s;
			ast_expr c = w.get_cond();

			c.traverse(this, is_post(), is_pre());
		} else if (type == GM_GPS_BBTYPE_PREPARE1) {
			// nothing
		} else if (type == GM_GPS_BBTYPE_PREPARE2) {
			// nothing
		} else if (type == GM_GPS_BBTYPE_MERGED_TAIL) {
			// nothing
		} else if (type == GM_GPS_BBTYPE_MERGED_IF) {
			// nothing
		} else {
			assert false;
		}
	}

	public final gm_gps_basic_block get_curr_BB() {
		return _curr;
	}

	public final void set_is_post(boolean b) {
		_is_post = b;
	}

	public final void set_is_pre(boolean b) {
		_is_pre = b;
	}

	public final boolean is_post() {
		return _is_post;
	}

	public final boolean is_pre() {
		return _is_pre;
	}

	public final boolean is_check_receiver() {
		return _check_receiver;
	}

	public final void set_check_receiver(boolean b) {
		_check_receiver = b;
	}

	protected final boolean is_under_receiver_traverse() {
		return _under_receiver;
	}

	protected final void set_under_receiver_traverse(boolean b) {
		_under_receiver = b;
	}

	protected final gm_gps_comm_t get_receiver_type() {
		return _receiver_type;
	}

	protected final void set_receiver_type(gm_gps_comm_t i) {
		_receiver_type = i;
	}

}