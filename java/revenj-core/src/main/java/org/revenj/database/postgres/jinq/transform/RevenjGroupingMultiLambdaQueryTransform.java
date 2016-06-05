package org.revenj.database.postgres.jinq.transform;

import org.revenj.database.postgres.jinq.jpqlquery.JinqPostgresQuery;

public abstract class RevenjGroupingMultiLambdaQueryTransform extends RevenjQueryTransform {

	RevenjGroupingMultiLambdaQueryTransform(RevenjQueryTransformConfiguration config) {
		super(config);
	}

	public <U, V> JinqPostgresQuery<U> apply(JinqPostgresQuery<V> query, LambdaInfo lambda, LambdaInfo[] groupingLambdas, SymbExArgumentHandler parentArgumentScope) throws QueryTransformException {
		return null;
	}
}
