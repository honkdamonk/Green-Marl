package backend_gps;


//C++ TO JAVA CONVERTER NOTE: Java has no need of forward class declarations:
//struct gm_gps_congruent_msg_class;
public class gm_gps_communication_size_info
{
	public gm_gps_communication_size_info()
	{
		id = 0;
		num_int = num_bool = num_long = num_float = num_double = 0;
		msg_class = null;
	}
	public int id;
	public int num_int;
	public int num_bool;
	public int num_long;
	public int num_float;
	public int num_double;
	public gm_gps_congruent_msg_class msg_class;

	public final boolean is_equivalent(gm_gps_communication_size_info s)
	{
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