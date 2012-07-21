import ast.ast_procdef;
import inc.gm_compile_step;

public class GlobalMembersGm_apply_compiler_stage
{
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define TO_STR(X) #X
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define DEF_STRING(X) static const char *X = "X"
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define GM_COMPILE_STEP(CLASS, DESC) class CLASS : public gm_compile_step { private: CLASS() {set_description(DESC);}public: virtual void process(ast_procdef*p); virtual gm_compile_step* get_instance(){return new CLASS();} static gm_compile_step* get_factory(){return new CLASS();} };
	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	///#define GM_COMPILE_STEP_FACTORY(CLASS) CLASS::get_factory()

	public static boolean gm_apply_compiler_stage(java.util.LinkedList<gm_compile_step> LIST)
	{
		boolean is_okay = true;
		java.util.Iterator<gm_compile_step> I;
		int i = 0;

		// for each compilation step
		for (I = LIST.iterator(); I.hasNext(); I++, i++)
		{
			gm_compile_step step = (I.next());
			GlobalMembersGm_main.gm_begin_minor_compiler_stage(i + 1, step.get_description());

			is_okay = GlobalMembersGm_apply_compiler_stage.gm_apply_all_proc(step) && is_okay;

			GlobalMembersGm_main.gm_end_minor_compiler_stage();
			if (!is_okay)
				break;
		}
		return is_okay;
	}

	public static boolean gm_apply_all_proc(gm_compile_step org)
	{
		boolean is_okay = true;

		// apply to every procedure
		GlobalMembersGm_main.FE.prepare_proc_iteration();
		ast_procdef p;
		while ((p = GlobalMembersGm_main.FE.get_next_proc()) != null)
		{
			gm_compile_step step = org.get_instance();

			step.process(p);
			boolean okay = step.is_okay();
			is_okay = is_okay && okay;

			if (step != null)
			step.dispose();
		}
		return is_okay;
	}
}