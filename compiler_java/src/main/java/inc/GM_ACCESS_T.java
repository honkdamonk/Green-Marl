package inc;


public enum GM_ACCESS_T { // 16 bit bitmap
	GMACCESS_NONE, //
	GMACCESS_EMPTY, //
	GMACCESS_SHRINK, //
	GMACCESS_GROW, //
	GMACCESS_FULL, //
	GMACCESS_LOOKUP, //
	GMACCESS_COPY;//

	public boolean is_collection_access_none() {
		return (this == GM_ACCESS_T.GMACCESS_NONE);
	}
}