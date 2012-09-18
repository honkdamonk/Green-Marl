package common;

import inc.gm_compile_step;

import java.util.LinkedList;

import ast.ast_procdef;

public class gm_apply_compiler_stage {
	
	public static boolean apply(LinkedList<gm_compile_step> LIST) {
		boolean is_okay = true;
		int i = 1;

		// for each compilation step
		for (gm_compile_step step : LIST) {
			gm_main.gm_begin_minor_compiler_stage(i, step.get_description());
			i++;

			is_okay = gm_apply_compiler_stage.gm_apply_all_proc(step) && is_okay;

			gm_main.gm_end_minor_compiler_stage();
			if (!is_okay)
				break;
		}
		return is_okay;
	}

	public static boolean gm_apply_all_proc(gm_compile_step org) {
		boolean is_okay = true;

		// apply to every procedure
		gm_main.FE.prepare_proc_iteration();
		ast_procdef p;
		while ((p = gm_main.FE.get_next_proc()) != null) {
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