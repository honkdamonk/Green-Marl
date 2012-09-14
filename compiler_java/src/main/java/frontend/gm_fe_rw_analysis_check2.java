package frontend;

import inc.gm_compile_step;
import ast.ast_procdef;
import ast.ast_sent;

/**
* <b>[Read-Write conflict]</b><br>
* Foreach(t: G.Nodes) {<br>
* <dd>Forach(u: t.Nbrs) {<br>
* <dd><dd>t.C += u.A; // read A (random)<br>
* <dd>}<br>
* <dd>t.A = t.B + 3; // write A (linear) --> Error<br>
* }<br>
*
* Foreach(t: G.Nodes) {<br>
* <dd>Foreach(u: t.Nbrs)<br>
* <dd><dd>u.A <= t.A; // defer<br>
* <dd>t.A = t.B + 3; // write (okay)<br>
* }<br>
*
* BFS(t:G.nodes) {<br>
* <dd>t.B = Sum(u:G.UpNbrs) u.A; // read A (LEV +1)<br>
* <dd>t.A = t.B + 3; // write A (LEV) --> Okay<br>
* }<br>
* 
* <b>[Write-Write conflict]</b><br>
* Foreach(t: G.Nodes) {<br>
* <dd>Forach(u: t.Nbrs) {<br>
* <dd><dd>u.A += t.A + u.B; // write A (random) [-->Error]<br>
* <dd>}<br>
* }<br>
*
* BFS(t:G.nodes) {<br>
* <dd>t.B = Sum(u:G.UpNbrs) u.A; // read A (LEV +1)<br>
* <dd>t.A = t.B + 3; // write A (LEV) --> Okay<br>
* }<br>
*/
public class gm_fe_rw_analysis_check2 extends gm_compile_step {

	private gm_fe_rw_analysis_check2() {
		set_description("Check RW conflict errors");
	}

	@Override
	public void process(ast_procdef p) {
		set_okay(gm_check_parall_conflict_error(p.get_body()));
	}

	@Override
	public gm_compile_step get_instance() {
		return new gm_fe_rw_analysis_check2();
	}

	public static gm_compile_step get_factory() {
		return new gm_fe_rw_analysis_check2();
	}

	private static boolean gm_check_parall_conflict_error(ast_sent b) {
		gm_check_conf_t T = new gm_check_conf_t();
		b.traverse_post(T); // post apply
		return T.is_okay;
	}
}