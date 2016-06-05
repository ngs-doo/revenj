package org.revenj.database.postgres.jinq.jpqlquery;

import java.util.Objects;

public class ParameterExpression extends Expression {
	final int lambdaIndex;
	final int argIndex;
	final String argType;

	public ParameterExpression(int lambdaIndex, int argIndex, String argType) {
		this.lambdaIndex = lambdaIndex;
		this.argIndex = argIndex;
		this.argType = argType;
	}

	@Override
	public void generateQuery(QueryGenerationState queryState, OperatorPrecedenceLevel operatorPrecedenceScope) {
		//TODO switch to Postgres $ so params can be reused
		queryState.registerParameter(this, lambdaIndex, argIndex, argType);
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
		ParameterExpression o = (ParameterExpression) obj;
		return lambdaIndex == o.lambdaIndex && argIndex == o.argIndex && Objects.equals(argType, o.argType);
	}

	@Override
	public void visit(ExpressionVisitor visitor) {
		visitor.visitParameter(this);
	}
}
