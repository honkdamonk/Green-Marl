package frontend;

public class gm_type_rule {
	
	public final gm_operator_t opclass;
	public final gm_operator_type_class_t type1;
	public final gm_operator_type_class_t type2;
	public final gm_operator_result_t result_type;
	public final gm_operator_coercion_t coercion_rule;

	public gm_type_rule(gm_operator_t assignOp, gm_operator_type_class_t tCompatible, gm_operator_type_class_t tCompatible2, gm_operator_result_t resultLeft,
			gm_operator_coercion_t coercionRight) {
		opclass = assignOp;
		type1 = tCompatible;
		type2 = tCompatible2;
		result_type = resultLeft;
		coercion_rule = coercionRight;
	}

}