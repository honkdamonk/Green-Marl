package frontend;

public class gm_type_rule {
	public gm_operator_t opclass;
	public gm_operator_type_class_t type1;
	public gm_operator_type_class_t type2;
	public gm_operator_result_t result_type;
	public gm_operator_coercion_t coercion_rule;

	public gm_type_rule(gm_operator_t assignOp, gm_operator_type_class_t tCompatible, gm_operator_type_class_t tCompatible2, gm_operator_result_t resultLeft,
			gm_operator_coercion_t coercionRight) {
		this.opclass = assignOp;
		this.type1 = tCompatible;
		this.type2 = tCompatible2;
		this.result_type = resultLeft;
		this.coercion_rule = coercionRight;
	}

}