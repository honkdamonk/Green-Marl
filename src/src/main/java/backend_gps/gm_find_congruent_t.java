package backend_gps;

import inc.gps_apply_bb;

//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define TO_STR(X) #X
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define DEF_STRING(X) static const char *X = "X"
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP(CLASS, DESC) class CLASS : public gm_compile_step { private: CLASS() {set_description(DESC);}public: virtual void process(ast_procdef*p); virtual gm_compile_step* get_instance(){return new CLASS();} static gm_compile_step* get_factory(){return new CLASS();} };
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP_FACTORY(CLASS) CLASS::get_factory()
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define AUX_INFO(X,Y) "X"":""Y"
///#define GM_BLTIN_MUTATE_GROW 1
///#define GM_BLTIN_MUTATE_SHRINK 2
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_BLTIN_FLAG_TRUE true

//------------------------------------------------
// Find congruent message classes
//  - Iterate over basic blocks, check all messaging loops
//  - And figure out all the congurent instances.
//  
//  - Two messages are congruent iff
//     * they have same number of elements
//     * they belong to different basic block
//------------------------------------------------
public class gm_find_congruent_t extends gps_apply_bb
{
	public gm_find_congruent_t(gm_gps_beinfo beinfo)
	{
		this.BEINFO = beinfo;
		this.CLIST = new java.util.LinkedList<gm_gps_congruent_msg_class>(beinfo.get_congruent_message_classes());
		// do nothing 
	}

	@Override
	public void apply(gm_gps_basic_block b)
	{
		// iterate over messages and find congruent class
		// add one if not-exist
		java.util.LinkedList<gm_gps_comm_unit> LOOPS = b.get_receivers();
		for (gm_gps_comm_unit unit : LOOPS)
		{
			find_congruent_class_per_loop(unit, b);
		}

		// special case for prepare
		if (b.get_type() == gm_gps_bbtype_t.GM_GPS_BBTYPE_PREPARE1)
		{
			gm_gps_comm_unit U = new gm_gps_comm_unit(gm_gps_comm_t.GPS_COMM_INIT, null);
			find_congruent_class_per_loop(U, b);
		}

	}

	public final void find_congruent_class_per_loop(gm_gps_comm_unit U, gm_gps_basic_block b)
	{
		gm_gps_communication_size_info INFO = BEINFO.find_communication_size_info(U);

		gm_gps_congruent_msg_class s = find_congurent_class(INFO, b);
		if (s == null)
		{
			s = BEINFO.add_congruent_message_class(INFO, b);
		}
		else
		{
			s.add_receiving_basic_block(b);
		}
		INFO.msg_class = s;
	}

	public final gm_gps_congruent_msg_class find_congurent_class(gm_gps_communication_size_info INFO, gm_gps_basic_block b)
	{
		for (gm_gps_congruent_msg_class CLASS : CLIST)
		{
			if (!CLASS.sz_info.is_equivalent(INFO))
				continue;
			if (CLASS.find_basic_block_in_receiving_list(b))
				continue;
			return CLASS;
		}
		return null;
	}

	private gm_gps_beinfo BEINFO;
	private java.util.LinkedList<gm_gps_congruent_msg_class> CLIST;
}