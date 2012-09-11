package opt;

import frontend.gm_symtab_entry;

public class triple_t implements Comparable<triple_t>
{
	public gm_symtab_entry bound;
	public gm_symtab_entry target;
	public int is_rev_bfs;
	
	@Override
	public boolean equals(Object rhs) {
		return compareTo((triple_t) rhs) == 0;
	}
	
	@Override
	public int compareTo(triple_t rhs) {
		triple_t lhs = this;
		if (lhs.bound != rhs.bound)
			return lhs.bound.hashCode() < rhs.bound.hashCode() ? -1 : +1;

		if (lhs.target != rhs.target)
			return lhs.target.hashCode() < rhs.target.hashCode() ? -1 : +1;

		if (lhs.is_rev_bfs != rhs.is_rev_bfs)
			return lhs.is_rev_bfs < rhs.is_rev_bfs ? -1 : +1;

		else
			return 0;
	}
}