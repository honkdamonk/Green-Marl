package inc;

public abstract class gm_graph_library {
	
	protected gm_code_writer Body;

	public abstract boolean do_local_optimize();

	public final void set_code_writer(gm_code_writer w) {
		Body = w;
	}

	public final gm_code_writer get_code_writer() {
		return Body;
	}

}