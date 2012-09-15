package frontend;

import static ast.ast_node_type.AST_PROCDEF;
import static ast.ast_node_type.AST_SENTBLOCK;
import ast.ast_node;
import ast.ast_sent;
import ast.gm_rwinfo_list;
import ast.gm_rwinfo_map;

import common.gm_apply;

//----------------------------------------------------------------------
// routines for degugging
//----------------------------------------------------------------------
// print rw info per sentence
// (need to make it sentence block, such as then-clause, for best print-out)
public class gm_print_rw_info extends gm_apply {

	private int _tab = 0;

	private void print_tab(int j) {
		for (int i = 0; i <= j; i++)
			System.out.print("..");
	}

	private void print_set(String c, gm_rwinfo_map m) {
		print_tab(_tab);
		System.out.printf(" <%s>", c);
		int cnt2 = 0;
		for (gm_symtab_entry e : m.keySet()) {
			gm_rwinfo_list l = m.get(e);
			assert e != null;
			assert l != null;
			if ((cnt2 % 8) == 7) {
				System.out.print("\n");
				print_tab(_tab + 1);
			}
			if (cnt2 != 0) // (it != m.iterator())
				System.out.print(",");

			if (e.getType().is_property())
				System.out.printf("{%s(%s):", e.getId().get_orgname(), e.getType().get_target_graph_id().get_orgname());
			else
				System.out.printf("{%s:", e.getId().get_orgname());

			boolean first = true;
			for (gm_rwinfo info : l) {
				if (first)
					first = false;
				else
					System.out.print(",");
				info.print();
			}

			System.out.print("}");
			cnt2++;
		}
		System.out.print("\n");
	}

	@Override
	public boolean apply(ast_sent s) {
		if (s.get_nodetype() == AST_SENTBLOCK) {
			_tab--;
		}

		if ((s.get_parent() != null) && ((s.get_parent().get_nodetype() != AST_SENTBLOCK) && (s.get_parent().get_nodetype() != AST_PROCDEF))) {
			_tab++;
		}
		gm_rwinfo_sets sets = gm_rw_analysis.get_rwinfo_sets(s);
		gm_rwinfo_map R = sets.read_set;
		gm_rwinfo_map W = sets.write_set;
		gm_rwinfo_map D = sets.reduce_set;

		print_tab(_tab);
		System.out.printf("[%s]\n", s.get_nodetype().get_nodetype_string());
		if (R.size() > 0)
			print_set("R", R);
		if (W.size() > 0)
			print_set("W", W);
		if (D.size() > 0)
			print_set("D", D);

		if (s.get_nodetype() == AST_SENTBLOCK) {
			_tab++;
		}
		if ((s.get_parent() != null) && ((s.get_parent().get_nodetype() != AST_SENTBLOCK) && (s.get_parent().get_nodetype() != AST_PROCDEF))) {
			_tab--;
		}
		return true;
	}

	@Override
	public void begin_context(ast_node n) {
		_tab++;
	}

	@Override
	public void end_context(ast_node n) {
		_tab--;
	}
}