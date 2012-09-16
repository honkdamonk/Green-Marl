package backend_gps;

import ast.ast_foreach;
import ast.ast_sentblock;
import frontend.gm_symtab_entry;

public class gm_gps_comm_unit {
	
	public gm_gps_comm type_of_comm = null; // INIT, NESTED, RANDOM_WRITE
	public ast_foreach fe = null; // for NESTED communication
	public ast_sentblock sb = null; // for RANDOM_WRITE communication
	public gm_symtab_entry sym = null; // for RANDOM_WRITE communication

	public gm_gps_comm_unit() {
		type_of_comm = gm_gps_comm.GPS_COMM_INIT;
	}

	public gm_gps_comm_unit(gm_gps_comm t, ast_foreach f) {
		type_of_comm = t;
		fe = f;
	}

	public gm_gps_comm_unit(gm_gps_comm t, ast_sentblock s, gm_symtab_entry m) {
		type_of_comm = t;
		sb = s;
		sym = m;
	}

	public final gm_gps_comm get_type() {
		return type_of_comm;
	}

	/* Eclipse generated hashCode() */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fe == null) ? 0 : fe.hashCode());
		result = prime * result + ((sb == null) ? 0 : sb.hashCode());
		result = prime * result + ((sym == null) ? 0 : sym.hashCode());
		result = prime * result + ((type_of_comm == null) ? 0 : type_of_comm.hashCode());
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
		gm_gps_comm_unit other = (gm_gps_comm_unit) obj;
		if (fe == null) {
			if (other.fe != null)
				return false;
		} else if (!fe.equals(other.fe))
			return false;
		if (sb == null) {
			if (other.sb != null)
				return false;
		} else if (!sb.equals(other.sb))
			return false;
		if (sym == null) {
			if (other.sym != null)
				return false;
		} else if (!sym.equals(other.sym))
			return false;
		if (type_of_comm != other.type_of_comm)
			return false;
		return true;
	}

}