package backend_gps;

import static backend_gps.GPSConstants.GPS_FLAG_WHILE_HEAD;
import static backend_gps.GPSConstants.GPS_FLAG_WHILE_TAIL;

import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import ast.ast_if;
import ast.ast_node_type;
import ast.ast_sent;
import ast.ast_while;

import common.gm_apply;

public class gm_gps_create_basic_block1_t extends gm_apply {

	private final Map<ast_sent, gm_gps_basic_block> prev_map = new TreeMap<ast_sent, gm_gps_basic_block>();
	private final Map<ast_sent, gm_gps_basic_block> next_map = new TreeMap<ast_sent, gm_gps_basic_block>();
	private Map<ast_sent, gps_gps_sentence> s_mark;

	private gm_gps_basic_block prev;
	private gm_gps_basic_block entry;
	private gm_gps_basic_block next;
	private gm_gps_basic_block exit;

	private final LinkedList<gm_gps_basic_block> prev_stack = new LinkedList<gm_gps_basic_block>();
	private final LinkedList<gm_gps_basic_block> next_stack = new LinkedList<gm_gps_basic_block>();

	private boolean already_added = false;
	private int added_depth = 0;
	private gm_gps_beinfo gen;

	public gm_gps_create_basic_block1_t(Map<ast_sent, gps_gps_sentence> s, gm_gps_beinfo _gen) {
		s_mark = s;
		gen = _gen;
		entry = prev = newBB(); // entry
		exit = next = newBB(); // exit
		entry.add_exit(exit);
	}

