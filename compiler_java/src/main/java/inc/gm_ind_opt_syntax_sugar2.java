package inc;

import ast.ast_procdef;
import opt.Replace_PropertyItarator_With_NodeIterator;
import opt.ss2_reduce_op;

public class gm_ind_opt_syntax_sugar2 extends gm_compile_step
{
	private gm_ind_opt_syntax_sugar2()
	{
		set_description("Regularize syntax");
	}
	public void process(ast_procdef p)
	{
		// 2. ReduceOP --> Reduce Assign
		ss2_reduce_op A = new ss2_reduce_op();
		p.traverse_pre(A);
		A.post_process(); // process
    
		// Should re-do rw-analysis
		/*    gm_redo_rw_analysis(p->get_body()); */
    
		Replace_PropertyItarator_With_NodeIterator B = new Replace_PropertyItarator_With_NodeIterator();
		p.traverse_pre(B);
	}
	@Override
	public gm_compile_step get_instance()
	{
		return new gm_ind_opt_syntax_sugar2();
	}
	public static gm_compile_step get_factory()
	{
		return new gm_ind_opt_syntax_sugar2();
	}
}