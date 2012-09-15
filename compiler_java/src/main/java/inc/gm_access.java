package inc;


public enum gm_access { // 16 bit bitmap
	GMACCESS_NONE, //
	GMACCESS_EMPTY, //
	GMACCESS_SHRINK, //
	GMACCESS_GROW, //
	GMACCESS_FULL, //
	GMACCESS_LOOKUP, //
	GMACCESS_COPY;//

	public boolean is_collection_access_none() {
		return (this == gm_access.GMACCESS_NONE);
	}
}