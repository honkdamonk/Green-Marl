//--------------------------------------------------------------------------
//--------------------------------------------------------------------------
// temporary iterator symbol
// (e.g. G.A = G.B)   ==> [G.B]
// (e.g. X = sum(t:G.Nodes)(t.x == 0){t.A} ==> [t.A]
public class range_cond_t
{
	public range_cond_t(int r, boolean b)
	{
		this.range_type = r;
		this.is_always = b;
	}
	public range_cond_t()
	{
		this.range_type = 0;
		this.is_always = false;
	}
	public int range_type;
	public boolean is_always;
}