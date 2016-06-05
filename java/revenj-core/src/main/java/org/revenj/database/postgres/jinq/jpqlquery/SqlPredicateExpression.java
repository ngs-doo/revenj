package org.revenj.database.postgres.jinq.jpqlquery;

import org.revenj.database.postgres.jinq.transform.LambdaInfo;

import java.util.List;
import java.util.function.Function;

public class SqlPredicateExpression extends Expression {
	final String function;
	final int lambdaIndex;
	final List<GetterExpression> parameters;

	public SqlPredicateExpression(String function, int lambdaIndex, List<GetterExpression> parameters) {
		this.function = function;
		this.lambdaIndex = lambdaIndex;
		this.parameters = parameters;
	}

	@Override
	public void generateQuery(QueryGenerationState queryState, OperatorPrecedenceLevel operatorPrecedenceScope) {
		queryState.appendQuery(function);
		queryState.appendQuery("(");
		queryState.appendQuery(queryState.fromAliases.values().iterator().next());
		for(GetterExpression pf : parameters) {
			queryState.appendQuery(",?");
			Function<LambdaInfo, Object> getValue = li -> pf.getter.apply(li.getCapturedArg(0));
			queryState.parameters.add(new GeneratedQueryParameter(null, lambdaIndex, getValue, pf.javaType, pf.sqlType));
		}
		queryState.appendQuery(")");
	}

	@Override
	public void prepareQueryGeneration(
			QueryGenerationPreparationPhase preparePhase,
			QueryGenerationState queryState) {
	}

	@Override
	public boolean equals(Object obj) {
		if (!getClass().equals(obj.getClass())) return false;
		SqlPredicateExpression o = (SqlPredicateExpression) obj;
		return function.equals(o.function) && parameters.size() == o.parameters.size();
	}

	@Override
	public void visit(ExpressionVisitor visitor) {
		visitor.visitDefaultExpression(this);
	}
}