	@Override
	public boolean apply(ast_sent s) {
		if (already_added) {
			added_depth++;
			return true;
		}

		if (prev_map.containsKey(s)) {
			prev_stack.addFirst(prev);
			next_stack.addFirst(next);
			prev = prev_map.get(s);
			next = next_map.get(s);

		}

		/*
		 * if (s_mark->find(s) == s_mark->end()) { printf("[\n");
		 * s->reproduce(0); gm_flush_reproduce(); printf("]\n"); fflush(stdout);
		 * }
		 */
		assert (s_mark.containsKey(s));

		if ((s_mark.get(s) == gps_gps_sentence.GPS_TYPE_SEQ)) {
			// add this sentence to the basic block
			prev.add_sent(s);
			already_added = true;
			added_depth = 1;
			return true;
		}

		else if ((s_mark.get(s) == gps_gps_sentence.GPS_TYPE_BEGIN_VERTEX)) {
			gm_gps_basic_block bb1 = newBB(gm_gps_bbtype.GM_GPS_BBTYPE_BEGIN_VERTEX);
			gm_gps_basic_block bb2 = newBB();
			bb1.add_exit(bb2);
			insert_between_prev_next(bb1, bb2);
			bb2.set_after_vertex(true);

			// add this sentence to the basic block
			bb1.add_sent(s);
			already_added = true;
			added_depth = 1;
			return true;
		}

		else if ((s_mark.get(s) == gps_gps_sentence.GPS_TYPE_CANBE_VERTEX)) {

			if (s.get_nodetype() == ast_node_type.AST_SENTBLOCK) {
				// do nothing but recurse
			} else if (s.get_nodetype() == ast_node_type.AST_IF) {
				ast_if i = (ast_if) s;
				boolean has_else = (i.get_else() != null);

				// create new basic blocks
				gm_gps_basic_block cond = newBB(gm_gps_bbtype.GM_GPS_BBTYPE_IF_COND);
				gm_gps_basic_block fin = newBB();

				insert_between_prev_next(cond, fin);
				cond.add_sent(s);

				gm_gps_basic_block then_begin = newBB();
				gm_gps_basic_block then_end = newBB();
				cond.add_exit(then_begin);
				then_begin.add_exit(then_end);
				then_end.add_exit(fin);

				prev_map.put(i.get_then(), then_begin);
				next_map.put(i.get_then(), then_end);

				if (has_else) {
					gm_gps_basic_block else_begin = newBB();
					gm_gps_basic_block else_end = newBB();
					cond.add_exit(else_begin);
					else_begin.add_exit(else_end);
					else_end.add_exit(fin);

					prev_map.put(i.get_else(), else_begin);
					next_map.put(i.get_else(), else_end);
				} else {
					cond.add_exit(fin);
				}

				// prev/next after this sentence
				prev = fin;

			} else if (s.get_nodetype() == ast_node_type.AST_WHILE) {
				// create while blocks
				ast_while w = (ast_while) s;

				// create new basic blocks
				gm_gps_basic_block cond = newBB(gm_gps_bbtype.GM_GPS_BBTYPE_WHILE_COND);
				cond.add_sent(w);

				gm_gps_basic_block body_begin = newBB();
				gm_gps_basic_block body_end = newBB();
				gm_gps_basic_block dummy = newBB();
				gm_gps_basic_block head = newBB();

				body_begin.add_exit(body_end);
				if (w.is_do_while()) // do-while
				{
					head.add_exit(body_begin);
					cond.add_exit(head);
					cond.add_exit(dummy);
					// (prev) -> head -> begin ... end -> cond -> dummy ->
					// (next)
					// ^ |
					// +------------------------+
					body_end.add_exit(cond);
					insert_between_prev_next(head, dummy);

					// printf("head:%d, tail:%d\n", head->get_id(),
					// cond->get_id());
					cond.add_info_int(GPS_FLAG_WHILE_TAIL, head.get_id());
					head.add_info_int(GPS_FLAG_WHILE_HEAD, head.get_id());

				} // while
				else {
					// V-------------------------+
					// (prev) -> cond -> begin ... end -> head dummy -> (next)
					// | ^
					// +--------------------------------+
					cond.add_exit(body_begin);
					cond.add_exit(dummy);
					body_end.add_exit(head);
					head.add_exit(cond);
					insert_between_prev_next(cond, dummy);

					// printf("head:%d, tail:%d\n", cond->get_id(),
					// head->get_id());
					cond.add_info_int(GPS_FLAG_WHILE_HEAD, cond.get_id());
					head.add_info_int(GPS_FLAG_WHILE_TAIL, cond.get_id());
				}

				// begin/end for while sentence block
				prev_map.put(w.get_body(), body_begin);
				next_map.put(w.get_body(), body_end);

				// prev/next after this sentence
				prev = dummy;
			} else {
				assert false;
			}
		} else {
			assert false;
		}

		return true;
	}

	// post
	@Override
	public boolean apply2(ast_sent s) {
		if (already_added) {
			added_depth--;
			if (added_depth == 0)
				already_added = false;
		}

		if (prev_map.containsKey(s)) {
			prev = prev_stack.getFirst();
			prev_stack.removeFirst();
			next = next_stack.getFirst();
			next_stack.removeFirst();
		}

		return true;
	}

	private void insert_between_prev_next(gm_gps_basic_block bb1, gm_gps_basic_block bb2) {
		// ------------------------------
		// prev -> next
		// prev -> (bb1 ... bb2) -> next
		// bb2 becomes new prev
		// ------------------------------
		assert prev.get_num_exits() == 1;
		assert next.get_num_entries() == 1;

		prev.remove_all_exits();
		next.remove_all_entries();

		prev.add_exit(bb1);
		bb2.add_exit(next);

		prev = bb2;
	}

	private gm_gps_basic_block newBB() {
		return newBB(gm_gps_bbtype.GM_GPS_BBTYPE_SEQ);
	}

	private gm_gps_basic_block newBB(gm_gps_bbtype gmGpsBbtypeSeq) {
		assert gen != null;
		gm_gps_basic_block bb = new gm_gps_basic_block(gen.issue_basicblock_id(), gmGpsBbtypeSeq);
		return bb;
	}

	public final gm_gps_basic_block get_entry() {
		return entry;
	}

}