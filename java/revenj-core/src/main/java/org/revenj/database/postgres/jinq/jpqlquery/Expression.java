package org.revenj.database.postgres.jinq.jpqlquery;

public abstract class Expression {
	enum QueryGenerationPreparationPhase {
		FROM
	}

	public void generateQuery(QueryGenerationState queryState, OperatorPrecedenceLevel operatorPrecedenceScope) {
	}

	public abstract void prepareQueryGeneration(QueryGenerationPreparationPhase preparePhase, QueryGenerationState queryState);

	public abstract void visit(ExpressionVisitor visitor);

	@Override
	public boolean equals(Object obj) {
		return getClass().equals(obj.getClass());
	}
}
