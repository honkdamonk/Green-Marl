package backend_gps;

import ast.ast_foreach;
import ast.ast_sentblock;
import frontend.gm_symtab_entry;

public class gm_gps_comm_unit {
	
  public gm_gps_comm_unit() {
    this.type_of_comm = gm_gps_comm_t.GPS_COMM_INIT;
    this.fe = null;
    this.sb = null;
    this.sym = null;
  }

  public gm_gps_comm_unit(gm_gps_comm_t t, ast_foreach f) {
    this.type_of_comm = t;
    this.fe = f;
    this.sb = null;
    this.sym = null;
  }

  public gm_gps_comm_unit(gm_gps_comm_t t, ast_sentblock s, gm_symtab_entry m) {
    this.type_of_comm = t;
    this.fe = null;
    this.sb = s;
    this.sym = m;
  }

  public final gm_gps_comm_t get_type() {
    return type_of_comm;
  }

  public gm_gps_comm_t type_of_comm; // INIT, NESTED, RANDOM_WRITE
  public ast_foreach fe; // for NESTED communication
  public ast_sentblock sb; // for RANDOM_WRITE communication
  public gm_symtab_entry sym; // for RANDOM_WRITE communication

  // for comparison (less)
  // C++ TO JAVA CONVERTER TODO TASK: The following operator cannot be converted
  // to Java:
  @Deprecated
  boolean operator /*()*/(gm_gps_comm_unit lhs, gm_gps_comm_unit rhs)
  {
//    if (lhs.type_of_comm != rhs.type_of_comm)
//      return (lhs.type_of_comm < rhs.type_of_comm);
//
//    else if (lhs.fe != rhs.fe)
//      return (lhs.fe < rhs.fe);
//
//    else if (lhs.sb != rhs.sb)
//      return (lhs.sb < rhs.sb);
//
//    else if (lhs.sym != rhs.sym)
//      return (lhs.sym < rhs.sym);

    return false; // if same, answer is false
  }

}