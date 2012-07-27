package backend_gps;

import static backend_gps.gps_gps_sentence_t.GPS_TYPE_BEGIN_VERTEX;
import static backend_gps.gps_gps_sentence_t.GPS_TYPE_CANBE_VERTEX;
import static backend_gps.gps_gps_sentence_t.GPS_TYPE_IN_VERTEX;
import static backend_gps.gps_gps_sentence_t.GPS_TYPE_SEQ;

import java.util.HashMap;
import java.util.LinkedList;

import ast.AST_NODE_TYPE;
import ast.ast_if;
import ast.ast_sent;
import ast.ast_sentblock;
import ast.ast_while;

import common.gm_apply;

//TOOD new name?

public class gm_stage_create_pre_process_t extends gm_apply {
	
	private boolean master_context;
	private LinkedList<Boolean> master_context_stack = new LinkedList<Boolean>();
	private HashMap<ast_sent, gps_gps_sentence_t> s_mark;

	public gm_stage_create_pre_process_t(HashMap<ast_sent, gps_gps_sentence_t> mk) {
		s_mark = mk;
		master_context = true;
	}

	// pre-apply
	@Override
	public boolean apply(ast_sent s) {
		if (s.get_nodetype() == AST_NODE_TYPE.AST_FOREACH) {
			master_context_stack.addFirst(master_context);
			master_context = false;
		}
		return true;
	}

	@Override
	public boolean apply2(ast_sent s) {
		if (s.get_nodetype() == AST_NODE_TYPE.AST_FOREACH) {
			master_context = master_context_stack.getFirst();
			master_context_stack.removeFirst();
		}

		if (master_context)
			master_mode_post(s);
		else
			vertex_mode_post(s);
		return true;
	}

	// C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced
	// in-line:
	// /#define ASSERT_MARKED(s) (assert(s_mark->find(s) != s_mark->end()))
	// C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced
	// in-line:
	// /#define MARKED_AS_SEQ(s) (s_mark->find(s)->second == GPS_TYPE_SEQ)
	// C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced
	// in-line:
	// /#define MARKED_AS_VBEGIN(s) (s_mark->find(s)->second ==
	// GPS_TYPE_BEGIN_VERTEX)
	// C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced
	// in-line:
	// /#define MARKED_AS_CANBE(s) (s_mark->find(s)->second ==
	// GPS_TYPE_CANBE_VERTEX)
	// C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced
	// in-line:
	// /#define MARK_SEQ(s, b) ((*s_mark)[s] = (b) ? GPS_TYPE_SEQ :
	// GPS_TYPE_CANBE_VERTEX)

	public void master_mode_post(ast_sent s) {

		if (s.get_nodetype() == AST_NODE_TYPE.AST_FOREACH) {
			s_mark.put(s, GPS_TYPE_BEGIN_VERTEX);
		} else if (s.get_nodetype() == AST_NODE_TYPE.AST_IF) {
			ast_if i = (ast_if) s;
			ast_sent thenp = i.get_then();
			assert (s_mark.containsKey(thenp));
			ast_sent elsep = i.get_else();
			if (elsep != null) {
				assert (s_mark.containsKey(elsep));
			}
			boolean seq1 = (s_mark.get(thenp) == GPS_TYPE_SEQ);
			boolean seq2 = (elsep == null) ? true : (s_mark.get(elsep) == GPS_TYPE_SEQ);
			boolean seq = seq1 && seq2;

			s_mark.put(s, (seq) ? GPS_TYPE_SEQ : GPS_TYPE_CANBE_VERTEX);
		} else if (s.get_nodetype() == AST_NODE_TYPE.AST_WHILE) {
			ast_while w = (ast_while) s;
			ast_sent body = w.get_body();
			assert (s_mark.containsKey(body));
			boolean seq = (s_mark.get(body) == GPS_TYPE_SEQ);

			s_mark.put(s, (seq) ? GPS_TYPE_SEQ : GPS_TYPE_CANBE_VERTEX);
		} else if (s.get_nodetype() == AST_NODE_TYPE.AST_SENTBLOCK) {
			// seq if every body sentence is sequential
			LinkedList<ast_sent> L = ((ast_sentblock) s).get_sents();
			boolean seq = true;
			for (ast_sent sent : L) {
				assert (s_mark.containsKey(sent));
				seq = (s_mark.get(sent) == GPS_TYPE_SEQ) && seq;
			}
			s_mark.put(s, (seq) ? GPS_TYPE_SEQ : GPS_TYPE_CANBE_VERTEX);
		} else {
			s_mark.put(s, GPS_TYPE_SEQ);
		}
	}

	public void vertex_mode_post(ast_sent s) {
		s_mark.put(s, GPS_TYPE_IN_VERTEX);
	}

}