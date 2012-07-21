package frontend;

public class gm_type_rule
{
	public gm_type_rule(int O, int T1, int T2, int R, int C)
	{
		this.opclass = O;
		this.type1 = T1;
		this.type2 = T2;
		this.result_type = R;
		this.coercion_rule = C;
	}
	public int opclass;
	public int type1;
	public int type2;
	public int result_type;
	public int coercion_rule;
}