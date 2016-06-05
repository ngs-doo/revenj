package org.revenj.database.postgres.jinq.jpqlquery;

import java.util.Objects;

public class ParameterFieldExpression extends Expression {
	final int lambdaIndex;
	final String fieldName;
	final String fieldType;

	public ParameterFieldExpression(int lambdaIndex, String fieldName, String fieldType) {
		this.lambdaIndex = lambdaIndex;
		this.fieldName = fieldName;
		this.fieldType = fieldType;
	}

	@Override
	public void generateQuery(QueryGenerationState queryState, OperatorPrecedenceLevel operatorPrecedenceScope) {
		//TODO switch to Postgres $ so params can be reused
		queryState.registerParameter(this, lambdaIndex, fieldName, fieldType);
		queryState.appendQuery("?");
	}

	@Override
	public void prepareQueryGeneration(
			QueryGenerationPreparationPhase preparePhase,
			QueryGenerationState queryState) {
		// Nothing to do.
	}

	@Override
	public boolean equals(Object obj) {
		if (!getClass().equals(obj.getClass())) return false;
		ParameterFieldExpression o = (ParameterFieldExpression) obj;
		return lambdaIndex == o.lambdaIndex && fieldName.equals(o.fieldName) && Objects.equals(fieldType, o.fieldType);
	}

	@Override
	public void visit(ExpressionVisitor visitor) {
		visitor.visitParameterField(this);
	}
}
