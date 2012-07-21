package frontend;

//----------------------------------------------------------
// Phase I : Analyze Read, Write, reDuce set
// In this phase, parallel conflicts are 'not' examined yet. 
// Instead, each sentence's read/write set are examined and 
// merged in recursive way.
//
// (however, doubly bound reductions are checked in this phase)
//----------------------------------------------------------



//-----------------------------------------------------------------
// Read-Write Analysis 
//
//  Build-up RW info
//  Each sentence (or procedure def) keeps following information: 
//    Read-set,  Write-set, Defer/Reduce-Set
//
//  [Example Code]
//      Procedure foo(G:Graph, ... ; Z: Int)  // ...0
//      {                                // ...1
//        Int Y; Y = 0;                  // ...2
//        Foreach(X:G.Nodes)             // ...3
//        {                              // ...4
//            If (X.val2 > 3) {           // ...5
//              Y+= X.val @X;            // ...6
//            } 
//        }
//        Z = Y;                         // ... 7
//      }
//   
//  [Example Result]    
//    (2: Y=0)                   WSET-> (Y:Scala),   (-),  Always
//        (6: Y+=X.val@X)        RSET-> (val:Field), (by X),  Always
//                               DSET-> (Y:Scala),   (-),  Always, (+=, bound X)
//      (5: IF...)               RSET-> (val:Field), (by X),  Cond
//                                      (val2:Field),(by X),  Always
//                               DSET-> (Y:Scala),   (-),  Cond (+=, bound X)
//      (4=5)
//    (3: Foreach)               RSET-> (val:Field),  (Linear), Cond
//                                      (val2:Field), (Linear), Always
//                               WSET-> (Y:Scala),    (-),   Condi
//
//    (7: Z=Y)                   RSET-> (Y:Scala), (-),  Always
//                               WSET-> (Z:Scala), (-),  Always
//  (1: {} )                     RSET-> (val: Field),  (Linear), Cond
//                                      (val2: Field), (Linear), Alwyas
//                                      (Y:Scala), (-),  Always
//                               WSET-> (Y:Scala), (-), Always
//                                      (Z:Scala), (-), Always
//  (0: Proc)                    WSET-> (Z:Scala), (-), Always
//                               RSET-> (val: Field), (Linear), Cond
//                                      (val2: Field), (Linear), Always
//
// Note. Same variable may have multiple WSET/RSET entries
//-----------------------------------------------------------------

public enum gm_range_type_t
{
	GM_RANGE_LINEAR(0), // G.Nodes or G.Edges
	GM_RANGE_RANDOM(1), // G.Nbrs, ...  (or access via non-iterator variable)
	GM_RANGE_SINGLE(2), // t.X, t is a fixed iterator
	GM_RANGE_LEVEL(3), // BFS iteration
	GM_RANGE_LEVEL_UP(4), // BFS iteration, up level
	GM_RANGE_LEVEL_DOWN(5), // BFS iteration, down level
	GM_RANGE_INVALID(6);

	private int intValue;
	private static java.util.HashMap<Integer, gm_range_type_t> mappings;
	private static java.util.HashMap<Integer, gm_range_type_t> getMappings()
	{
		if (mappings == null)
		{
			synchronized (gm_range_type_t.class)
			{
				if (mappings == null)
				{
					mappings = new java.util.HashMap<Integer, gm_range_type_t>();
				}
			}
		}
		return mappings;
	}

	private gm_range_type_t(int value)
	{
		intValue = value;
		gm_range_type_t.getMappings().put(value, this);
	}

	public int getValue()
	{
		return intValue;
	}

	public static gm_range_type_t forValue(int value)
	{
		return getMappings().get(value);
	}
}