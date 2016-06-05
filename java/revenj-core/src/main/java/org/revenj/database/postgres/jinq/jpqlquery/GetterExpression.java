package org.revenj.database.postgres.jinq.jpqlquery;

import java.util.function.Function;

public class GetterExpression extends Expression {
	final Function getter;
	final String javaType;
	final String sqlType;

	public GetterExpression(Function getter, String javaType, String sqlType) {
		this.getter = getter;
		this.javaType = javaType;
		this.sqlType = sqlType;
	}

	@Override
	public void generateQuery(QueryGenerationState queryState, OperatorPrecedenceLevel operatorPrecedenceScope) { }

	@Override
	public void prepareQueryGeneration(QueryGenerationPreparationPhase preparePhase, QueryGenerationState queryState) { }

	@Override
	public boolean equals(Object obj) { return false; }

	@Override
	public void visit(ExpressionVisitor visitor) { }
}
