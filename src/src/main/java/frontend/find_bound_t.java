package frontend;

//-----------------------------------------------------------------
// Fix Bound symbols
//    (a) If bound is null  (defer-assign)
//            -> find closest binding for loop (canbe a seq-loop). Error, if none.
//
//    (b) If bound is null, or seq-loop ==> find appropriate bound
//           [The first parallel loop (strictly comes) after the scope where target/driver is defined
//           => if no such thing, reduction can be optimized
//
//-----------------------------------------------------------------

//-------------------------------------------------------------------------
// Empty bound
//   -> will simply choose highest parallel bound after property or scala definition
//-------------------------------------------------------------------------
//  
//  Foreach(s: G.Node)
//     s.A += ..           -> @ s
//     Foreach (t: s.Nbrs)
//         t.A += ..       -> @s
//
//  Foreach(s: G.Node)
//     s.A += ..           -> @ s  // [note: this can be replace with normal read and write!]
//     Foreach (t: s.Nbrs)
//         s.A += ..       -> @ s  // [note: @t can be used, if above += is replaced with normal R & W)
//     ... 
//
//  Foreach(x: G.Node)
//    Foreach(s: G.Node)
//     Foreach (t: s.Nbrs)
//         t.A += ..       -> @x
//
//  For(s: G.Node)
//     Foreach (t: s.Nbrs)
//         s.A += ..       -> @t
//
//-----------------------------------------------------------------

public class find_bound_t
{
	public gm_symtab v_scope;
	public gm_symtab f_scope;
	public boolean is_par;
	public boolean is_boundary;
	public gm_symtab_entry iter;
}