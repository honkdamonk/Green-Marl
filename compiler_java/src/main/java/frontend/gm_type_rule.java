package frontend;

public class gm_type_rule {
	
	public final gm_operator opclass;
	public final gm_operator_type_class type1;
	public final gm_operator_type_class type2;
	public final gm_operator_result result_type;
	public final gm_operator_coercion coercion_rule;

	public gm_type_rule(gm_operator assignOp, gm_operator_type_class tCompatible, gm_operator_type_class tCompatible2, gm_operator_result resultLeft,
			gm_operator_coercion coercionRight) {
		opclass = assignOp;
		type1 = tCompatible;
		type2 = tCompatible2;
		result_type = resultLeft;
		coercion_rule = coercionRight;
	}

}