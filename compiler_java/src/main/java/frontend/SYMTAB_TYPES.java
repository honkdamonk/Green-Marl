package frontend;

public enum SYMTAB_TYPES {
	GM_SYMTAB_ARG, // argument
	GM_SYMTAB_VAR, // variable
	GM_SYMTAB_FIELD, // node/edge property
	GM_SYMTAB_PROC; // procedures

	public int getValue() {
		return ordinal();
	}

}