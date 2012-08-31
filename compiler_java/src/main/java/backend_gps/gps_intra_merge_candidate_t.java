package backend_gps;

//------------------------------------------------------------------------------------------------
// Merge BB Intra while 
// (1) Find Simple While Segment 
//    (do-while)
//    -> [HEAD] -> (SEQ0) -> [P1:S1]  -> ...  -> [PN-1:SN-1] -> [PN:SN] -> [TAIL] ->
//         ^                                                                 |
//         +-----------------------------------------------------------------+
//  Or (while)
//         +--------------------------------------------------+
//         |                                                  |
//    -> [HEAD] -> (SEQ0) -> [P1:S1]  -> ...  -> [PN:SN]      V
//         ^                                          | 
//         +------------------------------------------+
//
//
// (2) <Conditions> 
//    - Say, First two is [PAR1]->[SEQ1], last two is [PARN]->[SEQN]
//    - [PAR 1] contains no receive
//    - [PAR N] contains no send
//    - [PAR 1/SEQ 1/SEQ0] does not modify any symbol that is used outside while loop (or arguments). 
//    - There should be no dependency between SN and P1
//    - There should be no dependency between S0 and P1
//    - There should be no dependency between S0 and S1
//
// (3)
//   <after merge>
//   (do-while, with SEQ0)
//          +-----------------------------------+
//          |                                   V
//    -> [HEAD] [P2:S2] ...  -> [PN-1:SN-1] -> (SEQ0) -> [PN/P1:SN/SN] -> [TAIL2] -> [TAIL] ->
//         ^       ^                                                        |        |
//         |       +--------------------------------------------------------+        |
//         +-------------------------------------------------------------------------+
//
//
//   (while, wite SEQ0)
//        +------------------------------------------------------------------------------+
//        |  +----------------------------------------+                                  |
//        |  |                                        V                                  |
//    -> [HEAD]     [P2:S2] ...  -> [PN-1:SN-1] -> (SEQ0) -> [PN/P1:SN/S1] -> [TAIL2]    V
//         ^           ^                                                       |  |
//         |           |-------------------------------------------------------+  |  
//         +----------------------------------------------------------------------+
//         (while_cond -> HEAD)
//
//
// < Especially, when only two states >
//
//    -> [HEAD] -> (SEQ0) -> [PN/P1:SN/SN] -> [TAIL2] -> [TAIL] ->
//         ^         ^                            |        |
//         |         +----------------------------+        |
//         + ----------------------------------------------+
//        
//         +----------------------------------------------+
//         |                                              |
//    -> [HEAD] -> (SEQ0)  -> [PN/P1:SN/S1] -> [TAIL2]    V
//         ^         ^                           |   |
//         |         +---------------------------+   |  
//         +-----------------------------------------+
//
//   PN/SN is 'conditioned' on TAIL2 variable (i.e. checking is_first_execution flag)
//   TAIL2 (is_first_execution) -> go to P2.
//         (otherwise) -> go to HEAD
//    
//------------------------------------------------------------------

public class gps_intra_merge_candidate_t
{
	public gps_intra_merge_candidate_t()
	{
		while_cond = par1 = seq1 = parn = seqn = seq0 = null;
	}
	public gm_gps_basic_block while_cond;
	public gm_gps_basic_block seq0;
	public gm_gps_basic_block par1;
	public gm_gps_basic_block seq1;
	public gm_gps_basic_block parn;
	public gm_gps_basic_block seqn;
}