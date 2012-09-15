package backend_gps;

import inc.gm_compile_step;
import ast.ast_procdef;

public class gm_gps_new_check_random_write extends gm_compile_step {
	
	private gm_gps_new_check_random_write() {
		set_description("Checking if there exists random writing");
	}

	@Override
	public void process(ast_procdef proc) {
		gps_check_random_access_t2 T = new gps_check_random_access_t2();
		proc.traverse_pre(T);

		set_okay(!T.is_error());
	}

	@Override
	public gm_compile_step get_instance() {
		return new gm_gps_new_check_random_write();
	}

	public static gm_compile_step get_factory() {
		return new gm_gps_new_check_random_write();
	}
}