package opt;

import frontend.gm_symtab_entry;

public class triple_t
{
	public gm_symtab_entry bound;
	public gm_symtab_entry target;
	public int is_rev_bfs;

	/* Eclipse generated hashCode() */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bound == null) ? 0 : bound.hashCode());
		result = prime * result + is_rev_bfs;
		result = prime * result + ((target == null) ? 0 : target.hashCode());
		return result;
	}

	/* Eclipse generated equals() */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		triple_t other = (triple_t) obj;
		if (bound == null) {
			if (other.bound != null)
				return false;
		} else if (!bound.equals(other.bound))
			return false;
		if (is_rev_bfs != other.is_rev_bfs)
			return false;
		if (target == null) {
			if (other.target != null)
				return false;
		} else if (!target.equals(other.target))
			return false;
		return true;
	}
	

}