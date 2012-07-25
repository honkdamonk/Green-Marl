package backend_gps;

import common.GlobalMembersGm_main;

import frontend.gm_symtab_entry;

public class GlobalMembersGm_gps_sym_analyze_create_summary
{

	public static int comp_start_byte(java.util.HashSet<gm_symtab_entry> prop)
	{
		int byte_begin = 0;
		for (gm_symtab_entry sym : prop)
		{
			gps_syminfo syminfo = (gps_syminfo) sym.find_info(GlobalMembersGps_syminfo.GPS_TAG_BB_USAGE);

			int size = GlobalMembersGm_main.PREGEL_BE.get_lib().get_type_size(sym.getType().get_target_type());
			syminfo.set_start_byte(byte_begin);
			byte_begin += size;
		}
		return byte_begin;
	}
}