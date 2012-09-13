package common;

public class gm_dumptree {
	// ----------------------------------------------------------------------------------------
	// For debugging.
	// ----------------------------------------------------------------------------------------
	public static final int TAB_SZ = 2;

	public static void IND(int l) {
		for (int i = 0; i < l; i++)
			for (int j = 0; j < TAB_SZ; j++)
				System.out.print(" ");
	}
}