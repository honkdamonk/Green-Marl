package ast;

import static inc.GMTYPE_T.GMTYPE_MAP;
import inc.GMTYPE_T;

public class ast_maptypedecl extends ast_typedecl {

	private ast_typedecl keyType;
	private ast_typedecl valueType;

	private ast_maptypedecl() {
		super();
	}

	public static ast_maptypedecl new_map(ast_typedecl keyType, ast_typedecl valueType) {
		ast_maptypedecl newMap = new ast_maptypedecl();
		newMap.type_id = GMTYPE_MAP;
		newMap.keyType = keyType;
		newMap.valueType = valueType;
		keyType.set_parent(newMap);
		valueType.set_parent(newMap);
		return newMap;
	}

	public ast_typedecl copy() {
		ast_maptypedecl clone = new ast_maptypedecl();
		clone.type_id = type_id;
		clone.keyType = (keyType == null) ? null : keyType.copy();
		clone.valueType = (valueType == null) ? null : valueType.copy();
		clone.line = line;
		clone.col = col;
		clone._well_defined = this._well_defined;
		return clone;
	}

	public void set_key_type(ast_typedecl newKeyType) {
		assert (newKeyType.getTypeSummary().can_be_key_type());
		keyType = newKeyType;
	}

	public ast_typedecl get_key_type() {
		return keyType;
	}

	public ast_typedecl get_value_type() {
		return valueType;
	}

	public void set_value_type(ast_typedecl newValueType) {
		assert (newValueType.getTypeSummary().can_be_key_type());
		valueType = newValueType;
	}

	@Override
	public boolean is_map() {
		return true;
	}

	@Override
	public GMTYPE_T get_typeid() {
		return GMTYPE_MAP;
	}

	@Override
	public GMTYPE_T getTypeSummary() {
		return get_typeid();
	}

	public GMTYPE_T getKeyTypeSummary() {
		assert (keyType != null);
		return keyType.getTypeSummary();
	}

	public GMTYPE_T getValueTypeSummary() {
		assert (valueType != null);
		return valueType.getTypeSummary();
	}

	@Override
	public void reproduce(int indLevel) {
		Out.push("Map <");
		keyType.reproduce(0);
		Out.push(", ");
		valueType.reproduce(0);
		Out.push(">");
	}

};
