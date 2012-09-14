package backend_gps;

public class gm_gps_communication_size_info {

	public int id = 0;
	public int num_int = 0;
	public int num_bool = 0;
	public int num_long = 0;
	public int num_float = 0;
	public int num_double = 0;
	public gm_gps_congruent_msg_class msg_class = null;

	public final boolean is_equivalent(gm_gps_communication_size_info s) {
		if (num_int != s.num_int)
			return false;
		if (num_bool != s.num_bool)
			return false;
		if (num_long != s.num_long)
			return false;
		if (num_float != s.num_float)
			return false;
		if (num_double != s.num_double)
			return false;
		return true;
	}

}