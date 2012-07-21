package backend_gps;

import ast.ast_expr;
import ast.ast_sentblock;
import frontend.gm_symtab_entry;
import inc.gm_code_writer;

import common.GlobalMembersGm_main;
import common.gm_apply;

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



public class gps_random_write_rhs_t extends gm_apply
{
	public gps_random_write_rhs_t(ast_sentblock _sb, gm_symtab_entry _sym, gm_gpslib l, gm_code_writer b)
	{
		this.sb = _sb;
		this.sym = _sym;
		this.lib = l;
		this.Body = new gm_code_writer(b);
		set_for_expr(true);
		U = new gm_gps_comm_unit(gm_gps_comm_t.GPS_COMM_RANDOM_WRITE, sb, sym);
		INFO = (gm_gps_beinfo) GlobalMembersGm_main.FE.get_current_backend_info();
	}

	public final boolean apply(ast_expr e)
	{
		if (!e.is_id() && !e.is_field())
			return true;

		gm_gps_communication_symbol_info SS;
		gm_symtab_entry tg;

		if (e.is_id())
		{
			tg = e.get_id().getSymInfo();
		}
		else
		{
			tg = e.get_field().get_second().getSymInfo();
			if (e.get_field().get_first().getSymInfo() == sym)
				return true;
		}

		SS = INFO.find_communication_symbol_info(U, tg);
		if (SS == null)
			return true;

		String mname = lib.get_random_write_message_name(sym);
		Body.push(mname); // should not delete this.
		Body.push(".");

		String fname = lib.get_message_field_var_name(SS.gm_type, SS.idx);
		Body.push(fname);
		fname = null;
		Body.push(" = ");

		if (e.is_id())
			lib.get_main().generate_rhs_id(e.get_id());
		else
			lib.generate_vertex_prop_access_rhs(e.get_field().get_second(), Body);

		Body.pushln(";");
		return true;
	}

	private ast_sentblock sb;
	private gm_symtab_entry sym;
	private gm_gps_comm_unit U;
	private gm_gps_beinfo INFO;
	private gm_gpslib lib;
	private gm_code_writer Body;

}