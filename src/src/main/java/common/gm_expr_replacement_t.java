import ast.ast_expr;

// implement following function 
public abstract class gm_expr_replacement_t
{
	public void dispose()
	{
	}
	public abstract boolean is_target(ast_expr e);
	public abstract ast_expr create_new_expr(ast_expr target, tangible.RefObject<Boolean> destory_target_after);
}