package org.revenj.database.postgres.jinq.jpqlquery;

public class BinaryExpression extends Expression {
	final Expression left;
	final Expression right;
	final String operator;
	final String prepare;
	final String finish;

	public BinaryExpression(Expression left, String operator, Expression right) {
		this.operator = operator;
		this.prepare = "";
		this.finish = "";
		this.left = left;
		this.right = right;
	}

	public BinaryExpression(String prepare, Expression left, String between, Expression right, String finish) {
		this.operator = between;
		this.prepare = prepare;
		this.finish = finish;
		this.left = left;
		this.right = right;
	}

	@Override
	public void generateQuery(QueryGenerationState queryState, OperatorPrecedenceLevel operatorPrecedenceScope) {
		OperatorPrecedenceLevel precedence = OperatorPrecedenceLevel.forOperator(operator);
		if (!precedence.hasPrecedence(operatorPrecedenceScope))
			queryState.appendQuery("(");
		queryState.appendQuery(prepare);
		left.generateQuery(queryState, precedence);
		queryState.appendQuery(" ").appendQuery(operator).appendQuery(" ");
		// Things on the right hand side should be wrapped in brackets if they are at the same precedence level
		// so we need to pass down a lower precedence scope there.
		right.generateQuery(queryState, precedence.getLevelBelow());
		queryState.appendQuery(finish);
		if (!precedence.hasPrecedence(operatorPrecedenceScope))
			queryState.appendQuery(")");
	}

	@Override
	public void prepareQueryGeneration(
			QueryGenerationPreparationPhase preparePhase,
			QueryGenerationState queryState) {
		left.prepareQueryGeneration(preparePhase, queryState);
		right.prepareQueryGeneration(preparePhase, queryState);
	}

	@Override
	public boolean equals(Object obj) {
		if (!getClass().equals(obj.getClass())) return false;
		BinaryExpression o = (BinaryExpression) obj;
		return operator.equals(o.operator) && left.equals(o.left) && right.equals(o.right);
	}

	@Override
	public void visit(ExpressionVisitor visitor) {
		visitor.visitBinary(this);
	}
}
