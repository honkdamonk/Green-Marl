package backend_gps;

import ast.ast_id;

public class GlobalMembersGps_syminfo
{

	// symbol usage info
	public static String GPS_TAG_BB_USAGE = "GPS_TAG_BB";

	public static gps_syminfo gps_get_global_syminfo(ast_id i)
	{
		return (gps_syminfo) i.getSymInfo().find_info(GPS_TAG_BB_USAGE);
	}
}